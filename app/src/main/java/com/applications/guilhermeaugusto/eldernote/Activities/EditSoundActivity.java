package com.applications.guilhermeaugusto.eldernote.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.applications.guilhermeaugusto.eldernote.ArrayAdapters.BasicArrayAdapter;
import com.applications.guilhermeaugusto.eldernote.Dialogs.MessageDialogFragment;
import com.applications.guilhermeaugusto.eldernote.Managers.DataBaseHandler;
import com.applications.guilhermeaugusto.eldernote.Managers.LogFiles;
import com.applications.guilhermeaugusto.eldernote.Managers.SoundFiles;
import com.applications.guilhermeaugusto.eldernote.R;
import com.applications.guilhermeaugusto.eldernote.beans.Activities;
import com.applications.guilhermeaugusto.eldernote.beans.Annotations;
import com.applications.guilhermeaugusto.eldernote.beans.Enums;
import com.skd.androidrecording.audio.AudioRecordingHandler;
import com.skd.androidrecording.audio.AudioRecordingThread;
import com.skd.androidrecording.visualizer.VisualizerView;
import com.skd.androidrecording.visualizer.renderer.BarGraphRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guilhermemartins on 12/3/14.
 */
public class EditSoundActivity extends FragmentActivity implements MessageDialogFragment.MessageFragmentListener {

