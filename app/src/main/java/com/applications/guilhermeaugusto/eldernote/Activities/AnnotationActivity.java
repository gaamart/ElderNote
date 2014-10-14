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
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.view.View;
import android.util.Log;
import android.media.MediaPlayer;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.applications.guilhermeaugusto.eldernote.Dialogs.AlarmRingtoneFragment;
import com.applications.guilhermeaugusto.eldernote.Dialogs.AudioDialogFragment;
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

public class AnnotationActivity extends FragmentActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener,
    TextDialogFragment.TextDialogFragmentListener, AudioDialogFragment.SoundDialogFragmentListener, AlarmRingtoneFragment.AlarmRingToneFragmentListener{



//region Variables
    public Annotations currentAnnotation;
    private Alarms currentAlarm;
    private static final String LOG_TAG = "AnnotationActivityLog";
    private boolean startPlaying;
    private Handler seekHandler;
    private MediaPlayer mediaPlayer;
    private DataBaseHandler dataBaseHandler;
    private InputMethodManager inputMethodManager;
    private DialogFragment dialogFragment;
    private Enums.ContentTypes contentType;
    private Enums.OperationType operationType;
    private Enums.AlarmTypes alarmType;

    private SeekBar seekBar;
    private RelativeLayout writeTextLayout;
    private RelativeLayout soundRecordLayout;
    private Button soundPlayingManageButton;
    private Button createAlarmButton;
    private ImageButton deleteAlarmButton;
    private TextView alarmDatetextView;
    private ImageButton refreshContentButton;
    private TextView createAnnotationTextView;
    private TextView annotationTextView;
    private Spinner spinner;
//endregion

//region ActivityMethods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annotation);
        dataBaseHandler = new DataBaseHandler(getApplicationContext());
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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
        refreshContentButton = (ImageButton) findViewById(R.id.refreshContentButton);
        annotationTextView = (TextView) findViewById(R.id.annotationTextView);
        spinner = (Spinner) findViewById(R.id.activitiesSpinner);
        createAlarmButton = (Button) findViewById(R.id.createAlarmButton);
        deleteAlarmButton = (ImageButton) findViewById(R.id.deleteAlarmButton);
        createAnnotationTextView = (TextView) findViewById(R.id.createAnnotationTextView);
        alarmDatetextView = (TextView) findViewById(R.id.alarmDatetextView);
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
        if(currentAnnotation.getId() >= 0) {
            spinner.setSelection(currentAnnotation.getAtivity().getId() - 1);
            operationType = Enums.OperationType.Update;
        } else { operationType = Enums.OperationType.Create; }

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
        if(currentAlarm.getId() > 0) {
            alarmType = Enums.AlarmTypes.Created;
            if(currentAlarm.getPlayRingtone()){
                dialogFragment = new AlarmRingtoneFragment();
                dialogFragment.show(getSupportFragmentManager(), "alarmRingtone");
            }
        }
        else { alarmType = Enums.AlarmTypes.Blank; }
    }

    private void rulesForShowOperation(){
        switch (operationType){
            case Create: {
                createAnnotationTextView.setText(getResources().getString(R.string.createAnnotationTextView));
                break;
            }
            case Update: {
                createAnnotationTextView.setText(getResources().getString(R.string.updateAnnotationTextView));
                break;
            }
            default: break;
        }
    }

    private void rulesForShowAlarm(){
        switch (alarmType){
            case Blank: {
                createAlarmButton.setVisibility(View.VISIBLE);
                deleteAlarmButton.setVisibility(View.INVISIBLE);
                alarmDatetextView.setVisibility(View.INVISIBLE);
                break;
            }
            case New: {
                createAlarmButton.setVisibility(View.INVISIBLE);
                deleteAlarmButton.setVisibility(View.VISIBLE);
                currentAlarm.createTimeInMillis();
                alarmDatetextView.setText(getResources().getString(R.string.createAlarmDateText) + currentAlarm.createDateLayout());
                alarmDatetextView.setVisibility(View.VISIBLE);
                break;
            }
            case Created:{
                createAlarmButton.setVisibility(View.INVISIBLE);
                deleteAlarmButton.setVisibility(View.VISIBLE);
                alarmDatetextView.setText(getResources().getString(R.string.createAlarmDateText) + currentAlarm.createDateLayout());
                alarmDatetextView.setVisibility(View.VISIBLE);
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
                refreshContentButton.setVisibility(View.INVISIBLE);
                break;
            }
            case PlayingSound: {
                annotationTextView.setVisibility(View.INVISIBLE);
                seekBar.setVisibility(View.VISIBLE);
                soundPlayingManageButton.setVisibility(View.VISIBLE);
                refreshContentButton.setVisibility(View.INVISIBLE);
                soundPlayingManageButton.setText(getResources().getString(R.string.stopPlayButtonText));
                break;
            }
            case ShowingSound: {
                annotationTextView.setVisibility(View.INVISIBLE);
                seekBar.setVisibility(View.VISIBLE);
                soundPlayingManageButton.setVisibility(View.VISIBLE);
                refreshContentButton.setVisibility(View.VISIBLE);
                soundPlayingManageButton.setText(getResources().getString(R.string.startPlayButtonText));
                break;
            }
            case ShowingText: {
                annotationTextView.setVisibility(View.VISIBLE);
                seekBar.setVisibility(View.INVISIBLE);
                soundPlayingManageButton.setVisibility(View.INVISIBLE);
                refreshContentButton.setVisibility(View.VISIBLE);
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
                if(alarmType != Enums.AlarmTypes.Blank) {
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

                    if(alarmType == Enums.AlarmTypes.New){
                        AlarmEntity.createAlarm(getApplicationContext(), currentAnnotation);
                    }

                    if(operationType == Enums.OperationType.Create){ dataBaseHandler.insertAnnotation(currentAnnotation); }
                    else { dataBaseHandler.updateAnnotation(currentAnnotation); }

                    Toast.makeText(this, getResources().getString(R.string.saveSuccessAnnotationToastText), Toast.LENGTH_LONG).show();
                    goToMainActivity();
                } else {
                    callErroDialog(getResources().getString(R.string.emptyContentErroTitleDialogText),
                            getResources().getString(R.string.emptyContentErroMessageDialogText));
                }
                break;
            }
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
        if(alarmType != Enums.AlarmTypes.Blank) {
            b.putInt(TimePickerFragment.HOUR, currentAlarm.getHour());
            b.putInt(TimePickerFragment.MINUTE, currentAlarm.getMinute());
        } else {
            Calendar c = Calendar.getInstance();
            b.putInt(TimePickerFragment.HOUR, c.get(Calendar.HOUR_OF_DAY));
            b.putInt(TimePickerFragment.MINUTE, c.get(Calendar.MINUTE));
        }

        dialogFragment = new TimePickerFragment();
        dialogFragment.setArguments(b);
        dialogFragment.show(getSupportFragmentManager(), "frag_time_picker");
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        currentAlarm.setHour(hourOfDay);
        currentAlarm.setMinute(minute);
        currentAlarm.createTimeInMillis();
        currentAlarm.setId(randInt());
        alarmType = Enums.AlarmTypes.New;
        rulesForShowAlarm();
    }

    public void onTextDialogPositiveClick(EditText editText){
        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        if(editText.getText().toString() != null && !editText.getText().toString().isEmpty()) {
            currentAnnotation.setMessage(editText.getText().toString());
            annotationTextView.setText(editText.getText().toString());
            contentType = Enums.ContentTypes.ShowingText;
            rulesForShowComponents();
        } else {
            returnTextToLastState();
            callErroDialog(getResources().getString(R.string.emptyTextErroTitleDialogText),
                    getResources().getString(R.string.emptyTextErroMessageDialogText));
        }
    }

    public void onTextDialogNegativeClick(EditText editText){
        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        returnTextToLastState();
    }

    public void onSoundDialogPositiveClick(String soundCreated){
        deleteOldSoundContent();
        currentAnnotation.setSound(soundCreated);
        contentType = Enums.ContentTypes.ShowingSound;
        prepareToPlayingSound();
        prepareSeekBar();
        rulesForShowComponents();
    }

    public void onSoundDialogNegativeClick(){ returnSoundToLastState(); }

    public void onAlarmRingtoneDialogClick(){
        currentAlarm.setPlayRingnote(false);
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

    private void returnTextToLastState(){
        if(currentAnnotation.getMessage() == null || currentAnnotation.getMessage().isEmpty()){ contentType = Enums.ContentTypes.Blank; }
        else {
            contentType = Enums.ContentTypes.ShowingText;
            annotationTextView.setText(currentAnnotation.getMessage());
        }
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
        contentType = Enums.ContentTypes.EditingText;
        Bundle b = new Bundle();
        b.putString(TextDialogFragment.TEXT, currentAnnotation.getMessage());
        dialogFragment = new TextDialogFragment();
        dialogFragment.setArguments(b);
        dialogFragment.show(getSupportFragmentManager(), "text");
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
        alarmType = Enums.AlarmTypes.Blank;
        rulesForShowAlarm();
        AlarmEntity.removeAlarm(getApplicationContext(), currentAlarm.getId());
        currentAlarm.setId(-1);
        currentAlarm.setDateInMillis(null);
    }

    private void deleteOldSoundContent(){
        if(SoundFiles.removeOutputFile(currentAnnotation.getSound())){
            currentAnnotation.setSound(null);
            releaseMedia();
        }
    }
//endregion

//region MenuMethods
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.annotation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }
//endregion
}