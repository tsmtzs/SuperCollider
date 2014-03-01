// $LastChangedDate$
// $Rev$
PNPlaceN {
	classvar <>all;
	var <tokens, <>name;
	var <isInhibitorPlaceTo, <isInputPlaceTo, <isOutputPlaceTo;//arrays of Transitions

	*initClass { 
		all = IdentityDictionary.new;
	}

	//methods *new, *at and prAdd from Pdef

	// global storage
	*at { arg key;
		^this.all.at(key)
	}

	*new {| key, anInteger |
		var place;
		place = this.at( key );
		if( place.isNil){
			place = super.newCopyArgs( anInteger ?? { 0 }, key ).prAdd.init;
		}{
			if( anInteger.notNil ){ place.tokens_( anInteger ) };
		}
		^place
	}

	*clearAll {
		this.all.clear;
	}

	init {
		isInputPlaceTo = List[];
		isInhibitorPlaceTo = List[];
		isOutputPlaceTo = List[];
	}

	prAdd {
		all.put( name, this );
		// name = argKey;
	}

	tokens_ { | anInteger |
		this.warning( anInteger );		// remove this line?
		tokens = anInteger;
	}

	addOneToken { tokens = tokens + 1 }

	removeOneToken { tokens = tokens - 1 }//allow negative number of tokens

	addTokens { | anInteger |
		this.warning( anInteger );
		tokens = tokens + anInteger;
	}

	removeTokens { | anInteger | 
		this.warning( anInteger );
		tokens = tokens - anInteger;
	}

	isEmpty { ^ tokens == 0 }

	gui {| aWindow | }

	printOn { arg stream;
		stream << this.class.name << "( "<< name <<" , " << tokens  << " )";
	}

// Modify method warning so that it can print or not the message?
	warning {| anObject |
		if( anObject.isKindOf( Integer ).not ){
			("\nPlace"+this.name.asString+anObject.asString + "is not an integer").warn 
		};
	}
}

