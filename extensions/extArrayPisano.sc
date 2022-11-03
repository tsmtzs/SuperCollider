+ Array { 
	// The pisano period for the given integer.
	// see:
	// https://en.wikipedia.org/wiki/Pisano_period
	// accessed: 23/10/2019
	*pisano { |divisor = 3|
		var list, lastPair, sum;
		if (divisor.isInteger.not or: { divisor < 0 } ){
			"The divisor must be a positive integer".throw
		};
		list = List[0, 1];
		while {
			lastPair = list.keep(-2);
			sum = lastPair.sum;
			sum != 1 or: { lastPair.last != 0 }
		} {
			list.add(lastPair.sum % divisor);
		};
		^list.array.drop(-1)
	}
}