package com.applications.guilhermeaugusto.eldernote.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import com.applications.guilhermeaugusto.eldernote.R;

/**
 * Created by guilhermeaugusto on 19/08/2014.
 */
public class TimePickerFragment extends DialogFragment{

    public static final String HOUR = "Hour";
    public static final String MINUTE = "Minute";
    private TimePicker timePicker;
    private TimePickerDialog.OnTimeSetListener listener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (TimePickerDialog.OnTimeSetListener) activity;
        } catch (ClassCastException e) { throw new ClassCastException(activity.toString() + "must implement TimePickerFragmentListener"); }
    }

    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
    }

    @Override
    public Dialog onCreateDialog(Bundle saveInstanceState) {
        Bundle b = getArguments();
        int h = b.getInt(HOUR);
        int m = b.getInt(MINUTE);

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_time_picker, null);
        timePicker = (TimePicker) view.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(h);
        timePicker.setCurrentMinute(m);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        defineCancelButton(view, dialog);
        defineDoneButton(view, dialog);
        return dialog;
    }

    private void defineCancelButton(View view, final Dialog dialog){
        Button cancelButton = (Button) view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
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
                listener.onTimeSet(timePicker, timePicker.getCurrentHour(), timePicker.getCurrentMinute());
                dialog.dismiss();
            }
        });
    }
}