PNTransitionN {
	classvar <>all;
	classvar <updateInputPlacesDefault, <updateOutputPlacesDefault, <enabledFunctionDefault, <clockSpeedDefault;
	var inputPlaces, inhibitorPlaces, outputPlaces; //Sets of PNPlaceN instances or names of PNPlaceNs
	var <>updateInputPlaces, <>updateOutputPlaces; //Functions with second arg a SPetriNet ( first for clockSpeed )
	var <>enabledFunction;										 // a Function with args | inputPlaces, inhibitorPlaces | and values true - false
	var <>name, <>spnMediator;
	var <currentState;				// put this var in subclass SPNTimedTransition only?

	*initClass{
		all = IdentityDictionary.new;
		updateInputPlacesDefault  = {| aSet | { aSet.do { |elem| elem.removeOneToken } } };
		updateOutputPlacesDefault = {| aSet | { aSet.do { |elem| elem.addOneToken } } };
		enabledFunctionDefault = {| inputPlaces, inhibitorPlaces |
			//transition is enabled when all input places contain at least one token
			//and all inhibitor places contain no tokens. The message asCollection added 
			//to prevent nil sets of places
			inputPlaces.asCollection.collect{ |elem| elem.tokens != 0 }.every( _ == true )
			//		 inputPlaces.asCollection.collect{ |elem| elem.tokens > 0 }.every( _ == true ) // if tokens is positive integer
			and: 
			{ inhibitorPlaces.asCollection.collect{ |elem| elem.tokens == 0 }.every( _ == true ) }
		};
	}

	*new { | key, inputPlaces, outputPlaces, inhibitorPlaces, updateInputPlaces, updateOutputPlaces, enabledFunction |
		var transition;
		transition = this.at( key );
		if( inhibitorPlaces.notNil and: { (inputPlaces.asSet & inhibitorPlaces.asSet).isEmpty.not } ){ 
			^( "There are  common Places in InputPlaces and InhibitorPlaces of transition" + key.asString ).error;
		};
		if( transition.isNil ){
			transition = this.basicNew( key ).init( inputPlaces, outputPlaces, inhibitorPlaces, updateInputPlaces, updateOutputPlaces, enabledFunction );
		}{
			if(
				[ inputPlaces, outputPlaces, inhibitorPlaces, updateInputPlaces, updateOutputPlaces, enabledFunction ].any {| elem | elem.notNil }
			){
				transition.init( inputPlaces, outputPlaces, inhibitorPlaces, updateInputPlaces, updateOutputPlaces, enabledFunction );
			}
		}
		^transition
	}

	// global storage
	*at { arg key;
		^this.all.at(key)
	}

	*clearAll {
		this.all.clear;
	}

	*basicNew {| key |
		^super.new
		.instVarPut( \name, key )
		.prAdd
	}
		
	init { | inputPlaces, outputPlaces, inhibitorPlaces, updateInputPlaces, updateOutputPlaces, enabledFunction |
		this.inputPlaces_( inputPlaces )
		.inhibitorPlaces_( inhibitorPlaces )
		.outputPlaces_( outputPlaces )
		.updateInputPlaces_( updateInputPlaces ?? { updateInputPlacesDefault.( this.inputPlaces ) }  )
		.updateOutputPlaces_( updateOutputPlaces ?? { updateOutputPlacesDefault.( this.outputPlaces ) } )
		.enabledFunction_( enabledFunction ?? { enabledFunctionDefault } )
		.informPlaces;
	}

	inputPlaces_ {| inputPlacesArray |
		this.prSetIfNotNil( \inputPlaces, inputPlacesArray );
	}

	inputPlaces {| aBoolean = false |
		^ this.prGetPlaces( inputPlaces, aBoolean );
	}

	inhibitorPlaces_ {| inhibitorPlacesArray |
		this.prSetIfNotNil( \inhibitorPlaces, inhibitorPlacesArray );
	}

	inhibitorPlaces {| aBoolean = false |
		^ this.prGetPlaces( inhibitorPlaces, aBoolean );
	}

	outputPlaces_ {| outputPlacesArray |
		this.prSetIfNotNil( \outputPlaces, outputPlacesArray );
	}

	outputPlaces {| aBoolean = false |
		^ this.prGetPlaces( outputPlaces, aBoolean );
	}

	prAdd {
		all.put( this.name, this );
	}

	prSetIfNotNil {| aSymbol, aCollection |
		if( aCollection.notNil ){
			this.instVarPut( aSymbol , this.prCollectPlaceInstances( aCollection ) )
		}
	}

	prCollectPlaceInstances {| aCollection |
		^ aCollection.collect {| elem | 
			if( elem.isKindOf( Symbol ) ){ 
				PNPlaceN( elem );
			}{
				elem
			}
		}
	}

	prGetPlaces {| aCollection, aBoolean = false |
		// aBoolean: if true, get symbols, otherwise get PNPlaceN instances
		^ if( aBoolean ){
			aCollection.collect {| elem | elem.name }
		}{
			aCollection
		}
	}

// modify this method to avoid duplicate writings
	informPlaces {
		var instanceName = this.name, informFunc;
		informFunc = {| aSelector1, aSelector2 | 
			this.perform( aSelector1, false ).do {| place | place.perform( aSelector2 ).add( instanceName ) };
		};
		informFunc.( \inputPlaces, \isInputPlaceTo );
		informFunc.( \outputPlaces, \isOutputPlaceTo );
		informFunc.( \inhibitorPlaces, \isInhibitorPlaceTo );
	}

	fire {| aPetriNetN |
		this.updateInputPlaces.( inputPlaces, aPetriNetN ); // args marking, currentTime ?
		this.updateOutputPlaces.( outputPlaces, aPetriNetN );
	}

	isTimed { ^false }

	isEnabled {
		^enabledFunction.( inputPlaces, inhibitorPlaces )
	}

	neutralize {						// remove this method?
		#inputPlaces, inhibitorPlaces, outputPlaces  = nil ! 3;
		updateInputPlaces = updateInputPlacesDefault;
		updateOutputPlaces = updateOutputPlacesDefault;
	}

	gui { | aWindow | }

	printOn { arg stream;
		stream << this.class.name << "( "<< this.name << " )";
	}
}

