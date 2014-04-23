// $LastChangedDate$
// $Rev$
PNPlaceN {
	classvar <>all;
	var  <>name, <tokens;

	*initClass {
		all = IdentityDictionary.new;
	}

	*at {| key |
		^this.all.at( key )
	}

	*new {| key, anInteger |
		var place;
		place = this.at( key );
		if( place.isNil ){
			place = super.newCopyArgs( key, anInteger ?? { 0 } ).store;
		}{
			if( anInteger.notNil ){ place.tokens_( anInteger ) }
		};
		^ place
	}

	*clearAll { this.all.clear }

	store {
		all.put( name, this );
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
		// this.warn( anInteger );
		// tokens = tokens - anInteger;
		this.addTokens( -1 * anInteger );
	}

	isEmpty { ^ tokens == 0 }

	gui {| aWindow | }

	printOn { arg stream;
		stream << this.class.name << "( " << name <<" , " << tokens  << " )";
	}

// Modify method warning so that it can print or not the message?
	warn {| anObject |
		if( anObject.isKindOf( Integer ).not ){
			("\nThe number" + anObject.asString + "is not an integer").warn 
		};
	}

	isTransition { ^false }
	isPlace { ^true }
}

// look again instance method 'pnEnvironment'
// discriminate between places and transitions?
// if a place and a transition have the same name, one is ovewriten
// OR
// don't store pnEnvironment in each instance. Just pass it over so that an instance
// can register in the Environment
PNTransitionN {
	classvar <>all;
	classvar <updateInputPlacesDefault, <updateOutputPlacesDefault, <enabledFunctionDefault;
	var  <>name, inputPlaces, outputPlaces, inhibitorPlaces; //Sets of PNPlaceN instances or names of PNPlaceNs
	var <>updateInputPlaces, <>updateOutputPlaces; //Functions with second arg a SPetriNet ( first for clockSpeed )
	var <>enabledFunction;										 // a Function with args | inputPlaces, inhibitorPlaces | and values true - false
	var <>source;

	*initClass{
		all = IdentityDictionary.new;
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

	*at {| aSymbol |
		^this.all.at( aSymbol );
	}

	*new { | name, inputPlaces, outputPlaces, inhibitorPlaces, updateInputPlaces, updateOutputPlaces, enabledFunction, source |
		var transition;
		// look again this message - symbols - places
		// should it assume that all places are given as PNPlace Objects?
		if( inhibitorPlaces.notNil and: { (inputPlaces.asSet & inhibitorPlaces.asSet).isEmpty.not } ){
			"There are  common places in input places and inhibitor places of this transition.".error;
		};
		transition = this.at( name );
		if( transition.isNil ){
			transition = this.basicNew( name )
			.init( inputPlaces, outputPlaces, inhibitorPlaces, updateInputPlaces, updateOutputPlaces, enabledFunction )
			.source_( source )
			.store;
		}{
			transition.init( inputPlaces, outputPlaces, inhibitorPlaces, updateInputPlaces, updateOutputPlaces, enabledFunction )
			.source_( source );
		}
		^ transition
	}

	*basicNew {| aSymbol |
		^super.new
		.name_( aSymbol );
	}

	store { all.put( name, this ) }
	
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

	// prCollectPlaceInstances {| aCollection |
	// 	var place;
	// 	^ aCollection.collect {| elem | 
	// 		if( elem.isKindOf( Symbol ) ){ // modify to pass tokens after name e.x [ \p0, 10, \p1, \p2]
	// 			place = PNPlaceN.at( elem );
	// 			if( place.isNil ){ place = PNPlaceN( elem ); };
	// 			place
	// 		}{
	// 			// check for other objects?
	// 			elem
	// 		}
	// 	}
	// }

	prCollectPlaceInstances {| aCollection |
		var i = 0, aSymbol, tokens, size;
		var instList;
		instList = List[];
		size = aCollection.size;
		while { 
			i < size
		}{
			# aSymbol, tokens = aCollection.copyRange( i, i + 1 );
			if ( tokens.isKindOf( SimpleNumber ) ){
				instList.add( PNPlaceN( aSymbol, tokens ) );
				i = i + 2;
			}{
				instList.add( PNPlaceN( aSymbol ) );
				i = i + 1;
			}
		}
		^instList.array;
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

	printOn { arg stream;
		stream << this.class.name << "( "<< this.name << " )";
	}

	isTransition { ^true }
	isPlace { ^false }
	isTimed { ^false }

	// asTimedPN {| clock |
	// 	// maybe delete from all dictionary the instanse?
	// 	clock = clock ?? { 1 };
	// 	^ PNTimedTransitionN( name,	inputPlaces, outputPlaces, inhibitorPlaces, updateInputPlaces, updateOutputPlaces, enabledFunction, clock, source )
	// }
}

PNTimedTransitionN : PNTransitionN {
	classvar <clockFunctionDefault;
	var <>clock, <>clockReading;

	*initClass{
		clockFunctionDefault = 1;
	}

	*new { | name, inputPlaces, outputPlaces, inhibitorPlaces, updateInputPlaces, updateOutputPlaces, enabledFunction, clock, source |
		^super.new( name, inputPlaces, outputPlaces, inhibitorPlaces, updateInputPlaces, updateOutputPlaces, enabledFunction, source )
		.clock_( clock ?? { clockFunctionDefault } )
	}

	neutralize {						// remove this method?
		super.neutralize;
		this.clock_( clockFunctionDefault );
	}

	gui { | aWindow | }

	isTimed { ^true }

	// asSimplePN {| aSymbol |
	// 	aSymbol ?? { 
	// 		all.removeAt( name ); 
	// 	};
	// 	^ PNTransitionN( name, inputPlaces, outputPlaces, inhibitorPlaces, updateInputPlaces, updateOutputPlaces, enabledFunction, source )
	// }
}

// PetriNetN as a subclass of IdentityDictionary. It stores every node 
// as a key with the given name and sets its value to the corresponding object ( place - transition ).
// Thus, places and transitions must have unique names.
PetriNetN : IdentityDictionary {
	var <places, <transitions, <type;

// Each argument corresponds to one transition and is an IdentityDictionary with keys:
// \transition : name, \inputPlaces: setOfPlaceNames or nil, \outputPlaces: setOfOutputNames or nil,
// \inhibitorPlaces: setOfInhibitorPlaces or nil, \updateInputPlaces: aFunction(optional),
// \updateOutputPlaces: aFunction(optional), \clockSpeed: aFunction(optional), \clock: aFunction(optional, \isTimed: aBoolean or nil)
	*new {| ... dictionaries |
		^ super.new.init( dictionaries );
	}

	init {| dictionaries |
		var className;

		// what if there are timed and simple transitions with the same name
		// stored in PNTransition's all dictionary???
		# className, type = if( ().putAll( *dictionaries ).includesKey( \clock ) ){
			[ PNTimedTransitionN, \timed ];
		}{
			[ PNTransitionN, \simple ];
		};
	
		dictionaries.do {| aDict |
			this.prAddTransition( aDict, className );
		};
	}

	prAddTransition {| aDict, transitionClass |
		var name, transition;
		name = aDict.removeAt( \transition );
		aDict.put( \name, name );
		transition = transitionClass.performWithEnvir( \new, aDict ); // is there a better approach for this???
		this.put( name, transition );
		transitions = transitions.add( transition ); // look this again
		this.prAddPlaces( transition );
	}

	// NOT GOOD
	prAddPlaces {| aPNTransitionN |
		var placeName;
		[ \inputPlaces, \inhibitorPlaces, \outputPlaces ].do { | aSymbol |
			aPNTransitionN.perform( aSymbol ).do {| place |
				placeName = place.name;
				// modify to take into account the number of tokens?
				if( this.at( placeName ).isNil ){
					this.put( placeName, place );
					places = places.add( place ); // look this again
				}
			}
		}
	}

	marking {
		^ this.places.collect {| place | [ place.name, place.tokens ] }.flat.as( Event )
	}

	setMarking {| anIdentityDictionary |
		// this supposes that places and transitions have distinct names
		anIdentityDictionary.keysValuesDo {| key, val |
			if( this.includesKey( key ) ){
				this.at( key ).tokens_( val )
			}
		};
	}

	setSources {| anIdentityDictionary |
		// this supposes that places and transitions have distinct names
		anIdentityDictionary.keysValuesDo {| key, val |
			if( this.includesKey( key ) ){
				this.at( key ).source_( val )
			}
		};
	}

	setSource {| aSymbol, source |
		// add some testing?
		this.at( aSymbol ).source_( source );
	}

	inputPlacesOf {| aSymbol | ^ this.at( aSymbol ).inputPlaces.collect {| p | p.name } }
	outputPlacesOf {| aSymbol | ^ this.at( aSymbol ).outputPlaces.collect {| p | p.name } }
	inhibitorPlacesOf {| aSymbol | ^ this.at( aSymbol ).inhibitorPlaces.collect {| p | p.name } }

	// Overide this methods? What object should return?
	// Needs work - returns error
	// collect {| aFunction |
	// 	^super.collect( aFunction )
	// }

	// select {| aFunction |
	// 	^super.select( aFunction )
	// }

	// reject {| aFunction |
	// 	^super.reject( aFunction )
	// }
}

// abstract class - stores symbols, the names of the transitions.
// ( store Transition instances instead? )
PNSamplePath {
	var >petriNet;
	// add setter - getter methods?
	var <transitions, <b1;
	var <enabledTransitions;

	*new {| aPetriNetN |
		^ super.new.petriNet_( aPetriNetN ).init
	}

	init {
		petriNet.keysValuesDo {| key, val |
			if( val.isTransition ){ 
				transitions = transitions.add( key );
			};
		};
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
}

SimpleSamplePath : PNSamplePath {
	// step 1:
	computeInitEnabledTransitions {
		enabledTransitions = Set[];
		transitions.do {| aSymbol |
			// if petriNet is a subclass of IdentityDictionary then use
			// petriNet[ aSymbol ].isEnabled
			// assuming that places and transitions don't have common names
			if( petriNet.at( aSymbol ).isEnabled ){
				enabledTransitions.add( aSymbol );
			}
		};
	}
	// step 2:
	computeEnabledTransitions {
		var candidateTrans;				// make this an instance var? is it significantly faster?
		candidateTrans = b1.select {| value, key |
			enabledTransitions.includes( key )
		}
		.values.flat.asSet;
		enabledTransitions.clear;
		candidateTrans.do {| aSymbol |
			if( petriNet.at( aSymbol ).isEnabled ){ 
				enabledTransitions.add( aSymbol );
			}
		};
	}
	//step 3:
	generateNewMarking {
		enabledTransitions.do {| aSymbol | petriNet.at( aSymbol ).fire( petriNet ); }
	}
	//end of algorithm	
}

TimedSamplePath : PNSamplePath {
	var <oldTransitions, <newTransitions, <firingTransitions;
	var <currentTime, <holdingTime;

	//the algorithm to generate a sample for the underlying process of the SPetriNet
	// from Chapter 3 of "Stochastic Petri Nets: Modelling, Stability, Simulation" by Peter Haas
	// step 1:
	computeInitEnabledTransitions {
		var trans;
		currentTime = 0;//timeOffset
		enabledTransitions = Set[];
		oldTransitions = Set[];
		transitions.do {| aSymbol |
			trans = petriNet[ aSymbol ];
			if( trans.isEnabled ){ 
				enabledTransitions.add( aSymbol );
				trans.clockReading = trans.clock.value( petriNet );
			}{
				trans.clockReading = 0;
			};
		};
		newTransitions = enabledTransitions; // .copy ???
	}
	//step 2:
	computeFiringTransitions {
		if( enabledTransitions.isEmpty ){ 
			// maybe return nil so that the PNPattern stop?
			^ Error("There are no enabled transitions in petri net"+this.name.asString).throw; 
		};
		holdingTime = enabledTransitions.collect{| aSymbol | 
			petriNet[ aSymbol ].clockReading 
		}.minItem;
		firingTransitions = enabledTransitions.select {| aSymbol |
			petriNet[ aSymbol ].clockReading == holdingTime;
		};
	}
	//step 3:
	nextMarkingChangeAt {
		currentTime = currentTime + holdingTime;
	}
	//step 4:
	generateNewMarking {
		firingTransitions.do {| aSymbol | 
			petriNet[ aSymbol ].fire( petriNet ); 
		}
	}
	//step 5:
	computeOldTransitions {
		var trans;
		oldTransitions.clear;
		( enabledTransitions - firingTransitions ).do {| aSymbol |
			trans = petriNet[ aSymbol ];
			if( trans.isEnabled ){
				oldTransitions.add( aSymbol );
				trans.clockReading = trans.clockReading -  holdingTime;
			}
		}
	}
	//step 6:
	computeNewTransitions {
		var trans, candidateTrans;

		candidateTrans = b1.select {| value, key |
			enabledTransitions.includes( key )
		}
		.values.flat.asSet;

		newTransitions.clear;

		( candidateTrans - oldTransitions ).do {| aSymbol |
			trans = petriNet[ aSymbol ];
			if( trans.isEnabled ){
				newTransitions.add( aSymbol );
				trans.clockReading = trans.clock.value( petriNet );
			};
		};
		enabledTransitions = oldTransitions.union( newTransitions );
	}
	//step 7:
	zeroRemainingClocks {
		( transitions.as(Set) - enabledTransitions ).do {| aSymbol |
			petriNet[ aSymbol ].clockReading = 0;
		}
	}
	//end of algorithm
}

PNPatternN : Pattern {
	var petriNet, marking, length, sources, samplePath;

	*new {| aPetriNet, marking, length = inf, sources |
		^ super.newCopyArgs( aPetriNet, marking, length, sources )
	}

	storeArgs { ^ [ petriNet, length, marking, sources ] }

	embedInStream {| inval |
		var samplePath, transitions, streamDict, net;

		net = petriNet.copy;

		if( sources.notNil ){ net.setSources( sources ) };

		streamDict = net.transitions.collect {| trans | 
			[ trans.name, trans.source.asStream ] 
		}.flatten.as( Event );

		if( marking.notNil ){ net.setMarking( marking ) };

		samplePath = SimpleSamplePath( net );

		samplePath.computeInitEnabledTransitions;

		length.value.do {
			inval = samplePath.enabledTransitions.collect {| aSymbol |
				streamDict.at( aSymbol ).next( inval ) // or use embedInStream?
			}.asArray;

			if( inval.size == 1 ){ inval = inval.pop }; // better approach for this?

			inval = inval.yield;

			samplePath.generateNewMarking
			.computeEnabledTransitions;
			};
		^inval
	}
}