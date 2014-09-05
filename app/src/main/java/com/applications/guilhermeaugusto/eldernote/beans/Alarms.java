package com.applications.guilhermeaugusto.eldernote.beans;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by guilhermeaugusto on 20/08/2014.
 */
public class Alarms implements  Serializable {

    private static final long serialVersionUID = 0L;
    private int id;
    private String dateInMillis;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private boolean playRingnote;

    public Alarms(){}

    public Alarms(int id, String dateInMillis){
        this.id = id;
        this.dateInMillis = dateInMillis;
        this.year = 0;
        this.month = 0;
        this.day = 0;
        this.hour = 0;
        this.minute = 0;
        this.playRingnote = false;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeInt(id);
        out.writeObject(dateInMillis);
        out.writeBoolean(playRingnote);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        id = in.readInt();
        dateInMillis = (String) in.readObject();
        playRingnote = in.readBoolean();
    }

    public String createDateLayout(){
        if(this.dateInMillis != null) {
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            gregorianCalendar.setTimeInMillis(Long.parseLong(this.dateInMillis));
            return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(gregorianCalendar.getTime());
        } else return null;
    }

    public Long createTimeInMillis(){
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.set(Calendar.YEAR, this.year);
        gregorianCalendar.set(Calendar.MONTH, this.month);
        gregorianCalendar.set(Calendar.DAY_OF_MONTH, this.day);
        gregorianCalendar.set(Calendar.HOUR_OF_DAY, this.hour);
        gregorianCalendar.set(Calendar.MINUTE, this.minute);
        this.dateInMillis = Long.toString(gregorianCalendar.getTimeInMillis());
        return gregorianCalendar.getTimeInMillis();
    }

    public void createDateConponentesByTimeInMillis(){
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTimeInMillis(Long.parseLong(this.dateInMillis));
        this.year = gregorianCalendar.get(Calendar.YEAR);
        this.month = gregorianCalendar.get(Calendar.MONTH);
        this.day = gregorianCalendar.get(Calendar.DAY_OF_MONTH);
        this.hour = gregorianCalendar.get(Calendar.HOUR_OF_DAY);
        this.minute = gregorianCalendar.get(Calendar.MINUTE);
    }

    public int getId(){ return this.id;}
    public String getDateInMillis(){ return this.dateInMillis;}
    public int getYear(){
        return this.year;
    }
    public int getMonth(){
        return this.month;
    }
    public int getDay(){
        return this.day;
    }
    public int getHour(){
        return this.hour;
    }
    public int getMinute(){
        return this.minute;
    }
    public boolean getPlayRingtone(){
        return this.playRingnote;
    }

    public void setId(int id){this.id = id; }
    public void setDateInMillis(String dateInMillis){ this.dateInMillis = dateInMillis; }
    public void setYear(int year){
        this.year = year;
    }
    public void setMonth(int month){
        this.month = month;
    }
    public void setDay(int day){
        this.day = day;
    }
    public void setHour(int hour){
        this.hour = hour;
    }
    public void setMinute(int minute){
        this.minute = minute;
    }
    public void setPlayRingnote(boolean playRingnote){ this.playRingnote = playRingnote; }
}