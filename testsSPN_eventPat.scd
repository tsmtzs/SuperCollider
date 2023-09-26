//a net for tests
(
PNPlace.clearAll;

a = SPetriNet(
	(
		\transition: \t1,
		\inputPlaces: [\p1],
		\outputPlaces: [\p2,\p3],
		\inhibitorPlaces: [],
		\clock: 1
	),
	(
		\transition: \t2,
		\inputPlaces: [\p2],
		\outputPlaces: [\p1],
		\inhibitorPlaces: [],
		\clock: 0.5
	),
	(
		\transition: \t3,
		\inputPlaces: [\p3],
		\outputPlaces: [\p1],
		\inhibitorPlaces: [],
		\isTimed: true
	)
)
)
a.marking
a.setMarking( (\p1:1, \p2: 0, \p3: 0)  )


(
a.removeUniqueMethod( \show );
a.addUniqueMethod( \show,
	{| net |
		Post<<net.perform( \currentStateAsString, showClockReadings:true, showMarking:true );
		net
	}
)
)
a.currentStateAsString

(
a.removeUniqueMethod( \putCurrentStateToFile );
a.addUniqueMethod(\putCurrentStateToFile, {
	| inst, aFile, showCurrentTime = true, showMarking = false, showOldTransitions = true, showNewTransitions = true, showFiringTransitions = true, showClockReadings = false,  showHoldingTime = true |
	var string;
	string = inst.currentStateAsString( showCurrentTime, showMarking, showOldTransitions, showNewTransitions, showFiringTransitions, showClockReadings,  showHoldingTime);
	aFile.write( string );
	inst
}
);
)

f = File(File.getcwd +/+ "test", "w" )
a.putCurrentStateToFile( f, showMarking: true, showClockReadings: true )
a.putCurrentStateToFile( f )

(
a.removeUniqueMethod(\samplePathAlgorithm);
a.addUniqueMethod(\samplePathAlgorithm,{| inst, dur = 5, startTime = 0, aSelector ...moreArgs |
	var endTime;
	endTime = startTime + dur;
	inst.computeInitEnabledTransitions;

	inst.prSamplePathsBasic( startTime );

	//	("First firing after time"+startTime.asString+"occured at time"+currentTime.asString).postln;

	inst.prSamplePathsWithPerform( endTime, aSelector, *moreArgs );
}
)
)

a.samplePathAlgorithm( 5, 4, \putCurrentStateToFile, f )
a.computeInitEnabledTransitions;
a.prSamplePathsWithPerform(4, \putCurrentStateToFile , f )
a.prSamplePathsBasic(4 )
a.samplePathAlgorithm(15, 5.6, \postCurrentState )

f.close
File.delete(File.getcwd +/+ "test");

//////////////////////////////////////////////////////////////////////
// Adding sound objects to Transitions -- playing them in real Time

(
PNPlace.clearAll;

a = SPetriNet(
	(
		\transition: \t1,
		\inputPlaces: [\p2],
		\outputPlaces: [\p1],
		\inhibitorPlaces: [],
		\clock: { 2 }
	),
	(
		\transition: \t2,
		\inputPlaces: [\p1],
		\outputPlaces: [\p2],
		\inhibitorPlaces: [],
		\clock: { 1 }
	)
);
)

(
m = SPNMediator( \sound );

~t1 = a.transitions.at( \t1 );
~t1.spnMediator_( m );
~t1.addDependant(\sound , Pdef(\t1, Pbind(\freq, 300, \dur, 1.0 ) ) );
// ~t1.dependants.at(\sound).source = Pbind(\freq, 250, \dur, 0.5 );
// ~t1.clock_({ 2.0 } );

~t2 = a.transitions.at(\t2);
~t2.spnMediator_( m );
~t2.addDependant(\sound ,
	Pdef(\t2,
		Pbind(
			\freq, Pwhite( 300, 600, inf ),
			\dur, 0.1,
			\legato, 0.4
		)
	)
);
// ~t2.clock_( { 1.15 } );
)

// if you dont run the next line, some Events from Pdef
// might not be played.
Pdef.all.do {|pdef| pdef.quant = 0; /* pdef.fadeTime = 0 */ }

a.setMarking( (\p1: 1, \p2: 0) )
a.prTask

a.play
a.pause
a.resume
a.stop
a.reset

