package com.pgms.model;

import com.pgms.util.Check;

/** MESS module: what is served on one day of the week. Evening snacks are optional. */
public class DayMenu {

    public static final String[] MEALS = {"Breakfast", "Lunch", "Evening snacks", "Dinner"};

    private final String day;
    private final String breakfast;
    private final String lunch;
    private final String snacks;   // "" = no snacks that day
    private final String dinner;

    public DayMenu(String day, String breakfast, String lunch, String snacks, String dinner) {
        this.day = day;
        this.breakfast = Check.text(breakfast, 80, "Breakfast", true);
        this.lunch = Check.text(lunch, 80, "Lunch", true);
        String s = Check.text(snacks, 80, "Evening snacks", false);
        this.snacks = s == null || s.equals("-") ? "" : s;
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

    public String getSnacks() {
        return snacks;
    }

    public String getDinner() {
        return dinner;
    }

    /** Meal by position in MEALS: 0 breakfast, 1 lunch, 2 snacks, 3 dinner. */
    public String meal(int i) {
        switch (i) {
            case 0: return breakfast;
            case 1: return lunch;
            case 2: return snacks;
            default: return dinner;
        }
    }

    /** A copy of this day with one meal changed. */
    public DayMenu withMeal(int i, String text) {
        return new DayMenu(day,
                i == 0 ? text : breakfast,
                i == 1 ? text : lunch,
                i == 2 ? text : snacks,
                i == 3 ? text : dinner);
    }
}
