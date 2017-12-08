package mini.asset.plugins;

import mini.asset.AssetInfo;
import mini.asset.AssetKey;
import mini.asset.AssetLoadException;
import mini.asset.AssetLocator;
import mini.asset.AssetManager;

import java.io.File;
import java.io.IOException;

public class FileLocator implements AssetLocator {
    private File root;

    @Override
    public void setRootPath(String rootPath) {
        if (rootPath == null) {
            throw new NullPointerException();
        }

        try {
            root = new File(rootPath).getCanonicalFile();
            if (!root.isDirectory()) {
                throw new IllegalArgumentException(
                        "Given root path \"" + root + "\" is not a directory.");
            }
        } catch (IOException ex) {
            throw new AssetLoadException("Root path is invalid", ex);
        }
    }

    @Override
    public AssetInfo locate(AssetManager manager, AssetKey key) {
        return null;
    }
}
