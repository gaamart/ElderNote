package com.applications.guilhermeaugusto.eldernote.Activities;

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
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.applications.guilhermeaugusto.eldernote.Dialogs.MessageDialogFragment;
import com.applications.guilhermeaugusto.eldernote.Managers.AlarmEntity;
import com.applications.guilhermeaugusto.eldernote.Managers.DataBaseHandler;
import com.applications.guilhermeaugusto.eldernote.Managers.LogFiles;
import com.applications.guilhermeaugusto.eldernote.Managers.SoundFiles;
import com.applications.guilhermeaugusto.eldernote.R;
import com.applications.guilhermeaugusto.eldernote.beans.Annotations;
import com.applications.guilhermeaugusto.eldernote.beans.Enums;

import java.io.IOException;

/**
 * Created by guilhermemartins on 11/7/14.
 */
public class VisualizeAnnotationActivity extends FragmentActivity implements MessageDialogFragment.MessageFragmentListener {

    private boolean startPlaying;
    private DataBaseHandler dataBaseHandler;
    private static final String LOG_TAG = "AlarmDialogFragmentLog";
    private Ringtone ringtone;
    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private Annotations annotation;
    private Handler seekHandler;

    private TextView annotationTextView;
    private TextView alarmCycleDescriptionTextView;
    private TextView alarmDateTextView;
    private Button soundPlayingButton;
    private Button doneButton;
    private Button deleteButton;
    private Button editButton;
    private ImageView alarmImageView;
    private RelativeLayout alarmRingtoneLayout;
    private TextView annotationTitleTextView;
    private TextView soundTitleTextView;
    private RelativeLayout annotationMessageLayout;
    private LinearLayout alarmControllerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualize_annotation);
        annotation = (Annotations) getIntent().getSerializableExtra("Annotation");
        seekHandler = new Handler();
        dataBaseHandler = new DataBaseHandler(getApplicationContext());
        if (annotation.getOperationType() == Enums.OperationType.Triggered) {
            callAlarmRingtone();
            startVibrate();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogFiles.writeActivityEventsLog(Enums.ActivityType.VisualizeAnnotation, "Stop");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        LogFiles.writeActivityEventsLog(Enums.ActivityType.VisualizeAnnotation, "BackButton");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Window window = this.getWindow();
        window.addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    @Override
    protected void onStart() {
        super.onStart();
        init();
        fillComponents();
        showComponents();
        LogFiles.writeActivityEventsLog(Enums.ActivityType.VisualizeAnnotation, "Start");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMedia();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // MotionEvent object holds X-Y values
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            LogFiles.writeTouchLog(Enums.ActivityType.VisualizeAnnotation, event.getX(), event.getY());
        }

        return super.onTouchEvent(event);
    }

    public void init() {
        annotationTextView = (TextView) findViewById(R.id.annotationTextView);
        alarmCycleDescriptionTextView = (TextView) findViewById(R.id.alarmCycleDescriptionTextView);
        alarmDateTextView = (TextView) findViewById(R.id.alarmDateTextView);
        seekBar = (SeekBar) findViewById(R.id.soundProgressSeekbar);
        soundPlayingButton = (Button) findViewById(R.id.soundPlayingButton);
        alarmImageView = (ImageView) findViewById(R.id.alarmRingtoneImageView);
        alarmRingtoneLayout = (RelativeLayout) findViewById(R.id.alarmRingtoneLayout);
        doneButton = (Button) findViewById(R.id.doneButton);
        deleteButton = (Button) findViewById(R.id.deleteButton);
        editButton = (Button) findViewById(R.id.editButton);
        annotationTitleTextView = (TextView) findViewById(R.id.annotationTitleTextView);
        soundTitleTextView = (TextView) findViewById(R.id.soundTitleTextView);
        annotationMessageLayout = (RelativeLayout) findViewById(R.id.annotationMessageLayout);
        alarmControllerLayout = (LinearLayout) findViewById(R.id.alarmControllerLayout);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.deleteButton: {
                LogFiles.writeButtonActionLog(Enums.ActivityType.VisualizeAnnotation, Enums.ButtonActionTypes.Delete);
                callMessageDialog(getResources().getString(R.string.deleteAnnotationTitleDialogText),
                        getResources().getString(R.string.deleteAnnotationMessageDialogText),
                        Enums.MessageTypes.DecisionMessage);
                break;
            }
            case R.id.editButton: {
                LogFiles.writeButtonActionLog(Enums.ActivityType.VisualizeAnnotation, Enums.ButtonActionTypes.Update);
                goToEditAnnotationActivity();
                break;
            }
            case R.id.doneButton: {
                if (annotation.getOperationType() == Enums.OperationType.Triggered) {
                    LogFiles.writeButtonActionLog(Enums.ActivityType.VisualizeAnnotation, Enums.ButtonActionTypes.Stop);
                    ringtone.stop();
                    vibrator.cancel();
                    annotation.setOperationType(Enums.OperationType.Visualize);
                    showComponents();
                } else {
                    LogFiles.writeButtonActionLog(Enums.ActivityType.VisualizeAnnotation, Enums.ButtonActionTypes.Back);
                    goToMainActivity();
                }
                break;
            }
            case R.id.soundPlayingButton: {
                startPlaying = !startPlaying;
                if (startPlaying) {
                    LogFiles.writeButtonActionLog(Enums.ActivityType.VisualizeAnnotation, Enums.ButtonActionTypes.Listen);
                    soundPlayingButton.setText(getResources().getString(R.string.stopPlayButtonText));
                    mediaPlayer.start();
                } else {
                    LogFiles.writeButtonActionLog(Enums.ActivityType.VisualizeAnnotation, Enums.ButtonActionTypes.Pause);
                    soundPlayingButton.setText(getResources().getString(R.string.startPlayButtonText));
                    mediaPlayer.pause();
                }
                break;
            }
            default:
                break;
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
        vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 1000, 1000};
        vibrator.vibrate(pattern, 0);
    }

    private void startAlarmAnnimation() {
        AnimationDrawable flyAnimationDrawable = new AnimationDrawable();
        flyAnimationDrawable.addFrame(getResources().getDrawable(R.drawable.leftturnedclock), 1000);
        flyAnimationDrawable.addFrame(getResources().getDrawable(R.drawable.rightturnedclock), 1000);
        flyAnimationDrawable.setOneShot(false);

        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            alarmImageView.setBackgroundDrawable(flyAnimationDrawable);
        } else {
            alarmImageView.setBackground(flyAnimationDrawable);
        }
        flyAnimationDrawable.start();
    }

    private void fillComponents() {
        if (annotation.contentIsText()) {
            annotationTextView.setText(annotation.getMessage());
        } else {
            annotationTitleTextView.setText(annotation.getAtivity().getTitle());
            prepareToPlayingSound();
            prepareSeekBar();
        }
        if (annotation.getAlarm().getId() >= 0) {
            alarmCycleDescriptionTextView.setText(annotation.getAlarm().createPeriodLayout(getApplicationContext()));
            alarmDateTextView.setText(annotation.getAlarm().createDateLayout(getApplicationContext()));
        } else {
            alarmCycleDescriptionTextView.setText(getResources().getString(R.string.emptyAlarmText));
        }
    }

    private void showComponents() {

        if (annotation.getOperationType() == Enums.OperationType.Triggered) {
            annotationTitleTextView.setText(getResources().getString(R.string.alarmRingtoneDateText));
            editButton.setVisibility(View.INVISIBLE);
            deleteButton.setVisibility(View.INVISIBLE);
            doneButton.setVisibility(View.VISIBLE);
            doneButton.setText(getResources().getString(R.string.stopText));
            alarmImageView.setVisibility(View.VISIBLE);
            alarmRingtoneLayout.setVisibility(View.VISIBLE);
            if (annotation.contentIsText()) {
                soundTitleTextView.setVisibility(View.INVISIBLE);
                annotationMessageLayout.setVisibility(View.VISIBLE);
            }
            else {
                annotationMessageLayout.setVisibility(View.INVISIBLE);
                soundTitleTextView.setVisibility(View.VISIBLE);
                soundTitleTextView.setText(annotation.getAtivity().getTitle());
            }
            alarmControllerLayout.setVisibility(View.INVISIBLE);
            startAlarmAnnimation();
        } else {
            doneButton.setText(getResources().getString(R.string.backText));
            doneButton.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.VISIBLE);
            annotationMessageLayout.setVisibility(View.VISIBLE);
            alarmControllerLayout.setVisibility(View.VISIBLE);
            soundTitleTextView.setVisibility(View.INVISIBLE);
            alarmImageView.setVisibility(View.INVISIBLE);
            alarmRingtoneLayout.setVisibility(View.INVISIBLE);
            if (annotation.contentIsText()) {
                annotationTitleTextView.setText(getResources().getString(R.string.annotationText));
                annotationTextView.setVisibility(View.VISIBLE);
                seekBar.setVisibility(View.INVISIBLE);
                soundPlayingButton.setVisibility(View.INVISIBLE);
            } else {
                annotationTitleTextView.setText(annotation.getAtivity().getTitle());
                annotationTextView.setVisibility(View.INVISIBLE);
                seekBar.setVisibility(View.VISIBLE);
                soundPlayingButton.setVisibility(View.VISIBLE);
            }
            if (annotation.getAlarm().getId() >= 0) {
                alarmDateTextView.setVisibility(View.VISIBLE);
            } else {
                alarmDateTextView.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void prepareSeekBar() {
        seekBar.setMax(mediaPlayer.getDuration());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
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
        @Override
        public void run() {
            seekUpdation();
        }
    };

    private void seekUpdation() {
        if (mediaPlayer != null) seekBar.setProgress(mediaPlayer.getCurrentPosition());
        seekHandler.postDelayed(run, 100);
    }

    private void releaseMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void goToMainActivity() {
        releaseMedia  ();
        LogFiles.writeAnnotationsLog(annotation);
        annotation = null;
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void goToEditAnnotationActivity() {
        releaseMedia();
        LogFiles.writeAnnotationsLog(annotation);
        annotation.setOperationType(Enums.OperationType.Update);
        Intent intent = new Intent(getApplicationContext(), EditAnnotationActivity.class);
        intent.putExtra("Annotation", annotation);
        startActivity(intent);
    }

    private void callMessageDialog(String title, String message, Enums.MessageTypes messageType) {
        Bundle messageBundle = new Bundle();
        messageBundle.putSerializable(MessageDialogFragment.TYPE, messageType);
        messageBundle.putString(MessageDialogFragment.TITLE, title);
        messageBundle.putString(MessageDialogFragment.MESSAGE, message);
        DialogFragment dialogFragment = new MessageDialogFragment();
        dialogFragment.setArguments(messageBundle);
        dialogFragment.show(getSupportFragmentManager(), "message_dialog");
    }

    public void onMessageDialogPositiveClick(Enums.MessageTypes messageType) {
        deleteAnnotation();
        goToMainActivity();
    }

    public void onMessageDialogNegativeClick(Enums.MessageTypes messageType) {
    }

    private void deleteAnnotation() {
        if (annotation.contentIsSound()) {
            SoundFiles.removeOutputFile(annotation.getSound());
        }
        if (annotation.getAlarm().getId() != -1) {
            AlarmEntity.removeAlarm(getApplicationContext(), annotation.getAlarm().getId());
        }
        dataBaseHandler.deleteAnnotation(annotation);
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.deleteSuccessAnnotationToastText), Toast.LENGTH_LONG).show();
    }
}