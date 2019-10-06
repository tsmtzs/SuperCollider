//////////////////////////////////////////////////////////////////////
// Nowhere differentiable solutions of the functional equation
// 
// f( \frac{x}2 ) = \alpha_0 f(x) + g_0(x)
// f( \frac{x+1}2 ) = \alpha_1 f(x) + g_1(x)
// 
// where |\alpha_i | < 1, g_i continuous, f: [0,1] \rightarrow \mathbb{R}
// see: 
// * Girgensohn, Roland. “Nowhere Differentiable Solutions of a System of Functional Equations.” 
//		Aequationes Mathematicae 47, no. 1 (1994): 89–99. https://doi.org/10.1007/bf01838143.
// * Girgensohn, Roland, Hans-Heinrich Kairies, and Weinian Zhang.
//		“Regular and Irregular Solutions of a System of Functional Equations.”
//		Aequationes Mathematicae 72, no. 1-2 (2006): 27–40. https://doi.org/10.1007/s00010-006-2823-0.
// * Kairies, Hans-Heinrich. “Functional Equations for Peculiar Functions.”
//		Aequationes Mathematicae 53, no. 1-2 (1997): 207–41. https://doi.org/10.1007/bf02215973.
// * Experimental Mathematics in Action. Chapter 5. Wellesley, Ma.: A K Peters, 2007.
//////////////////////////////////////////////////////////////////////


Girgensohn {
	classvar <top;
	var >a0, >a1, >g0, >g1;
	var listX, listY;
	var f0, f1, f05;

	*new {|a0 = 1, a1 = 1, g0 = 0, g1 = 0, depth = 10|
		if (top.isNil){
			top = this.basicNew(a0, a1, g0, g1);
			^ top.makeFunc(depth)
		}{
			^ top.a0_(a0).a1_(a1).g0_(g0).g1_(g1)
			.init
			.makeFunc(depth)
		}
	}

	*basicNew {|a0 = 1, a1 = 1, g0 = 0, g1 = 0|
		^ super.newCopyArgs(a0, a1, g0, g1)
		.init
	}

	init {
		if (abs(a0) >= 1 or: {abs(a1) >= 1} ){
			"a0 and a1 must be real numbers in the interval (-1,1).".throw
		};
		f0 = g0.(0) /(1 - a0);		// value of f at 0
		f1 = g1.(1) /(1 - a1);		// value of f at 1
		f05 = a0 * f1 + g0.(1);			// value of f at 0.5
		# listX, listY = List[] ! 2
	}

	prBitRev {|anArray, depth|
		var bitRevI, powOfTwo;
		powOfTwo = 2.pow(depth);
		for (1, powOfTwo - 1, {|i|
			bitRevI = i.asBinaryDigits(depth).reverse.convertDigits(2);
			if (bitRevI > i){anArray.swap(i, bitRevI);};
		});
		^ anArray
	}

	prLeftBranch {|x, f|
		var leftX, leftValue;
		leftX = 0.5 * x;
		leftValue = (a0 * f) + g0.(x);
		^ [leftX, leftValue]
	}
	prRightBranch {|x, f|
		var rightX, rightValue;
		rightX = x + 1 * 0.5;
		rightValue = a1 * f + g1.(x);
		^ [rightX, rightValue]
	}

	prRecursion {|depth|
		var i = 1, leftValues, rightValues, x, y;
		var size;

		size = 2.pow(depth);			// you calculate twice this( in method 'prBitRev'). fix it.

		// [ listX, listY ].do (_.clear);
		listX.addAll([0, 0.5]);
		listY.addAll([f0, f05]);

		while {
			listX.size  < size
		}{
			x = listX[i];
			y = listY[i];
			leftValues = this.prLeftBranch(x, y);
			rightValues = this.prRightBranch(x, y);
			listX.addAll([leftValues[0], rightValues[0]]);
			listY.addAll([leftValues[1], rightValues[1]]);
			i = i + 1;
		};
	}

	makeFunc {|depth = 10|
		^ this.prRecursion(depth)
		.prBitRev(listY, depth)
		.asArray
	}

	*takagi {|depth = 10|
		var a = 0.5, g0, g1;
		g0 = {|x| 0.5 * x};
		g1 = {|x| 1 - x * 0.5};

		^this.new(a, a, g0, g1, depth)
	}

	*weierstrass {|a = 0.5, theta = 0.0, depth = 10|
		var g0, g1;
		if (a <= 0 or: {a >= 1}){
			"Parameter 'a' takes values in the open interval (0,1)".throw;
		};
		g0 = {|x| sin(pi * x + theta)};
		g1 = {|x| -1*sin(pi*x + theta)};

		^ this.new(a, a, g0, g1, depth)
	}

	*takacs {|rho = 2, depth = 10|
		var reciprocal;
		if (rho <= 0 or: {rho == 1}){
			"Parameter 'rho' is a positive real number, not equal to 1".throw
		};
		reciprocal = (1+rho).reciprocal; 

		^ this.new(reciprocal, reciprocal * rho, 0, reciprocal, depth)
	}

	*kairies {|depth = 10|
		var g0, g1;
		g0 = {|x| sin(pi * x)};
		g1 = {|x| sin(x + 1 * pi)};
		^ this.new(0.5, 0.5, g0, g1, depth)
	}

	*deRham {|a = 0.5, depth = 10|
		if (a <= 0 or: {a >= 1}){
			"Parameter 'a' must be in the open interval (0,1).".throw
		};

		^ this.new(a, 1 - a, 0, a)
	}

	*knopp {|a = 0.5, g = ({|x| sin(2pi * x)}), depth = 10|
		var g0, g1;
		// "Function g must be a 1-periodic function".warn; // This is true...
		if (abs(a) >= 1){
			"Parameter 'a' must be in the open interval (-1,1).".throw
		};
		g0 = {|x| g.(0.5 * x)};
		g1 = {|x| g.(x + 1 * 0.5)};

		^ this.new(a, a, g0, g1, depth)
	}
}
