package com.applications.guilhermeaugusto.eldernote.Managers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.applications.guilhermeaugusto.eldernote.Activities.VisualizeAnnotationActivity;
import com.applications.guilhermeaugusto.eldernote.beans.Annotations;
import com.applications.guilhermeaugusto.eldernote.beans.Enums;

/**
 * Created by guilhermeaugusto on 15/08/2014.
 */
public class AlarmReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        DataBaseHandler dataBaseHandler = new DataBaseHandler(context);
        Intent activityIntent = new Intent(context, VisualizeAnnotationActivity.class);
        Annotations annotation = dataBaseHandler.selectAnnotation(intent.getLongExtra("Annotation_ID", 0));
        if(annotation.getAlarm().getCyclePeriod() != Enums.PeriodTypes.None) {
            Long dateInMillis = Long.parseLong(annotation.getAlarm().getDateInMillis());
            Long periodInMillis = annotation.getAlarm().createCycleTimeInMillis();
            annotation.getAlarm().setDateInMillis(Long.toString(dateInMillis + periodInMillis));
            AlarmEntity.createAlarm(context, annotation);
        } else { annotation.getAlarm().setId(0); }
        dataBaseHandler.updateAnnotation(annotation);
        annotation.setOperationType(Enums.OperationType.Triggered);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        activityIntent.putExtra("Annotation", annotation);
        context.startActivity(activityIntent);
    }
}
