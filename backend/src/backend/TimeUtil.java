package backend;

import java.sql.Timestamp;

public class TimeUtil {
    public static final int SECOND = 1000;
    public static final int MINUTE = 60 * SECOND;
    public static final int HOUR = 60 * MINUTE;
    public static Timestamp ts = new Timestamp(System.currentTimeMillis());

    //convert long to duration Format hh:mm:ss.msmsms
    public static String ltd(long time) {
        String timeString = "";
        long hour = time / HOUR;
        long minute = (time - hour * HOUR) / MINUTE;
        long second = (time - hour * HOUR - minute * MINUTE) / SECOND;
        long milliSecond = time - hour * HOUR - minute * MINUTE - second * SECOND;

        timeString += hour > 0 ? hour + ":" : "00:";
        timeString += minute > 0 ? minute + ":" : "00:";
        timeString += second > 0 ? second + "." : "00.";
        timeString += milliSecond > 0 ? milliSecond : "000";

        return timeString;
    }

    //convert long to now
    public static String ltn(long time) {
        ts.setTime(time);
        return ts.toString().substring(10);
/*        String timeString = "";
        long hour = time / HOUR;
        long minute = (time - hour * HOUR) / MINUTE;
        long second = (time - hour * HOUR - minute * MINUTE) / SECOND;
        long milliSecond = time - hour * HOUR - minute * MINUTE - second * SECOND;

        timeString += hour > 0 ? hour + ":" : "";
        timeString += minute > 0 ? minute + ":" : "";
        timeString += second > 0 ? second + "." : "";
        timeString += milliSecond > 0 ? milliSecond : "";

        return timeString;*/
    }

}
