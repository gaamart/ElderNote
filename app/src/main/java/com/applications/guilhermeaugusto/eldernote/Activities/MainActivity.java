package com.applications.guilhermeaugusto.eldernote.Activities;

import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.applications.guilhermeaugusto.eldernote.ArrayAdapters.AnnotationsArrayAdapter;
import com.applications.guilhermeaugusto.eldernote.Managers.DataBaseHandler;
import com.applications.guilhermeaugusto.eldernote.Managers.SoundFiles;
import com.applications.guilhermeaugusto.eldernote.R;
import com.applications.guilhermeaugusto.eldernote.beans.Activities;
import com.applications.guilhermeaugusto.eldernote.beans.Alarms;
import com.applications.guilhermeaugusto.eldernote.beans.Annotations;
import com.applications.guilhermeaugusto.eldernote.beans.Enums;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    private DataBaseHandler dataBaseHandler;
    private ListView listView;
    private TextView emptyAnnotationsListView;
    private AnnotationsArrayAdapter dataAdapter;
    private  List<Annotations> annotationsList = new ArrayList<Annotations>();
    private Activities currentActivity;

    private void init(){
        listView = (ListView) findViewById(R.id.annotationsListView);
        emptyAnnotationsListView = (TextView) findViewById(R.id.emptyAnnotationsListView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dataBaseHandler = new DataBaseHandler(getApplicationContext());
        init();
        prepareListView();
        SoundFiles.removeNotUsedSoundFiles(annotationsList);
        //showOverLay();
    }

    private void prepareListView(){
        dataAdapter = new AnnotationsArrayAdapter(this, new ArrayList<Annotations>());
        listView.setAdapter(dataAdapter);
        listView.setOnItemClickListener (new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Annotations annotation = (Annotations) listView.getItemAtPosition(position);
                annotation.setOperationType(Enums.OperationType.Visualize);
                Intent intent = new Intent(getApplicationContext(), VisualizeAnnotationActivity.class);
                intent.putExtra("Annotation", annotation);
                startActivity(intent);
            }
        });
        currentActivity = null;
        populateListViewByActivity();
    }

    public void createNewAnnotationButtonOnClick(View v) {
        Intent intent = new Intent(this, AnnotationActivity.class);
        Annotations annotation = new Annotations(-1,"","","",new Activities(-1,""), new Alarms(-1, null, Enums.PeriodTypes.None));
        annotation.setOperationType(Enums.OperationType.Create);
        intent.putExtra("Annotation", annotation);
        startActivity(intent);
    }

    private void populateListViewByActivity(){
        dataAdapter.clear();
        if(currentActivity == null){ annotationsList = dataBaseHandler.selectAllAnnotations(); }
        else { annotationsList = dataBaseHandler.selectAnnotationsByActivity(currentActivity); }
        if(!annotationsList.isEmpty()) {
            for (int i = 0; i < annotationsList.size(); i++) {
                dataAdapter.add(annotationsList.get(i));
            }
            emptyAnnotationsListView.setVisibility(View.INVISIBLE);
        } else { emptyAnnotationsListView.setVisibility(View.VISIBLE); }
        dataAdapter.notifyDataSetChanged();
    }

    private void showOverLay(){
        final Dialog dialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.setContentView(R.layout.activity_main_help);
        RelativeLayout layout = (RelativeLayout) dialog.findViewById(R.id.main_help_view);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}