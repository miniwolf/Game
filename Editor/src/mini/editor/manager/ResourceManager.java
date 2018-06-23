package mini.editor.manager;

import com.ss.rlib.common.util.ArrayUtils;
import com.ss.rlib.common.util.FileUtils;
import com.ss.rlib.common.util.Utils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import com.ss.rlib.common.util.dictionary.DictionaryFactory;
import com.ss.rlib.common.util.dictionary.ObjectDictionary;
import com.ss.rlib.common.util.ref.Reference;
import mini.asset.AssetEventListener;
import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.editor.FileExtensions;
import mini.editor.config.EditorConfig;
import mini.editor.util.EditorUtil;
import mini.editor.util.SimpleFileVisitor;
import mini.editor.util.SimpleFolderVisitor;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static mini.editor.util.ObjectsUtil.notNull;

/**
 * Manage working with resources of an editor
 */
public class ResourceManager implements AssetEventListener {
    private static final WatchService WATCH_SERVICE;
    private static ResourceManager instance;

    static {
        try {
            WATCH_SERVICE = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private final ObjectDictionary<String, Reference> assetCacheTable;
    private final ObjectDictionary<String, Array<String>> interestedResources;
    private Array<WatchKey> watchKeys;
    private Array<URLClassLoader> classLoaders;

    public ResourceManager() {
        assetCacheTable = DictionaryFactory.newObjectDictionary();
        interestedResources = DictionaryFactory.newObjectDictionary();
        watchKeys = ArrayFactory.newArray(WatchKey.class);
        classLoaders = ArrayFactory.newArray(URLClassLoader.class);
    }

    public static ResourceManager getInstance() {
        if (instance == null) {
            instance = new ResourceManager();
        }
        return instance;
    }

    @Override
    public void assetDependencyNotFound(AssetKey parentKey, AssetKey dependentAssetKey) {

    }

    @Override
    public <T> void assetLoaded(AssetKey<T> key) {

    }

    public void reload() {
        ObjectDictionary<String, Reference> lastModifyTable = getAssetCacheTable();
        lastModifyTable.clear();

        Array<WatchKey> watchKeys = getWatchKeys();
        watchKeys.forEach(WatchKey::cancel);
        watchKeys.clear();

        final AssetManager assetManager = EditorUtil.getAssetManager();

        final Array<URLClassLoader> classLoaders = getClassLoaders();
        classLoaders.forEach(assetManager, (loader, manager) -> manager.removeClassLoader(loader));
        classLoaders.clear();

        assetManager.clearCache();

        final ObjectDictionary<String, Array<String>> interestedSources = getInterestedSources();
        interestedSources.forEach((extension, resources) -> resources.clear());

        final EditorConfig editorConfig = EditorConfig.getInstance();
        final Path currentAsset = editorConfig.getCurrentAsset();
        if (currentAsset == null) {
            return;
        }

        try {
            Files.walkFileTree(currentAsset, (SimpleFileVisitor) (file, attrs) -> handleFile(file));
        } catch (IOException e) {
            System.err.println("Warning: " + e.getMessage());
        }

        try {
            watchKeys.add(currentAsset.register(WATCH_SERVICE, ENTRY_CREATE, ENTRY_DELETE,
                                                ENTRY_MODIFY));
            Files.walkFileTree(currentAsset,
                               (SimpleFolderVisitor) (file, attrs) -> registerFiles(watchKeys,
                                                                                    file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void registerFiles(Array<WatchKey> watchKeys, Path file) {
        watchKeys.add(Utils.get(file, toRegister -> toRegister
                .register(WATCH_SERVICE, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY)));
    }

    private synchronized void handleFile(final Path file) {
        if (Files.isDirectory(file)) {
            return;
        }

        final String extension = FileUtils.getExtension(file);

        final ObjectDictionary<String, Array<String>> interestedSources = getInterestedSources();
        final Array<String> toStore = interestedSources.get(extension);

        if (toStore != null) {
            final Path assetFile = notNull(EditorUtil.getAssetFile(file),
                                           "Not found asset file for " + file);
            toStore.add(EditorUtil.toAssetPath(assetFile));
        }

        if (extension.endsWith(FileExtensions.JAVA_LIBRARY)) {
            final AssetManager assetManager = EditorUtil.getAssetManager();
            final URL url = Utils.get(file, FileUtils::toUrl);

            final Array<URLClassLoader> classLoaders = getClassLoaders();
            final URLClassLoader oldLoader = classLoaders.search(url,
                                                                 (loader, toCheck) -> ArrayUtils
                                                                         .contains(loader.getURLs(),
                                                                                   toCheck));
            if (oldLoader != null) {
                return;
            }

            final URLClassLoader newLoader = new URLClassLoader(ArrayFactory.toArray(url),
                                                                getClass().getClassLoader());
            classLoaders.add(newLoader);
            assetManager.addClassLoader(newLoader);
        }
    }

    private ObjectDictionary<String, Array<String>> getInterestedSources() {
        return interestedResources;
    }

    private ObjectDictionary<String, Reference> getAssetCacheTable() {
        return assetCacheTable;
    }

    public Array<WatchKey> getWatchKeys() {
        return watchKeys;
    }

    public Array<URLClassLoader> getClassLoaders() {
        return classLoaders;
    }
}
