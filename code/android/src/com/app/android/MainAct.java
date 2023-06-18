package com.app.android;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainAct extends AppCompatActivity {

    FirebaseUser firebaseUser;
    FirebaseAuth firebaseAuth;

   // Button b;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.mainact);
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser= firebaseAuth.getCurrentUser();

        firebaseAuth = FirebaseAuth.getInstance();




        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // on below line we are
                // creating a new intent

                if(firebaseUser != null)
                {
                    Intent i = new Intent(MainAct.this, Firstscreen.class);

                    // on below line we are
                    // starting a new activity.
                    startActivity(i);

                    // on the below line we are finishing
                    // our current activity.
                    finish();
                }
                else {
                    Intent i = new Intent(MainAct.this, OtpSendActivity.class);

                    // on below line we are
                    // starting a new activity.
                    startActivity(i);

                    // on the below line we are finishing
                    // our current activity.
                    finish();
                }

            }
        }, 2000);

      /*  b=(Button) findViewById(R.id.btnn);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in=new Intent(MainAct.this, AndroidLauncher.class);
                startActivity(in);
                finish();
            }
        });

       */
    }
}
