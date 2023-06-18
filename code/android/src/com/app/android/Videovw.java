package com.app.android;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import androidx.annotation.Nullable;

public class Videovw extends Activity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videoview);
        VideoView videoView =(VideoView)findViewById(R.id.vdVw);
        MediaController mediaController= new MediaController(this);
        mediaController.setAnchorView(videoView);
        Uri uri= Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video);
        Display display= getWindowManager().getDefaultDisplay();
        int width=display.getWidth();
        int height=display.getHeight();
        videoView.setLayoutParams(new RelativeLayout.LayoutParams(width,height));


        videoView.setMediaController(mediaController);
        videoView.setVideoURI(uri);
        videoView.requestFocus();
        videoView.start();

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Intent i = new Intent(Videovw.this, Firstscreen.class);

                // on below line we are
                // starting a new activity.
                startActivity(i);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(Videovw.this, Firstscreen.class);

        // on below line we are
        // starting a new activity.
        startActivity(i);
        finish();
    }
}
