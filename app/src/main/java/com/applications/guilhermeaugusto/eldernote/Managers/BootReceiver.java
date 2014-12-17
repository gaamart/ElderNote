package com.applications.guilhermeaugusto.eldernote.Managers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.applications.guilhermeaugusto.eldernote.Activities.AnnotationActivity;
import com.applications.guilhermeaugusto.eldernote.R;
import com.applications.guilhermeaugusto.eldernote.beans.Annotations;
import com.applications.guilhermeaugusto.eldernote.beans.Enums;

import java.util.List;
import java.util.Random;

/**
 * Created by guilhermemartins on 10/29/14.
 */

public class BootReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        DataBaseHandler dataBaseHandler = new DataBaseHandler(context);
        List<Annotations> annotationsList = dataBaseHandler.selectAllAnnotations();
        for (int i = 0; i < annotationsList.size(); i++) {
            Annotations annotation = annotationsList.get(i);
            if (annotation.getAlarm().getId() > 0) {
                if (System.currentTimeMillis() > Long.parseLong(annotation.getAlarm().getDateInMillis())) {
                        createNotification(context, annotation);
                        callNotificationRingtone(context);
                }
                if(annotation.getAlarm().getCyclePeriod() != Enums.PeriodTypes.None) {
                    while (System.currentTimeMillis() > Long.parseLong(annotation.getAlarm().getDateInMillis())) {
                        Long dateInMillis = Long.parseLong(annotation.getAlarm().getDateInMillis());
                        Long periodInMillis = annotation.getAlarm().createCycleTimeInMillis();
                        annotation.getAlarm().setDateInMillis(Long.toString(dateInMillis + periodInMillis));
                        if (System.currentTimeMillis() > Long.parseLong(annotation.getAlarm().getDateInMillis())) {
                            createNotification(context, annotation);
                            callNotificationRingtone(context);
                        }
                    }
                }
                dataBaseHandler.updateAnnotation(annotation);
                AlarmEntity.createAlarm(context, annotation);
            }
        }
    }

    private void createNotification(Context context, Annotations annotation){
        NotificationCompat.Builder mBuilder;

        if(annotation.contentIsText()){
            mBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.eldernote)
                    .setContentTitle(annotation.getMessage())
                    .setContentText(annotation.getAlarm().createDateLayout(context));
        } else {
            mBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.eldernote)
                    .setContentTitle(annotation.getAtivity().getTitle())
                    .setContentText(annotation.getAlarm().createDateLayout(context));
        }
        Intent activityIntent = new Intent(context, AnnotationActivity.class);
        annotation.setOperationType(Enums.OperationType.Visualize);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activityIntent.putExtra("Annotation", annotation);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(AnnotationActivity.class);
        stackBuilder.addNextIntent(activityIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(randInt(), mBuilder.build());
    }

    private void callNotificationRingtone(Context context) {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone ringtone = RingtoneManager.getRingtone( context, notification);
            ringtone.play();

        } catch (Exception e) {
            String LOG_TAG = "BootReceiver";
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private static int randInt() {
        Random rand = new Random() ;
        return rand.nextInt((2000000) + 1);
    }
}