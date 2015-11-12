/*
 * Copyright 2010 Google Inc.
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

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.levien.synthesizer.R;
/**
 * An Activity for editing a track.
 */
public class DesignTestActivity extends SynthActivity {
  final Context context = this;
  final String tag = "Add Track";
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.test);
    
    final FloatingActionButton actionA = (FloatingActionButton) findViewById(R.id.action_a);
    final FloatingActionsMenu floatMenu = (FloatingActionsMenu) findViewById(R.id.multiple_actions);
    actionA.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        floatMenu.collapse();
        final Dialog addDialog = new Dialog(context);
        addDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        addDialog.setContentView(R.layout.add_track_dialog);
        addDialog.show();
        
        Button cancelButton = (Button)addDialog.findViewById(R.id.cancelButton);
        Button saveButton = (Button)addDialog.findViewById(R.id.saveButton);
        
        cancelButton.setOnClickListener(new View.OnClickListener() {          
        public void onClick(View v) {
            addDialog.dismiss();
        }
      });
        
      saveButton.setOnClickListener(new View.OnClickListener() {   
        public void onClick(View v) {
          String trackname = ((EditText)addDialog.findViewById(R.id.trackName)).getText().toString();                 
          String channel=((Spinner) addDialog.findViewById(R.id.channelSelector)).getSelectedItem().toString();
          String instrument=((Spinner) addDialog.findViewById(R.id.presetSpinner)).getSelectedItem().toString();
          String toggleval = ((ToggleButton) addDialog.findViewById(R.id.on_off)).isChecked() ? "on" : "off";
          
          TableLayout tl = (TableLayout) findViewById(R.id.track_editor_table);
          /* Create a new row to be added. */
          TableRow tr = new TableRow(context);
          tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
          
          TextView tracknameview = new TextView(context);
          tracknameview.setText(trackname);
          tracknameview.setTextColor(Color.WHITE);
          
          TextView channelnameview = new TextView(context);
          channelnameview.setText(channel);
          channelnameview.setTextColor(Color.WHITE);
          
          TextView instrumentnameview = new TextView(context);
          instrumentnameview.setText(instrument);
          instrumentnameview.setTextColor(Color.WHITE);
          
          TextView onoffview = new TextView(context);
          onoffview.setText(toggleval);
          onoffview.setTextColor(Color.WHITE);
          
          tr.addView(tracknameview);
          tr.addView(channelnameview);
          tr.addView(instrumentnameview);
          tr.addView(onoffview);
          
          tr.setClickable(true);
          tr.setOnClickListener(
                  new View.OnClickListener() {
                    public void onClick(View v) {
                        String currenttext = ((TextView)v).getText().toString();
                        Snackbar.make(v, currenttext, Snackbar.LENGTH_SHORT).show();
                    }
                });
          
          tl.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
          addDialog.dismiss();
          
        }
      });
      }
    });
    /*
    FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
          Snackbar.make(v, "FAB Clicked", Snackbar.LENGTH_SHORT).show();
      }
  });
  */
  }
}

