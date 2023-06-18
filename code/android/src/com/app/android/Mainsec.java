package com.app.android;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetFileDescriptor;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

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

import java.io.IOException;
import java.util.Locale;

public class Mainsec extends Activity {
	Toast toast;
    Handler h = new Handler();
    SharedPreferences sp;
    Editor ed;
    boolean isForeground = true;
    MediaPlayer mp;
    SoundPool sndpool;
    int snd_info;
    int snd_result;
    int snd_move;
    int snd_yes;
    int snd_no;
    int score;
    int screen_width;
    int screen_height;
    int current_section = R.id.main;
    boolean showLeaders, isSigned;
    int card_size;
    AnimatorSet anim;
    int prev_card;
    int current_card;
    final int margin = 20;
    final int time = 30; // time in seconds
    final int num_cards = 5; // number of cards

    // AdMob
    AdView adMobBanner;

    AdRequest adRequest;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gamesecond);

        // fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // preferences
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        ed = sp.edit();

        // AdMob
        adMob();

        // signed
        if (getResources().getBoolean(R.bool.connect_games) && GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN.getScopeArray()))
            onSignIn();

        // bg sound
        mp = new MediaPlayer();
        try {
            AssetFileDescriptor descriptor = getAssets().openFd("snd_bg.mp3");
            mp.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mp.setLooping(true);
            mp.setVolume(0, 0);
            mp.prepare();
            mp.start();
        } catch (Exception e) {
        }

        // if mute
        if (sp.getBoolean("mute", false)) {
            ((Button) findViewById(R.id.btn_sound)).setText(getString(R.string.btn_sound));
        } else {
            mp.setVolume(0.2f, 0.2f);
        }

        // SoundPool
        sndpool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
        try {
            snd_result = sndpool.load(getAssets().openFd("snd_result.mp3"), 1);
            snd_info = sndpool.load(getAssets().openFd("snd_info.mp3"), 1);
            snd_move = sndpool.load(getAssets().openFd("snd_move.mp3"), 1);
            snd_yes = sndpool.load(getAssets().openFd("snd_yes.mp3"), 1);
            snd_no = sndpool.load(getAssets().openFd("snd_no.mp3"), 1);
        } catch (IOException e) {
        }

        // hide navigation bar listener
        findViewById(R.id.all).setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                hide_navigation_bar();
            }
        });

        // custom font
        Typeface font = Typeface.createFromAsset(getAssets(), "CooperBlack.otf");
        ((TextView) findViewById(R.id.txt_result)).setTypeface(font);
        ((TextView) findViewById(R.id.txt_high_result)).setTypeface(font);
        ((TextView) findViewById(R.id.txt_ask)).setTypeface(font);
        ((TextView) findViewById(R.id.txt_yes)).setTypeface(font);
        ((TextView) findViewById(R.id.txt_no)).setTypeface(font);
        ((TextView) findViewById(R.id.mess)).setTypeface(font);

        SCALE();

        // touch listener
        findViewById(R.id.game).setOnTouchListener(new OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.isEnabled() && event.getAction() == MotionEvent.ACTION_DOWN && current_section == R.id.game) {
                    v.setEnabled(false);

                    // check answer
                    if (prev_card == current_card) {
                        if (event.getX() >= screen_width / 2f) {
                            // true
                            score += 10;
                            if (!sp.getBoolean("mute", false) && isForeground)
                                sndpool.play(snd_yes, 1f, 1f, 0, 0, 1);
                        } else {
                            // wrong
                            score = Math.max(score - 10, 0);
                            if (!sp.getBoolean("mute", false) && isForeground)
                                sndpool.play(snd_no, 1f, 1f, 0, 0, 1);
                        }
                    } else {
                        if (event.getX() < screen_width / 2f) {
                            // true
                            score += 10;
                            if (!sp.getBoolean("mute", false) && isForeground)
                                sndpool.play(snd_yes, 1f, 1f, 0, 0, 1);
                        } else {
                            // wrong
                            score = Math.max(score - 10, 0);
                            if (!sp.getBoolean("mute", false) && isForeground)
                                sndpool.play(snd_no, 1f, 1f, 0, 0, 1);
                        }
                    }

                    h.post(hide_card);
                }

                return false;
            }
        });
    }

    // SCALE
    void SCALE() {
        // txt_yes
        ((TextView) findViewById(R.id.txt_yes)).setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(22));

        // txt_no
        ((TextView) findViewById(R.id.txt_no)).setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(22));

        // txt_ask
        ((TextView) findViewById(R.id.txt_ask)).setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(16));

        // buttons text
        ((TextView) findViewById(R.id.btn_sign)).setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(26));
        ((TextView) findViewById(R.id.btn_leaderboard)).setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(26));
        ((TextView) findViewById(R.id.btn_sound)).setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(26));
        ((TextView) findViewById(R.id.btn_start)).setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(50));
        ((TextView) findViewById(R.id.btn_exit)).setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(26));
        ((TextView) findViewById(R.id.btn_home)).setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(26));
        ((TextView) findViewById(R.id.btn_start2)).setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(26));

        // text result
        ((TextView) findViewById(R.id.txt_result)).setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(60));
        ((TextView) findViewById(R.id.txt_high_result)).setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(30));

        // text mess
        ((TextView) findViewById(R.id.mess)).setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(60));
    }

    // START
    void START() {
        score = 0;
        prev_card = -1;
        show_section(R.id.game);
        findViewById(R.id.mess).setVisibility(View.GONE);
        findViewById(R.id.game).setEnabled(false);
        findViewById(R.id.txt_ask).setAlpha(0);
        findViewById(R.id.txt_yes).setAlpha(0);
        findViewById(R.id.txt_no).setAlpha(0);
        findViewById(R.id.progress).setAlpha(0);

        // progress
        ((ProgressBar) findViewById(R.id.progress)).setMax(time);
        ((ProgressBar) findViewById(R.id.progress)).setProgress(time);

        // screen size
        screen_width = Math.max(findViewById(R.id.all).getWidth(), findViewById(R.id.all).getHeight());
        screen_height = Math.min(findViewById(R.id.all).getWidth(), findViewById(R.id.all).getHeight());

        // txt_ask
        ((TextView) findViewById(R.id.txt_ask)).setY(DpToPx(margin));

        // progress
        findViewById(R.id.progress).setX(DpToPx(margin));
        findViewById(R.id.progress).setY(screen_height - (int) DpToPx(10 + margin));
        findViewById(R.id.progress).getLayoutParams().width = (int) (screen_width - DpToPx(margin * 2));
        findViewById(R.id.progress).getLayoutParams().height = (int) DpToPx(10);

        // card position
        card_size = (int) (screen_height - DpToPx(margin * 4) - findViewById(R.id.txt_ask).getHeight() - DpToPx(10));
        findViewById(R.id.card).getLayoutParams().width = findViewById(R.id.card).getLayoutParams().height = card_size;
        findViewById(R.id.card).setX((screen_width - card_size) / 2f);
        findViewById(R.id.card).setY(DpToPx(margin * 2) + findViewById(R.id.txt_ask).getHeight());

        // txt_no & txt_yes
        findViewById(R.id.txt_no).getLayoutParams().width = findViewById(R.id.txt_yes).getLayoutParams().width = (int) ((screen_width - card_size) / 2f);

        // current_card
        current_card = (int) Math.round(Math.random() * num_cards);
        ((ImageView) findViewById(R.id.card)).setImageResource(getResources().getIdentifier("card" + current_card, "drawable",
                getPackageName()));

        h.postDelayed(hide_card, 2000);
    }

    // hide_card
    Runnable hide_card = new Runnable() {
        @Override
        public void run() {
            // animate
            anim = new AnimatorSet();
            anim.playTogether(ObjectAnimator.ofFloat(findViewById(R.id.card), "x", -card_size));
            anim.setDuration(200);
            anim.addListener(new AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    h.post(show_card);
                }
            });
            anim.start();
        }
    };

    // show_card
    Runnable show_card = new Runnable() {
        @Override
        public void run() {
            anim = new AnimatorSet();
            anim.setDuration(200);

            // first card
            if (prev_card == -1) {
                h.postDelayed(TIMER, 1000);
                anim.playTogether(ObjectAnimator.ofFloat(findViewById(R.id.card), "x", (screen_width - card_size) / 2f),
                        ObjectAnimator.ofFloat(findViewById(R.id.txt_ask), "alpha", 1f),
                        ObjectAnimator.ofFloat(findViewById(R.id.txt_yes), "alpha", 1f),
                        ObjectAnimator.ofFloat(findViewById(R.id.txt_no), "alpha", 1f),
                        ObjectAnimator.ofFloat(findViewById(R.id.progress), "alpha", 1f));
            } else {
                anim.playTogether(ObjectAnimator.ofFloat(findViewById(R.id.card), "x", (screen_width - card_size) / 2f));
            }

            prev_card = current_card;

            // current_card
            if (Math.random() > 0.5)
                current_card = (int) Math.round(Math.random() * num_cards);
            ((ImageView) findViewById(R.id.card)).setImageResource(getResources().getIdentifier("card" + current_card,
                    "drawable", getPackageName()));
            findViewById(R.id.card).setX(screen_width);

            // animate
            anim.addListener(new AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    // sound
                    if (!sp.getBoolean("mute", false) && isForeground)
                        sndpool.play(snd_move, 0.8f, 0.8f, 0, 0, 1);
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    findViewById(R.id.game).setEnabled(true);
                }
            });
            anim.start();
        }
    };

    // TIMER
    Runnable TIMER = new Runnable() {
        @Override
        public void run() {
            ((ProgressBar) findViewById(R.id.progress))
                    .setProgress(((ProgressBar) findViewById(R.id.progress)).getProgress() - 1);

            // time is up
            if (((ProgressBar) findViewById(R.id.progress)).getProgress() == 0) {
                findViewById(R.id.game).setEnabled(false);
                findViewById(R.id.mess).setVisibility(View.VISIBLE);

                // animation
                if (anim != null) {
                    anim.removeAllListeners();
                    anim.cancel();
                }

                // sound
                if (!sp.getBoolean("mute", false) && isForeground)
                    sndpool.play(snd_info, 1f, 1f, 0, 0, 1);

                h.postDelayed(STOP, 3000);
                return;
            }

            h.postDelayed(TIMER, 1000);
        }
    };

    // STOP
    Runnable STOP = new Runnable() {
        @Override
        public void run() {
            // show result
            show_section(R.id.result);

            saveScore(score);

            // show score
            ((TextView) findViewById(R.id.txt_result)).setText(getString(R.string.score) + " " + score);
            ((TextView) findViewById(R.id.txt_high_result)).setText(getString(R.string.high_score) + " " + sp.getInt("scoresec", 0));

            // sound
            if (!sp.getBoolean("mute", false) && isForeground)
                sndpool.play(snd_result, 0.5f, 0.5f, 0, 0, 1);// AdMob Interstitial
            // load
        }
    };

    // onClick
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                START();
                break;
            case R.id.btn_start2:
                Intent in=new Intent(Mainsec.this, Mainthird.class);
                startActivity(in);
                finish();
                break;
            case R.id.btn_home:
                show_section(R.id.main);
                break;
            case R.id.btn_exit:
                finish();
                break;
            case R.id.btn_sound:
                if (sp.getBoolean("mute", false)) {
                    ed.putBoolean("mute", false);
                    mp.setVolume(0.2f, 0.2f);
                    ((Button) findViewById(R.id.btn_sound)).setText(getString(R.string.btn_mute));
                } else {
                    ed.putBoolean("mute", true);
                    mp.setVolume(0, 0);
                    ((Button) findViewById(R.id.btn_sound)).setText(getString(R.string.btn_sound));
                }
                ed.commit();
                break;
            case R.id.btn_leaderboard:
                showLeaders();
                break;
            case R.id.btn_sign:
                // sign in/out
                if (isSigned)
                    signOut();
                else
                    signIn();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        return;
      /*  switch (current_section) {
            case R.id.main:
                super.onBackPressed();
                break;
            case R.id.result:
                show_section(R.id.main);
                break;
            case R.id.game:
                show_section(R.id.main);
                h.removeCallbacks(STOP);
                h.removeCallbacks(TIMER);
                h.removeCallbacks(show_card);
                h.removeCallbacks(hide_card);

                // animation
                if (anim != null) {
                    anim.removeAllListeners();
                    anim.cancel();
                }
                break;
        }

       */
    }

    // show_section
    void show_section(int section) {
        current_section = section;
        findViewById(R.id.main).setVisibility(View.GONE);
        findViewById(R.id.game).setVisibility(View.GONE);
        findViewById(R.id.result).setVisibility(View.GONE);
        findViewById(current_section).setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        h.removeCallbacks(STOP);
        h.removeCallbacks(TIMER);
        h.removeCallbacks(show_card);
        h.removeCallbacks(hide_card);
        mp.release();
        sndpool.release();

        // animation
        if (anim != null) {
            anim.removeAllListeners();
            anim.cancel();
        }

        // destroy AdMob
        if (adMobBanner != null) {
            adMobBanner.setAdListener(null);
            adMobBanner.destroy();
            adMobBanner = null;
        }

		adRequest = null;

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        isForeground = false;
        mp.setVolume(0, 0);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isForeground = true;

        if (!sp.getBoolean("mute", false) && isForeground)
            mp.setVolume(0.2f, 0.2f);
    }

    // DpToPx
    float DpToPx(float dp) {
        return (dp * Math.max(getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels) / 540f);
    }

    // hide_navigation_bar
    void hide_navigation_bar() {
        // fullscreen mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus)
            hide_navigation_bar();
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

    // TOAST
    void TOAST(String mess) {
        if (toast != null)
            toast.cancel();
        toast = Toast.makeText(this, mess, Toast.LENGTH_SHORT);
        ((TextView) toast.getView().findViewById(android.R.id.message)).setGravity(Gravity.CENTER);
        toast.show();
    }

    // signIn
    void signIn() {
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

    // signOut
    void signOut() {
        if (getResources().getBoolean(R.bool.connect_games)) {
            GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    onSignOut();
                }
            });
        }
    }

    // onSignIn
    void onSignIn() {
        isSigned = true;
        ((Button) findViewById(R.id.btn_sign)).setText(getString(R.string.btn_sign_out));

        if (sp.contains("score")) // save score to leaderboard
            saveScore(sp.getInt("score",0));

        // show leaders
        if (showLeaders) {
            showLeaders = false;
            showLeaders();
        }

        // get score from leaderboard
        Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this)).loadCurrentPlayerLeaderboardScore(getString(R.string.leaderboard), LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC).addOnSuccessListener(this, new OnSuccessListener<AnnotatedData<LeaderboardScore>>() {
            @Override
            public void onSuccess(final AnnotatedData<LeaderboardScore> leaderboardScoreAnnotatedData) {
                if (leaderboardScoreAnnotatedData != null && leaderboardScoreAnnotatedData.get() != null && (int) leaderboardScoreAnnotatedData.get().getRawScore() > sp.getInt("score", 0))
                    sp.edit().putInt("score", (int) leaderboardScoreAnnotatedData.get().getRawScore()).apply(); // save score local
            }
        });
    }

    // onSignOut
    void onSignOut() {
        isSigned = false;
        showLeaders = false;
        ((Button) findViewById(R.id.btn_sign)).setText(getString(R.string.btn_sign_in));
    }

    // saveScore
    void saveScore(int score) {
        // save score local
       // if (score > sp.getInt("scoresec", 0))
            sp.edit().putInt("scoresec", score).commit();
        System.out.println("Saving the score which isssss:"+score);

        // save score in leaderboard
        if (getResources().getBoolean(R.bool.connect_games) && isSigned)
            Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this)).submitScore(getString(R.string.leaderboard), score);
    }

    // showLeaders
    void showLeaders() {
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

    // adMob
    void adMob() {
        if (getResources().getBoolean(R.bool.show_admob)) {

        }
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


}