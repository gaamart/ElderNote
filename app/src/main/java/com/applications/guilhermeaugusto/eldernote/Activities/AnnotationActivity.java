package com.applications.guilhermeaugusto.eldernote.Activities;

import android.support.v4.app.DialogFragment;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.view.View;
import android.util.Log;
import android.media.MediaPlayer;
import android.widget.RelativeLayout;
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

public class AnnotationActivity extends FragmentActivity implements MessageDialogFragment.MessageFragmentListener{
//region Variables
    public Annotations currentAnnotation;
    private static final String LOG_TAG = "AnnotationActivityLog";
    private boolean startPlaying;
    private Handler seekHandler;
    private MediaPlayer mediaPlayer;
    private DataBaseHandler dataBaseHandler;
    private DialogFragment dialogFragment;
    private Enums.ContentTypes contentType;
    private Enums.AlarmTypes alarmType;

    private SeekBar seekBar;
    private RelativeLayout writeTextLayout;
    private RelativeLayout soundRecordLayout;
    private Button soundPlayingManageButton;
    private Button createAlarmButton;
    private TextView alarmDateTextView;
    private TextView alarmCycleDescriptionTextView;
    private TextView alarmTitleTextView;
    private TextView annotationTextView;
    private TextView annotationTitleTextView;
    private TextView selectionAnnotationTypeTextView;
    private Button saveButton;
    private Button cancelButton;
    private Button singleCancelButton;
//endregion

//region ActivityMethods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annotation);
        dataBaseHandler = new DataBaseHandler(getApplicationContext());
        getInit();
        rulesForAddDataToComponents();
        rulesForShowComponents();
        rulesForShowAlarm();
    }

    @Override
    protected void onStart() {
        super.onStart();
        pauseSoundIfNeeded();
        LogFiles.writeActivityEventsLog(Enums.ActivityType.CreateAnnotation, "Start");
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseSoundIfNeeded();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogFiles.writeActivityEventsLog(Enums.ActivityType.CreateAnnotation, "Stop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMedia();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        LogFiles.writeActivityEventsLog(Enums.ActivityType.CreateAnnotation, "BackButton");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // MotionEvent object holds X-Y values
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            LogFiles.writeTouchLog(Enums.ActivityType.CreateAnnotation, event.getX(), event.getY());
        }

        return super.onTouchEvent(event);
    }
//endregion

//region InitializationMethods
    public AnnotationActivity() {
        startPlaying = false;
        mediaPlayer = null;
        seekHandler = new Handler();
        mediaPlayer = null;
        dialogFragment = null;
    }

    private void getInit(){
        seekBar = (SeekBar) findViewById(R.id.soundProgressSeekbar);
        writeTextLayout = (RelativeLayout) findViewById(R.id.writeTextLayout);
        soundRecordLayout = (RelativeLayout) findViewById(R.id.soundRecordLayout);
        soundPlayingManageButton = (Button) findViewById(R.id.soundPlayingManageButton);
        annotationTextView = (TextView) findViewById(R.id.annotationTextView);
        annotationTitleTextView = (TextView) findViewById(R.id.annotationTitleTextView);
        selectionAnnotationTypeTextView = (TextView) findViewById(R.id.selectionAnnotationTypeTextView);
        createAlarmButton = (Button) findViewById(R.id.createAlarmButton);
        alarmDateTextView = (TextView) findViewById(R.id.alarmDateTextView);
        alarmCycleDescriptionTextView = (TextView) findViewById(R.id.alarmCycleDescriptionTextView);
        alarmTitleTextView = (TextView) findViewById(R.id.alarmTitleTextView);
        saveButton = (Button) findViewById(R.id.saveButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);
        singleCancelButton  = (Button) findViewById(R.id.singleCancelButton);
    }
//endregion

//region PrepareMethods
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
            mediaPlayer.setDataSource(currentAnnotation.getSound());
            mediaPlayer.prepare();
            currentAnnotation.setSoundDuration(Long.toString(mediaPlayer.getDuration()));
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                startPlaying = false;
                contentType = Enums.ContentTypes.ShowingSound;
                rulesForShowComponents();
                mediaPlayer.seekTo(0);
            }
        });
    }
//endregion

