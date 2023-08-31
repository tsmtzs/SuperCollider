//////////////////////////////////////////////////////////////////////
// Tests for revision 26  of PetriNetsN.sc
//////////////////////////////////////////////////////////////////////

(
~updateFunction = {
	var place = PNPlaceN(\p2);
	if ( place.tokens < 1 ){ place.addOneToken }
}
)

(
a = PetriNetN(
	(
		\transition: \t1,
		\inputPlaces: [\p1],
		\outputPlaces: [\p2],
		\inhibitorPlaces: [],
		\source: 1
	),
	(
		\transition: \t2,
		\inputPlaces: [\p2],
		\outputPlaces: [\p3,\p4],
		\inhibitorPlaces: [],
		// \updateOutputPlaces: {| outputPlaces |
		// 	outputPlaces.choose.addOneToken;
		// }
		\source: 2
	),
	(
		\transition: \t3,
		\inputPlaces: [\p3],
		\outputPlaces: [\p5],
		\inhibitorPlaces: [],
		\source: 3
	),
	(
		\transition: \t4,
		\inputPlaces: [\p4],
		\outputPlaces: [\p6],
		\inhibitorPlaces: [],
		\source: 4
	),
	(
		\transition: \t5,
		\inputPlaces: [\p5],
		\outputPlaces: [\p2],
		\inhibitorPlaces: [],
		// \updateOutputPlaces: ~updateFunction,
		\source: 5
	),
	(
		\transition: \t6,
		\inputPlaces: [\p6],
		\outputPlaces: [\p2],
		\inhibitorPlaces: [],
		// \updateOutputPlaces: ~updateFunction,
		\source: 6
	)
)
)

~objects = ( t1: 100, t2: Pseq([ 200, 0.2 ], inf ), t3: 300, t4: 400, t5: 500, t6: 600 )
a.setSources( ~objects )
a.setSource( \t6, Pfunc { 0.02.rand2 } )

(
~objects = (
	t1: ( freq: 100, amp: 0.5, dur: 0.5 ),
	t2: ( freq: 200, amp: 0.2, dur: 0.5 ),
	t3: ( freq: 300, amp: 0.2, dur: 0.5 ),
	t4: ( freq: 400, amp: 0.2, dur: 0.5 ),
	t5: ( freq: 500, amp: 0.2, dur: 0.5 ),
	t6: ( freq: 600, amp: 0.2, dur: 0.5 )
)
)

(
~marking = 	a.marking.keysValuesChange({| key, value |
		value = if( key == 'p1' ){ 1 }{ 0 };
	})
)

p = PNPatternN( a, ~marking, inf)
p = PNPatternN( a, ~marking, inf, ~objects )
q = p.asStream
q.nextN( 20 )

p.play

// Remark: Works ONLY with value patterns

//////////////////////////////////////////////////////////////////////
// Tests for revision 29  of PetriNetsN.sc
//////////////////////////////////////////////////////////////////////

(
a = PetriNetN(
	(
		\transition: \t1,
		\inputPlaces: [\p1],
		\outputPlaces: [\p2],
		\inhibitorPlaces: [],
		\source: 1
	),
	(
		\transition: \t2,
		\inputPlaces: [\p2],
		\outputPlaces: [\p3,\p4],
		\inhibitorPlaces: [],
		// \updateOutputPlaces: {| outputPlaces |
		// 	outputPlaces.choose.addOneToken;
		// }
		\source: 2
	),
	(
		\transition: \t3,
		\inputPlaces: [\p3],
		\outputPlaces: [\p5],
		\inhibitorPlaces: [],
		\source: 3
	),
	(
		\transition: \t4,
		\inputPlaces: [\p4],
		\outputPlaces: [\p6],
		\inhibitorPlaces: [],
		\source: 4
	),
	(
		\transition: \t5,
		\inputPlaces: [\p5],
		\outputPlaces: [\p2],
		\inhibitorPlaces: [],
		// \updateOutputPlaces: ~updateFunction,
		\source: 5
	),
	(
		\transition: \t6,
		\inputPlaces: [\p6],
		\outputPlaces: [\p2],
		\inhibitorPlaces: [],
		// \clock: { 1.rrand( 4 ) },
		// \updateOutputPlaces: ~updateFunction,
		\source: 6
	)
)
)

