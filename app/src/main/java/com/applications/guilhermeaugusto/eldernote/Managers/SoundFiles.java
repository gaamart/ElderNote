package com.applications.guilhermeaugusto.eldernote.Managers;

import android.os.Environment;
import android.util.Log;

import com.applications.guilhermeaugusto.eldernote.beans.Annotations;

import java.io.File;
import java.util.List;

/**
 * Created by guilhermeaugusto on 24/08/2014.
 */
public abstract class SoundFiles {

    private static final String LOG_TAG = "SoundFilesLog";
    public static String createFilePath(){
        String elderNoteFolder = "/ElderNoteRecorders";
        File folder = new File(Environment.getExternalStorageDirectory() + elderNoteFolder);
        boolean success = true;

        if (!folder.exists()) { success = folder.mkdir(); }
        if (success) {
            return Environment.getExternalStorageDirectory().getAbsolutePath() + elderNoteFolder;
        } else {
            Log.w(LOG_TAG, "create folder failure");
            return null;
        }
    }

    public static boolean removeOutputFile(String fileName){
        if(fileName != null && !fileName.isEmpty()) {
            String elderNoteFolder = "/ElderNoteRecorders";
            File folder = new File(Environment.getExternalStorageDirectory() + elderNoteFolder);
            boolean soundExist = false;
            if (!folder.exists()) { Log.w(LOG_TAG, "folder not exist"); return false; }
            else {
                File[] sounds = folder.listFiles();
                for (File sound : sounds) {
                    if (sound.getPath().equals(fileName)) {
                        soundExist = sound.delete();
                        break;
                    }
                }
                if (!soundExist) {Log.w(LOG_TAG, "sound not exist"); return false; }
                else { return true; }
            }
        } else { return false; }
    }

    public static void removeNotUsedSoundFiles(List<Annotations> annotationsList){
        String elderNoteFolder = "/ElderNoteRecorders";
        File folder = new File(Environment.getExternalStorageDirectory() + elderNoteFolder);

        if (!folder.exists()) {
            Log.w(LOG_TAG, "folder not exist");
        } else {
            File[] sounds = folder.listFiles();
            for (File sound : sounds) {
                boolean soundExist = false;
                for (Annotations anAnnotationsList : annotationsList) {
                    if (sound.getPath().equals(anAnnotationsList.getSound())) {
                        soundExist = true;
                        break;
                    }
                }
                if (!soundExist) {
                    sound.delete();
                }
            }
        }
    }
}
