package com.applications.guilhermeaugusto.eldernote.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.applications.guilhermeaugusto.eldernote.R;

/**
 * Created by guilhermeaugusto on 02/09/2014.
 */
public class AlarmRingtoneFragment  extends DialogFragment {

    private static final String LOG_TAG = "AlarmDialogFragmentLog";
    private Ringtone ringtone;
    private boolean userFinish = false;

    public interface AlarmRingToneFragmentListener {
        public void onAlarmRingtoneDialogClick();
    }

    AlarmRingToneFragmentListener listener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (AlarmRingToneFragmentListener) activity;
        } catch (ClassCastException e) { throw new ClassCastException(activity.toString() + "must implement AlarmRingToneFragmentListener"); }
    }

    @Override
    public Dialog onCreateDialog(Bundle saveInstanceState) {
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_alarm, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.deleteAlarmTitleDialogText));
        builder.setView(view)
                .setPositiveButton(getResources().getString(R.string.doneText), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        userFinish = true;
                        ringtone.stop();
                        listener.onAlarmRingtoneDialogClick();
                    }
                });
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onStart(){
        super.onStart();
        callAlarmRingtone();
    }

    @Override
    public void onDismiss(DialogInterface dialog){
        super.dismiss();
        if(!userFinish) {
            ringtone.stop();
            listener.onAlarmRingtoneDialogClick();
        }
    }

    private void callAlarmRingtone() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            ringtone = RingtoneManager.getRingtone(getActivity(), notification);
            ringtone.play();

        } catch (Exception e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }
}
