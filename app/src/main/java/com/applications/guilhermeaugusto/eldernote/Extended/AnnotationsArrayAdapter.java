package com.applications.guilhermeaugusto.eldernote.Extended;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.applications.guilhermeaugusto.eldernote.Activities.AnnotationActivity;
import com.applications.guilhermeaugusto.eldernote.Activities.VisualizeAnnotationActivity;
import com.applications.guilhermeaugusto.eldernote.Managers.AlarmEntity;
import com.applications.guilhermeaugusto.eldernote.Managers.DataBaseHandler;
import com.applications.guilhermeaugusto.eldernote.Managers.SoundFiles;
import com.applications.guilhermeaugusto.eldernote.R;
import com.applications.guilhermeaugusto.eldernote.beans.Annotations;
import com.applications.guilhermeaugusto.eldernote.beans.Enums;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by guilhermeaugusto on 19/08/2014.
 */
public class AnnotationsArrayAdapter extends ArrayAdapter<Annotations> {

    private final Context context;
    private DataBaseHandler dataBaseHandler;
    private final ArrayList<Annotations> itemsArrayList;

    public interface ExtendedArrayAdapterListener {
        public void onDeleteClick();
    }

    ExtendedArrayAdapterListener listener;

    public void setTheListener(ExtendedArrayAdapterListener listener) {
        this.listener = listener;
    }

    public AnnotationsArrayAdapter(Context context, ArrayList<Annotations> itemsArrayList) {
        super(context, R.layout.row_annotation, itemsArrayList);
        this.context = context;
        this.dataBaseHandler = new DataBaseHandler(context);
        this.itemsArrayList = itemsArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.row_annotation, parent, false);
        defineBackgroundColor(rowView, position);
        defineTitle(rowView, position);
        defineAlarmContent(rowView, position);
        defineAnnotationContent(rowView, position);
        defineDeleteButton(rowView,position);
        defineEditButton(rowView,position);
        defineVisualizeButton(rowView,position);
        return rowView;
    }

    public void defineBackgroundColor(View view, int position){
        if (position % 2 == 1) { view.setBackgroundColor(Color.parseColor("#ffdeddde")); }
        else { view.setBackgroundColor(Color.parseColor("#fff2f2f5")); }
    }

    public void defineTitle(View view, int position){
        TextView activityTitleTextView = (TextView) view.findViewById(R.id.activityTitleTextVIew);
        activityTitleTextView.setText(itemsArrayList.get(position).getAtivity().getTitle());
    }

    public void defineAlarmContent(View view,  int position){
        //TextView alarmPeriodDescriptionTextView = (TextView) view.findViewById(R.id.alarmPeriodDescriptionTextView);
        TextView alarmDateContentTextView = (TextView) view.findViewById(R.id.alarmDateContentTextView);
        if(itemsArrayList.get(position).getAlarm().createDateLayout(getContext()) != null){
            //alarmPeriodDescriptionTextView.setText(itemsArrayList.get(position).getAlarm().createPeriodLayout(getContext()));
            alarmDateContentTextView.setText(itemsArrayList.get(position).getAlarm().createDateFormat(getContext()));
        }
    }

    private void defineAnnotationContent(View view, int position){
        TextView annotationContentTextView = (TextView) view.findViewById(R.id.annotationContentTextView);
        ImageView contentSoundImageView = (ImageView) view.findViewById(R.id.contentSoundImageView);
        ImageView contentTextImageView = (ImageView) view.findViewById(R.id.contentTextImageView);

        if(itemsArrayList.get(position).getMessage() != null && !itemsArrayList.get(position).getMessage().isEmpty()) {
            annotationContentTextView.setText(itemsArrayList.get(position).getMessage());
            contentTextImageView.setVisibility(View.VISIBLE);
            contentSoundImageView.setVisibility(View.INVISIBLE);
        } else {
            Long soundDuration = TimeUnit.MILLISECONDS.toSeconds(Long.parseLong(itemsArrayList.get(position).getSoundDuration()));
            String soundSeconds = String.format(getContext().getResources().getString(R.string.soundSecondText));
            if(soundDuration > 1) { soundSeconds = soundSeconds + 's'; }

            annotationContentTextView.setText( String.format(getContext().getResources().getString(R.string.createSoundDurantionText) +
                    "%d " + soundSeconds, soundDuration));

            contentTextImageView.setVisibility(View.INVISIBLE);
            contentSoundImageView.setVisibility(View.VISIBLE);
        }
    }

    private void defineDeleteButton(View view, final int position){
        ImageButton deleteAnnotationButton = (ImageButton) view.findViewById(R.id.deleteAnnotationButton);
        deleteAnnotationButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                callDeleteDialog(context.getResources().getString(R.string.deleteAnnotationTitleDialogText),
                        context.getResources().getString(R.string.deleteAnnotationMessageDialogText),
                        itemsArrayList.get(position));
                notifyDataSetChanged();
            }
        });
    }

    private void defineEditButton(View view, final int position){
        ImageButton editAnnotationButton = (ImageButton) view.findViewById(R.id.editAnnotationButton);
        editAnnotationButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Annotations annotation = itemsArrayList.get(position);
                annotation.setOperationType(Enums.OperationType.Update);
                Intent intent = new Intent(context, AnnotationActivity.class);
                intent.putExtra("Annotation", annotation);
                context.startActivity(intent);
            }
        });
    }

    private void defineVisualizeButton(View view, final int position){
        Button visualizeAnnotationButton = (Button) view.findViewById(R.id.visualizeAnnotationButton);
        visualizeAnnotationButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Annotations annotation = itemsArrayList.get(position);
                annotation.setOperationType(Enums.OperationType.Visualize);
                Intent intent = new Intent(context, VisualizeAnnotationActivity.class);
                intent.putExtra("Annotation", annotation);
                context.startActivity(intent);
            }
        });
    }

    private void callDeleteDialog(String title, String message, final Annotations annotation){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(message)
                .setTitle(title)
                .setCancelable(false)
                .setPositiveButton(context.getResources().getString(R.string.yesText), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteAnnotation(annotation);
                    }
                })
                .setNegativeButton(context.getResources().getString(R.string.noText), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void deleteAnnotation(Annotations annotation){
        if(annotation.getSound() != null && annotation.getSound().isEmpty() ) {
            SoundFiles.removeOutputFile(annotation.getSound());
        }
        if(annotation.getAlarm().getId() != -1) {
            AlarmEntity.removeAlarm(context, annotation.getAlarm().getId());
        }
        dataBaseHandler.deleteAnnotation(annotation);
        Toast.makeText(context, context.getResources().getString(R.string.deleteSuccessAnnotationToastText), Toast.LENGTH_LONG).show();
        listener.onDeleteClick();
    }
}