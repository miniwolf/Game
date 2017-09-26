package mini.input.events;

/**
 * Mouse movement event.
 * <p>
 * Movement events are only generated if the mouse is on-screen.
 */
public class MouseMotionEvent extends InputEvent {
    private int x, y, dx, dy, wheel, deltaWheel;

    public MouseMotionEvent(int x, int y, int dx, int dy, int wheel, int deltaWheel) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.wheel = wheel;
        this.deltaWheel = deltaWheel;
    }

    /**
     * The change in wheel rotation.
     *
     * @return change in wheel rotation.
     */
    public int getDeltaWheel() {
        return deltaWheel;
    }

    /**
     * The change in X coordinate
     *
     * @return change in X coordinate
     */
    public int getDX() {
        return dx;
    }

    /**
     * The change in Y coordinate
     *
     * @return change in Y coordinate
     */
    public int getDY() {
        return dy;
    }

    /**
     * Current mouse wheel value
     *
     * @return Current mouse wheel value
     */
    public int getWheel() {
        return wheel;
    }

    /**
     * Current X coordinate
     *
     * @return Current X coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Current Y coordinate
     *
     * @return Current Y coordinate
     */
    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return "MouseMotion(X=" + x + ", Y=" + y + ", DX=" + dx + ", DY=" + dy +
               ", Wheel=" + wheel + ", dWheel=" + deltaWheel + ")";
    }
}
