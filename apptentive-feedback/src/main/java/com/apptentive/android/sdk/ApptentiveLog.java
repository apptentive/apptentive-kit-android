package com.apptentive.android.sdk;

public class ApptentiveLog {

    public static void d(ApptentiveLogTag logTag, String message) {

    }

    public static void d(ApptentiveLogTag logTag, String message, Object... args) {

    }

    public static void e(Throwable throwable, String message, Object... args) {

    }

    public static void e(ApptentiveLogTag logTag, Throwable throwable, String message, Object... args) {

    }

    public static void v (ApptentiveLogTag logTag, String message, Object... args) {

    }

    public static void i (ApptentiveLogTag logTag, String message) {

    }

    public static void w (ApptentiveLogTag logTag, String message) {

    }

    public static void w(ApptentiveLogTag logTag, String message, Object... args) {

    }

    /**
     * If this object is the current level, returns true if the Level passed in is of a sufficient level to be logged.
     *
     * @return true if "level" can be logged.
     */
    public static boolean canLog(Level level) {
        return true;
    }

    public static Object hideIfSanitized(Object value) {
        return value;
    }

}
