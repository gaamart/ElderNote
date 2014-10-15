package com.applications.guilhermeaugusto.eldernote.beans;

/**
 * Created by guilhermemartins on 10/12/14.
 */
public abstract class Enums {
    public enum OperationType { Create, Update }
    public enum AlarmTypes { Selectable, New, Created, Hidden }
    public enum ContentTypes { Blank, EditingText, RecordingSound, PlayingSound, ShowingText, ShowingSound }
    public enum DeleteTypes {Annotation, Alarm }
}
