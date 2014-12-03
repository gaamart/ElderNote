package com.applications.guilhermeaugusto.eldernote.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.applications.guilhermeaugusto.eldernote.Dialogs.MessageDialogFragment;
import com.applications.guilhermeaugusto.eldernote.R;
import com.applications.guilhermeaugusto.eldernote.beans.Annotations;
import com.applications.guilhermeaugusto.eldernote.beans.Enums;

/**
 * Created by guilhermemartins on 11/24/14.
 */
public class EditTextActivity extends FragmentActivity {

    private Annotations annotation;
    private EditText annotationEditText;
    private InputMethodManager inputMethodManager;
    private DialogFragment dialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_text);
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        annotation = (Annotations) getIntent().getSerializableExtra("Annotation");
        annotationEditText = (EditText) findViewById(R.id.annotationEditText);
        if(annotation.getMessage() != null && !annotation.getMessage().isEmpty()){
            annotationEditText.setText(annotation.getMessage());
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.doneButton: {
                if (annotationEditText.getText().toString() != null && !annotationEditText.getText().toString().isEmpty() && annotationEditText.getText().toString().trim().length() > 0) {
                    annotation.setMessage(annotationEditText.getText().toString());
                    goToAnnotationActivity();
                } else {
                    callMessageDialog(getResources().getString(R.string.emptyTextErroTitleDialogText),
                            getResources().getString(R.string.emptyTextErroMessageDialogText),
                            Enums.MessageTypes.ErrorMessage);
                }
                break;
            }
            case R.id.cancelButton: {
                goToAnnotationActivity();
                break;
            }
            default:
                break;
        }

    }

    private void goToAnnotationActivity(){
        inputMethodManager.hideSoftInputFromWindow(annotationEditText.getWindowToken(), 0);
        Intent intent = new Intent(getApplicationContext(), AnnotationActivity.class);
        intent.putExtra("Annotation", annotation);
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
}