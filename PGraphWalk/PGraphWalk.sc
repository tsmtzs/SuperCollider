PGraphWalk : Pattern {
	// Make this class a subclass of ListPattern? In such a case
	// instance var 'vertices' should be the 'list' var
	// and 'steps' should be 'repeats'
	var <>transitionMatrix, <>vertices, transitions;
	var <>startVertex, <>steps, size;

	*new { | transitionMatrix, anArray, startVertex = 0, steps = inf |
		^super.new
		.transitionMatrix_( transitionMatrix )
		.vertices_( anArray )
		.startVertex_( startVertex )
		.steps_( steps )
		.init
	}

	init {
		var vertexTransitions;
		// add some checks for the dimension of transition matrix
		// and the size of vertices array ?
		size = vertices.size;
		transitions = Array.newClear( size );
		transitionMatrix.do {| row, i |
			vertexTransitions = row.collect {| prob, j |
				[ j, prob ]
			}.select{ | pair |
				pair[ 1 ] != 0			// check also if element is nil?
			};
			if( vertexTransitions.notEmpty ){ transitions.put( i, vertexTransitions.flop ); };
		};
	}

	storeArgs { ^[ transitionMatrix, vertices, startVertex, steps ] }

	embedInStream {| inval |
		var vertex, nextVertices = 0, transitionProbs, repeats;

		repeats = steps.value( inval );
		vertex = startVertex.value( inval ).clip( 0, size - 1 );

		while {
			# nextVertices, transitionProbs = transitions[ vertex ].asArray;
			inval = vertices[ vertex ].embedInStream( inval );
			nextVertices.notNil and: { repeats > 0 }
		}{
			vertex = nextVertices.wchoose( transitionProbs );
			repeats = repeats - 1;
		};

		^inval
	}
}