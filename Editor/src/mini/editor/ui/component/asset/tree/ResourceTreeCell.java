package mini.editor.ui.component.asset.tree;

import javafx.geometry.Side;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import mini.editor.annotation.FxThread;
import mini.editor.ui.FxConstants;
import mini.editor.ui.component.asset.tree.resource.FolderResourceElement;
import mini.editor.ui.component.asset.tree.resource.LoadingResourceElement;
import mini.editor.ui.component.asset.tree.resource.ResourceElement;

public class ResourceTreeCell extends TreeCell<ResourceElement> {

    private Tooltip tooltip;

    protected ResourceTreeCell() {
        setOnMouseClicked(this::handleMouseClickedEvent);
    }

    @FxThread
    private void handleMouseClickedEvent(MouseEvent event) {
        var item = getItem();
        if (item == null) {
            return;
        }

        var isFolder = item instanceof FolderResourceElement;
        var treeView = (ResourceTree) getTreeView();

        if (event.getButton() == MouseButton.SECONDARY) {
            var contextMenu = treeView.getContextMenu(item);
            if (contextMenu == null) {
                return;
            }

            contextMenu.show(this, Side.BOTTOM, 0, 0);
        } else if ((treeView.isOnlyFolders() || !isFolder) &&
                   event.getButton() == MouseButton.PRIMARY && event.getClickCount() > 1) {
            var openFunction = treeView.getOpenFunction();
            if (openFunction != null) {
                openFunction.accept(item);
            }
        }
    }

    @Override
    @FxThread
    protected void updateItem(ResourceElement item, boolean empty) {
        super.updateItem(item, empty);

        removeToolTip();
        if (item == null) {
            setText("");
            setGraphic(null);
            return;
        } else if (item instanceof LoadingResourceElement) {
            setText("Loading...");
            setGraphic(createIndicator());
            return;
        }

        var file = item.getFile();
        var fileName = file.getFileName();
        //var folder = item instanceof FolderResourceElement;

        setText(fileName == null ? file.toString() : fileName.toString());
        createToolTip();
    }

    @FxThread
    private void createToolTip() {
        tooltip = getItem().createToolTip();
        if (tooltip == null) {
            return;
        }

        Tooltip.install(this, tooltip);
    }

    @FxThread
    private void removeToolTip() {
        if (tooltip == null) {
            return;
        }

        Tooltip.uninstall(this, tooltip);
        tooltip = null;
    }

    @FxThread
    private ProgressIndicator createIndicator() {
        var indicator = new ProgressIndicator();
        indicator.setMaxHeight(FxConstants.RESOURCE_TREE_CELL_HEIGHT - 2);
        indicator.setMaxWidth(FxConstants.RESOURCE_TREE_CELL_HEIGHT - 2);
        return indicator;
    }
}