//////////////////////////////////////////////////////////////////////
// Immediate transitions in real time
(
PNPlace.clearAll;

a = SPetriNet(
	(
		\transition: \t1,
		\inputPlaces: [\p11],
		\outputPlaces: [\p21],
		\inhibitorPlaces: [],
		\clock: { 2 }
	),
	(
		\transition: \t2,
		\inputPlaces: [\p12],
		\outputPlaces: [\p22],
		\inhibitorPlaces: [],
		\clock: { 1 }
	),
	(
		\transition: \t3,
		\inputPlaces: [\p21,\p22],
		\outputPlaces: [\p3],
		\inhibitorPlaces: [],
		\isTimed: false
	),
	(
		\transition: \t4,
		\inputPlaces: [\p3],
		\outputPlaces: [\p11,\p12],
		\inhibitorPlaces: [],
		\clock: { 1 }
	)
);
)

(
m = SPNMediator( \sound );

~t1 = a.transitions.at(\t1);
~t1.spnMediator_( m );
~t1.addDependant(\sound , Pdef(\t1, Pbind(\freq, 300, \dur, 10.0 ) ) );
// ~t1.dependants.at(\sound).source = Pbind(\freq, 250, \dur, 0.5 );
// ~t1.clock_({ 2.0 } );

~t2 = a.transitions.at(\t2);
~t2.spnMediator_( m );
~t2.addDependant(\sound ,
	Pdef(\t2,
		Pbind(
			\freq, Pwhite( 600, 800, inf ),
			\dur, 0.25,
			\legato, 0.4
		)
	)
);
// ~t2.clock_( { 1.15 } );

~t3 = a.transitions.at(\t3);
~t3.spnMediator_( m );

~t4 = a.transitions.at(\t4);
~t4.spnMediator_( m );
~t4.addDependant(\sound, Pdef(\t4, Pbind( \freq, 500, \dur, 0.1, \amp, 0.7)));
)

// RUN the next line
Pdef.all.do {|pdef| pdef.quant = 0; }

(
a.setMarking(
	a.marking.keysValuesChange({| key, value |
		value = if( key == \p3 ){ 1 }{ 0 };
	})
)
)
a.prTask

a.play
a.pause
a.resume
a.stop
a.reset

//////////////////////////////////////////////////////////////////////
// Real time mode
// Changing the args of a synth

(
// adapted from 'iterated functions.scd'
SynthDef(\noise,{| out = 0, pan = 0.0, amp = 1.0, coef1 = 30.0, coef2 = 400.0, x0 = 110.02, r0 = 1000.40, gate = 1 |
	var env, xn, r = 0;
	xn = SinOsc.ar( r0 * x0 ) ;
	r = SinOsc.ar( coef1 * r0 * xn.abs.sqrt, 0, coef2 );
	2.do({
		xn = SinOsc.ar(r*xn) ;
		r = SinOsc.ar( coef1 *r * xn.abs.sqrt, 0, coef2 );
	});

	env  = EnvGen.kr( Env.cutoff( 1 ) , gate, doneAction: 2 );
	//xn = HPF.ar(xn,30,0.03);

	Out.ar( out, Pan2.ar( xn * env, pan, amp ) )
}).add
)

(
~updateFunction = { | places, aSPetriNet |
	var place = aSPetriNet.places.at(\p2);
	if ( place.tokens < 1 ){ place.addOneToken }
}
)

