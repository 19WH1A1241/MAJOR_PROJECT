package com.app.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;


import java.util.concurrent.TimeUnit;


public class OtpSendActivity extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    EditText etd;
    Button b,skip;
    ProgressBar p;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_otp_send);
        mAuth = FirebaseAuth.getInstance();
       // talertg();
       etd=(EditText)findViewById(R.id.etPhone);
       b=(Button) findViewById(R.id.btnSend);

       p=(ProgressBar) findViewById(R.id.progressBar);
       b.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               if (etd.getText().toString().trim().isEmpty()) {
                   Toast.makeText(OtpSendActivity.this, "Invalid Phone Number", Toast.LENGTH_SHORT).show();
               } else if (etd.getText().toString().trim().length() != 10) {
                   Toast.makeText(OtpSendActivity.this, "Type valid Phone Number", Toast.LENGTH_SHORT).show();
               } else {
                   otpSend();
               }
           }
       });







    }

    private void otpSend() {
       p.setVisibility(View.VISIBLE);
        b.setVisibility(View.INVISIBLE);

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
               p.setVisibility(View.GONE);
               b.setVisibility(View.VISIBLE);
                Toast.makeText(OtpSendActivity.this, "Try to verify again", Toast.LENGTH_SHORT).show();
               // Log.d("msg verification..........",e.getLocalizedMessage());
               /* Intent inten = new Intent(OtpSendActivity.this, Login_A.class);
                startActivity(inten);
                finish();

                */
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                p.setVisibility(View.GONE);
               b.setVisibility(View.VISIBLE);
                Toast.makeText(OtpSendActivity.this, "OTP is successfully send.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(OtpSendActivity.this, OtpVerifyActivity.class);
                intent.putExtra("phone", etd.getText().toString().trim());
                intent.putExtra("verificationId", verificationId);
                startActivity(intent);
            }
        };

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+91" + etd.getText().toString().trim())
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

  /*  public void talertg()
    {



        TextView textView = new TextView(OtpSendActivity.this);
        textView.setText("You must accept the Terms and condition to use the app");
        textView.setPadding(20, 30, 20, 30);
        textView.setTextSize(20F);
        textView.setBackgroundColor(Color.BLACK);
        textView.setTextColor(Color.WHITE);

        AlertDialog.Builder builders= new AlertDialog.Builder(OtpSendActivity.this);
        builders.setCustomTitle(textView);

        final View customelayout=getLayoutInflater().inflate(R.layout.customalert,null);
        builders.setView(customelayout);
        TextView mw=(TextView) customelayout.findViewById(R.id.webvs);
        // mw.loadUrl("https://gujaratidayro.in/privacy.html");
        mw.setText(Constants.termsandc);
        mw.setMovementMethod(new ScrollingMovementMethod());
        builders.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(OtpSendActivity.this, "You accepted the terms and conditions", Toast.LENGTH_SHORT).show();

                // signInWithGmail();

                dialog.cancel();
            }
        });
        builders.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(OtpSendActivity.this, "You must accept the Terms and conditions before Using the app", Toast.LENGTH_SHORT).show();
                System.exit(0);
                dialog.cancel();
            }
        });
        AlertDialog dialogg
                = builders.create();
        dialogg.setCancelable(false);
        dialogg.show();
    }

   */
}