//region RulesMethods
    private void rulesForAddDataToComponents(){
        currentAnnotation = (Annotations) getIntent().getSerializableExtra("Annotation");

        if(currentAnnotation.contentIsText()){
            contentType = Enums.ContentTypes.ShowingText;
            annotationTextView.setText(currentAnnotation.getMessage());
        }
        else if(currentAnnotation.contentIsSound()){

            contentType = Enums.ContentTypes.ShowingSound;
            prepareToPlayingSound();
            prepareSeekBar();
        }
        else
        {
            contentType = Enums.ContentTypes.Blank;
        }

        if(currentAnnotation.getAlarm().getId() >= 0) { alarmType = Enums.AlarmTypes.Created; }
        else {
            if (contentType == Enums.ContentTypes.Blank) { alarmType = Enums.AlarmTypes.Hidden; }
            else { alarmType = Enums.AlarmTypes.Selectable; }
        }
    }

    private void rulesForShowAlarm(){
        switch (alarmType){
            case Selectable: {
                createAlarmButton.setVisibility(View.VISIBLE);
                alarmTitleTextView.setVisibility(View.INVISIBLE);
                alarmDateTextView.setVisibility(View.INVISIBLE);
                alarmCycleDescriptionTextView.setVisibility(View.INVISIBLE);
                break;
            }
            case Created:{
                createAlarmButton.setVisibility(View.INVISIBLE);
                alarmCycleDescriptionTextView.setText(currentAnnotation.getAlarm().createPeriodLayout(getApplicationContext()));
                alarmDateTextView.setText(currentAnnotation.getAlarm().createDateLayout(getApplicationContext()));
                alarmDateTextView.setVisibility(View.VISIBLE);
                alarmCycleDescriptionTextView.setVisibility(View.VISIBLE);
                alarmTitleTextView.setVisibility(View.VISIBLE);
                break;
            }
            case Hidden:{
                alarmTitleTextView.setVisibility(View.INVISIBLE);
                createAlarmButton.setVisibility(View.INVISIBLE);
                alarmDateTextView.setVisibility(View.INVISIBLE);
                alarmCycleDescriptionTextView.setVisibility(View.INVISIBLE);
                break;
            }
            default: break;
        }
    }

    private void rulesForShowComponents(){
        writeTextLayout.setVisibility(View.INVISIBLE);
        soundRecordLayout.setVisibility(View.INVISIBLE);

        switch (contentType){
            case Blank: {
                writeTextLayout.setVisibility(View.VISIBLE);
                soundRecordLayout.setVisibility(View.VISIBLE);
                annotationTextView.setVisibility(View.INVISIBLE);
                seekBar.setVisibility(View.INVISIBLE);
                soundPlayingManageButton.setVisibility(View.INVISIBLE);
                saveButton.setVisibility(View.INVISIBLE);
                cancelButton.setVisibility(View.INVISIBLE);
                singleCancelButton.setVisibility(View.VISIBLE);
                annotationTitleTextView.setText(getResources().getString(R.string.createAnnotationTextView));
                selectionAnnotationTypeTextView.setVisibility(View.VISIBLE);

                break;
            }
            case PlayingSound: {
                annotationTextView.setVisibility(View.INVISIBLE);
                seekBar.setVisibility(View.VISIBLE);
                soundPlayingManageButton.setVisibility(View.VISIBLE);
                soundPlayingManageButton.setText(getResources().getString(R.string.stopPlayButtonText));
                saveButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
                singleCancelButton.setVisibility(View.INVISIBLE);
                annotationTitleTextView.setText(currentAnnotation.getAtivity().getTitle());
                selectionAnnotationTypeTextView.setVisibility(View.INVISIBLE);
                break;
            }
            case ShowingSound: {
                annotationTextView.setVisibility(View.INVISIBLE);
                seekBar.setVisibility(View.VISIBLE);
                soundPlayingManageButton.setVisibility(View.VISIBLE);
                soundPlayingManageButton.setText(getResources().getString(R.string.startPlayButtonText));
                saveButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
                singleCancelButton.setVisibility(View.INVISIBLE);
                annotationTitleTextView.setText(currentAnnotation.getAtivity().getTitle());
                selectionAnnotationTypeTextView.setVisibility(View.INVISIBLE);
                break;
            }
            case ShowingText: {
                annotationTextView.setVisibility(View.VISIBLE);
                seekBar.setVisibility(View.INVISIBLE);
                soundPlayingManageButton.setVisibility(View.INVISIBLE);
                saveButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
                singleCancelButton.setVisibility(View.INVISIBLE);
                annotationTitleTextView.setText(getResources().getString(R.string.annotationText));
                selectionAnnotationTypeTextView.setVisibility(View.INVISIBLE);
                break;
            }
            default: break;
        }
    }
//endregion

