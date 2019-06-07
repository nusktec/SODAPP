package com.nsc.sodapp;

import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

public class Youtube_Panel extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener{

    private String PLAY_PIST = "UUrDg-KgwTtv88H32I4KEZcw";

    private YouTubePlayerView playerView;
    private YouTubePlayer.OnInitializedListener ylistener;

    YouTubePlayer pre_youTubePlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_youtube);


        playerView = (YouTubePlayerView) findViewById(R.id.youtube_palyer);

        playerView.initialize("AIzaSyDkVP3ddux3_W1_QyzCXzpD66oreimkG1A", this);


    }


    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        Toast.makeText(this,"Network Error",Toast.LENGTH_LONG).show();
    }


    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        youTubePlayer.setPlaybackEventListener(playbackEventListener);
        youTubePlayer.setPlayerStateChangeListener(playerStateChangeListener);

        Toast.makeText(this,"Player ready !",Toast.LENGTH_LONG).show();


           youTubePlayer.loadPlaylist(PLAY_PIST);

          if(!b){
              pre_youTubePlayer = youTubePlayer;
          }

    }

    private YouTubePlayer.PlaybackEventListener playbackEventListener = new YouTubePlayer.PlaybackEventListener() {
        @Override
        public void onPlaying() {

        }

        @Override
        public void onPaused() {
            Toast.makeText(getApplicationContext(),"Player paused",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStopped() {

        }

        @Override
        public void onBuffering(boolean b) {
            Toast.makeText(getApplicationContext(),"Now buffering",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSeekTo(int i) {
            Toast.makeText(getApplicationContext(),"Seeking "+i/2,Toast.LENGTH_SHORT).show();
        }
    };



    private YouTubePlayer.PlayerStateChangeListener playerStateChangeListener = new YouTubePlayer.PlayerStateChangeListener() {
        @Override
        public void onLoading() {
            Toast.makeText(getApplicationContext(),"Player loading",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLoaded(String s) {

        }

        @Override
        public void onAdStarted() {

        }

        @Override
        public void onVideoStarted() {

        }

        @Override
        public void onVideoEnded() {
            Toast.makeText(getApplicationContext(),"End playing",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(YouTubePlayer.ErrorReason errorReason) {
            Toast.makeText(getApplicationContext(),"Error playing\n"+errorReason.toString(),Toast.LENGTH_SHORT).show();
        }
    };

    public void vib(){
        Vibrator v = (Vibrator) getApplicationContext().getSystemService(VIBRATOR_SERVICE);
        v.vibrate(100);
    }




    public void w_pause(View view){
        vib();
        pre_youTubePlayer.pause();
    }

    public void w_play(View view){
        vib();
        pre_youTubePlayer.play();
    }

    public void w_stop(View view){
        vib();
        pre_youTubePlayer.pause();
    }




    public void playVideo(MenuItem view){
        vib();
    }

    public void stopVideo(MenuItem view){
        vib();
    }

    public void reloadVideo(MenuItem view){
        vib();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_youtube, menu);
        return true;
    }


    boolean press_one = false;
    @Override
    public void onBackPressed() {
        if(press_one==false){
            Toast.makeText(getApplicationContext(),"Use restore button on the frame...",Toast.LENGTH_LONG).show();
            press_one = true;
            return;
        }
        finish();
    }
}
