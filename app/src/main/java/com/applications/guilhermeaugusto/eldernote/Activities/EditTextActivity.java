package com.applications.guilhermeaugusto.eldernote.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.applications.guilhermeaugusto.eldernote.Dialogs.MessageDialogFragment;
import com.applications.guilhermeaugusto.eldernote.Managers.LogFiles;
import com.applications.guilhermeaugusto.eldernote.R;
import com.applications.guilhermeaugusto.eldernote.beans.Annotations;
import com.applications.guilhermeaugusto.eldernote.beans.Enums;

/**
 * Created by guilhermemartins on 11/24/14.
 */
public class EditTextActivity extends FragmentActivity implements MessageDialogFragment.MessageFragmentListener {

    private Annotations annotation;
    private EditText annotationEditText;
    private InputMethodManager inputMethodManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_text);
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        annotation = (Annotations) getIntent().getSerializableExtra("Annotation");
        annotationEditText = (EditText) findViewById(R.id.annotationEditText);
        if(annotation.contentIsText()){
            annotationEditText.setText(annotation.getMessage());
            Annotations oldAnnotation = new Annotations(annotation.getId(),
                    annotation.getMessage(),
                    annotation.getSound(),
                    annotation.getSoundDuration(),
                    annotation.getAtivity(),
                    annotation.getAlarm());
            annotation.setOldAnnotation(oldAnnotation);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogFiles.writeActivityEventsLog(Enums.ActivityType.EditText, "Start");
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogFiles.writeActivityEventsLog(Enums.ActivityType.EditText, "Stop");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        LogFiles.writeActivityEventsLog(Enums.ActivityType.EditText, "BackButton");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // MotionEvent object holds X-Y values
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            LogFiles.writeTouchLog(Enums.ActivityType.EditText, event.getX(), event.getY());
        }

        return super.onTouchEvent(event);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.doneButton: {
                LogFiles.writeButtonActionLog(Enums.ActivityType.EditText, Enums.ButtonActionTypes.Done);
                if (annotationEditText.getText().toString() != null && !annotationEditText.getText().toString().isEmpty() && annotationEditText.getText().toString().trim().length() > 0) {
                    annotation.setMessage(annotationEditText.getText().toString());
                    if(annotation.getOperationType() == Enums.OperationType.Create){ goToAnnotationActivity(); }
                    else { goToEditAnnotationActivity(); }
                } else {
                    callMessageDialog(getResources().getString(R.string.emptyTextErroTitleDialogText),
                            getResources().getString(R.string.emptyTextErroMessageDialogText),
                            Enums.MessageTypes.ErrorMessage);
                }
                break;
            }
            case R.id.cancelButton: {
                LogFiles.writeButtonActionLog(Enums.ActivityType.EditText, Enums.ButtonActionTypes.Cancel);
                annotation.setOldAnnotation(null);
                if(annotation.getOperationType() == Enums.OperationType.Create){ goToAnnotationActivity(); }
                else { goToEditAnnotationActivity(); }
                break;
            }
            default:
                break;
        }

    }

    private void goToAnnotationActivity(){
        inputMethodManager.hideSoftInputFromWindow(annotationEditText.getWindowToken(), 0);
        LogFiles.writeAnnotationsLog(annotation);
        annotation.setOldAnnotation(null);
        Intent intent = new Intent(getApplicationContext(), AnnotationActivity.class);
        intent.putExtra("Annotation", annotation);
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

    public void onMessageDialogPositiveClick(Enums.MessageTypes messageType) { }

    public void onMessageDialogNegativeClick(Enums.MessageTypes messageType) { }
}