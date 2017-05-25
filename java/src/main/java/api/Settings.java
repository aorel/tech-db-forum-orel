package api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Settings {
    public static final String DATE_FORMAT_PATTERN_ZULU = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'+03:00'";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Timestamp timestampNow() {
        return new Timestamp(System.currentTimeMillis());
    }

    public static Timestamp timestampFromStringZone(String time) {
        return new Timestamp(ZonedDateTime.parse(ZonedDateTime.parse(time).format(DateTimeFormatter.ISO_INSTANT)).toLocalDateTime().toInstant(ZoneOffset.UTC).toEpochMilli());
    }

    public static void printObject(Object o) {
        try {
            System.out.println("     " + objectMapper.writeValueAsString(o));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
