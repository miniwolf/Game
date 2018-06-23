package mini.editor.config;

import com.ss.rlib.common.util.ClassUtils;
import com.ss.rlib.common.util.ObjectUtils;
import com.ss.rlib.common.util.Utils;
import com.ss.rlib.common.util.dictionary.ConcurrentObjectDictionary;
import com.ss.rlib.common.util.dictionary.DictionaryFactory;
import com.ss.rlib.common.util.dictionary.DictionaryUtils;
import mini.asset.AssetEventListener;
import mini.asset.AssetKey;
import mini.editor.MiniEditor;
import mini.editor.annotation.FromAnyThread;
import mini.editor.injfx.MiniToJfxIntegrator;
import mini.editor.util.ObjectsUtil;
import mini.math.Vector3f;
import mini.system.ApplicationSettings;

import java.awt.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static mini.editor.config.DefaultSettingsProvider.Defaults.PREF_DEFAULT_FRAME_RATE;
import static mini.editor.config.DefaultSettingsProvider.Defaults.PREF_DEFAULT_GAMMA_CORRECTION;
import static mini.editor.config.DefaultSettingsProvider.Preferences.PREF_FRAME_RATE;
import static mini.editor.config.DefaultSettingsProvider.Preferences.PREF_GAMMA_CORRECTION;

public class EditorConfig implements AssetEventListener {

    private static final String SCREEN_ALIAS = "screen";
    private static final String ASSET_ALIAS = "asset";
    private static final String OTHER_ALIAS = "other";

    private static final String PREF_SCREEN_WIDTH = SCREEN_ALIAS + ".width";
    private static final String PREF_SCREEN_HEIGHT = SCREEN_ALIAS + ".height";
    private static final String PREF_SCREEN_MAXIMIZED = SCREEN_ALIAS + ".maximized";

    private static final String PREF_OTHER_GLOBAL_LEFT_TOOL_WIDTH = OTHER_ALIAS
                                                                    + ".global.leftTool.width";
    private static final String PREF_OTHER_GLOBAL_LEFT_TOOL_COLLAPSED = OTHER_ALIAS
                                                                        + ".global.leftTool.collapsed";
    private static final String PREF_OTHER_GLOBAL_BOTTOM_TOOL_WIDTH = OTHER_ALIAS
                                                                      + ".global.bottomTool.width";
    private static final String PREF_OTHER_GLOBAL_BOTTOM_TOOL_COLLAPSED = OTHER_ALIAS
                                                                          + ".global.bottomTool.collapsed";

    private static final String PREF_ASSET_CURRENT_ASSET = ASSET_ALIAS + ".current";

    private static volatile EditorConfig instance;
    private final ConcurrentObjectDictionary<String, Object> settings;
    private final List<String> lastOpenedAssets;
    /**
     * The current asset folder.
     */
    private volatile Path currentAsset;
    /**
     * The width of this screen
     */
    private volatile int screenWidth;
    /**
     * The height of this screen
     */
    private volatile int screenHeight;
    /**
     * Flag is for maximizing a window
     */
    private volatile boolean maximized;
    /**
     * The global left tool width
     */
    private volatile int globalLeftToolWidth;
    /**
     * Flag is for collapsing the global left tool
     */
    private volatile boolean globalLeftToolCollapsed;
    private int globalBottomToolWidth;
    private boolean globalBottomToolCollapsed;

    public EditorConfig() {
        lastOpenedAssets = new ArrayList<>();
        settings = DictionaryFactory.newConcurrentAtomicObjectDictionary();
        initialize();
    }

    public static EditorConfig getInstance() {
        if (instance == null) {
            synchronized (EditorConfig.class) {
                if (instance == null) {
                    instance = new EditorConfig();
                }
            }
        }
        return instance;
    }

