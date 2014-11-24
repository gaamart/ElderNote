package com.applications.guilhermeaugusto.eldernote.Extended;

import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.DatePicker;

/**
 * Created by guilhermemartins on 11/13/14.
 */
public class ExtendedDataPickerDialog extends DatePickerDialog {

    private CharSequence title;

    public ExtendedDataPickerDialog(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
        super(context, callBack, year, monthOfYear, dayOfMonth);
    }

    public void setPermanentTitle(CharSequence title) {
        this.title = title;
        setTitle(title);
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int month, int day) {
        super.onDateChanged(view, year, month, day);
        setTitle(title);
    }
}