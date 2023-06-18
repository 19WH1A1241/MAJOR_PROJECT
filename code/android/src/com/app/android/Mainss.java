package com.app.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Mainss extends Activity {
    SharedPreferences sp;
    int scsec,scth,sc,total;
    double percentg,tot;
    ProgressBar prog;
    TextView loadtext,txtstgs,txtstg;
    Button hom,ext;
    String stag;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.finalscreen);

        prog=(ProgressBar)findViewById(R.id.dPBLoading);
        loadtext=(TextView)findViewById(R.id.txtv);

        txtstgs=(TextView)findViewById(R.id.txt_stgs);
        txtstg=(TextView)findViewById(R.id.txt_stg);

        hom=(Button)findViewById(R.id.homs);
        ext=(Button)findViewById(R.id.exits);




        SharedPreferences settings= getSharedPreferences("pref", Activity.MODE_PRIVATE);
        sc= settings.getInt("scor",0);
        System.out.println("It is first Scoreeeeeeeeeeee----"+sc);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        scsec= sp.getInt("scoresec",0);
        scth=sp.getInt("scoreth",0);
        System.out.println("It is second Scoreeeeeeeeeeee----"+scsec);
        System.out.println("It is third Scoreeeeeeeeeeee----"+scth);
        total=scsec+scth+sc;
        tot=total;
        percentg= tot/10.0;


        Log.d("Percentage isss", String.valueOf(percentg));
        if(percentg<35)
        {
            //severe
            stag="Severe";
            Log.d("Condition", "severe");
            sp.edit().putString("stage","Severe").commit();

        }
        else if(percentg>35 && percentg<65)
        {

            stag="Moderate";
            Log.d("Condition", "moderate");
            sp.edit().putString("stage","Moderate").commit();
            //moderate
        }
        else if(percentg>65 && percentg<80)
        {
            stag="Mild";
            Log.d("Condition", "Mild");
            sp.edit().putString("stage","Mild").commit();
            //Mild
        }
        else
        {
            stag="No Alzheimers";
            Log.d("Condition", "No alzheimers");
            sp.edit().putString("stage","No alzheimers").commit();

            // No alzheimers
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // on below line we are
                // creating a new intent
                prog.setVisibility(View.GONE);
                loadtext.setVisibility(View.GONE);

                txtstgs.setVisibility(View.VISIBLE);
                txtstg.setText(stag);
                txtstg.setVisibility(View.VISIBLE);
                hom.setVisibility(View.VISIBLE);
                ext.setVisibility(View.VISIBLE);


            }
        }, 3000);

         hom.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent i = new Intent(Mainss.this, Firstscreen.class);

                 // on below line we are
                 // starting a new activity.
                 startActivity(i);
                 finish();
             }
         });


         ext.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 System.exit(0);
             }
         });
    }

    @Override
    public void onBackPressed() {
        System.exit(0);
    }
}
