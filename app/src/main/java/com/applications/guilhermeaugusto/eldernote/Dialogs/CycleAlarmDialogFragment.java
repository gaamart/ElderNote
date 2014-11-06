package com.applications.guilhermeaugusto.eldernote.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import com.applications.guilhermeaugusto.eldernote.R;
import com.applications.guilhermeaugusto.eldernote.beans.Enums;

/**
 * Created by guilhermemartins on 10/22/14.
 */
public class CycleAlarmDialogFragment extends DialogFragment {

    private static final String LOG_TAG = "CycleAlarmDialogFragmentLog";
    private boolean userFinish = false;

    public interface CycleAlarmFragmentListener {
        public void onCycleAlarmDialogPositiveClick(int time, Enums.PeriodTypes period);
        public void onCycleAlarmDialogNegativeClick();
    }

    CycleAlarmFragmentListener listener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (CycleAlarmFragmentListener) activity;
        } catch (ClassCastException e) { throw new ClassCastException(activity.toString() + "must implement CycleAlarmFragmentListener"); }
    }

    @Override
    public Dialog onCreateDialog(Bundle saveInstanceState) {
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_cycle_alarm, null);

        final NumberPicker numberPicker = (NumberPicker) view.findViewById(R.id.numberPicker);
        numberPicker.setMaxValue(60);
        numberPicker.setMinValue(1);
        numberPicker.setWrapSelectorWheel(false);

        final NumberPicker stringPicker = (NumberPicker) view.findViewById(R.id.stringPicker);
        stringPicker.setMinValue(0);
        stringPicker.setMaxValue(3);
        stringPicker.setDisplayedValues( new String[] { getResources().getString(R.string.cyclePeriodMinuteText),
                getResources().getString(R.string.cyclePeriodHourText),
                getResources().getString(R.string.cyclePeriodDayText),
                getResources().getString(R.string.cyclePeriodWeekText) });
        numberPicker.setWrapSelectorWheel(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setPositiveButton(getResources().getString(R.string.doneText), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        userFinish = true;
                        listener.onCycleAlarmDialogPositiveClick(numberPicker.getValue(), Enums.PeriodTypes.values()[stringPicker.getValue()]);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancelText), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        userFinish = true;
                        listener.onCycleAlarmDialogNegativeClick();
                    }
                });
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onDismiss(DialogInterface dialog){
        super.dismiss();
        if(!userFinish) {
            listener.onCycleAlarmDialogNegativeClick();
        }
    }
}
