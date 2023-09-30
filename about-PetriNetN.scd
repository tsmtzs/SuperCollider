// NOTE:
// The classes presented here are a naive and incomplete attempt to build an algorithmic sound framework based on Petri Nets.
// It is based on the assumption that Petri nets "are a promising tool for describing and studying information processing systems that are
// characterized as being concurrent, asynchronous, distributed, parallel, nondeterministic, and/or stochastic" (Murata 1989).
// For an introduction and terminology refer to (Murata 1989), (Wikipedia 2023). Further resources of interest might be (Girault et al. 2003),
// (Haas 2002), (Recalde et al. 2003).

// In the literature, Petri net transitions are used to model events and actions. This is the approach we follow here to model sound events.
// All examples that we present should be seen as a demonstration of the classes. They were not included for their musicality and/or network
// properties. In fact, this project was largerly discontinued because of the difficulty in developing intuitive associations between
// sound and network structure.

// REFERENCES and RESOURCES:
// *. Antoni, G.D., & Haus, G. (1982). Music and Causality. International Conference on Mathematics and Computing.
// *. Baratè, A. (2009). Real-time Interaction with Music Structures in IEEE 1599. J. Multim., 4, 15-18.
// *. Baratè, A., Haus, G., & Ludovico, L.A. (2005). Music Analysis and Modeling Through Petri Nets. Computer Music Modeling and Retrieval.
// *. Baratè, A., Haus, G., & Ludovico, L.A. (2007). Petri Nets Applicability to Music Analysis and Composition. International Conference on Mathematics and Computing.
// *. Girault, C., & Valk, R. (2003). Petri Nets for Systems Engineering. Springer Berlin Heidelberg.
// *. Haas, P.J. (2002). Stochastic Petri nets - modelling, stability, simulation. Springer series in operations research.
// *. Matteis, A.D., & Haus, G. (1996). Formalization of generative structures within stravinsky's “The rite of spring”. Journal of New Music Research, 25, 47-76.
// *. Lyon, D.A. (1995). Using stochastic petri nets for real-time nth-order stochastic composition. Computer Music Journal, 19, 13.
// *. Murata, T. (1989). Petri nets: Properties, analysis and applications. Proc. IEEE, 77, 541-580.
// *. Recalde, L., Suárez, M.S., Ezpeleta, J., & Teruel, E. (2003). Petri Nets and Manufacturing Systems: An Examples-Driven Tour. Lectures on Concurrency and Petri Nets.
// *. Rodriguez, A. (2011). Formal Music Representation ; a Case Study : the Model of Ravel ' s Bolero by Petri Nets † Goffredo Haus.
// *. Wikipedia contributors. (2023, September 2). Petri net. In Wikipedia, The Free Encyclopedia. Retrieved 05:39, September 9, 2023, from https://en.wikipedia.org/w/index.php?title=Petri_net&oldid=1173376221

(
PNPlace.clearAll;

~net = PetriNetN(
	(
		transition: \t1,
		inputPlaces: [\p1],
		outputPlaces: [\p2]
	),
	(
		transition: \t2,
		inputPlaces: [\p2],
		outputPlaces: [\p3]
	),
	(
		transition: \t3,
		inputPlaces: [\p3, \p4],
		outputPlaces: [\p7]
	),
	(
		transition: \t4,
		inputPlaces: [\p4],
		outputPlaces: [\p5]
	),
	(
		transition: \t5,
		inputPlaces: [\p5],
		outputPlaces: [\p6]
	),
	(
		transition: \t6,
		inputPlaces: [\p6, \p1],
		outputPlaces: [\p7]
	),
	(
		transition: \t7,
		inputPlaces: [\p7],
		outputPlaces: [\p1, \p4]
	)
);
)

~net.marking;
~net.transitions;
// Access a transition
~net[\t1];

~net.places;
// Access a place
~net[\p1]

~net.sources;
~net.setSources((t1: 60, t2: 62, t3: 64, t4: 69, t5: 71, t6: 72, t7: 55));

