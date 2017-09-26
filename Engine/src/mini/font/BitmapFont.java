package mini.font;

import mini.material.Material;

/**
 * Represents a font within jME that is generated with the AngelCode Bitmap Font Generator
 */
public class BitmapFont {

    private BitmapCharacterSet charSet;
    private Material[] pages;

    public BitmapFont() {
    }

    public BitmapText createLabel(String content) {
        BitmapText label = new BitmapText(this);
        label.setSize(getCharSet().getRenderedSize());
        label.setText(content);
        return label;
    }

    public float getPreferredSize() {
        return getCharSet().getRenderedSize();
    }

    public void setPages(Material[] pages) {
        this.pages = pages;
        charSet.setPageSize(pages.length);
    }

    public Material getPage(int index) {
        return pages[index];
    }

    public int getPageSize() {
        return pages.length;
    }

    public BitmapCharacterSet getCharSet() {
        return charSet;
    }

    public void setCharSet(BitmapCharacterSet charSet) {
        this.charSet = charSet;
    }

    /**
     * Gets the line height of a StringBlock.
     *
     * @param sb
     * @return the line height
     */
    public float getLineHeight(StringBlock sb) {
        return charSet.getLineHeight() * (sb.getSize() / charSet.getRenderedSize());
    }

    public float getCharacterAdvance(char curChar, char nextChar, float size) {
        BitmapCharacter c = charSet.getCharacter(curChar);
        if (c == null) {
            return 0f;
        }

        float advance = size * c.getXAdvance();
        advance += c.getKerning(nextChar) * size;
        return advance;
    }

    private int findKerningAmount(int newLineLastChar, int nextChar) {
        BitmapCharacter c = charSet.getCharacter(newLineLastChar);
        if (c == null) {
            return 0;
        }
        return c.getKerning(nextChar);
    }

    public float getLineWidth(CharSequence text) {

        // This method will probably always be a bit of a maintenance
        // nightmare since it basis its calculation on a different
        // routine than the Letters class.  The ideal situation would
        // be to abstract out letter position and size into its own
        // class that both BitmapFont and Letters could use for
        // positioning.
        // If getLineWidth() here ever again returns a different value
        // than Letters does with the same text then it might be better
        // just to create a Letters object for the sole purpose of
        // getting a text size.  It's less efficient but at least it
        // would be accurate.

        // And here I am mucking around in here again...
        //
        // A font character has a few values that are pertinent to the
        // line width:
        //  xOffset
        //  xAdvance
        //  kerningAmount(nextChar)
        //
        // The way BitmapText ultimately works is that the first character
        // starts with xOffset included (ie: it is rendered at -xOffset).
        // Its xAdvance is wider to accomodate that initial offset.
        // The cursor position is advanced by xAdvance each time.
        //
        // So, a width should be calculated in a similar way.  Start with
        // -xOffset + xAdvance for the first character and then each subsequent
        // character is just xAdvance more 'width'.
        //
        // The kerning amount from one character to the next affects the
        // cursor position of that next character and thus the ultimate width
        // and so must be factored in also.

        float lineWidth = 0f;
        float maxLineWidth = 0f;
        char lastChar = 0;
        boolean firstCharOfLine = true;
//        float sizeScale = (float) block.getSize() / charSet.getRenderedSize();
        float sizeScale = 1f;
        for (int i = 0; i < text.length(); i++) {
            char theChar = text.charAt(i);
            if (theChar == '\n') {
                maxLineWidth = Math.max(maxLineWidth, lineWidth);
                lineWidth = 0f;
                firstCharOfLine = true;
                continue;
            }
            BitmapCharacter c = charSet.getCharacter((int) theChar);
            if (c != null) {
                if (theChar == '\\' && i < text.length() - 1 && text.charAt(i + 1) == '#') {
                    if (i + 5 < text.length() && text.charAt(i + 5) == '#') {
                        i += 5;
                        continue;
                    } else if (i + 8 < text.length() && text.charAt(i + 8) == '#') {
                        i += 8;
                        continue;
                    }
                }
                if (!firstCharOfLine) {
                    lineWidth += findKerningAmount(lastChar, theChar) * sizeScale;
                } else {
                    // The first character needs to add in its xOffset but it
                    // is the only one... and negative offsets = postive width
                    // because we're trying to account for the part that hangs
                    // over the left.  So we subtract.
                    lineWidth -= c.getXOffset() * sizeScale;
                    firstCharOfLine = false;
                }
                float xAdvance = c.getXAdvance() * sizeScale;

                // If this is the last character, then we really should have
                // only add its width.  The advance may include extra spacing
                // that we don't care about.
                if (i == text.length() - 1) {
                    lineWidth += c.getWidth() * sizeScale;

                    // Since the width includes the xOffset then we need
                    // to take it out again by adding it, ie: offset the width
                    // we just added by the appropriate amount.
                    lineWidth += c.getXOffset() * sizeScale;
                } else {
                    lineWidth += xAdvance;
                }
            }
        }
        return Math.max(maxLineWidth, lineWidth);
    }

    /**
     * Merge two fonts.
     * If two font have the same style, merge will fail.
     *
     * @param newFont Style must be assigned to this.
     */
    public void merge(BitmapFont newFont) {
        charSet.merge(newFont.charSet);
        final int size1 = this.pages.length;
        final int size2 = newFont.pages.length;

        Material[] tmp = new Material[size1 + size2];
        System.arraycopy(this.pages, 0, tmp, 0, size1);
        System.arraycopy(newFont.pages, 0, tmp, size1, size2);

        this.pages = tmp;

//        this.pages = Arrays.copyOf(this.pages, size1+size2);
//        System.arraycopy(newFont.pages, 0, this.pages, size1, size2);
    }

    /**
     * Specifies horizontal alignment for text.
     *
     * @see BitmapText#setAlignment(com.jme3.font.BitmapFont.Align)
     */
    public enum Align {

        /**
         * Align text on the left of the text block
         */
        Left,

        /**
         * Align text in the center of the text block
         */
        Center,

        /**
         * Align text on the right of the text block
         */
        Right
    }

    /**
     * Specifies vertical alignment for text.
     *
     * @see BitmapText#setVerticalAlignment(com.jme3.font.BitmapFont.VAlign)
     */
    public enum VAlign {
        /**
         * Align text on the top of the text block
         */
        Top,

        /**
         * Align text in the center of the text block
         */
        Center,

        /**
         * Align text at the bottom of the text block
         */
        Bottom
    }

//    public void setStyle(int style) {
//        charSet.setStyle(style);
//    }

}
