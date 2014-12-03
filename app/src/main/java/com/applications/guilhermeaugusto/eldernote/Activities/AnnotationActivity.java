package com.applications.guilhermeaugusto.eldernote.Activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.support.v4.app.DialogFragment;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.util.Log;
import android.media.MediaPlayer;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.applications.guilhermeaugusto.eldernote.Dialogs.SoundDialogFragment;
import com.applications.guilhermeaugusto.eldernote.Dialogs.CycleAlarmDialogFragment;
import com.applications.guilhermeaugusto.eldernote.Dialogs.MessageDialogFragment;
import com.applications.guilhermeaugusto.eldernote.Dialogs.SoundTitleDialogFragment;
import com.applications.guilhermeaugusto.eldernote.Managers.AlarmEntity;
import com.applications.guilhermeaugusto.eldernote.Managers.DataBaseHandler;
import com.applications.guilhermeaugusto.eldernote.Dialogs.DatePickerFragment;
import com.applications.guilhermeaugusto.eldernote.Dialogs.TimePickerFragment;
import com.applications.guilhermeaugusto.eldernote.Managers.SoundFiles;
import com.applications.guilhermeaugusto.eldernote.R;
import com.applications.guilhermeaugusto.eldernote.beans.Activities;
import com.applications.guilhermeaugusto.eldernote.beans.Alarms;
import com.applications.guilhermeaugusto.eldernote.beans.Annotations;
import com.applications.guilhermeaugusto.eldernote.beans.Enums;

import java.io.IOException;
import java.util.Calendar;
import java.util.Random;

public class AnnotationActivity extends FragmentActivity implements DatePickerDialog.OnDateSetListener,
                                                                    TimePickerDialog.OnTimeSetListener,
                                                                    SoundDialogFragment.SoundDialogFragmentListener,
                                                                    CycleAlarmDialogFragment.CycleAlarmFragmentListener,
                                                                    MessageDialogFragment.MessageFragmentListener,
                                                                    SoundTitleDialogFragment.SoundTitleFragmentListener{
//region Variables
    public Annotations currentAnnotation;
    private Alarms currentAlarm;
    private static final String LOG_TAG = "AnnotationActivityLog";
    private boolean startPlaying;
    private Handler seekHandler;
    private MediaPlayer mediaPlayer;
    private DataBaseHandler dataBaseHandler;
    private DialogFragment dialogFragment;
    private Enums.ContentTypes contentType;
    private Enums.AlarmTypes alarmType;
    private String soundBuffer;

    private SeekBar seekBar;
    private RelativeLayout writeTextLayout;
    private RelativeLayout soundRecordLayout;
    private LinearLayout titleLayout;
    private Button soundPlayingManageButton;
    private Button createAlarmButton;
    private RelativeLayout deleteAlarmLayout;
    private TextView alarmDateTextView;
    private TextView alarmCycleDescriptionTextView;
    private TextView alarmTitleTextView;
    private RelativeLayout refreshContentLayout;
    private TextView annotationTextView;
    private TextView titleTextView;
    private Button saveButton;
    private Button cancelButton;
    private Button okButton;
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
        if (contentType == Enums.ContentTypes.RecordingSound) { dialogFragment.getDialog().dismiss(); }
        if (currentAlarm.getPlayRingtone()){ dialogFragment.getDialog().dismiss(); }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMedia();
    }
//endregion

