package com.app.custommusicplayer.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created by mind on 8/2/17.
 */

public class CommonMethods {
    public static String getSongTime(String songDuration) {
        if (songDuration != null && !songDuration.isEmpty()) {
            long timeInMilliSeconds = Long.parseLong(songDuration);
            long seconds = timeInMilliSeconds / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            NumberFormat f = new DecimalFormat("00");

            String time = "";
            if (hours > 0) {
                time = f.format(hours) + ":" + f.format(minutes % 60) + ":" + f.format(seconds % 60);
            } else {
                time = f.format(minutes) + ":" + f.format(seconds % 60);
            }
            return time;
        } else {
            return null;
        }
    }

    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }
}
