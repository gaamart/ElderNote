package com.applications.guilhermeaugusto.eldernote.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.applications.guilhermeaugusto.eldernote.R;

/**
 * Created by guilhermeaugusto on 02/09/2014.
 */
public class AlarmRingtoneFragment  extends DialogFragment {

    public static final String TITLE = "Title";
    private static final String LOG_TAG = "AlarmDialogFragmentLog";
    private Ringtone ringtone;
    private Vibrator vibrator;
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
        Bundle b = getArguments();
        String title = b.getString(TITLE);
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_alarm, null);
        createAlarmAnnimation(view);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //builder.setTitle( getResources().getString(R.string.alarmRingtoneTitleText) + title);
        builder.setView(view)
                .setPositiveButton(getResources().getString(R.string.doneText), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        userFinish = true;
                        ringtone.stop();
                        vibrator.cancel();
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
        startVibrate();
    }

    @Override
    public void onDismiss(DialogInterface dialog){
        super.dismiss();
        if(!userFinish) {
            ringtone.stop();
            vibrator.cancel();
            listener.onAlarmRingtoneDialogClick();
        }
    }

    private void startVibrate() {
        vibrator = (Vibrator)  getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 1000, 1000};
        vibrator.vibrate(pattern, 0);
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

    public void createAlarmAnnimation(View rootView) {
        ImageView alarmImageView = (ImageView) rootView.findViewById(R.id.alarmImageView);
        AnimationDrawable flyAnimationDrawable = new AnimationDrawable();
        flyAnimationDrawable.addFrame(getResources().getDrawable(R.drawable.leftturnedclock), 1000);
        flyAnimationDrawable.addFrame(getResources().getDrawable(R.drawable.rightturnedclock), 1000);
        flyAnimationDrawable.setOneShot(false);

        int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) { alarmImageView.setBackgroundDrawable(flyAnimationDrawable); }
        else { alarmImageView.setBackground(flyAnimationDrawable); }
        flyAnimationDrawable.start();
    }
}
