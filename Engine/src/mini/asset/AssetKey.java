package mini.asset;

import mini.utils.MyFile;

import java.util.Objects;

/**
 * <code>AssetKey</code> is a key that is used to
 * look up a resource from a cache.
 * This class should be immutable.
 */
public class AssetKey<T> {
    protected MyFile filename;

    public AssetKey(MyFile filename) {
        this.filename = filename;
    }

    public AssetKey() {
    }

    public MyFile getFile() {
        return filename;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AssetKey<?> assetKey = (AssetKey<?>) o;
        return Objects.equals(filename, assetKey.filename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filename);
    }
}
