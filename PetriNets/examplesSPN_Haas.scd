// Examples of Stochastic Petri Nets
// from "Stochastic Petri Nets: Modelling, Stability, Simulation"
// by Peter Haas

// Ex. 1.4 p.19
(
PNPlace.clearAll;

a = SPetriNet(
	(
		\transition: \e1,
		\inputPlaces: [\d1],
		\outputPlaces: [\d2],
		\inhibitorPlaces: [],
		\isTimed: true
	),
	(
		\transition: \e2,
		\inputPlaces: [\d2],
		\outputPlaces: [\d1],
		\inhibitorPlaces: [],
		\isTimed: true
	)
)
)

(
[\e1,\e2].do {|tran, i|
	a.transitions.at(tran)
	.clock_( {
		// 0.1.rrand(2.0)
		[
			( (1..5)/2 ).choose,
			( (3..7)/2 ).choose
		].at(i)
	} )
}
)

p = 0.6;

(
var transition;
transition = a.transitions.at(\e1);
transition.updateOutputPlaces_( {
	if( p.coin ){
		transition.outputPlaces.do {|elem| elem.addOneToken }
	}{
		transition.inputPlaces.do {|elem| elem.addOneToken }
	}
}
)
)

a.setMarking( (\d1:2, \d2:0) )

a.samplePathAlgorithm( 100, 0, \postCurrentState, *( true ! 7 ) )

// Ex. 2.1 p.24
(
PNPlace.clearAll;

a = SPetriNet(
	(
		\transition: \e1,
		\inputPlaces: [\d1],
		\outputPlaces: [\d2],
		\inhibitorPlaces: [],
		\isTimed: true
	),
	(
		\transition: \e2,
		\inputPlaces: [\d2,\d7],
		\outputPlaces: [\d3],
		\inhibitorPlaces: [],
		\isTimed: false
	),
	(
		\transition: \e3,
		\inputPlaces: [\d3],
		\outputPlaces: [\d1,\d7],
		\inhibitorPlaces: [],
		\isTimed: true
	),
	(
		\transition: \e4,
		\inputPlaces: [\d4],
		\outputPlaces: [\d5],
		\inhibitorPlaces: [],
		\isTimed: true
	),
	(
		\transition: \e5,
		\inputPlaces: [\d5,\d7],
		\outputPlaces: [\d6],
		\inhibitorPlaces: [\d2],
		\isTimed: false
	),
	(
		\transition: \e6,
		\inputPlaces: [\d6],
		\outputPlaces: [\d4,\d7],
		\inhibitorPlaces: [],
		\isTimed: true
	)
)
)

(
b = (
	\b1: 4, 							// capacity of Buffer 1
	\b2: 3, 							// capacity of Buffer 2
	\l1: { [0.25,0.5].choose },			//
	\l2: { [0.25,0.5].choose },
	\a1: { [0.5,0.75].choose },
	\a2: { [0.25,0.5].choose }
)
)

(
a.transitions.at(\e1).clock_( b.a1 );
a.transitions.at(\e4).clock_( b.a2 );
a.transitions.at(\e3).clock_( b.l1 );
a.transitions.at(\e6).clock_( b.l2 );
)

// Each buffer contains at most bi tokens. When places \e1, \e4 are
// enabled, and there are more than \bi, respectively, tokens in the bus,
// then no item is created from transition \ei
(
var transition;
transition = a.transitions.at( \e1 );
transition.updateOutputPlaces_( {
	var tokens = 0;
	tokens = 3.do {|j|
		tokens + PNPlace( ("d"++ (1+j).asString).asSymbol ).tokens;
	};
	if( tokens <= b.b1 ){
		transition.outputPlaces.do {|elem|
			elem.addOneToken;
		}
	}
}
)
)

(
var transition;
transition = a.transitions.at( \e4 );
transition.updateOutputPlaces_( {
	var tokens = 0;
	tokens = 3.do {|j|
		tokens + PNPlace( ("d"++ (4+j).asString).asSymbol ).tokens;
	};
	if( tokens <= b.b2 ){
		transition.outputPlaces.do {|elem|
			elem.addOneToken;
		}
	}
}
)
)

a.setMarking( (\d1: 3, \d2:0, \d3: 0, \d4:1, \d5:0, \d6:0, \d7:1) )

a.samplePathAlgorithm( 10, 0, \postCurrentState, *( true ! 7 ) )
// a real time implementation
Pdef.all.do {|pdef| pdef.quant = 0; }

~scale = Scale.hijaz.degrees
~scale = (0..11).scramble.keep( 9 )

(
Pdef(\e1,
	Pbind(
		\freq, Prand( 50 + ~scale, 1).midicps,
		\amp, 0.2,
		\pan, 0.8,
		\dur, 0.3
	)
);

Pdef(\e3,
	Pbind(
		\freq, Prand( 62 + ~scale, 1).midicps,
		\amp, 0.1,
		\pan, 0.8,
		\dur, 0.3
	)
);

Pdef(\e4,
	Pbind(
		\freq, Prand( 74 + ~scale, 1).midicps,
		\amp, 0.2,
		\pan, 0.8,
		\dur, 0.3
	)
);

Pdef(\e6,
	Pbind(
		\freq, Prand( 86 + ~scale, inf).midicps,
		\amp, 0.05,
		\pan, 0.8,
		\dur, 0.05
	)
);
)

