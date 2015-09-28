package com.applications.guilhermeaugusto.eldernote.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.applications.guilhermeaugusto.eldernote.ArrayAdapters.BasicArrayAdapter;
import com.applications.guilhermeaugusto.eldernote.Dialogs.MessageDialogFragment;
import com.applications.guilhermeaugusto.eldernote.Managers.LogFiles;
import com.applications.guilhermeaugusto.eldernote.R;
import com.applications.guilhermeaugusto.eldernote.beans.Alarms;
import com.applications.guilhermeaugusto.eldernote.beans.Annotations;
import com.applications.guilhermeaugusto.eldernote.beans.Enums;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

/**
 * Created by guilhermemartins on 12/3/14.
 */
public class EditAlarmActivity extends FragmentActivity implements MessageDialogFragment.MessageFragmentListener {

    private Annotations annotation;
    private Alarms alarm;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private Enums.AlarmStageTypes alarmStage;
    private int itemPosition = -1;
    private ListView listView;
    private BasicArrayAdapter adapter;
    private LinearLayout dateLayout;
    private LinearLayout timeLayout;
    private LinearLayout listLayout;
    private LinearLayout cycleLayout;
    private Button doneButton;
    private Button cancelButton;
    private TextView titleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_alarm);
        annotation = (Annotations) getIntent().getSerializableExtra("Annotation");
        Annotations oldAnnotation = new Annotations(annotation.getId(),
                                                    annotation.getMessage(),
                                                    annotation.getSound(),
                                                    annotation.getSoundDuration(),
                                                    annotation.getAtivity(),
                                                    annotation.getAlarm());
        annotation.setOldAnnotation(oldAnnotation);
        alarm = new Alarms();
        init();
        manageComponents();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogFiles.writeActivityEventsLog(Enums.ActivityType.EditAlarm, "Start");
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogFiles.writeActivityEventsLog(Enums.ActivityType.EditAlarm, "Stop");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        LogFiles.writeActivityEventsLog(Enums.ActivityType.EditAlarm, "BackButton");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // MotionEvent object holds X-Y values
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            LogFiles.writeTouchLog(Enums.ActivityType.EditAlarm, event.getX(), event.getY());
        }

        return super.onTouchEvent(event);
    }

    private void init(){
        dateLayout = (LinearLayout) findViewById(R.id.dateLayout);
        timeLayout = (LinearLayout) findViewById(R.id.timeLayout);
        listLayout = (LinearLayout) findViewById(R.id.listLayout);
        cycleLayout = (LinearLayout) findViewById(R.id.cycleLayout);

        doneButton = (Button) findViewById(R.id.doneButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);
        listView = (ListView) findViewById(R.id.listView);
        titleTextView = (TextView) findViewById(R.id.titleTextView);

        prepareListView();
        preparePickers();

        alarmStage = Enums.AlarmStageTypes.DateSelection;
    }

    private void prepareListView(){
        ArrayList<String> arrayList = new ArrayList<String>();
        arrayList.add(getResources().getString(R.string.cyclePeriodEachSixHoursText));
        arrayList.add(getResources().getString(R.string.cyclePeriodEachEightHoursText));
        arrayList.add(getResources().getString(R.string.cyclePeriodEachTwelveHoursText));
        arrayList.add(getResources().getString(R.string.cyclePeriodEverDayText));
        arrayList.add(getResources().getString(R.string.cyclePeriodEveryOtherDayText));
        arrayList.add(getResources().getString(R.string.cyclePeriodEverWeekText));
        adapter = new BasicArrayAdapter(getApplicationContext(), arrayList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener (new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                itemPosition = position;
                adapter.setSelectedItem(position);
                LogFiles.writeButtonActionLog(Enums.ActivityType.EditAlarm, Enums.ButtonActionTypes.Select);
            }
        });
    }

    private void preparePickers(){
        datePicker = (DatePicker) findViewById(R.id.datePicker);
        timePicker = (TimePicker) findViewById(R.id.timePicker);

        if(annotation.getAlarm().getId() > -1){
            annotation.getAlarm().createDateConponentesByTimeInMillis();
            datePicker.init(annotation.getAlarm().getYear(), annotation.getAlarm().getMonth(), annotation.getAlarm().getDay(), null);
            timePicker.setIs24HourView(true);
            timePicker.setCurrentHour(annotation.getAlarm().getHour());
            timePicker.setCurrentMinute(annotation.getAlarm().getMinute());
        } else {
            Calendar c = Calendar.getInstance();
            datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), null);
            timePicker.setIs24HourView(true);
            timePicker.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
            timePicker.setCurrentMinute(c.get(Calendar.MINUTE));
        }
    }

    private void manageComponents(){
        showComponents();
        fillComponents();
    }

    private void fillComponents(){
        switch (alarmStage) {
            case DateSelection: {
                doneButton.setText(getResources().getString(R.string.continueText));
                cancelButton.setText(getResources().getString(R.string.cancelText));
                titleTextView.setText(getResources().getString(R.string.datePickerSubTitleText));
                break;
            }
            case TimeSelection: {
                doneButton.setText(getResources().getString(R.string.continueText));
                cancelButton.setText(getResources().getString(R.string.cancelText));
                titleTextView.setText(getResources().getString(R.string.timePickerSubTitleText));
                break;
            }
            case CycleDecision: {
                doneButton.setText(getResources().getString(R.string.yesText));
                cancelButton.setText(getResources().getString(R.string.noText));
                titleTextView.setText(getResources().getString(R.string.cycleAlarmDecisionSubTitleText));
                break;
            }
            case PeriodSelection: {
                doneButton.setText(getResources().getString(R.string.doneText));
                cancelButton.setText(getResources().getString(R.string.cancelText));
                titleTextView.setText(getResources().getString(R.string.cycleAlarmPeriodSubTitleText));
                break;
            }
            default: break;
        }
    }

    private void showComponents(){
        switch (alarmStage) {
            case DateSelection: {
                dateLayout.setVisibility(View.VISIBLE);
                timeLayout.setVisibility(View.INVISIBLE);
                listLayout.setVisibility(View.INVISIBLE);
                cycleLayout.setVisibility(View.INVISIBLE);
                break;
            }
            case TimeSelection: {
                dateLayout.setVisibility(View.INVISIBLE);
                timeLayout.setVisibility(View.VISIBLE);
                listLayout.setVisibility(View.INVISIBLE);
                cycleLayout.setVisibility(View.INVISIBLE);
                break;
            }
            case CycleDecision: {
                dateLayout.setVisibility(View.INVISIBLE);
                timeLayout.setVisibility(View.INVISIBLE);
                listLayout.setVisibility(View.INVISIBLE);
                cycleLayout.setVisibility(View.VISIBLE);
                break;
            }
            case PeriodSelection: {
                dateLayout.setVisibility(View.INVISIBLE);
                timeLayout.setVisibility(View.INVISIBLE);
                listLayout.setVisibility(View.VISIBLE);
                cycleLayout.setVisibility(View.INVISIBLE);
                break;
            }
            default: break;
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.doneButton: {
                switch (alarmStage) {
                    case DateSelection: {
                        LogFiles.writeButtonActionLog(Enums.ActivityType.EditAlarm, Enums.ButtonActionTypes.Continue);
                        alarm.setYear(datePicker.getYear());
                        alarm.setMonth(datePicker.getMonth());
                        alarm.setDay(datePicker.getDayOfMonth());

                        Long currentTime = System.currentTimeMillis() - (60000 * 60 * 24);
                        alarm.createTimeInMillis();
                        if (currentTime < Long.parseLong(alarm.getDateInMillis())) {
                            alarmStage = Enums.AlarmStageTypes.TimeSelection;
                            manageComponents();
                        } else{
                            callMessageDialog(getResources().getString(R.string.olderAlarmErroTitleDialogText),
                                    getResources().getString(R.string.olderAlarmErroMessageDialogText),
                                    Enums.MessageTypes.ErrorMessage);
                        }
                        break;
                    }
                    case TimeSelection: {
                        LogFiles.writeButtonActionLog(Enums.ActivityType.EditAlarm, Enums.ButtonActionTypes.Continue);
                        alarm.setHour(timePicker.getCurrentHour());
                        alarm.setMinute(timePicker.getCurrentMinute());
                        alarm.createTimeInMillis();

                        Long currentTime = System.currentTimeMillis() - 60000;
                        if (currentTime < Long.parseLong(alarm.getDateInMillis())) {
                            alarm.setId(randInt());
                            alarmStage = Enums.AlarmStageTypes.CycleDecision;
                            manageComponents();
                        } else{
                            callMessageDialog(getResources().getString(R.string.olderAlarmErroTitleDialogText),
                                    getResources().getString(R.string.olderAlarmErroMessageDialogText),
                                    Enums.MessageTypes.ErrorMessage);
                        }
                        break;
                    }
                    case CycleDecision: {
                        LogFiles.writeButtonActionLog(Enums.ActivityType.EditAlarm, Enums.ButtonActionTypes.YES);
                        alarmStage = Enums.AlarmStageTypes.PeriodSelection;
                        manageComponents();
                        break;
                    }

                    case PeriodSelection: {
                        LogFiles.writeButtonActionLog(Enums.ActivityType.EditAlarm, Enums.ButtonActionTypes.Done);
                        if(itemPosition != -1) {
                            alarm.setCyclePeriod(Enums.PeriodTypes.values()[itemPosition]);
                            annotation.setAlarm(alarm);
                            Toast.makeText(this, getResources().getString(R.string.createSuccessAlarmToastText), Toast.LENGTH_LONG).show();
                            if(annotation.getOperationType() == Enums.OperationType.Create){ goToAnnotationActivity(); }
                            else { goToEditAnnotationActivity(); }

                        } else {
                            callMessageDialog(getResources().getString(R.string.emptyCyclePeriodErroTitleDialogText),
                                    getResources().getString(R.string.emptyCyclePeriodErroMessageDialogText),
                                    Enums.MessageTypes.ErrorMessage);
                        }
                        break;
                    }
                    default: break;
                }
                break;
            }
            case R.id.cancelButton: {
                if(alarmStage == Enums.AlarmStageTypes.CycleDecision) {
                    LogFiles.writeButtonActionLog(Enums.ActivityType.EditAlarm, Enums.ButtonActionTypes.NO);
                    alarm.setCyclePeriod(Enums.PeriodTypes.None);
                    annotation.setAlarm(alarm);
                    Toast.makeText(this, getResources().getString(R.string.createSuccessAlarmToastText), Toast.LENGTH_LONG).show();
                    if(annotation.getOperationType() == Enums.OperationType.Create){
                        annotation.setOldAnnotation(null);
                        goToAnnotationActivity();
                    }
                    else {
                        goToEditAnnotationActivity();
                    }
                } else {
                    LogFiles.writeButtonActionLog(Enums.ActivityType.EditAlarm, Enums.ButtonActionTypes.Cancel);
                    annotation.setOldAnnotation(null);
                    if (annotation.getOperationType() == Enums.OperationType.Create) {
                        goToAnnotationActivity();
                    } else {
                        goToEditAnnotationActivity();
                    }
                }
                break;
            }
            default:
                break;
        }

    }

    private void goToAnnotationActivity(){
        LogFiles.writeAnnotationsLog(annotation);
        Intent intent = new Intent(getApplicationContext(), AnnotationActivity.class);
        intent.putExtra("Annotation", annotation);
        annotation.setOldAnnotation(annotation);
        startActivity(intent);
    }

    private void goToEditAnnotationActivity() {
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

    private static int randInt() {
        Random rand = new Random() ;
        return rand.nextInt((2000000) + 1);
    }

    public void onMessageDialogPositiveClick(Enums.MessageTypes messageType) { }

    public void onMessageDialogNegativeClick(Enums.MessageTypes messageType) { }
}