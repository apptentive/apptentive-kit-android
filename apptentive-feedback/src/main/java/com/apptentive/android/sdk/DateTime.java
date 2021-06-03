package com.apptentive.android.sdk;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class DateTime implements Serializable, Comparable<DateTime> {
    private static final long serialVersionUID = -7893194735115350118L;

    public static final String SEC = "sec";

    private String sec;

    public DateTime(JSONObject json) throws JSONException {
        this.sec = json.optString(SEC);
    }

    public DateTime(double dateTime) {
        setDateTime(dateTime);
    }

    public void setDateTime(double dateTime) {
        sec = String.valueOf(dateTime);
    }

    public double getDateTime() {
        return Double.valueOf(sec);
    }

    @Override
    public String toString() {
        return Double.toString(getDateTime());
    }

    @Override
    public int compareTo(DateTime other) {
        double thisDateTime = getDateTime();
        double thatDateTime = other.getDateTime();
        return Double.compare(thisDateTime, thatDateTime);
    }
}
