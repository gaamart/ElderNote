package com.applications.guilhermeaugusto.eldernote.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.applications.guilhermeaugusto.eldernote.Extended.ActivitiesArrayAdapter;
import com.applications.guilhermeaugusto.eldernote.R;
import com.applications.guilhermeaugusto.eldernote.beans.Enums;

import java.util.ArrayList;


/**
 * Created by guilhermemartins on 10/22/14.
 */
public class CycleAlarmDialogFragment extends DialogFragment {

    private static final String LOG_TAG = "CycleAlarmDialogFragmentLog";
    private boolean userFinish = false;
    private int itemPosition = -1;
    private ListView listView;
    private ActivitiesArrayAdapter adapter;

    public interface CycleAlarmFragmentListener {
        public void onCycleAlarmDialogPositiveClick(Enums.PeriodTypes period);
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
    public void onStart(){
        super.onStart();
        ArrayList<String> arrayList = new ArrayList<String>();
        arrayList.add(getResources().getString(R.string.cyclePeriodEachSixHoursText));
        arrayList.add(getResources().getString(R.string.cyclePeriodEachEightHoursText));
        arrayList.add(getResources().getString(R.string.cyclePeriodEachTwelveHoursText));
        arrayList.add(getResources().getString(R.string.cyclePeriodEverDayText));
        arrayList.add(getResources().getString(R.string.cyclePeriodEveryOtherDayText));
        arrayList.add(getResources().getString(R.string.cyclePeriodEverWeekText));
        adapter = new ActivitiesArrayAdapter(getActivity(), arrayList);
        listView.setAdapter(adapter);
    }

    @Override
    public Dialog onCreateDialog(Bundle saveInstanceState) {

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_cycle_alarm, null);
        listView = (ListView) view.findViewById(R.id.listView);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        prepareListView();
        defineCancelButton(view, dialog);
        defineDoneButton(view, dialog);
        return dialog;
    }

    private void prepareListView(){
        listView.setOnItemClickListener (new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                itemPosition = position;
                adapter.setSelectedItem(position);
            }
        });
    }

    private void defineCancelButton(View view, final Dialog dialog){
        Button cancelButton = (Button) view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                userFinish = true;
                listener.onCycleAlarmDialogNegativeClick();
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
                listener.onCycleAlarmDialogPositiveClick(Enums.PeriodTypes.values()[itemPosition]);
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onDismiss(DialogInterface dialog){
        super.dismiss();
        if(!userFinish) {
            listener.onCycleAlarmDialogNegativeClick();
        }
    }
}