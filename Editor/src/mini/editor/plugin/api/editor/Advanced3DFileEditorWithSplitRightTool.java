package mini.editor.plugin.api.editor;

import javafx.scene.control.SplitPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import mini.editor.annotation.FxThread;
import mini.editor.plugin.api.editor.part3d.Advanced3DEditorPart;
import mini.editor.ui.component.editor.state.impl.Editor3DWithEditorToolEditorState;
import mini.editor.ui.css.CssClasses;

public abstract class Advanced3DFileEditorWithSplitRightTool<T extends Advanced3DEditorPart,
        S extends Editor3DWithEditorToolEditorState>
        extends Advanced3DFileEditorWithRightTool<T, S> {

    @FxThread
    protected Region buildSplitComponent(
            final javafx.scene.Node first,
            final javafx.scene.Node second,
            final StackPane root) {
        final SplitPane splitPane = new SplitPane(first, second);
        splitPane.prefHeightProperty().bind(root.heightProperty());
        splitPane.prefWidthProperty().bind(root.widthProperty());

        root.heightProperty()
            .addListener(((observable, oldValue, newValue) -> calcVSplitSize(splitPane)));

        splitPane.getStyleClass().add(CssClasses.FILE_EDITOR_TOOL_SPLIT_PANE);

        return splitPane;
    }

    @FxThread
    private void calcVSplitSize(SplitPane splitPane) {
        splitPane.setDividerPosition(0, 0.3);
    }
}
