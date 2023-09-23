// $LastChangedDate$
// $Rev$

SPNImmediateTransition {
	classvar <>all;
	classvar <updateInputPlacesDefault, <updateOutputPlacesDefault, <enabledFunctionDefault, <clockSpeedDefault;
	var inputPlaces, inhibitorPlaces, outputPlaces; //Sets of PNPlace instances or names of PNPlaces
	var <>clockSpeed, <>updateInputPlaces, <>updateOutputPlaces; //Functions with second arg a SPetriNet ( first for clockSpeed )
	var <>enabledFunction;										 // a Function with args | inputPlaces, inhibitorPlaces | and values true - false
	var <>name, <>spnMediator;
	var <clock = 0, <>clockReading; // clock is a function with first arg a SPetriNet, and clockReading is a value of this function
	var <currentState;				// put this var in subclass SPNTimedTransition only?

	*initClass {
		all = IdentityDictionary.new;
		updateInputPlacesDefault  = { {| aSet | aSet.do { |elem| elem.removeOneToken } } };
		updateOutputPlacesDefault = { {| aSet | aSet.do { |elem| elem.addOneToken } } };
		enabledFunctionDefault = {| inputPlaces, inhibitorPlaces |
			//transition is enabled when all input places contain at least one token
			//and all inhibitor places contain no tokens. The message asCollection added
			//to prevent nil sets of places
			inputPlaces.asCollection.every{ |elem| elem.tokens !== 0 }
			and:
			{ inhibitorPlaces.asCollection.every { |elem| elem.tokens === 0 } }
		};
		clockSpeedDefault = 1;
	}

	*new { | key, inputPlaces, outputPlaces, inhibitorPlaces, updateInputPlaces, updateOutputPlaces, enabledFunction, clockSpeed |
		var transition;
		transition = this.at( key );
		if( inhibitorPlaces.notNil and: { (inputPlaces.asSet & inhibitorPlaces.asSet).isEmpty.not } ){
			^("There are  common Places in InputPlaces and InhibitorPlaces of transition"+key.asString).error;
		};
		if( transition.isNil ){
			transition = this.basicNew( key ).init( inputPlaces, outputPlaces, inhibitorPlaces, updateInputPlaces, updateOutputPlaces, enabledFunction, clockSpeed );
		}{
			if(
				[ inputPlaces, outputPlaces, inhibitorPlaces, updateInputPlaces, updateOutputPlaces, enabledFunction, clockSpeed ].any {| elem | elem.notNil }
			){
				transition.init( inputPlaces, outputPlaces, inhibitorPlaces, updateInputPlaces, updateOutputPlaces, enabledFunction, clockSpeed );
			}
		}
		^transition
	}

	// global storage
	*at { | key |
		^this.all.at(key)
	}

	*clearAll {
		this.all.clear;
	}

	*basicNew {| key |
		^super.new
		.prAdd( key )
	}

	init { | inputPlaces, outputPlaces, inhibitorPlaces, updateInputPlaces, updateOutputPlaces, enabledFunction, clockSpeed |
		this.inputPlaces_( inputPlaces )
		.inhibitorPlaces_( inhibitorPlaces )
		.outputPlaces_( outputPlaces )
		.updateInputPlaces_( updateInputPlaces ?? { updateInputPlacesDefault.( this.inputPlaces ) }  )
		.updateOutputPlaces_( updateOutputPlaces ?? { updateOutputPlacesDefault.( this.outputPlaces ) } )
		.enabledFunction_( enabledFunction ?? { enabledFunctionDefault } )
		.clockSpeed_( clockSpeed ?? { clockSpeedDefault } );
	}

	inputPlaces_ {| inputPlacesSet |
		this.prSetIfNotNil( \inputPlaces, inputPlacesSet );
	}

	inputPlaces {| aBoolean = false |
		^ this.prGetPlaces( inputPlaces, aBoolean );
	}

	inhibitorPlaces_ {| inhibitorPlacesSet |
		this.prSetIfNotNil( \inhibitorPlaces, inhibitorPlacesSet );
	}

	inhibitorPlaces {| aBoolean = false |
		^ this.prGetPlaces( inhibitorPlaces, aBoolean );
	}

	outputPlaces_ {| outputPlacesSet |
		this.prSetIfNotNil( \outputPlaces, outputPlacesSet );
	}

	outputPlaces {| aBoolean = false |
		^ this.prGetPlaces( outputPlaces, aBoolean );
	}

	prAdd {| argKey |
		all.put( argKey, this );
		this.instVarPut( \name, argKey );
	}

	prSetIfNotNil {| aSymbol, aCollection |
		if( aCollection.notNil ){
			this.instVarPut( aSymbol , this.prCollectPlaceInstances( aCollection ) )
		}
	}

	prCollectPlaceInstances {| aCollection |
		var place;
		^ aCollection.collect {| elem |
			if( elem.isKindOf( Symbol ) ){
				place = PNPlace.at( elem );
				if( place.isNil ){ place = PNPlace( elem ); };
				place
			}{
				elem
			}
		}
	}

	prGetPlaces {| aCollection, aBoolean = false |
		// aBoolean: if true, get symbols, otherwise get PNPlace instances
		^ if( aBoolean ){
			aCollection.collect {| elem | elem.name }
		}{
			aCollection
		}
	}

	fire {| aSPetriNet |
		this.updateInputPlaces.( inputPlaces, aSPetriNet ); // args marking, currentTime ?
		this.updateOutputPlaces.( outputPlaces, aSPetriNet );
	}

	isTimed { ^false }

	isEnabled {
		^enabledFunction.( inputPlaces, inhibitorPlaces )
	}

	neutralize {
		#inputPlaces, inhibitorPlaces, outputPlaces  = nil ! 3;
		updateInputPlaces = updateInputPlacesDefault;
		updateOutputPlaces = updateOutputPlacesDefault;
	}

	gui { | aWindow | }

	printOn { | stream |
		stream << this.class.name << "( "<< this.name << " )";
	}

	// override dependancy support
	dependants {
		^dependantsDictionary.at(this) ?? { IdentityDictionary.new };
	}
	changed {| what ... moreArgs |
		spnMediator.transitionChanged( this, what, *moreArgs );
	}
	addDependant { | key, dependant |
		var theDependants;
		// for the next line:
		// write instent 'event.copy' in collect?
		// add an 'if' in order to collect conditionaly the currentState?
		// It is asumed that 'dependant' is a Pdef with Pbind as source. What if is something alse?
		//		dependant.source = dependant.source.collect {| event | currentState = event; event.postln }; // <++++++++++++++++++++
		theDependants = dependantsDictionary.at(this);
		if(theDependants.isNil,{
			theDependants = IdentityDictionary.new.put( key, dependant );
			dependantsDictionary.put(this, theDependants);
		},{
			theDependants.put( key, dependant );
		});
	}
	removeDependant {| key |
		var theDependants;
		theDependants = dependantsDictionary.at(this);
		if (theDependants.notNil, {
			theDependants.removeAt( key );
			if (theDependants.size === 0, {
				dependantsDictionary.removeAt(this);
			});
		});
	}

}