(
PNPlace.clearAll;

a = SPetriNet(
	(
		\transition: \e1,
		\inputPlaces: [\d1],
		\outputPlaces: [\d2],
		\inhibitorPlaces: [],
		\isTimed: true,
		\clock: { 1.0.rrand( 2.0 ) },
		\dependants: (
			\sound: Pdef(\e1)
		)
	),
	(
		\transition: \e2,
		\inputPlaces: [\d2,\d7],
		\outputPlaces: [\d3],
		\inhibitorPlaces: [],
		\isTimed: false
	),
	(
		\transition: \e3,
		\inputPlaces: [\d3],
		\outputPlaces: [\d1,\d7],
		\inhibitorPlaces: [],
		\isTimed: true,
		\clock: { 1.0.rrand( 2.0 ) },
		\dependants: (
			\sound: Pdef(\e3)
		)
	),
	(
		\transition: \e4,
		\inputPlaces: [\d4],
		\outputPlaces: [\d5],
		\inhibitorPlaces: [],
		\isTimed: true,
		\clock: { 1.0.rrand( 2.0 ) },
		\dependants: (
			\sound: Pdef(\e4)
		)
	),
	(
		\transition: \e5,
		\inputPlaces: [\d5,\d7],
		\outputPlaces: [\d6],
		\inhibitorPlaces: [\d2],
		\isTimed: false
	),
	(
		\transition: \e6,
		\inputPlaces: [\d6],
		\outputPlaces: [\d4,\d7],
		\inhibitorPlaces: [],
		\isTimed: true,
		\clock: { 1.0.rrand( 2.0 ) },
		\dependants: (
			\sound: Pdef(\e6)
		)
	)
);

a.mediator_( SPNMediator( \sound ) );
a.prTask;
)

a.setMarking( (\d1: 1, \d2:2, \d3: 0, \d4:1, \d5:0, \d6:0, \d7:1) );
a.play;

// Ex. 2.2 p.29
(
PNPlace.clearAll;

a = SPetriNet(
	(
		\transition: \e1,
		\inputPlaces: [\d1],
		\outputPlaces: [\d2],
		\inhibitorPlaces: [],
		\clock: { 1.0 }
	),
	(
		\transition: \e2,
		\inputPlaces: [\d2],
		\outputPlaces: [\d1],
		\inhibitorPlaces: [],
		\clock: { 0.5 }
	),
	(
		\transition: \e3,
		\inputPlaces: [\d3],
		\outputPlaces: [\d4],
		\inhibitorPlaces: [],
		\clock: { 1.0 }
	),
	(
		\transition: \e4,
		\inputPlaces: [\d4],
		\outputPlaces: [\d3],
		\inhibitorPlaces: [\d2],
		\clock: { 0.5 }
	)
)
)

(
var transition;
transition = a.transitions.at( \e4 );
transition.clock_( { ((5..10)/20).choose } );
)

~duration = 60;

a.setMarking( (\d1: 1, \d2: 0, \d3: 1, \d4:0 ) )
a.samplePathAlgorithm( ~duration, 0, \postCurrentState, *( true ! 7 ) )

a.initTimeDurPairs
a.samplePathAlgorithm( ~duration, 0, \collectTimeDurPairs )
a.samplePathWithMultiPerform( ~duration, 0, \postCurrentState, \collectTimeDurPairs )
a.timeDurPairs.at(\e4)

s.boot

(
~noteEvent = (
	\e1: {[61,62,64].midicps.choose},
	\e2: {[66,68].midicps.choose},
	\e3: {[73,75,76].midicps.choose},
	\e4: 50.midicps
)
)

~score = Score.new;

(
var transitions, offset = 0.5, note, durList, aPetriNet, amp = 0.2;

~score.score = nil;

aPetriNet = a;

transitions = a.transitions.keys;

transitions.do {|aSymbol|
	durList = aPetriNet.timeDurPairs.at( aSymbol );
	durList.do {|aPair|
		note = Synth.basicNew(\default);
		~score.add(
			[
				aPair[0] + offset,
				note.newMsg( 1, [ \freq, ~noteEvent.at( aSymbol ).value, \amp, amp] )
			]);
		~score.add(
			[
				aPair[0] + aPair[1] + offset,
				note.releaseMsg
			]);
	}
};

~score.add( [ ~duration + 1, [0] ] );
~score.sort;
)

~score.play
~score.stop
// RT implementation
Pdef.all.do {|pdef| pdef.quant = 0; }

~scale = Scale.major.degrees
~scale = (0..11).scramble.keep( 9 )

