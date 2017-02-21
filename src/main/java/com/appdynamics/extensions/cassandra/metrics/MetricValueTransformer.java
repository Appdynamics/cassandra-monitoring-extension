package com.appdynamics.extensions.cassandra.metrics;

import com.appdynamics.extensions.util.DeltaMetricsCalculator;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

class MetricValueTransformer {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MetricValueTransformer.class);

    private final DeltaMetricsCalculator deltaCalculator = new DeltaMetricsCalculator(10);

    BigDecimal transform (String metricPath, Object metricValue, MetricProperties props) {
        if (metricValue == null) {
            logger.error("Metric value for {} is null", metricPath);
            throw new IllegalArgumentException("Metric value cannot be null");
        }
        Object convertedValue = applyConvert(metricPath, metricValue, props);
        BigDecimal val = applyMultiplier(metricPath, convertedValue, props);
        BigDecimal deltaValue = applyDelta(metricPath, val, props);
        return deltaValue;
    }

    private BigDecimal applyDelta (String metricPath, BigDecimal val, MetricProperties props) {
        if (props.isDelta()) {
            return deltaCalculator.calculateDelta(metricPath, val);
        }
        return val;
    }

    private BigDecimal applyMultiplier (String metricName, Object metricValue, MetricProperties props) {
        try {
            BigDecimal bigD = new BigDecimal(metricValue.toString());
            double multiplier = props.getMultiplier();
            bigD = bigD.multiply(new BigDecimal(multiplier));
            return bigD;
        } catch (NumberFormatException nfe) {
            logger.debug("Cannot convert into BigDecimal {} value for metric {}.", metricValue, metricName, nfe);
        }
        throw new IllegalArgumentException("Cannot convert into BigInteger " + metricValue);
    }

    private Object applyConvert (String metricName, Object metricValue, MetricProperties props) {
        //get converted values if configured
        if (props.getConversionValues() != null && !props.getConversionValues().isEmpty()) {
            Object convertedValue = props.getConversionValues().get(metricValue);
            if (convertedValue != null) {
                logger.debug("Applied conversion on {} and replaced value {} with {}", metricName, metricValue,
                        convertedValue);
                return convertedValue;
            } else {
                if (props.getConversionValues().get("$default") != null) {
                    logger.debug("Choosing the $default value to go with {} for conversion", metricValue);
                    return props.getConversionValues().get("$default");
                }
            }
        }
        return metricValue;
    }
}