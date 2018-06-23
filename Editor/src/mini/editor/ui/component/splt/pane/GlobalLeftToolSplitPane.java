package mini.editor.ui.component.splt.pane;

import javafx.scene.Scene;
import mini.editor.config.EditorConfig;

public class GlobalLeftToolSplitPane extends TabToolSplitPane<EditorConfig> {
    public GlobalLeftToolSplitPane(Scene scene) {
        super(scene, EditorConfig.getInstance());
    }

    @Override
    protected boolean loadCollapsed() {
        return getConfig().isGlobalLeftToolCollapsed();
    }

    @Override
    protected int loadSize() {
        return getConfig().getGlobalLeftToolWidth();
    }

}
