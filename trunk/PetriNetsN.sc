// $LastChangedDate$
// $Rev$
PNPlaceN {
	var <tokens; // don't store Environment in a variable?

	*new {|  anInteger |
		^ super.newCopyArgs( anInteger ?? { 0 } )
	}

	tokens_ { | anInteger |
		this.warn( anInteger );		// remove this line?
		tokens = anInteger;
	}

	addOneToken { tokens = tokens + 1 }

	removeOneToken { tokens = tokens - 1 }//allow negative number of tokens

	addTokens { | anInteger |
		this.warn( anInteger );
		tokens = tokens + anInteger;
	}

	removeTokens { | anInteger | 
		this.warn( anInteger );
		tokens = tokens - anInteger;
	}

	isEmpty { ^ tokens == 0 }

	gui {| aWindow | }

// Modify method warning so that it can print or not the message?
	warn {| anObject |
		if( anObject.isKindOf( Integer ).not ){
			("\nThe number" + anObject.asString + "is not an integer").warn 
		};
	}
}



// look again instance method 'pnEnvironment'
// discriminate between places and transitions?
// if a place and a transition have the same name, one is ovewriten
// OR
// don't store pnEnvironment in each instance. Just pass it over so that an instance
// can register in the Environment
PNTransitionN {
	classvar <updateInputPlacesDefault, <updateOutputPlacesDefault, <enabledFunctionDefault;
	var  inputPlaces, outputPlaces, inhibitorPlaces; //Sets of PNPlaceN instances or names of PNPlaceNs
	var <>updateInputPlaces, <>updateOutputPlaces; //Functions with second arg a SPetriNet ( first for clockSpeed )
	var <>enabledFunction;										 // a Function with args | inputPlaces, inhibitorPlaces | and values true - false

	*initClass{
		updateInputPlacesDefault  = {| aSet | { aSet.do { |elem| elem.removeOneToken } } };
		updateOutputPlacesDefault = {| aSet | { aSet.do { |elem| elem.addOneToken } } };
		enabledFunctionDefault = {| inputPlaces, inhibitorPlaces |
			//transition is enabled when all input places contain at least one token
			//and all inhibitor places contain no tokens. The message asCollection added 
			//to prevent nil sets of places
			inputPlaces.asCollection.collect{ |elem| elem.tokens != 0 }.every({| elem | elem == true })
			//		 inputPlaces.asCollection.collect{ |elem| elem.tokens > 0 }.every({| elem | elem == true }) // if tokens is positive integer
			and: 
			{ inhibitorPlaces.asCollection.collect{ |elem| elem.tokens == 0 }.every({| elem | elem  == true }) }
		};
	}

	*new { | inputPlaces, outputPlaces, inhibitorPlaces, updateInputPlaces, updateOutputPlaces, enabledFunction, anEnvir |
		var transition;
		// look again this message - symbols - places
		if( inhibitorPlaces.notNil and: { (inputPlaces.asSet & inhibitorPlaces.asSet).isEmpty.not } ){
			"There are  common places in input places and inhibitor places of this transition.".error;
		};
		^super.new.init( inputPlaces, outputPlaces, inhibitorPlaces, updateInputPlaces, updateOutputPlaces, enabledFunction );
	}

	init {| inputPlaces, outputPlaces, inhibitorPlaces, updateInputPlaces, updateOutputPlaces, enabledFunction |
		this.inputPlaces_( inputPlaces )
		.outputPlaces_( outputPlaces )
		.inhibitorPlaces_( inhibitorPlaces )
		.updateInputPlaces_( updateInputPlaces ?? { updateInputPlacesDefault.( this.inputPlaces ) } ) 
		.updateOutputPlaces_( updateOutputPlaces ?? { updateOutputPlacesDefault.( this.outputPlaces ) } )
		.enabledFunction_( enabledFunction ?? { enabledFunctionDefault } );
	}

	inputPlaces_ {| aCollection |
		inputPlaces = this.prCollectPlaceInstances( aCollection )
	}

	inputPlaces {| aBoolean = false | ^ this.prGetPlaces( inputPlaces, aBoolean ) }

	outputPlaces_ {| aCollection |
		outputPlaces = this.prCollectPlaceInstances( aCollection )
	}

	outputPlaces {| aBoolean = false | ^ this.prGetPlaces( outputPlaces, aBoolean ) }

	inhibitorPlaces_ {| aCollection |
		inhibitorPlaces = this.prCollectPlaceInstances( aCollection )
	}

	inhibitorPlaces {| aBoolean = false | ^ this.prGetPlaces( inhibitorPlaces, aBoolean ) }

	prCollectPlaceInstances {| aCollection |
		var place;
		^ aCollection.collect {| elem | 
			if( elem.isKindOf( Symbol ) ){ // modify to pass tokens after name e.x [ \p0, 10, \p1, \p2]
				place = currentEnvironment.at( elem );
				if( place.isNil ){ place = PNPlaceN( elem ); };
				place
			}{
				// check for other objects?
				elem
			}
		}
	}

	prGetPlaces {| aCollection, aBoolean = false |
		// aBoolean: if true, get symbols, otherwise get SPNPlace instances
		^ if( aBoolean ){
			aCollection.collect {| elem | elem.name }
		}{
			aCollection
		}
	}

	fire {| aPetriNetN |
		this.updateInputPlaces.( inputPlaces, aPetriNetN ); // args marking, currentTime ?
		this.updateOutputPlaces.( outputPlaces, aPetriNetN );
	}

	isEnabled {
		^enabledFunction.( inputPlaces, inhibitorPlaces )
	}

	neutralize {						// remove this method?
		#inputPlaces, inhibitorPlaces, outputPlaces  = nil ! 3;
		updateInputPlaces = updateInputPlacesDefault;
		updateOutputPlaces = updateOutputPlacesDefault;
	}

	gui { | aWindow | }

	// printOn { arg stream;
	// 	stream << this.class.name << "( "<< this.name << " )";
	// }
}

