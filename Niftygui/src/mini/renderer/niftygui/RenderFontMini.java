package mini.renderer.niftygui;

import de.lessvoid.nifty.spi.render.RenderFont;
import mini.font.BitmapFont;
import mini.font.BitmapText;
import mini.post.niftygui.NiftyMiniDisplay;

public class RenderFontMini implements RenderFont {
    private BitmapFont font;
    private BitmapText text;

    public RenderFontMini(String fileName, NiftyMiniDisplay display) {
        font = display.getAssetManager().loadFont(fileName);
        text = new BitmapText(font);
        text.setSize(font.getPreferredSize());
    }

    @Override
    public int getWidth(String str) {
        if (str.length() == 0) {
            return 0;
        }

        return (int) font.getLineWidth(str);
    }

    @Override
    public int getWidth(String str, final float size) {
        // TODO: This is supposed to return the width of the string when scaled with the size factor.
        // Not applicable right now.
        return getWidth(str);
    }

    @Override
    public int getHeight() {
        return (int) text.getLineHeight();
    }

    @Override
    public int getCharacterAdvance(char currentChar, char nextChar, float size) {
        return Math.round(font.getCharacterAdvance(currentChar, nextChar, size));
    }

    @Override
    public void dispose() {
    }

    public BitmapFont getFont() {
        return font;
    }

    public BitmapText createText() {
        return new BitmapText(font);
    }
}
