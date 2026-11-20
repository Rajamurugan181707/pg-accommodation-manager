package com.pgms.model;

import com.pgms.util.Check;

/** MESS module: what is served on one day of the week. */
public class DayMenu {

    private final String day;
    private String breakfast;
    private String lunch;
    private String dinner;

    public DayMenu(String day, String breakfast, String lunch, String dinner) {
        this.day = day;
        this.breakfast = Check.text(breakfast, 80, "Breakfast", true);
        this.lunch = Check.text(lunch, 80, "Lunch", true);
        this.dinner = Check.text(dinner, 80, "Dinner", true);
    }

    public String getDay() {
        return day;
    }

    public String getBreakfast() {
        return breakfast;
    }

    public String getLunch() {
        return lunch;
    }

    public String getDinner() {
        return dinner;
    }
}