// PetriNetN as a subclass of IdentityDictionary. It stores every node 
// as a key with the given name and sets its value to the corresponding object ( place - transition ).
// Thus, places and transitions must have unique names.
PetriNetN : IdentityDictionary {
	var <places, <transitions;

// Each argument corresponds to one transition and is an IdentityDictionary with keys:
// \transition : name, \inputPlaces: setOfPlaceNames or nil, \outputPlaces: setOfOutputNames or nil,
// \inhibitorPlaces: setOfInhibitorPlaces or nil, \updateInputPlaces: aFunction(optional),
// \updateOutputPlaces: aFunction(optional), \clockSpeed: aFunction(optional), \clock: aFunction(optional, \isTimed: aBoolean or nil)
	*new {| ... dictionaries |
		^ super.new.init( dictionaries );
	}

	init {| dictionaries |
		dictionaries.do {| aDict |
			this.prAddPlaces( aDict )
			.prAddTransition( aDict );
		};
		places = this.select {| value, key | value.isKindOf( PNPlaceN ) }.as(Event);
		transitions = this.select {| value, key | value.isKindOf( PNTransitionN ) }.as(Event)
	}

	prAddTransition {| aDict |
		var name;
		name = aDict.removeAt( \transition );
		this.put( PNTransitionN.new.performWithEnvir( \init, aDict ) );
	}

	prAddPlaces {| aDict |
		[ \inputPlaces, \inhibitorPlaces, \outputPlaces ].do { | aSymbol |
			this.prAddPlacesBasic( aDict.at( aSymbol ) );
		}
	}

	prAddPlacesBasic {| anArray |
		var i = 0, aSymbol, tokens, size;
		size = anArray.size;
		while { 
			i < size
		}{
			# aSymbol, tokens = anArray.copyRange( i, i + 1 );
			if ( tokens.isKindOf( SimpleNumber ) ){
				this.put( aSymbol, PNPlaceN( tokens ) );
				i = i + 2;
			}{
				this.put( aSymbol , PNPlaceN( 0 ) );
				i = i + 1;
			}
		}
	}

	// what is this for???
	prAddToDict {| aDict, aSymbol, anObject |
		if( aDict.includesKey( aSymbol ).not ){ aDict.put( aSymbol, anObject ); }
	}

	// places {| onlyNames = true |
	// 	^ this.prGetObjects( PNPlaceN, onlyNames )
	// }

	// transitions {| onlyNames = true |
	// 	^ this.prGetObjects( PNTransitionN, onlyNames )
	// }	

	// prGetObjects {|  class, onlyNames = true |
	// 	var post;
	// 	post = pnEnvironment.select {| value, key |
	// 		value.isKindOf( class }
	// 	};
	// 	^ if( onlyNames ){
	// 		post.keys
	// 	}{
	// 		post.as(Event)
	// 	}
	// }

	marking {
		^ this.places.collect {| place | place.tokens }
	}

	setMarking {| anIdentityDictionary |
		anIdentityDictionary.keysValuesDo {| key, val |
			if( places.includesKey( key ) ){
				this.at( key ).tokens_( val )
			}
		};
	}

	inputPlacesOf {| aSymbol | ^ this.at( aSymbol ).inputPlaces.collect {| p | p.name } }
	outputPlacesOf {| aSymbol | ^ this.at( aSymbol ).outputPlaces.collect {| p | p.name } }
	inhibitorPlacesOf {| aSymbol | ^ this.at( aSymbol ).inhibitorPlaces.collect {| p | p.name } }

}

