package mini.input.controls;

/**
 * <code>AnalogListener</code> is used to receive events of inputs
 * in analog format.
 */
public interface AnalogListener extends InputListener {
    /**
     * Called to notify the implementation that an analog event has occurred.
     * <p>
     * The results of KeyTrigger and MouseButtonTrigger events will have tpf
     * == value.
     *
     * @param name  The name of the mapping that was invoked
     * @param value Value of the axis, from 0 to 1.
     * @param tpf   The time per frame value.
     */
    void onAnalog(String name, float value, float tpf);
}
