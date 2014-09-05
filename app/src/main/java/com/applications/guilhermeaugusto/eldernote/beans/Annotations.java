package com.applications.guilhermeaugusto.eldernote.beans;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by guilhermeaugusto on 01/08/2014.
 */
public class Annotations implements Serializable {
    private static final long serialVersionUID = 0L;
    private int id;
    private String message;
    private String sound;
    private String soundDuration;
    private Activities activity;
    private Alarms alarm;

    public Annotations(int id, String message, String sound, String soundDuration, Activities activity, Alarms alarm){
        this.id = id;
        this.message = message;
        this.sound = sound;
        this.soundDuration = soundDuration;
        this.activity = activity;
        this.alarm = alarm;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeInt(id);
        out.writeObject(message);
        out.writeObject(sound);
        out.writeObject(soundDuration);
        out.writeObject(activity);
        out.writeObject(alarm);
    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        id = in.readInt();
        message = (String) in.readObject();
        sound = (String) in.readObject();
        soundDuration = (String) in.readObject();
        activity = (Activities) in.readObject();
        alarm = (Alarms) in.readObject();
    }

    public int getId(){
        return this.id;
    }
    public String getMessage(){
        return this.message;
    }
    public String getSound(){ return this.sound; }
    public String getSoundDuration(){ return this.soundDuration; }
    public Activities getAtivity(){
        return this.activity;
    }
    public Alarms getAlarm(){
        return this.alarm;
    }

    public void setId(int id){ this.id = id; }
    public void setMessage(String message){ this.message = message; }
    public void setSound(String sound){
        this.sound = sound;
    }
    public void setSoundDuration(String soundDuration){
        this.soundDuration = soundDuration;
    }
    public void setActivity(Activities activity){ this.activity = activity; }
    public void setAlarm(Alarms alarm){ this.alarm = alarm; }
}