SPNTimedTransition : SPNImmediateTransition {
	classvar <clockFunctionDefault;

	*initClass {
		clockFunctionDefault = 1;
	}

	*new { | key, inputPlaces, outputPlaces, inhibitorPlaces, clock, updateInputPlaces, updateOutputPlaces,  enabledFunction, clockSpeed |
		^super.new( key, inputPlaces, outputPlaces, inhibitorPlaces, updateInputPlaces, updateOutputPlaces, enabledFunction, clockSpeed, clock )
		// is this well writen? 'super' doesn't have a 'clock' arg
	}

	*basicNew {| key |
		^super.basicNew( key )
		.instVarPut(\clock, clockFunctionDefault )
	}

	init {| inputPlaces, outputPlaces, inhibitorPlaces,  updateInputPlaces, updateOutputPlaces,  enabledFunction, clockSpeed, clock |
		^super.init( inputPlaces, outputPlaces, inhibitorPlaces, updateInputPlaces, updateOutputPlaces,  enabledFunction, clockSpeed )
		.clock_( clock ?? { clockFunctionDefault } );
	}

	clock_ {| aFunction |
		clock = aFunction;
	}

	isTimed { ^true }

	neutralize {
		super.neutralize;
		this.clock_( clockFunctionDefault );
	}

	gui {| aWindow | }
}

