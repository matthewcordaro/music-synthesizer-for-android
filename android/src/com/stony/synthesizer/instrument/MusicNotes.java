package com.stony.synthesizer.instrument;

public class MusicNotes {
  
  int note, channel, velocity, isNote;
  private int dur;
  long st, played;
  public void addNote(int n, int c, int v, int is, int d, int s)
  {
    note = n;
    channel = c;
    velocity = v;
    isNote = is;
    dur=d;
    st = s;
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

  public void setDur(int i) {
    // TODO Auto-generated method stub
    
  }

}
