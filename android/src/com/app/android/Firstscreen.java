package com.app.android;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Firstscreen extends Activity {
    SharedPreferences sp;
Button start,dem;
TextView reslt;
String rl;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firstscreen);
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        start=(Button) findViewById(R.id.prevdata);
        dem=(Button) findViewById(R.id.demo);
        reslt=(TextView) findViewById(R.id.textstages);
        rl=sp.getString("stage","Not Tested Yet");

        reslt.setText(rl);


        dem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Firstscreen.this, Videovw.class);

                // on below line we are
                // starting a new activity.
                startActivity(i);
                finish();
            }
        });
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Firstscreen.this, AndroidLauncher.class);

                // on below line we are
                // starting a new activity.
                startActivity(i);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        System.exit(0);
    }
}
