package mini.gui;

import mini.app.SimpleApplication;
import mini.font.BitmapFont;
import mini.font.BitmapText;
import mini.font.LineWrapMode;
import mini.font.Rectangle;
import mini.input.KeyboardKey;
import mini.input.RawInputListener;
import mini.input.controls.ActionListener;
import mini.input.controls.InputListener;
import mini.input.controls.KeyTrigger;
import mini.input.events.KeyInputEvent;
import mini.input.events.MouseButtonEvent;
import mini.input.events.MouseMotionEvent;

public class TestBitmapFont extends SimpleApplication {
    private BitmapText text;
    private BitmapText text2;
    private BitmapText text3;

    private InputListener keyListener = (ActionListener) (name, isPressed, tpf) -> {
        if ("WordWrap".equals(name) && !isPressed) {
            text.setLineWrapMode(text.getLineWrapMode() == LineWrapMode.Word ? LineWrapMode.NoWrap
                                                                             : LineWrapMode.Word);
        }
    };
    private RawInputListener textListener = new RawInputListener() {
        private StringBuilder str = new StringBuilder();

        @Override
        public void beginInput() {
        }

        @Override
        public void endInput() {
        }

        @Override
        public void onMouseMotionEvent(MouseMotionEvent evt) {
        }

        @Override
        public void onMouseButtonEvent(MouseButtonEvent evt) {
        }

        @Override
        public void onKeyEvent(KeyInputEvent evt) {
            if (evt.isReleased()) {
                return;
            }

            if (evt.getKeyChar() == '\n' || evt.getKeyChar() == '\r') {
                text3.setText(str.toString());
                str = new StringBuilder();
            } else if (evt.getKeyChar() != 0) {
                str.append(evt.getKeyChar());
            }
        }
    };

    public static void main(String[] args) {
        TestBitmapFont app = new TestBitmapFont();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        inputManager.addMapping("WordWrap", new KeyTrigger(KeyboardKey.KEY_TAB));
        inputManager.addListener(keyListener, "WordWrap");
        inputManager.addRawInputListener(textListener);

        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");

        text = new BitmapText(font);
        text.setBox(new Rectangle(0, 0, 1024, 768)); // TODO: Bad hardcoding
        text.setSize(font.getPreferredSize() * 2f);
        text.setText("Hello World!");
        text.setLocalTranslation(0, text.getHeight(), 0);
        guiNode.attachChild(text);

        text2 = new BitmapText(font);
        text2.setSize(font.getPreferredSize() * 1.2f);
        text2.setText(
                "Text without restrictions.\n Text without restrictions. Text without restrictions. Text without restrictions.");
        text2.setLocalTranslation(0, text2.getHeight(), 0);
        guiNode.attachChild(text2);

        text3 = new BitmapText(font); // Wrapping example
        text3.setBox(new Rectangle(0, 0, 1024, 0));
        text3.setText("Press Tab to toggle word-wrap. Type text and enter to input text");
        text3.setLocalTranslation(0, 384, 0);
        guiNode.attachChild(text3);
    }
}
