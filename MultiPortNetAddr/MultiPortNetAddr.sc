MultiPortNetAddr {
	var <addr = 0, <ports;
	var addresses;

	*disconnectAll {
		NetAddr.disconnectAll;
	}

	*connections {
		^ NetAddr.connections;
	}

	*new { |addr, portArray|
		^ super.newCopyArgs(addr, portArray).init;
	}

	init {
		addresses = this.ports.collect { |port| NetAddr(this.addr, port) };
	}

	sendMsg { |...args|
		addresses.do { |aNetAddr|
			aNetAddr.sendMsg(*args)
		};
	}

	isConnected {
		^ addresses.collect { |aNetAddr| aNetAddr.isConnected };
	}

	connect { |disconnectHandler|
		addresses.do { |aNetAddr| aNetAddr.connect(disconnectHandler) }
	}

	disconnect {
		addresses.do { |aNetAddr| aNetAddr.disconnect }
	}

	ip {
		^ addr.asIPString;
	}
}