(
Pdef(\e1,
	Pbind(
		\freq, Plazy({ [61,62,64].choose } ).midicps,
		\amp, 0.2,
		\pan, 1.0.rand2,
		\dur, 0.02
	)
);

Pdef(\e2,
	Pbind(
		\freq, Plazy({ [66,68].choose }).midicps,
		\amp, 0.1,
		\pan, 1.0.rand2,
		\dur, 0.02
	)
);

Pdef(\e3,
	Pbind(
		\freq, Plazy({ [73,75,76].choose }).midicps,
		\amp, 0.1,
		\pan, 1.0.rand2,
		\dur, 0.02
	)
);

Pdef(\e4,
	Pbind(
		\freq, Plazy({ 50 }).midicps,
		\amp, 0.6,
		\pan, 1.0.rand2,
		\dur, 0.02
	)
);
)

// this is not the net from the example.
(
PNPlace.clearAll;

a = SPetriNet(
	(
		\transition: \e1,
		\inputPlaces: [\d1],
		\outputPlaces: [\d2],
		\inhibitorPlaces: [],
		\clock: { 0.5 },
		\dependants: (
			\sound: Pdef(\e1)
		)
	),
	(
		\transition: \e2,
		\inputPlaces: [\d2],
		\outputPlaces: [\d1],
		\inhibitorPlaces: [],
		\clock: { 0.25 },
		\dependants: (
			\sound: Pdef(\e2)
		)
	),
	(
		\transition: \e3,
		\inputPlaces: [\d3],
		\outputPlaces: [\d4],
		\inhibitorPlaces: [],
		\clock: { 0.5 },
		\dependants: (
			\sound: Pdef(\e3)
		)
	),
	(
		\transition: \e4,
		\inputPlaces: [\d4],
		\outputPlaces: [\d3],
		\inhibitorPlaces: [\d2],
		\clock: { 0.25 },
		\dependants: (
			\sound: Pdef(\e4)
		)
	)
);

a.mediator_( SPNMediator( \sound ) );
a.prTask;
)

a.setMarking( (\d1: 1, \d2:0, \d3: 1, \d4:0 ) );
a.play;

// three customers
(
Pdef(\e1,
	Pbind(
		\freq, Plazy({ [61,62,64].choose } ).midicps,
		\amp, 0.2,
		\pan, 1.0.rand2,
		\dur, 2
	)
);

Pdef(\e2,
	Pbind(
		\freq, Plazy({ [66,68].choose }).midicps,
		\amp, 0.1,
		\pan, 1.0.rand2,
		\dur, 2
	)
);

Pdef(\e3,
	Pbind(
		\freq, Plazy({ [73,75,76].choose }).midicps,
		\amp, 0.1,
		\pan, 1.0.rand2,
		\dur, 2
	)
);

Pdef(\e4,
	Pbind(
		\freq, Plazy({ 50 }).midicps,
		\amp, 0.2,
		\pan, 1.0.rand2,
		\dur, 2
	)
);

Pdef(\e5,
	Pbind(
		\freq, Plazy({ [61,62,64].choose } ).midicps,
		\amp, 0.2,
		\pan, 1.0.rand2,
		\dur, 2
	)
);

Pdef(\e6,
	Pbind(
		\freq, Plazy({ [66,68].choose }).midicps,
		\amp, 0.1,
		\pan, 1.0.rand2,
		\dur, 2
	)
)
)

(
PNPlace.clearAll;

a = SPetriNet(
	(
		\transition: \e1,
		\inputPlaces: [\d1],
		\outputPlaces: [\d2],
		\inhibitorPlaces: [],
		\clock: { 1.0 },
		\dependants: (
			\sound: Pdef(\e1)
		)
	),
	(
		\transition: \e2,
		\inputPlaces: [\d2],
		\outputPlaces: [\d1],
		\inhibitorPlaces: [],
		\clock: { 0.5 },
		\dependants: (
			\sound: Pdef(\e2)
		)
	),
	(
		\transition: \e3,
		\inputPlaces: [\d3],
		\outputPlaces: [\d4],
		\inhibitorPlaces: [],
		\clock: { 1.0 },
		\dependants: (
			\sound: Pdef(\e3)
		)
	),
	(
		\transition: \e4,
		\inputPlaces: [\d4],
		\outputPlaces: [\d3],
		\inhibitorPlaces: [\d2],
		\clock: { 0.5 },
		\dependants: (
			\sound: Pdef(\e4)
		)
	),
	(
		\transition: \e5,
		\inputPlaces: [\d5],
		\outputPlaces: [\d6],
		\inhibitorPlaces: [],
		\clock: { 1.0 },
		\dependants: (
			\sound: Pdef(\e5)
		)
	),
	(
		\transition: \e6,
		\inputPlaces: [\d6],
		\outputPlaces: [\d5],
		\inhibitorPlaces: [\d2],
		\clock: { 0.5 },
		\dependants: (
			\sound: Pdef(\e6)
		)
	)
);

a.mediator_( SPNMediator( \sound ) );
a.prTask;
)

a.setMarking( (\d1: 1, \d2:0, \d3: 1, \d4:0, \d5: 1, \d6: 0 ) );

a.play;

// Ex. 2.3 p.30

// In the current setting, when clockSpeed = 0 for a transition \e4, the only
// thing that happens is that the clockReading remains unchanged and \e4, when enabled, becomes an oldTransition.
// If you 're going to implement the behaviour from this example, you want \e4
// to interrupt (pause a sound?) when clockSpeed = 0 and when clockSpeed \neq 0
// \e4 resumes from the point at which it was interrupted.
// Change the methods 'computeOldTransitions' and 'zeroRemainingClocks' ?

