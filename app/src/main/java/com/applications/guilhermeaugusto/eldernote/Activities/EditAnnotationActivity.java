package com.applications.guilhermeaugusto.eldernote.Activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.applications.guilhermeaugusto.eldernote.Dialogs.MessageDialogFragment;
import com.applications.guilhermeaugusto.eldernote.Managers.AlarmEntity;
import com.applications.guilhermeaugusto.eldernote.Managers.DataBaseHandler;
import com.applications.guilhermeaugusto.eldernote.Managers.LogFiles;
import com.applications.guilhermeaugusto.eldernote.R;
import com.applications.guilhermeaugusto.eldernote.beans.Annotations;
import com.applications.guilhermeaugusto.eldernote.beans.Enums;

import java.io.IOException;

/**
 * Created by guilhermemartins on 12/17/14.
 */
public class EditAnnotationActivity extends FragmentActivity implements MessageDialogFragment.MessageFragmentListener {

    private static final String LOG_TAG = "EditAnnotationActivityLog";
    private Handler seekHandler;
    private boolean newStartPlaying;
    private boolean oldStartPlaying;
    private boolean updateAlarm = false;
    private DataBaseHandler dataBaseHandler;
    private Annotations annotation;
    private Button deleteAlarmButton;
    private Button alarmButton;
    private Button editAnnotationButton;
    private Button backButton;
    private Button cancelButton;
    private Button saveButton;
    private TextView titleTextView;

    private RelativeLayout annotationUpdateCheckLayout;
    private ScrollView oldScrollView;
    private TextView oldTextView;
    private RelativeLayout oldSoundLayout;
    private SeekBar oldSoundProgressSeekbar;
    private Button oldSoundPlayingButton;
    private LinearLayout oldAlarmLayout;
    private TextView oldAlarmCycleDescriptionTextView;
    private TextView oldAlarmDateTextView;
    private MediaPlayer oldMediaPlayer;

