// $LastChangedDate$
// $Rev$
PNPlaceN {
	var <tokens;

	*new {| anInteger |
		^super.newCopyArgs( anInteger ?? { 0 } );
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

	printOn { arg stream;
		stream << this.class.name << "( " << tokens  << " )";
	}

// Modify method warning so that it can print or not the message?
	warn {| anObject |
		if( anObject.isKindOf( Integer ).not ){
			("\nThe number" + anObject.asString + "is not an integer").warn 
		};
	}
}

PNTransitionN {
	classvar <updateInputPlacesDefault, <updateOutputPlacesDefault, <enabledFunctionDefault;
	var <>inputPlaces, <>outputPlaces, <>inhibitorPlaces; //Sets of PNPlaceN instances or names of PNPlaceNs
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

	*new { | inputPlaces, outputPlaces, inhibitorPlaces, updateInputPlaces, updateOutputPlaces, enabledFunction |
		if( inhibitorPlaces.notNil and: { (inputPlaces.asSet & inhibitorPlaces.asSet).isEmpty.not } ){ 
			"There are  common places in input places and inhibitor places of this transition.".error;
		};
			^super.new.init( inputPlaces, outputPlaces, inhibitorPlaces, updateInputPlaces,	updateOutputPlaces,	enabledFunction );
	}

	init {| inputPlaces, outputPlaces, inhibitorPlaces, updateInputPlaces, updateOutputPlaces, enabledFunction |
		this.inputPlaces_( inputPlaces )
		.outputPlaces_( outputPlaces )
		.inhibitorPlaces_( inhibitorPlaces )
		.updateInputPlaces_( updateInputPlaces ?? { updateInputPlacesDefault.( this.inputPlaces ) } ) 
		.updateOutputPlaces_( updateOutputPlaces ?? { updateOutputPlacesDefault.( this.outputPlaces ) } )
		.enabledFunction_( enabledFunction ?? { enabledFunctionDefault } );
	}

// modify this method to avoid duplicate writings
	// informPlaces {
	// 	var instanceName = this.name, informFunc;
	// 	informFunc = {| aSelector1, aSelector2 | 
	// 		this.perform( aSelector1, false ).do {| place | place.perform( aSelector2 ).add( instanceName ) };
	// 	};
	// 	informFunc.( \inputPlaces, \isInputPlaceTo );
	// 	informFunc.( \outputPlaces, \isOutputPlaceTo );
	// 	informFunc.( \inhibitorPlaces, \isInhibitorPlaceTo );
	// }

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

PetriNetN {
	var places, transitions, <transitionData;

// Each argument corresponds to one transition and is an IdentityDictionary with keys:
// \transition : name, \inputPlaces: setOfPlaceNames or nil, \outputPlaces: setOfOutputNames or nil,
// \inhibitorPlaces: setOfInhibitorPlaces or nil, \updateInputPlaces: aFunction(optional),
// \updateOutputPlaces: aFunction(optional), \clockSpeed: aFunction(optional), \clock: aFunction(optional, \isTimed: aBoolean or nil)
	*new {| ... dictionaries |
		^ super.new.init( dictionaries );
	}
	// is there a better data structure for the following?
	init {| dictionaries | 
		places = IdentityDictionary[];
		transitions = IdentityDictionary[];
		dictionaries.do {| aDict |
			this.prAddPlaces( aDict )
			.prAddTransition( aDict );
		};
		transitionData = Dictionary.newFrom(
			dictionaries.collect {| aDict |
				[ aDict.removeAt( \transition ), aDict ]
			}.flat
		)
	}

	prAddTransition {| aDict |
		var transitionName;
		transitionName = aDict.at( \transition );
		// this.prAddToDict( transitions, transitionName, PNTransitionN.performWithEnvir( aDict ) );
		transitions.put( transitionName, PNTransitionN.performWithEnvir( \init, aDict ) );
	}

	prAddPlaces {| aDict |
		[ \inputPlaces, \inhibitorPlaces, \outputPlaces ].do { | aSymbol |
			this.prAddPlacesBasic( aDict.at( aSymbol ) );
		}
	}

	prAddPlacesBasic {| anArray |
		var place;
		anArray.do {| aSymbol |
			place = PNPlaceN.new;
			this.prAddToDict( places, aSymbol, place );
		};
	}

	prAddToDict {| aDict, aSymbol, anObject |
		if( aDict.includesKey( aSymbol ).not ){ aDict.put( aSymbol, anObject ); }
	}

	places {| onlyNames = true |
		^ this.prGetObjects( places, onlyNames )
	}

	transitions {| onlyNames = true |
		^ this.prGetObjects( transitions, onlyNames )
	}	

	prGetObjects { | aDictionary, aBoolean |
		^ if( aBoolean ){
			aDictionary.keys.as( Array )
		}{
			aDictionary.getPairs.as( Event )
		}
	}

	marking {
		^ places.collect {| place | place.tokens }.as(Event)
	}

	setMarking {| anIdentityDictionary |
		anIdentityDictionary.keysValuesDo {| key, val |
			if( places.includesKey( key ) ){
				places.at( key ).tokens_( val )
			}
		};
		^ Post<< "The new marking is\n\t " << this.marking << "\n";
	}

	inputPlacesOf {| aSymbol | ^transitionData.at( aSymbol ).at( \inputPlaces ) }
	outputPlacesOf {| aSymbol | ^transitionData.at( aSymbol ).at( \outputPlaces ) }
	inhibitorPlacesOf {| aSymbol | ^transitionData.at( aSymbol ).at( \inhibitorPlaces ) }

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
		enabledTransitions.do {| aSymbol | petriNet.transitions.at( aSymbol ).fire( this ); }
	}
	//end of algorithm	
}

PNPatternN : Pattern {
	var petriNet, dictionary, length, marking, samplePath; // change the name of dictionary?

	*new {| aPetriNet, aDictionary, length = inf, marking |
		^ super.newCopyArgs( aPetriNet, aDictionary, length, marking ).init // replace copy method with something else?
	}

	init {
		dictionary = dictionary.keysValuesChange {| key, val | val.asStream };
		if( marking.notNil ){ petriNet.setMarking( marking ) };
	}

	storeArgs { ^ [ petriNet, dictionary, length, marking ] }

	embedInStream {| inval |
		var samplePath, transitions;

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