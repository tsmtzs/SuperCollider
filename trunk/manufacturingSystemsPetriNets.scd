// Examples from 
// Petri nets and manufacturing systems: An examples-driven tour
// by L. Recalde , M. Silva, J. Ezpeleta , and E. Teruel
// for revisions > 23 of PetriNetsN.sc

//////////////////////////////////////////////////////////////////////
// Assembly manufacturing cell
//////////////////////////////////////////////////////////////////////

(
// a machine
~machine = {| index = 0, rOffPlace, assemblyDonePlace, startAssemblyTransition, clockT1, t1Source, clockT2, t2Source, clockT3, t3Source, t4Source, clockT5, t5Source |
	var inputPlaces, place5;

	inputPlaces = startAssemblyTransition.inputPlaces( true );
	if( inputPlaces.includes( place5 = "p%5".format( index ).asSymbol ).not ){
		startAssemblyTransition.inputPlaces = inputPlaces ++ [ place5 ];
	};

	[
		(
			transition: "t%1".format( index ).asSymbol,
			inputPlaces: [ "p%0".format( index ).asSymbol, 1, rOffPlace ],
			outputPlaces: [ "p%1".format( index ).asSymbol ],
			inhibitorPlaces: [],
			clock: clockT1,
			source: t1Source
		),
		(
			transition: "t%2".format( index ).asSymbol,
			inputPlaces: [ "p%1".format( index ).asSymbol ],
			outputPlaces: [ "p%2".format( index ).asSymbol, rOffPlace ],
			inhibitorPlaces: [],
			clock: clockT2,
			source: t2Source
		),
		(
			transition: "t%3".format( index ).asSymbol,
			inputPlaces: [ "p%2".format( index ).asSymbol ],
			outputPlaces: [ "p%3".format( index ).asSymbol ],
			inhibitorPlaces: [],
			clock: clockT3,
			source: t3Source
		),
		(
			transition: "t%4".format( index ).asSymbol,
			inputPlaces: [ "p%3".format( index ).asSymbol, rOffPlace, assemblyDonePlace, 1 ],
			outputPlaces: [ "p%4".format( index ).asSymbol ],
			inhibitorPlaces: [],
			clock: 0,
			source: t4Source
		),
		(
			transition: "t%5".format( index ).asSymbol,
			inputPlaces: [ "p%4".format( index ).asSymbol ],
			outputPlaces: [ place5, rOffPlace ],
			inhibitorPlaces: [],
			clock: clockT5,
			source: t5Source
		)
	]
};

// a machine with failure and buffer
~machineBuffer = {| index = 0, rOffPlace, assemblyDonePlace, startAssemblyTransition, bufferSize = 1, clockT1, t1Source, clockT2, t2Source, clockT3, t3Source, t4Source, clockT5, t5Source, t6Source, clockT7, t7Source, clockT8, t8Source, clockT9, t9Source |
	var inputPlaces, place7;

	inputPlaces = startAssemblyTransition.inputPlaces( true );
	if( inputPlaces.includes( place7 = "p%7".format( index ).asSymbol ).not ){
		startAssemblyTransition.inputPlaces = inputPlaces ++ [ place7 ];
	};

	[
		(
			transition: "t%1".format( index ).asSymbol,
			inputPlaces: [ "p%0".format( index ).asSymbol, 1, rOffPlace ],
			outputPlaces: [ "p%1".format( index ).asSymbol ],
			inhibitorPlaces: [],
			clock: clockT1,
			source: t1Source
		),
		(
			transition: "t%2".format( index ).asSymbol,
			inputPlaces: [ "p%1".format( index ).asSymbol ],
			outputPlaces: [ "p%2".format( index ).asSymbol, rOffPlace ],
			inhibitorPlaces: [],
			clock: clockT2,
			source: t2Source
		),
		(
			transition: "t%3".format( index ).asSymbol,
			inputPlaces: [ "p%2".format( index ).asSymbol ],
			outputPlaces: [ "p%3".format( index ).asSymbol ],
			inhibitorPlaces: [],
			clock: clockT3,
			source: t3Source
		),
		(
			transition: "t%4".format( index ).asSymbol,
			inputPlaces: [ "p%3".format( index ).asSymbol, "p%8".format( index ).asSymbol, bufferSize ],
			outputPlaces: [ "p%4".format( index ).asSymbol ],
			inhibitorPlaces: [],
			clock: 0,
			source: t4Source
		),
		(
			transition: "t%5".format( index ).asSymbol,
			inputPlaces: [ "p%4".format( index ).asSymbol ],
			outputPlaces: [ "p%5".format( index ).asSymbol, "p%0".format( index ).asSymbol ],
			inhibitorPlaces: [],
			clock: clockT5,
			source: t5Source
		),
		(
			transition: "t%6".format( index ).asSymbol,
			inputPlaces: [ "p%5".format( index ).asSymbol, assemblyDonePlace, 1 ],
			outputPlaces: [ "p%6".format( index ).asSymbol ],
			inhibitorPlaces: [],
			clock: 0,
			source: t6Source
		),
		(
			transition: "t%7".format( index ).asSymbol,
			inputPlaces: [ "p%6".format( index ).asSymbol ],
			outputPlaces: [ place7, "p%8".format( index ).asSymbol ],
			inhibitorPlaces: [],
			clock: clockT7,
			source: t7Source
		),
		(
			transition: "t%8".format( index ).asSymbol,
			inputPlaces: [ "p%2".format( index ).asSymbol ],
			outputPlaces: [ "p%9".format( index ).asSymbol ],
			inhibitorPlaces: [],
			clock: clockT8,
			source: t8Source
		),
		(
			transition: "t%9".format( index ).asSymbol,
			inputPlaces: [ "p%9".format( index ).asSymbol ],
			outputPlaces: [ "p%2".format( index ).asSymbol ],
			inhibitorPlaces: [],
			clock: clockT9,
			source: t9Source
		)
	]
}
)

[ PNTransitionN, PNTimedTransitionN, PNPlaceN ].do {|a| a.clearAll };