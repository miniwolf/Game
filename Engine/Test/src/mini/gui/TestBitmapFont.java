package mini.gui;

import mini.app.SimpleApplication;
import mini.font.BitmapFont;
import mini.font.BitmapText;
import mini.font.LineWrapMode;
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
                text2.setText(str.toString());
                str.setLength(0);
            } else {
                str.append(evt.getKeyChar());
            }
        }
    };

    public static void main(String[] args) {
        System.setProperty("org.lwjgl.librarypath",
                           "C:/Users/miniwolf/Engine/Engine/lib/native/windows/");
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
        text.setText("Hello World!");
        text.setLocalTranslation(0, text.getHeight(), 0);
        guiNode.attachChild(text);

        text2 = new BitmapText(font);
        text2.setText("Type text and enter to input text");
        text2.setLocalTranslation(0, text2.getHeight() * 2, 0);
        guiNode.attachChild(text2);
    }
}
