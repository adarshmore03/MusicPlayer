package com.example.adplayer;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class PlaySong<MusicService> extends AppCompatActivity implements ActionPlaying, ServiceConnection {
    TextView txtsn, time2, time1;
    ImageView play,pause, previous, next, favbt, shufflebt, repeatbt;
   // static MediaPlayer mediaPlayer;
    String sname;
    SeekBar seekBar;
    BarVisualizer visualizer;
    ImageView image;

    public static final String EXTRA_NAME = "song_name";
    int position;
    ArrayList<File> mySongs;
    Thread updateseekbar;
    static boolean repeateBoolean=false,favouriteBoolean=false;
    com.example.adplayer.MusicService musicService;




    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (visualizer != null) {
            visualizer.release();
        }
        super.onDestroy();
    }






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);

        txtsn = findViewById(R.id.txtsn);
        play = findViewById(R.id.play);
        favbt=findViewById(R.id.fav);


        previous = findViewById(R.id.previous);
        next = findViewById(R.id.next);
        seekBar = findViewById(R.id.seekBar);
        visualizer = findViewById(R.id.blast);
        image = findViewById(R.id.image);
        time2 = findViewById(R.id.time2);
        time1 = findViewById(R.id.time1);

        repeatbt=findViewById(R.id.repeatbt);

        if (musicService != null) {
            musicService.stop();
            musicService.release();
        }



        Intent i = getIntent();
        Bundle bundle = i.getExtras();

        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        String songName = i.getStringExtra("songname");
        position = bundle.getInt("pos", 0);
        txtsn.setSelected(true);
        if (mySongs != null && !mySongs.isEmpty()) {

            Intent intent=new Intent(this, com.example.adplayer.MusicService.class);
            intent.putExtra("servicePosition",position);
            startService(intent);

        //  musicService.start();



            updateseekbar = new Thread() {
                @Override
                public void run() {
                    int totalDuration = musicService.getDuration();
                    int currentposition = 0;

                    while (currentposition < totalDuration) {
                        seekBar.setProgress(currentposition);
                        try {
                            Thread.sleep(1000);
                            currentposition = musicService.getCurrentPosition();

                        } catch (InterruptedException | IllegalStateException e) {
                            e.printStackTrace();
                        }

                    }
                }

            };
            seekBar.setMax(musicService.getDuration());
            updateseekbar.start();
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                int progress = 0;

                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                    musicService.seekTo(seekBar.getProgress());


                }
            });

            String endTime = createTime(musicService.getDuration());
            time2.setText(endTime);

            final Handler handler = new Handler();
            final int delay = 1000;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    String currentTime = createTime(musicService.getCurrentPosition());
                    time1.setText(currentTime);
                    handler.postDelayed(this, delay);
                }
            }, delay);
        }

        playpause();

        int audiosessionId = musicService.getAudioSessionId();
        if (audiosessionId != -1) {
            visualizer.setAudioSessionId(audiosessionId);
        }
       nextbt();
       previousbt();
       setRepeatbt();
       setFavbt();


    }
    @Override
    protected void onResume() {
        Intent intent=new Intent(this, com.example.adplayer.MusicService.class);
        bindService(intent,this,BIND_AUTO_CREATE);
        playpause();
        nextbt();
        previousbt();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    @Override
    public void onEnterAnimationComplete() {
        nextbt();
        if (musicService!=null) {
            musicService.createMediaPlayer(position);
            musicService.start();


           super.onEnterAnimationComplete();
        }
    }

    public void startAnimation(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(image, "rotation", 0f, 360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }

    public void startAnimation2(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(image, "rotation", 0f, -360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }

    public String createTime(int duration) {
        String time = "";
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;

        time += min + ":";

        if (sec < 10) {
            time += "0";
        }
        time += sec;

        return time;
    }



    private int getRandom(int i){
        Random random=new Random();
        return random.nextInt(i+1);
    }

    public void setSeekBar(){
        updateseekbar = new Thread() {
            @Override
            public void run() {
                int totalDuration = musicService.getDuration();
                int currentposition = 0;

                while (currentposition < totalDuration) {
                    seekBar.setProgress(currentposition);
                    try {
                        Thread.sleep(1000);
                        currentposition = musicService.getCurrentPosition();

                    } catch (InterruptedException | IllegalStateException e) {
                        e.printStackTrace();
                    }

                }
            }

        };
        seekBar.setMax(musicService.getDuration());
        updateseekbar.start();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                musicService.seekTo(seekBar.getProgress());


            }
        });

        String endTime = createTime(musicService.getDuration());
        time2.setText(endTime);

        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(musicService.getCurrentPosition());
                time1.setText(currentTime);
                handler.postDelayed(this, delay);
            }
        }, delay);
    }
    public void setRepeatbt(){
        repeatbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (repeateBoolean){
                    repeateBoolean=false;
                    //next song
                    repeatbt.setImageResource(R.drawable.shuffle);
                    Toast.makeText(PlaySong.this,"playing song in sequence",Toast.LENGTH_SHORT).show();
                    musicService.setLooping(false);
                    musicService.start();

                }else {
                    repeateBoolean=true;
                    repeatbt.setImageResource(R.drawable.repeat);
                    Toast.makeText(PlaySong.this,"Started song repeating mode",Toast.LENGTH_SHORT).show();
                    musicService.setLooping(true);
                    musicService.start();
                }
            }
        });
    }
    public void setFavbt(){
        favbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (favouriteBoolean){
                    favouriteBoolean=false;
                    favbt.setImageResource(R.drawable.fav);
                    Toast.makeText(PlaySong.this,"Song remove from favourite",Toast.LENGTH_SHORT).show();



                }else {
                    favouriteBoolean=true;
                    favbt.setImageResource(R.drawable.fav1);
                    Toast.makeText(PlaySong.this,"Song add to favourite",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void playpause(){
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (musicService.isPlaying()) {
                    play.setBackgroundResource(R.drawable.play);
                    musicService.pause();

                } else {
                    play.setBackgroundResource(R.drawable.pause);
                    musicService.start();
                }
            }
        });
    }

    public void nextbt(){
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicService.stop();
                musicService.release();

                position = ((position + 1) % mySongs.size());

                // position = ((position + 1) % mySongs.size());

                Uri u = Uri.parse(mySongs.get(position).toString());
               musicService.createMediaPlayer(position);
                sname = mySongs.get(position).getName();
                txtsn.setText(sname);
                musicService.start();

                play.setBackgroundResource(R.drawable.pause);
                startAnimation(image);
                setSeekBar();
                setRepeatbt();


                int audiosessionId = musicService.getAudioSessionId();
                if (audiosessionId != -1) {
                    visualizer.setAudioSessionId(audiosessionId);
                }

            }


        });
        onEnterAnimationComplete();

    }
    public void previousbt(){
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicService.stop();
                musicService.release();

                position = ((position - 1) < 0) ? (mySongs.size() - 1) : (position - 1);


                Uri u = Uri.parse(mySongs.get(position).toString());
                musicService.createMediaPlayer(position);
                sname = mySongs.get(position).getName();
                txtsn.setText(sname);
                musicService.start();
                play.setBackgroundResource(R.drawable.pause);
                startAnimation2(image);
                setSeekBar();
                setRepeatbt();


                int audiosessionId = musicService.getAudioSessionId();
                if (audiosessionId != -1) {
                    visualizer.setAudioSessionId(audiosessionId);
                }

            }
        });
      onEnterAnimationComplete();
    }


    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        com.example.adplayer.MusicService.MyBinder myBinder=
                (com.example.adplayer.MusicService.MyBinder) service;
        musicService=myBinder.getService();
        setSeekBar();
        Uri uri = Uri.parse(mySongs.get(position).toString());
        sname = mySongs.get(position).getName();
        txtsn.setText(sname);
        musicService.start();

    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
         musicService=null;
    }
}