a.type
a.transitions

//////////////////////////////////////////////////////////////////////
// Tests for revisions 43 of PetriNetsN.sc
//////////////////////////////////////////////////////////////////////

(
~series = {| n = 3 |
	var aSymbol;
	n.collect {| i |
		aSymbol = i.asSymbol;
		(
			transition: ( \t ++ aSymbol ).asSymbol,
			inputPlaces: [ ( \p ++ aSymbol ).asSymbol, if( i == 0 ){ 1 }{ 0 } ],
			outputPlaces: [ ( \p ++ ( i + 1).mod( n ).asSymbol ).asSymbol ],
			inhibitorPlaces: [],
			source: ( type: \on, freq: i + 1 * 300, amp: 0.3 ),
			clock: 1
		)
	}
}
)

[ PNPlaceN, PNTransitionN ].do (_.clearAll);

(
a = PetriNetN( *(~series.( 3 )) );
)

a.transitions
a.places
a.marking
a.type
a.sources
a.sourceAt(\t1)
a.pattern = nil
a.pattern.play
a.play
a.stop

(
~objects = (
	t0: ( type: \on, freq: 500, amp: 0.5 ),
	t1: ( type: \on, freq: 200, amp: 0.5 ),
	// t1: ( type: \rest ),				// ok
	// t1: nil,							// ok
	t2: ( type: \on, freq: 300, amp: 0.2 )
)
)

(
~marking = 	a.marking.keysValuesChange({| key, value |
		value = if( key == 'p0' ){ 1 }{ 0 };
	})
)

~marking = ( p0: 1, p1: 0, p2: 1 )

p = PNEventPattern( a )
p = PNEventPattern( a, a.marking, inf, a.sources )
q = p.asStream
q.next( () )
q.reset
// reset doesn't work. Why?
// Ans: Because the marking has changed
q = p.play
q.stop

a.push
// the following doesn't work due to the initialization of
// streamDict var in embedInStream method of PNEventPattern
~t0.source = ( type: \on, freq: 219, amp: 0.2 ) // ok if use
~t0.clock = 0.2;							// ok
~p0.tokens = 1;							// ok
~p0.addOneToken;						// ok
a.marking
a.pop

~score = p.asScore( 10 );				// ok
~score.play								// ok - notes end when score finishes

//////////////////////////////////////////////////////////////////////
// Examples of Stochastic Petri Nets
// from "Stochastic Petri Nets: Modelling, Stability, Simulation"
// by Peter Haas
//////////////////////////////////////////////////////////////////////

// Haas, Ex. 1.4 p.19
[ PNTransitionN, PNPlaceN ].do {|a| a.clearAll }
(
a = PetriNetN(
		(
			transition: \e1,
			inputPlaces: [\d1],
			outputPlaces: [\d2,\d1],
			inhibitorPlaces: [],
			clock: 1,
			source: ( type: \on, freq: 400 )
		),
		(
			transition: \e2,
			inputPlaces: [\d2],
			outputPlaces: [\d1],
			inhibitorPlaces: [],
			clock: 1,
			source: ( type: \on, freq: 600, amp: 0.4 )
		)
)
)

(
var transition, p = 0.6;
transition = a.at(\e1);
transition.updateOutputPlaces_( {| outputPlaces, net |
	if( p.coin ){
		net[ \d2 ].addOneToken
	}{
		net[ \d1 ].addOneToken
	}
}
)
)

// if you set marking while the net is playing
// you have to recompute newTransitions.
// NOT CURRENTLY SUPPORTED
a.setMarking( ( d1:1, d2:0) )

a.marking

p = a.pattern.asStream;
p.next(())

// debuging
p = PNPostState( a ).routine
p.next;

a.play
a.stop


