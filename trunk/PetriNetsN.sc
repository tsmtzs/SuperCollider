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
			( "There are  common Places in InputPlaces and InhibitorPlaces of transition" + key.asString ).error;
		};
			^super.newCopyArgs( 
				inputPlaces, 
				outputPlaces, 
				inhibitorPlaces, 
				updateInputPlaces ?? { updateInputPlacesDefault.( this.inputPlaces ) }, 
				updateOutputPlaces ?? { updateOutputPlacesDefault.( this.outputPlaces ) }, 
				enabledFunction ?? { enabledFunctionDefault }
			);
	}


	// *basicNew {| key |
	// 	^super.new
	// 	.instVarPut( \name, key )
	// 	.prAdd
	// }

	// inputPlaces_ {| inputPlacesArray |
	// 	this.prSetIfNotNil( \inputPlaces, inputPlacesArray );
	// }

	// inputPlaces {| aBoolean = false |
	// 	^ this.prGetPlaces( inputPlaces, aBoolean );
	// }

	// inhibitorPlaces_ {| inhibitorPlacesArray |
	// 	this.prSetIfNotNil( \inhibitorPlaces, inhibitorPlacesArray );
	// }

	// inhibitorPlaces {| aBoolean = false |
	// 	^ this.prGetPlaces( inhibitorPlaces, aBoolean );
	// }

	// outputPlaces_ {| outputPlacesArray |
	// 	this.prSetIfNotNil( \outputPlaces, outputPlacesArray );
	// }

	// outputPlaces {| aBoolean = false |
	// 	^ this.prGetPlaces( outputPlaces, aBoolean );
	// }

	// prSetIfNotNil {| aSymbol, aCollection |
	// 	if( aCollection.notNil ){
	// 		this.instVarPut( aSymbol , this.prCollectPlaceInstances( aCollection ) )
	// 	}
	// }

	// prCollectPlaceInstances {| aCollection |
	// 	^ aCollection.collect {| elem | 
	// 		if( elem.isKindOf( Symbol ) ){ 
	// 			PNPlaceN( elem );
	// 		}{
	// 			elem
	// 		}
	// 	}
	// }

	// prGetPlaces {| aCollection, aBoolean = false |
	// 	// aBoolean: if true, get symbols, otherwise get PNPlaceN instances
	// 	^ if( aBoolean ){
	// 		aCollection.collect {| elem | elem.name }
	// 	}{
	// 		aCollection
	// 	}
	// }

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
	var places, transitions, <graph;

// Each argument corresponds to one transition and is an IdentityDictionary with keys:
// \transition : name, \inputPlaces: setOfPlaceNames or nil, \outputPlaces: setOfOutputNames or nil,
// \inhibitorPlaces: setOfInhibitorPlaces or nil, \updateInputPlaces: aFunction(optional),
// \updateOutputPlaces: aFunction(optional), \clockSpeed: aFunction(optional), \clock: aFunction(optional, \isTimed: aBoolean or nil)
	*new {| ... dictionaries |
		^ super.new.init( dictionaries );
	}

	init {| dictionaries | 
		places = IdentityDictionary[];
		transitions = IdentityDictionary[];
		graph = dictionaries;
		dictionaries.do {| aDict |
			this.prAddPlaces( aDict )
			.prAddTransition( aDict );
		}
	}

	prAddTransition {| aDict |
		var transitionName;
		transitionName = aDict.at( \transition );
		// this.prAddToDict( transitions, transitionName, PNTransitionN.performWithEnvir( aDict ) );
		transitions.put( transitionName, PNTransitionN.performWithEnvir( \new, aDict ) );
	}

	prAddPlaces {| aDict |
		[ \inputPlaces, \inhibitorPlaces, \outputPlaces ].do { | aSymbol|
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
		^ if( onlyNames ){
			places.keys.as( Array )
		}{
		places.getPairs.as( Event )
		}
	}

	transitions {| onlyNames = true |
		^ if( onlyNames ){
			transitions.keys.as( Array )
		}{
		transitions.getPairs.as( Event )
		}
	}	

	marking {
		^ places.collect {| place | place.tokens }.as(Event)
	}

	setMarking {| anIdentityDictionary |
		anIdentityDictionary.keysValuesDo {| key, val |
			if( places.includesKey( key ){
				places.at( key ).tokens_( val )
			}	
		};
			^ Post<< "The new marking is\n\t " << this.marking << "\n";
			
		}
	}

	outputPlacesOf {| aSymbol | }
}

PNSamplePath {
	var >petriNet;
	// add setter - getter methods?
	var <transitions, <enabledTransitions;
	var <unionOfB1;

	*new {| aPetriNetN |
		^ super.new.petriNet_( aPetriNetN ).init
	}

	init {
		transitions = petriNet.transitions;
		this.makeB1;
	}

	makeB1 {
		var transName;
		unionOfB1 = Set[];
		transitions.do {| transition |
			transName = transition.name;
			transitions.do {| trans |
				if( 
					( transition.outputPlaces.as(Set) & trans.inputPlaces.as(Set) ).notEmpty 
					or: 
					{ ( transition.inputPlaces.as(Set) & trans.inhibitorPlaces.as(Set) ).notEmpty } ){ 
						unionOfB1.add( trans );
					};
			};
		};
	}

	//the algorithm to generate a sample for the underlying process of the PetriNetN
	// from Chapter 3 of "Stochastic Petri Nets: Modelling, Stability, Simulation" by Peter Haas
	//step 1:
	computeEnabledTransitions {
		enabledTransitions = Set[];
		transitions.do {| e |
			if( e.isEnabled ){ 
				enabledTransitions.add( e );
			}
		};
	}
	//step 4:
	generateNewMarking {
		enabledTransitions.do {|e| e.fire( this ); }
	}
	//end of algorithm	
}

PNPatternN : Pattern {
	var petriNet, dictionary, length, samplePath; // change the name of dictionary?

	*new {| aPetriNet, aDictionary, length = inf |
		^ super.newCopyArgs( aPetriNet, aDictionary.copy, length ).init // replace copy method with something else?
	}

	init {
		dictionary = dictionary.keysValuesChange {| key, val | val.asStream };
	}

	storeArgs { ^ [ petriNet, dictionary, length ] }

	embedInStream {| inval |
		var samplePath, transitions;

		samplePath = PNSamplePath( petriNet ); // remove message 'copy' ?

		length.value.do {
			samplePath.computeEnabledTransitions
			.generateNewMarking;

			inval = samplePath.enabledTransitions.collect {|e|
				dictionary.at( e.name ).next( inval )
			}.asArray;

			if( inval.size == 1 ){ inval = inval.pop }; // better approach for this?

			inval = inval.yield;			
			};
		^inval
	}
}