// Examples from
// Petri nets and manufacturing systems: An examples-driven tour
// by L. Recalde , M. Silva, J. Ezpeleta , and E. Teruel

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
			outputPlaces: [ place5, rOffPlace, "p%0".format( index ).asSymbol ],
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

// note: use symbols as arguments for a PetriNetN ( transitions - places )

// first example - One machine - No assembly
(
var assemblyDonePlace, assemblyOnTrans, rOffPlace, startAssemblyTrans, clockT1, t1Source, clockT2, t2Source, clockT3, t3Source, t4Source, clockT5, t5Source;

[ PNTimedTransitionN, PNPlace ].do {|a| a.clearAll };

assemblyDonePlace = PNPlace( \done, 1 );
rOffPlace = PNPlace( \rOff, 1 );
startAssemblyTrans = PNTimedTransitionN( \startAssembly );
assemblyOnTrans = PNTimedTransitionN( \assemblyOnTrans );

clockT1 = { [ 0.25, 0.5, 0.75, 1.0 ].choose };
t1Source = ( type: \on, note: 0, amp: 0.1, octave: { [ 4, 5, 6 ].choose } );
clockT2 = { [ 0.25, 0.5, 0.75, 1.0 ].choose };
t2Source = ( type: \on, note: 1, octave: { [ 4, 5, 6 ].choose }, amp: 0.1 );
clockT3 = { [ 0.25, 0.5, 0.75, 1.0 ].choose };
t3Source = ( type: \on, note: 2, octave: { [ 4, 5, 6 ].choose }, amp: 0.1 );
clockT5 = { [ 0.25, 0.5, 0.75, 1.0 ].choose };
t5Source = ( type: \on, note: 3, octave: { [ 4, 5, 6 ].choose }, amp: 0.1 );

p = PetriNetN(
	(
		transition: startAssemblyTrans.name,
		inputPlaces: [ \p05 ],
		outputPlaces: [ \assemblyOn ],
		inhibitorPlaces: [],
		clock: 0
	),
	(
		transition: assemblyOnTrans.name,
		inputPlaces: [ \assemblyOn ],
		outputPlaces: [ assemblyDonePlace.name ],
		inhibitorPlaces: [],
		clock: 5,
		source: ( type: \note, midinote: { { 60.rrand( 90 ) } ! 15 }, amp: 0.03, dur: 4.5, strum: 0.05 )
	),
	*
	~machine.(
		0,								// index,
		rOffPlace.name,
		assemblyDonePlace.name,
		startAssemblyTrans,
		clockT1,
		t1Source,
		clockT2,
		t2Source,
		clockT3,
		t3Source,
		t4Source,
		clockT5,
		t5Source
	)
)
)

p.marking

p.play
//////////////////////////////////////////////////////////////////////
// Basic pull control system
//////////////////////////////////////////////////////////////////////
(
~bpcs = {| index = 0, startDeliveryPlace, demandsPlace, demandsClock = 1, demandsSource, machine1Clock = 1, machine1Source, k1 = 1, machine2Clock = 1, machine2Source, k2 = 1, assemblySource, machine3Clock = 1, machine3Source, k3 = 1, deliveryDestPlace, deliverySource |
	[
		(
			transition: "customersDemands%".format( index ).asSymbol,
			inputPlaces: [ demandsPlace ],
			outputPlaces: [ "dr%1", "dr%2" ].collect {| string | string.format( index ).asSymbol } ++ [ startDeliveryPlace ],
			inhibitorPlaces: [],
			clock: demandsClock,
			source: demandsSource
		),
		(
			transition: "machine%1".format( index ).asSymbol,
			inputPlaces: [ "dr%1".format( index ).asSymbol ],
			outputPlaces: [ "f%1".format( index ).asSymbol, k1 ],
			inhibitorPlaces: [],
			clock: machine1Clock,
			source: machine1Source
		),
		(
			transition: "machine%2".format( index ).asSymbol,
			inputPlaces: [ "dr%2".format( index ).asSymbol ],
			outputPlaces: [ "f%2".format( index ).asSymbol, k2 ],
			inhibitorPlaces: [],
			clock: machine2Clock,
			source: machine2Source
		),
		(
			transition: "assembly%".format( index ),
			inputPlaces: [ "f%1", "f%2" ].collect {| string | string.format( index ).asSymbol },
			outputPlaces: [ "dr%3".format( index ).asSymbol ],
			inhibitorPlaces: [],
			clock: 0,
			source: assemblySource
		),
		(
			transition: "machine%3".format( index ).asSymbol,
			inputPlaces: [ "dr%3".format( index ).asSymbol ],
			outputPlaces: [ "f%3".format( index ).asSymbol, k3 ],
			inhibitorPlaces: [],
			clock: machine3Clock,
			source: machine3Source
		),
		(
			transition: "delivery%".format( index ).asSymbol,
			inputPlaces: [ "f%3".format( index ).asSymbol, demandsPlace ],
			outputPlaces: [ deliveryDestPlace ],
			inhibitorPlaces: [],
			clock: 0,
			source: deliverySource
		)
	]
}
)

