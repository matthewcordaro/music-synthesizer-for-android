package com.levien.synthesizer.android.ui;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.levien.synthesizer.R;

/**
 * just intro menu
 * you can go to any activity you choose.
 * 
 * @author koreanguest
 *
 */
public class IntroMenuActivity extends Activity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.intro_menu);

    btn_goComposing = (Button)findViewById(R.id.btn_compose);
    btn_goPlaying = (Button)findViewById(R.id.btn_play);
    btn_goStorage = (Button)findViewById(R.id.btn_directory);

    btn_goComposing.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        // TODO Auto-generated method stub
        Intent intent = new Intent(IntroMenuActivity.this, ScoreActivity.class);
        startActivity(intent);
      }
    });

    btn_goPlaying.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        // TODO Auto-generated method stub
        Intent intent = new Intent(IntroMenuActivity.this, PianoActivity2.class);
        startActivity(intent);
      }
    });

    btn_goStorage.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        // TODO Auto-generated method stub
        /** 
         * you should add the function 
         * to show storage for midi file user recored.
         */
        //          Intent intent = new Intent(IntroMenuActivity.this, ScoreActivity.class);
        //          startActivity(intent);
        //          Toast.makeText(getApplicationContext(), "We are preparing", Toast.LENGTH_LONG).show();

        listdialog();

      }
    });


  }

  public void listdialog()  {
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
      else  {
        String message = file[0].getName();

        Log.d("Files", "Size: "+ file.length);
        for (int i=1; i < file.length; i++)
        {
          Log.d("Files", "FileName:" + file[i].getName());
          message = message + "\n"+ file[i].getName() ;
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("File List")
        .setMessage(message)
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {

          public void onClick(DialogInterface dialog, int which) {
            // TODO Auto-generated method stub

          }
        })
        .show();
      }
    }
  }

  private Button btn_goComposing;
  private Button btn_goPlaying;
  private Button btn_goStorage;
}
