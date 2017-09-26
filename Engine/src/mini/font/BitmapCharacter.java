package mini.font;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a single bitmap character.
 */
public class BitmapCharacter implements Cloneable {
    private char c;
    private int x;
    private int y;
    private int width;
    private int height;
    private int xOffset;
    private int yOffset;
    private int xAdvance;
    private Map<Integer, Integer> kerning = new HashMap<>();
    private int page;

    public BitmapCharacter() {
    }

    public BitmapCharacter(char c) {
        this.c = c;
    }

    @Override
    public BitmapCharacter clone() {
        try {
            BitmapCharacter result = (BitmapCharacter) super.clone();
            result.kerning = new HashMap<>(kerning);
            return result;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
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

    public int getXOffset() {
        return xOffset;
    }

    public void setXOffset(int offset) {
        xOffset = offset;
    }

    public int getYOffset() {
        return yOffset;
    }

    public void setYOffset(int offset) {
        yOffset = offset;
    }

    public int getXAdvance() {
        return xAdvance;
    }

    public void setXAdvance(int advance) {
        xAdvance = advance;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public char getChar() {
        return c;
    }

    public void setChar(char c) {
        this.c = c;
    }

    public void addKerning(int second, int amount) {
        kerning.put(second, amount);
    }

    public int getKerning(int second) {
        Integer i = kerning.get(second);
        return i == null ? 0 : i;
    }
}