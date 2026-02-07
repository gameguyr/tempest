package com.tempest.entity;

/**
 * Comparison operators for alert threshold evaluation.
 */
public enum ComparisonOperator {
    GREATER_THAN(">"),
    LESS_THAN("<"),
    EQUALS("="),
    GREATER_EQUAL("≥"),
    LESS_EQUAL("≤");

    private final String symbol;

    ComparisonOperator(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    /**
     * Evaluate the comparison between actual and threshold values.
     *
     * @param actual    the actual measured value
     * @param threshold the threshold value to compare against
     * @return true if the condition is met, false otherwise
     */
    public boolean evaluate(Double actual, Double threshold) {
        if (actual == null || threshold == null) {
            return false;
        }

        return switch (this) {
            case GREATER_THAN -> actual > threshold;
            case LESS_THAN -> actual < threshold;
            case EQUALS -> Math.abs(actual - threshold) < 0.01; // Small epsilon for floating point comparison
            case GREATER_EQUAL -> actual >= threshold;
            case LESS_EQUAL -> actual <= threshold;
        };
    }
}