(
PNPlace.clearAll;

a = SPetriNet(
	(
		\transition: \t1,
		\inputPlaces: [\p1],
		\outputPlaces: [\p2],
		\inhibitorPlaces: [],
		\clock: { 0.1 },
		\dependants: (
			\sound:	(
				\type: \on,
				\instrument: \noise,
				\id:  ~synthID = 1000,
				\x0: { 0.1.rrand( 400.0 ).debug( "x_0" ) },
				\r0: { 0.2.rrand( 200.0 ).debug( "r_0" ) }
			)
		)
	),
	(
		\transition: \t2,
		\inputPlaces: [\p2],
		\outputPlaces: [\p3,\p4],
		\inhibitorPlaces: [],
		\updateOutputPlaces: {| outputPlaces |
			outputPlaces.choose.addOneToken;
		}
	),
	(
		\transition: \t3,
		\inputPlaces: [\p3],
		\outputPlaces: [\p5],
		\inhibitorPlaces: [],
		\clock: { rrand( 1.0, 5.0 ) },
		\dependants: (
			\sound: (
				\type: \set,
				\id: ~synthID,
				\args: #[ \pan ],
				\pan: { 1.0.rand2.debug("pan") }
			)
		)
	),
	(
		\transition: \t4,
		\inputPlaces: [\p4],
		\outputPlaces: [\p6],
		\inhibitorPlaces: [],
		\clock: { rrand( 1.0, 10.0 ) },
		\dependants: (
			\sound: (
				\type: \set,
				\id: ~synthID,
				\args: #[ \amp ],
				\amp: { 0.05.rrand(0.4).debug("amp") }
			)
		)
	),
	(
		\transition: \t5,
		\inputPlaces: [\p5],
		\outputPlaces: [\p2],
		\inhibitorPlaces: [],
		\clock: { 1.0.rrand( 10.0 ) },
		\updateOutputPlaces: ~updateFunction,
		\dependants: (
			\sound: (
				\type: \set,
				\id: ~synthID,
				\args: #[ \coef1 ],
				\coef1: { 0.05.rrand(1000.4).debug("coef1") }
			)
		)
	),
	(
		\transition: \t6,
		\inputPlaces: [\p6],
		\outputPlaces: [\p2],
		\inhibitorPlaces: [],
		\clock: { 1.0.rrand( 10.0 ) },
		\updateOutputPlaces: ~updateFunction,
		\dependants: (
			\sound: (
				\type: \set,
				\id: ~synthID,
				\args: #[ \coef2 ],
				\coef2: { 0.05.rrand(1000.4).debug("coef2") }
			)
		)
	)
);

a.mediator_( SPNMediator( \sound ) );
a.prTask;
)

(
a.setMarking(
	a.marking.keysValuesChange({| key, value |
		value = if( key == \p1 ){ 1 }{ 0 };
	})
)
)

a.play;
a.stop; s.sendMsg( "/n_free", ~synthID );
a.reset
a.places.at(\p2).tokens
a.marking

a.samplePathAlgorithm(15, 0, \postCurrentState )

//////////////////////////////////////////////////////////////////////
// RUN the next line
Pdef.all.do {|pdef| pdef.quant = 0; }

(
~updateFunction = {| outputPlaces, aPetriNet |
	outputPlaces.choose.addOneToken
};

Pdef(\p1,{
	var repeats;
	repeats = 1.rrand(7);
	Pbind(
		\note, Pseq([[6,20]], repeats),
		\dur, Pseq([0.6], repeats),
		\amp, 0.2,
		\octave, 6
	)
}
);

Pdef(\p2,{
	var notes, durs;
	notes = [4,5,4,2,-2,-3,7,5,4].scramble;
	durs = ( [1/4,1/12] ++ ((1/6)!7) ).scramble;
	Pbind(
		\note, Pseq( notes, 1 ),
		\dur, 2*Pseq( durs, 1 ),
		\amp, 0.52,
		\octave, 5,
		\legato, 1.2
	)
}
);

//  Pdef(\p2,
// 	Pbind(
// 		\note, Pseq([4,5,4,2,-2,-3,7,5,4],1),
// 		\dur, 2*Pseq([1/4,1/12] ++ ((1/6)!7), 1),
// 		\amp, 0.52,
// 		\octave, 5,
// 		\legato, 1.2
// 	)
// );

Pdef(\p3,{
	var durs, size;
	durs = [ [ rrand( 8, 10 ), 0.8,7/10], [ rrand( 8, 12),0.1,10/7] ].collect{ |i| Array.geom( *i ) }.flatten;
	size = durs.size;
	Pbind(
		\dur, Pseq( durs ),
		\note, Pseq( [ Pseq([ [4,6,7,9] ], size - 4 ), Pseq( [ [4,5,6] ], 4 ) ] ),
		\amp, 0.18,
		\octave, 7
	)
});

// Pdef(\p3,{
// 	var durs, size;
// 	durs = [ [10,0.8,7/10], [8,0.1,10/7] ].collect{ |i| Array.geom( *i ) }.flatten;
// 	size = durs.size;
// 	Pbind(
// 		\dur, Pseq( durs ),
// 		\note, Pseq( [ Pseq([ [4,6,7,9] ], size - 4 ), Pseq( [ [4,5,6] ], 4 ) ] ),
// 		\amp, 0.21,
// 		\octave, 7
// 	)
// });

Pdef(\p4,{
	var notes;
	notes = [ [-1,1,8], [ -5,-1,1,8], [-9,-5,-1,1,8], [-13,-9,-8,-5,-1,1,8] ].choose;
	Pbind(
		\note, Pseq( notes ),
		\dur, 0.05,
		\amp, 0.3,
		\octave, 7
	)
});

Pdef( \left,
	Pbind(
		\note, Pseq( [2,7,9,17], inf ),
		\dur, 0.25,
		\amp, 0.4,
		\octave, 3,
		\legato, 0.2

	)
);
)

