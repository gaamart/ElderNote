package com.applications.guilhermeaugusto.eldernote.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.applications.guilhermeaugusto.eldernote.Managers.SoundFiles;
import com.applications.guilhermeaugusto.eldernote.R;

import java.io.IOException;

/**
 * Created by guilhermeaugusto on 24/08/2014.
 */
public class AudioDialogFragment extends DialogFragment {

    private static final String LOG_TAG = "AudioDialogFragmentLog";
    private String soundPath;
    private MediaRecorder mediaRecorder;
    private boolean userFinish = false;
    public interface SoundDialogFragmentListener {
        public void onSoundDialogPositiveClick(String soundCreated);
        public void onSoundDialogNegativeClick();
    }

    SoundDialogFragmentListener listener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (SoundDialogFragmentListener) activity;
        } catch (ClassCastException e) { throw new ClassCastException(activity.toString() + "must implement SoundDialogFragmentListener"); }
    }

    @Override
    public Dialog onCreateDialog(Bundle saveInstanceState) {
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_sound, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.soundDialogTitleText));
        builder.setView(view)
                .setPositiveButton(getResources().getString(R.string.doneText), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        userFinish = true;
                        mediaRecorder.stop();
                        releaseMedia();
                        listener.onSoundDialogPositiveClick(soundPath);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancelText), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        userFinish = true;
                        mediaRecorder.stop();
                        releaseMedia();
                        SoundFiles.removeOutputFile(soundPath);
                        listener.onSoundDialogNegativeClick();
                    }
                });
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onStart(){
        super.onStart();
        prepareToRecordingSound();
        mediaRecorder.start();
    }

    @Override
    public void onDismiss(DialogInterface dialog){
        super.dismiss();
        if(!userFinish) {
            mediaRecorder.stop();
            releaseMedia();
            listener.onSoundDialogPositiveClick(soundPath);
        }
    }

    private void prepareToRecordingSound() {
        String filePath;
        String fileName;
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        Long currentTime = System.currentTimeMillis();
        fileName = currentTime.toString() + ".ogg";
        filePath = SoundFiles.createFilePath();
        soundPath = filePath + "/" + fileName;
        mediaRecorder.setOutputFile(filePath + "//" + fileName);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void releaseMedia() {
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }
}
