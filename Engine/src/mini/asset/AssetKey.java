package mini.asset;

import mini.utils.MyFile;

import java.util.Objects;

/**
 * <code>AssetKey</code> is a key that is used to
 * look up a resource from a cache.
 * This class should be immutable.
 */
public class AssetKey<T> {
    protected MyFile file;
    protected String name;
    private String extension;

    public AssetKey(String name) {
        this.file = new MyFile(name);
        this.name = name;
        this.extension = file.getExtension();
    }

    public AssetKey() {
    }

    public AssetKey(AssetKey<T> key) {
        this.file = key.file;
        this.extension = key.extension;
        this.name = key.name;
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
        return Objects.equals(name, assetKey.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * @return The asset path
     */
    public String getName() {
        return name;
    }

    /**
     * @return The extension of the <code>AssetKey</code>'s name. For example the name
     * "Interface/Pictures/Monkey.png" has an extension of "png".
     */
    public String getExtension() {
        return extension.toLowerCase();
    }

    public String getFolder() {
        return file.getDirectory();
    }
}
