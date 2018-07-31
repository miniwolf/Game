package mini.editor.ui.component.splt.pane;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import mini.editor.annotation.FxThread;
import mini.editor.ui.component.editor.state.EditorToolConfig;
import mini.editor.ui.component.tab.TabToolComponent;

public class EditorToolSplitPane extends TabToolSplitPane<EditorToolConfig> {
    private final Region root;

    public EditorToolSplitPane(Scene scene, Region root) {
        super(scene, null);
        this.root = root;
    }

    @Override
    @FxThread
    public void initFor(TabToolComponent toolComponent, Node another) {
        super.initFor(toolComponent, another);
        root.widthProperty().addListener(((observable, oldValue, newValue) -> {
            handleSceneChanged(getSceneSize());
        }));
    }

    @Override
    @FxThread
    protected double getSceneSize() {
        var width = root.getWidth();
        return Double.compare(width, 0D) == 0 ? scene.getWidth() : width;
    }
}
