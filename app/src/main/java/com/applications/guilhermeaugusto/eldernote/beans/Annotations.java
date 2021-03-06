package com.applications.guilhermeaugusto.eldernote.beans;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by guilhermeaugusto on 01/08/2014.
 */
public class Annotations implements Serializable {
    private static final long serialVersionUID = 0L;
    private long id;
    private String message;
    private String sound;
    private String soundDuration;
    private Activities activity;
    private Alarms alarm;
    private Enums.OperationType operationType;
    private Annotations oldAnnotation;

    public Annotations(long id,
                       String message,
                       String sound,
                       String soundDuration,
                       Activities activity,
                       Alarms alarm){
        this.id = id;
        this.message = message;
        this.sound = sound;
        this.soundDuration = soundDuration;
        this.activity = activity;
        this.alarm = alarm;
        this.oldAnnotation = null;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeLong(id);
        out.writeObject(message);
        out.writeObject(sound);
        out.writeObject(soundDuration);
        out.writeObject(activity);
        out.writeObject(alarm);
        out.writeObject(operationType);
        out.writeObject(oldAnnotation);
    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        id = in.readLong();
        message = (String) in.readObject();
        sound = (String) in.readObject();
        soundDuration = (String) in.readObject();
        activity = (Activities) in.readObject();
        alarm = (Alarms) in.readObject();
        operationType = (Enums.OperationType) in.readObject();
        oldAnnotation = (Annotations) in.readObject();
    }

    public boolean contentIsText(){
        return this.message != null && !this.message.isEmpty();
    }

    public boolean contentIsSound(){
        return this.sound != null && !this.sound.isEmpty();
    }

    public boolean hasAlarm() { return this.getAlarm().getId() >= 0; }

    public long getId(){
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
    public Alarms getAlarm(){ return this.alarm; }
    public Enums.OperationType getOperationType(){ return this.operationType; }
    public Annotations getOldAnnotation() { return this.oldAnnotation; }

    public void setId(long id){ this.id = id; }
    public void setMessage(String message){ this.message = message; }
    public void setSound(String sound){
        this.sound = sound;
    }
    public void setSoundDuration(String soundDuration){
        this.soundDuration = soundDuration;
    }
    public void setActivity(Activities activity){ this.activity = activity; }
    public void setAlarm(Alarms alarm){ this.alarm = alarm; }
    public void setOperationType(Enums.OperationType operationType){ this.operationType = operationType; }
    public void setOldAnnotation(Annotations oldAnnotation){ this.oldAnnotation = oldAnnotation; }
}