(
PNPlace.clearAll;

a = SPetriNet(
	(
		\transition: \t6,
		\inputPlaces: [\p0],
		\outputPlaces: [\p6],
		\inhibitorPlaces: [],
		\isTimed: false
	),
	(
		\transition: \t5,
		\inputPlaces: [\p0],
		\outputPlaces: [\p5],
		\inhibitorPlaces: [],
		\isTimed: false
	),
	(
		\transition: \t7,
		\inputPlaces: [\p6],
		\outputPlaces: [\p1,\p2,\p3,\p4],
		\inhibitorPlaces: [],
		\updateOutputPlaces: ~updateFunction,
		\isTimed: false
	),
	// right hand
	(
		\transition: \t1,
		\inputPlaces: [\p1],
		\outputPlaces: [\p6],
		\inhibitorPlaces: [],
		\dependants:(
			\sound: Pdef(\p1)
		),
		\clock: { 2.0.rrand(10.0) }
	),
	(
		\transition: \t2,
		\inputPlaces: [\p2],
		\outputPlaces: [\p6],
		\inhibitorPlaces: [],
		\dependants: (
			\sound: Pdef(\p2)
		),
		\clock: { 1.5.rrand(15.0) }
	),
	(
		\transition: \t3,
		\inputPlaces: [\p3],
		\outputPlaces: [\p6],
		\inhibitorPlaces: [],
		\dependants: (
			\sound: Pdef(\p3)
		),
		\clock: { 1.5.rrand(10.0) }
	),
	(
		\transition: \t4,
		\inputPlaces: [\p4],
		\outputPlaces: [\p6],
		\inhibitorPlaces: [],
		\dependants: (
			\sound: Pdef(\p4)
		),
		\clock: { 0.2.rrand(10.0) }
	),
	// left hand
	(
		\transition: \t8,
		\inputPlaces: [\p5],
		\outputPlaces: [],
		\inhibitorPlaces: [],
		\clock: { inf },
		\dependants: (
			\sound: Pdef(\left)
		)
	)
);

a.mediator_( SPNMediator( \sound ) );
)

a.prTask;

(
a.setMarking(
	a.marking.keysValuesChange({| key, value |
		value = if( key == \p0 ){ 1 }{ 0 };
	})
)
)

TempoClock.default.tempo = 1.2

a.play
a.stop
a.reset
//////////////////////////////////////////////////////////////////////
// changing synth parameters on the fly - multiple identical components

(
// adapted from 'iterated functions.scd'
SynthDef(\noise,{| out = 0, pan = 0.0, amp = 1.0, coef1 = 30.0, coef2 = 400.0, x0 = 110.02, r0 = 1000.40, gate = 1 |
	var env, xn, r = 0;
	xn = SinOsc.ar( r0 * x0 ) ;
	r = SinOsc.ar( coef1 * r0 * xn.abs.sqrt, 0, coef2 );
	2.do({
		xn = SinOsc.ar(r*xn) ;
		r = SinOsc.ar( coef1 *r * xn.abs.sqrt, 0, coef2 );
	});

	env  = EnvGen.kr( Env.cutoff( 1 ) , gate, doneAction: 2 );
	xn = HPF.ar(xn,80,0.03);

	Out.ar( out, Pan2.ar( xn * env, pan, amp ) )
}).add
)

