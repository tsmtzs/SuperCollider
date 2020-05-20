/*
	Given an array with the values of a function, this class, computes the local extrema
	of the function, as well as the positions at where this extrema occur.
*/
Extrema {
	var relativeMaxima, relativeMinima, relativeExtrema;
	var relativeMaximaPositions, relativeMinimaPositions, relativeExtremaPositions;
	var array, size, diff;
	/*
		relativeMaxima, etc, contain the value of the corresponding local max, etc
		relativeMaximaPositions, etc, contain the position at which a local max
		occurs, normalized in [0,1] with respect to the size of the given array, etc
	*/

	*new { |anArray|
		^super.new.init(anArray)
	}

	init { |anArray|
		array = anArray;
		size = anArray.size;
		diff = anArray.differentiate;
		# relativeMaxima, relativeMinima, relativeExtrema = { List[] } ! 3;
		# relativeMaximaPositions, relativeMinimaPositions, relativeExtremaPositions  = { List[] } ! 3;
	}

	relativeMaxima {
		if(relativeMaxima.isEmpty){
			this.findExtrema(\max)
		}
		^relativeMaxima
	}

	relativeMinima {
		if(relativeMinima.isEmpty){
			this.findExtrema(\min)
		}
		^relativeMinima
	}

	relativeExtrema {
		if(relativeExtrema.isEmpty){
			this.findExtrema(\all)
		}
		^relativeExtrema
	}

	relativeMaximaPositions {
		if(relativeMaximaPositions.isEmpty){
			this.findExtrema(\max)
		}
		^relativeMaximaPositions
	}

	relativeMinimaPositions {
		if(relativeMinimaPositions.isEmpty){
			this.findExtrema(\min)
		}
		^relativeMinimaPositions
	}

	relativeExtremaPositions {
		if(relativeExtremaPositions.isEmpty){
			this.findExtrema(\all)
		}
		^relativeExtremaPositions
	}

	findExtrema { |aSymbol|
		var extremaType;
		// change the next 'case' with something more robust?
		extremaType = case
		{ aSymbol == \max } {
			{ |anInteger, aNumber|
				if(aNumber > 0) {
					relativeMaxima.add(array[ anInteger ]);
					relativeMaximaPositions.add(anInteger / size)
				}
			}
		}
		{ aSymbol == \min }{
			{ |anInteger, aNumber|
				if(aNumber < 0) {
					relativeMinima.add(array[ anInteger ]);
					relativeMinimaPositions.add(anInteger / size)
				}
			}
		}
		{
			{ |anInteger, aNumber|
				relativeExtrema.add(array[ anInteger ]);
				relativeExtremaPositions.add(anInteger / size)
			}
		};

		diff.doAdjacentPairs { |a, b, index|
			if(a * b < 0) {
				extremaType.(index, a)
			}
		}
	}
}
