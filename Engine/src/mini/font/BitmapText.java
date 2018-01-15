package mini.font;

import mini.material.MatParam;
import mini.material.Material;
import mini.math.ColorRGBA;
import mini.renderer.RenderManager;
import mini.scene.Node;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BitmapText extends Node {

    private BitmapFont font;
    private StringBlock block;
    private boolean needRefresh = true;
    private BitmapTextPage[] textPages;
    private Letters letters;

    public BitmapText(BitmapFont font) {
        this(font, false, false);
    }

    public BitmapText(BitmapFont font, boolean rightToLeft) {
        this(font, rightToLeft, false);
    }

    public BitmapText(BitmapFont font, boolean rightToLeft, boolean arrayBased) {
        textPages = new BitmapTextPage[font.getPageSize()];
        for (int page = 0; page < textPages.length; page++) {
            textPages[page] = new BitmapTextPage(font, page);
            attachChild(textPages[page]);
        }

        this.font = font;
        this.block = new StringBlock();
        block.setSize(font.getPreferredSize());
        letters = new Letters(font, block, rightToLeft);
    }

    @Override
    public BitmapText clone() {
        return (BitmapText) super.clone(false);
    }

    public BitmapFont getFont() {
        return font;
    }

    public float getSize() {
        return block.getSize();
    }

    /**
     * Changes text size
     *
     * @param size text size
     */
    public void setSize(float size) {
        block.setSize(size);
        needRefresh = true;
        letters.invalidate();
    }

    /**
     * @param text charsequence to change text to
     */
    public void setText(CharSequence text) {
        // note: text.toString() is free if text is already a java.lang.String.
        setText(text != null ? text.toString() : null);
    }

    /**
     * @return returns text
     */
    public String getText() {
        return block.getText();
    }

    /**
     * @param text String to change text to
     */
    public void setText(String text) {
        text = text == null ? "" : text;

        if (block.getText().equals(text)) {
            return;
        }

        // Update the text content
        block.setText(text);
        letters.setText(text);

        // Flag for refresh
        needRefresh = true;
    }

    /**
     * @return color of the text
     */
    public ColorRGBA getColor() {
        return letters.getBaseColor();
    }

    /**
     * changes text color. all substring colors are deleted.
     *
     * @param color new color of text
     */
    public void setColor(ColorRGBA color) {
        letters.setColor(color);
        letters.invalidate(); // TODO: Don't have to align.
        needRefresh = true;
    }

    public float getAlpha() {
        return letters.getBaseAlpha();
    }

    /**
     * Sets an overall alpha that will be applied to all
     * letters.  If the alpha passed is -1 then alpha reverts
     * to default... which will be 1 for anything unspecified
     * and color tags will be reset to 1 or their encoded
     * alpha.
     */
    public void setAlpha(float alpha) {
        letters.setBaseAlpha(alpha);
        needRefresh = true;
    }

    /**
     * Define area where bitmaptext will be rendered
     *
     * @param rect position and size box where text is rendered
     */
    public void setBox(Rectangle rect) {
        block.setTextBox(rect);
        letters.invalidate();
        needRefresh = true;
    }

    /**
     * @return height of the line
     */
    public float getLineHeight() {
        return font.getLineHeight(block);
    }

    /**
     * @return height of whole textblock
     */
    public float getHeight() {
        if (needRefresh) {
            assemble();
        }
        float height = getLineHeight() * block.getLineCount();
        Rectangle textBox = block.getTextBox();
        if (textBox != null) {
            return Math.max(height, textBox.height);
        }
        return height;
    }

    /**
     * @return width of line
     */
    public float getLineWidth() {
        if (needRefresh) {
            assemble();
        }
        Rectangle textBox = block.getTextBox();
        if (textBox != null) {
            return Math.max(letters.getTotalWidth(), textBox.width);
        }
        return letters.getTotalWidth();
    }

    /**
     * @return line count
     */
    public int getLineCount() {
        if (needRefresh) {
            assemble();
        }
        return block.getLineCount();
    }

    public LineWrapMode getLineWrapMode() {
        return block.getLineWrapMode();
    }

    /**
     * Available only when bounding is set. <code>setBox()</code> method call is needed in advance.
     * true when
     *
     * @param wrap NoWrap   : Letters over the text bound is not shown. the last character is set to '...'(0x2026)
     *             Character: Character is split at the end of the line.
     *             Word     : Word is split at the end of the line.
     *             Clip     : The text is hard-clipped at the border including showing only a partial letter if it goes beyond the text bound.
     */
    public void setLineWrapMode(LineWrapMode wrap) {
        if (block.getLineWrapMode() != wrap) {
            block.setLineWrapMode(wrap);
            letters.invalidate();
            needRefresh = true;
        }
    }

    public BitmapFont.Align getAlignment() {
        return block.getAlignment();
    }

    /**
     * Set horizontal alignment. Applicable only when text bound is set.
     *
     * @param align
     */
    public void setAlignment(BitmapFont.Align align) {
        if (block.getTextBox() == null && align != BitmapFont.Align.Left) {
            throw new RuntimeException("Bound is not set");
        }
        block.setAlignment(align);
        letters.invalidate();
        needRefresh = true;
    }

    public BitmapFont.VAlign getVerticalAlignment() {
        return block.getVerticalAlignment();
    }

    /**
     * Set vertical alignment. Applicable only when text bound is set.
     *
     * @param align
     */
    public void setVerticalAlignment(BitmapFont.VAlign align) {
        if (block.getTextBox() == null && align != BitmapFont.VAlign.Top) {
            throw new RuntimeException("Bound is not set");
        }
        block.setVerticalAlignment(align);
        letters.invalidate();
        needRefresh = true;
    }

    /**
     * Set the font style of substring. If font doesn't contain style, default style is used
     *
     * @param start start index to set style. inclusive.
     * @param end   end index to set style. EXCLUSIVE.
     * @param style
     */
    public void setStyle(int start, int end, int style) {
        letters.setStyle(start, end, style);
    }

    /**
     * Set the font style of substring. If font doesn't contain style, default style is applied
     *
     * @param regexp regular expression
     * @param style
     */
    public void setStyle(String regexp, int style) {
        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(block.getText());
        while (m.find()) {
            setStyle(m.start(), m.end(), style);
        }
    }

    /**
     * Set the color of substring.
     *
     * @param start start index to set style. inclusive.
     * @param end   end index to set style. EXCLUSIVE.
     * @param color
     */
    public void setColor(int start, int end, ColorRGBA color) {
        letters.setColor(start, end, color);
        letters.invalidate();
        needRefresh = true;
    }

    /**
     * Set the color of substring.
     *
     * @param regexp regular expression
     * @param color
     */
    public void setColor(String regexp, ColorRGBA color) {
        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(block.getText());
        while (m.find()) {
            letters.setColor(m.start(), m.end(), color);
        }
        letters.invalidate();
        needRefresh = true;
    }

    /**
     * @param tabs tab positions
     */
    public void setTabPosition(float... tabs) {
        block.setTabPosition(tabs);
        letters.invalidate();
        needRefresh = true;
    }

    /**
     * used for the tabs over the last tab position.
     *
     * @param width tab size
     */
    public void setTabWidth(float width) {
        block.setTabWidth(width);
        letters.invalidate();
        needRefresh = true;
    }

    /**
     * for setLineWrapType(LineWrapType.NoWrap),
     * set the last character when the text exceeds the bound.
     *
     * @param c
     */
    public void setEllipsisChar(char c) {
        block.setEllipsisChar(c);
        letters.invalidate();
        needRefresh = true;
    }

    @Override
    public void updateLogicalState(float tpf) {
        super.updateLogicalState(tpf);
        if (needRefresh) {
            assemble();
        }
    }

    private void assemble() {
        // first generate quadlist
        letters.update();
        for (int i = 0; i < textPages.length; i++) {
            textPages[i].assemble(letters);
        }
        needRefresh = false;
    }

    private ColorRGBA getColor(Material mat, String name) {
        MatParam mp = mat.getParam(name);
        if (mp == null) {
            return null;
        }
        return (ColorRGBA) mp.getValue();
    }

    public void render(RenderManager rm, ColorRGBA color) {
        for (BitmapTextPage page : textPages) {
            Material mat = page.getMaterial();
            mat.setTexture("ColorMap", page.getTexture());
            //ColorRGBA original = getColor(mat, "Color");
            //mat.setColor("Color", color);
            mat.render(page, rm);

            //if( original == null ) {
            //    mat.clearParam("Color");
            //} else {
            //    mat.setColor("Color", original);
            //}
        }
    }
}

