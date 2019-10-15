////////////////////////////////////////////////////////////////////////////////
// A subclass of Score where the score is an array of triads.
// Each triad is of the form
//		[ time, func, server ]
// where
//		time: the time (sec) that the event will occur
//		func: a function ( which might use closures - hence the name CScore )
//			  that returns an OSC msg when evaluated
//		server: the Server to send the OSC msg from func
// 
// CAUTION: Needs more work - not thoroughly tested
// see again the message 'sendBundle' in the play method
// 
// Use this class if you want to have a Score object
// that plays in RT along with other OSC msgs,
// so that there is no conflict with
// the node IDs.
////////////////////////////////////////////////////////////////////////////////
CScore : Score {
	// a problem here if list is nil
	init {|list|
		var servers;
		servers = list.collect {|cmd| cmd[2]}.asSet;
		servers.do {|aServer|
			score = score.add( 
				[0.0, ["/g_new", 1, 0, 0], aServer]
			);
		};
		score =  score ++ list;
		this.sort;
	}

	// the osc bundle from score is of the form
	// [time, aFunction, aServer]
	play { arg server, clock, quant=0.0;
		var size, osccmd, timekeep, inserver, rout;
		isPlaying.not.if({
			size = score.size;
			timekeep = 0;
			routine = Routine({
				size.do { |i|
					var deltatime, msg;
					osccmd = score[i];
					deltatime = osccmd[0];
					inserver = server ?? {osccmd[2]} ?? {Server.default};
					msg = osccmd[1].value;
					(deltatime-timekeep).wait;
					// look again the next line. Does it work in all cases?
					inserver.sendBundle( inserver.latency, msg );
					timekeep = deltatime;
				};
				isPlaying = false;
			});
			isPlaying = true;
			this.changed(\play, clock);
			routine.play(clock, quant);
		}, {"Score already playing".warn;}
		);
	}

	*write { arg list, oscFilePath, clock;
		var osccmd, f, tempoFactor;
		f = File(oscFilePath, "w");
		tempoFactor = (clock ? TempoClock.default).tempo.reciprocal;
		protect {
			list.size.do { |i|
				var msg = list[i].copy;
				msg[0] = msg[0] * tempoFactor;
				msg[1] = msg[1].value;
				osccmd = msg.keep(2).asRawOSC;
				f.write(osccmd.size).write(osccmd);
			};
		}{
			f.close;
		};
		//"done".postln;
	}
}