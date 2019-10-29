// //////////////////////////////////////////////////////////////////////////////////////////
// A ScoreTimer instance accepts a Score object and creates a gui with a timer.
// This timer shows the current playback time of the given Score.
// //////////////////////////////////////////////////////////////////////////////////////////
ScoreTimer {
	// Delete var score? Only use aScore as argument for duration
	var <score, <duration, <>timeStep = 0.05, time;
	var window, routine, durText;

	*new {|aScore|
		^ super.newCopyArgs(aScore).init
	}

	init {|aScore|
		score.addDependant(this);
		duration = score.score.last[0];

		time = 0.0;

		if(window.isNil){
			window = Window( 
				"ScoreTimer", 
				Rect(Window.screenBounds.right - 380, Window.screenBounds.bottom - 180, 350, 150), 
				false 
			).alpha_(0.7).background_(Color.grey(0.7));

			StaticText(window, Rect(10, 30, 115, 30))
			.string_("Current time:").font_(Font.serif(16));
			
			StaticText(window, Rect(10, 90, 115, 30))
			.string_("Duration:").font_(Font.serif(16));
			
			durText = StaticText(window, Rect(135, 90, 190, 30))
			.font_(Font.sansSerif(28)).stringColor_(Color.red);

			// window.front.alwaysOnTop_(true);
			window.front;

			window.drawFunc = {
				Pen.color = Color.blue;
				Pen.font = Font.sansSerif(28);
				Pen.stringLeftJustIn(time.min(duration).asTimeString(0.001), Rect(135, 30, 190, 30))
			};

			routine = Routine {
				{time < duration}.while {
					time = time + timeStep;
					defer {window.refresh};
					timeStep.wait;
				};
			}
		};

		durText.string_(duration.asTimeString(0.001));
	}

	score_ {|aScore|
		score.removeDependant(this);
		score = aScore;
		time = 0.0;
		window.refresh;
		this.init;
	}

	zeroTimer {
		time = 0.0;
		window.refresh;
	}

	// Different tempi for  score.play work only 
	// if TempoClock.default is used
	// play { 	
	// 	this.zeroTimer;
	// 	routine.reset.play
	// }
	// stop { 
	// 	routine.stop;
	// 	this.zeroTimer 
	// }

	// *play {|aScore|
	// 	^this.new(aScore).play
	// }

	update {|aScore, what, aTempoClock|
		switch( what,
			\play, { 
				// routine.clock_( aTempoClock.debug("tempoClock") ?? { TempoClock.default } );
				// this.play 
				this.zeroTimer;
				routine.reset.play(clock: (aTempoClock ?? {TempoClock.default}))
			},
			\stop, { 
				routine.stop;
				this.zeroTimer 
				// this.stop 
			}
		)
	}
}