package mini.asset.plugins;

import mini.asset.AssetInfo;
import mini.asset.AssetKey;
import mini.asset.AssetLoadException;
import mini.asset.AssetLocator;
import mini.asset.AssetManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * <code>FileLocator</code> allows you to specify a folder where to look for assets.
 *
 * @author miniwolf
 */
public class FileLocator implements AssetLocator {
    private File root;

    @Override
    public AssetInfo locate(AssetManager manager, AssetKey key) {
        String name = key.getName();
        File file = new File(root, name);
        if (!file.exists() || !file.isFile()) {
            return null;
        }

        try {
            String canonicalPath = file.getCanonicalPath();
            String absolutePath = file.getAbsolutePath();
            if (!canonicalPath.endsWith(absolutePath)) {
                throw new IllegalArgumentException("Asset name doesn't match requirements.\n" +
                                                   "\"" + canonicalPath + "\" does not match \"" +
                                                   absolutePath + "\""
                );
            }
            return new AssetInfoFile(manager, key, file);
        } catch (IOException e) {
            System.err.println("Filed to get file canonical path " + file);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void setRootPath(String rootPath) {
        try {
            root = new File(rootPath).getCanonicalFile();
            if (!root.isDirectory()) {
                throw new IllegalArgumentException(
                        "Given root path \"" + root + "\" is not a directory");
            }
        } catch (IOException e) {
            throw new AssetLoadException("Root path is invalid", e);
        }
    }

    private static class AssetInfoFile extends AssetInfo {
        private File file;

        public AssetInfoFile(AssetManager manager, AssetKey key, File file) {
            super(manager, key);
            this.file = file;
        }

        @Override
        public InputStream openStream() {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                // NOTE: Can still happen if file.exists is true, e.g. permission issues and similar
                throw new AssetLoadException("Failed to open file: " + file, e);
            }
        }
    }
}
