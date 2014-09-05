package com.applications.guilhermeaugusto.eldernote.Managers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.applications.guilhermeaugusto.eldernote.beans.Activities;
import com.applications.guilhermeaugusto.eldernote.beans.Alarms;
import com.applications.guilhermeaugusto.eldernote.beans.Annotations;

/**
 * Created by guilhermeaugusto on 24/08/2014.
 */
public abstract class AlarmEntity {

    public static void createAlarm(Context context, Annotations annotation){
        Intent intentAlarm = new Intent(context, AlarmReciever.class);
        intentAlarm.putExtra("Annotation", annotation);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP,
                Long.parseLong(annotation.getAlarm().getDateInMillis()),
                PendingIntent.getBroadcast(context, annotation.getAlarm().getId(), intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public static void removeAlarm(Context context, int alarmID){
        Intent intentAlarm = new Intent(context, AlarmReciever.class);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(PendingIntent.getBroadcast(context, alarmID, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
    }
}