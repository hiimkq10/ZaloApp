package hcmute.nhom2.zaloapp.utilities;

import java.util.Date;

public class Utils {
    public static long getDateDiffInSecond(Date startDate, Date endDate) {
        long different = endDate.getTime() - startDate.getTime();
        long secondsInMilli = 1000;
        return different/secondsInMilli;
    }
}
