package com.applications.guilhermeaugusto.eldernote.Dialogs;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.support.v4.app.DialogFragment;
import android.widget.TimePicker;

import com.applications.guilhermeaugusto.eldernote.R;

/**
 * Created by guilhermeaugusto on 19/08/2014.
 */
public class TimePickerFragment extends DialogFragment{

    public static final String HOUR = "Hour";
    public static final String MINUTE = "Minute";

    private boolean isCancelled = true;
    private TimePickerDialog.OnTimeSetListener mListener;

    private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            if (!isCancelled) { mListener.onTimeSet(view,hourOfDay,minute); }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mListener = (TimePickerDialog.OnTimeSetListener) activity;
    }

    @Override
    public void onDetach() {
        this.mListener = null;
        super.onDetach();
    }

    @TargetApi(11)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle b = getArguments();
        int h = b.getInt(HOUR);
        int m = b.getInt(MINUTE);

        final TimePickerDialog picker = new TimePickerDialog(getActivity(), getConstructorListener(), h, m,
                DateFormat.is24HourFormat(getActivity()));

        picker.setCanceledOnTouchOutside(false);
        if (hasJellyBeanAndAbove()) {
            picker.setButton(DialogInterface.BUTTON_POSITIVE,
                    getResources().getString(R.string.doneText),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isCancelled = false;
                        }
                    });
            picker.setButton(DialogInterface.BUTTON_NEGATIVE,
                    getResources().getString(R.string.cancelText),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isCancelled = true;
                        }
                    });
        }
        return picker;
    }

    private static boolean hasJellyBeanAndAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    private TimePickerDialog.OnTimeSetListener getConstructorListener() {
        return hasJellyBeanAndAbove() ? mTimeSetListener : mListener;
    }
}