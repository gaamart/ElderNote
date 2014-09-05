package com.applications.guilhermeaugusto.eldernote.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
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

        editText = (EditText) view.findViewById(R.id.dialogAnnotationEditText);
        editText.setText(text);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.textDialogTitleText));
        builder.setView(view)
                .setPositiveButton(getResources().getString(R.string.doneText), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        userFinish = true;
                        listener.onTextDialogPositiveClick(editText);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancelText), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        userFinish = true;
                        listener.onTextDialogNegativeClick(editText);
                    }
                });
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog){
        super.dismiss();
        if(!userFinish){ listener.onTextDialogPositiveClick(editText); }
    }
}