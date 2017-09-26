package mini.asset;

import mini.utils.MyFile;

public class FontKey extends AssetKey {
    public FontKey(String name) {
        super(new MyFile(name));
    }

    public FontKey() {
        super();
    }
}
