package mini.editor.ui.dialog.node.selector;

import com.ss.rlib.common.util.array.Array;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mini.editor.annotation.FxThread;
import mini.editor.ui.control.model.ModelNodeTree;
import mini.editor.ui.css.CssClasses;
import mini.editor.ui.dialog.AbstractSimpleEditorDialog;
import mini.scene.Spatial;

import java.util.function.Consumer;

public class NodeSelectorDialog<T> extends AbstractSimpleEditorDialog {
    private final Spatial model;
    private final Class<T> type;
    private final Consumer<T> handler;
    private ModelNodeTree nodeTree;

    public NodeSelectorDialog(
            final Spatial model,
            final Class<T> type,
            final Consumer<T> handler) {
        this.model = model;
        this.type = type;
        this.handler = handler;

        final ModelNodeTree nodeTree = getNodeTree();
        nodeTree.fill(getModel());
        nodeTree.getTreeView().setEditable(false);

        final Button okButton = getOKButton();
        okButton.setDisable(true);
    }

    @Override
    protected void createBeforeActions(HBox container) {
    }

    @Override
    protected void createAdditionalActions(HBox container) {
    }

    @Override // TODO: Is this necessary when we know isGridStructure?
    protected void createContent(VBox root) {
    }

    @Override
    protected boolean isGridStructure() {
        return true;
    }

    @Override
    @FxThread
    protected void createContent(GridPane container) {
        nodeTree = new ModelNodeTree(this::processSelect, null, SelectionMode.SINGLE);
        nodeTree.prefHeightProperty().bind(heightProperty());
        nodeTree.prefWidthProperty().bind(widthProperty());

        container.add(nodeTree, 0, 0);

        container.getStyleClass().add(CssClasses.MODE_SELECTOR_DIALOG);
    }

    private void processSelect(final Array<Object> objects) {
        throw new UnsupportedOperationException();
    }

    public ModelNodeTree getNodeTree() {
        return nodeTree;
    }

    public Spatial getModel() {
        return model;
    }
}
