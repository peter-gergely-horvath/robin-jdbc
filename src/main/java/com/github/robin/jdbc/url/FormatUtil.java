package com.github.robin.jdbc.url;

public final class FormatUtil {

    static final FormatUtil INSTANCE = new FormatUtil();

    public String format(String formatString, Object value) {
        return String.format(formatString, value);
    }
}