(
~updateOutputPlaces = {| outputPlaces |
	outputPlaces.choose.addOneToken;
};

~updateFunction = {| aPNPlace | {
	if ( aPNPlace.tokens < 1 ){ aPNPlace.addOneToken }
}
};

~petriComponent = {| inputPlace, index, nodeIndex |
	[
		(
			\transition: ( \t1 ++ index ).asSymbol,
			\inputPlaces: [ inputPlace ],
			\outputPlaces: [ ( \p1 ++ index ).asSymbol ],
			\inhibitorPlaces: [],
			\isTimed: true
		),
		(
			\transition: ( \t2 ++ index ).asSymbol,
			\inputPlaces: [ ( \p1 ++ index ).asSymbol ],
			\outputPlaces: [ ( \p2 ++ index ).asSymbol ],
			\inhibitorPlaces: [],
			\clock: { 0.1 },
			\dependants: (
				\sound:	(
					\type: \on,
					\instrument: \noise,
					\id:  nodeIndex,
					\x0: { 01.0.rrand( 400.0 ) },
					\r0: { 01.0.rrand( 200.0 ) }
				)
			)
		),
		(
			\transition: ( \t3 ++ index ).asSymbol,
			\inputPlaces: [ ( \p2 ++ index ).asSymbol ],
			\outputPlaces: [ ( \p3 ++ index ).asSymbol, ( \p4 ++ index ).asSymbol, ( \p5 ++ index ).asSymbol, ( \p6 ++ index ).asSymbol ],
			\inhibitorPlaces: [],
			\updateOutputPlaces: ~updateOutputPlaces
		),
		(
			\transition: ( \t4 ++ index ).asSymbol,
			\inputPlaces: [ (\p3 ++ index ).asSymbol ],
			\outputPlaces: [ ( \p2 ++ index ).asSymbol],
			\inhibitorPlaces: [],
			\clock: { rrand( 0.01, 3 ) },
			\dependants: (
				\sound: (
					\type: \set,
					\id: nodeIndex,
					\args: #[ \pan ],
					\pan: { 1.0.rand2 }
				)
			)
		),
		(
			\transition: ( \t5 ++ index ).asSymbol,
			\inputPlaces: [ (\p4 ++ index ).asSymbol ],
			\outputPlaces: [ ( \p2 ++ index ).asSymbol],
			\inhibitorPlaces: [],
			\clock: { rrand( 0.01, 3 ) },
			\dependants: (
				\sound: (
					\type: \set,
					\id: nodeIndex,
					\args: #[ \amp ],
					\amp: { 0.05.rrand(0.1) }
				)
			)
		),
		(
			\transition: ( \t6 ++ index ).asSymbol,
			\inputPlaces: [ (\p5 ++ index ).asSymbol ],
			\outputPlaces: [ ( \p2 ++ index ).asSymbol ],
			\inhibitorPlaces: [],
			\clock: { 0.01.rrand(3) },
			\dependants: (
				\sound: (
					\type: \set,
					\id: nodeIndex,
					\args: #[ \coef1 ],
					\coef1: { 0.05.rrand(1000.4) }
				)
			)
		),
		(
			\transition: ( \t7 ++ index ).asSymbol,
			\inputPlaces: [ (\p6 ++ index ).asSymbol ],
			\outputPlaces: [ ( \p2 ++ index ).asSymbol ],
			\inhibitorPlaces: [],
			\clock: { 0.01.rrand(3) },
			\dependants: (
				\sound: (
					\type: \set,
					\id: nodeIndex,
					\args: #[ \coef2 ],
					\coef2: { 0.05.rrand(1000.4) }
				)
			)
		)
	]
};
)

(
var components = 10;

PNPlace.clearAll;

a = SPetriNet(
	*(
		components.collect {|i| ~petriComponent.( \p0, i, s.nextNodeID ) }.flatten
	)
);

a.mediator_( SPNMediator( \sound ) );
a.prTask;
)

(
a.setMarking(
	a.marking.keysValuesChange({| key, value |
		value = if( key == \p0 ){ 1 }{ 0 };
	})
)
)

a.play;
a.stop; s.freeAll;
a.reset;