    private TextView newTitleTextView;
    private ScrollView newScrollView;
    private TextView newTextView;
    private RelativeLayout newSoundLayout;
    private SeekBar newSoundProgressSeekbar;
    private Button newSoundPlayingButton;
    private LinearLayout newAlarmLayout;
    private TextView newAlarmCycleDescriptionTextView;
    private TextView newAlarmDateTextView;
    private MediaPlayer newMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_annotation);
        dataBaseHandler = new DataBaseHandler(getApplicationContext());
        seekHandler = new Handler();
        init();
        showContent();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogFiles.writeActivityEventsLog(Enums.ActivityType.EditAnnotation, "Start");
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogFiles.writeActivityEventsLog(Enums.ActivityType.EditAnnotation, "Stop");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        LogFiles.writeActivityEventsLog(Enums.ActivityType.EditAnnotation, "BackButton");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMedias();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // MotionEvent object holds X-Y values
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            LogFiles.writeTouchLog(Enums.ActivityType.EditAnnotation, event.getX(), event.getY());
        }

        return super.onTouchEvent(event);
    }

    private void init(){
        annotation = (Annotations) getIntent().getSerializableExtra("Annotation");
        deleteAlarmButton = (Button) findViewById(R.id.deleteAlarmButton);
        editAnnotationButton = (Button) findViewById(R.id.editAnnotationButton);
        alarmButton = (Button) findViewById(R.id.alarmButton);
        backButton = (Button) findViewById(R.id.backButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);
        saveButton = (Button) findViewById(R.id.saveButton);
        titleTextView = (TextView) findViewById(R.id.titleTextView);

        annotationUpdateCheckLayout = (RelativeLayout) findViewById(R.id.annotationUpdateCheckLayout);
        oldScrollView = (ScrollView) findViewById(R.id.oldScrollView);
        oldTextView = (TextView) findViewById(R.id.oldTextView);
        oldSoundLayout = (RelativeLayout) findViewById(R.id.oldSoundLayout);
        oldSoundProgressSeekbar = (SeekBar) findViewById(R.id.oldSoundProgressSeekbar);
        oldSoundPlayingButton = (Button) findViewById(R.id.oldSoundPlayingButton);
        oldAlarmLayout = (LinearLayout) findViewById(R.id.oldAlarmLayout);
        oldAlarmCycleDescriptionTextView = (TextView) findViewById(R.id.oldAlarmCycleDescriptionTextView);
        oldAlarmDateTextView = (TextView) findViewById(R.id.oldAlarmDateTextView);

        newTitleTextView = (TextView) findViewById(R.id.newTitleTextView);
        newScrollView = (ScrollView) findViewById(R.id.newScrollView);
        newTextView = (TextView) findViewById(R.id.newTextView);
        newSoundLayout = (RelativeLayout) findViewById(R.id.newSoundLayout);
        newSoundProgressSeekbar = (SeekBar) findViewById(R.id.newSoundProgressSeekbar);
        newSoundPlayingButton = (Button) findViewById(R.id.newSoundPlayingButton);
        newAlarmLayout = (LinearLayout) findViewById(R.id.newAlarmLayout);
        newAlarmCycleDescriptionTextView = (TextView) findViewById(R.id.newAlarmCycleDescriptionTextView);
        newAlarmDateTextView = (TextView) findViewById(R.id.newAlarmDateTextView);
    }

    private void showContent(){
        if(annotation.getOldAnnotation() == null) {
            editAnnotationButton.setVisibility(View.VISIBLE);
            backButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.INVISIBLE);
            saveButton.setVisibility(View.INVISIBLE);
            annotationUpdateCheckLayout.setVisibility(View.INVISIBLE);
            if (annotation.getAlarm().getId() > -1) {
                alarmButton.setText(getResources().getString(R.string.editAlarmButtonText));
                deleteAlarmButton.setVisibility(View.VISIBLE);
            } else {
                alarmButton.setText(getResources().getString(R.string.createAlarmButtonText));
                deleteAlarmButton.setVisibility(View.INVISIBLE);
            }
        } else {
            editAnnotationButton.setVisibility(View.INVISIBLE);
            alarmButton.setVisibility(View.INVISIBLE);
            deleteAlarmButton.setVisibility(View.INVISIBLE);
            backButton.setVisibility(View.INVISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
            annotationUpdateCheckLayout.setVisibility(View.VISIBLE);

            if(annotation.getAlarm().getId() != annotation.getOldAnnotation().getAlarm().getId()) {
                oldScrollView.setVisibility(View.INVISIBLE);
                newScrollView.setVisibility(View.INVISIBLE);
                oldSoundLayout.setVisibility(View.INVISIBLE);
                newSoundLayout.setVisibility(View.INVISIBLE);
                oldAlarmLayout.setVisibility(View.VISIBLE);
                newAlarmLayout.setVisibility(View.VISIBLE);
                updateAlarm = true;
                titleTextView.setText(getResources().getString(R.string.oldAlarmTitleText));
                newTitleTextView.setText(getResources().getString(R.string.newAlarmTitleText));
                if (annotation.getOldAnnotation().hasAlarm()) {
                    oldAlarmCycleDescriptionTextView.setText(annotation.getOldAnnotation().getAlarm().createPeriodLayout(getApplicationContext()));
                    oldAlarmDateTextView.setText(annotation.getOldAnnotation().getAlarm().createDateLayout(getApplicationContext()));
                } else {
                    oldAlarmCycleDescriptionTextView.setText(getResources().getString(R.string.emptyAlarmText));
                }
                newAlarmCycleDescriptionTextView.setText(annotation.getAlarm().createPeriodLayout(getApplicationContext()));
                newAlarmDateTextView.setText(annotation.getAlarm().createDateLayout(getApplicationContext()));
            } else if(annotation.contentIsSound()){
                oldScrollView.setVisibility(View.INVISIBLE);
                newScrollView.setVisibility(View.INVISIBLE);
                oldAlarmLayout.setVisibility(View.INVISIBLE);
                newAlarmLayout.setVisibility(View.INVISIBLE);
                oldSoundLayout.setVisibility(View.VISIBLE);
                newSoundLayout.setVisibility(View.VISIBLE);
                titleTextView.setText(getResources().getString(R.string.oldSoundTitleText) + annotation.getOldAnnotation().getAtivity().getTitle());
                newTitleTextView.setText(getResources().getString(R.string.newSoundTitleText) + annotation.getAtivity().getTitle());
                prepareToPlayingBothSounds();
                prepareBothSeekBars();
            } else {
                oldSoundLayout.setVisibility(View.INVISIBLE);
                newSoundLayout.setVisibility(View.INVISIBLE);
                oldAlarmLayout.setVisibility(View.INVISIBLE);
                newAlarmLayout.setVisibility(View.INVISIBLE);
                oldScrollView.setVisibility(View.VISIBLE);
                newScrollView.setVisibility(View.VISIBLE);
                oldTextView.setText(annotation.getOldAnnotation().getMessage());
                newTextView.setText(annotation.getMessage());
                titleTextView.setText(getResources().getString(R.string.oldTextTitleText));
                newTitleTextView.setText(getResources().getString(R.string.newTextTitleText));
            }
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.editAnnotationButton: {
                LogFiles.writeButtonActionLog(Enums.ActivityType.EditAnnotation, Enums.ButtonActionTypes.UpdateAnnotation);
                if (annotation.contentIsSound()) {
                    goToEditSoundActivity();
                } else {
                    goToEditTextActivity();
                }
                break;
            }
            case R.id.alarmButton: {
                LogFiles.writeButtonActionLog(Enums.ActivityType.EditAnnotation, Enums.ButtonActionTypes.UpdateAlarm);
                goToEditAlarmActivity();
                break;
            }
            case R.id.deleteAlarmButton: {
                LogFiles.writeButtonActionLog(Enums.ActivityType.EditAnnotation, Enums.ButtonActionTypes.Delete);
                callMessageDialog(getResources().getString(R.string.deleteAlarmTitleDialogText),
                        getResources().getString(R.string.deleteAlarmMessageDialogText),
                        Enums.MessageTypes.DeleteMessage);
                break;
            }
            case R.id.cancelButton:{
                LogFiles.writeButtonActionLog(Enums.ActivityType.EditAnnotation, Enums.ButtonActionTypes.Cancel);
                Annotations oldAnnotation = new Annotations(annotation.getOldAnnotation().getId(),
                        annotation.getOldAnnotation().getMessage(),
                        annotation.getOldAnnotation().getSound(),
                        annotation.getOldAnnotation().getSoundDuration(),
                        annotation.getOldAnnotation().getAtivity(),
                        annotation.getOldAnnotation().getAlarm());
                annotation = oldAnnotation;
                goToVisualizeAnnotationActivity();
                break;
            }
            case R.id.backButton: {
                LogFiles.writeButtonActionLog(Enums.ActivityType.EditAnnotation, Enums.ButtonActionTypes.Back);
                goToVisualizeAnnotationActivity();
                break;
            }
            case R.id.oldSoundPlayingButton: {
                oldStartPlaying = !oldStartPlaying;
                if (oldStartPlaying) {
                    LogFiles.writeButtonActionLog(Enums.ActivityType.EditAnnotation, Enums.ButtonActionTypes.Listen);
                    oldSoundPlayingButton.setText(getResources().getString(R.string.stopPlayButtonText));
                    oldMediaPlayer.start();
                } else {
                    LogFiles.writeButtonActionLog(Enums.ActivityType.EditAnnotation, Enums.ButtonActionTypes.Pause);
                    oldSoundPlayingButton.setText(getResources().getString(R.string.startPlayButtonText));
                    oldMediaPlayer.pause();
                }
                break;
            }
            case R.id.newSoundPlayingButton: {
                newStartPlaying = !newStartPlaying;
                if (newStartPlaying) {
                    LogFiles.writeButtonActionLog(Enums.ActivityType.EditAnnotation, Enums.ButtonActionTypes.Listen);
                    newSoundPlayingButton.setText(getResources().getString(R.string.stopPlayButtonText));
                    newMediaPlayer.start();
                } else {
                    LogFiles.writeButtonActionLog(Enums.ActivityType.EditAnnotation, Enums.ButtonActionTypes.Pause);
                    newSoundPlayingButton.setText(getResources().getString(R.string.startPlayButtonText));
                    newMediaPlayer.pause();
                }
                break;
            }
            case R.id.saveButton: {
                LogFiles.writeButtonActionLog(Enums.ActivityType.EditAnnotation, Enums.ButtonActionTypes.Save);
                dataBaseHandler.updateAnnotation(annotation);
                if (updateAlarm) {
                    AlarmEntity.removeAlarm(getApplicationContext(),annotation.getOldAnnotation().getAlarm().getId());
                    AlarmEntity.createAlarm(getApplicationContext(), annotation);
                }
                Toast.makeText(this, getResources().getString(R.string.saveSuccessAnnotationToastText), Toast.LENGTH_LONG).show();
                goToVisualizeAnnotationActivity();

                break;
            }
            default:
                break;
        }

    }

    private void goToVisualizeAnnotationActivity(){
        releaseMedias();
        LogFiles.writeAnnotationsLog(annotation);
        annotation.setOldAnnotation(null);
        Intent intent = new Intent(getApplicationContext(), VisualizeAnnotationActivity.class);
        intent.putExtra("Annotation", annotation);
        startActivity(intent);
    }

    private void goToEditAlarmActivity(){
        Intent intent = new Intent(getApplicationContext(), EditAlarmActivity.class);
        intent.putExtra("Annotation", annotation);
        startActivity(intent);
    }

    private void goToEditTextActivity(){
        Intent intent = new Intent(getApplicationContext(), EditTextActivity.class);
        intent.putExtra("Annotation", annotation);
        startActivity(intent);
    }

    private void goToEditSoundActivity(){
        Intent intent = new Intent(getApplicationContext(), EditSoundActivity.class);
        intent.putExtra("Annotation", annotation);
        startActivity(intent);
    }