// Set the number of tokens in each place:
~net.setMarking((p1: 1, p2: 0, p3: 1, p4: 1, p5: 0, p6: 0, p7: 0));

// Change the tokens of one place:
~net[\p1].addOneToken;
~net[\p2].addTokens(3);
~net[\p3].tokens_(4);

~netPattern = ~net.pattern;
~netPattern.asStream.nextN(10);

// For each transition you can change the way that it consumes tokens from its input places
// when it fires. For this assign a function to the 'updateInputPlaces' instance variable of the transition.
// E.x. when transition \t3 fires, remove one token from a random input place:
~net[\t3].updateInputPlaces_({ | aSet | aSet.choose.removeOneToken }); // aSet: The Set of input places

~net.setMarking((p1: 1, p2: 0, p3: 1, p4: 1, p5: 0, p6: 0, p7: 0));

~netPattern = ~net.pattern;
~netPattern.asStream.nextN(10);

// The default 'updateInputPlaces' function removes one token from each input place.
~net[\t3].updateInputPlaces_(PNTransitionN.updateInputPlacesDefault)

// For each transition you can change the number of tokens that it adds to each output place
// when it fires. For this assign a function to the 'updateOutputPlaces' instance variable of the transition.
// E.x. when transition \t7 fires, add three tokens to each output place:
~net[\t7].updateInputPlaces_({ | aSet | aSet.do { | aPlace | aPlace.addTokens(3) } }); // aSet: The Set of output places

~net.setMarking((p1: 1, p2: 0, p3: 1, p4: 1, p5: 0, p6: 0, p7: 0));

~netPattern = ~net.pattern;
~netPattern.asStream.nextN(10);

// The default 'updateOutputPlaces' function adds one token to each output place.
~net[\t7].updateOutputPlaces_(PNTransitionN.updateOutputPlacesDefault);

// To change the condition on which a transition is enabled, set the instance variable 'enabledFunction'.
// E.x. transition \t3 is enabled when there is at least one token in its input places:
//		inputPlaces, inhibitorPlaces: Set instances with the input and inhibitor places, respectively, of the transition
~net[\t3].enabledFunction({ | inputPlaces, inhibitorPlaces | inputPlaces.sum { | aPlace | aPlace.tokens } > 1 });

~net.setMarking((p1: 1, p2: 0, p3: 1, p4: 0, p5: 0, p6: 0, p7: 0));
~net[\t3].isEnabled;

~net[\p4].tokens_(1);
~net[\t3].isEnabled;

// By default, a transition is enabled when each input place holds at least one token and each inhibitor place
// contains no tokens.
~net[\t3].enabledFunction(PNTransitionN.enabledFunctionDefault);

// A sound example:
(
~net.setMarking((p1: 1, p2: 0, p3: 1, p4: 1, p5: 0, p6: 0, p7: 0));

Pbind(
	\midinote, ~net.pattern
).play;
)

// While the above event pattern is playing you can change
// the tokens of a place and/or the source, update input/output functions, enabled function of a transition.
~net.setSource(\t7, 48);
// or
~net[\t7].source_(60);
~net.setSource(\t7, { [83, 79, 81].choose });
~net.setSource(\t7, Pseq([83, 79, 81], inf).asStream);

// //////////////////////////////////////////////////
// In this implementation of PetriNetN a transition fires whenever is enabled.
// On the other hand, two or more transitions of a Petri Net are in conflict when they share input places and are enabled at the same time.
// In a situation like this one transition might fire before the others, consuming tokens from its input places. This change of marking
// can potentially turn an enabled transition to disabled.
// In the above example, when places \p1, \p6 have at least one token, then transitions \t1, \t6 are in conflict.
// With PetriNetN we can model conflicting transitions by adding places/transtions and changing the function 'updateOutputPlaces'.
// For example, the next network is a modification of the above one:

