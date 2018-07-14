package mini.editor.asset.locator;

import mini.asset.AssetInfo;
import mini.asset.AssetKey;
import mini.asset.AssetLocator;
import mini.asset.AssetManager;
import mini.editor.annotation.MiniThread;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FolderAssetLocator implements AssetLocator {

    @Override
    public void setRootPath(String rootPath) {

    }

    @Override
    public AssetInfo locate(final AssetManager manager,
                            final AssetKey key) {

        return null;
    }

    public static class PathAssetInfo extends AssetInfo {
        private final Path path;

        public PathAssetInfo(final AssetManager manager,
                             final AssetKey key,
                             final Path path) {
            super(manager, key);
            this.path = path;
        }

        @Override
        @MiniThread
        public InputStream openStream() {
            try {
                return Files.newInputStream(path, StandardOpenOption.READ);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }
}
