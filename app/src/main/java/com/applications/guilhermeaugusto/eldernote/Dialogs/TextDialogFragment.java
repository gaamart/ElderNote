package com.applications.guilhermeaugusto.eldernote.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.applications.guilhermeaugusto.eldernote.R;

/**
 * Created by guilhermeaugusto on 23/08/2014.
 */
public class TextDialogFragment extends DialogFragment {

    public static final String TEXT = "text";
    public EditText editText;
    private boolean userFinish = false;
    public interface TextDialogFragmentListener {
        public void onTextDialogPositiveClick(EditText editText);
        public void onTextDialogNegativeClick(EditText editText);
    }

    TextDialogFragmentListener listener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (TextDialogFragmentListener) activity;
        } catch (ClassCastException e) { throw new ClassCastException(activity.toString() + "must implement TextDIalogFragmentListener"); }
    }

    @Override
    public Dialog onCreateDialog(Bundle saveInstanceState) {
        Bundle b = getArguments();
        String text = b.getString(TEXT);

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_text, null);

        editText = (EditText) view.findViewById(R.id.annotationEditText);
        editText.setText(text);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.textDialogTitleText));
        builder.setView(view);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog){
        super.dismiss();
        if(!userFinish){ listener.onTextDialogPositiveClick(editText); }
    }

    private void defineCancelButton(View view, final Dialog dialog){
        Button cancelButton = (Button) view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                userFinish = true;
                listener.onTextDialogNegativeClick(editText);
                dialog.dismiss();
            }
        });
    }

    private void defineDoneButton(View view, final Dialog dialog){
        Button doneButton = (Button) view.findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                userFinish = true;
                listener.onTextDialogPositiveClick(editText);
                dialog.dismiss();
            }
        });
    }
}