//////////////////////////////////////////////////////////////////////
// a cell from a kanban system.
// see ch. 8 'Modelling Flexible Manufacturing Systems' from the book
// 'Modelling With Generalized Stochastic Petri Nets'
//////////////////////////////////////////////////////////////////////
// index: the index of the cell
// cards: the number of cards in the bulletin board
// inputPlaces: a PNPlace or an Array of PNPlaces
// outputPlaces: a PNPlace or an Array of PNPlaces
// clockOutM, dependants: clock and dependants of transition outM
// canFail: a boolean indicating the absence or not of the failure subblock
// clockRepair, dependantsRepair: clock and dependants of transition Reapair
// clockFailure, dependantsFailure: clock and dependants of transition Failure
(
~kanbanCell = {| index, cards, inputPlaces, outputPlaces, clockOutM, dependantsOutM, canFail = false, clockRepair, dependantsRepair, clockFailure, dependantsFailure |
	var inhibitorPlace, net;

	PNPlace( ( \bb ++ index ).asSymbol, cards );
	PNPlace( ( \idleM ++ index ).asSymbol, 1 );

	if ( canFail ){
		inhibitorPlace = ( \failed ++ index ).asSymbol;
		PNPlace( ( \ok ++ index ).asSymbol, 1 );
		net = [
			(
				\transition: ( \repair ++ index ).asSymbol,
				\inputPlaces: [ ( \failed ++ index ).asSymbol ],
				\outputPlaces: [ ( \ok ++ index ).asSymbol ],
				\inhibitorPlaces: [],
				\isTimed: true,
				\clock: clockRepair,
				\dependants: dependantsRepair
			),
			(
				\transition: ( \failure ++ index ).asSymbol,
				\inputPlaces: [ ( \ok ++ index ).asSymbol ],
				\outputPlaces: [ ( \failed ++ index ).asSymbol ],
				\inhibitorPlaces: [],
				\isTimed: true,
				\clock: clockFailure,
				\dependants: dependantsFailure
			)
		]
	};

	net = net ++ [
		(
			\transition: ( \enterCell ++ index ).asSymbol,
			\inputPlaces: inputPlaces.asArray ++ [ ( \bb ++ index ).asSymbol ],
			\outputPlaces: [ ( \ib ++ index ).asSymbol ],
			\inhibitorPlaces: [],
			\isTimed: false
		),
		(
			\transition: ( \inM ++ index ).asSymbol,
			\inputPlaces: [ ( \ib ++ index ).asSymbol, ( \idleM ++ index ).asSymbol ],
			\outputPlaces: [ ( \busyM ++ index ).asSymbol ],
			\inhibitorPlaces: [],
			\isTimed: false
		),
		(
			\transition: ( \outM ++ index ).asSymbol,
			\inputPlaces: [ ( \busyM ++ index ).asSymbol ],
			\outputPlaces: [ ( \idleM ++ index ).asSymbol, ( \ob ++ index ).asSymbol ],
			\inhibitorPlaces: inhibitorPlace.asArray,
			\isTimed: true,
			\clock: clockOutM,
			\dependants: dependantsOutM
		),
		(
			\transition: ( \exitCell ++ index ).asSymbol,
			\inputPlaces: [ ( \ob ++ index ).asSymbol ],
			\outputPlaces: [ ( \bb ++ index ).asSymbol ] ++ outputPlaces.asArray,
			\inhibitorPlaces: [],
			\isTimed: false
		)
	];
}
)

//////////////////////////////////////////////////////////////////////
// exaple using the kanban cell

(
PNPlace.clearAll;

~dur = 0.01;

a = SPetriNet(
	*(
		~kanbanCell.( 0, 4, \p0, \p1, { 1.0 },
			( \sound: ( \instrument: \default, \midinote: 60.rrand( 80 ), \amp: 0.2, \pan: 1.0.rand2, \dur: ~dur ) )
		)
		++
		~kanbanCell.( 1, 2, \p1, \p2, { 1.0 },
			( \sound: ( \instrument: \default, \midinote: 60.rrand( 80 ), \amp: 0.2, \pan: 1.0.rand2, \dur: ~dur ) )
		)
		++
		~kanbanCell.( 2, 3, \p2, \p3, { 1.0 },
			( \sound: ( \instrument: \default, \midinote: 60.rrand( 80 ), \amp: 0.2, \pan: 1.0.rand2, \dur: ~dur ) )
		)
		++
		~kanbanCell.( 3, 1, \p3, \p4, { 1.0 },
			( \sound: ( \instrument: \default, \midinote: 60.rrand( 80 ), \amp: 0.2, \pan: 1.0.rand2, \dur: ~dur ) )
		)
		++
		~kanbanCell.( 4, 4, \p4, \p0, { 1.0 },
			( \sound: ( \instrument: \default, \midinote: 60.rrand( 80 ), \amp: 0.2, \pan: 1.0.rand2, \dur: ~dur ) )
		)
	)
);

a.mediator_( SPNMediator( \sound ) );
a.prTask;
)

