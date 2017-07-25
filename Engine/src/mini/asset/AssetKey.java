package mini.asset;

import mini.utils.MyFile;

/**
 * <code>AssetKey</code> is a key that is used to
 * look up a resource from a cache.
 * This class should be immutable.
 */
public class AssetKey {
    protected MyFile filename;

    public AssetKey(MyFile filename) {
        this.filename = filename;
    }

    public AssetKey() {
    }

    public MyFile getFilename() {
        return filename;
    }
}
