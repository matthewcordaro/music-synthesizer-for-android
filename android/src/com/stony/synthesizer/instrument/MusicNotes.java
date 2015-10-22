package com.stony.synthesizer.instrument;

public class MusicNotes {
  
  int note, channel, velocity, isNote, instrument;
  private int dur;
  long st, played;
  public MusicNotes(int n, int c, int v, int is, int d, int s, int i)
  {
    note = n;
    channel = c;
    velocity = v;
    isNote = is;
    dur=d;
    st = s;
    instrument = i;
  }

  public int getNote() {
    return note;
  }
  public int getChannel() {
    return channel;
  }
  public int getVelocity() {
    return velocity;
  }
  public int getIsNote() {
    return isNote;
  }
  public int getDur() {
    return dur;
  }
  public long getSt() {
    return st;
  }
  public long getPlayed() {
    return played;
  }

  public void setDur(int duration) {
    dur = duration;
  }
  
  public int getInstrument() {
    return instrument;
  }
  
  public void setInstrument(int instrument) {
    this.instrument = instrument;
  }
  
  public String toString(){    
    return "Note: "+note+" Channel"+channel+" Instrument: "+instrument+" Duration: "+dur;
  }
}
