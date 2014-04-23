//////////////////////////////////////////////////////////////////////
// Tests for revisions 18, 23 of PetriNetsN.sc
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
	),
	(
		\transition: \t2,
		\inputPlaces: [\p2],
		\outputPlaces: [\p3,\p4],
		\inhibitorPlaces: []
		// \updateOutputPlaces: {| outputPlaces |
		// 	outputPlaces.choose.addOneToken;
		// }
	),
	(
		\transition: \t3,
		\inputPlaces: [\p3],
		\outputPlaces: [\p5],
		\inhibitorPlaces: [],
	),
	(
		\transition: \t4,
		\inputPlaces: [\p4],
		\outputPlaces: [\p6],
		\inhibitorPlaces: [],
	),
	(
		\transition: \t5,
		\inputPlaces: [\p5],
		\outputPlaces: [\p2],
		\inhibitorPlaces: [],
		// \updateOutputPlaces: ~updateFunction,
	),
	(
		\transition: \t6,
		\inputPlaces: [\p6],
		\outputPlaces: [\p2],
		\inhibitorPlaces: [],
		// \updateOutputPlaces: ~updateFunction,
	)
)
)

~objects = ( t1: 1, t2: 2, t3: 3, t4: 4, t5: 5, t6: 6 )
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

p = PNPatternN( a, ~objects, ~marking, inf )
p = p.asStream

p.nextN( 20 )

p.play

// Remark: Works ONLY with value patterns

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