//region InitializationMethods
    public AnnotationActivity() {
        currentAlarm = new Alarms();
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
        refreshContentLayout = (RelativeLayout) findViewById(R.id.refreshContentLayout);
        annotationTextView = (TextView) findViewById(R.id.annotationTextView);
        titleTextView = (TextView) findViewById(R.id.titleTextView);
        createAlarmButton = (Button) findViewById(R.id.createAlarmButton);
        deleteAlarmLayout = (RelativeLayout) findViewById(R.id.deleteAlarmLayout);
        alarmDateTextView = (TextView) findViewById(R.id.alarmDateTextView);
        alarmCycleDescriptionTextView = (TextView) findViewById(R.id.alarmCycleDescriptionTextView);
        alarmTitleTextView = (TextView) findViewById(R.id.alarmTitleTextView);
        saveButton = (Button) findViewById(R.id.saveButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);
        okButton = (Button) findViewById(R.id.okButton);
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

        if(currentAnnotation.getMessage() != null && !currentAnnotation.getMessage().isEmpty()){
            contentType = Enums.ContentTypes.ShowingText;
            annotationTextView.setText(currentAnnotation.getMessage());
        }
        else if(currentAnnotation.getSound() != null && !currentAnnotation.getSound().isEmpty()){
            titleTextView.setText(currentAnnotation.getAtivity().getTitle());
            contentType = Enums.ContentTypes.ShowingSound;
            prepareToPlayingSound();
            prepareSeekBar();
        }
        else { contentType = Enums.ContentTypes.Blank; }

        currentAlarm.setId(currentAnnotation.getAlarm().getId());
        currentAlarm.setDateInMillis(currentAnnotation.getAlarm().getDateInMillis());
        currentAlarm.setPlayRingnote(currentAnnotation.getAlarm().getPlayRingtone());
        currentAlarm.setCyclePeriod(currentAnnotation.getAlarm().getCyclePeriod());
        if(currentAlarm.getId() >= 0) { alarmType = Enums.AlarmTypes.Created; }
        else {
            if (contentType == Enums.ContentTypes.Blank){ alarmType = Enums.AlarmTypes.Hidden; }
            else alarmType = Enums.AlarmTypes.Selectable;
        }
    }

    private void rulesForShowOperation(){
        switch (currentAnnotation.getOperationType()){
            case Create: {
                setTitle(getResources().getString(R.string.createAnnotationTextView));
                saveButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
                okButton.setVisibility(View.INVISIBLE);
                break;
            }
            case Update: {
                setTitle(getResources().getString(R.string.updateAnnotationTextView));
                saveButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
                okButton.setVisibility(View.INVISIBLE);
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
                deleteAlarmLayout.setVisibility(View.INVISIBLE);
                alarmDateTextView.setVisibility(View.INVISIBLE);
                alarmCycleDescriptionTextView.setVisibility(View.INVISIBLE);
                break;
            }
            case Created:
            case New: {
                createAlarmButton.setVisibility(View.INVISIBLE);
                deleteAlarmLayout.setVisibility(View.VISIBLE);
                alarmCycleDescriptionTextView.setText(currentAlarm.createPeriodLayout(getApplicationContext()));
                alarmDateTextView.setText(currentAlarm.createDateLayout(getApplicationContext()));
                alarmDateTextView.setVisibility(View.VISIBLE);
                alarmCycleDescriptionTextView.setVisibility(View.VISIBLE);
                alarmTitleTextView.setVisibility(View.VISIBLE);
                break;
            }
            case Hidden:{
                alarmTitleTextView.setVisibility(View.INVISIBLE);
                createAlarmButton.setVisibility(View.INVISIBLE);
                deleteAlarmLayout.setVisibility(View.INVISIBLE);
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
                refreshContentLayout.setVisibility(View.INVISIBLE);
                break;
            }
            case PlayingSound: {
                annotationTextView.setVisibility(View.INVISIBLE);
                seekBar.setVisibility(View.VISIBLE);
                soundPlayingManageButton.setVisibility(View.VISIBLE);
                refreshContentLayout.setVisibility(View.INVISIBLE);
                soundPlayingManageButton.setText(getResources().getString(R.string.stopPlayButtonText));
                titleLayout.setVisibility(View.VISIBLE);
                break;
            }
            case ShowingSound: {
                annotationTextView.setVisibility(View.INVISIBLE);
                seekBar.setVisibility(View.VISIBLE);
                soundPlayingManageButton.setVisibility(View.VISIBLE);
                refreshContentLayout.setVisibility(View.VISIBLE);
                soundPlayingManageButton.setText(getResources().getString(R.string.startPlayButtonText));
                titleLayout.setVisibility(View.VISIBLE);
                break;
            }
            case ShowingText: {
                annotationTextView.setVisibility(View.VISIBLE);
                seekBar.setVisibility(View.INVISIBLE);
                soundPlayingManageButton.setVisibility(View.INVISIBLE);
                refreshContentLayout.setVisibility(View.VISIBLE);
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
                contentType = Enums.ContentTypes.RecordingSound;
                dialogFragment = new SoundDialogFragment();
                dialogFragment.show(getSupportFragmentManager(), "audio");
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
            case R.id.refreshContentButton: {
                if(contentType == Enums.ContentTypes.ShowingSound) {
                    contentType = Enums.ContentTypes.RecordingSound;
                    dialogFragment = new SoundDialogFragment();
                    dialogFragment.show(getSupportFragmentManager(), "audio");
                } else {
                    goToEditTextActivity();
                }
                break;
            }
            case R.id.annotationTextView:
            case R.id.writeTextLayout: {
                goToEditTextActivity();
                break;
            }
            case R.id.createAlarmButton: {
                Bundle b = new Bundle();
                if(alarmType != Enums.AlarmTypes.Selectable) {
                    currentAlarm.createDateConponentesByTimeInMillis();
                    b.putInt(DatePickerFragment.YEAR, currentAlarm.getYear());
                    b.putInt(DatePickerFragment.MONTH, currentAlarm.getMonth());
                    b.putInt(DatePickerFragment.DATE, currentAlarm.getMinute());
                } else {
                    Calendar c = Calendar.getInstance();
                    b.putInt(DatePickerFragment.YEAR, c.get(Calendar.YEAR));
                    b.putInt(DatePickerFragment.MONTH, c.get(Calendar.MONTH));
                    b.putInt(DatePickerFragment.DATE, c.get(Calendar.DAY_OF_MONTH));
                }
                dialogFragment = new DatePickerFragment();
                dialogFragment.setArguments(b);
                dialogFragment.show(getSupportFragmentManager(), "frag_date_picker");
               break;
            }
            case R.id.deleteAlarmButton:{
                callMessageDialog(getResources().getString(R.string.deleteAlarmTitleDialogText),
                        getResources().getString(R.string.deleteAlarmMessageDialogText),
                        Enums.MessageTypes.DeleteMessage);
                break;
            }

            case R.id.saveButton: {
                if(contentType != Enums.ContentTypes.Blank){
                    pauseSoundIfNeeded();
                    currentAnnotation.setMessage(annotationTextView.getText().toString());
                    currentAnnotation.setAlarm(currentAlarm);

                    if(currentAnnotation.getOperationType() == Enums.OperationType.Create){
                        currentAnnotation.setId(dataBaseHandler.insertAnnotation(currentAnnotation));
                    } else { dataBaseHandler.updateAnnotation(currentAnnotation); }

                    if(alarmType == Enums.AlarmTypes.New){
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
    public void onDateSet(DatePicker view, int year, int month, int day) {
        currentAlarm.setYear(year);
        currentAlarm.setMonth(month);
        currentAlarm.setDay(day);

        Bundle timePickerBundle = new Bundle();
        if(alarmType != Enums.AlarmTypes.Selectable) {
            timePickerBundle.putInt(TimePickerFragment.HOUR, currentAlarm.getHour());
            timePickerBundle.putInt(TimePickerFragment.MINUTE, currentAlarm.getMinute());
        } else {
            Calendar c = Calendar.getInstance();
            timePickerBundle.putInt(TimePickerFragment.HOUR, c.get(Calendar.HOUR_OF_DAY));
            timePickerBundle.putInt(TimePickerFragment.MINUTE, c.get(Calendar.MINUTE));
        }

        Long currentTime = System.currentTimeMillis() - (60000 * 60 * 24);
        currentAlarm.createTimeInMillis();
        if (currentTime < Long.parseLong(currentAlarm.getDateInMillis())) {
            dialogFragment = new TimePickerFragment();
            dialogFragment.setArguments(timePickerBundle);
            dialogFragment.show(getSupportFragmentManager(), "frag_time_picker");
        } else{
            callMessageDialog(getResources().getString(R.string.olderAlarmErroTitleDialogText),
                    getResources().getString(R.string.olderAlarmErroMessageDialogText),
                    Enums.MessageTypes.ErrorMessage);
        }
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        currentAlarm.setHour(hourOfDay);
        currentAlarm.setMinute(minute);
        currentAlarm.createTimeInMillis();
        Long currentTime = System.currentTimeMillis() - 60000;
        if (currentTime < Long.parseLong(currentAlarm.getDateInMillis())) {
            currentAlarm.setId(randInt());
            callMessageDialog(getResources().getString(R.string.cycleAlarmTitleDialogText),
                    getResources().getString(R.string.cycleAlarmMessageDialogText),
                    Enums.MessageTypes.DecisionMessage);
        } else{
            callMessageDialog(getResources().getString(R.string.olderAlarmErroTitleDialogText),
                    getResources().getString(R.string.olderAlarmErroMessageDialogText),
                    Enums.MessageTypes.ErrorMessage);
        }
    }

    public void onSoundDialogPositiveClick(String soundCreated){
        soundBuffer = soundCreated;
        dialogFragment = new SoundTitleDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "frag_sound_title");
    }

    public void onSoundDialogNegativeClick(){ returnSoundToLastState(); }

    public void onSoundTitleDialogPositiveClick(Activities activity){
        deleteOldSoundContent();
        titleTextView.setText(activity.getTitle());
        currentAnnotation.setActivity(activity);
        currentAnnotation.setSound(soundBuffer);
        contentType = Enums.ContentTypes.ShowingSound;
        prepareToPlayingSound();
        prepareSeekBar();
        rulesForShowComponents();

        if(alarmType == Enums.AlarmTypes.Hidden) {
            alarmType = Enums.AlarmTypes.Selectable;
            rulesForShowAlarm();
        }
    }
    public void onSoundTitleDialogNegativeClick(){
        returnSoundToLastState();
        soundBuffer = null;
    }

    public void onCycleAlarmDialogPositiveClick(Enums.PeriodTypes period) {
        currentAlarm.setCyclePeriod(period);
        alarmType = Enums.AlarmTypes.New;
        rulesForShowAlarm();
    }

    public void onCycleAlarmDialogNegativeClick() {
        currentAlarm.setCyclePeriod(Enums.PeriodTypes.None);
    }

    public void onMessageDialogPositiveClick(Enums.MessageTypes messageType) {
        if(messageType == Enums.MessageTypes.DeleteMessage){
            deleteAlarm();
        } else {
            dialogFragment = new CycleAlarmDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), "cycle_alarm_picker");
        }
    }

    public void onMessageDialogNegativeClick(Enums.MessageTypes messageType){
        if(messageType == Enums.MessageTypes.DecisionMessage) {
            currentAlarm.setCyclePeriod(Enums.PeriodTypes.None);
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

    private static int randInt() {
        Random rand = new Random() ;
        return rand.nextInt((2000000) + 1);
    }

    private void releaseMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void returnSoundToLastState(){
        if(currentAnnotation.getSound() == null || currentAnnotation.getSound().isEmpty()){ contentType = Enums.ContentTypes.Blank; }
        else { contentType = Enums.ContentTypes.ShowingSound; }
        rulesForShowComponents();
    }

    private void goToMainActivity(){
        releaseMedia();
        currentAnnotation = null;
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void pauseSoundIfNeeded(){
        if (contentType == Enums.ContentTypes.PlayingSound) {
        startPlaying = !startPlaying;
        mediaPlayer.pause();
        contentType = Enums.ContentTypes.ShowingSound;
        rulesForShowComponents();
        }
    }

    private void goToEditTextActivity(){
        Intent intent = new Intent(getApplicationContext(), EditTextActivity.class);
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
        AlarmEntity.removeAlarm(getApplicationContext(), currentAlarm.getId());
        currentAlarm.setId(-1);
        currentAlarm.setDateInMillis(null);
        currentAlarm.setCyclePeriod(Enums.PeriodTypes.None);
    }

    private void deleteOldSoundContent(){
        if(SoundFiles.removeOutputFile(currentAnnotation.getSound())){
            currentAnnotation.setSound(null);
            releaseMedia();
        }
    }
//endregion
}