//////////////////////////////////////////////////////////////////////
// Simultaneous kanban control system
//////////////////////////////////////////////////////////////////////
(
~skcs = {| index = 0, demandsPlace, demandsClock = 1, demandsSource, machine1Clock = 1, machine1Source, k1 = 1, machine2Clock = 1, machine2Source, k2 = 1, assemblySource, machine3Clock = 1, machine3Source, k3 = 1, deliveryDestPlace, deliverySource |

	[
		(
			transition: "customersDemands%".format( index ).asSymbol,
			inputPlaces: [ demandsPlace ],
			outputPlaces: [ "startDeliveryPlace%".format( index ).asSymbol ],
			inhibitorPlaces: [],
			clock: demandsClock,
			source: demandsSource
		),
		(
			transition: "machine%1".format( index ).asSymbol,
			inputPlaces: [ "dr%1".format( index ).asSymbol ],
			outputPlaces: [ "f%1".format( index ).asSymbol, k1 ],
			inhibitorPlaces: [],
			clock: machine1Clock,
			source: machine1Source
		),
		(
			transition: "machine%2".format( index ).asSymbol,
			inputPlaces: [ "dr%2".format( index ).asSymbol ],
			outputPlaces: [ "f%2".format( index ).asSymbol, k2 ],
			inhibitorPlaces: [],
			clock: machine2Clock,
			source: machine2Source
		),
		(
			transition: "assembly%".format( index ),
			inputPlaces: [ "f%1", "f%2", "startAssemblyPlace%" ].collect {| string | string.format( index ).asSymbol },
			outputPlaces: [ "dr%3", "dr%2", "dr%1" ].collect {| string | string.format( index ).asSymbol },
			inhibitorPlaces: [],
			clock: 0,
			source: assemblySource
		),
		(
			transition: "machine%3".format( index ).asSymbol,
			inputPlaces: [ "dr%3".format( index ).asSymbol ],
			outputPlaces: [ "f%3".format( index ).asSymbol, k3 ],
			inhibitorPlaces: [],
			clock: machine3Clock,
			source: machine3Source
		),
		(
			transition: "delivery%".format( index ).asSymbol,
			inputPlaces: [ "f%3", "startDeliveryPlace%" ].collect {| string | string.format( index ).asSymbol },
			outputPlaces: [ "startAssemblyPlace%".format( index ).asSymbol, deliveryDestPlace ],
			inhibitorPlaces: [],
			clock: 0,
			source: deliverySource
		)
	]
}
)

//////////////////////////////////////////////////////////////////////
// Independent kanban control system
//////////////////////////////////////////////////////////////////////

(
~ikcs = {| index = 0, demandsPlace, demandsClock = 1, demandsSource, machine1Clock = 1, machine1Source, k1 = 1, machine1DoneSource, machine2Clock = 1, machine2Source, k2 = 1, machine2DoneSource, assemblySource, machine3Clock = 1, machine3Source, k3 = 1, deliveryDestPlace, deliverySource |

	[
		(
			transition: "customersDemands%".format( index ).asSymbol,
			inputPlaces: [ demandsPlace ],
			outputPlaces: [ "startDeliveryPlace%".format( index ).asSymbol ],
			inhibitorPlaces: [],
			clock: demandsClock,
			source: demandsSource
		),
		(
			transition: "machine%1".format( index ).asSymbol,
			inputPlaces: [ "dr%1".format( index ).asSymbol ],
			outputPlaces: [ "f%1".format( index ).asSymbol, k1 ],
			inhibitorPlaces: [],
			clock: machine1Clock,
			source: machine1Source
		),
		(
			transition: "machine%1Done".format( index ).asSymbol,
			inputPlaces: [ "f%1", "machine%1DonePlace" ].collect {| string | string.format( index ).asSymbol },
			outputPlaces: [ "b%1", "dr%1" ].collect {| string | string.format( index ).asSymbol },
			inhibitorPlaces: [],
			clock: 0,
			source: machine1DoneSource
		),
		(
			transition: "machine%2".format( index ).asSymbol,
			inputPlaces: [ "dr%2".format( index ).asSymbol ],
			outputPlaces: [ "f%2".format( index ).asSymbol, k2 ],
			inhibitorPlaces: [],
			clock: machine2Clock,
			source: machine2Source
		),
		(
			transition: "machine%2Done".format( index ).asSymbol,
			inputPlaces: [ "f%2", "machine%2DonePlace" ].collect {| string | string.format( index ).asSymbol },
			outputPlaces: [ "b%2", "dr%2" ].collect {| string | string.format( index ).asSymbol },
			inhibitorPlaces: [],
			clock: 0,
			source: machine2DoneSource
		),
		(
			transition: "assembly%".format( index ),
			inputPlaces: [ "b%1", "b%2" ].collect {| string | string.format( index ).asSymbol },
			outputPlaces: [ "dr%3".format( index ).asSymbol ],
			inhibitorPlaces: [],
			clock: 0,
			source: assemblySource
		),
		(
			transition: "machine%3".format( index ).asSymbol,
			inputPlaces: [ "dr%3".format( index ).asSymbol ],
			outputPlaces: [ "f%3".format( index ).asSymbol, k3 ],
			inhibitorPlaces: [],
			clock: machine3Clock,
			source: machine3Source
		),
		(
			transition: "delivery%".format( index ).asSymbol,
			inputPlaces: [ "f%3", "startDeliveryPlace%" ].collect {| string | string.format( index ).asSymbol },
			outputPlaces: [ "machine%1DonePlace", "machine%2DonePlace" ].collect {| string | string.format( index ).asSymbol } ++ [ deliveryDestPlace ],
			inhibitorPlaces: [],
			clock: 0,
			source: deliverySource
		)
	]
}
)

