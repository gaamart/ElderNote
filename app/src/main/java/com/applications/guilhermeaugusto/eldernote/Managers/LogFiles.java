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
            File file = new File(createFile(), "annotations_log.txt");
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            String log;
            if(annotation.contentIsText()) {
                log = getCurrentDate() + " ;" +
                        Long.toString(annotation.getId()) + " ; " +
                        Integer.toString(annotation.getAlarm().getCyclePeriod().ordinal()) + " ; " +
                        "texto " + "\n";
            } else {
                log = getCurrentDate() + " ;" +
                        Long.toString(annotation.getId()) + " ; " +
                        Integer.toString(annotation.getAlarm().getCyclePeriod().ordinal()) + " ; " +
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

    public static void writeBackActionLog(Enums.BackActionTypes backActionTypes) {
        try {
            File file = new File(createFile(), "back_action_log.txt");
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            String log;
            switch (backActionTypes){
                case Upper: {
                    log = getCurrentDate() + " ;" + "Botao back da action bar pressionado"  + "\n";
                    break;
                }
                case Bottom: {
                    log = getCurrentDate() + " ;" + "Botao back do android pressionado"  + "\n";
                    break;
                }
                case ByButton: {
                    log = getCurrentDate() + " ;" + "Botao voltar do aplicativo pressionado"  + "\n";
                    break;
                }
                default: {
                    log = getCurrentDate() + " ;" + "Aconteceu um erro"  + "\n";
                    break;
                }
            }
            outputStreamWriter.write(log);
            outputStreamWriter.close();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
