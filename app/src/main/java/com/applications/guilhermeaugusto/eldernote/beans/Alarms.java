package com.applications.guilhermeaugusto.eldernote.beans;

import android.content.Context;

import com.applications.guilhermeaugusto.eldernote.R;

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
    private boolean isCycle;
    private Enums.PeriodTypes cyclePeriod;

    public Alarms(){}

    public Alarms(int id, String dateInMillis, Enums.PeriodTypes cyclePeriod){
        this.id = id;
        this.dateInMillis = dateInMillis;
        this.year = 0;
        this.month = 0;
        this.day = 0;
        this.hour = 0;
        this.minute = 0;
        this.playRingnote = false;
        this.isCycle = false;
        this.cyclePeriod = cyclePeriod;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeInt(id);
        out.writeObject(dateInMillis);
        out.writeBoolean(playRingnote);
        out.writeObject(cyclePeriod);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        id = in.readInt();
        dateInMillis = (String) in.readObject();
        playRingnote = in.readBoolean();
        cyclePeriod = (Enums.PeriodTypes) in.readObject();
    }

    public String createPeriodLayout(Context context){
        String periodName = null;

        switch (this.cyclePeriod){
            case EachSixHours: { periodName = context.getResources().getString(R.string.cyclePeriodEachSixHoursText); break; }
            case EachEightHours: { periodName = context.getResources().getString(R.string.cyclePeriodEachEightHoursText); break; }
            case EachTwelveHours: { periodName = context.getResources().getString(R.string.cyclePeriodEachTwelveHoursText); break; }
            case EverDay: { periodName = context.getResources().getString(R.string.cyclePeriodEverDayText ); break; }
            case EveryOtherDay: { periodName = context.getResources().getString(R.string.cyclePeriodEveryOtherDayText ); break; }
            case EverWeek: { periodName = context.getResources().getString(R.string.cyclePeriodEverWeekText ); break; }
            default: break;
        }

        if(this.getCyclePeriod() != Enums.PeriodTypes.None) {
             return context.getResources().getString(R.string.cycleAlarmDescriptionText) +
                    periodName;
        } else {
            return context.getResources().getString(R.string.nonCycleAlarmDescriptionText);
        }
    }

    public String createDateLayout(Context context) {
        String dateFormat;

        if (this.dateInMillis != null) {
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            gregorianCalendar.setTimeInMillis(Long.parseLong(this.dateInMillis));
            dateFormat = createDateFormat(context);

            if(this.getCyclePeriod() != Enums.PeriodTypes.None) {
                return context.getResources().getString(R.string.nextAlarmDateText) +
                        context.getResources().getString(R.string.alarmInDateText) +
                        dateFormat;
            } else {
                return context.getResources().getString(R.string.alarmInDateText) + dateFormat;
            }
        } else return null;
    }

    public String createMissDateLayout(Context context) {
        String dateFormat;

        if (this.dateInMillis != null) {
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            gregorianCalendar.setTimeInMillis(Long.parseLong(this.dateInMillis));
            dateFormat = createDateFormat(context);

            return context.getResources().getString(R.string.missAlarmDateText) +
                    dateFormat;
        } else return null;
    }

    public String createDateFormat(Context context){
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTimeInMillis(Long.parseLong(this.dateInMillis));
        return DateFormat.getDateInstance(DateFormat.SHORT).format(gregorianCalendar.getTime()) +
                context.getResources().getString(R.string.alarmInHourText) +
                DateFormat.getTimeInstance(DateFormat.SHORT).format(gregorianCalendar.getTime());
    }

    public Long createTimeInMillis(){
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.set(Calendar.YEAR, this.year);
        gregorianCalendar.set(Calendar.MONTH, this.month);
        gregorianCalendar.set(Calendar.DAY_OF_MONTH, this.day);
        gregorianCalendar.set(Calendar.HOUR_OF_DAY, this.hour);
        gregorianCalendar.set(Calendar.MINUTE, this.minute);
        gregorianCalendar.set(Calendar.SECOND, 0);
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

    public long createCycleTimeInMillis(){
        long timeInMillis = 0;
        switch (this.cyclePeriod){
            case EachSixHours: { timeInMillis = 60000 * 60 * 6; break; }
            case EachEightHours: { timeInMillis = 60000 * 60 * 8; break; }
            case EachTwelveHours: { timeInMillis = 60000 * 60 * 12; break; }
            case EverDay: { timeInMillis = 60000 * 60 * 24; break; }
            case EveryOtherDay: { timeInMillis = 60000 * 60 * 24 * 2; break; }
            case EverWeek: { timeInMillis = 60000 * 60 * 24 * 7; break; }
            case None: { timeInMillis = 0; break; }
            default: break;
        }
        return timeInMillis;
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
    public boolean getIsCycle() { return this.isCycle; }
    public Enums.PeriodTypes getCyclePeriod() { return this.cyclePeriod; }

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
    public void setIsCycle(boolean isCycle) { this.isCycle = isCycle; }
    public void setCyclePeriod(Enums.PeriodTypes cyclePeriod) { this.cyclePeriod = cyclePeriod; }
}