// 	computeOldTransitions {
// 		oldTransitions.clear;
// 		( enabledTransitions -- firingTransitions ).do {|e|
// 			if( e.isEnabled ){
// 				e.clockReading = e.clockReading - ( holdingTime * e.clockSpeed );
// 				if( e.clockSpeed != 0 ){ oldTransitions.add( e ); }
// 			}
// 		}
// 	}

// 	zeroRemainingClocks {
// 		( transitions.as(Set) -- enabledTransitions ).do {|e|
// 			if( e.clockSpeed != 0 ){ e.clockReading = 0; }
// 		}
// 	}

// Ex. 2.4 p.31

// maybe discriminate between timed and immediate transitions of SPetriNet
// with additional instance variables?
(
PNPlace.clearAll;

a = SPetriNet(
	(
		\transition: \e1,
		\inputPlaces: [\d1],
		\outputPlaces: [\d2,\d1],
		\inhibitorPlaces: [],
		\clock: { 3.0.rrand(4.0) }
	),
	(
		\transition: \e2,
		\inputPlaces: [\d2],
		\outputPlaces: [\d2,\d3],
		\inhibitorPlaces: [],
		\isTimed: false
	),
	(
		\transition: \e3,
		\inputPlaces: [\d3],
		\outputPlaces: [\d4],
		\inhibitorPlaces: [\d2],
		\isTimed: false
	),
	(
		\transition: \e4,
		\inputPlaces: [\d4],
		\outputPlaces: [],
		\inhibitorPlaces: [],
		\clock: { 0.15.rrand(0.3) }
	)
)
)

(
var transition;
transition = a.transitions.at(\e2);
transition.updateOutputPlaces_( {| outputPlaces, aPetriNet |
	var p, d3Tokens;
	d3Tokens = aPetriNet.marking.at(\d3);
	p = ( d3Tokens/(d3Tokens + 7.5) - 1 ).abs;
	if( p.coin ){
		outputPlaces.do {|aPlace| aPlace.addOneToken; }
	}{
		PNPlace(\d3).addOneToken;
	}
}
)
)

(
var transition;
transition = a.transitions.at(\e4);
transition.clock_({ [0.1,0.2].choose })
)


~duration = 60;

a.transitions.at(\e4).inhibitorPlaces

a.setMarking( (\d1: 1, \d2: 0, \d3: 0, \d4: 0 ) )
a.samplePathAlgorithm( 10, 0, \postCurrentState, *( true ! 7 ) )
a.marking

a.setMarking( (\d1: 1, \d2: 0, \d3: 0, \d4:0 ) )
a.initTimeDurPairs
a.samplePathAlgorithm( ~duration, 0, \collectTimeDurPairs )
a.samplePathWithMultiPerform( ~duration, 0, \postCurrentState, \collectTimeDurPairs )
a.timeDurPairs.at(\e4)

s.boot

(
~noteEvent = (
	\e1: { (50 + Scale.phrygian.degrees).midicps.choose },
	\e4: { (74 + Scale.phrygian.degrees).midicps.choose }
)
)

~score = Score.new;

(
var transitions, offset = 0.5, note, durList, aPetriNet, amp = 0.2;

~score.score = nil;

aPetriNet = a;

transitions = a.transitions.select {|aTrans| aTrans.isTimed }.keys;

transitions.do {|aSymbol|
	durList = aPetriNet.timeDurPairs.at( aSymbol );
	durList.do {|aPair|
		note = Synth.basicNew(\default);
		~score.add(
			[
				aPair[0],
				note.newMsg( 1, [ \freq, ~noteEvent.at( aSymbol ).value, \amp, amp] )
			]);
		~score.add(
			[
				aPair[0] + aPair[1],
				note.releaseMsg
			]);
	}
};

~score.add( [ ~duration + 1, [0] ] );
~score.sort;
)

~score.play
~score.stop

// Ex. 2.6 p.33
~rings = 3;

(
~port = {| index, inputPlace |
	var transition;
	PNPlace.clearAll;

	[
		(
			\transition: ( "e1" ++ index ).asSymbol,
			\inputPlaces: [ ( "d2" ++ index ).asSymbol ],
			\outputPlaces: [ ( "d1" ++ index ).asSymbol ],
			\inhibitorPlaces: [],
			\clock: { [0.25, 0.5, 0.75, 1.0 ].choose }
		),
		(
			\transition: ( "e2" ++ index).asSymbol,
			\inputPlaces: [ ( "d1" ++ index ).asSymbol, ( "d3" ++ index ).asSymbol ],
			\outputPlaces: [ ( "d2" ++ index).asSymbol, ( "d4" ++ index ).asSymbol ],
			\inhibitorPlaces: [],
			\clock: { [ 0.25, 0.5, 0.75, 1.0 ].choose }
		),
		(
			\transition: ( "e3" ++ index ).asSymbol,
			\inputPlaces: [ inputPlace  ],
			\outputPlaces: [ ( "d3" ++ index ).asSymbol, ( "d4" ++ index ).asSymbol ],
			\inhibitorPlaces: [],
			\clock: { 1.5 }
		)
	]
}
)

