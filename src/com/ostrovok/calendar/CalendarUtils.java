
package com.ostrovok.calendar;

import android.text.format.Time;

public class CalendarUtils {

    public static int getFirstDayOfWeek(Time t) {
        Time time = new Time(t);
        return getDayOfWeek(getFirstTime(time));
    }

    public static int getDayOfWeek(Time time) {
        //original 0 - sunday, 1 - monday
        if (time.weekDay == 0) {
            return 6;
        } else {
            return time.weekDay - 1;
        }
    }

    public static Time getFirstTime(Time time) {
        time.monthDay = 1;
        time.normalize(true);
        return time;
    }

    public static Time getLastTime(Time time) {
        time.monthDay = time.getActualMaximum(Time.MONTH_DAY);
        time.normalize(true);
        return time;
    }

}
