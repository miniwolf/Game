package mini.renderer;

/**
 * <code>RendererException</code> is raised when a renderer encounters
 * a fatal rendering error.
 */
public class RendererException extends RuntimeException {
    /**
     * Creates a new instance of <code>RendererException</code>
     */
    public RendererException(String message) {
        super(message);
    }
}