(
var net;
net = ~rings.collect{|i|
	if( i == 0 ){
		~port.( i, ("d4" ++ ( ~rings - 1 ) ).asSymbol )
	}{
		~port.( i, ( "d4" ++ (i-1) ).asSymbol )
	}
}.flatten;

a = SPetriNet( *( net ) )
)

(
var transition;
~rings.do {|i|
	transition = a.transitions.at( ( "e3" ++ i ).asSymbol);
	transition.updateOutputPlaces_({| outputPlaces, aSPetriNet |
		var d1Tokens;
		d1Tokens = aSPetriNet.marking.at( ( "d1" ++ i ).asSymbol );
		if( d1Tokens == 1 ){
			PNPlace( ( "d3" ++ i ).asSymbol ).addOneToken;
		}{
			PNPlace( ( "d4" ++ i ).asSymbol ).addOneToken;
		}
	}
	)
}
)

~duration = 60;

(
~perRingInitMarking = {|index, d3 = 0|
	(
		( "d1" ++ index ).asSymbol : 1,
		( "d2" ++ index ).asSymbol : 0,
		( "d3" ++ index ).asSymbol : d3,
		( "d4" ++ index ).asSymbol : 0
	)
}
)

a.samplePathAlgorithm( 10, 0, \postCurrentState, *( true ! 7 ) )
a.marking

(
a.setMarking(
	().putAll( *(
		~rings.collect {|i|
			if( i == 0 ){
				~perRingInitMarking.( i, 1 )
			}{
				~perRingInitMarking.( i )
			}
		}
	)
	)
)
)

a.initTimeDurPairs
a.samplePathAlgorithm( ~duration, 0, \collectTimeDurPairs )
a.samplePathWithMultiPerform( ~duration, 0, \postCurrentState, \collectTimeDurPairs )
a.timeDurPairs.at(\e11)

s.boot

(
var scale;
scale = Scale.phrygian.degrees;
~perRingNoteEvent ={|index|
	(
		( "e1" ++ index ).asSymbol :
		{( 38 + scale ).midicps.choose},
		( "e2" ++ index ).asSymbol :
		( 50 + scale ).midicps.choose,
		( "e3" ++ index ).asSymbol :
		( 62 + scale ).midicps.choose,
	)
};
)

~noteEvent = ().putAll( *( ~rings.collect {|i|  ~perRingNoteEvent.(i)  } ) )

~score = Score.new;

(
var transitions, offset = 0.5, note, durList, aPetriNet, amp = 0.2;

~score.score = nil;

aPetriNet = a;

transitions = a.transitions.select {|aTrans| aTrans.isTimed }.keys;

transitions.do {|aSymbol|
	durList = aPetriNet.timeDurPairs.at( aSymbol );
	durList.do {|aPair|
		note = Synth.basicNew(\default);
		~score.add(
			[
				aPair[0],
				note.newMsg( 1, [ \freq, ~noteEvent.at( aSymbol ).value, \amp, amp] )
			]);
		~score.add(
			[
				aPair[0] + aPair[1],
				note.releaseMsg
			]);
	}
};

~score.add( [ ~duration + 1, [0] ] );
~score.sort;
)

~score.play
~score.stop


// Ex. 2.7 p.36
(
SynthDef(\help_mdapiano, { |out=0, freq=440, gate=1|
	var son = MdaPiano.ar(freq, gate, release: 0.9, stereo: 0.3, sustain: 0);
	DetectSilence.ar(son, 0.01, doneAction:2);
	Out.ar(out, son * 0.1);
}).add;
)


PNPlace.clearAll

~rings = 5;

(
~port = {| index, inputPlace |
	var transition;
	[
		(
			\transition: ( "e1" ++ index ).asSymbol,
			\inputPlaces: [ ( "d2" ++ index ).asSymbol ],
			\outputPlaces: [ ( "d1" ++ index ).asSymbol ],
			\inhibitorPlaces: [],
			\clock: { [ 0.25, 0.5, 0.75, 1.0 ].choose }
		),
		(
			\transition: ( "e2" ++ index).asSymbol,
			\inputPlaces: [ ( "d3" ++ index ).asSymbol ],
			\outputPlaces: [ ( "d2" ++ index).asSymbol, ( "d4" ++ index ).asSymbol ],
			\inhibitorPlaces: [],
			\clock:  [ 0.125, 1/7, 2/9 ].choose
		),
		(
			\transition: ( "e3" ++ index ).asSymbol,
			\inputPlaces: [ inputPlace  ],
			\outputPlaces: [ ( "d5" ++ index ).asSymbol ],
			\inhibitorPlaces: [],
			\clock: { [ 0.125, 2/11 ].choose }
		),
		(
			\transition: ( "e4" ++ index ).asSymbol,
			\inputPlaces: [ ("d1" ++ index ).asSymbol, ("d5" ++ index ).asSymbol  ],
			\outputPlaces: [ ( "d3" ++ index ).asSymbol ],
			\inhibitorPlaces: [],
			\isTimed: false
		),
		(
			\transition: ( "e5" ++ index ).asSymbol,
			\inputPlaces: [ ("d5" ++ index ).asSymbol  ],
			\outputPlaces: [ ( "d4" ++ index ).asSymbol ],
			\inhibitorPlaces: [ ( "d1" ++ index ).asSymbol  ],
			\isTimed: false
		)
	]
}
)