//region SoundPreparationMethods
    private void prepareBothSeekBars(){
        oldSoundProgressSeekbar.setMax(oldMediaPlayer.getDuration());
        oldSoundProgressSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) { oldMediaPlayer.seekTo(progress); }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        newSoundProgressSeekbar.setMax(newMediaPlayer.getDuration());
        newSoundProgressSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) { newMediaPlayer.seekTo(progress); }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekUpdation();
    }

    private void prepareToPlayingBothSounds() {
        oldMediaPlayer = new MediaPlayer();
        newMediaPlayer = new MediaPlayer();

        try {
            oldMediaPlayer.setDataSource(annotation.getOldAnnotation().getSound());
            oldMediaPlayer.prepare();
            annotation.setSoundDuration(Long.toString(oldMediaPlayer.getDuration()));
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        try {
            newMediaPlayer.setDataSource(annotation.getSound());
            newMediaPlayer.prepare();
            annotation.setSoundDuration(Long.toString(newMediaPlayer.getDuration()));
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        oldMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                oldMediaPlayer.seekTo(0);
                oldStartPlaying = false;
                oldSoundPlayingButton.setText(getResources().getString(R.string.startPlayButtonText));
            }
        });

        newMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                newMediaPlayer.seekTo(0);
                newStartPlaying = false;
                newSoundPlayingButton.setText(getResources().getString(R.string.startPlayButtonText));
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
        if (oldMediaPlayer != null) oldSoundProgressSeekbar.setProgress(oldMediaPlayer.getCurrentPosition());
        if (newMediaPlayer != null) newSoundProgressSeekbar.setProgress(newMediaPlayer.getCurrentPosition());
        seekHandler.postDelayed(run, 100);
    }

    private void releaseMedias() {
        if (oldMediaPlayer != null) {
            oldMediaPlayer.release();
            oldMediaPlayer = null;
        }

        if (newMediaPlayer != null) {
            newMediaPlayer.release();
            newMediaPlayer = null;
        }
    }
