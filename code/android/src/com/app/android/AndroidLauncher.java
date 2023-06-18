package com.app.android;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.app.InterfaceListener;
import com.app.Main;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.games.AnnotatedData;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// android launcher
public class AndroidLauncher extends AndroidApplication implements InterfaceListener {
    Main app;
    int score = 0;
    int i=0;
    boolean showLeaders, isSigned;
    Toast toast;
   Preferences pref;
    String v;
    // AdMob
    AdView adMobBanner;

    InterstitialAd adMobInterstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


     //   pref = Gdx.app.getPreferences("preferences");
      /*  if (pref.contains("score")) {
            i = pref.getInteger("score");
            v = String.valueOf(i);
            Log.d("The result isssssssss", v);
        }

       */
        // run
        app = new Main(this);
        runOnUiThread(() -> {
            AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
            config.useImmersiveMode = true;
            ((ViewGroup) findViewById(R.id.app)).addView(initializeForView(app, config));
        });

        pref = Gdx.app.getPreferences("preferences");
        i = pref.getInteger("scoresec");
        if (score > 0)
            saveScore(score);
        v = String.valueOf(score);
        Log.d("The result isssssssss", v);
        // signed
        if (getResources().getBoolean(R.bool.connect_games) && GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN.getScopeArray()))
            onSignIn();

        // AdMob
        adMob();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100)
            if (resultCode == RESULT_OK) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                if (result.isSuccess()) // sign ok
                    onSignIn();
                else { // sign fail
                    String message = result.getStatus().getStatusMessage();
                    if (message == null || message.isEmpty())
                        message = getString(R.string.error_sign_in);
                    TOAST(message);
                    onSignOut();
                }
            } else {
                TOAST(getString(R.string.error_sign_in));
                onSignOut();
            }
    }

    @Override
    protected void onDestroy() {
        // destroy AdMob
        if (adMobBanner != null) {
            adMobBanner.setAdListener(null);
            adMobBanner.destroy();
            adMobBanner = null;
        }

        super.onDestroy();
    }

    // onSignIn
    void onSignIn() {
        isSigned = true;

        // set signed
        Gdx.app.postRunnable(() -> app.setSigned(true));

        // save score to leaderboard
        if (score > 0)
            saveScore(score);
        v = String.valueOf(score);
        Log.d("The result isssssssss", v);

        // show leaders
        if (showLeaders) {
            showLeaders = false;
            showLeaders();
        }

        // get score from leaderboard
        Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this)).loadCurrentPlayerLeaderboardScore(getString(R.string.leaderboard), LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC).addOnSuccessListener(this, new OnSuccessListener<AnnotatedData<LeaderboardScore>>() {
            @Override
            public void onSuccess(final AnnotatedData<LeaderboardScore> leaderboardScoreAnnotatedData) {
                if (leaderboardScoreAnnotatedData != null && leaderboardScoreAnnotatedData.get() != null) {
                    Gdx.app.postRunnable(() -> {
                        app.saveScore((int) leaderboardScoreAnnotatedData.get().getRawScore()); // save score local
                    });
                }
            }
        });
    }

    // onSignOut
    void onSignOut() {
        isSigned = false;
        showLeaders = false;

        // set signed
        Gdx.app.postRunnable(() -> app.setSigned(false));
    }

    // TOAST
    void TOAST(String mess) {
        if (toast != null)
            toast.cancel();
        toast = Toast.makeText(this, mess, Toast.LENGTH_SHORT);
      //  ((TextView) toast.getView().findViewById(android.R.id.message)).setGravity(Gravity.CENTER);
        toast.show();
    }

    // log
    void log(Object obj) {
        Log.d("@", String.valueOf(obj));
    }

    @Override
    public void saveScore(int score) {
        // called when game score has been changed
        this.score = score;
        SharedPreferences settings= getSharedPreferences("pref", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor=settings.edit();
        editor.putInt("scor",score);
        editor.commit();

        int sc= settings.getInt("scor",0);
        System.out.println("Shared Prefernce score issss----"+sc);

        if (getResources().getBoolean(R.bool.connect_games) && isSigned)
            Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this)).submitScore(getString(R.string.leaderboard), score);

    }

    @Override
    public void signIn() {
        // called when pressed "Sign In" to Google Play Game Services
        if (getResources().getBoolean(R.bool.connect_games)) {
            final GoogleSignInClient signInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
            signInClient.silentSignIn().addOnCompleteListener(this, new OnCompleteListener<GoogleSignInAccount>() {
                @Override
                public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                    if (task.isSuccessful()) // silent sign ok
                        onSignIn();
                    else // silent sign fail
                        startActivityForResult(signInClient.getSignInIntent(), 100);
                }
            });
        }
    }

    @Override
    public void signOut() {
        // called when pressed "Sign Out" from Google Play Game Services
        if (getResources().getBoolean(R.bool.connect_games)) {
            GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    onSignOut();
                }
            });
        }
    }

    @Override
    public void rate() {
        // called if need to rate the App
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
    }

    @Override
    public void showLeaders() {
        // called when pressed "Leaders"
        if (getResources().getBoolean(R.bool.connect_games)) {
            if (isSigned)
                Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this)).getLeaderboardIntent(getString(R.string.leaderboard)).addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        try {
                            startActivityForResult(intent, 200);
                        } catch (Exception e) {
                            TOAST(getString(R.string.error_games_exists));
                        }
                    }
                });
            else {
                showLeaders = true;
                signIn();
            }
        }
    }

    @Override
    public void admobInterstitial() {
        // called to show AdMob Interstitial
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (adMobInterstitial != null) // show
                    adMobInterstitial.show(AndroidLauncher.this);
                else // load
                    InterstitialAd.load(AndroidLauncher.this, getString(R.string.adMob_interstitial), new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            adMobInterstitial = interstitialAd;
                            adMobInterstitial.setFullScreenContentCallback(new FullScreenContentCallback() {
                                @Override
                                public void onAdFailedToShowFullScreenContent(AdError adError) {
                                    adMobInterstitial = null;
                                    admobInterstitial();
                                }

                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    adMobInterstitial = null;
                                    admobInterstitial();
                                }
                            });
                        }
                    });
            }
        });
    }

    @Override
    public void move() {
      /* runOnUiThread(new Runnable() {
           @Override
           public void run() {
               Log.d("Msg", "in threaddddd");
           }
       });
        Log.d("Msg", "Not in intenttttttttttttt");

       */

        Intent in=new Intent(AndroidLauncher.this, Mainsec.class);
        startActivity(in);
        finish();
    }

    // adMob
    void adMob() {
        if (getResources().getBoolean(R.bool.show_admob))
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // init
                    MobileAds.initialize(AndroidLauncher.this, initializationStatus -> {
                    });

                    // add test device
                    if (getResources().getBoolean(R.bool.admob_test)) {
                        List<String> testDevices = new ArrayList<>();
                        testDevices.add(AdRequest.DEVICE_ID_EMULATOR);
                        testDevices.add(MD5(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID)));
                        MobileAds.setRequestConfiguration(new RequestConfiguration.Builder().setTestDeviceIds(testDevices).build());
                    }

                    // adMob banner
                    adMobBanner = new AdView(AndroidLauncher.this);
                    adMobBanner.setAdUnitId(getString(R.string.adMob_banner));
                    DisplayMetrics outMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
                    adMobBanner.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(AndroidLauncher.this, (int) (outMetrics.widthPixels / outMetrics.density)));
                    ((ViewGroup) findViewById(R.id.admob)).addView(adMobBanner);
                    adMobBanner.setAdListener(new AdListener() {
                        @Override
                        public void onAdLoaded() {
                            super.onAdLoaded();
                            runOnUiThread(() -> findViewById(R.id.admob).setVisibility(View.VISIBLE));
                        }

                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            runOnUiThread(() -> findViewById(R.id.admob).setVisibility(View.GONE));
                        }
                    });
                    adMobBanner.loadAd(new AdRequest.Builder().build());

                    // adMob interstitial
                    admobInterstitial();
                }
            });
    }

    // MD5
    String MD5(String str) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(str.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i)
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            return sb.toString().toUpperCase(Locale.ENGLISH);
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }

    void mov()
    {
        Intent in=new Intent(AndroidLauncher.this, AndroidLauncher.class);
        startActivity(in);
    }

    @Override
    public void onBackPressed() {
        return;
    }
}