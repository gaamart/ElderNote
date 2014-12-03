package com.applications.guilhermeaugusto.eldernote.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.applications.guilhermeaugusto.eldernote.Activities.AnnotationActivity;
import com.applications.guilhermeaugusto.eldernote.Extended.ActivitiesArrayAdapter;
import com.applications.guilhermeaugusto.eldernote.Managers.DataBaseHandler;
import com.applications.guilhermeaugusto.eldernote.R;
import com.applications.guilhermeaugusto.eldernote.beans.Activities;
import com.applications.guilhermeaugusto.eldernote.beans.Annotations;
import com.applications.guilhermeaugusto.eldernote.beans.Enums;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guilhermemartins on 12/2/14.
 */
public class SoundTitleDialogFragment extends DialogFragment {

    private boolean userFinish = false;
    private int itemPosition = -1;
    private ListView listView;
    private List<Activities> activities;
    private ActivitiesArrayAdapter adapter;
    private DataBaseHandler dataBaseHandler;

    public interface SoundTitleFragmentListener {
        public void onSoundTitleDialogPositiveClick(Activities activity);
        public void onSoundTitleDialogNegativeClick();
    }

    SoundTitleFragmentListener listener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (SoundTitleFragmentListener) activity;
        } catch (ClassCastException e) { throw new ClassCastException(activity.toString() + "must implement SoundTitleFragmentListener"); }
    }

    @Override
    public void onStart(){
        super.onStart();
        dataBaseHandler = new DataBaseHandler(getActivity());
        activities = dataBaseHandler.selectAllActivities();
        ArrayList<String> arrayList = new ArrayList<String>();
        for(int i=0; i < activities.size(); i++){ arrayList.add(activities.get(i).getTitle()); }
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
                listener.onSoundTitleDialogNegativeClick();
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
                listener.onSoundTitleDialogPositiveClick(activities.get(itemPosition));
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onDismiss(DialogInterface dialog){
        super.dismiss();
        if(!userFinish) {
            listener.onSoundTitleDialogNegativeClick();
        }
    }
}