PNSamplePath {
	var >petriNet;
	// add setter - getter methods?
	var <transitions, <enabledTransitions;
	var <b1;

	*new {| aPetriNetN |
		^ super.new.petriNet_( aPetriNetN ).init
	}

	init {
		transitions = petriNet.transitions.keys.asArray;
		b1 = IdentityDictionary[];
		this.makeB1;
	}

	makeB1 {
		var unionOfB1;					// change this name
		b1.clear;
		unionOfB1 = Set[];
		transitions.do {| transition |
			unionOfB1.clear;
			transitions.do {| trans |
				if( 
					( petriNet.outputPlacesOf( transition ).as(Set) & petriNet.inputPlacesOf( trans ).as(Set) ).notEmpty 
					or: 
					{ ( petriNet.inputPlacesOf( transition ).as(Set) & petriNet.inhibitorPlacesOf( trans ).as(Set) ).notEmpty } ){ 
						unionOfB1.add( trans );
					};
			};
			if( unionOfB1.notEmpty ){ b1.put( transition, unionOfB1.asArray ) };
		};
	}

	//the algorithm to generate a sample for the underlying process of the PetriNetN
	// from Chapter 3 of "Stochastic Petri Nets: Modelling, Stability, Simulation" by Peter Haas
	//step 1:
	computeInitEnabledTransitions {
		enabledTransitions = Set[];
		transitions.do {| aSymbol |
			// if petriNet is a subclass of IdentityDictionary then use
			// petriNet[ aSymbol ].isEnabled
			// assuming that places and transitions don't have common names
			if( petriNet.transitions.at( aSymbol ).isEnabled ){
				enabledTransitions.add( aSymbol );
			}
		};
	}		
	computeEnabledTransitions {
		var candidateTrans;				// make this an instance var? is it significantly faster?
		candidateTrans = b1.select {| value, key |
			enabledTransitions.includes( key )
		}
		.values.flat.asSet;
		enabledTransitions.clear;
		candidateTrans.do {| aSymbol |
			if( petriNet.transitions.at( aSymbol ).isEnabled ){ 
				enabledTransitions.add( aSymbol );
			}
		};
	}
	//step 4:
	generateNewMarking {
		enabledTransitions.do {| aSymbol | petriNet.transitions.at( aSymbol ).fire( petriNet ); }
	}
	//end of algorithm	
}

PNPatternN : Pattern {
	var petriNet, dictionary, marking, length, samplePath; // change the name of dictionary?

	*new {| aPetriNet, aDictionary, marking, length = inf |
		^ super.newCopyArgs( aPetriNet, aDictionary, marking, length )
	}

	storeArgs { ^ [ petriNet, dictionary, length, marking ] }

	embedInStream {| inval |
		var samplePath, transitions;

		dictionary.keysValuesChange {| key, val | val.asStream };
		if( marking.notNil ){ petriNet.setMarking( marking ) };

		samplePath = PNSamplePath( petriNet );

		samplePath.computeInitEnabledTransitions;

		length.value.do {
			inval = samplePath.enabledTransitions.collect {| aSymbol |
				dictionary.at( aSymbol ).next( inval ) // or use embedInStream?
			}.asArray;

			if( inval.size == 1 ){ inval = inval.pop }; // better approach for this?

			inval = inval.yield;

			samplePath.generateNewMarking
			.computeEnabledTransitions;
			};
		^inval
	}
}