a.push;
~e1.clock = 1
~e2.clock = 1
~e1.clock = { 0.7.rrand( 1.0 ) }
~e2.clock = { (0.1,0.2..1.0).choose  }
~e2.source = ( type: \on, freq: 230, amp: 0.4 )


(
~e2.enabledFunction_( {| inputPlaces, inhibitorPlaces |
	var sum;
	sum = inputPlaces.collect {|p| p.tokens }.sum;
	sum.postln;
	sum > 5
})
)

~e2.enabledFunction_( PNTransitionN.enabledFunctionDefault )


// Haas, Ex. 2.1 p.24

(
[ PNTransitionN, PNPlaceN ].do {|a| a.clearAll };

a = PetriNetN(
	(
		transition: \e1,
		inputPlaces: [\d1, 3],
		outputPlaces: [\d2],
		inhibitorPlaces: [],
		clock: { 1.0.rrand( 2.0 ) },
		source: ( type: \on, freq: 300, pan: -0.5, amp: 0.5 )
	),
	(
		transition: \e2,
		inputPlaces: [\d2,\d7],
		outputPlaces: [\d3],
		inhibitorPlaces: [],
		clock: 0
	),
	(
		transition: \e3,
		inputPlaces: [\d3],
		outputPlaces: [\d1,\d7],
		inhibitorPlaces: [],
		clock: { 2.0.rrand( 4.0 ) },
		source: ( type: \on, freq: 550, pan: -0.5, amp: 0.8 )
	),
	(
		transition: \e4,
		inputPlaces: [\d4, 5],
		outputPlaces: [\d5],
		inhibitorPlaces: [],
		clock: { 1.0.rrand( 2.0 ) },
		source: ( type: \on, freq: 400, pan: 0.5, amp: 0.5 )
	),
	(
		transition: \e5,
		inputPlaces: [\d5,\d7,1],
		outputPlaces: [\d6],
		inhibitorPlaces: [\d2],
		clock: 0
	),
	(
		transition: \e6,
		inputPlaces: [\d6],
		outputPlaces: [\d4,\d7],
		inhibitorPlaces: [],
		clock: { 1.0.rrand( 2.0 ) },
		source: ( type: \on, freq: 700, pan: 0.5, amp: 0.8 )
	)
);
)

a.setMarking( (\d1: 3, \d2:0, \d3: 0, \d4:7, \d5:0, \d6:0, \d7:1) );
a.marking

p = PNPostState( a ).routine
p.next
p.play
p.stop

a.play
a.stop

// Haas, Ex. 2.2 p.29

(
[ PNTransitionN, PNPlaceN ].do {|a| a.clearAll };

a = PetriNetN(
	(
		transition: \e1,
		inputPlaces: [\d1],
		outputPlaces: [\d2],
		inhibitorPlaces: [],
		clock: { 1.0 },
		source: ( type: \on, freq: 300, amp: 0.2, pan: -0.5 )
	),
	(
		transition: \e2,
		inputPlaces: [\d2],
		outputPlaces: [\d1],
		inhibitorPlaces: [],
		clock: { 0.5 },
		source: ( type: \on, freq: 350, amp: 0.2, pan: -0.5 )
	),
	(
		transition: \e3,
		inputPlaces: [\d3],
		outputPlaces: [\d4],
		inhibitorPlaces: [],
		clock: { 1.0 },
		source: ( type: \on, freq: 500, amp: 0.2, pan: 0.5 )
	),
	(
		transition: \e4,
		inputPlaces: [\d4],
		outputPlaces: [\d3],
		inhibitorPlaces: [\d2],
		clock: { 0.5 },
		source: ( type: \on, freq: 570, amp: 0.2, pan: 0.5 )
	)
)
)

a.setMarking( (d1: 3, d2: 0, d3: 4, d4: 0 ) )

a.play

a.push;
~e1.clock = { 0.5.rrand(1.5) }

a.pop

a.stop

// Ex. 2.4 p.31 Haas