(
PNPlace.clearAll;

~net = PetriNetN(
	(
		transition: \t0,
		inputPlaces: [\p0],
		outputPlaces: [\p1a, \p1b],
		updateOutputPlaces: {| aSet | aSet.choose.addOneToken }
	),
	(
		transition: \t1,
		inputPlaces: [\p1a],
		outputPlaces: [\p2]
	),
	(
		transition: \t2,
		inputPlaces: [\p2],
		outputPlaces: [\p3]
	),
	(
		transition: \t3,
		inputPlaces: [\p3, \p5b],
		outputPlaces: [\p8]
	),
	(
		transition: \t4,
		inputPlaces: [\p4],
		outputPlaces: [\p5a, \p5b],
		updateOutputPlaces: {| aSet | aSet.choose.addOneToken }
	),
	(
		transition: \t5,
		inputPlaces: [\p5a],
		outputPlaces: [\p6]
	),
	(
		transition: \t6,
		inputPlaces: [\p6],
		outputPlaces: [\p7]
	),
	(
		transition: \t7,
		inputPlaces: [\p7, \p1b],
		outputPlaces: [\p8]
	),
	(
		transition: \t8,
		inputPlaces: [\p8],
		outputPlaces: [\p0, \p4]
	),
);
)
~net.setSources((t0: 59, t1: 60, t2: 62, t3: 64, t4: 69, t5: 71, t6: 72, t7: 74, t8: { [48, 46, \rest].choose }));
~net.setMarking((p0: 2, p1a: 0, p1b: 0, p2: 0, p3: 1, p4: 2, p5a: 0, p5b: 0, p6: 0, p7: 1, p8: 0));

~netPattern = ~net.pattern;
~netPattern.asStream.nextN(50);

TempoClock.default.tempo = 3;
(
~net.setMarking((p0: 2, p1a: 0, p1b: 0, p2: 0, p3: 1, p4: 2, p5a: 0, p5b: 0, p6: 0, p7: 1, p8: 1));

Pbind(
	\midinote, ~net.pattern
).play;
)

// //////////////////////////////////////////////////
// Timed transitions
// //////////////////////////////////////////////////
(
PNPlace.clearAll;

~net = PetriNetN(
	(
		transition: \t1,
		inputPlaces: [\p1],
		outputPlaces: [\p2],
		clock: 1
	),
	(
		transition: \t2,
		inputPlaces: [\p2],
		outputPlaces: [\p3],
		clock: 1
	),
	(
		transition: \t3,
		inputPlaces: [\p3, \p4],
		outputPlaces: [\p7],
		clock: 1
	),
	(
		transition: \t4,
		inputPlaces: [\p4],
		outputPlaces: [\p5],
		clock: 1
	),
	(
		transition: \t5,
		inputPlaces: [\p5],
		outputPlaces: [\p6],
		clock: 1
	),
	(
		transition: \t6,
		inputPlaces: [\p6, \p1],
		outputPlaces: [\p7],
		clock: 1
	),
	(
		transition: \t7,
		inputPlaces: [\p7],
		outputPlaces: [\p1, \p4]
	),
);
)

// When at least one transition is 'timed', then the network is of type 'timed'.
// Transitions without an explicit 'clock' function get 'clock: 1'.
// In this example transition \t7 will be a timed transition with 'clock: 1'.
~net.type;
~net[\t7].isTimed;
~net[\t7].clock;

~net.sources;
// ASSUMPTION: sources are events
~net.setSources((t1: (midinote: 60), t2: (midinote: 62), t3: (midinote: 64), t4: (midinote: 69), t5: (midinote: 71), t6: (midinote: 72), t7: (midinote: 55)));

~net.setMarking((p1: 1, p2: 0, p3: 1, p4: 1, p5: 0, p6: 0, p7: 0));

~net.marking;

p = ~net.pattern.asStream;
p.next(());

// PNPostState works only with timed Petri Nets.
~post = PNPostState(~net).routine.asStream;
Post << ~post.nextN(30);

(
~net.setMarking((p1: 1, p2: 0, p3: 1, p4: 1, p5: 0, p6: 0, p7: 10));

~net.pattern.play;
)

(
~net.at(\t1).clock_({ [0.5, 1].choose });
~net.at(\t4).clock_({ [0.5, 1].choose });
~net.at(\t5).clock_(0.5);
)
