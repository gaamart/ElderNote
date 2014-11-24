package com.applications.guilhermeaugusto.eldernote.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.applications.guilhermeaugusto.eldernote.R;
import com.applications.guilhermeaugusto.eldernote.beans.Annotations;
import com.applications.guilhermeaugusto.eldernote.beans.Enums;

/**
 * Created by guilhermemartins on 11/24/14.
 */
public class EditTextActivity extends Activity {

    private Annotations annotation;
    private EditText annotationEditText;
    private InputMethodManager inputMethodManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_text);
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        annotation = (Annotations) getIntent().getSerializableExtra("Annotation");
    }

    public void onClick(View view) {
        annotationEditText = (EditText) findViewById(R.id.annotationEditText);
        switch (view.getId()) {
            case R.id.doneButton: {
                if ((annotationEditText.getText().toString() != null && !annotationEditText.getText().toString().isEmpty()) || (annotationEditText.getText().toString().trim().length() > 0)) {
                    annotation.setMessage(annotationEditText.getText().toString());
                    goToAnnotationActivity();
                } else {
                    callErroDialog(getResources().getString(R.string.emptyTextErroTitleDialogText),
                            getResources().getString(R.string.emptyTextErroMessageDialogText));
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
        annotation.setOperationType(Enums.OperationType.Update);
        Intent intent = new Intent(getApplicationContext(), AnnotationActivity.class);
        intent.putExtra("Annotation", annotation);
        startActivity(intent);
    }

    private void callErroDialog(String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EditTextActivity.this);
        alertDialogBuilder.setMessage(message)
                .setTitle(title)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.doneText), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}