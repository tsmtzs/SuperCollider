PNPlace {
	classvar <>all;
	var <name, <tokens;

	*initClass {
		all = IdentityDictionary.new;
	}

	//methods *new, *at and prAdd from Pdef

	// global storage
	*at { | key |
		^this.all.at(key)
	}

	*new {| key, anInteger |
		var place;
		place = this.at( key );
		if( place.isNil){
			place = super.newCopyArgs( key, anInteger ?? { 0 } ).prAdd;
		}{
			if( anInteger.notNil ){ place.tokens_( anInteger ) };
		}
		^place
	}

	*clearAll {
		this.all.clear;
	}

	prAdd {
		all.put( name, this );
	}

	tokens_ { | anInteger |
		this.throwIfNotValidInt( anInteger );
		tokens = anInteger;
	}

	addOneToken { tokens = tokens + 1 }

	removeOneToken { tokens = 0.max(tokens - 1) }

	addTokens { | anInteger |
		this.throwIfNotValidInt( anInteger );
		tokens = tokens + anInteger;
	}

	removeTokens { | anInteger |
		this.addTokens( -1 * anInteger );
	}

	isEmpty { ^ tokens === 0 }

	gui {| aWindow | }

	printOn { | stream |
		stream << this.class.name << "( "<< name <<" , " << tokens  << " )";
	}

	throwIfNotValidInt {| anObject |
		if ( anObject.isKindOf( Integer ).not ) {
			Error("Argument % is not an integer".format( anObject.asString )).throw;
		};

		if( anObject < 0 ){
			Error("Tokens % should be positive".format( anObject )).throw;
		};
	}

	isTransition { ^false }
	isPlace { ^true }
}