(
[ PNTransitionN, PNPlaceN ].do {|a| a.clearAll };

a = PetriNetN(
	(
		transition: \e1,
		inputPlaces: [\d1, 1],
		outputPlaces: [\d2,\d1],
		inhibitorPlaces: [],
		clock: 3,
		source: ( type: \on, freq: 300, pan: -0.6 )
	),
	(
		transition: \e2,
		inputPlaces: [\d2],
		outputPlaces: [\d2,\d3],
		inhibitorPlaces: [],
		clock: 0,
		updateOutputPlaces: {| outputPlaces, aPetriNet |
			var p, d3Tokens;
			d3Tokens = aPetriNet.at(\d3).tokens;
			// p = ( d3Tokens/(d3Tokens + 7.5) - 1 ).abs;
			if( d3Tokens < 2  ){
				outputPlaces.do {|aPlace| aPlace.addOneToken; }
			}{
				aPetriNet.at(\d3).addOneToken;
			}
		}
	),
	(
		transition: \e3,
		inputPlaces: [\d3],
		outputPlaces: [\d4],
		inhibitorPlaces: [\d2],
		clock: 0
	),
	(
		transition: \e4,
		inputPlaces: [\d4],
		outputPlaces: [],
		inhibitorPlaces: [],
		clock: 0.4,
		source: ( type: \on, freq: { 500.0.rrand( 600.0 ) }, pan: 0.6 )
	)
)
)

a.play
a.stop

p = PNPostState( a ).routine.asStream
a.marking
p.next

p = a.pattern.asStream
20.do { p.next(()).postln }
p.next(())

// Ex. 2.6 p.33 Haas

~rings = 3;

(
~port = {| index, rings = 3 |
	var transition;

	[
		(
			transition: ( "e1" ++ index ).asSymbol,
			inputPlaces: [ ( "d2" ++ index ).asSymbol ],
			outputPlaces: [ ( "d1" ++ index ).asSymbol ],
			inhibitorPlaces: [],
			clock: { [ 0.25, 0.5, 0.75, 1.0 ].choose },
			source: ( type: \on, freq: 100.rrand( 200 ), amp: 0.3 )
		),
		(
			transition: ( "e2" ++ index).asSymbol,
			inputPlaces: [ ( "d1" ++ index ).asSymbol, ( "d3" ++ index ).asSymbol ],
			outputPlaces: [ ( "d2" ++ index).asSymbol, ( "d4" ++ index ).asSymbol ],
			inhibitorPlaces: [],
			clock: { [ 0.25, 0.5, 0.75, 1.0 ].choose },
			source: ( type: \on, freq: 300.rrand( 400 ) )
		),
		(
			transition: ( "e3" ++ index ).asSymbol,
			inputPlaces: [ ( "d4" ++ ( index - 1 ).mod( rings ) ).asSymbol  ],
			outputPlaces: [ ( "d3" ++ index ).asSymbol, ( "d4" ++ index ).asSymbol ],
			inhibitorPlaces: [],
			clock: { 1.5 },
			source: ( type: \on, freq: 500.rrand( 600 ) )
			updateOutputPlaces: {| outputPlaces, aPetriNet |
				var d1Tokens;
				d1Tokens = aPetriNet.at( ( "d1" ++ index ).asSymbol );
				if( d1Tokens == 1 ){
					aPetriNet.at( ( "d3" ++ index ).asSymbol ).addOneToken;
				}{
					aPetriNet.at( ( "d4" ++ index ).asSymbol ).addOneToken;
				}
			}
		)
	]
}
)

(
var net;

[ PNTransitionN, PNPlaceN ].do (_.clearAll);

net = ~rings.collect {|i| ~port.( i, ~rings ) }.flatten;

a = PetriNetN( *net );
)

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


p = PNPostState( a ).routine.asStream
a.marking
p.next

a.play;
a.stop;

// Ex. 2.7 p.36 Haas
(
SynthDef(\help_mdapiano, { |out=0, freq=440, gate=1|
	var son = MdaPiano.ar(freq, gate, release: 0.9, stereo: 0.3, sustain: 0);
	DetectSilence.ar(son, 0.01, doneAction:2);
	Out.ar(out, son * 0.1);
}).add;
)

~rings = 5;

