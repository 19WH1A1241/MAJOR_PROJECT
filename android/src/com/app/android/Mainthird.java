package com.app.android;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.app.Activity;
import android.view.View;
import android.widget.Toast;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdRequest.Builder;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import com.google.android.gms.ads.MobileAds;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Mainthird extends Activity {
    Toast toast;
    final float MARGIN = 3f; // margin between items
    final float NUM_ITEMS = 6; // number of items on side
    final long ANIM_SPEED = 100; // animation speed
    final Handler h = new Handler();
    MediaPlayer mp;
    int score;
    int num_opened;
    SharedPreferences sp;
    boolean isForeground = true;
    SoundPool sndpool;
    int current_section;
    int snd_ok;
    int snd_info;
    int snd_move;
    boolean showLeaders, isSigned;
    int item_size;
    AnimatorSet anim;
    Bitmap bitmap_place;
    List<Bitmap> bitmap_items;
    List<View> current_items = new ArrayList<View>();

    // AdMob
    AdView adMobBanner;

    AdRequest adRequest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainthird);

        // fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // preferences
        sp = PreferenceManager.getDefaultSharedPreferences(this);

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
        if (sp.getBoolean("mute", false))
            ((Button) findViewById(R.id.btn_sound)).setText(getString(R.string.btn_sound));

        // SoundPool
        sndpool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        try {
            snd_ok = sndpool.load(getAssets().openFd("snd_ok.mp3"), 1);
            snd_info = sndpool.load(getAssets().openFd("snd_info.mp3"), 1);
            snd_move = sndpool.load(getAssets().openFd("snd_move.mp3"), 1);
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
        ((TextView) findViewById(R.id.mess)).setTypeface(font);

        font = Typeface.createFromAsset(getAssets(), "BerlinSans.ttf");
        ((TextView) findViewById(R.id.txt_score)).setTypeface(font);
        ((Button) findViewById(R.id.btn_sign)).setTypeface(font);
        ((Button) findViewById(R.id.btn_leaderboard)).setTypeface(font);
        ((Button) findViewById(R.id.btn_sound)).setTypeface(font);
        ((Button) findViewById(R.id.btn_start)).setTypeface(font);
        ((Button) findViewById(R.id.btn_exit)).setTypeface(font);
        ((Button) findViewById(R.id.btn_home)).setTypeface(font);
        ((Button) findViewById(R.id.btn_restart)).setTypeface(font);

        // text fields
        ((TextView) findViewById(R.id.txt_score)).setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(34));
        ((TextView) findViewById(R.id.mess)).setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(26));
        ((TextView) findViewById(R.id.txt_result)).setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(36));
        ((TextView) findViewById(R.id.txt_high_result)).setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(18));

        // buttons
        ((Button) findViewById(R.id.btn_sign)).setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(34));
        ((Button) findViewById(R.id.btn_leaderboard)).setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(34));
        ((Button) findViewById(R.id.btn_sound)).setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(34));
        ((Button) findViewById(R.id.btn_start)).setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(34));
        ((Button) findViewById(R.id.btn_exit)).setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(34));
        ((Button) findViewById(R.id.btn_home)).setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(34));
        ((Button) findViewById(R.id.btn_restart)).setTextSize(TypedValue.COMPLEX_UNIT_PX, DpToPx(34));

        // show section
        show_section(R.id.main);
      // START.run();
    }

    // START
    Runnable START = new Runnable() {
        @Override
        public void run() {
            num_opened = 0;
            score = 0;
            current_items.clear();
            findViewById(R.id.items).setEnabled(false);
            findViewById(R.id.mess).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.txt_score)).setText(getString(R.string.score) + score);
            show_section(R.id.game);

            // remove items
            ((ViewGroup) findViewById(R.id.items)).removeAllViews();

            // first run
            if (bitmap_items == null) {
                // item_size
                item_size = (int) ((findViewById(R.id.all).getWidth() - (NUM_ITEMS + 1) * DpToPx(MARGIN)) / NUM_ITEMS);

                // items container size
                findViewById(R.id.items).getLayoutParams().width = findViewById(R.id.items).getLayoutParams().height = (int) Math.ceil(item_size * NUM_ITEMS + (NUM_ITEMS - 1) * DpToPx(MARGIN));

                bitmap_items = new ArrayList<Bitmap>();

                // bitmap_place
                bitmap_place = bitmapFromAssets("place.png", item_size, item_size);

                // bitmap_items
                for (int i = 0; i < NUM_ITEMS * NUM_ITEMS / 2; i++)
                    bitmap_items.add(bitmapFromAssets("item" + i + ".png", item_size, item_size));
                bitmap_items.addAll(bitmap_items);
            }

            // shuffle items
            Collections.shuffle(bitmap_items);

            // add items
            int x_pos = 0;
            int y_pos = 0;
            List<Animator> anim_list = new ArrayList<Animator>();
            for (int i = 0; i < NUM_ITEMS * NUM_ITEMS; i++) {
                ImageView item = new ImageView(Mainthird.this);
                ((ViewGroup) findViewById(R.id.items)).addView(item);
                item.setClickable(true);
                item.setImageBitmap(bitmap_place);
                item.getLayoutParams().width = item.getLayoutParams().height = item_size;
                item.setX(x_pos * item_size + x_pos * DpToPx(MARGIN));
                item.setY(y_pos * item_size + y_pos * DpToPx(MARGIN));
                item.setAlpha(0f);
                item.setTag(i);
                anim_list.add(ObjectAnimator.ofFloat(item, "alpha", 1f)); // animation

                // next item position
                x_pos++;
                if (x_pos == NUM_ITEMS) {
                    x_pos = 0;
                    y_pos++;
                }

                // touch listener
                item.setOnTouchListener(new OnTouchListener() {
                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        // item touch
                        if (findViewById(R.id.items).isEnabled() && (current_items.size() == 0 || current_items.indexOf(v) == -1) && event.getAction() == MotionEvent.ACTION_DOWN) {
                            // score
                            score++;
                            ((TextView) findViewById(R.id.txt_score)).setText(getString(R.string.score) + score);

                            current_items.add(v);
                            findViewById(R.id.items).setEnabled(false); // disable items
                            hide_place();
                        }

                        return false;
                    }
                });
            }

            // animate items
            anim = new AnimatorSet();
            anim.playSequentially(anim_list);
            anim.setDuration(50);
            anim.addListener(new AnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    findViewById(R.id.items).setEnabled(true); // enable items
                }

                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }
            });
            anim.start();
        }
    };

    // hide_place
    void hide_place() {
        // animate
        anim.removeAllListeners();
        anim = null;
        anim = new AnimatorSet();
        anim.playTogether(ObjectAnimator.ofFloat(current_items.get(current_items.size() - 1), "scaleX", 0.5f), ObjectAnimator.ofFloat(current_items.get(current_items.size() - 1), "scaleY", 0.5f), ObjectAnimator.ofFloat(current_items.get(current_items.size() - 1), "alpha", 0f));
        anim.setDuration(ANIM_SPEED);
        anim.addListener(new AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                show_item();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                // sound touch
                if (!sp.getBoolean("mute", false) && isForeground)
                    sndpool.play(snd_move, 0.8f, 0.8f, 0, 0, 1);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });
        anim.start();
    }

    // show_item
    void show_item() {
        // update item
        ((ImageView) current_items.get(current_items.size() - 1)).setImageBitmap(bitmap_items.get((int) current_items.get(current_items.size() - 1).getTag()));

        // animate
        anim.removeAllListeners();
        anim = null;
        anim = new AnimatorSet();
        anim.playTogether(ObjectAnimator.ofFloat(current_items.get(current_items.size() - 1), "scaleX", 1f), ObjectAnimator.ofFloat(current_items.get(current_items.size() - 1), "scaleY", 1f), ObjectAnimator.ofFloat(current_items.get(current_items.size() - 1), "alpha", 1f));
        anim.setDuration(ANIM_SPEED);
        anim.addListener(new AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                compare_items();
            }

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });
        anim.start();
    }

    // hide_first_item
    void hide_first_item() {
        // animate
        anim.removeAllListeners();
        anim = null;
        anim = new AnimatorSet();
        anim.playTogether(ObjectAnimator.ofFloat(current_items.get(0), "scaleX", 0.5f), ObjectAnimator.ofFloat(current_items.get(0), "scaleY", 0.5f), ObjectAnimator.ofFloat(current_items.get(0), "alpha", 0f));
        anim.setDuration(ANIM_SPEED);
        anim.addListener(new AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                show_place();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                // sound touch
                if (!sp.getBoolean("mute", false) && isForeground)
                    sndpool.play(snd_move, 0.8f, 0.8f, 0, 0, 1);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });
        anim.start();
    }

    // show_place
    void show_place() {
        // update item
        ((ImageView) current_items.get(0)).setImageBitmap(bitmap_place);

        // animate
        anim.removeAllListeners();
        anim = null;
        anim = new AnimatorSet();
        anim.playTogether(ObjectAnimator.ofFloat(current_items.get(0), "scaleX", 1f), ObjectAnimator.ofFloat(current_items.get(0), "scaleY", 1f), ObjectAnimator.ofFloat(current_items.get(0), "alpha", 1f));
        anim.setDuration(ANIM_SPEED);
        anim.addListener(new AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                current_items.remove(0);
                findViewById(R.id.items).setEnabled(true); // enable items
            }

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });
        anim.start();
    }

    // hide_two_items
    void hide_two_items() {
        anim.removeAllListeners();
        anim = null;
        anim = new AnimatorSet();
        anim.playTogether(ObjectAnimator.ofFloat(current_items.get(0), "scaleX", 0.5f), ObjectAnimator.ofFloat(current_items.get(0), "scaleY", 0.5f), ObjectAnimator.ofFloat(current_items.get(1), "scaleX", 0.5f), ObjectAnimator.ofFloat(current_items.get(1), "scaleY", 0.5f), ObjectAnimator.ofFloat(current_items.get(0), "alpha", 0f), ObjectAnimator.ofFloat(current_items.get(1), "alpha", 0f));
        anim.setDuration(ANIM_SPEED);
        anim.addListener(new AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                num_opened += 2;

                // remove opened items
                ((ViewGroup) findViewById(R.id.items)).removeView(current_items.get(0));
                ((ViewGroup) findViewById(R.id.items)).removeView(current_items.get(1));
                current_items.clear();

                // check complete
                if (num_opened == NUM_ITEMS * NUM_ITEMS)
                    game_over();
                else
                    findViewById(R.id.items).setEnabled(true); // enable items
            }

            @Override
            public void onAnimationStart(Animator animation) {
                // sound touch
                if (!sp.getBoolean("mute", false) && isForeground)
                    sndpool.play(snd_ok, 1f, 1f, 0, 0, 1);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });
        anim.start();
    }

    // compare_items
    void compare_items() {
        if (current_items.size() == 2) {
            if (bitmap_items.get((int) current_items.get(0).getTag()).equals(bitmap_items.get((int) current_items.get(1).getTag())))
                hide_two_items(); // hide two opened items
            else
                hide_first_item(); // hide first opened item
        } else
            findViewById(R.id.items).setEnabled(true); // enable items
    }

    // game_over
    void game_over() {
        findViewById(R.id.items).setEnabled(false);
        findViewById(R.id.mess).setVisibility(View.VISIBLE);

        // sound info
        if (!sp.getBoolean("mute", false) && isForeground)
            sndpool.play(snd_info, 1f, 1f, 0, 0, 1);

        saveScore(score);

        // show score
        ((TextView) findViewById(R.id.txt_result)).setText(getString(R.string.score) + score);
        ((TextView) findViewById(R.id.txt_high_result)).setText(getString(R.string.high_score) + sp.getInt("scoreth", 0));

        // delay
        h.postDelayed(STOP, 3000);
    }

    // STOP
    Runnable STOP = new Runnable() {
        @Override
        public void run() {
            show_section(R.id.result);
            h.removeCallbacks(START);
            h.removeCallbacks(STOP);


        }
    };

    // onClick
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                START.run();
                break;
            case R.id.btn_restart:
                // start
                Intent in=new Intent(Mainthird.this, Mainss.class);
                startActivity(in);
                finish();
                break;
            case R.id.mess:
                // message
                STOP.run();
                break;
            case R.id.btn_exit:
                // exit
                finish();
                break;
            case R.id.btn_sound:
                // sound
                if (sp.getBoolean("mute", false)) {
                    sp.edit().putBoolean("mute", false).commit();
                    mp.setVolume(0.5f, 0.5f);
                    ((Button) findViewById(R.id.btn_sound)).setText(getString(R.string.btn_mute));
                } else {
                    sp.edit().putBoolean("mute", true).commit();
                    mp.setVolume(0, 0);
                    ((Button) findViewById(R.id.btn_sound)).setText(getString(R.string.btn_sound));
                }
                break;
            case R.id.btn_leaderboard:
                showLeaders();
                break;
            case R.id.btn_sign:
                if (isSigned)
                    signOut();
                else
                    signIn();
                break;
            case R.id.btn_home:
                show_section(R.id.main);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        return;
        /*switch (current_section) {
            case R.id.main:
                super.onBackPressed();
                break;
            case R.id.result:
                show_section(R.id.main);
                break;
            case R.id.game:
                // stop anim
                if (anim != null) {
                    anim.removeAllListeners();
                    anim.cancel();
                }

                show_section(R.id.main);
                h.removeCallbacks(START);
                h.removeCallbacks(STOP);
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
        h.removeCallbacks(START);
        h.removeCallbacks(STOP);
        sndpool.release();
        mp.release();

        // clear bitmap_place
        if (bitmap_place != null)
            bitmap_place.recycle();

        // clear bitmap_items
        if (bitmap_items != null) {
            for (int i = 0; i < bitmap_items.size(); i++)
                bitmap_items.get(i).recycle();
            bitmap_items.clear();
            bitmap_items = null;
        }

        // stop anim
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
            mp.setVolume(0.4f, 0.4f);
    }

    // DpToPx
    float DpToPx(float dp) {
        return (dp * Math.max(getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels) / 520f);
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

        if (sp.contains("scoreth")) // save score to leaderboard
            saveScore(sp.getInt("scoreth", 0));

        // show leaders
        if (showLeaders) {
            showLeaders = false;
            showLeaders();
        }

        // get score from leaderboard
        Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this)).loadCurrentPlayerLeaderboardScore(getString(R.string.leaderboard), LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC).addOnSuccessListener(this, new OnSuccessListener<AnnotatedData<LeaderboardScore>>() {
            @Override
            public void onSuccess(final AnnotatedData<LeaderboardScore> leaderboardScoreAnnotatedData) {
                if (leaderboardScoreAnnotatedData != null && leaderboardScoreAnnotatedData.get() != null && (!sp.contains("score") || (int) leaderboardScoreAnnotatedData.get().getRawScore() < sp.getInt("score", 0)))
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
        if (!sp.contains("scoreth") || score < sp.getInt("scoreth", 0))
            sp.edit().putInt("scoreth", score).commit();

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

    // bitmapFromAssets
    Bitmap bitmapFromAssets(String src, int width, int height) {
        final BitmapFactory.Options options = new BitmapFactory.Options();

        try {
            // get original bitmap size
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(getAssets().open(src), null, options);

            // calculate bitmap scale
            int inSampleSize = 1; // original
            if (options.outHeight > height || options.outWidth > width) {
                final int halfHeight = options.outHeight / 2;
                final int halfWidth = options.outWidth / 2;
                while (halfHeight / inSampleSize > height && halfWidth / inSampleSize > width)
                    inSampleSize *= 2;
            }
            options.inSampleSize = inSampleSize;

            // decode scaled bitmap
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeStream(getAssets().open(src), null, options);
        } catch (IOException e1) {
        }

        return null;
    }

    // adMob
    void adMob() {

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