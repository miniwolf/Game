package mini.input.dummy;

import mini.input.Input;
import mini.input.RawInputListener;

/**
 * DummyInput is an implementation of <code>Input</code> that raises no input events.
 */
public class DummyInput implements Input {
    protected boolean initialized = false;

    @Override
    public void initialize() {
        if (initialized) {
            throw new IllegalStateException("Input already initialized");
        }

        initialized = true;
    }

    @Override
    public void update() {
        if (!initialized) {
            throw new IllegalStateException("Input not initialized");
        }
    }

    @Override
    public void destroy() {
        if (!initialized) {
            throw new IllegalStateException("Input not initialized");
        }

        initialized = false;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void setInputListener(RawInputListener listener) {
    }

    @Override
    public long getInputTimeNanos() {
        return System.currentTimeMillis() * 1000000;
    }
}
