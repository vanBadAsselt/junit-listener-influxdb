package com.github.vanbadasselt.metadata;
public enum TestMetadata {

    /**
     * In this way these enums are fit to use in junit 5 test tags (@Tag)
     */
    SMOKE(Risk.SMOKE),
    HIGH(Risk.HIGH),
    MEDIUM(Risk.MEDIUM),
    LOW(Risk.LOW),

    LOGIN(Feature.LOGIN);

    /**
     * An example of risk categories.
     * Don't forget to refactor setFeatureAndRiskFromTestTags if you want to use other categories!
     */
    public static class Risk {
        public static final String SMOKE = "SMOKE";
        public static final String HIGH = "HIGH";
        public static final String MEDIUM = "MEDIUM";
        public static final String LOW = "LOW";
    }

    /**
     * An example of a feature category.
     * Don't forget to refactor setFeatureAndRiskFromTestTags if you want to use other categories!
     */
    public static class Feature {
        public static final String LOGIN = "Login";
    }

    private final String label;

    TestMetadata(final String label) {
        this.label = label;
    }

    public String toString() {
        return this.label;
    }
}
