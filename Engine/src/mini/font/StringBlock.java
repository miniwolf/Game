package mini.font;

import mini.math.ColorRGBA;

/**
 * Defines a String that is to be drawn in one block that can be constrained by a {@link Rectangle}. Also holds
 * formatting information for the StringBlock
 *
 * @author dhdd
 */
class StringBlock implements Cloneable {

    private String text;
    private Rectangle textBox;
    private BitmapFont.Align alignment = BitmapFont.Align.Left;
    private BitmapFont.VAlign valignment = BitmapFont.VAlign.Top;
    private float size;
    private ColorRGBA color = new ColorRGBA(ColorRGBA.White);
    private boolean kerning;
    private int lineCount;
    private LineWrapMode wrapType = LineWrapMode.Word;
    private float[] tabPos;
    private float tabWidth = 50;
    private char ellipsisChar = 0x2026;

    /**
     * @param text      the text that the StringBlock will hold
     * @param textBox   the rectangle that constrains the text
     * @param alignment the initial alignment of the text
     * @param size      the size in pixels (vertical size of a single line)
     * @param color     the initial color of the text
     * @param kerning
     */
    StringBlock(String text, Rectangle textBox, BitmapFont.Align alignment, float size,
                ColorRGBA color,
                boolean kerning) {
        this.text = text;
        this.textBox = textBox;
        this.alignment = alignment;
        this.size = size;
        this.color.set(color);
        this.kerning = kerning;
    }

    StringBlock() {
        this.text = "";
        this.textBox = null;
        this.alignment = BitmapFont.Align.Left;
        this.size = 100;
        this.color.set(ColorRGBA.White);
        this.kerning = true;
    }

    @Override
    public StringBlock clone() {
        try {
            StringBlock clone = (StringBlock) super.clone();
            clone.color = color.clone();
            if (textBox != null) {
                clone.textBox = textBox.clone();
            }
            return clone;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    String getText() {
        return text;
    }

    void setText(String text) {
        this.text = text == null ? "" : text;
    }

    Rectangle getTextBox() {
        return textBox;
    }

    void setTextBox(Rectangle textBox) {
        this.textBox = textBox;
    }

    BitmapFont.Align getAlignment() {
        return alignment;
    }

    void setAlignment(BitmapFont.Align alignment) {
        this.alignment = alignment;
    }

    BitmapFont.VAlign getVerticalAlignment() {
        return valignment;
    }

    void setVerticalAlignment(BitmapFont.VAlign alignment) {
        this.valignment = alignment;
    }

    float getSize() {
        return size;
    }

    void setSize(float size) {
        this.size = size;
    }

    ColorRGBA getColor() {
        return color;
    }

    void setColor(ColorRGBA color) {
        this.color.set(color);
    }

    boolean isKerning() {
        return kerning;
    }

    void setKerning(boolean kerning) {
        this.kerning = kerning;
    }

    int getLineCount() {
        return lineCount;
    }

    void setLineCount(int lineCount) {
        this.lineCount = lineCount;
    }

    LineWrapMode getLineWrapMode() {
        return wrapType;
    }

    /**
     * available only when bounding is set. <code>setBox()</code> method call is needed in advance.
     *
     * @param wrap true when word need not be split at the end of the line.
     */
    void setLineWrapMode(LineWrapMode wrap) {
        this.wrapType = wrap;
    }

    float getTabWidth() {
        return tabWidth;
    }

    void setTabWidth(float tabWidth) {
        this.tabWidth = tabWidth;
    }

    float[] getTabPosition() {
        return tabPos;
    }

    void setTabPosition(float[] tabs) {
        this.tabPos = tabs;
    }

    int getEllipsisChar() {
        return ellipsisChar;
    }

    void setEllipsisChar(char c) {
        this.ellipsisChar = c;
    }
}