package com.applications.guilhermeaugusto.eldernote.Dialogs;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import com.applications.guilhermeaugusto.eldernote.Extended.ExtendedArrayAdapter;
import com.applications.guilhermeaugusto.eldernote.Extended.ExtendedDataPickerDialog;
import com.applications.guilhermeaugusto.eldernote.R;

public class DatePickerFragment extends DialogFragment {

    public static final String YEAR = "Year";
    public static final String MONTH = "Month";
    public static final String DATE = "Day";

    private OnDateSetListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mListener = (OnDateSetListener) activity;
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
        int y = b.getInt(YEAR);
        int m = b.getInt(MONTH);
        int d = b.getInt(DATE);

        final ExtendedDataPickerDialog picker = new ExtendedDataPickerDialog(getActivity(), getConstructorListener(), y, m, d);
        picker.setPermanentTitle(getResources().getString(R.string.datePickerDialogTitleText));
        picker.setCanceledOnTouchOutside(false);
        if (hasJellyBeanAndAbove()) {
            picker.setButton(DialogInterface.BUTTON_POSITIVE,
                    getResources().getString(R.string.doneText),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DatePicker dp = picker.getDatePicker();
                            mListener.onDateSet(dp,
                                    dp.getYear(), dp.getMonth(), dp.getDayOfMonth());
                        }
                    });
            picker.setButton(DialogInterface.BUTTON_NEGATIVE,
                    getResources().getString(R.string.cancelText),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    });
        }
        return picker;
    }

    private static boolean hasJellyBeanAndAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    private OnDateSetListener getConstructorListener() {
        return hasJellyBeanAndAbove() ? null : mListener;
    }
}