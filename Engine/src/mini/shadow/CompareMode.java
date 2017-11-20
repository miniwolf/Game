package mini.shadow;

/**
 * Specifies the shadow comparision mode
 */
public enum CompareMode {
    /**
     * Shadow depth comparisons are done by using shader code
     */
    Software,
    /**
     * Shadow depth comparisons are done by using the GPU's dedicated shadowing pipeline
     */
    Hardware
}
