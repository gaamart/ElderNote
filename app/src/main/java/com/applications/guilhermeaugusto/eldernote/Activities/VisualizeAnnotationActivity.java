package com.applications.guilhermeaugusto.eldernote.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.applications.guilhermeaugusto.eldernote.R;
import com.applications.guilhermeaugusto.eldernote.beans.Annotations;
import com.applications.guilhermeaugusto.eldernote.beans.Enums;

import java.io.IOException;

/**
 * Created by guilhermemartins on 11/7/14.
 */
public class VisualizeAnnotationActivity extends Activity {

    private Window window;
    private Ringtone ringtone;
    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private Annotations annotation;
    private Handler seekHandler;
    private TextView titleTextView;
    private TextView annotationTextView;
    private TextView alarmCycleDescriptionTextView;
    private TextView alarmDateTextView;
    private Button soundPlayingButton;
    private Button doneButton;
    private ImageView alarmImageView;
    private TextView annotationTitleTextView;
    private RelativeLayout annotationMessageLayout;
    private LinearLayout alarmControllerLayout;
    private LinearLayout titleLayout;
    private boolean startPlaying;
    private static final String LOG_TAG = "AlarmDialogFragmentLog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualize_annotation);
        annotation = (Annotations) getIntent().getSerializableExtra("Annotation");
        seekHandler = new Handler();
        if(annotation.getOperationType() == Enums.OperationType.Triggered) { callAlarmRingtone(); startVibrate(); }
    }
    @Override
    protected  void onResume(){
        super.onResume();
        window = this.getWindow();
        window.addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    @Override
    protected void onStart(){
        super.onStart();
        init();
        fillComponents();
        showComponents();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMedia();
    }

    public void init(){
        titleTextView = (TextView) findViewById(R.id.titleTextView);
        annotationTextView = (TextView) findViewById(R.id.annotationTextView);
        alarmCycleDescriptionTextView = (TextView) findViewById(R.id.alarmCycleDescriptionTextView);
        alarmDateTextView = (TextView) findViewById(R.id.alarmDateTextView);
        seekBar = (SeekBar) findViewById(R.id.soundProgressSeekbar);
        soundPlayingButton = (Button) findViewById(R.id.soundPlayingButton);
        alarmImageView = (ImageView) findViewById(R.id.alarmRingtoneImageView);
        doneButton = (Button) findViewById(R.id.doneButton);
        annotationTitleTextView = (TextView) findViewById(R.id.annotationTitleTextView);
        annotationMessageLayout = (RelativeLayout) findViewById(R.id.annotationMessageLayout);
        alarmControllerLayout = (LinearLayout) findViewById(R.id.alarmControllerLayout);
        titleLayout = (LinearLayout) findViewById(R.id.titleLayout);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.doneButton: {
                if(annotation.getOperationType() == Enums.OperationType.Triggered) {
                    ringtone.stop();
                    vibrator.cancel();
                    annotation.setOperationType(Enums.OperationType.Visualize);
                    showComponents();
                } else {
                    releaseMedia();
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.soundPlayingButton: {
                startPlaying = !startPlaying;
                if (startPlaying) {
                    soundPlayingButton.setText(getResources().getString(R.string.stopPlayButtonText));
                    mediaPlayer.start();
                } else {
                    soundPlayingButton.setText(getResources().getString(R.string.startPlayButtonText));
                    mediaPlayer.pause();
                }
                break;
            }
            default: break;
        }
    }

    private void callAlarmRingtone() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
            ringtone.play();
        } catch (Exception e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void startVibrate() {
        vibrator = (Vibrator)  getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 1000, 1000};
        vibrator.vibrate(pattern, 0);
    }

    private void startAlarmAnnimation() {
        AnimationDrawable flyAnimationDrawable = new AnimationDrawable();
        flyAnimationDrawable.addFrame(getResources().getDrawable(R.drawable.leftturnedclock), 1000);
        flyAnimationDrawable.addFrame(getResources().getDrawable(R.drawable.rightturnedclock), 1000);
        flyAnimationDrawable.setOneShot(false);

        int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) { alarmImageView.setBackgroundDrawable(flyAnimationDrawable); }
        else { alarmImageView.setBackground(flyAnimationDrawable); }
        flyAnimationDrawable.start();
    }

    private void fillComponents(){
        if(annotation.getMessage() != null && !annotation.getMessage().isEmpty()){
            annotationTextView.setText(annotation.getMessage());
        }
        else {
            titleTextView.setText(annotation.getAtivity().getTitle());
            prepareToPlayingSound();
            prepareSeekBar();
        }
        if(annotation.getAlarm().getId() >= 0 ) {
            alarmCycleDescriptionTextView.setText(annotation.getAlarm().createPeriodLayout(getApplicationContext()));
            alarmDateTextView.setText(annotation.getAlarm().createDateLayout(getApplicationContext()));
        } else {
            alarmCycleDescriptionTextView.setText(getResources().getString(R.string.emptyAlarmText));
        }
    }

    private void showComponents(){

        if(annotation.getOperationType() == Enums.OperationType.Triggered) {
            setTitle(getResources().getString(R.string.alarmRingtoneDateText));
            doneButton.setText(getResources().getString(R.string.stopText));
            alarmImageView.setVisibility(View.VISIBLE);
            annotationTitleTextView.setVisibility(View.INVISIBLE);
            annotationMessageLayout.setVisibility(View.INVISIBLE);
            alarmControllerLayout.setVisibility(View.INVISIBLE);
            titleLayout.setVisibility(View.INVISIBLE);
            startAlarmAnnimation();
        } else {
            setTitle(getResources().getString(R.string.app_name));
            doneButton.setText(getResources().getString(R.string.doneText));
            annotationTitleTextView.setVisibility(View.VISIBLE);
            annotationMessageLayout.setVisibility(View.VISIBLE);
            alarmControllerLayout.setVisibility(View.VISIBLE);
            alarmImageView.setVisibility(View.INVISIBLE);
            if (annotation.getMessage() != null && !annotation.getMessage().isEmpty()) {
                annotationTextView.setVisibility(View.VISIBLE);
                seekBar.setVisibility(View.INVISIBLE);
                soundPlayingButton.setVisibility(View.INVISIBLE);
                titleLayout.setVisibility(View.INVISIBLE);
            } else {
                annotationTextView.setVisibility(View.INVISIBLE);
                seekBar.setVisibility(View.VISIBLE);
                soundPlayingButton.setVisibility(View.VISIBLE);
                titleLayout.setVisibility(View.VISIBLE);
            }
            if(annotation.getAlarm().getId() >= 0 ) {
                alarmDateTextView.setVisibility(View.VISIBLE);
            } else {
                alarmDateTextView.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void prepareSeekBar(){
        seekBar.setMax(mediaPlayer.getDuration());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) { mediaPlayer.seekTo(progress); }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekUpdation();
    }

    private void prepareToPlayingSound() {
        mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(annotation.getSound());
            mediaPlayer.prepare();
            annotation.setSoundDuration(Long.toString(mediaPlayer.getDuration()));
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.seekTo(0);
                startPlaying = false;
                soundPlayingButton.setText(getResources().getString(R.string.startPlayButtonText));
            }
        });
    }

    Runnable run = new Runnable() {
        @Override public void run() { seekUpdation(); }
    };

    private void seekUpdation() {
        if(mediaPlayer != null) seekBar.setProgress(mediaPlayer.getCurrentPosition());
        seekHandler.postDelayed(run, 100);
    }

    private void releaseMedia(){
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}