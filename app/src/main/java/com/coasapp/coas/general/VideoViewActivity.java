package com.coasapp.coas.general;

import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import com.coasapp.coas.R;


public class VideoViewActivity extends MyAppCompatActivity {

    MediaPlayer mediaPlayer;
    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);

         videoView = findViewById(R.id.videoViewFullScreen);
        String file = getIntent().getStringExtra("file"), type = getIntent().getStringExtra("type");
        if (type.equalsIgnoreCase("url")) {
            videoView.setVideoURI(Uri.parse(file));

        } else {
            videoView.setVideoPath(file);
        }
        findViewById(R.id.layoutProgress).setVisibility(View.VISIBLE);
        /*findViewById(R.id.layoutProgress).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*/
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaController.show();
                findViewById(R.id.layoutProgress).setVisibility(View.GONE);
                videoView.start();
                mp.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                    @Override
                    public void onBufferingUpdate(MediaPlayer mp, int percent) {
                        Log.i("VideoAttachment",""+percent);
                        if(percent==100){

                        }
                    }
                });

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(videoView!=null){
            videoView.stopPlayback();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getSupportActionBar().hide();
        } else {
            getSupportActionBar().show();
        }
    }
}
