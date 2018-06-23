package mini.editor.ui.component.splt.pane;

import javafx.scene.Scene;
import mini.editor.config.EditorConfig;

public class GlobalBottomToolSplitPane extends TabToolSplitPane<EditorConfig> {
    public GlobalBottomToolSplitPane(Scene scene) {
        super(scene, EditorConfig.getInstance());
    }

    @Override
    protected int loadSize() {
        return getConfig().getGlobalBottomToolWidth();
    }

    @Override
    protected boolean loadCollapsed() {
        return getConfig().isGlobalBottomToolCollapsed();
    }
}
