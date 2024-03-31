package hr.axion.logbook;

import org.zalando.logbook.BodyFilter;

/**
 *
 */
public final class LogbookUtils {

    /**
     * Empty body filter that returns empty JSON object
     */
    public static final BodyFilter EMPTY_BODY_FILTER = (contentType, body) -> "{}";
    public final static String DATA_MASK = "*****";

    private LogbookUtils() {
    }

    public static String maskData(String data) {
        return data.replaceAll(".", "*");
    }
}