(
~port = {| index, rings = 3 |
	var transition;
	[
		(
			transition: ( "e1" ++ index ).asSymbol,
			inputPlaces: [ ( "d2" ++ index ).asSymbol ],
			outputPlaces: [ ( "d1" ++ index ).asSymbol ],
			inhibitorPlaces: [],
			clock: { [ 0.5, 1.0, 1.5, 2.0 ].choose }
		),
		(
			transition: ( "e2" ++ index).asSymbol,
			inputPlaces: [ ( "d3" ++ index ).asSymbol ],
			outputPlaces: [ ( "d2" ++ index).asSymbol, ( "d4" ++ index ).asSymbol ],
			inhibitorPlaces: [],
			clock:  [ 0.25, 2/7, 4/9 ].choose
		),
		(
			transition: ( "e3" ++ index ).asSymbol,
			inputPlaces: [ ( "d4" ++ ( index - 1 ).mod( rings ) ).asSymbol  ],
			outputPlaces: [ ( "d5" ++ index ).asSymbol ],
			inhibitorPlaces: [],
			clock: { [ 0.25, 4/11 ].choose }
		),
		(
			transition: ( "e4" ++ index ).asSymbol,
			inputPlaces: [ ("d1" ++ index ).asSymbol, ("d5" ++ index ).asSymbol  ],
			outputPlaces: [ ( "d3" ++ index ).asSymbol ],
			inhibitorPlaces: [],
			clock: 0
		),
		(
			transition: ( "e5" ++ index ).asSymbol,
			inputPlaces: [ ("d5" ++ index ).asSymbol  ],
			outputPlaces: [ ( "d4" ++ index ).asSymbol ],
			inhibitorPlaces: [ ( "d1" ++ index ).asSymbol  ],
			clock: 0
		)
	]
}
)

(
var net;

[ PNTransitionN, PNPlaceN ].do (_.clearAll);

net = ~rings.collect {|i| ~port.( i, ~rings ) }.flat;

a = PetriNetN( *net );
)

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

(
a.setMarking(
	().putAll( *(
		~rings.collect {|i|
			if( i == 0 ){
				~perRingInitMarking.( i, 1 )
			}{
				~perRingInitMarking.( i, [0,1].wchoose([0.9,0.1]).postln )
			}
		}
	)
	)
)
)


(
var scale;
scale = Scale.scriabin.degrees;
~perRingSources ={| index, aPetriNet|
	(
		( "e1" ++ index ).asSymbol : (
			type: \on, instrument: \help_mdapiano, freq: {
			( 50.rrand(55) + scale + [-12,0,12].choose ).midicps.choose
			}, amp: 0.2
		),
		( "e2" ++ index ).asSymbol : (
			type: \on, instrument: \help_mdapiano, freq: ( 62 + scale + [-12,0,12].choose ).midicps.choose, amp: 0.1
		),
		( "e3" ++ index ).asSymbol : (
			type: \on, instrument: \help_mdapiano, freq: {
			( 74 + scale + [-12,0,12].choose ).midicps.choose
		}, amp: 0.2
		)
	)
};
)

~sourceEvent = ().putAll( *(~rings.collect {|i| ~perRingSources.( i, a ) } ) );
a.setSources( ~sourceEvent )

a.play
a.stop

// Ex. 2.9 p.41 Haas
(
~updateFunction = {| outputPlaces, aPetriNet |
	var place, name, tokens;
	place = aPetriNet.places.select {| p |
		name = p.name;
		tokens = p.tokens;
		name == \d4 or: { name == \d5 } or: { name == \d6 }
		and: { tokens == 0 }
	};
	if( place.notEmpty ){ place.choose.addOneToken; };
}
)

