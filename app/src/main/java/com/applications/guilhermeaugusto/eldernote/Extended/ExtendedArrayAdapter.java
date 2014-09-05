package com.applications.guilhermeaugusto.eldernote.Extended;

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
import java.util.concurrent.TimeUnit;

/**
 * Created by guilhermeaugusto on 19/08/2014.
 */
public class ExtendedArrayAdapter extends ArrayAdapter<Annotations> {

    private final Context context;
    private final ArrayList<Annotations> itemsArrayList;

    public ExtendedArrayAdapter(Context context, ArrayList<Annotations> itemsArrayList) {
        super(context, R.layout.row_annotation, itemsArrayList);
        this.context = context;
        this.itemsArrayList = itemsArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.row_annotation, parent, false);

        TextView activityTitleTextView = (TextView) rowView.findViewById(R.id.activityTitleTextVIew);
        TextView annotationContentTextView = (TextView) rowView.findViewById(R.id.annotationContentTextView);
        TextView alarmContentTextView = (TextView) rowView.findViewById(R.id.alarmContentTextView);
        ImageView contentSoundImageView = (ImageView) rowView.findViewById(R.id.contentSoundImageView);
        ImageView contentTextImageView = (ImageView) rowView.findViewById(R.id.contentTextImageView);

        if (position % 2 == 1) { rowView.setBackgroundColor(Color.parseColor("#ffdeddde")); }
        else { rowView.setBackgroundColor(Color.parseColor("#fff2f2f5")); }

        activityTitleTextView.setText(itemsArrayList.get(position).getAtivity().getTitle());
        if(itemsArrayList.get(position).getAlarm().createDateLayout() != null && !itemsArrayList.get(position).getMessage().isEmpty()){
            alarmContentTextView.setText(getContext().getResources().getString(R.string.createAlarmDateText) +
                    itemsArrayList.get(position).getAlarm().createDateLayout());
        }

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
        return rowView;
    }
}
