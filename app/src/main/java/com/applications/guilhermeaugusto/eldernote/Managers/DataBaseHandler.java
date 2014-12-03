package com.applications.guilhermeaugusto.eldernote.Managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.applications.guilhermeaugusto.eldernote.beans.Activities;
import com.applications.guilhermeaugusto.eldernote.beans.Alarms;
import com.applications.guilhermeaugusto.eldernote.beans.Annotations;
import com.applications.guilhermeaugusto.eldernote.beans.Enums;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guilhermeaugusto on 01/08/2014.
 */
public class DataBaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "annotationsmanager";

    public DataBaseHandler(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE activities " +
                "(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                " title TEXT NOT NULL)");

        db.execSQL("INSERT INTO activities (title) VALUES ('Fazer')");
        db.execSQL("INSERT INTO activities (title) VALUES ('Pagar')");
        db.execSQL("INSERT INTO activities (title) VALUES ('Ir até')");
        db.execSQL("INSERT INTO activities (title) VALUES ('Comprar')");
        db.execSQL("INSERT INTO activities (title) VALUES ('Tomar')");
        db.execSQL("INSERT INTO activities (title) VALUES ('Visitar')");
        db.execSQL("INSERT INTO activities (title) VALUES ('Falar com')");
        db.execSQL("INSERT INTO activities (title) VALUES ('Outros')");

        db.execSQL("CREATE TABLE annotations " +
                "(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                " id_activity INTEGER NOT NULL," +
                " title_activity TEXT NOT NULL," +
                " message TEXT," +
                " sound TEXT," +
                " sound_duration TEXT," +
                " id_alarm INTEGER," +
                " date_alarm TEXT," +
                " cycle_period_alarm INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS annotations");
        db.execSQL("DROP TABLE IF EXISTS activities");
        onCreate(db);
    }

    public long insertAnnotation(Annotations annotation){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_activity", annotation.getAtivity().getId());
        values.put("title_activity", annotation.getAtivity().getTitle());
        values.put("message", annotation.getMessage());
        values.put("sound", annotation.getSound());
        values.put("sound_duration", annotation.getSoundDuration());
        values.put("id_alarm", annotation.getAlarm().getId());
        values.put("date_alarm", annotation.getAlarm().getDateInMillis());
        values.put("cycle_period_alarm", annotation.getAlarm().getCyclePeriod().ordinal());
        long id = db.insert("annotations", null, values);
        db.close();
        return id;
    }

    public void insertActivity(Activities activity){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", activity.getTitle());
        db.insert("activities", null, values);
        db.close();
    }

    public List<Annotations> selectAnnotationsByActivity(Activities activity){
        SQLiteDatabase db = getReadableDatabase();
        List<Annotations> annotationsList = new ArrayList<Annotations>();
        Cursor cursor = db.query("annotations",
                new String[]{"id",
                        "id_activity",
                        "title_activity",
                        "message", "sound",
                        "sound_duration",
                        "id_alarm",
                        "date_alarm",
                        "cycle_period_alarm"},
                "id_activity" + "=?",
                new String[]{Integer.toString(activity.getId())}, null, null, null, null);
        if(cursor.moveToFirst()){
            do{
                Annotations annotation = new Annotations(Long.parseLong(cursor.getString(0)),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        new Activities(Integer.parseInt(cursor.getString(1)), cursor.getString(2)),
                        new Alarms(Integer.parseInt(cursor.getString(6)),
                                cursor.getString(7),
                                Enums.PeriodTypes.values()[Integer.parseInt(cursor.getString(8))])
                );
                annotationsList.add(annotation);
            } while(cursor.moveToNext());
        }
        db.close();
        cursor.close();
        return annotationsList;
    }

    public Activities selectActivity(int id){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("activities", new String[]{"id", "title"}, "id" + "=?", new String[]{ Integer.toString(id)}, null, null, null, null);
        if(cursor != null){ cursor.moveToFirst(); }
        Activities activity = new Activities(Integer.parseInt(cursor.getString(0)), cursor.getString(1));
        db.close();
        cursor.close();
        return activity;
    }

    public List<Annotations> selectAllAnnotations(){
        SQLiteDatabase db = getReadableDatabase();
        List<Annotations> annotationsList = new ArrayList<Annotations>();
        Cursor cursor = db.rawQuery("SELECT * FROM annotations", null);
        if(cursor.moveToFirst()){
            do{
                Annotations annotation = new Annotations(Long.parseLong(cursor.getString(0)),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        new Activities(Integer.parseInt(cursor.getString(1)), cursor.getString(2)),
                        new Alarms(Integer.parseInt(cursor.getString(6)),
                                cursor.getString(7),
                                Enums.PeriodTypes.values()[Integer.parseInt(cursor.getString(8))])
                );
                annotationsList.add(annotation);
            } while(cursor.moveToNext());
        }
        db.close();
        cursor.close();
        return annotationsList;
    }

    public List<Activities> selectAllActivities(){
        SQLiteDatabase db = getReadableDatabase();
        List<Activities> activitiesList = new ArrayList<Activities>();
        Cursor cursor = db.rawQuery("SELECT * FROM activities", null);
        if(cursor.moveToFirst()){
            do{
                Activities activity = new Activities(Integer.parseInt(cursor.getString(0)), cursor.getString(1));
                activitiesList.add(activity);
            } while(cursor.moveToNext());
        }
        db.close();
        cursor.close();
        return activitiesList;
    }

    public void deleteAnnotation(Annotations annotation){
       SQLiteDatabase db = getWritableDatabase();
        db.delete("annotations", "id=?", new String[]{ Long.toString(annotation.getId())});
        db.close();
    }

    public Annotations selectAnnotation(long id){
        SQLiteDatabase db = getReadableDatabase();
        Annotations annotation = null;
        Cursor cursor = db.query("annotations",
                new String[]{"id",
                        "id_activity",
                        "title_activity",
                        "message", "sound",
                        "sound_duration",
                        "id_alarm",
                        "date_alarm",
                        "cycle_period_alarm"},
                "id" + "=?",
                new String[]{Long.toString(id)}, null, null, null, null);
        if(cursor.moveToFirst()){
            annotation = new Annotations(Long.parseLong(cursor.getString(0)),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5),
                new Activities(Integer.parseInt(cursor.getString(1)), cursor.getString(2)),
                new Alarms(Integer.parseInt(cursor.getString(6)),
                        cursor.getString(7),
                        Enums.PeriodTypes.values()[Integer.parseInt(cursor.getString(8))])
            );
        }
        db.close();
        cursor.close();
        return annotation;
    }

    public void updateAnnotation(Annotations annotation){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_activity", annotation.getAtivity().getId());
        values.put("title_activity", annotation.getAtivity().getTitle());
        values.put("message", annotation.getMessage());
        values.put("sound", annotation.getSound());
        values.put("sound_duration", annotation.getSoundDuration());
        values.put("id_alarm", annotation.getAlarm().getId());
        values.put("date_alarm", annotation.getAlarm().getDateInMillis());
        values.put("cycle_period_alarm", annotation.getAlarm().getCyclePeriod().ordinal());
        db.update("annotations", values, "id=?", new String[]{Long.toString(annotation.getId())});
        db.close();
    }
}