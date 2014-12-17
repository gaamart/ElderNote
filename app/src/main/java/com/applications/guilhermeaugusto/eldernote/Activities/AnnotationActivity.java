package com.applications.guilhermeaugusto.eldernote.Activities;

import android.support.v4.app.DialogFragment;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.util.Log;
import android.media.MediaPlayer;
import android.widget.LinearLayout;
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
    private LinearLayout titleLayout;
    private Button soundPlayingManageButton;
    private Button createAlarmButton;
    private TextView alarmDateTextView;
    private TextView alarmCycleDescriptionTextView;
    private TextView alarmTitleTextView;
    private TextView annotationTextView;
    private TextView titleTextView;
    private Button saveButton;
    private Button cancelButton;
//endregion

//region ActivityMethods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annotation);
        dataBaseHandler = new DataBaseHandler(getApplicationContext());
        getInit();
        rulesForAddDataToComponents();
        rulesForShowOperation();
        rulesForShowComponents();
        rulesForShowAlarm();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseSoundIfNeeded();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMedia();
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
        titleLayout = (LinearLayout) findViewById(R.id.titleLayout);
        soundPlayingManageButton = (Button) findViewById(R.id.soundPlayingManageButton);
        annotationTextView = (TextView) findViewById(R.id.annotationTextView);
        titleTextView = (TextView) findViewById(R.id.titleTextView);
        createAlarmButton = (Button) findViewById(R.id.createAlarmButton);
        alarmDateTextView = (TextView) findViewById(R.id.alarmDateTextView);
        alarmCycleDescriptionTextView = (TextView) findViewById(R.id.alarmCycleDescriptionTextView);
        alarmTitleTextView = (TextView) findViewById(R.id.alarmTitleTextView);
        saveButton = (Button) findViewById(R.id.saveButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);
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
            titleTextView.setText(currentAnnotation.getAtivity().getTitle());
            contentType = Enums.ContentTypes.ShowingSound;
            prepareToPlayingSound();
            prepareSeekBar();
        }
        else { contentType = Enums.ContentTypes.Blank; }

        if(currentAnnotation.getAlarm().getId() >= 0) { alarmType = Enums.AlarmTypes.Created; }
        else {
            if(currentAnnotation.getAlarm().getDateInMillis() != null) { alarmType = Enums.AlarmTypes.New; }
            else {
                if (contentType == Enums.ContentTypes.Blank) { alarmType = Enums.AlarmTypes.Hidden; }
                else { alarmType = Enums.AlarmTypes.Selectable; }
            }
        }
    }

    private void rulesForShowOperation(){
        switch (currentAnnotation.getOperationType()){
            case Create: {
                setTitle(getResources().getString(R.string.createAnnotationTextView));
                saveButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
                break;
            }
            case Update: {
                setTitle(getResources().getString(R.string.updateAnnotationTextView));
                saveButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
                break;
            }
            default: break;
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
            case Created:
            case New: {
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
        titleLayout.setVisibility(View.INVISIBLE);

        switch (contentType){
            case Blank: {
                writeTextLayout.setVisibility(View.VISIBLE);
                soundRecordLayout.setVisibility(View.VISIBLE);
                annotationTextView.setVisibility(View.INVISIBLE);
                seekBar.setVisibility(View.INVISIBLE);
                soundPlayingManageButton.setVisibility(View.INVISIBLE);
                break;
            }
            case PlayingSound: {
                annotationTextView.setVisibility(View.INVISIBLE);
                seekBar.setVisibility(View.VISIBLE);
                soundPlayingManageButton.setVisibility(View.VISIBLE);
                soundPlayingManageButton.setText(getResources().getString(R.string.stopPlayButtonText));
                titleLayout.setVisibility(View.VISIBLE);
                break;
            }
            case ShowingSound: {
                annotationTextView.setVisibility(View.INVISIBLE);
                seekBar.setVisibility(View.VISIBLE);
                soundPlayingManageButton.setVisibility(View.VISIBLE);
                soundPlayingManageButton.setText(getResources().getString(R.string.startPlayButtonText));
                titleLayout.setVisibility(View.VISIBLE);
                break;
            }
            case ShowingText: {
                annotationTextView.setVisibility(View.VISIBLE);
                seekBar.setVisibility(View.INVISIBLE);
                soundPlayingManageButton.setVisibility(View.INVISIBLE);
                titleLayout.setVisibility(View.INVISIBLE);
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
                goToEditSoundActivity();
                break;
            }
            case R.id.soundPlayingManageButton: {
                startPlaying = !startPlaying;
                if (startPlaying) {
                    mediaPlayer.start();
                    contentType = Enums.ContentTypes.PlayingSound;
                } else {
                    mediaPlayer.pause();
                    contentType = Enums.ContentTypes.ShowingSound;
                }
                rulesForShowComponents();
                break;
            }
            //case R.id.refreshContentButton: {
            //    if(contentType == Enums.ContentTypes.ShowingSound) {
            //        goToEditSoundActivity();
            //    } else {
            //        goToEditTextActivity();
            //    }
            //    break;
            //}
            case R.id.annotationTextView:
            case R.id.writeTextLayout: {
                goToEditTextActivity();
                break;
            }
            case R.id.createAlarmButton: {
                goToEditAlarmActivity();
               break;
            }
            //case R.id.deleteAlarmButton:{
            //    callMessageDialog(getResources().getString(R.string.deleteAlarmTitleDialogText),
            //            getResources().getString(R.string.deleteAlarmMessageDialogText),
            //            Enums.MessageTypes.DeleteMessage);
            //    break;
            //}

            case R.id.saveButton: {
                if(contentType != Enums.ContentTypes.Blank){
                    pauseSoundIfNeeded();

                    if(currentAnnotation.getOperationType() == Enums.OperationType.Create){
                        currentAnnotation.setId(dataBaseHandler.insertAnnotation(currentAnnotation));
                    } else { dataBaseHandler.updateAnnotation(currentAnnotation); }

                    if(alarmType == Enums.AlarmTypes.New){
                        AlarmEntity.createAlarm(getApplicationContext(), currentAnnotation);
                    }
                    LogFiles.writeAnnotationsLog(currentAnnotation);
                    Toast.makeText(this, getResources().getString(R.string.saveSuccessAnnotationToastText), Toast.LENGTH_LONG).show();
                    goToMainActivity();
                } else {
                    callMessageDialog(getResources().getString(R.string.emptyContentErroTitleDialogText),
                            getResources().getString(R.string.emptyContentErroMessageDialogText),
                            Enums.MessageTypes.ErrorMessage);
                }
                break;
            }
            case R.id.okButton:
            case R.id.cancelButton: {
                pauseSoundIfNeeded();
                goToMainActivity();
                break;
            }
            default: break;
        }
    }
//endregion

//region ListenerMethods

    public void onMessageDialogPositiveClick(Enums.MessageTypes messageType) {
        if(messageType == Enums.MessageTypes.DeleteMessage){
            deleteAlarm();
        }
    }

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

//region deleteMethods
    private void deleteAlarm(){
        alarmType = Enums.AlarmTypes.Selectable;
        rulesForShowAlarm();
        AlarmEntity.removeAlarm(getApplicationContext(), currentAnnotation.getAlarm().getId());
        currentAnnotation.getAlarm().setId(-1);
        currentAnnotation.getAlarm().setDateInMillis(null);
        currentAnnotation.getAlarm().setCyclePeriod(Enums.PeriodTypes.None);
    }
//endregion
}