    private void initialize() {
        var preferences = Preferences.userNodeForPackage(MiniEditor.class);
        var stamp = settings.writeLock();
        try {
            for (String key: preferences.keys()) {
                settings.put(key, preferences.get(key, null));
            }
        } catch (BackingStoreException e) {
            throw new RuntimeException();
        } finally {
            settings.writeUnlock(stamp);
        }

        maximized = preferences.getBoolean(PREF_SCREEN_MAXIMIZED, false);
        screenHeight = preferences.getInt(PREF_SCREEN_HEIGHT, 800);
        screenWidth = preferences.getInt(PREF_SCREEN_WIDTH, 1200);
        globalLeftToolWidth = preferences.getInt(PREF_OTHER_GLOBAL_LEFT_TOOL_WIDTH, 300);
        globalLeftToolCollapsed = preferences
                .getBoolean(PREF_OTHER_GLOBAL_LEFT_TOOL_COLLAPSED, false);
        globalBottomToolWidth = preferences.getInt(PREF_OTHER_GLOBAL_BOTTOM_TOOL_WIDTH, 300);
        globalBottomToolCollapsed = preferences
                .getBoolean(PREF_OTHER_GLOBAL_BOTTOM_TOOL_COLLAPSED, false);

        var currentAssetURI = preferences.get(PREF_ASSET_CURRENT_ASSET, null);

        if (currentAssetURI != null) {
            currentAsset = Utils.get(currentAssetURI, uri -> Paths.get(new URI(uri)));
        }

        if (currentAsset != null && !Files.exists(currentAsset)) {
            this.currentAsset = null;
        }

    }

    @Override
    public void assetDependencyNotFound(AssetKey parentKey, AssetKey dependentAssetKey) {

    }

    @Override
    public <T> void assetLoaded(AssetKey<T> key) {

    }

    /**
     * @return the current asset folder.
     */
    public Path getCurrentAsset() {
        return currentAsset;
    }

