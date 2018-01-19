package mini.renderer.niftygui;

import mini.font.BitmapFont;

import java.util.Objects;

public class CachedTextKey {
    private final BitmapFont font;
    private final String str;

    public CachedTextKey(BitmapFont font, String str) {

        this.font = font;
        this.str = str;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CachedTextKey that = (CachedTextKey) o;
        return Objects.equals(font, that.font) &&
               Objects.equals(str, that.str);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + font.hashCode();
        hash = 53 * hash + str.hashCode();
        return hash;
        //return Objects.hash(font, str);
    }

    @Override
    public String toString() {
        return "CachedTextKey{" + "str='" + str + '\'' + '}';
    }
}
