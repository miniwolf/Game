package mini.input;

import mini.app.SimpleApplication;
import mini.input.controls.ActionListener;
import mini.input.controls.AnalogListener;
import mini.input.controls.KeyTrigger;
import mini.input.controls.MouseAxisTrigger;

public class TestControls extends SimpleApplication {
    private ActionListener actionListener = (name, pressed, tpf) -> System.out
            .println(name + " = " + pressed);
    private AnalogListener analogListener = (name, value, tpf) -> System.out
            .println(name + " = " + value);

    public static void main(String[] args) {
        TestControls app = new TestControls();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Test multiple inputs per mapping
        inputManager.addMapping("My Action",
                                new KeyTrigger(KeyInput.KEY_SPACE),
                                new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));

        // Test multiple listeners per mapping
        inputManager.addListener(actionListener, "My Action");
        inputManager.addListener(analogListener, "My Action");
    }
}