    /**
     * Set the current asset folder.
     */
    @FromAnyThread
    public void setCurrentAsset(Path currentAsset) {
        this.currentAsset = currentAsset;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(int screenHeight) {
        this.screenWidth = screenHeight;
    }

    public boolean isMaximized() {
        return maximized;
    }

    public int getGlobalLeftToolWidth() {
        return globalLeftToolWidth;
    }

    public void setGlobalLeftToolWidth(int globalLeftToolWidth) {
        this.globalLeftToolWidth = globalLeftToolWidth;
    }

    public boolean isGlobalLeftToolCollapsed() {
        return globalLeftToolCollapsed;
    }

    public void setGlobalLeftToolCollapsed(boolean globalLeftToolCollapsed) {
        this.globalLeftToolCollapsed = globalLeftToolCollapsed;
    }

    public int getGlobalBottomToolWidth() {
        return globalBottomToolWidth;
    }

    public void setGlobalBottomToolWidth(int globalBottomToolWidth) {
        this.globalBottomToolWidth = globalBottomToolWidth;
    }

    public boolean isGlobalBottomToolCollapsed() {
        return globalBottomToolCollapsed;
    }

    public void setGlobalBottomToolCollapsed(boolean globalBottomToolCollapsed) {
        this.globalBottomToolCollapsed = globalBottomToolCollapsed;
    }

    public ApplicationSettings getSettings() {
        var graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        var device = graphicsEnvironment.getDefaultScreenDevice();
        var displayMode = device.getDisplayMode();

        var settings = new ApplicationSettings(true);
        settings.setFrequency(displayMode.getRefreshRate());
        settings.setGammaCorrection(
                getBoolean(PREF_GAMMA_CORRECTION, PREF_DEFAULT_GAMMA_CORRECTION));
        settings.setResizable(true);

        MiniToJfxIntegrator.prepareSettings(settings,
                                            getInteger(PREF_FRAME_RATE, PREF_DEFAULT_FRAME_RATE));

        return settings;
    }

    public boolean getBoolean(String id, boolean def) {
        return Boolean.TRUE.equals(get(id, Boolean.class, def));
    }

    public int getInteger(String id, int def) {
        return ObjectUtils.notNull(get(id, Integer.class, def));
    }

    public <T extends Enum<T>> T getEnum(String id, T def) {
        return ObjectsUtil.notNull(get(id, ClassUtils.unsafeCast(def.getClass()), def));
    }

    @FromAnyThread
    private <T> T get(String id, Class<T> type, T def) {
        var value = DictionaryUtils
                .getInReadLock(settings, id, (objects, s) -> objects.get(id));
        if (value == null) {
            return def;
        } else if (type.isInstance(value)) {
            return ClassUtils.unsafeCast(value);
        }

        T result = null;

        if (type == Boolean.class) {
            if (value instanceof String) {
                result = ClassUtils.unsafeCast(Boolean.valueOf(value.toString()));
            }
        } else if (type == Integer.class) {
            if (value instanceof String) {
                result = ClassUtils.unsafeCast(Integer.valueOf(value.toString()));
            }
        } else if (type == Vector3f.class) {
            throw new UnsupportedOperationException();
        } else if (Path.class.isAssignableFrom(type)) {
            if (value instanceof String) {
                var uri = Utils.get(value.toString(), URI::new);
                result = ClassUtils.unsafeCast(Paths.get(uri));
            }
        } else if (Enum.class.isAssignableFrom(type)) {
            final Class<Enum> enumType = ClassUtils.unsafeCast(type);
            if (value instanceof String) {
                var enumValue = Enum.valueOf(enumType, value.toString());
                result = ClassUtils.unsafeCast(enumValue);
            }
        }

        if (result != null) {
            set(id, result);
            return result;
        }

        throw new IllegalArgumentException(
                "Cannot convert the value " + value + "to this type " + type);
    }

    /**
     * Set the new value of the settings by the id.
     *
     * @param id    the setting's id.
     * @param value the setting's value.
     */
    private void set(String id, Object value) {
        var stamp = settings.writeLock();
        try {
            if (value == null) {
                settings.remove(id);
            } else {
                settings.put(id, value);
            }
        } finally {
            settings.writeUnlock(stamp);
        }
    }

    /**
     * Add the new last opened asset folder.
     */
    @FromAnyThread
    public synchronized void addOpenedAsset(Path currentAsset) {
        var filePath = currentAsset.toString();
        var lastOpenedAssets = getLastOpenedAssets();
        lastOpenedAssets.remove(filePath);
        lastOpenedAssets.add(0, filePath);

        if (lastOpenedAssets.size() > 10) {
            lastOpenedAssets.remove(lastOpenedAssets.size() - 1);
        }
    }

    /**
     * @return the list of last opened asset folders.
     */
    public synchronized List<String> getLastOpenedAssets() {
        return lastOpenedAssets;
    }

    public void save() {
        var prefs = Preferences.userNodeForPackage(MiniEditor.class);
        long stamp = settings.readLock();

        try {
            settings.forEach((key, value) -> {
                if (value instanceof Boolean) {
                    prefs.putBoolean(key, (Boolean) value);
                } else if (value instanceof Integer) {
                    prefs.putInt(key, (Integer) value);
                } else if (value instanceof Vector3f) {
                    final Vector3f vector = (Vector3f) value;
                    prefs.put(key, vector.getX() + "," + vector.getY() + "," + vector.getZ());
                } else {
                    prefs.put(key, value.toString());
                }
            });
        } finally {
            settings.readUnlock(stamp);
        }

        if (currentAsset != null && !Files.exists(currentAsset)) {
            currentAsset = null;
        }

        prefs.putInt(PREF_SCREEN_HEIGHT, getScreenHeight());
        prefs.putInt(PREF_SCREEN_WIDTH, getScreenWidth());
        prefs.putInt(PREF_OTHER_GLOBAL_BOTTOM_TOOL_WIDTH, getGlobalBottomToolWidth());
        prefs.putBoolean(PREF_OTHER_GLOBAL_BOTTOM_TOOL_COLLAPSED, isGlobalBottomToolCollapsed());
        prefs.putInt(PREF_OTHER_GLOBAL_LEFT_TOOL_WIDTH, getGlobalLeftToolWidth());
        prefs.putBoolean(PREF_OTHER_GLOBAL_LEFT_TOOL_COLLAPSED, isGlobalLeftToolCollapsed());

        if (currentAsset != null) {
            prefs.put(PREF_ASSET_CURRENT_ASSET, currentAsset.toUri().toString());
        } else {
            prefs.remove(PREF_ASSET_CURRENT_ASSET);
        }

        try {
            prefs.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }
}
