
/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.levien.synthesizer.android.ui;

import java.io.*;
import java.util.*;

import android.view.*;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.levien.synthesizer.R;
import com.levien.synthesizer.android.widgets.keyboard.KeyboardSpec;
import com.levien.synthesizer.android.widgets.keyboard.KeyboardView;
import com.levien.synthesizer.android.widgets.keyboard.KeyboardViewListener;
import com.levien.synthesizer.android.widgets.keyboard.ScrollStripView;
import com.levien.synthesizer.android.widgets.knob.KnobListener;
import com.levien.synthesizer.android.widgets.knob.KnobView;
import com.levien.synthesizer.core.midi.MidiAdapter;
import com.levien.synthesizer.core.midi.MidiListener;
import com.stony.synthesizer.instrument.MusicNotes;

/**
 * Activity for simply playing the piano.
 * This version is hacked up to send MIDI to the C++ engine. This needs to
 * be refactored to make it cleaner.
 */
public class PianoActivity2 extends SynthActivity implements OnSharedPreferenceChangeListener 
{
  StringBuffer recordbuf=new StringBuffer("");
  Runnable r;
  
  @SuppressLint("InlinedApi")
  @Override
  public void onCreate(Bundle savedInstanceState) {
    Log.d("synth", "activity onCreate " + getIntent());
    super.onCreate(savedInstanceState);
    
    int orientation = this.getRequestedOrientation();
    int rotation = ((WindowManager) this.getSystemService(
            Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
    switch (rotation) {
    case Surface.ROTATION_0:
        orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        break;
    case Surface.ROTATION_90:
        orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        break;
    case Surface.ROTATION_180:
        orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
        break;
    default:
        orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
        break;
    }

    this.setRequestedOrientation(orientation);
    
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    setContentView(R.layout.piano2);

    keyboard_ = (KeyboardView)findViewById(R.id.piano);
    ScrollStripView scrollStrip_ = (ScrollStripView)findViewById(R.id.scrollstrip);
    scrollStrip_.bindKeyboard(keyboard_);
    cutoffKnob_ = (KnobView)findViewById(R.id.cutoffKnob);
    resonanceKnob_ = (KnobView)findViewById(R.id.resonanceKnob);
    overdriveKnob_ = (KnobView)findViewById(R.id.overdriveKnob);
    presetSpinner_ = (Spinner)findViewById(R.id.presetSpinner);
    channelSpinner_=(Spinner)findViewById(R.id.channelSelector);
    tvTimer_ = (TextView)findViewById(R.id.record_timer);
    //For keyboardview.java
    keyboard_.setChannelSpinnner(channelSpinner_);
    
    
    
    filelist_ = (Spinner)findViewById(R.id.filelist);
    keyboard_.setKeyboardViewListener(new KeyboardViewListener(){
      public void noteDown(int channel, int note, int velocity) {
        if(recordBtnIsActive) { // record
          noteOn_ms=getCurrentTimeMilli();
          noteOn_ms=noteOn_ms-startRecording_ms;
          writeFile(0,noteOn_ms);
          writeFile(9,0);
          writeFile(channel,0);
          writeFile(note,0);
          writeFile(velocity,0);
          writeFile(getPresetNumber(),0);
        }
      }
      public void noteUp(int channel, int note) {
        // TODO Auto-generated method stub
        if(recordBtnIsActive){  //record
          noteOff_ms=getCurrentTimeMilli();
          noteOff_ms=noteOff_ms-startRecording_ms;
          writeFile(0,noteOff_ms);
          writeFile(8,0);
          writeFile(channel,0);
          writeFile(note,0);
          writeFile(0,0); // we don't care
          writeFile(getPresetNumber(),0);
        }
      }
    });

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
      setupUsbMidi(getIntent());
    }

    recordBtn = (ImageButton)findViewById(R.id.record_btn);
    recordBtn.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {
        // TODO Auto-generated method stub
        
        if (wannaRecord)  {
          recordBtn.setImageResource(R.drawable.stopnew);
          openFile(); // open file to write file.
          tvTimer_.setText(recordTime);
          t = new Timer();
          t.scheduleAtFixedRate(new TimerTask() {

              @Override
              public void run() {
                  // TODO Auto-generated method stub
                  runOnUiThread(new Runnable() {
                      public void run() {
                        long hours = TimeCounter / 3600;
                        long minutes = (TimeCounter % 3600) / 60;
                        long seconds = TimeCounter % 60;

                        recordTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                          tvTimer_.setText(recordTime); // you can set it to a textView to show it to the user to see the time passing while he is writing.
                          TimeCounter++;
                      }
                  });

              }
          }, 0, 1000);

          
          
          wannaRecord = false;  // next time, this button will stop recording.
          recordBtnIsActive = true; // if you press keyboard, will record.
          Toast.makeText(getApplicationContext(), "Start Recording", Toast.LENGTH_SHORT).show();
          startRecording_ms = getCurrentTimeMilli();  // get start time.
          paused = false;
          resumed = false;
          clicked = false;
          notescnt=0;
          
        }
        else  { // want to stop recording
          t.cancel();
          TimeCounter = 0;
          tvTimer_.setText(recordTime);
          recordTime = "00:00:00";

          nameDialog();
          recordBtn.setImageResource(R.drawable.recordnew);
          DataOutputStream dos = new DataOutputStream(fileout);

          try {
            dos.writeLong(0); // It means deadLine for file.
            dos.writeLong(getCurrentTimeMilli() - startRecording_ms);
          } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }

          closeFile();  // maybe you want to stop recording so close file
          wannaRecord=true; // if you press this btn next time, will record.
          recordBtnIsActive = false;  // although you press keyboard, nothing happens.
          Toast.makeText(getApplicationContext(), "Stop Recording", Toast.LENGTH_SHORT).show();
        }
      }
    });

    playBtn = (ImageButton)findViewById(R.id.play_btn);
    playBtn.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {
        Log.wtf(tag, "In click listener:");
        printFlags();
        /*
        if((notescnt-1) == start_play || notescnt == start_play)
        {
          start_play = 1;
          keyboard_.onNote(m.get(0).getNote(), m.get(0).getVelocity());
          synthMidi_.onNoteOn(m.get(0).getChannel(), m.get(0).getNote(), m.get(0).getVelocity());
          first_current_time = getCurrentTimeMilli();
          ArrayList<Integer> temp = new ArrayList<Integer>();
          temp.add(m.get(0).getVelocity());
          temp.add(m.get(0).getInstrument());
          opennotes.put(m.get(0).getNote(), temp);
          clicked = true;
          readFile();
          
        } */ 
        if(clicked)
        {
          paused=true;
          resumed = false;
          clicked = false;
          readFile();
        }
        else if(paused)
        {
          resumed = true;
          clicked = true;
          readFile();
        }
        else if (!recordBtnIsActive)  {
          if((notescnt-1) == start_play || notescnt == start_play)
          {
            start_play = 1;
          }
          // open file, look at first time.
          // openBtnWasPressed = true; for playBtn's working
          try {
            String path = Environment.getExternalStorageDirectory().
                    getAbsolutePath()+"/here";
            file = new File(path);
            if(!file.exists())
              file.mkdirs();
            file = new File(path+"/"+fileName+".txt");  // real name of file
            try {
              filein = new FileInputStream(file);
            } catch (FileNotFoundException e1) {
              e1.printStackTrace();
            }
            dis = new DataInputStream(filein);  
            sc = new Scanner(file);
            readline = sc.nextLine(); // get first line.
            if(readline != null) {            
              synthMidi_ = synthesizerService_.getMidiListener();
              keyboard_.setMidiListener(synthMidi_);
              copyFile();
              keyboard_.onNote(m.get(0).getNote(), m.get(0).getVelocity());
              synthMidi_.onNoteOn(m.get(0).getChannel(), m.get(0).getNote(), m.get(0).getVelocity());
              first_current_time = getCurrentTimeMilli();
              ArrayList<Integer> temp = new ArrayList<Integer>();
              temp.add(m.get(0).getVelocity());
              temp.add(m.get(0).getInstrument());
              opennotes.put(m.get(0).getNote(), temp);
              opennotes.put(m.get(0).getNote(), temp);
              clicked = true;
              readFile();
            }
 
          } catch (FileNotFoundException e) {
            e.printStackTrace();
          }
        }
        else {
          Toast.makeText(getApplicationContext(), "Please stop recording first.", Toast.LENGTH_SHORT).show();
        }
      }
    });
  }

  /**
   * called if you want to get current time by milliseconds.
   */
  protected long getCurrentTimeMilli() {
    long millis = System.currentTimeMillis();
    return millis;
  }
  
  protected void nameDialog() {
    
    final EditText et = new EditText(this);
    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
    dialog.setTitle("Set File Name")
    .setMessage("What do you want?")
    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        fileName = et.getText().toString();
        //Rename
        String path = Environment.getExternalStorageDirectory().
                getAbsolutePath()+"/here";
        File newfile = new File(path+"/"+fileName+".txt");
        file.renameTo(newfile);
      }
    })
    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        file.delete();
      }
    })
    .setView(et)
    .show();
  }

  protected void openFile() {
    String path = Environment.getExternalStorageDirectory().
            getAbsolutePath()+"/here";
    file = new File(path);
    if(!file.exists())
      file.mkdirs();
    file = new File(path+"/"+"TempRecording"+".txt");  // real name of file
    if (file.exists())
      file.delete();
    try {
      fileout = new FileOutputStream(file,true);
    }
    catch (IOException e) {
      Log.e("PianoActivity2.java", "File open failed: " + e.toString());
    } 
  }

  protected void playFile(File file) {
  try {
    filein = new FileInputStream(file);
    dis = new DataInputStream(filein);  
    sc = new Scanner(file);
  }
  catch (FileNotFoundException e1) {
    e1.printStackTrace();
  }
  readline = sc.nextLine(); // get first line.
  if(readline != null) {            
    synthMidi_ = synthesizerService_.getMidiListener();
    keyboard_.setMidiListener(synthMidi_);
    paused = true;
    readFile();
  }
}

  protected void writeFile(int midiData, long milis)  {
    try {
      DataOutputStream dos = new DataOutputStream(fileout);
      if (milis != 0) {
        dos.writeLong(milis);
      }
      else {
        dos.writeInt(midiData); 
      }

    }
    catch (IOException e) {
      Log.e("PianoActivity2.java", "File write failed: " + e.toString());
    } 
  }

  static HashMap<Integer,ArrayList<Integer>> opennotes = new HashMap<Integer, ArrayList<Integer>>();
  protected void readFile(){
    try {
      Log.wtf(tag, "In readFile: ");
      printFlags();
      if(!paused) {   
        Log.wtf(tag, "Inside not paused:");
        printOpenNotes();
        for(int i=start_play;i<notescnt;i++)
        {
          final int isNote = m.get(i).getIsNote();
          final int channel = m.get(i).getChannel();
          final int note = m.get(i).getNote();
          final int velocity = m.get(i).getVelocity();
          final int instrument = m.get(i).getInstrument();
          r = new Runnable() {
            public void run() {
              //Log.wtf(tag, "start play:"+start_play+" notescnt: "+notescnt);
              if(isNote==9)
              {
                synthesizerService_.getMidiListener().onProgramChange(getChannelNumber(), instrument);  
                keyboard_.onNote(note, velocity);
                synthMidi_.onNoteOn(channel, note, velocity);
                
                ArrayList<Integer> temp = new ArrayList<Integer>();
                temp.add(velocity);
                temp.add(instrument);
                opennotes.put(note, temp);
              }
              else
              {
                synthesizerService_.getMidiListener().onProgramChange(getChannelNumber(), instrument);
                keyboard_.onNote(note, velocity);
                synthMidi_.onNoteOff(channel, note, velocity);
                opennotes.remove(note);              
                
                
                for(int i=start_play;i<notescnt;i++)
                {
                 
                  if(m.get(i).getNote()==note && m.get(i)!=null)
                  {
                    start_play=i+1;
                    break;
                  }
                }   
              }
            }
          };
          handler.postDelayed(r, m.get(i).getDur());
        }
        tvTimer_.setText(recordTime);
        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                runOnUiThread(new Runnable() {
                    public void run() {
                      long hours = TimeCounter / 3600;
                      long minutes = (TimeCounter % 3600) / 60;
                      long seconds = TimeCounter % 60;

                      recordTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                        tvTimer_.setText(recordTime); // you can set it to a textView to show it to the user to see the time passing while he is writing.
                        TimeCounter++;
                        
                        if (TimeCounter*1000 >= totalRecordTime) {
                          t.cancel();
                          TimeCounter = 0;
                        }
                        
                    }
                });

            }
        }, 0, 1000);
       
        
      }
      else if(paused && !resumed)
      {
        Log.wtf(tag, "Inside paused:");
        printOpenNotes();
        long paused_time = getCurrentTimeMilli();
        handler.removeCallbacksAndMessages(null);
        Iterator it = opennotes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            ArrayList<Integer> temp = (ArrayList<Integer>)pair.getValue();
            
            keyboard_.onNote((Integer)pair.getKey(), temp.get(0));
            synthMidi_.onNoteOff(0, (Integer)pair.getKey(), temp.get(0));
            it.remove(); // avoids a ConcurrentModificationException
        }
        
        
        for(int i=start_play;i<notescnt;i++) {
          m.get(i).setDur(m.get(i).getDur() - (int)(paused_time - first_current_time));
        }
        Log.wtf(tag, "Inside paused after duration change:");
        printOpenNotes();
        
        t.cancel();
        tvTimer_.setText(recordTime);
        
        
      }
      else if(resumed)
      {
        Log.wtf(tag, "Inside resumed:");
        printOpenNotes();
        first_current_time = getCurrentTimeMilli();
        Iterator it = opennotes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();          
            ArrayList<Integer> temp = (ArrayList<Integer>)pair.getValue();
            keyboard_.onNote((Integer)pair.getKey(), temp.get(0));
            synthMidi_.onNoteOn(temp.get(1), (Integer)pair.getKey(), temp.get(0));
            it.remove(); // avoids a ConcurrentModificationException
        }
        clicked = true;
        paused = false;
        resumed = false;
        readFile();
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
 
  protected void copyFile(){
    int cnt = 0;
    start_play=0;
    try {
      long first_pressed_time=0,first_current_time=0;
      first_pressed_time=dis.readLong();
      time=first_pressed_time;
      while(time>0)
      {
        if(first_current_time==0)
          first_current_time=getCurrentTimeMilli();
        final int isNote = dis.readInt();
        final int channel = dis.readInt();
        final int note = dis.readInt();
        final int velocity = dis.readInt();   
        final int instrument = dis.readInt();
        long dur = time - first_pressed_time;
        m.add(new MusicNotes(note, channel, velocity, isNote, (int)dur, 0, instrument));
        time = dis.readLong(); 
        cnt++;
        }
      totalRecordTime = dis.readLong();
      
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    notescnt = cnt;
  }
  
  public int getChannelNumber(){
    String channelNumText[] = channelSpinner_.getSelectedItem().toString().split(" ");    
    return Integer.parseInt(channelNumText[1]);
  }
  
  
  public void printOpenNotes(){
    
    Log.wtf(tag, "Printing Open Notes from start_play:"+start_play);
    for(int i=start_play; i<m.size();i++)
      Log.wtf(tag, m.get(i).toString());
  }
  
  public void printFlags(){
    Log.wtf(tag, "Clicked: "+clicked+" paused: "+paused+" resumed: "+resumed+" start play: "+start_play);
  }
  
  public int getPresetNumber(){
    return presetSpinner_.getSelectedItemPosition();
  }

  protected void closeFile()  {
    try {
      fileout.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.synth_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.settings:
        startActivity(new Intent(this, SettingsActivity.class));
        return true;
      case R.id.compose:
        startActivity(new Intent(this, ScoreActivity.class));
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  protected void onDestroy() {
    Log.d("synth", "activity onDestroy");
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
      unregisterReceiver(usbReceiver_);
    }
    super.onDestroy();
  }

  @Override
  protected void onResume() {
    super.onResume();
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    prefs.registerOnSharedPreferenceChangeListener(this);
    onSharedPreferenceChanged(prefs, "keyboard_type");
    onSharedPreferenceChanged(prefs, "vel_sens");
  }

  @Override
  protected void onPause() {
    super.onPause();
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    prefs.unregisterOnSharedPreferenceChangeListener(this);

  }

  public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
    if (key.equals("keyboard_type")) {
      String keyboardType = prefs.getString(key, "2row");
      keyboard_.setKeyboardSpec(KeyboardSpec.make(keyboardType));
    } else if (key.equals("vel_sens") || key.equals("vel_avg")) {
      float velSens = prefs.getFloat("vel_sens", 0.5f);
      float velAvg = prefs.getFloat("vel_avg", 64);
      keyboard_.setVelocitySensitivity(velSens, velAvg);
    }
  }

  @Override
  protected void onNewIntent(Intent intent) {
    Log.d("synth", "activity onNewIntent " + intent);
    connectUsbFromIntent(intent);
  }

  boolean connectUsbMidi(UsbDevice device) {
    if (synthesizerService_ != null) {
      return synthesizerService_.connectUsbMidi(device);
    }
    usbDevicePending_ = device;
    return true;
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
  boolean connectUsbFromIntent(Intent intent) {
    if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(intent.getAction())) {
      UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
      return connectUsbMidi(device);
    } else {
      return false;
    }
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
  void setupUsbMidi(Intent intent) {
    permissionIntent_ = PendingIntent.getBroadcast(this, 0, new Intent(
            ACTION_USB_PERMISSION), 0);
    IntentFilter filter = new IntentFilter();
    filter.addAction(ACTION_USB_PERMISSION);
    registerReceiver(usbReceiver_, filter);
    connectUsbFromIntent(intent);
  }

  private static final String ACTION_USB_PERMISSION = "com.levien.synthesizer.USB_PERSMISSION";
  BroadcastReceiver usbReceiver_ = new BroadcastReceiver() {
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (ACTION_USB_PERMISSION.equals(action)) {
        synchronized (this) {
          UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
          if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
            connectUsbMidi(device);
          } else {
            Log.d("synth", "permission denied for device " + device);
          }
          permissionRequestPending_ = false;
        }
      }
    }
  };

  public void sendMidiBytes(byte[] buf) {
    if (synthesizerService_ != null) {
      synthesizerService_.sendRawMidi(buf);
    }
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
  protected void onSynthConnected() {
    final MidiListener synthMidi = synthesizerService_.getMidiListener();
    keyboard_.setMidiListener(synthMidi);

    cutoffKnob_.setKnobListener(new KnobListener() {
      public void onKnobChanged(double newValue) {
        int value = (int)Math.round(newValue * 127);
        synthMidi.onController(0, 1, value);
      }
    });
	  
    resonanceKnob_.setKnobListener(new KnobListener() {
      public void onKnobChanged(double newValue) {
        int value = (int)Math.round(newValue * 127);
        synthMidi.onController(0, 2, value);
      }
    });
	  
    overdriveKnob_.setKnobListener(new KnobListener() {
      public void onKnobChanged(double newValue) {
        int value = (int)Math.round(newValue * 127);
        synthMidi.onController(0, 3, value);
      }
    });

    // Connect controller changes to knob views
    synthesizerService_.setMidiListener(new MidiAdapter() {
      public void onNoteOn(final int channel, final int note, final int velocity) {
        runOnUiThread(new Runnable() {
          public void run() {
            keyboard_.onNote(note, velocity);
          }
        });
      }

      public void onNoteOff(final int channel, final int note, final int velocity) {
        runOnUiThread(new Runnable() {
          public void run() {
            keyboard_.onNote(note, 0);
          }
        });
      }

      public void onController(final int channel, final int cc, final int value) {
        runOnUiThread(new Runnable() {
          public void run() {
            if (cc == 1) {
              cutoffKnob_.setValue(value * (1.0 / 127));
            } else if (cc == 2) {
              resonanceKnob_.setValue(value * (1.0 / 127));
            } else if (cc == 3) {
              overdriveKnob_.setValue(value * (1.0 / 127));
            }
          }
        });
      }
    });
    
    //Set adapter for populating channel selection spinner with channel names
    ArrayAdapter<CharSequence> channelAdapter = ArrayAdapter.createFromResource(this,
            R.array.channels_array, android.R.layout.simple_spinner_item);
    // Specify the layout to use when the list of choices appears
    channelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // Apply the adapter to the spinner
    channelSpinner_.setAdapter(channelAdapter);
    
    //Set listener on Channel Spinner
    channelSpinner_.setOnItemSelectedListener(new OnItemSelectedListener() {
      
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //What to do here?
      }
      public void onNothingSelected(AdapterView<?> parent) {
      }
      
    });
    
    
    // Populate patch names (note: we could update an existing list rather than
    // creating a new adapter, but it probably wouldn't save all that much).
    if (presetSpinner_.getAdapter() == null) {
      // Only set it once, which is a workaround that allows the preset
      // selection to persist for onCreate lifetime. Of course, it should
      // be persisted for real, instead.
      List<String> patchNames = synthesizerService_.getPatchNames();
      ArrayAdapter<String> adapter = new ArrayAdapter<String>(
              PianoActivity2.this, android.R.layout.simple_spinner_item, patchNames);
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      presetSpinner_.setAdapter(adapter);
    }

    presetSpinner_.setOnItemSelectedListener(new OnItemSelectedListener() {
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        synthesizerService_.getMidiListener().onProgramChange(getChannelNumber(), position);
      }
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });

    List<String> fileNames=new ArrayList<String>() ;
    
    if (filelist_.getAdapter() == null) {
      // Only set it once, which is a workaround that allows the preset
      // selection to persist for onCreate lifetime. Of course, it should
      // be persisted for real, instead.
      
      String path = Environment.getExternalStorageDirectory().
              getAbsolutePath()+"/here";
      Log.d("Files", "Path: " + path);
      File f = new File(path);     

      if(!f.exists()) {
        Toast.makeText(getApplicationContext(), "No Directory No List", Toast.LENGTH_SHORT).show();
      }
      else  {
        File file[] = f.listFiles();

        if(file.length==0)  {
          Toast.makeText(getApplicationContext(), "There is Directory but No List", Toast.LENGTH_SHORT).show();
        }
      
      fileNames.add("Recordings");
      for(int i=0;i<file.length;i++)
        fileNames.add(file[i].getName());
      ArrayAdapter<String> adapter = new ArrayAdapter<String>(
              PianoActivity2.this, android.R.layout.simple_spinner_item, fileNames) {
                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent)
                {
                    View v = null;
                    // If this is the initial dummy entry, make it hidden
                    if (position == 0) {
                        TextView tv = new TextView(getContext());
                        tv.setHeight(0);
                        tv.setVisibility(View.GONE);
                        v = tv;
                    }
                    else {
                        // Pass convertView as null to prevent reuse of special case views
                        v = super.getDropDownView(position, null, parent);
                    }

                    // Hide scroll bar because it appears sometimes unnecessarily, this does not prevent scrolling 
                    parent.setVerticalScrollBarEnabled(false);
                    return v;
                }
            };
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      filelist_.setAdapter(adapter);
      }
    }
    filelist_.setOnItemSelectedListener(new OnItemSelectedListener() {
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        
        clicked = false;
        resumed = false;
        if(position!=0)
        {String path = Environment.getExternalStorageDirectory().
                  getAbsolutePath()+"/here";
        File f = new File(path);     
        File file[] = f.listFiles();
        fileName = file[position-1].getName();
        fileName = fileName.substring(0, fileName.length()-4);
        notescnt=0;
        }
      }
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });

    // Handle any pending USB device events
    if (usbDevicePending_ != null) {
      synthesizerService_.connectUsbMidi(usbDevicePending_);
      usbDevicePending_ = null;
    } else {
      UsbDevice device = synthesizerService_.usbDeviceNeedsPermission();
      if (device != null) {
        synchronized (usbReceiver_) {
          if (!permissionRequestPending_) {
            permissionRequestPending_ = true;
            UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
            usbManager.requestPermission(device, permissionIntent_);
          }
        }
      }
    }
  }

  protected void onSynthDisconnected() {
    synthesizerService_.setMidiListener(null);
  }

  private KeyboardView keyboard_;
  private KnobView cutoffKnob_;
  private KnobView resonanceKnob_;
  private KnobView overdriveKnob_;
  private Spinner presetSpinner_;
  private Spinner channelSpinner_;
  private Spinner filelist_;
  private TextView tvTimer_;
  private PendingIntent permissionIntent_;
  private boolean permissionRequestPending_;
  private UsbDevice usbDevicePending_;

  private MidiListener synthMidi_;

  private ImageButton recordBtn; // will be StopBtn
  private ImageButton playBtn; // play back

  private FileOutputStream fileout;
  private FileInputStream filein;
  private File file;
  private DataInputStream dis;

  boolean recordBtnIsActive = false;
  boolean wannaRecord = true;
  boolean openBtnWasPressed = false;
  boolean paused=false, resumed=false, clicked=false;
  private Scanner sc;
  private String readline;

  // for time checking
  long startRecording_ms;
  long noteOn_ms;
  long noteOff_ms;
  long startPlayback_ms;

  long time;
  long realeasedTime;
  long pressedTimeSave;
  Handler handler = new Handler();
  private String fileName = "apple"; // default
  ArrayList<MusicNotes> m = new ArrayList<MusicNotes>();
  static int start_play=1;
  static long first_current_time=0;
  static final String tag = "pianoactivity2";
  
  private Timer t;
  private int TimeCounter = 0;
  private String recordTime = "00:00:00";
  private long totalRecordTime = 0;
  
  Runnable stlist[]= new Runnable[100];
  long notescnt;

  public void onClick(View v) {
    // TODO Auto-generated method stub
  }
}