    private Annotations annotation;
    private String soundPath;
    private boolean userSpoke = false;
    private boolean userFinish = false;
    private VisualizerView visualizerView;
    private ListView listView;
    private Button doneButton;
    private LinearLayout visualizerLayout;
    private LinearLayout listLayout;
    private AudioRecordingThread recordingThread;
    private int itemPosition = -1;
    private List<Activities> activities;
    private TextView titleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_sound);
        annotation = (Annotations) getIntent().getSerializableExtra("Annotation");
        Annotations oldAnnotation = new Annotations(annotation.getId(),
                annotation.getMessage(),
                annotation.getSound(),
                annotation.getSoundDuration(),
                annotation.getAtivity(),
                annotation.getAlarm());
        annotation.setOldAnnotation(oldAnnotation);
        init();
        showComponents();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogFiles.writeActivityEventsLog(Enums.ActivityType.EditSound, "Stop");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        LogFiles.writeActivityEventsLog(Enums.ActivityType.EditSound, "BackButton");
    }

    @Override
    public void onStart(){
        super.onStart();
        prepareToRecordingSound();
        prepareVisualizer();
        prepareListView();
        startRecording();
        LogFiles.writeActivityEventsLog(Enums.ActivityType.EditSound, "Start");
    }

    @Override
    public void onPause(){
        super.onPause();
        if(!userFinish) {
            stopRecording();
            SoundFiles.removeOutputFile(soundPath);
            if(annotation.getOperationType() == Enums.OperationType.Create){ goToAnnotationActivity(); }
            else { goToEditAnnotationActivity(); }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // MotionEvent object holds X-Y values
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            LogFiles.writeTouchLog(Enums.ActivityType.EditSound, event.getX(), event.getY());
        }

        return super.onTouchEvent(event);
    }

    private void init() {
        listView = (ListView) findViewById(R.id.listView);
        visualizerView = (VisualizerView) findViewById(R.id.visualizerView);
        doneButton = (Button) findViewById(R.id.doneButton);
        visualizerLayout = (LinearLayout) findViewById(R.id.visualizerLayout);
        listLayout = (LinearLayout) findViewById(R.id.listLayout);
        titleTextView = (TextView) findViewById(R.id.titleTextView);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.doneButton: {
                if (!userSpoke) {
                    LogFiles.writeButtonActionLog(Enums.ActivityType.EditSound, Enums.ButtonActionTypes.Stop);
                    userSpoke = true;
                    stopRecording();
                    showComponents();
                } else {
                    LogFiles.writeButtonActionLog(Enums.ActivityType.EditSound, Enums.ButtonActionTypes.Done);
                    if (itemPosition != -1) {
                        annotation.setActivity(activities.get(itemPosition));
                        annotation.setSound(soundPath);
                        if(annotation.getOperationType() == Enums.OperationType.Create){ goToAnnotationActivity(); }
                        else { goToEditAnnotationActivity(); }
                    } else {
                        callMessageDialog(getResources().getString(R.string.emptyDescriptionErroTitleDialogText),
                                getResources().getString(R.string.emptyDescriptionErroMessageDialogText),
                                Enums.MessageTypes.ErrorMessage);
                    }
                }
                break;
            }
            case R.id.cancelButton: {
                LogFiles.writeButtonActionLog(Enums.ActivityType.EditSound, Enums.ButtonActionTypes.Cancel);
                stopRecording();
                SoundFiles.removeOutputFile(soundPath);
                annotation.setOldAnnotation(null);
                if(annotation.getOperationType() == Enums.OperationType.Create){ goToAnnotationActivity(); }
                else { goToEditAnnotationActivity(); }
                break;
            }
            default:
                break;
        }
    }

    private void prepareToRecordingSound() {
        String filePath;
        Long currentTime = System.currentTimeMillis();
        String fileName = currentTime.toString();
        filePath = SoundFiles.createFilePath();
        soundPath = filePath + "/" + fileName;
    }

    private void prepareVisualizer() {
        Paint paint = new Paint();
        paint.setStrokeWidth(6f);
        paint.setAntiAlias(true);
        paint.setColor(Color.GRAY);
        BarGraphRenderer barGraphRendererBottom = new BarGraphRenderer(2, paint, false);
        visualizerView.addRenderer(barGraphRendererBottom);
    }

    private void prepareListView() {

        DataBaseHandler dataBaseHandler = new DataBaseHandler(getApplicationContext());
        activities = dataBaseHandler.selectAllActivities();
        ArrayList<String> arrayList = new ArrayList<String>();
        for (Activities activity : activities) { arrayList.add(activity.getTitle()); }
        final BasicArrayAdapter adapter = new BasicArrayAdapter(getApplicationContext(), arrayList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener (new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LogFiles.writeButtonActionLog(Enums.ActivityType.EditSound, Enums.ButtonActionTypes.Select);
                itemPosition = position;
                adapter.setSelectedItem(position);
            }
        });
    }

    private void showComponents() {
        if(!userSpoke) {
            titleTextView.setText(getResources().getString(R.string.speakNowText));
            doneButton.setText(getResources().getString(R.string.stopText));
            visualizerView.setVisibility(View.VISIBLE);
            visualizerLayout.setVisibility(View.VISIBLE);
            listView.setVisibility(View.INVISIBLE);
            listLayout.setVisibility(View.INVISIBLE);
        } else {
            titleTextView.setText(getResources().getString(R.string.descriptionText));
            doneButton.setText(getResources().getString(R.string.doneText));
            visualizerView.setVisibility(View.INVISIBLE);
            visualizerLayout.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.VISIBLE);
            listLayout.setVisibility(View.VISIBLE);
        }
    }

    private void startRecording() {
        recordingThread = new AudioRecordingThread(soundPath, new AudioRecordingHandler() {
            @Override
            public void onFftDataCapture(final byte[] bytes) {
                runOnUiThread(new Runnable() {
                    public void run() {
                    if (visualizerView != null) {
                        visualizerView.updateVisualizerFFT(bytes);
                    }
                    }
                });
            }

            @Override
            public void onRecordSuccess() { runOnUiThread(new Runnable() { public void run() { }}); }

            @Override
            public void onRecordingError() { runOnUiThread(new Runnable() { public void run() { }}); }

            @Override
            public void onRecordSaveError() { runOnUiThread(new Runnable() { public void run() { } }); }
        });
        recordingThread.start();
    }

    private void stopRecording() {
        if (recordingThread != null) {
            recordingThread.stopRecording();
            recordingThread = null;
        }
    }

    private void goToAnnotationActivity(){
        userFinish = true;
        LogFiles.writeAnnotationsLog(annotation);
        annotation.setOldAnnotation(null);
        Intent intent = new Intent(getApplicationContext(), AnnotationActivity.class);
        intent.putExtra("Annotation", annotation);
        startActivity(intent);
    }

    private void goToEditAnnotationActivity() {
        userFinish = true;
        LogFiles.writeAnnotationsLog(annotation);
        Intent intent = new Intent(getApplicationContext(), EditAnnotationActivity.class);
        intent.putExtra("Annotation", annotation);
        startActivity(intent);
    }

    private void callMessageDialog(String title, String message, Enums.MessageTypes messageType){
        Bundle messageBundle = new Bundle();
        messageBundle.putSerializable(MessageDialogFragment.TYPE, messageType);
        messageBundle.putString(MessageDialogFragment.TITLE, title);
        messageBundle.putString(MessageDialogFragment.MESSAGE, message);
        DialogFragment dialogFragment = new MessageDialogFragment();
        dialogFragment.setArguments(messageBundle);
        dialogFragment.show(getSupportFragmentManager(), "message_dialog");
    }

    public void onMessageDialogPositiveClick(Enums.MessageTypes messageType) { }

    public void onMessageDialogNegativeClick(Enums.MessageTypes messageType) { }
}