//////////////////////////////////////////////////////////////////////
// Workstation from section "A car manufacturing system"
//////////////////////////////////////////////////////////////////////

(
~workstation = {| index = 0, loadPlace, loadClock = 1, loadSource, startProcessingClock = 1, startProcessingSource, stopProcessingClock = 1, stopProcessingSource, unloadPlace, unloadClock = 1, unloadSource |
	[
		(
			transition: "load%".format( index ).asSymbol,
			inputPlaces: [ "fl%".format( index ).asSymbol, 1, loadPlace ],
			outputPlaces: [ "l%".format( index ).asSymbol ],
			inhibitorPlaces: [],
			clock: loadClock,
			source: loadSource
		),
		(
			transition: "startProcessing%".format( index ).asSymbol,
			inputPlaces: [ "l%", "fp%" ].collect {| string | string.format( index ).asSymbol } ++ [ 1 ],
			outputPlaces: [ "fl%", "p%" ].collect {| string | string.format( index ).asSymbol },
			inhibitorPlaces: [],
			clock: startProcessingClock,
			source: startProcessingSource
		),
		(
			transition: "stopProcessing%".format( index ).asSymbol,
			inputPlaces: [ "p%", "fu%" ].collect {| string | string.format( index ).asSymbol } ++ [ 1 ],
			outputPlaces: [ "fp%", "u%" ].collect {| string | string.format( index ).asSymbol },
			inhibitorPlaces: [],
			clock: stopProcessingClock,
			source: stopProcessingSource
		),
		(
			transition: "unload%".format( index ).asSymbol,
			inputPlaces: [ "u%".format( index ).asSymbol ],
			outputPlaces: [ "fu%".format( index ).asSymbol, unloadPlace ],
			inhibitorPlaces: [],
			clock: unloadClock,
			source: unloadSource
		)
	]
}
)

//////////////////////////////////////////////////////////////////////
// Single kanban cell
// from Marsan et. "Modelling with generalized stochastic petri nets"
//////////////////////////////////////////////////////////////////////

(
~singleKanban = {| index = 0, enterCellPlace, enterCellSource, cards = 1, inMSource, outMClock = 1, outMSource, exitCellPlace, exitCellSource |
	[
		(
			transition: "enterCell%".format( index ).asSymbol,
			inputPlaces: [ enterCellPlace, "bb%".format( index ).asSymbol, cards ],
			outputPlaces: [ "ib%".format( index ).asSymbol ],
			inhibitorPlaces: [],
			clock: 0,
			source: enterCellSource
		),
		(
			transition: "inM%".format( index ).asSymbol,
			inputPlaces: [ "ib%", "idleM%" ].collect {| string | string.format( index ).asSymbol } ++ [ 1 ],
			outputPlaces: [ "busyM%".format( index ).asSymbol ],
			inhibitorPlaces: [],
			clock: 0,
			source: inMSource
		),
		(
			transition: "outM%".format( index ).asSymbol,
			inputPlaces: [ "busyM%".format( index ).asSymbol ],
			outputPlaces: [ "ob%", "idleM%" ].collect {| string | string.format( index ).asSymbol },
			clock: outMClock,
			source: outMSource
		),
		(
			transition: "exitCell%".format( index ).asSymbol,
			inputPlaces: [ "ob%".format( index ).asSymbol ],
			outputPlaces: [ exitCellPlace, "bb%".format( index ).asSymbol ],
			inhibitorPlaces: [],
			clock: 0,
			source: exitCellSource
		)
	]
}
)
