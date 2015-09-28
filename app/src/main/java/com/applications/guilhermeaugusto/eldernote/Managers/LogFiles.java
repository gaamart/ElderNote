package com.applications.guilhermeaugusto.eldernote.Managers;

import android.os.Environment;
import android.util.Log;

import com.applications.guilhermeaugusto.eldernote.beans.Annotations;
import com.applications.guilhermeaugusto.eldernote.beans.Enums;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by guilhermemartins on 12/4/14.
 */
public abstract class LogFiles {

    private static final String LOG_TAG = "SoundFilesLog";

    public static File createFile(){

        String elderNoteFolder = "/ElderNoteLogs";

        File folder = new File(Environment.getExternalStorageDirectory() + elderNoteFolder);
        boolean success = true;

        if (!folder.exists()) { success = folder.mkdir(); }
        if (success) {
            return new File(Environment.getExternalStorageDirectory().getAbsolutePath() + elderNoteFolder);
        } else {
            Log.w(LOG_TAG, "create folder failure");
            return null;
        }
    }

    private static String getCurrentDate(){
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return sdf.format(new Date());
    }

    public static void writeAnnotationsLog(Annotations annotation) {
        try {
            File file = new File(createFile(), "master_log.txt");
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            String log;
            if(annotation.contentIsText()) {
                log = getCurrentDate() + " ;" +
                        Long.toString(annotation.getId()) + " ; " +
                        Long.toString(annotation.getMessage().length()) + " ; " +
                        annotation.getAlarm().getCyclePeriod().name() + " ; " +
                        annotation.getAlarm().getDateInMillis() + " ; " +
                        "texto " + "\n";
            } else {
                log = getCurrentDate() + " ;" +
                        Long.toString(annotation.getId()) + " ; " +
                        annotation.getAlarm().getCyclePeriod().name() + " ; " +
                        annotation.getAlarm().getDateInMillis() + " ; " +
                        annotation.getAtivity().getTitle() + " ; " +
                        Long.toString(TimeUnit.MILLISECONDS.toSeconds(Long.parseLong(annotation.getSoundDuration()))) + " ; " +
                        "audio" + "\n";
            }
            outputStreamWriter.write(log);
            outputStreamWriter.close();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public static void writeTouchLog(Enums.ActivityType activityType, float x, float y) {
        try {
            File file = new File(createFile(), "master_log.txt");
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            String log;
            log = getCurrentDate() + " ;" + activityType.name() + " ; x= " + Float.toString(x) + " ; y= " + Float.toString(y) + "\n";
            outputStreamWriter.write(log);
            outputStreamWriter.close();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public static void writeButtonActionLog(Enums.ActivityType activityType, Enums.ButtonActionTypes buttonActionTypes) {
        try {
            File file = new File(createFile(), "master_log.txt");
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            String log;
            log = getCurrentDate() + " ;" + activityType.name() + " ; " + buttonActionTypes.name() + "\n";
            outputStreamWriter.write(log);
            outputStreamWriter.close();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public static void writeActivityEventsLog(Enums.ActivityType activityType, String event) {
        try {
            File file = new File(createFile(), "master_log.txt");
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            String log;
            log = getCurrentDate() + " ;" + activityType.name() + " ; " + event + "\n";
            outputStreamWriter.write(log);
            outputStreamWriter.close();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public static void writeAlarmTriggerLog(Enums.TriggerType triggerType) {
        try {
            File file = new File(createFile(), "master_log.txt");
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            String log;
            log = getCurrentDate() + " ;" + triggerType.name() + "\n";
            outputStreamWriter.write(log);
            outputStreamWriter.close();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
