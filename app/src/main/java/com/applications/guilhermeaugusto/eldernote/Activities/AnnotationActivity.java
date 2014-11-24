package com.applications.guilhermeaugusto.eldernote.Activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.view.View;
import android.util.Log;
import android.media.MediaPlayer;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.applications.guilhermeaugusto.eldernote.Dialogs.AudioDialogFragment;
import com.applications.guilhermeaugusto.eldernote.Dialogs.CycleAlarmDialogFragment;
import com.applications.guilhermeaugusto.eldernote.Managers.AlarmEntity;
import com.applications.guilhermeaugusto.eldernote.Managers.DataBaseHandler;
import com.applications.guilhermeaugusto.eldernote.Dialogs.TextDialogFragment;
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
                                                                    AudioDialogFragment.SoundDialogFragmentListener,
                                                                    CycleAlarmDialogFragment.CycleAlarmFragmentListener{
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

    private SeekBar seekBar;
    private RelativeLayout writeTextLayout;
    private RelativeLayout soundRecordLayout;
    private Button soundPlayingManageButton;
    private Button createAlarmButton;
    private RelativeLayout deleteAlarmLayout;
    private TextView alarmDateTextView;
    private TextView alarmCycleDescriptionTextView;
    private TextView alarmTitleTextView;
    private RelativeLayout refreshContentLayout;
    private TextView annotationTextView;
    private Spinner spinner;
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
        if (contentType == Enums.ContentTypes.EditingText) { dialogFragment.getDialog().dismiss(); }
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
        soundPlayingManageButton = (Button) findViewById(R.id.soundPlayingManageButton);
        refreshContentLayout = (RelativeLayout) findViewById(R.id.refreshContentLayout);
        annotationTextView = (TextView) findViewById(R.id.annotationTextView);
        spinner = (Spinner) findViewById(R.id.activitiesSpinner);
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
    private void prepareSpinner(){
        ArrayAdapter<Activities> dataAdapter = new ArrayAdapter<Activities>(this,
                android.R.layout.simple_spinner_item,
                dataBaseHandler.selectAllActivities());
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
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
        prepareSpinner();

        if(currentAnnotation.getOperationType() == Enums.OperationType.Update){
            spinner.setSelection(currentAnnotation.getAtivity().getId() - 1);
        }

        if(currentAnnotation.getMessage() != null && !currentAnnotation.getMessage().isEmpty()){
            contentType = Enums.ContentTypes.ShowingText;
            annotationTextView.setText(currentAnnotation.getMessage());
        }
        else if(currentAnnotation.getSound() != null && !currentAnnotation.getSound().isEmpty()){
            contentType = Enums.ContentTypes.ShowingSound;
            prepareToPlayingSound();
            prepareSeekBar();
        }
        else { contentType = Enums.ContentTypes.Blank; }

        currentAlarm.setId(currentAnnotation.getAlarm().getId());
        currentAlarm.setDateInMillis(currentAnnotation.getAlarm().getDateInMillis());
        currentAlarm.setPlayRingnote(currentAnnotation.getAlarm().getPlayRingtone());
        currentAlarm.setCyclePeriod(currentAnnotation.getAlarm().getCyclePeriod());
        currentAlarm.setCycleTime(currentAnnotation.getAlarm().getCycleTime());
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
                break;
            }
            case ShowingSound: {
                annotationTextView.setVisibility(View.INVISIBLE);
                seekBar.setVisibility(View.VISIBLE);
                soundPlayingManageButton.setVisibility(View.VISIBLE);
                refreshContentLayout.setVisibility(View.VISIBLE);
                soundPlayingManageButton.setText(getResources().getString(R.string.startPlayButtonText));
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
                dialogFragment = new AudioDialogFragment();
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
                    dialogFragment = new AudioDialogFragment();
                    dialogFragment.show(getSupportFragmentManager(), "audio");
                } else {
                    callTextFragmentDialog();
                }
                break;
            }
            case R.id.annotationTextView:
            case R.id.writeTextLayout: {
                callTextFragmentDialog();
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
                callDeleteDialog(getResources().getString(R.string.deleteAlarmTitleDialogText),
                        getResources().getString(R.string.deleteAlarmMessageDialogText),
                        Enums.DeleteTypes.Alarm);
                break;
            }

            case R.id.saveButton: {
                if(contentType != Enums.ContentTypes.Blank){
                    pauseSoundIfNeeded();
                    Activities activity = (Activities) ( (Spinner) findViewById(R.id.activitiesSpinner) ).getSelectedItem();
                    currentAnnotation.setMessage(annotationTextView.getText().toString());
                    currentAnnotation.setActivity(activity);
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
                    callErroDialog(getResources().getString(R.string.emptyContentErroTitleDialogText),
                            getResources().getString(R.string.emptyContentErroMessageDialogText));
                }
                break;
            }
            case R.id.okButton:
            case R.id.cancelButton: {
                pauseSoundIfNeeded();
                goToMainActivity();
                break;
            }
            case R.id.deleteAnnotationButton:{
                pauseSoundIfNeeded();
                callDeleteDialog(getResources().getString(R.string.deleteAnnotationTitleDialogText),
                        getResources().getString(R.string.deleteAnnotationMessageDialogText),
                        Enums.DeleteTypes.Annotation);
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

        Bundle b = new Bundle();
        if(alarmType != Enums.AlarmTypes.Selectable) {
            b.putInt(TimePickerFragment.HOUR, currentAlarm.getHour());
            b.putInt(TimePickerFragment.MINUTE, currentAlarm.getMinute());
        } else {
            Calendar c = Calendar.getInstance();
            b.putInt(TimePickerFragment.HOUR, c.get(Calendar.HOUR_OF_DAY));
            b.putInt(TimePickerFragment.MINUTE, c.get(Calendar.MINUTE));
        }

        Long currentTime = System.currentTimeMillis() - (60000 * 60 * 24);
        currentAlarm.createTimeInMillis();
        if (currentTime < Long.parseLong(currentAlarm.getDateInMillis())) {
            dialogFragment = new TimePickerFragment();
            dialogFragment.setArguments(b);
            dialogFragment.show(getSupportFragmentManager(), "frag_time_picker");
        } else{
            callErroDialog(getResources().getString(R.string.olderAlarmErroTitleDialogText),
                    getResources().getString(R.string.olderAlarmErroMessageDialogText));
        }
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        currentAlarm.setHour(hourOfDay);
        currentAlarm.setMinute(minute);
        currentAlarm.createTimeInMillis();
        Long currentTime = System.currentTimeMillis() - 60000;
        if (currentTime < Long.parseLong(currentAlarm.getDateInMillis())) {
            currentAlarm.setId(randInt());
            callCycleAlarmDialog();
        } else{
            callErroDialog(getResources().getString(R.string.olderAlarmErroTitleDialogText),
                    getResources().getString(R.string.olderAlarmErroMessageDialogText));
        }
    }

    public void onSoundDialogPositiveClick(String soundCreated){
        deleteOldSoundContent();
        currentAnnotation.setSound(soundCreated);
        contentType = Enums.ContentTypes.ShowingSound;
        prepareToPlayingSound();
        prepareSeekBar();
        rulesForShowComponents();

        if(alarmType == Enums.AlarmTypes.Hidden) {
            alarmType = Enums.AlarmTypes.Selectable;
            rulesForShowAlarm();
        }
    }

    public void onSoundDialogNegativeClick(){ returnSoundToLastState(); }

    public void onCycleAlarmDialogPositiveClick(int time, Enums.PeriodTypes period) {
        currentAlarm.setCycleTime(time);
        currentAlarm.setCyclePeriod(period);
        alarmType = Enums.AlarmTypes.New;
        rulesForShowAlarm();
    }

    public void onCycleAlarmDialogNegativeClick() {
        currentAlarm.setCycleTime(-1);
        currentAlarm.setCyclePeriod(Enums.PeriodTypes.None);
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

    private void callTextFragmentDialog(){
        //annotation.setOperationType(Enums.OperationType.Visualize);
        Intent intent = new Intent(getApplicationContext(), EditTextActivity.class);
        intent.putExtra("Annotation", currentAnnotation);
        startActivity(intent);
        //contentType = Enums.ContentTypes.EditingText;
        //Bundle b = new Bundle();
        //b.putString(TextDialogFragment.TEXT, currentAnnotation.getMessage());
        //dialogFragment = new TextDialogFragment();
        //dialogFragment.setArguments(b);
        //dialogFragment.show(getSupportFragmentManager(), "text");
    }

    private void callDeleteDialog(String title, String message, final Enums.DeleteTypes deleteType){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AnnotationActivity.this);
        alertDialogBuilder.setMessage(message)
                .setTitle(title)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.deletePositiveButtonDialogText), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        switch (deleteType) {
                            case Annotation: { deleteAnnotation(); break; }
                            case Alarm: { deleteAlarm(); break; }
                            default: break;
                        }}})
                .setNegativeButton(getResources().getString(R.string.deleteNegativeButtonDialogText), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) { dialog.cancel(); }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void callErroDialog(String title, String message){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AnnotationActivity.this);
        alertDialogBuilder.setMessage(message)
                .setTitle(title)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.doneText), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { dialog.cancel(); }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void callCycleAlarmDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AnnotationActivity.this);
        alertDialogBuilder.setMessage(getResources().getString(R.string.cycleAlarmMessageDialogText))
                .setTitle(getResources().getString(R.string.cycleAlarmTitleDialogText))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.deletePositiveButtonDialogText), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialogFragment = new CycleAlarmDialogFragment();
                        dialogFragment.show(getSupportFragmentManager(), "cycle_alarm_picker");
                    }})
                .setNegativeButton(getResources().getString(R.string.deleteNegativeButtonDialogText), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        currentAlarm.setCycleTime(-1);
                        currentAlarm.setCyclePeriod(Enums.PeriodTypes.None);
                        alarmType = Enums.AlarmTypes.New;
                        rulesForShowAlarm();
                        dialog.cancel();
                    }});
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
//endregion

//region deleteMethods
    private void deleteAnnotation(){
        if(contentType == Enums.ContentTypes.ShowingSound) { SoundFiles.removeOutputFile(currentAnnotation.getSound()); }
        if(alarmType == Enums.AlarmTypes.Created) {
            AlarmEntity.removeAlarm(getApplicationContext(), currentAlarm.getId());
            currentAlarm.setId(-1);
            currentAlarm.setDateInMillis(null);
        }
        dataBaseHandler.deleteAnnotation(currentAnnotation);
        goToMainActivity();
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.deleteSuccessAnnotationToastText), Toast.LENGTH_LONG).show();
    }

    private void deleteAlarm(){
        alarmType = Enums.AlarmTypes.Selectable;
        rulesForShowAlarm();
        AlarmEntity.removeAlarm(getApplicationContext(), currentAlarm.getId());
        currentAlarm.setId(-1);
        currentAlarm.setDateInMillis(null);
        currentAlarm.setCycleTime(-1);
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