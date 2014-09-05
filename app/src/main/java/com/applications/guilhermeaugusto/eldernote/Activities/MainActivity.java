package com.applications.guilhermeaugusto.eldernote.Activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.applications.guilhermeaugusto.eldernote.Managers.DataBaseHandler;
import com.applications.guilhermeaugusto.eldernote.Extended.ExtendedArrayAdapter;
import com.applications.guilhermeaugusto.eldernote.Managers.SoundFiles;
import com.applications.guilhermeaugusto.eldernote.R;
import com.applications.guilhermeaugusto.eldernote.beans.Activities;
import com.applications.guilhermeaugusto.eldernote.beans.Alarms;
import com.applications.guilhermeaugusto.eldernote.beans.Annotations;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    private DataBaseHandler dataBaseHandler;
    private ListView listView;
    private ExtendedArrayAdapter dataAdapter;
    private static final String LOG_TAG = "ElderNoteLog";
    private  List<Annotations> annotationsList = new ArrayList<Annotations>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dataBaseHandler = new DataBaseHandler(getApplicationContext());
        listView = (ListView) findViewById(R.id.annotationsListView);
        prepareSpinner();
        prepareListView();
        SoundFiles.removeNotUsedSoundFiles(annotationsList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void prepareListView(){
        dataAdapter = new ExtendedArrayAdapter(this, new ArrayList<Annotations>());
        listView.setAdapter(dataAdapter);
        listView.setOnItemClickListener (new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Annotations annotation = (Annotations) listView.getItemAtPosition(position);
                Intent intent = new Intent(getApplicationContext(), AnnotationActivity.class);
                intent.putExtra("Annotation", annotation);
                startActivity(intent);
            }
        });
        populateListViewByActivity(null);
    }

    public void createNewAnnotationButtonOnClick(View v) {
        Intent intent = new Intent(this, AnnotationActivity.class);
        Annotations annotation = new Annotations(-1,"","","",new Activities(-1,""), new Alarms(-1,null));
        intent.putExtra("Annotation", annotation);
        startActivity(intent);
    }

    private void prepareSpinner(){
        Spinner spinner = (Spinner) findViewById(R.id.annotationsFilterSpinner);
        ArrayAdapter<Activities> dataAdapter = new ArrayAdapter<Activities>(this,
                android.R.layout.simple_spinner_item,
                dataBaseHandler.selectAllActivities());
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataAdapter.insert(new Activities(0,getResources().getString(R.string.spinnerFistElementText)),0);
        spinner.setAdapter(dataAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(position == 0) populateListViewByActivity(null);
                else populateListViewByActivity((Activities) parentView.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) { }
        });
    }

    private void populateListViewByActivity(Activities activity){
        dataAdapter.clear();
        if(activity == null){ annotationsList = dataBaseHandler.selectAllAnnotations(); }
        else { annotationsList = dataBaseHandler.selectAnnotationsByActivity(activity); }
        for(int i = 0; i < annotationsList.size(); i++){ dataAdapter.add(annotationsList.get(i)); }
        dataAdapter.notifyDataSetChanged();
    }
}