(
var net;
net = ~rings.collect{|i|
	if( i == 0 ){
		~port.( i, ("d4" ++ ( ~rings - 1 ) ).asSymbol )
	}{
		~port.( i, ( "d4" ++ (i-1) ).asSymbol )
	}
}.flatten;

a = SPetriNet( *( net ) )
)



~duration = 30;

(
~perRingInitMarking = {|index, d3 = 0|
	(
		( "d1" ++ index ).asSymbol : 1,
		( "d2" ++ index ).asSymbol : 0,
		( "d3" ++ index ).asSymbol : d3,
		( "d4" ++ index ).asSymbol : 0,
		( "d5" ++ index ).asSymbol : 0
	)
}
)

a.samplePathAlgorithm( 10, 0, \postCurrentState, *( true ! 7 ) )
a.marking

(
a.setMarking(
	().putAll( *(
		~rings.collect {|i|
			if( i == 0 ){
				~perRingInitMarking.( i, 1 )
			}{
				~perRingInitMarking.( i, [0,1].wchoose([0.8,0.2]).postln )
			}
		}
	)
	)
)
)

a.initTimeDurPairs
a.samplePathAlgorithm( ~duration, 0, \collectTimeDurPairs )
a.samplePathWithMultiPerform( ~duration, 0, \postCurrentState, \collectTimeDurPairs )
a.timeDurPairs.at(\e11)

s.boot

(
var scale;
scale = Scale.bartok.degrees;
~perRingNoteEvent ={|index|
	(
		( "e1" ++ index ).asSymbol :
		{( 50.rrand(55) + scale + [-12,0,12].choose ).midicps.choose },
		( "e2" ++ index ).asSymbol :
		( 62 + scale + [-12,0,12].choose ).midicps.choose,
		( "e3" ++ index ).asSymbol :
		{( 74 + scale + [-12,0,12].choose ).midicps.choose },
	)
};
)

~noteEvent = ().putAll( *( ~rings.collect {|i|  ~perRingNoteEvent.(i)  } ) )

~score = Score.new;

(
var transitions, offset = 0.5, note, durList, aPetriNet, amp = 0.2;

~score.score = nil;

aPetriNet = a;

transitions = a.transitions.select {|aTrans| aTrans.isTimed }.keys;

transitions.do {|aSymbol|
	durList = aPetriNet.timeDurPairs.at( aSymbol );
	durList.do {|aPair|
		note = Synth.basicNew(\help_mdapiano);
		~score.add(
			[
				aPair[0],
				note.newMsg( 1, [ \freq, ~noteEvent.at( aSymbol ).value, \amp, amp] )
			]);
		~score.add(
			[
				aPair[0] + aPair[1],
				note.releaseMsg
			]);
	}
};

~score.add( [ ~duration + 1, [0] ] );
~score.sort;
)

~score.play
~score.stop

// Ex. 2.9 p.41

(
PNPlace.clearAll;

a = SPetriNet(
	(
		\transition: \e1,
		\inputPlaces: [\d1],
		\outputPlaces: [\d2, \d9],
		\inhibitorPlaces: [],
		\isTimed: true
	),
	(

		\transition: \e2,
		\inputPlaces: [\d2],
		\outputPlaces: [\d4, \d5,\d6],
		\inhibitorPlaces: [],
		\isTimed: true
	),
	(
		\transition: \e3,
		\inputPlaces: [\d3],
		\outputPlaces: [\d4, \d5,\d6],
		\inhibitorPlaces: [],
		\isTimed: true
	),
	(
		\transition: \e4,
		\inputPlaces: [\d4,\d5,\d6],
		\outputPlaces: [\d7, \d8,\d3],
		\inhibitorPlaces: [],
		\isTimed: false
	),
	(
		\transition: \e5,
		\inputPlaces: [\d7,\d9],
		\outputPlaces: [\d1],
		\inhibitorPlaces: [\d8],
		\isTimed: false
	),
	(
		\transition: \e6,
		\inputPlaces: [\d8,\d9],
		\outputPlaces: [\d1],
		\inhibitorPlaces: [],
		\isTimed: false
	)
)
)

(
~updateFunction = {| outputPlaces, aSPetriNet |
	var place;
	place = aSPetriNet.marking
	.select {| value, key |
		key == \d4 or: { key == \d5 } or: { key == \d6 }
	}
	.findKeyForValue( 0 );
	if( place.notNil ){ PNPlace.at(place).addOneToken; };
}
)

(
var transition;
2.do {| i |
	transition = a.transitions.at( ("e" ++ (i+2)).asSymbol );
	transition.updateOutputPlaces_( ~updateFunction );
};
)