//region OnClickMethod
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.soundRecordLayout: {
                LogFiles.writeButtonActionLog(Enums.ActivityType.CreateAnnotation, Enums.ButtonActionTypes.Voice);
                goToEditSoundActivity();
                break;
            }
            case R.id.soundPlayingManageButton: {
                startPlaying = !startPlaying;
                if (startPlaying) {
                    LogFiles.writeButtonActionLog(Enums.ActivityType.CreateAnnotation, Enums.ButtonActionTypes.Listen);
                    mediaPlayer.start();
                    contentType = Enums.ContentTypes.PlayingSound;
                } else {
                    LogFiles.writeButtonActionLog(Enums.ActivityType.CreateAnnotation, Enums.ButtonActionTypes.Pause);
                    mediaPlayer.pause();
                    contentType = Enums.ContentTypes.ShowingSound;
                }
                rulesForShowComponents();
                break;
            }
            case R.id.annotationTextView:
            case R.id.writeTextLayout: {
                LogFiles.writeButtonActionLog(Enums.ActivityType.CreateAnnotation, Enums.ButtonActionTypes.Text);
                goToEditTextActivity();
                break;
            }
            case R.id.createAlarmButton: {
                LogFiles.writeButtonActionLog(Enums.ActivityType.CreateAnnotation, Enums.ButtonActionTypes.CreateAlarm);
                goToEditAlarmActivity();
               break;
            }

            case R.id.saveButton: {
                LogFiles.writeButtonActionLog(Enums.ActivityType.CreateAnnotation, Enums.ButtonActionTypes.Save);
                if(contentType != Enums.ContentTypes.Blank){
                    pauseSoundIfNeeded();

                    if(currentAnnotation.getOperationType() == Enums.OperationType.Create){
                        currentAnnotation.setId(dataBaseHandler.insertAnnotation(currentAnnotation));
                    } else { dataBaseHandler.updateAnnotation(currentAnnotation); }

                    if(alarmType == Enums.AlarmTypes.Created){
                        AlarmEntity.createAlarm(getApplicationContext(), currentAnnotation);
                    }
                    Toast.makeText(this, getResources().getString(R.string.saveSuccessAnnotationToastText), Toast.LENGTH_LONG).show();
                    goToMainActivity();
                } else {
                    callMessageDialog(getResources().getString(R.string.emptyContentErroTitleDialogText),
                            getResources().getString(R.string.emptyContentErroMessageDialogText),
                            Enums.MessageTypes.ErrorMessage);
                }
                break;
            }
            case R.id.singleCancelButton:
            case R.id.cancelButton: {
                LogFiles.writeButtonActionLog(Enums.ActivityType.CreateAnnotation, Enums.ButtonActionTypes.Cancel);
                pauseSoundIfNeeded();
                goToMainActivity();
                break;
            }
            default: break;
        }
    }
//endregion

//region ListenerMethods

    public void onMessageDialogPositiveClick(Enums.MessageTypes messageType) { }

    public void onMessageDialogNegativeClick(Enums.MessageTypes messageType){
        if(messageType == Enums.MessageTypes.DecisionMessage) {
            alarmType = Enums.AlarmTypes.New;
            rulesForShowAlarm();
        }
    }

//endregion

//region Miscellaneous
    Runnable run = new Runnable() {
        @Override public void run() { seekUpdation(); }
    };

    private void seekUpdation() {
        if(mediaPlayer != null) seekBar.setProgress(mediaPlayer.getCurrentPosition());
        seekHandler.postDelayed(run, 100);
    }

    private void releaseMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void pauseSoundIfNeeded(){
        if (contentType == Enums.ContentTypes.PlayingSound) {
        startPlaying = !startPlaying;
        mediaPlayer.pause();
        contentType = Enums.ContentTypes.ShowingSound;
        rulesForShowComponents();
        }
    }

    private void goToMainActivity(){
        releaseMedia();
        LogFiles.writeAnnotationsLog(currentAnnotation);
        currentAnnotation = null;
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void goToEditAlarmActivity(){
        Intent intent = new Intent(getApplicationContext(), EditAlarmActivity.class);
        intent.putExtra("Annotation", currentAnnotation);
        startActivity(intent);
    }

    private void goToEditTextActivity(){
        Intent intent = new Intent(getApplicationContext(), EditTextActivity.class);
        intent.putExtra("Annotation", currentAnnotation);
        startActivity(intent);
    }

    private void goToEditSoundActivity(){
        Intent intent = new Intent(getApplicationContext(), EditSoundActivity.class);
        intent.putExtra("Annotation", currentAnnotation);
        startActivity(intent);
    }

    private void callMessageDialog(String title, String message, Enums.MessageTypes messageType){
        Bundle messageBundle = new Bundle();
        messageBundle.putSerializable(MessageDialogFragment.TYPE, messageType);
        messageBundle.putString(MessageDialogFragment.TITLE, title);
        messageBundle.putString(MessageDialogFragment.MESSAGE, message);
        dialogFragment = new MessageDialogFragment();
        dialogFragment.setArguments(messageBundle);
        dialogFragment.show(getSupportFragmentManager(), "message_dialog");
    }
//endregion
}