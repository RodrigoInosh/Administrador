package cl.techk.lib;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import cl.techk.ext.database.DBCnx;

public class CalendarUtils {
    private static SimpleDateFormat local_date_format = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");

    public static String getActualDateInLocalFormat() {
        Calendar actual_date = Calendar.getInstance();

        return local_date_format.format(actual_date.getTime());
    }

    public static long getActualDateInMilis() {
        Calendar actual_date = Calendar.getInstance();
        return actual_date.getTimeInMillis();
    }

    public static String getFechaChile() {

        Calendar chilean_calendar_instance = Calendar.getInstance();
        chilean_calendar_instance.add(Calendar.HOUR, Integer.parseInt(DBCnx.time_zone));
        
        return local_date_format.format(chilean_calendar_instance.getTime());
    }
}
