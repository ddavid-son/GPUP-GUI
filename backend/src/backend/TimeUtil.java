package backend;

public class TimeUtil {
    public static final int SECOND = 1000;
    public static final int MINUTE = 60 * SECOND;
    public static final int HOUR = 60 * MINUTE;

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
}
