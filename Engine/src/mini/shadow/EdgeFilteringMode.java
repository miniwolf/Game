package mini.shadow;

public enum EdgeFilteringMode {
    /**
     * Shadows are not filtered. Nearest sample is used causing in blocky shadows.
     */
    Nearest(10),
    /**
     * Bilinear filtering is used. Has the potential of being hardware accelerated on some GPUs.
     */
    Bilinear(1),
    /**
     * 4x4 percentage-closer filtering is used. Shadow will be smoother at the cost of performance.
     */
    PCF4(3);

    private final int materialParamValue;

    EdgeFilteringMode(int val) {
        materialParamValue = val;
    }

    public int getMaterialParamValue() {
        return materialParamValue;
    }
}
