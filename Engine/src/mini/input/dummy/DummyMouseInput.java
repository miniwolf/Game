package mini.input.dummy;

import mini.input.MouseInput;

public class DummyMouseInput extends DummyInput implements MouseInput {
    @Override
    public void setCursorVisible(boolean visible) {
        if (!initialized) {
            throw new IllegalStateException("Input not initialized");
        }
    }

    @Override
    public int getButtonCount() {
        return 0;
    }
}
