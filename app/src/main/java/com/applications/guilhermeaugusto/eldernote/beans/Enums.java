package com.applications.guilhermeaugusto.eldernote.beans;

/**
 * Created by guilhermemartins on 10/12/14.
 */
public abstract class Enums {
    public enum OperationType { Create, Update, Visualize, Triggered }
    public enum AlarmTypes { Selectable, New, Created, Hidden }
    public enum ContentTypes { Blank, EditingText, RecordingSound, PlayingSound, ShowingText, ShowingSound }
    public enum MessageTypes {ErrorMessage, DeleteMessage, DecisionMessage }
    public enum PeriodTypes { EachSixHours, EachEightHours, EachTwelveHours, EverDay, EveryOtherDay, EverWeek, None }
    public enum AlarmStageTypes { DateSelection, TimeSelection, CycleDecision, PeriodSelection }
    public enum BackActionTypes { Upper, Bottom, ByButton }
}





