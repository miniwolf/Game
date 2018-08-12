package mini.editor.plugin;

import com.ss.rlib.common.plugin.PluginContainer;
import com.ss.rlib.common.plugin.impl.BasePlugin;
import mini.editor.file.converter.FileConverterRegistry;
import mini.editor.ui.component.asset.tree.AssetTreeContextMenuFillerRegistry;
import mini.editor.ui.component.editor.EditorRegistry;
import mini.editor.ui.control.property.builder.PropertyBuilderRegistry;
import mini.editor.ui.control.tree.node.factory.TreeNodeFactoryRegistry;
import mini.editor.ui.css.CssRegistry;

public class EditorPlugin extends BasePlugin {
    public EditorPlugin(PluginContainer container) {
        super(container);
    }

    public void register(CssRegistry registry) {
    }

    public void register(FileConverterRegistry registry) {
    }

    public void register(EditorRegistry instance) {

    }

    public void register(AssetTreeContextMenuFillerRegistry instance) {

    }

    public void register(TreeNodeFactoryRegistry instance) {

    }

    public void register(PropertyBuilderRegistry instance) {

    }
}
