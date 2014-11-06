package com.applications.guilhermeaugusto.eldernote.Managers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.applications.guilhermeaugusto.eldernote.Activities.AnnotationActivity;
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
        Intent activityIntent = new Intent(context, AnnotationActivity.class);
        Annotations annotation = dataBaseHandler.selectAnnotation(intent.getLongExtra("Annotation_ID", 0));
        annotation.getAlarm().setPlayRingnote(true);
        if(annotation.getAlarm().getCycleTime() > 0) {
            Long dateInMillis = Long.parseLong(annotation.getAlarm().getDateInMillis());
            Long periodInMillis = annotation.getAlarm().createCycleTimeInMillis();
            annotation.getAlarm().setDateInMillis(Long.toString(dateInMillis + periodInMillis));
            AlarmEntity.createAlarm(context, annotation);
            Toast.makeText(context, annotation.getAlarm().createDateLayout(context), Toast.LENGTH_LONG).show();
        } else { annotation.getAlarm().setId(0); }
        dataBaseHandler.updateAnnotation(annotation);
        annotation.setOperationType(Enums.OperationType.Visualize);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        activityIntent.putExtra("Annotation", annotation);
        context.startActivity(activityIntent);
    }
}
