package mini.font;

import java.util.HashMap;
import java.util.Map;

public class BitmapCharacterSet {

    private int lineHeight;
    private int base;
    private int renderedSize;
    private int width;
    private int height;
    private Map<Integer, Map<Integer, BitmapCharacter>> characters;
    private int pageSize;

    public BitmapCharacterSet() {
        characters = new HashMap<>();
    }

    public BitmapCharacter getCharacter(int index) {
        return getCharacter(index, 0);
    }

    public BitmapCharacter getCharacter(int index, int style) {
        return getCharacterSet(style).get(index);
    }

    private Map<Integer, BitmapCharacter> getCharacterSet(int style) {
        if (characters.size() == 0) {
            characters.put(style, new HashMap<>());
        }
        return characters.get(style);
    }

    public void addCharacter(int index, BitmapCharacter ch) {
        getCharacterSet(0).put(index, ch);
    }

    public int getLineHeight() {
        return lineHeight;
    }

    public void setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
    }

    public int getBase() {
        return base;
    }

    public void setBase(int base) {
        this.base = base;
    }

    public int getRenderedSize() {
        return renderedSize;
    }

    public void setRenderedSize(int renderedSize) {
        this.renderedSize = renderedSize;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Merge two fonts.
     * If two font have the same style, merge will fail.
     *
     * @param styleSet Style must be assigned to this.
     * @author Yonghoon
     */
    public void merge(BitmapCharacterSet styleSet) {
        if (this.renderedSize != styleSet.renderedSize) {
            throw new RuntimeException("Only support same font size");
        }
        for (Integer style : styleSet.characters.keySet()) {
            if (style == 0) {
                throw new RuntimeException("Style must be set first. use setStyle(int)");
            }
            Map<Integer, BitmapCharacter> charset = styleSet.characters.get(style);
            this.lineHeight = Math.max(this.lineHeight, styleSet.lineHeight);
            Map<Integer, BitmapCharacter> old = this.characters.put(style, charset);
            if (old != null) {
                throw new RuntimeException("Can't override old style");
            }

            for (BitmapCharacter ch : charset.values()) {
                ch.setPage(ch.getPage() + this.pageSize);
            }
        }
        this.pageSize += styleSet.pageSize;
    }

//    public void setStyle(int style) {
//        if (characters.size() > 1) {
//            throw new RuntimeException("Applicable only for single style font");
//        }
//        Map.Entry<Integer, Map<Integer, BitmapCharacter>> entry = characters.iterator().next();
//        Map<Integer, BitmapCharacter> charset = entry.getValue();
//        characters.remove(entry.getKey());
//        characters.put(style, charset);
//    }

    void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}