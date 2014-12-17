package com.applications.guilhermeaugusto.eldernote.ArrayAdapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.applications.guilhermeaugusto.eldernote.R;
import com.applications.guilhermeaugusto.eldernote.beans.Annotations;

import java.util.ArrayList;

/**
 * Created by guilhermeaugusto on 19/08/2014.
 */
public class AnnotationsArrayAdapter extends ArrayAdapter<Annotations> {

    private final Context context;
    private final ArrayList<Annotations> itemsArrayList;

    public AnnotationsArrayAdapter(Context context, ArrayList<Annotations> itemsArrayList) {
        super(context, R.layout.row_annotation, itemsArrayList);
        this.context = context;
        this.itemsArrayList = itemsArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.row_annotation, parent, false);
        defineBackgroundColor(rowView, position);
        defineAlarmContent(rowView, position);
        defineAnnotationContent(rowView, position);
        return rowView;
    }

    public void defineBackgroundColor(View view, int position){
        if (position % 2 == 1) { view.setBackgroundColor(Color.parseColor("#ffdeddde")); }
        else { view.setBackgroundColor(Color.parseColor("#fff2f2f5")); }
    }

    private void defineAnnotationContent(View view, int position){
        TextView annotationContentTextView = (TextView) view.findViewById(R.id.annotationContentTextView);
        ImageView contentSoundImageView = (ImageView) view.findViewById(R.id.contentSoundImageView);
        ImageView contentTextImageView = (ImageView) view.findViewById(R.id.contentTextImageView);

        if(itemsArrayList.get(position).contentIsText()) {
            annotationContentTextView.setText(itemsArrayList.get(position).getMessage());
            contentTextImageView.setVisibility(View.VISIBLE);
            contentSoundImageView.setVisibility(View.INVISIBLE);
        } else {
            annotationContentTextView.setText(itemsArrayList.get(position).getAtivity().getTitle());
            contentTextImageView.setVisibility(View.INVISIBLE);
            contentSoundImageView.setVisibility(View.VISIBLE);
        }
    }

    public void defineAlarmContent(View view,  int position){
        TextView alarmDateContentTextView = (TextView) view.findViewById(R.id.alarmDateContentTextView);
        if(itemsArrayList.get(position).getAlarm().createDateLayout(getContext()) != null){
            alarmDateContentTextView.setText(itemsArrayList.get(position).getAlarm().createDateFormat(getContext()));
        }
    }
}