package mini.editor.plugin;

import com.ss.rlib.common.plugin.PluginContainer;
import com.ss.rlib.common.plugin.impl.BasePlugin;
import mini.editor.file.converter.FileConverterRegistry;
import mini.editor.ui.css.CssRegistry;

public class EditorPlugin extends BasePlugin {
    public EditorPlugin(PluginContainer container) {
        super(container);
    }

    public void register(CssRegistry registry) {
    }

    public void register(FileConverterRegistry registry) {
    }
}