(
a.setMarking(
	a.marking.keysValuesChange({| key, value |
		value = if( key == \p0 ){ 1 };
	})
)
)

a.play

a.samplePathAlgorithm(1, 0, \postCurrentState )

(
5.do {|i|
	a.transitions.at( ( \outM ++ i ).asSymbol ).clock_( { 0.05.rrand(0.15) })
}
)

PNPlace(\p0).addOneToken

(
a.transitions.at( \outM0 ).dependants.put(
	\sound, (\instrument: \default, \midinote: 60, \dur: 1, \amp: 0.4 )
)
)

a.stop;

//////////////////////////////////////////////////////////////////////
(
PNPlace.clearAll;

~dur = 0.01;
~kanbans = 20;

a = SPetriNet(
	* ~kanbans.collect {|i|
		~kanbanCell.(
			i,
			1,
			( \p ++ i ).asSymbol,
			if( i == (~kanbans - 1) ){ \p0 }{ ( \p ++ (i+1) ).asSymbol },
			{ 0.05 },
			( \sound: ( \instrument: \default, \midinote: 50.rrand(100), \amp: 0.1.rrand(0.2), \pan: 1.0.rand2, \dur: ~dur ) )
		)
	}.reduce('++')
);

a.mediator_( SPNMediator( \sound ) );
a.prTask;
)

(
a.setMarking(
	a.marking.keysValuesChange({| key, value |
		value = if( key == \p0 ){ 1 };
	})
)
)

a.play

(
~kanbans.do {|i|
	a.transitions.at( ( \outM ++ i ).asSymbol ).clock_( { 0.01.rrand(0.02) })
}
)

PNPlace(\p0).addOneToken

(
a.transitions.at( \outM0 ).dependants.put(
	\sound, (\instrument: \default, \midinote: 50, \dur: 1, \amp: 0.4 )
)
)

a.stop;

//////////////////////////////////////////////////////////////////////
// use this to test parameter change in a node
// add a global storage Environment in SPetriNet?
(
PNPlace.clearAll;

a = SPetriNet(
	(
		\transition: \t1,
		\inputPlaces: [\p0],
		\outputPlaces: [\p1,\p4],
		\inhibitorPlaces: [],
		\isTimed: false,
		\dependants: (
			\sound:	(
				\type: \on,
				\instrument: \default,
				\id:  ~synthID = 1000
			)
		)
	),
	(
		\transition: \t2,
		\inputPlaces: [\p1,\p3],
		\outputPlaces: [\p2],
		\inhibitorPlaces: [\p5],
		\clock: 1
	),
	(
		\transition: \t3,
		\inputPlaces: [\p2],
		\outputPlaces: [\p3],
		\inhibitorPlaces: [],
		\isTimed: false,
		\dependants: (
			\sound: (
				\type: \set,
				\id: ~synthID,
				\args: #[ \freq ],
				\freq: { 200.0.rrand( 700.0 ) }
			)
		)
	),
	(
		\transition: \t4,
		\inputPlaces: [\p4],
		\outputPlaces: [\p5],
		\inhibitorPlaces: [],
		\clock: 10
	),
	(
		\transition: \t5,
		\inputPlaces: [\p5],
		\outputPlaces: [\p6],
		\inhibitorPlaces: [],
		\isTimed: false,
		\dependants: (
			\sound: (
				\type: \off,
				\id: ~synthID
			)
		)
	),
	(
		\transition: \t6,
		\inputPlaces: [\p6],
		\outputPlaces: [\p0],
		\inhibitorPlaces: [],
		\clock: 2
	)
);

a.mediator_( SPNMediator( \sound ) );
a.prTask;
)

(
a.setMarking(
	a.marking.keysValuesChange({| key, value |
		value = if( key == \p0 ){ 1 }{ 0 };
	})
)
)

a.play;
a.stop; s.sendMsg( "/n_free", ~synthID );
a.reset
//////////////////////////////////////////////////////////////////////
