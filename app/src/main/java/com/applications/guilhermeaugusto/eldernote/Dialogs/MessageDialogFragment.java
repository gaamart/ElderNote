package com.applications.guilhermeaugusto.eldernote.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.applications.guilhermeaugusto.eldernote.Managers.LogFiles;
import com.applications.guilhermeaugusto.eldernote.R;
import com.applications.guilhermeaugusto.eldernote.beans.Enums;

/**
 * Created by guilhermemartins on 11/26/14.
 */
public class MessageDialogFragment extends DialogFragment {

    private Enums.MessageTypes messageType;
    public static final String TITLE = "Title";
    public static final String MESSAGE = "Message";
    public static final String TYPE = "Type";
    public interface MessageFragmentListener {
        public void onMessageDialogPositiveClick(Enums.MessageTypes messageTypes);
        public void onMessageDialogNegativeClick(Enums.MessageTypes messageTypes);
    }

    MessageFragmentListener listener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (MessageFragmentListener) activity;
        } catch (ClassCastException e) { throw new ClassCastException(activity.toString() + "must implement MessageFragmentListener"); }
    }

    @Override
    public Dialog onCreateDialog(Bundle saveInstanceState) {
        Bundle b = getArguments();
        String title = b.getString(TITLE);
        String message = b.getString(MESSAGE);
        messageType = (Enums.MessageTypes) b.get(TYPE);

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_message, null);

        TextView titleMessageTextView = (TextView) view.findViewById(R.id.titleMessageTextView);
        TextView textMessageTextView = (TextView) view.findViewById(R.id.textMessageTextView);

        titleMessageTextView.setText(title);
        textMessageTextView.setText(message);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        if(messageType == Enums.MessageTypes.ErrorMessage) {
            defineOkButton(view, dialog);
        } else {
            defineCancelButton(view, dialog);
            defineDoneButton(view, dialog);
        }
        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog){
        super.dismiss();
    }

    private void defineCancelButton(View view, final Dialog dialog){
        LogFiles.writeButtonActionLog(Enums.ActivityType.PopUp, Enums.ButtonActionTypes.NO);
        Button cancelButton = (Button) view.findViewById(R.id.cancelButton);
        cancelButton.setVisibility(View.VISIBLE);
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listener.onMessageDialogNegativeClick(messageType);
                dialog.dismiss();
            }
        });
    }

    private void defineOkButton(View view, final Dialog dialog){
        LogFiles.writeButtonActionLog(Enums.ActivityType.PopUp, Enums.ButtonActionTypes.YES);
        Button okButton = (Button) view.findViewById(R.id.okButton);
        okButton.setVisibility(View.VISIBLE);
        okButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });
    }

    private void defineDoneButton(View view, final Dialog dialog){
        LogFiles.writeButtonActionLog(Enums.ActivityType.PopUp, Enums.ButtonActionTypes.Done);
        Button doneButton = (Button) view.findViewById(R.id.doneButton);
        doneButton.setVisibility(View.VISIBLE);
        doneButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listener.onMessageDialogPositiveClick(messageType);
                dialog.dismiss();
            }
        });
    }
}