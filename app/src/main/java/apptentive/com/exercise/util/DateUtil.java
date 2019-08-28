package apptentive.com.exercise.util;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, YYYY", Locale.US);

    public static String prettyDate(@NonNull Date date) {
        return dateFormat.format(date);
    }
}
