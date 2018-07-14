package mini.editor.manager;

import com.ss.rlib.common.plugin.ConfigurablePluginSystem;
import com.ss.rlib.common.plugin.impl.PluginSystemFactory;
import mini.editor.config.Config;
import mini.editor.plugin.EditorPlugin;

import java.util.function.Consumer;

public class PluginManager {
    private static final PluginManager INSTANCE = new PluginManager();

    private final ConfigurablePluginSystem pluginSystem;

    public PluginManager() {
        pluginSystem = PluginSystemFactory.newBasePluginSystem(getClass().getClassLoader());
        pluginSystem.setAppVersion(Config.APP_VERSION);
    }

    public static PluginManager getInstance() {
        return INSTANCE;
    }

    public void handlePlugins(Consumer<EditorPlugin> consumer) {
        pluginSystem.getPlugins().stream()
                    .filter(EditorPlugin.class::isInstance)
                    .map(EditorPlugin.class::cast)
                    .forEach(consumer);
    }
}
