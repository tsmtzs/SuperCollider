+ ScoreStreamPlayer {
	// change this name
	makeScoreWithEnding { | stream, duration = 1, event, timeOffset = 0, releaseTime = 1|
		var ev, startTime, proto;
		proto = (
			server: this,

			schedBundle: { | lag, offset, server ...bundle |
				this.add(offset * tempo + lag + beats, bundle)
			},
			schedBundleArray: { | lag, offset, server, bundle |
				this.add(offset * tempo + lag + beats, bundle)
			}
		);

		event = event ? Event.default;
		event = event.copy.putAll(proto);
		beats = timeOffset;
		tempo = 1;
		bundleList = [];
		maxTime = timeOffset + duration;
		Routine {
			thisThread.clock = this;
			while ({
				thisThread.beats = beats;
				ev = stream.next(event.copy);
				(maxTime >= beats) && ev.notNil
			},{
				ev.putAll(proto);
				ev.play;
				beats = ev.delta * tempo + beats
			});

			stream = ev[ \endStream ];
			// can this be written in a better way?
			// Most of the following duplicates the previous lines
			thisThread.beats = beats;
			while {
				ev = stream.next( event.copy );
				ev.notNil
			}{
				ev.putAll( proto );
				ev.play;
				// beats = ev.delta * tempo + beats;
			};

		}.next;
		bundleList = bundleList.sort({ | a, b | b[0] >= a[0] });
		if ((startTime = bundleList[0][0]) < 0 ) {
			timeOffset = timeOffset - startTime;
		};


		^Score(bundleList.add([ duration + timeOffset + releaseTime, [\c_set, 0, 0]]) );
	}
}
