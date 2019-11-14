package com.canary.desktop.snd;

import jass.engine.SinkIsFullException;
import jass.render.SourcePlayer;

public class Speaker extends AudioOutput {
  public final String OUTMIXER = "default";
  
    public Speaker() {
      setupPlayer();

        title = "Spkr";
        keys = new char[] {'\0'};
    }

    public Speaker(PlayerContainer player) {
      this.player = player;

        title = "Spkr";
        keys = new char[] {'\0'};
    }

    protected void setupPlayer() {
        player.content = new SourcePlayer(BUFFER_SIZE, NUM_BUFFERS_RTAUDIO, SRATE, OUTMIXER);
    }
}