PNTimedTransitionN : PNTransitionN {
	var <>clock, <>clockReading; // clock is a function with first arg a PetriNetN, and clockReading is a value of this function
	var <>clockSpeed;				// a function. Remove this var, assuming that clock speed is 1?
	var currentState;

	*new { | key, inputPlaces, outputPlaces, inhibitorPlaces, clock, updateInputPlaces, updateOutputPlaces,  enabledFunction, clock, clockSpeed |
		^super.new( key, inputPlaces, outputPlaces, inhibitorPlaces, updateInputPlaces, updateOutputPlaces, enabledFunction )
		.clock_( clock ?? { 0 } )		// a clock with value 0 gives an immediate transition
		.clockSpeed_( clockSpeed ?? { 1 } )
	}

	isTimed { ^true }
	isImmediate { ^ if( clock == 0 ){ true }{ false } }

	neutralize {
		super.neutralize
		.clock_( 0 )					// 0 for immediate transition
		.clockSpeed_( 1 )
	}

	gui {| aWindow | }
}

PetriNetN {
	var <places, <transitions, firingTransitions, oldTransitions, transitionsB1;
	var newTransitions, immediateTransitions, enabledTransitions, unionOfB1;
	var <currentTime, <>timeOffset, <holdingTime;
	var <timeDurPairs;

// Each argument corresponds to one transition and is an IdentityDictionary with keys:
// \transition : name, \inputPlaces: setOfPlaceNames or nil, \outputPlaces: setOfOutputNames or nil,
// \inhibitorPlaces: setOfInhibitorPlaces or nil, \updateInputPlaces: aFunction(optional),
// \updateOutputPlaces: aFunction(optional), \clockSpeed: aFunction(optional), \clock: aFunction(optional, \isTimed: aBoolean or nil)
	*new {| ... dictionaries |
		^ super.new.init( dictionaries );
	}

	init {| dictionaries | 
		places = List[];
		transitions = List[];
		transitionsB1 = IdentityDictionary[];
		dictionaries.do {| aDict |
			this.prAddPlaces( aDict )
			.prAddTransition( aDict );
		}
	}

	prAddTransition {| aDict |
		var transition, transitionName, isTimed;
		transitionName = aDict.at( \transition );
		transition = PNTransitionN.at( transitionName );
		if( transition.isNil ){
			isTimed = aDict.at( \clock ).notNil or: { aDict.trueAt( \isTimed ) };
			transition = [ PNTransitionN, PNTimedTransitionN ]
			.at( isTimed.asInteger )
			.basicNew( transitionName )
		};
		this.prAddToList( transitions, transition );
		[ \isTimed, \transition ].do {| aSymbol |
			aDict.removeAt( aSymbol );
		};
		transition.performWithEnvir( \init, aDict );
	}

	prAddPlaces {| aDict |
		[ \inputPlaces, \inhibitorPlaces, \outputPlaces ].do { | aSymbol|
			this.prAddPlacesBasic( aDict.at( aSymbol ) );
		}
	}

	prAddPlacesBasic {| anArray |
		var place;
		anArray.do {| aSymbol |
			place = PNPlaceN( aSymbol );
			this.prAddToList( places, place );
		};
	}

	prAddToList {| aList, anObject |
		if( aList.includes( anObject ).not ){ aList.add( anObject ); }
	}

	marking {
		^ this.prCollectAsEvent( places, \name, \tokens )
	}
	
	clockReadings {
		^ this.prCollectAsEvent( transitions, \name, \clockReading )
	}

	prCollectAsEvent {| aCollection, aSelectorA, aSelectorB |
		^ aCollection.collect {| anObject |
			[ anObject.perform( aSelectorA ), anObject.perform( aSelectorB ) ]
		}
		.flatten
		.as(Event)
	}

	setMarking {| anIdentityDictionary |
		anIdentityDictionary.keysValuesDo {| key, value |
			// change the boolean test in 'if' with the return value of a method
			// from a class named SPNUtilities? This class will haave as methods all
			// the private methods of PNPlaceN, PNImmediateTransitionN.
			// Specifically, for this method you use the method 'prGetPlaces'
			// if( places.collect {| place | place.name }.includes( key ).not ){ 
			if( places.every( _.name != key ){
				^ ( "Petri net"+this.name.asString+", doesn't have place"+key.asString ).error;
			};
			PNPlaceN( key, value );
		};
		^ Post << "The new marking is\n\t " << this.marking << "\n";
	}
}

// PNMediatorN {
// 	var <>show;

// 	*new {| ...aSymbol |
// 		^super.newCopyArgs( aSymbol )
// 	}

// 	transitionChanged {| aSPNTransition, what ...moreArgs |
// 		show.do {| aSymbol | 
// 			aSPNTransition.dependants.at( aSymbol ).perform( what, *moreArgs );
// 		}
// 	}
// }

PNSamplePathN {
	var >petriNet;
	// add setter - getter methods?
	var <transitions, <enabledTransitions, <oldTransitions, <newTransitions, <firingTransitions;
	var unionOfB1, <currentTime, <holdingTime;

	*new {| aPetriNetN |
		^ super.new.petriNet_( aPetriNet ).init
	}

	init {
		transitions = petriNet.transitions.array.copy;
	}

	// delete instance variable 'transitionsB1' ?
	makeB1 {
		var transName;
		unionOfB1 = Set[];
		transitions.do {| transition |
			transName = transition.name;
			// transitionsB1.put( transName, [] );
			transitions.do {| trans |
				if( 
					(transition.outputPlaces.as(Set) & trans.inputPlaces.as(Set) ).isEmpty.not 
					or: 
					{(transition.inputPlaces.as(Set) & trans.inhibitorPlaces.as(Set) ).isEmpty.not} ){ 
						// transitionsB1.at( transName ).add( trans );
						unionOfB1.add( trans );
					};
			};
		};
		// unionOfB1 = transitionsB1.inject( Set[], _.union( _ ) );//change method with reduce
	}

	//the algorithm to generate a sample for the underlying process of the PetriNetN
	// from Chapter 3 of "Stochastic Petri Nets: Modelling, Stability, Simulation" by Peter Haas
	//step 1:
	computeInitEnabledTransitions {
		currentTime = 0;//timeOffset
		enabledTransitions = Set[];
		oldTransitions = Set[];
		newTransitions = Set[];			// remove this line? dublicates with the last line
		transitions.do {| e |
			if( e.isEnabled ){ 
				enabledTransitions.add( e );
				e.clockReading = e.clock.value( this );
			}{
				e.clockReading = 0;
			};
		};
		newTransitions = enabledTransitions;
	}
	//step 2:
	computeFiringTransitions {
		if( enabledTransitions.isEmpty ){ 
			^ Error("There are no enabled transitions in petri net"+this.name.asString).throw; 
		};
		holdingTime = enabledTransitions.collect{|e| e.clockReading }.minItem;
		firingTransitions = enabledTransitions.select {|e|
			e.clockReading == holdingTime;
		};
	}
	//step 3:
	nextMarkingChange {
		currentTime = currentTime + holdingTime;
	}
	//step 4:
	generateNewMarking {
		firingTransitions.do {|e| e.fire( this ); }
	}
	//step 5:
	computeOldTransitions {
		oldTransitions.clear;
		( enabledTransitions - firingTransitions ).do {|e|
			if( e.isEnabled ){
				oldTransitions.add( e );
				e.clockReading = e.clockReading - ( holdingTime * e.clockSpeed.value( this ) );
			}
		}
	}
	//step 6:
	computeNewTransitions {
		newTransitions.clear;
		( unionOfB1 - oldTransitions ).do {|e|
			if( e.isEnabled ){
				newTransitions.add( e );
				e.clockReading = e.clock.value( this );
			};
		};
		enabledTransitions = oldTransitions.union( newTransitions );
	}
	//step 7:
	zeroRemainingClocks {
		( transitions.as(Set) - enabledTransitions ).do {|e|
			e.clockReading = 0;
		}
	}
	//end of algorithm	
}