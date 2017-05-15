package api;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Settings {
//    public static final String DATE_FORMAT_PATTERN_ZULU = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'+03:00'";
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_PATTERN);

    public static Timestamp timestampNow() {
        LocalDateTime now = LocalDateTime.now();

        Timestamp timestamp1 = Timestamp.valueOf(LocalDateTime.parse(now.toString(), DateTimeFormatter.ISO_DATE_TIME));
        Timestamp timestamp2 = new Timestamp(System.currentTimeMillis());

//        System.out.println("timestampNow: " + now);
//        System.out.println("              " + timestamp1);
//        System.out.println("              " + timestamp2);

        return timestamp1;
    }

    public static Timestamp timestampFromString(String time) {
        Timestamp timestamp1 = Timestamp.valueOf(LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME));
        Timestamp timestamp2 = new Timestamp(ZonedDateTime.parse(time).toLocalDateTime().toInstant(ZoneOffset.UTC).toEpochMilli());
        Timestamp timestamp3 = new Timestamp(ZonedDateTime.parse(time).toInstant().toEpochMilli());

//        System.out.println("timestampFromString: " + time);
//        System.out.println("                     " + timestamp1);
//        System.out.println("                     " + timestamp2);
//        System.out.println("                     " + timestamp3);

        return timestamp1;
    }

    public static Timestamp timestampFromStringZone(String time) {
        Timestamp timestamp1 = new Timestamp(ZonedDateTime.parse(time).toLocalDateTime().toInstant(ZoneOffset.UTC).toEpochMilli());
        Timestamp timestamp2 = new Timestamp(ZonedDateTime.parse(time).toInstant().toEpochMilli());
//        Timestamp timestamp3 = new Timestamp(ZonedDateTime.parse(time));


//        Timestamp timestamp_err = timestampFromString(time);

//        System.out.println("timestampZoneFromString: " + time);
//        System.out.println("                         " + timestamp1);
//        System.out.println("                         " + timestamp2);
//        System.out.println();

        return timestamp1;
    }
}