//endregion

//region deleteMethods
    private void deleteAlarm(){
        showContent();
        AlarmEntity.removeAlarm(getApplicationContext(), annotation.getAlarm().getId());
        annotation.getAlarm().setId(-1);
        annotation.getAlarm().setDateInMillis(null);
        annotation.getAlarm().setCyclePeriod(Enums.PeriodTypes.None);
        Toast.makeText(this, getResources().getString(R.string.deleteSuccessAlarmToastText), Toast.LENGTH_LONG).show();
        goToVisualizeAnnotationActivity();
    }
//endregion

    private void callMessageDialog(String title, String message, Enums.MessageTypes messageType){
        Bundle messageBundle = new Bundle();
        messageBundle.putSerializable(MessageDialogFragment.TYPE, messageType);
        messageBundle.putString(MessageDialogFragment.TITLE, title);
        messageBundle.putString(MessageDialogFragment.MESSAGE, message);
        DialogFragment dialogFragment = new MessageDialogFragment();
        dialogFragment.setArguments(messageBundle);
        dialogFragment.show(getSupportFragmentManager(), "message_dialog");
    }

    public void onMessageDialogPositiveClick(Enums.MessageTypes messageType) { deleteAlarm(); }

    public void onMessageDialogNegativeClick(Enums.MessageTypes messageType) { }
}