(
[ PNTransitionN, PNPlaceN ].do {|a| a.clearAll };

a = PetriNetN(
	(
		transition: \e1,
		inputPlaces: [\d1],
		outputPlaces: [\d2, \d9],
		inhibitorPlaces: [],
		clock: 1,
		source: ( type: \on, freq: 50.midicps, amp: 0.2, instrument: \help_mdapiano )
	),
	(

		transition: \e2,
		inputPlaces: [\d2],
		outputPlaces: [\d4, \d5,\d6],
		inhibitorPlaces: [],
		clock: 1,
		source: ( type: \on, freq: 60.midicps, amp: 0.2, instrument: \help_mdapiano ),
		updateOutputPlaces: ~updateFunction
	),
	(
		transition: \e3,
		inputPlaces: [\d3],
		outputPlaces: [\d4, \d5,\d6],
		inhibitorPlaces: [],
		clock: 2,
		source: ( type: \on, freq: 80.midicps, amp: 0.2, instrument: \help_mdapiano )
		updateOutputPlaces: ~updateFunction
	),
	(
		transition: \e4,
		inputPlaces: [\d4,\d5,\d6],
		outputPlaces: [\d7, \d8,\d3],
		inhibitorPlaces: [],
		clock: 1,
		updateOutputPlaces: {| outputPlaces, aPetriNet |
			var n1, n2;
			n1 = 4.rand;
			n2 = (4 - n1).rand;
			aPetriNet.at(\d7).addTokens( n1 );
			aPetriNet.at(\d8).addTokens( n2 );
			n2 = 3 - ( n1 + n2 );
			aPetriNet.at(\d3).addTokens( n2 );
		}
	),
	(
		transition: \e5,
		inputPlaces: [\d7,\d9],
		outputPlaces: [\d1],
		inhibitorPlaces: [\d8],
		clock: 0
	),
	(
		transition: \e6,
		inputPlaces: [\d8,\d9],
		outputPlaces: [\d1],
		inhibitorPlaces: [],
		clock: 0
	)
)
)

(
a.setMarking(
	( d1:0, d2:0, d3:1, d4:0, d5:0, d6:0, d7:1, d8:1, d9:1 )
)
)

a.play;
a.stop;

a.push
~e2.clock = 0.5
a.pop

// Ex. 2.10 p.44 Haas
(
~totalParts = 3;

~updateFunction = {| outputPlaces, aPetriNet |
	var place, name, tokens;
	place = aPetriNet.places.select {| p |
		name = p.name;
		tokens = p.tokens;
		name == \d41 or: { name == \d42 } or: { name == \d43 }
		and: { tokens == 0 }
	};
	if( place.notEmpty ){ place.choose.addOneToken; };
	aPetriNet.at(\d32).addOneToken;
};

~updateInputPlacesIf = {| aPNPlace |
	{| inputPlaces, aPetriNet |
		aPetriNet.at( aPNPlace ).removeOneToken;
	}
}
)

