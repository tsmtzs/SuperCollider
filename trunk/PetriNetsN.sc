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

	*clearAll { this.all.clear }

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

	neutralize {					// remove this method?
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

// PetriNetN as a subclass of Environment. It stores every node 
// as a key with the given name and sets its value to the corresponding object ( place - transition ).
// Thus, places and transitions must have unique names.
PetriNetN : Environment {
	var <places, <transitions, <type;
	var >pattern, player;

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
		^ this.places.collect {| place | [ place.name, place.tokens ] }.flatten.as( Event )
	}

	setMarking {| anIdentityDictionary |
		// this supposes that places and transitions have distinct names
		anIdentityDictionary.keysValuesDo {| key, val |
			if( this.includesKey( key ) ){
				this.at( key ).tokens_( val )
			}
		};
	}

	sources {
		^Event.newFrom( 
			this.transitions.collect {| val | 
				[ val.name, val.source ] 
			}.flat 
		)
	}

	sourceAt {| aSymbol |
		^ this[ aSymbol ].source;
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

	pattern {
		if( pattern.isNil ){
			^ ( simple: PNPatternN, timed: PNEventPattern )
			.at( type )
			.new( this, this.marking, inf, this.sources )
		}{
			^ pattern
		}
	}

	play {| clock, protoEvent, quant |
		var pat;
		pat = pattern ?? { this.pattern };
		// or should return PetriNetN?
		^ player = PNEventStreamPlayer( pat.asStream, protoEvent )
		.play( clock, false, quant );
	}

	stop {
		player.stop
	}
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
			unionOfB1.add( transition );
			transitions.do {| trans |
				if( 
					( petriNet.outputPlacesOf( transition ).as(Set) & petriNet.inputPlacesOf( trans ).as(Set) ).notEmpty 
					or: 
					{ ( petriNet.inputPlacesOf( transition ).as(Set) & petriNet.inhibitorPlacesOf( trans ).as(Set) ).notEmpty } ){ 
						unionOfB1.add( trans );
					};
			};
			b1.put( transition, unionOfB1.asArray );
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
	var <oldTransitions, <newTransitions, <firingTransitions, <previousEnabledTransitions;
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
		newTransitions = enabledTransitions.copy;
	}
	//step 2:
	computeFiringTransitions {
		if( enabledTransitions.isEmpty ){ 
			// maybe return nil so that the PNPattern stop?
			"There are no enabled transitions in petri net. The routine stops.".inform;
			thisThread.stop;
		};
		holdingTime = enabledTransitions.collect{| aSymbol | 
			petriNet[ aSymbol ].clockReading 
		}.minItem;
		firingTransitions = enabledTransitions.select {| aSymbol |
			// petriNet[ aSymbol ].clockReading == holdingTime;
			petriNet[ aSymbol ].clockReading.equalWithPrecision( holdingTime, 0.00000000000001 ); // ????
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
		previousEnabledTransitions = enabledTransitions.copy;
		enabledTransitions = oldTransitions.union( newTransitions );
	}
	//step 7:
	zeroRemainingClocks {
		( previousEnabledTransitions - enabledTransitions ).do {| aSymbol |
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

		net = petriNet;					// copy

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

PNEventPattern : Pattern {
	var petriNet, marking, length, sources, samplePath;

	*new {| aPetriNet, marking, length = inf, sources |
		^ super.newCopyArgs( aPetriNet, marking, length, sources )
	}

	storeArgs { ^ [ petriNet, length, marking, sources ] }

	play {| clock, protoEvent, quant |
		^ PNEventStreamPlayer( this.asStream, protoEvent ).play( clock, true, quant );
	}

	// for debuging 
	// embedInStream {| inevent |
	// 	var samplePath, transitions, streamDict, net, ev;
	// 	var aTrans, newTrans, cleanupTrans, cleanupEvents, cleanupType;
	// 	var clockReadings, array, str;

	// 	cleanupEvents = IdentityDictionary[];

	// 	net = petriNet;					// copy??

	// 	if( sources.notNil ){ net.setSources( sources ) };
	// 	if( marking.notNil ){ net.setMarking( marking ) };

	// 	samplePath = TimedSamplePath( net );

	// 	samplePath.computeInitEnabledTransitions;

	// 	length.value.do {
	// 		samplePath.computeFiringTransitions;

	// 		newTrans = samplePath.newTransitions.copy;
	// 		////////////////////////////////////////
	// 		clockReadings = petriNet.transitions.collect {| trans |
	// 			[ trans.name, trans.clockReading ]
	// 		}.flatten.as(Event);

	// 		array = [
	// 			currentTime: samplePath.currentTime,
	// 			holdingTime: samplePath.holdingTime,
	// 			clockReadings: clockReadings,
	// 			marking: net.marking,
	// 			newTransitions: samplePath.newTransitions.asArray,
	// 			firingTransitions: samplePath.firingTransitions.asArray,
	// 			oldTransitions: samplePath.oldTransitions.asArray,
	// 			enabledTransitions: samplePath.enabledTransitions.asArray
	// 		];

	// 		str = "** A marking change occured.\n";
	// 		array.keysValuesDo {| key, val |
	// 			// Post << Char.tab << key << ": " << val << Char.nl;
	// 			str = str + Char.tab + key + ": " + val.asString + Char.nl;
	// 		};
	// 		str.postln;
	// 		////////////////////////////////////////
	// 		0.5.wait;
	// 		samplePath.nextMarkingChangeAt
	// 		.generateNewMarking
	// 		.computeOldTransitions
	// 		.computeNewTransitions
	// 		.zeroRemainingClocks;

	// 	};
	// 	^inevent
	// }

	embedInStream {| inevent |
		var samplePath, transitions, streamDict, net, ev;
		var aTrans, newTrans, cleanupTrans, cleanupEvents, cleanupType, cleanupEventType;
		var size;

		cleanupEvents = IdentityDictionary[];

		net = petriNet;					// copy??

		if( sources.notNil ){ net.setSources( sources ) };

		if( marking.notNil ){ net.setMarking( marking ) };

		samplePath = TimedSamplePath( net );

		samplePath.computeInitEnabledTransitions;

		length.value.do {
			// if( inevent.isNil ){ ^ nil.yield };
			// if( inevent.isNil ){  ev = ( type: \rest ) };
			samplePath.computeFiringTransitions;

			newTrans = samplePath.newTransitions;

			if( newTrans.notEmpty ){
				size = newTrans.size;
				newTrans.do {| aSymbol, i |
					// If the source var of each transition stores only Events
					// and only one at a time then you have real time access to source
					ev = petriNet[ aSymbol ].source.( net ); // oneEventAssuption
					// ev = streamDict.at( aSymbol );

					ev = ev.next( inevent ) ?? { ( type: \rest ) };
					ev[ \delta ] = if( i == ( size - 1 ) ){
						samplePath.holdingTime
					}{ 
						0
					};

					cleanupEventType = EventTypesWithCleanup.cleanupEvent( ev );
					if ( cleanupEventType.notEmpty ){
						cleanupEvents.put( aSymbol, cleanupEventType  );
						this.prAddEndStream( ev, cleanupEvents );
					};
					inevent = ev.yield;

				};
			}{
				ev = ( type: \rest, delta: samplePath.holdingTime );
				this.prAddEndStream( ev, cleanupEvents );
				inevent = ev.yield;
			};
			
			samplePath.generateNewMarking
			.computeOldTransitions
			.computeNewTransitions
			.zeroRemainingClocks;

			samplePath.firingTransitions.do {| aSymbol |
				ev = cleanupEvents.removeAt( aSymbol );
				if( ev.notNil ){
					ev.put( \delta, 0 );
					this.prAddEndStream( ev, cleanupEvents ); // remove this line?
					inevent = ev.yield;
				};
			};

		};
		^inevent
	}

	prAddEndStream {| event, aDict |
		event.put(
			\endStream,
			r {| inev |
				aDict.do {| ev |
					ev.put( \delta, 0 );
					inev = ev.next( inev ).yield;
				};
				nil;					// remove this?
			}
		)
	}

	asScore{|duration=1.0, timeOffset=0.0, releaseTime = 1, protoEvent|
		var player;
		^ScoreStreamPlayer.new
		.makeScoreWithEnding( this.asStream, duration, protoEvent, timeOffset, releaseTime );
	}		
}

PNEventStreamPlayer : EventStreamPlayer {
	var outEvent;

	prNext { arg inTime;
		var nextTime;
		outEvent = stream.next(event.copy);

		if (outEvent.isNil) {
			streamHasEnded = stream.notNil;
			cleanup.clear;
			this.removedFromScheduler;
			^nil
		}{
			nextTime = outEvent.playAndDelta(cleanup, muteCount > 0);
			if (nextTime.isNil) { this.removedFromScheduler; ^nil };
			nextBeat = inTime + nextTime;	// inval is current logical beat
			^nextTime
		};
	}

	stop {
		cleanup.terminate;
		nextBeat = nil;
		isWaiting = false;
		stream = outEvent[ \endStream ];
	}
}

// A class for debuging
PNPostState {
	var >net, samplePath, <routine;

	*new {| petriNet |
		if( petriNet.type != \timed ){
			"The argument must be a timed petri net".throw;
		};
		^ super.newCopyArgs( petriNet ).init;
	}

	init { 
		samplePath = TimedSamplePath( net ) ;
		routine = this.prRoutine;
	}

	prRoutine {
		^ Routine {
			samplePath.computeInitEnabledTransitions;
			loop {
				samplePath.computeFiringTransitions;

				this.currentStateAsString.yield;

				// nil.yield;

				samplePath.nextMarkingChangeAt
				.generateNewMarking
				.computeOldTransitions
				.computeNewTransitions
				.zeroRemainingClocks
			}
		}
	}

	currentStateAsString {
		var clockReadings, array, str;

		clockReadings = net.transitions.collect {| trans |
			[ trans.name, trans.clockReading ]
		}.flatten.as(Event);

		array = [
			currentTime: samplePath.currentTime,
			holdingTime: samplePath.holdingTime,
			clockReadings: clockReadings,
			marking: net.marking,
			newTransitions: samplePath.newTransitions.asArray,
			firingTransitions: samplePath.firingTransitions.asArray,
			oldTransitions: samplePath.oldTransitions.asArray,
			enabledTransitions: samplePath.enabledTransitions.asArray
		];

		str = "** A marking change occured.\n";
		array.keysValuesDo {| key, val |
			// Post << Char.tab << key << ": " << val << Char.nl;
			str = str + Char.tab + key + ": " + val.asString + Char.nl;
		};
		^ str
	}
}

// for some reason, all class extensions should
// be at the end of file?
+ EventTypesWithCleanup {
	// almost a copy of the *cleanup method
	*cleanupEvent { | ev, flag = true |
		var type, notNode;
		type = ev[\type];
		notNode = notNodeType[type] ? true;
		if (flag || notNode) {
			^ (	parent: ev,
				type: cleanupTypes[type]
			);
		}
	}
}

+ ScoreStreamPlayer {
	// change this name
	makeScoreWithEnding { | stream, duration = 1, event, timeOffset = 0, releaseTime = 1|
		var ev, startTime, proto;
		proto = (
			server: this,

			schedBundle: { | lag, offset, server ...bundle |
				this.add(offset * tempo + lag + beats, bundle)
			},
			schedBundleArray: { | lag, offset, server, bundle |
				this.add(offset * tempo + lag + beats, bundle)
			}
		);

		event = event ? Event.default;
		event = event.copy.putAll(proto);
		beats = timeOffset;
		tempo = 1;
		bundleList = [];
		maxTime = timeOffset + duration;
		Routine {
			thisThread.clock = this;
			while ({
				thisThread.beats = beats;
				ev = stream.next(event.copy);
				(maxTime >= beats) && ev.notNil
			},{
				ev.putAll(proto);
				ev.play;
				beats = ev.delta * tempo + beats
			});

			stream = ev[ \endStream ];
			// can this be written in a better way?
			// Most of the following duplicates the previous lines
			thisThread.beats = beats;
			while {
				ev = stream.next( event.copy );
				ev.notNil
			}{
				ev.putAll( proto );
				ev.play;
				// beats = ev.delta * tempo + beats;
			};

		}.next;
		bundleList = bundleList.sort({ | a, b | b[0] >= a[0] });
		if ((startTime = bundleList[0][0]) < 0 ) {
			timeOffset = timeOffset - startTime;
		};


		^Score(bundleList.add([ duration + timeOffset + releaseTime, [\c_set, 0, 0]]) );
	}
}

+ Object {
	isTransition { ^false }
}