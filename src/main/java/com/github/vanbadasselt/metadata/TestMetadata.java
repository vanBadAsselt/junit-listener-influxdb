package com.github.vanbadasselt.metadata;
public enum TestMetadata {

    SMOKE(Risk.SMOKE),
    HIGH(Risk.HIGH),
    MEDIUM(Risk.MEDIUM),
    LOW(Risk.LOW),

    LOGIN(Feature.LOGIN);

    public static class Risk{
        public static final String SMOKE = "SMOKE";
        public static final String HIGH = "HIGH";
        public static final String MEDIUM = "MEDIUM";
        public static final String LOW = "LOW";
    }

    public static class Feature{
        public static final String LOGIN = "Login";
    }

    private final String label;

    private TestMetadata(final String label) {
        this.label = label;
    }

    public String toString() {
        return this.label;
    }
}