// updateFunction better approach should be to compute a random integer
// partition of 3. See 'matters computational'
(
var transition;
transition = a.transitions.at(\e4);
transition.updateOutputPlaces_( {| outputPlaces, aSPetriNet |
	var n1, n2;
	n1 = 4.rand;
	n2 = (4 - n1).rand;
	PNPlace.at(\d7).addTokens( n1 );
	PNPlace.at(\d8).addTokens( n2 );
	n2 = 3 - ( n1 + n2 );
	PNPlace.at(\d3).addTokens( n2 );
})
)

// You have a problem in referencing the previous marking. Add an extra
// instanse variable? something else?
(
a.transitions.at(\e1).clock_( { 0.5.rrand(1.0) } );
a.transitions.at(\e2).clock_( { 0.5.rrand(1.0) } );
a.transitions.at(\e3).clock_( { 1.5.rrand(2.0) } );
)

(
a.setMarking(
	(\d1:0, \d2:0, \d3:1, \d4:0, \d5:0, \d6:0, \d7:1, \d8:1, \d9:1 )
)
)

~duration = 40;

a.initTimeDurPairs
a.samplePathAlgorithm( ~duration, 0, \collectTimeDurPairs )
a.samplePathWithMultiPerform( ~duration, 0, \postCurrentState, \collectTimeDurPairs )

s.boot;

(
~noteEvent = (
	\e1: 69.midicps,
	\e2: 75.midicps,
	\e3: 38.midicps
)
)

~score = Score.new;

(
var transitions, offset = 0.5, note, durList, aPetriNet, amp = 0.2;

~score.score = nil;

aPetriNet = a;

transitions = a.transitions.select {|aTrans| aTrans.isTimed }.keys;

transitions.do {|aSymbol|
	durList = aPetriNet.timeDurPairs.at( aSymbol );
	durList.do {|aPair|
		note = Synth.basicNew(\default);
		~score.add(
			[
				aPair[0],
				note.newMsg( 1, [ \freq, ~noteEvent.at( aSymbol ).value, \amp, amp] )
			]);
		~score.add(
			[
				aPair[0] + aPair[1],
				note.releaseMsg
			]);
	}
};

~score.add( [ ~duration + 1, [0] ] );
~score.sort;
)

~score.play
~score.stop

// Ex. 2.10 p.44
(
PNPlace.clearAll;

a = SPetriNet(
	(
		\transition: \e211,
		\inputPlaces: [\d111,\d211],
		\outputPlaces: [\d112,\d31],
		\inhibitorPlaces: [],
		\isTimed: true
	),
	(
		\transition: \e212,
		\inputPlaces: [\d112,\d212],
		\outputPlaces: [\d41,\d42,\d43,\d32],
		\inhibitorPlaces: [],
		\isTimed: true
	),
	(
		\transition: \e3,
		\inputPlaces: [\d41,\d42,\d43],
		\outputPlaces: [\d111,\d121,\d123],
		\inhibitorPlaces: [],
		\isTimed: false
	),
	(
		\transition: \e111,
		\inputPlaces: [\d111,\d31],
		\outputPlaces: [\d211],
		\inhibitorPlaces: [\d121],
		\isTimed: false
	),
	(
		\transition: \e112,
		\inputPlaces: [\d112,\d32],
		\outputPlaces: [\d212],
		\inhibitorPlaces: [\d122],
		\isTimed: false
	),
	(
		\transition: \e121,
		\inputPlaces: [\d31,\d121],
		\outputPlaces: [\d221],
		\inhibitorPlaces: [],
		\isTimed: false
	),
	(
		\transition: \e122,
		\inputPlaces: [\d32,\d122],
		\outputPlaces: [\d222],
		\inhibitorPlaces: [],
		\isTimed: false
	),
	(
		\transition: \e221,
		\inputPlaces: [\d121,\d221],
		\outputPlaces: [\d122,\d31],
		\inhibitorPlaces: [],
		\isTimed: true
	),
	(
		\transition: \e222,
		\inputPlaces: [\d122,\d222],
		\outputPlaces: [\d41,\d42,\d43,\d32],
		\inhibitorPlaces: [],
		\isTimed: true
	),
	(
		\transition: \e223,
		\inputPlaces: [\d123],
		\outputPlaces: [\d41,\d42,\d43],
		\inhibitorPlaces: [],
		\isTimed: true
	)
)
)

(
var transition;
transition = a.transitions.at(\e3);
transition.updateOutputPlaces_( {| outputPlaces, aSPetriNet |
	var n1, n2;
	n1 = 4.rand;
	n2 = (4 - n1).rand;
	PNPlace.at(\d111).addTokens( n1 );
	PNPlace.at(\d121).addTokens( n2 );
	n2 = 3 - ( n1 + n2 );
	PNPlace.at(\d123).addTokens( n2 );
})
)

(
~updateFunction = {| outputPlaces, aSPetriNet |
	var place;
	place = aSPetriNet.marking
	.select {| value, key |
		key == \d41 or: { key == \d42 } or: { key == \d43 }
	}
	.findKeyForValue( 0 );
	if( place.notNil ){
		PNPlace.at(place).addOneToken;
	};
	outputPlaces.select {|aPlace| aPlace.name == \d32 }
	.do {|aPlace| try { aPlace.addOneToken } };
}
)