SPetriNet {
	var <places, <transitions, firingTransitions, oldTransitions, transitionsB1;
	var newTransitions, immediateTransitions, enabledTransitions, unionOfB1;
	var <currentTime, <>timeOffset, <holdingTime;
	var <timeDurPairs, <task, <mediator;

	// Each argument corresponds to one transition and is an IdentityDictionary with keys:
	// \transition : name, \inputPlaces: setOfPlaceNames or nil, \outputPlaces: setOfOutputNames or nil,
	// \inhibitorPlaces: setOfInhibitorPlaces or nil, \updateInputPlaces: aFunction(optional),
	// \updateOutputPlaces: aFunction(optional), \clockSpeed: aFunction(optional), \clock: aFunction(optional, \isTimed: aBoolean or nil)
	*new {| ...dictionaries |
		^ super.new.init( dictionaries );
	}

	mediator_ {| aSPNMediator |
		mediator = aSPNMediator;
		this.transitions.do {| aSPNTransition |
			aSPNTransition.spnMediator_( mediator );
		};
	}

	init {| dictionaries |
		var transition, dependants;
		places = List[];
		transitions = List[];
		transitionsB1 = IdentityDictionary[];
		dictionaries.do {| aDict |
			this.prAddPlaces( aDict );
			transition = this.prAddTransition( aDict );
			[ \isTimed, \transition].do {| aSymbol | // insert this 3 lines in method 'prAddTransition?
				aDict.removeAt( aSymbol );
			};
			dependants = aDict.at( \dependants );
			if( dependants.notNil ){
				dependants.keysValuesDo {| key, val |
					transition.addDependant( key, val )
				};
				aDict.removeAt( \dependants );
			};
			transition.performWithEnvir( \init, aDict );
		}
	}

	prAddTransition {| aDict |
		var transition, transitionName, isTimed;
		transitionName = aDict.at( \transition );
		transition = SPNImmediateTransition.at( transitionName );
		if( transition.isNil ){
			isTimed = aDict.at( \clock ).isNil.not or: { aDict.trueAt( \isTimed ) }; // notNil for isNil.not
			transition = [ SPNImmediateTransition, SPNTimedTransition ]
			.at( isTimed.asInteger )
			.basicNew( transitionName )
		};
		this.prAddToList( transitions, transition );
		^transition;
	}

	prAddPlaces {| aDict |
		[ \inputPlaces, \inhibitorPlaces, \outputPlaces ].do { | aSymbol|
			this.prAddPlacesBasic( aDict.at( aSymbol ) );
		}
	}

	prAddPlacesBasic {| anArray |
		var place;
		anArray.do {| aSymbol |
			place = PNPlace.at( aSymbol );
			if( place.isNil ){
				place = PNPlace.new( aSymbol );
			};
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

	setMarking {| anIdentityDictionary |
		anIdentityDictionary.keysValuesDo {| key, value |
			// change the boolean test in 'if' with the return value of a method
			// from a class named SPNUtilities? This class will have as methods all
			// the private methods of PNPlace, SPNImmediateTransition.
			// Specifically, for this method you use the method 'prGetPlaces'
			if( places.collect {| place | place.name }.includes( key ).not){
				^ ("Petri net, doesn't have place" + key.asString).error;
			};
			PNPlace( key, value );
		};
	}

	//the algorithm to generate a sample for the underlying process of the SPetriNet
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
			^ Error("There are no enabled transitions in this petri net.").throw;
		};
		holdingTime = enabledTransitions.collect{|e| e.clockReading }.minItem;
		firingTransitions = enabledTransitions.select {|e|
			e.clockReading === holdingTime;
		};
	}
	//step 3:
	nextMarkingChangeAt {
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

	//is there a better approach for the following method? ( or better, its intent )
	//put variable 'states' outside of this method in order to avoid recomputing its value
	currentStateAsString {| showCurrentTime = true, showMarking = true, showOldTransitions = true, showNewTransitions = true, showFiringTransitions = true, showClockReadings = true,  showHoldingTime = true |
		var string, states, stateName, stateValue;
		states = thisMethod.argNames.drop(1);
		string = "** A marking change occured:\n\t";
		[showCurrentTime, showMarking, showOldTransitions, showNewTransitions, showFiringTransitions, showClockReadings, showHoldingTime ]
		.do {| aBoolean, i |
			if( aBoolean ){
				stateName = states[i].asString.drop(4);
				stateName[0] = stateName[0].toLower;
				stateValue = if( stateName == "marking" or: { stateName == "clockReadings"} ){
					this.perform( stateName.asSymbol );
				}{
					//this 'if' is UNGLY. CHANGE IT
					//you want stateName to collect the names of the transitions
					if( stateName == "currentTime" or: { stateName == "holdingTime"} ){
						this.instVarAt( stateName.asSymbol );
					}{
						this.instVarAt( stateName.asSymbol ).collect {| elem | elem.name }.asArray;
					}
				};

				string = string ++ stateName ++":" + stateValue.asString ++ "\n\t"
			};
		};
		^ string+"\n"
	}

	samplePathAlgorithm {| dur = 5, startTime = 0, aSelector ...moreArgs |
		var endTime;
		endTime = startTime + dur;
		unionOfB1 ?? { this.makeB1 }; //maybe remove this line?
		this.computeInitEnabledTransitions;

		this.prSamplePathsBasic( startTime );
		//	this.prSamplePathsWithPerform( startTime, \value ); // use this line and delete method 'prSamplePathsBasic' ?

		("First firing after time"+startTime.asString+"occured at time"+currentTime.asString).postln;

		this.prSamplePathsWithPerform( endTime, aSelector, *moreArgs );
	}

	prSamplePathsBasic {| endTime |
		while({ currentTime < endTime },{
			this.computeFiringTransitions
			.generateNewMarking
			.nextMarkingChangeAt
			.computeOldTransitions
			.computeNewTransitions
			.zeroRemainingClocks
		});
	}

	prSamplePathsWithPerform {| endTime, aSelector ...moreArgs |
		while({ currentTime <= endTime },{
			this.computeFiringTransitions
			.perform( aSelector, *moreArgs )
			.generateNewMarking
			.nextMarkingChangeAt
			.computeOldTransitions
			.computeNewTransitions
			.zeroRemainingClocks
		});
	}

	prTask {
		var trans;
		trans = transitions.as(Set);
		task = Task({
			"Please set the initial marking. Then call 'play'.".postln;
			nil.yield;
			this.computeInitEnabledTransitions;

			loop {
				this.computeFiringTransitions;
				//.nextMarkingChangeAt;
				newTransitions.do {|e|
					e.changed( \play )
				};
				( trans - enabledTransitions ).do {|e|
					e.changed( \stop )
				};
				this.generateNewMarking
				.computeOldTransitions
				.computeNewTransitions
				.zeroRemainingClocks;

				holdingTime.wait;
			};
		})
	}

	play {| aClock, quant |
		this.task.play(
			aClock ?? { TempoClock.default },
			false,
			quant ?? { 0 }
		);
	}

	start {| aClock, quant |
		this.task.start(
			aClock ?? { TempoClock.default },
			quant ?? { 0 }
		)
	}

	resume {| aClock, quant |
		this.task.resume(
			aClock ?? { TempoClock.default },
			quant ?? { 0 }
		)
	}

	pause {
		this.task.pause;
		// see 'stop' method for 'do' receiver 'transitions'
		transitions.do {|e|
			e.changed( \stop );
		}

	}

	stop {
		// var playingTrans;
		// playingTrans = oldTransitions.union( newTransitions );
		this.task.stop;
		// change 'transitions' from the next do, to catch only playing transitions?
		// For some reason, replacing 'transitions' with 'playingTrans' doesn't work
		transitions.do {|e|
			e.changed( \stop );
		}
	}

	reset {
		this.task.reset;
	}

	////////////////////////////////////////////////////////
	// change the next 3 methods with something better. You want to perform many methods each one
	// maybe with additional arguments
	samplePathWithMultiPerform {| dur = 5, startTime = 0 ...selectors |
		var endTime;
		endTime = startTime + dur;
		unionOfB1 ?? { this.makeB1 }; //maybe remove this line?
		this.computeInitEnabledTransitions;

		this.prSamplePathsBasic( startTime );
		//	this.prSamplePathsWithPerform( startTime, \value ); // use this line and delete method 'prSamplePathsBasic' ?

		("First firing after time"+startTime.asString+"occured at time"+currentTime.asString).postln;

		this.prSamplePathsWithMultiPerform( endTime, *selectors );
	}


	prSamplePathsWithMultiPerform {| endTime ...selectors |
		while({ currentTime <= endTime },{
			this.computeFiringTransitions
			.multiPerform( *selectors )
			.generateNewMarking
			.nextMarkingChangeAt
			.computeOldTransitions
			.computeNewTransitions
			.zeroRemainingClocks
		});
	}

	multiPerform {| ...selectors |
		selectors.do {| aSymbol |
			this.perform( aSymbol )
		}
	}
	////////////////////////////////////////////////////////

	putCurrentStateToFile {| aFile, showCurrentTime = true, showMarking = true, showOldTransitions = true, showNewTransitions = true, showFiringTransitions = true, showClockReadings = true,  showHoldingTime = true |
		var string;
		string = this.currentStateAsString( showCurrentTime, showMarking, showOldTransitions, showNewTransitions, showFiringTransitions, showClockReadings,  showHoldingTime);
		aFile.write( string );
	}

	postCurrentState{ |showCurrentTime = true, showMarking = true, showOldTransitions = true, showNewTransitions = true, showFiringTransitions = true, showClockReadings = true,  showHoldingTime = true |
		Post << this.currentStateAsString( showCurrentTime, showMarking, showOldTransitions, showNewTransitions, showFiringTransitions, showClockReadings, showHoldingTime );
	}

	initTimeDurPairs {
		timeDurPairs ?? {
			timeDurPairs = transitions.collect {|trans| trans.name }.collectAs( {|aSymbol| aSymbol -> List[] }, IdentityDictionary );//or Event?
			// keep only timed transitions?
			// timeDurPairs = transitions.select {|trans| trans.isTimed}
			// .collect {|trans| trans.name }.collectAs( {|aSymbol| aSymbol -> List[] }, IdentityDictionary );
			^this
		};
		// maybe remove this line and keep the above without the conditional? Is it faster then?
		timeDurPairs.do {|aList| aList.clear };
	}

	collectTimeDurPairs {
		var list;
		enabledTransitions.do {|e|
			list = timeDurPairs.at( e.name );
			case
			{ newTransitions.includes( e ) }{
				list.add( [ currentTime, holdingTime ] );
			}
			{ oldTransitions.includes( e ) }{
				list.last[1] = list.last[1] + holdingTime;
			}
		}
	}

	/*
		play {| from, to | // modify to select the pattern / s to play
		}

		writeOSCFile {| path, clock, from, to |
		}

		recordNRT {
		}
	*/
}

SPNMediator {
	var <>show;

	*new {| ...aSymbol |
		^super.newCopyArgs( aSymbol )
	}

	transitionChanged {| aSPNTransition, what ...moreArgs |
		show.do {| aSymbol |
			aSPNTransition.dependants.at( aSymbol ).perform( what, *moreArgs );
		}
	}
}
