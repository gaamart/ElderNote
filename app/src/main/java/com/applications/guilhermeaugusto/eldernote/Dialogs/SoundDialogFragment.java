package com.applications.guilhermeaugusto.eldernote.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.applications.guilhermeaugusto.eldernote.Managers.SoundFiles;
import com.applications.guilhermeaugusto.eldernote.R;
import com.skd.androidrecording.audio.AudioRecordingHandler;
import com.skd.androidrecording.audio.AudioRecordingThread;
import com.skd.androidrecording.visualizer.VisualizerView;
import com.skd.androidrecording.visualizer.renderer.BarGraphRenderer;

/**
 * Created by guilhermeaugusto on 24/08/2014.
 */
public class SoundDialogFragment extends DialogFragment {

    private String soundPath;
    private String fileName;
    private boolean userFinish = false;
    private boolean userCancel = false;
    private VisualizerView visualizerView;
    private AudioRecordingThread recordingThread;

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
        visualizerView = (VisualizerView) view.findViewById(R.id.visualizerView);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        defineCancelButton(view, dialog);
        defineDoneButton(view, dialog);
        return dialog;
    }

    @Override
    public void onStart(){
        super.onStart();
        prepareToRecordingSound();
        setupVisualizer();
        startRecording();
    }

    @Override
    public void onDismiss(DialogInterface dialog){
        super.dismiss();
        if(!userFinish) {
            userCancel = false;
            stopRecording();
        }
    }

    private void prepareToRecordingSound() {
        String filePath;
        Long currentTime = System.currentTimeMillis();
        fileName = currentTime.toString();
        filePath = SoundFiles.createFilePath();
        soundPath = filePath + "/" + fileName;
    }

    private void setupVisualizer() {
        Paint paint = new Paint();
        paint.setStrokeWidth(5f);
        paint.setAntiAlias(false);
        paint.setColor(Color.argb(200, 227, 69, 53));
        BarGraphRenderer barGraphRendererBottom = new BarGraphRenderer(2, paint, false);
        visualizerView.addRenderer(barGraphRendererBottom);
    }

    private void startRecording() {
         recordingThread = new AudioRecordingThread(soundPath, new AudioRecordingHandler() {
            @Override
            public void onFftDataCapture(final byte[] bytes) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (visualizerView != null) {
                            visualizerView.updateVisualizerFFT(bytes);
                        }
                    }
                });
            }

            @Override
            public void onRecordSuccess() {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if(userCancel){
                            SoundFiles.removeOutputFile(soundPath);
                            listener.onSoundDialogNegativeClick();
                        } else {
                            listener.onSoundDialogPositiveClick(soundPath);
                        }
                    }
                });
            }

            @Override
            public void onRecordingError() {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        stopRecording();
                    }
                });

            }

            @Override
            public void onRecordSaveError() {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        stopRecording();
                    }
                });

            }
        });
        recordingThread.start();
    }

    private void stopRecording() {
        if (recordingThread != null) {
            recordingThread.stopRecording();
            try {
                recordingThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            recordingThread = null;
        }
    }

    private void defineCancelButton(View view, final Dialog dialog){
        Button cancelButton = (Button) view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                userFinish = true;
                userCancel = true;
                stopRecording();
                dialog.dismiss();
            }
        });
    }

    private void defineDoneButton(View view, final Dialog dialog){
        Button doneButton = (Button) view.findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                userFinish = true;
                userCancel = false;
                stopRecording();
                dialog.dismiss();
            }
        });
    }
}
