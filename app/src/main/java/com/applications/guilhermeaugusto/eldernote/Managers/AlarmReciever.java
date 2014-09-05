package com.applications.guilhermeaugusto.eldernote.Managers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.applications.guilhermeaugusto.eldernote.Activities.AnnotationActivity;
import com.applications.guilhermeaugusto.eldernote.beans.Annotations;

/**
 * Created by guilhermeaugusto on 15/08/2014.
 */
public class AlarmReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent activityIntent = new Intent(context, AnnotationActivity.class);
        Annotations annotation = (Annotations) intent.getSerializableExtra("Annotation");
        annotation.getAlarm().setPlayRingnote(true);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activityIntent.putExtra("Annotation", annotation);
        context.startActivity(activityIntent);
    }
}
