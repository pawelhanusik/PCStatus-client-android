package pl.hanusik.pawel.pcstatus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    static public Date getDateFromString(String str) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try {
            return format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    static public String getStringFromDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return format.format(date);
    }

    static public Date getNewer(Date dateA, Date dateB) {
        if (dateA == null) {
            return dateB;
        }
        if (dateB == null) {
            return dateA;
        }

        if (dateA.before(dateB)) {
            return dateB;
        }
        return dateA;
    }
}
