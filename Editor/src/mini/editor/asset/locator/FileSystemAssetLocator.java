package mini.editor.asset.locator;

import com.ss.rlib.common.util.ArrayUtils;
import com.ss.rlib.common.util.array.ArrayFactory;
import com.ss.rlib.common.util.array.ConcurrentArray;
import mini.asset.AssetInfo;
import mini.asset.AssetKey;
import mini.asset.AssetLocator;
import mini.asset.AssetManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

public class FileSystemAssetLocator implements AssetLocator {
    private static final ConcurrentArray<AssetKey<?>> LOCATED_KEYS = ArrayFactory
            .newConcurrentStampedLockArray(AssetKey.class);

    @Override
    public void setRootPath(String rootPath) {
    }

    @Override
    public AssetInfo locate(AssetManager manager, AssetKey key) {
        final Path absoluteFile = Paths.get(key.getName());
        if (!Files.exists(absoluteFile)) {
            return null;
        }

        ArrayUtils.runInWriteLock(LOCATED_KEYS, key, Collection::add);

        return new FolderAssetLocator.PathAssetInfo(manager, key, absoluteFile);
    }
}
