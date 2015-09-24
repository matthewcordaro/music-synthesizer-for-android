package com.levien.synthesizer.android.ui;

import com.levien.synthesizer.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.test.PerformanceTestCase;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
 
 
public class IntroActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro);
        
        LinearLayout l = (LinearLayout)findViewById(R.id.MainLayout);
        l.setOnTouchListener(new OnTouchListener(){
          public boolean onTouch(View v, MotionEvent event) {
            // TODO Auto-generated method stub
            Intent intent = new Intent(IntroActivity.this, IntroMenuActivity.class);
            startActivity(intent);
            return false;
          }
          
        });
        
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(IntroActivity.this, IntroMenuActivity.class);
                startActivity(intent);
                // 뒤로가기 했을경우 안나오도록 없애주기 >> finish!!
                finish();
            }
        }, 3000);  
    }
}