(
[ PNTimedTransitionN, PNPlaceN ].do {|a| a.clearAll };

a = PetriNetN(
	(
		transition: \e211,
		inputPlaces: [\d111,\d211],
		outputPlaces: [\d112,\d31],
		inhibitorPlaces: [],
		clock: 0.5,
		source: ( type: \on, freq: 60.midicps, instrument: \help_mdapiano )
	),
	(
		transition: \e212,
		inputPlaces: [\d112,\d212],
		outputPlaces: [\d41,\d42,\d43,\d32],
		inhibitorPlaces: [],
		clock: 0.5,
		updateOutputPlaces: ~updateFunction,
		source: ( type: \on, freq: 53.midicps, instrument: \help_mdapiano )
	),
	(
		transition: \e3,
		inputPlaces: [\d41,\d42,\d43],
		outputPlaces: [\d111,\d121,\d123],
		inhibitorPlaces: [],
		clock: 0,
		updateOutputPlaces: {| outputPlaces, aPetriNet |
			var n1, n2;
			n1 = ( ~totalParts + 1 ).rand;
			n2 = ( ~totalParts - n1).rand;
			aPetriNet.at(\d111).addTokens( n1 );
			aPetriNet.at(\d121).addTokens( n2 );
			n2 = ~totalParts - n1 - n2;
			aPetriNet.at(\d123).addTokens( n2 );
			// [ n1, n2, n2 ].postln;
		}
	),
	(
		transition: \e111,
		inputPlaces: [\d111,\d31],
		outputPlaces: [\d211],
		inhibitorPlaces: [\d121],
		clock: 0,
		updateInputPlaces: ~updateInputPlacesIf.( \d31 )
	),
	(
		transition: \e112,
		inputPlaces: [\d112,\d32],
		outputPlaces: [\d212],
		inhibitorPlaces: [\d122],
		clock: 0,
		updateInputPlaces: ~updateInputPlacesIf.( \d32 )
	),
	(
		transition: \e121,
		inputPlaces: [\d31,\d121],
		outputPlaces: [\d221],
		inhibitorPlaces: [],
		clock: 0,
		updateInputPlaces: ~updateInputPlacesIf.( \d31 )
	),
	(
		transition: \e122,
		inputPlaces: [\d32,\d122],
		outputPlaces: [\d222],
		inhibitorPlaces: [],
		clock: 0,
		updateInputPlaces: ~updateInputPlacesIf.( \d32 )
	),
	(
		transition: \e221,
		inputPlaces: [\d121,\d221],
		outputPlaces: [\d122,\d31],
		inhibitorPlaces: [],
		clock: 0.5,
		source: ( type: \on, freq: 64.midicps, instrument: \help_mdapiano )
	),
	(
		transition: \e222,
		inputPlaces: [\d122,\d222],
		outputPlaces: [\d41,\d42,\d43,\d32],
		inhibitorPlaces: [],
		clock: 0.5,
		updateOutputPlaces: ~updateFunction,
		source: ( type: \on, freq: 57.midicps, instrument: \help_mdapiano )
	),
	(
		transition: \e223,
		inputPlaces: [\d123],
		outputPlaces: [\d41,\d42,\d43],
		inhibitorPlaces: [],
		clock: 0.25,
		updateOutputPlaces: ~updateFunction,
		source: ( type: \on, freq: 62.midicps, instrument: \help_mdapiano )
	)
)
)

(
a.setMarking(
	a.marking.keysValuesChange {| key, value |
		value = if(
			key == \d41 or: { key == \d42 } or: { key == \d43 } or: { key == \d31 } or: { key == \d32 } ){ 1 }{ 0 };
	}
)
)

a.play;
a.stop;

p = PNPostState( a ).routine
p.next

//////////////////////////////////////////////////////////////////////
// testing the abiltity to set args of a node

(
[ PNTimedTransitionN, PNPlaceN ].do {|a| a.clearAll };

a = PetriNetN(
	(
		transition: \e0,
		inputPlaces: [ \d0, 1 ],
		outputPlaces: [ \d1, \d2 ],
		inhibitorPlaces: [],
		clock: 0
	),
	(
		transition: \e1,
		inputPlaces: [ \d1 ],
		outputPlaces: [ \d0 ],
		inhibitorPlaces: [ ],
		source: {| aPetriNet |
			var id = s.nextNodeID;
			aPetriNet.put( ( 'e0' ++ 'ID').asSymbol, id );
			( type: \on, id: id )
		},
		clock: { 10.0 }
	),
	(
		transition: \e2,
		inputPlaces: [ \d2 ],
		outputPlaces: [ \d3 ],
		inhibitorPlaces: [],
		clock: 0,
		updateOutputPlaces: {| outputPlaces, aPetriNet |
			outputPlaces.do {|p| p.addTokens( 5 ) };
		}
	),
	(
		transition: \e3,
		inputPlaces: [ \d3 ],
		outputPlaces: [],
		inhibitorPlaces: [],
		clock: 1,
		source: {| aPetriNet |
			var midinote = 50.rrand( 70 );
			( type: \set, id: aPetriNet.at('e0ID'), args: \midinote, midinote: midinote  )
		}
	)
)
)

a.marking

(
a.setMarking(
	a.marking.keysValuesChange {| key, value |
		value = if( key == \d0 ){ 1 }{ 0 };
	}
)
)

p = a.pattern.asStream
20.do { p.next(()).postln }
p.next(())

a.play
a.stop

p = PNPostState( a ).routine
p.next
