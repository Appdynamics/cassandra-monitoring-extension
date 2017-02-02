package com.appdynamics.extensions.cassandra;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CassandraUtil {

    public static String convertToString(final Object field, final String defaultStr) {
        if(field == null) {
            return defaultStr;
        }
        return field.toString();
    }

    public static String[] split(final String metricType,final String splitOn) {
        return metricType.split(splitOn);
    }

    public static String toBigIntString(final BigDecimal bigD) {
        return bigD.setScale(0, RoundingMode.HALF_UP).toBigInteger().toString();
    }
}