(
var transition;
[ \e212, \e222, \e223 ].do {| aSymbol |
	transition = a.transitions.at( aSymbol );
	transition.updateOutputPlaces_( ~updateFunction );
};
)

(
a.transitions.at(\e211).clock_( { 0.5 } );
a.transitions.at(\e212).clock_( { 0.75 } );
a.transitions.at(\e221).clock_( { 1.0 } );
a.transitions.at(\e222).clock_( { 1.25 } );
a.transitions.at(\e223).clock_( { 0.25 } );
)

(
[\e111,\e121].do {|aSymbol|
	a.transitions.at( aSymbol ).updateInputPlaces_( {| inputPlaces, aSPN |
		inputPlaces.select {| aPlace | aPlace.name == \d31 }
		.do {| aPlace | aPlace.removeOneToken };
	})
};

[\e112,\e122].do {|aSymbol|
	a.transitions.at( aSymbol ).updateInputPlaces_( {| inputPlaces, aSPN |
		inputPlaces.select {| aPlace | aPlace.name == \d32 }
		.do {| aPlace | aPlace.removeOneToken };
	})
};
)

(
a.setMarking(
	a.marking.keysValuesChange {| key, value |
		value = if(
			key == \d41 or: { key == \d42 } or: { key == \d43 } or: { key == \d31 } or: { key == \d32 } ){ 1 }{ 0 };
	}
)
)

~duration = 140;

a.initTimeDurPairs
a.samplePathAlgorithm( ~duration, 0, \collectTimeDurPairs )
a.samplePathWithMultiPerform( ~duration, 0, \postCurrentState, \collectTimeDurPairs )

s.boot;

(
~noteEvent = (
	\e211: 62.midicps,
	\e212: 65.midicps,
	\e221: 38.midicps,
	\e222: 84.midicps,
	\e223: 86.midicps
)
)

~score = Score.new;

(
var transitions, offset = 0.5, note, durList, aPetriNet, amp = 0.2;

~score.score = nil;

aPetriNet = a;

transitions = a.transitions.select {|aTrans| aTrans.isTimed }.keys;

transitions.do {|aSymbol|
	durList = aPetriNet.timeDurPairs.at( aSymbol );
	durList.do {|aPair|
		note = Synth.basicNew(\default);
		~score.add(
			[
				aPair[0],
				note.newMsg( 1, [ \freq, ~noteEvent.at( aSymbol ).value, \amp, amp] )
			]);
		~score.add(
			[
				aPair[0] + aPair[1],
				note.releaseMsg
			]);
	}
};

~score.add( [ ~duration + 1, [0] ] );
~score.sort;
)

~score.play
~score.stop

// Ex. 2.11 p.45
(
PNPlace.clearAll;

a = SPetriNet(
	(
		\transition: \e1,
		\inputPlaces: [\d1],
		\outputPlaces: [\d1,\d4],
		\inhibitorPlaces: [],
		\clock: { 0.5.rrand(2.0) }
	),
	(
		\transition: \e2,
		\inputPlaces: [\d2],
		\outputPlaces: [\d2], // in the book there is not output place
		\inhibitorPlaces: [],
		\clock: { 1.0 }
	),
	(
		\transition: \e3,
		\inputPlaces: [\d3],
		\outputPlaces: [\d2],
		\inhibitorPlaces: [],
		\isTimed: false
	),
	(
		\transition: \e4,
		\inputPlaces: [\d4,\d2],
		\outputPlaces: [\d3],
		\inhibitorPlaces: [],
		\isTimed: false
	),
	(
		\transition: \e5,
		\inputPlaces: [\d4],
		\outputPlaces: [\d2],
		\inhibitorPlaces: [\d2],
		\isTimed: false
	)
)
)

(
a.setMarking(
	a.marking.keysValuesChange({| key, value |
		value = if( key == \d1 ){ 1 }{ 0 };
	})
)
)

~duration = 40;

a.initTimeDurPairs
a.samplePathAlgorithm( ~duration, 0, \collectTimeDurPairs )
a.samplePathWithMultiPerform( ~duration, 0, \postCurrentState, \collectTimeDurPairs )

s.boot;

(
~noteEvent = (
	\e1: 62.midicps,
	\e2: 64.midicps
)
)

~score = Score.new;

(
var transitions, offset = 0.5, note, durList, aPetriNet, amp = 0.2;

~score.score = nil;

aPetriNet = a;

transitions = a.transitions.select {|aTrans| aTrans.isTimed }.keys;

transitions.do {|aSymbol|
	durList = aPetriNet.timeDurPairs.at( aSymbol );
	durList.do {|aPair|
		note = Synth.basicNew(\help_mdapiano);
		~score.add(
			[
				aPair[0],
				note.newMsg( 1, [ \freq, ~noteEvent.at( aSymbol ).value, \amp, amp] )
			]);
		~score.add(
			[
				aPair[0] + aPair[1],
				note.releaseMsg
			]);
	}
};

~score.add( [ ~duration + 1, [0] ] );
~score.sort;
)

~score.play
~score.stop

// Ex. 2.12 p.47
PNPlace.clearAll
