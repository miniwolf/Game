package mini.gui;

import mini.app.SimpleApplication;
import mini.asset.FontKey;
import mini.font.BitmapFont;
import mini.font.BitmapText;
import mini.font.plugins.BitmapFontLoader;
import mini.input.KeyInput;
import mini.input.controls.KeyTrigger;

public class TestBitmapFont extends SimpleApplication {

    public static void main(String[] args) {
        System.setProperty("org.lwjgl.librarypath",
                           "C:/Users/miniwolf/Engine/Engine/lib/native/windows/");
        TestBitmapFont app = new TestBitmapFont();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        inputManager.addMapping("WordWrap", new KeyTrigger(KeyInput.KEY_TAB));

        BitmapFont font = BitmapFontLoader
                .loadFont(new FontKey("Interface/Fonts/Default.fnt"));
        BitmapText text = new BitmapText(font);
        text.setText("Hello World!");

        text.setLocalTranslation(0, text.getHeight(), 0);
        //text.setLocalTranslation(17, 17, 0);

        guiNode.attachChild(text);
    }
}
