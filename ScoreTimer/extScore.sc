// ////////////////////////////////////////////////////////////
// the methods 'play' and 'stop' are overidden
// so that ScoreTimer can function properly
// ////////////////////////////////////////////////////////////
+ Score {
	// just one line added to the overidden methods
	play {|server, clock, quant=0.0|
		var size, osccmd, timekeep, inserver, rout;
		isPlaying.not.if({
			inserver = server ? Server.default;
			size = score.size;
			timekeep = 0;
			routine = Routine({
				size.do { |i|
					var deltatime, msg;
					osccmd = score[i];
					deltatime = osccmd[0];
					msg = osccmd.copyToEnd(1);
					(deltatime-timekeep).wait;
					inserver.listSendBundle(inserver.latency, msg);
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

	stop {
		isPlaying.if({
			this.changed(\stop);
			routine.stop; 
			isPlaying = false; 
			routine = nil;
		}, {
			"Score not playing".warn;
		});
	}

	// wouldn't be better if this method is replaced by an instance var?
	timer {
		^ ScoreTimer(this)
	}
}