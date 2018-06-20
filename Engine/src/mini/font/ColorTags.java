package mini.font;

import mini.math.ColorRGBA;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains the color information tagged in a text string
 * Format: \#rgb#
 * \#rgba#
 * \#rrggbb#
 * \#rrggbbaa#
 *
 * @author YongHoon
 */
class ColorTags {
    private static final Pattern
            colorPattern = Pattern.compile("\\\\#([0-9a-fA-F]{8})#|\\\\#([0-9a-fA-F]{6})#|" +
                                           "\\\\#([0-9a-fA-F]{4})#|\\\\#([0-9a-fA-F]{3})#");
    private LinkedList<Range> colors = new LinkedList<Range>();
    private String text;
    private String original;
    private float baseAlpha = -1;

    ColorTags() {
    }

    ColorTags(String seq) {
        setText(seq);
    }

    /**
     * @return text without color tags
     */
    String getPlainText() {
        return text;
    }

    LinkedList<Range> getTags() {
        return colors;
    }

    void setText(final String charSeq) {
        original = charSeq;
        colors.clear();
        if (charSeq == null) {
            return;
        }
        Matcher m = colorPattern.matcher(charSeq);
        if (m.find()) {
            StringBuilder builder = new StringBuilder(charSeq.length() - 7);
            int startIndex = 0;
            do {
                String colorStr = null;
                for (int i = 1; i <= 4 && colorStr == null; i++) {
                    colorStr = m.group(i);
                }
                builder.append(charSeq.subSequence(startIndex, m.start()));
                Range range = new Range(builder.length(), colorStr);
                startIndex = m.end();
                colors.add(range);
            } while (m.find());
            builder.append(charSeq.subSequence(startIndex, charSeq.length()));
            text = builder.toString();
        } else {
            text = charSeq;
        }
    }

    void setBaseAlpha(float alpha) {
        this.baseAlpha = alpha;
        if (alpha == -1) {
            // Need to reinitialize from the original text
            setText(original);
            return;
        }

        // Else set the alpha for all of them
        for (Range r : colors) {
            r.color.a = alpha;
        }
    }

    /**
     * Sets the colors of all ranges, overriding any color tags
     * that were in the original text.
     */
    void setBaseColor(ColorRGBA color) {
        // There are times when the alpha is directly modified
        // and the caller may have passed a constant... so we
        // should clone it.
        color = color.clone();
        for (Range r : colors) {
            r.color = color;
        }
    }

    class Range {
        int start;
        ColorRGBA color;

        Range(int start, String colorStr) {
            this.start = start;
            this.color = new ColorRGBA();

            if (colorStr.length() >= 6) {
                readHexColor(colorStr);
				return;
            }

            readColor(colorStr);
        }

        private void readHexColor(String colorStr) {
            color.r = readHexColorValue(colorStr, 0);
            color.g = readHexColorValue(colorStr, 1);
            color.b = readHexColorValue(colorStr, 2);

            if (baseAlpha != -1) {
                color.a = baseAlpha;
            } else if (colorStr.length() == 8) {
                color.a = readHexColorValue(colorStr, 3);
            } else {
                color.a = 0;
            }
        }

        private float readHexColorValue(String colorStr, int index) {
            int start = index * 2;
            int end = start + 2;
            return Integer.parseInt(colorStr.subSequence(start, end).toString(), 16) / 255f;
        }

        private void readColor(String colorStr) {
            color.r = readColorValue(colorStr, 0);
            color.g = readColorValue(colorStr, 1);
            color.b = readColorValue(colorStr, 2);

            if (baseAlpha != -1) {
                color.a = baseAlpha;
            } else if (colorStr.length() == 4) {
                readColorValue(colorStr, 3);
            } else {
                color.a = 0;
            }
        }

        private float readColorValue(String colorStr, int index) {
            return Integer.parseInt(Character.toString(colorStr.charAt(index)), 16) / 15f;
        }
    }
}
