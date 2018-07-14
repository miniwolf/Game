package mini.editor.ui.control.tree;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.layout.HBox;
import mini.editor.annotation.FxThread;
import mini.editor.model.undo.editor.ChangeConsumer;
import mini.editor.ui.css.CssClasses;
import mini.editor.util.UIUtils;

public class NodeTreeCell<C extends ChangeConsumer, M extends NodeTree<C>>
        extends TextFieldTreeCell<TreeNode<?>> {

    private static final PseudoClass EDITING_PSEUDO_CLASS = PseudoClass.getPseudoClass("editing");

    private final BooleanProperty editing = new BooleanPropertyBase(false) {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(EDITING_PSEUDO_CLASS, get());
        }

        @Override
        public Object getBean() {
            return NodeTreeCell.this;
        }

        @Override
        public String getName() {
            return "editing";
        }
    };

    private final HBox content;
    private final Label text;

    private M nodeTree;
    private boolean ignoreUpdate;

    public NodeTreeCell(final M nodeTree) {
        this.nodeTree = nodeTree;
        content = new HBox();
        text = new Label();

        // Add things like text, icon, etc. to it.
        content.getChildren().add(text);

        content.getStyleClass().add(CssClasses.DEF_HBOX);
        this.getStyleClass().add(CssClasses.ABSTRACT_NODE_TREE_CELL);
    }

    @Override
    @FxThread
    public void startEdit() {
        if (!isEditable()) {
            return;
        }

        final TreeItem<TreeNode<?>> treeItem = getTreeItem();
        if (treeItem != null) {
            treeItem.setGraphic(null);
        }

        setIgnoreUpdate(true);
        try {
            super.startEdit();
        } finally {
            setIgnoreUpdate(false);
        }

        UIUtils.updateEditedCell(this);
        editing.setValue(true);
    }

    @Override
    @FxThread
    public void cancelEdit() {
        super.cancelEdit();

        editing.setValue(false);

        final TreeItem<TreeNode<?>> treeItem = getTreeItem();
        if (treeItem != null) {
            treeItem.setGraphic(content);
        }

        setText("");
    }

    @Override
    @FxThread
    public void commitEdit(TreeNode<?> newValue) {
        super.commitEdit(newValue);

        editing.setValue(false);

        final TreeItem<TreeNode<?>> treeItem = getTreeItem();
        if (treeItem != null) {
            treeItem.setGraphic(content);
        }

        setText("");
    }

    @Override
    @FxThread
    public void updateItem(final TreeNode<?> item, final boolean empty) {
        super.updateItem(item, empty);

        if (isIgnoreUpdate()) {
            return;
        }

        final TreeItem<TreeNode<?>> treeItem = getTreeItem();
        if (treeItem != null) {
            treeItem.setGraphic(content);
        }

        // TODO: HideableNode

        text.setText(item.getName());

        setText("");
        setEditable(item.canEditName());
    }

    public boolean isIgnoreUpdate() {
        return ignoreUpdate;
    }

    public void setIgnoreUpdate(boolean ignoreUpdate) {
        this.ignoreUpdate = ignoreUpdate;
    }
}
