package mini.editor.ui.control.tree;

import com.ss.rlib.common.function.TripleConsumer;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayCollectors;
import com.ss.rlib.common.util.array.ArrayFactory;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import mini.editor.annotation.FxThread;
import mini.editor.model.undo.editor.ChangeConsumer;
import mini.editor.ui.control.tree.node.factory.TreeNodeFactoryRegistry;
import mini.editor.util.LocalObjects;
import mini.editor.util.ObjectsUtil;
import mini.editor.util.UIUtils;

import java.util.List;
import java.util.function.Consumer;

public class NodeTree<C extends ChangeConsumer> extends VBox {

    private static final TreeNodeFactoryRegistry
            FACTORY_REGISTRY = TreeNodeFactoryRegistry.getInstance();
    private static final Array<TripleConsumer<NodeTree<?>, List<MenuItem>, Array<TreeNode<?>>>>
            MULTI_ITEMS_ACTION_FILLERS =
            ArrayFactory.newArray(TripleConsumer.class);
    private final SelectionMode selectionMode;
    private final C changeConsumer;
    private final Consumer<Array<Object>> selectionHandler;
    private TreeView<TreeNode<?>> treeView;

    public NodeTree(final Consumer<Array<Object>> selectionHandler, final C consumer, SelectionMode selectionMode) {
        this.selectionHandler = selectionHandler;
        this.changeConsumer = consumer;
        this.selectionMode = selectionMode;
        createComponents();
    }

    @FxThread
    public static void register(
            TripleConsumer<NodeTree<?>, List<MenuItem>, Array<TreeNode<?>>> actionFiller) {
        MULTI_ITEMS_ACTION_FILLERS.add(actionFiller);
    }

    @FxThread
    private void createComponents() {
        treeView = new TreeView<>();
        treeView.setCellFactory(param -> createNodeTreeCell());
        treeView.setShowRoot(true);
        treeView.setEditable(true);
        treeView.setFocusTraversable(true);
        treeView.prefHeightProperty().bind(heightProperty());
        treeView.prefWidthProperty().bind(widthProperty());

        final MultipleSelectionModel<TreeItem<TreeNode<?>>> selectionModel = treeView
                .getSelectionModel();
        selectionModel.setSelectionMode(selectionMode);
        selectionModel.selectedItemProperty().addListener(this::updateSelection);

        this.getChildren().add(treeView);
    }

    @FxThread
    private void updateSelection(
            final ObservableValue<? extends TreeItem<TreeNode<?>>> observableValue,
            final TreeItem<TreeNode<?>> oldValue,
            final TreeItem<TreeNode<?>> treeItem) {
        final ObservableList<TreeItem<TreeNode<?>>> selectedItems = getTreeView()
                .getSelectionModel()
                .getSelectedItems();

        final Array<Object> objects = LocalObjects.get().nextObjectArray();
        objects.clear();

        for (final TreeItem<TreeNode<?>> selectedItem : selectedItems) {
            if (selectedItem == null) {
                continue;
            }

            objects.add(selectedItem);
        }

        selectionHandler.accept(objects);
    }

    @FxThread
    private NodeTreeCell<C, ?> createNodeTreeCell() {
        return new NodeTreeCell<>(this);
    }

    /**
     * Fill the tree using the object and its children
     */
    @FxThread
    public void fill(Object object) {
        var treeView = getTreeView();
        var currentRoot = treeView.getRoot();

        if (currentRoot != null) {
            treeView.setRoot(null);
        }

        var rootElement = FACTORY_REGISTRY.createFor(object);
        var newRoot = new TreeItem<TreeNode<?>>(rootElement);
        newRoot.setExpanded(true);

        fill(newRoot, false, 1);
        treeView.setRoot(newRoot);
    }

    @FxThread
    private void fill(TreeItem<TreeNode<?>> treeItem, boolean expanded, int level) {
        treeItem.setExpanded(expanded || level == 1);

        var element = treeItem.getValue();
        if (!element.hasChildren(this)) {
            return;
        }

        var items = treeItem.getChildren();
        var children = element.getChildren(this);
        children.forEach(child -> {
            element.notifyChildPreAdd(child);
            items.add(new TreeItem<>(child));
            element.notifyChildAdded(child);
        });

        items.forEach(item -> fill(item, expanded, level == -1 ? -1 : level + 1));
    }

    /**
     * @return the tree describing this model.
     */
    @FxThread
    public TreeView<TreeNode<?>> getTreeView() {
        return ObjectsUtil.notNull(treeView);
    }

    @FxThread
    public C getChangeConsumer() {
        return changeConsumer;
    }

    /**
     * @return the selected nodes from the treeview
     */
    @FxThread
    public Array<TreeNode<?>> getSelectedItems() {
        final TreeView<TreeNode<?>> treeView = getTreeView();
        return treeView.getSelectionModel()
                       .getSelectedItems()
                       .stream()
                       .map(TreeItem::getValue)
                       .collect(ArrayCollectors.toArray(TreeNode.class));
    }

    public void notifyRemoved(final Object parent, final Object child) {
        final TreeItem<TreeNode<?>> treeItem = tryToFindItem(parent, child);
        if (treeItem == null) {
            return;
        }

        final TreeItem<TreeNode<?>> parentItem = treeItem.getParent();
        final TreeNode<?> parentNode = parentItem.getValue();
        final TreeNode<?> node = treeItem.getValue();

        final ObservableList<TreeItem<TreeNode<?>>> children = parentItem.getChildren();
        parentNode.notifyChildPreRemove(node);
        children.remove(treeItem);
        parentNode.notifyChildRemoved(node);

        if (parentItem.isExpanded() && children.isEmpty()) {
            parentItem.setExpanded(false);
        }
    }

    private TreeItem<TreeNode<?>> tryToFindItem(final Object parent, final Object child) {
        final TreeView<TreeNode<?>> treeView = getTreeView();
        final TreeItem<TreeNode<?>> treeItem;

        if (parent != null) {
            final TreeItem<TreeNode<?>> parentItem = UIUtils.findItemForValue(treeView, parent);
            if (parentItem == null) {
                return null;
            }
            treeItem = UIUtils.findItemForValue(parentItem, child);
        } else {
            treeItem = UIUtils.findItemForValue(treeView, child);
        }
        return treeItem;
    }

    @FxThread
    public Object getSelectedObject() {
        final TreeView<TreeNode<?>> treeView = getTreeView();
        final MultipleSelectionModel<TreeItem<TreeNode<?>>> selectionModel = treeView.getSelectionModel();
        final TreeItem<TreeNode<?>> selectedItem = selectionModel.getSelectedItem();
        if (selectedItem == null) {
            return null;
        }

        final TreeNode<?> treeNode = selectedItem.getValue();
        return treeNode.getElement();
    }

    @FxThread
    public int getSelectedCount() {
        final TreeView<TreeNode<?>> treeView = getTreeView();
        return treeView.getSelectionModel()
                .getSelectedItems().size();
    }

    @FxThread
    public void selectSingle(final Object object) {
        final TreeView<TreeNode<?>> treeView = getTreeView();
        final MultipleSelectionModel<TreeItem<TreeNode<?>>> selectionModel = treeView.getSelectionModel();

        if (object == null) {
            selectionModel.clearSelection();
            return;
        }

        final TreeNode<Object> treeNode = FACTORY_REGISTRY.createFor(object);
        final TreeItem<TreeNode<?>> treeItem = UIUtils.findItemForValue(treeView, treeNode);
        if (treeItem == null) {
            selectionModel.clearSelection();
            return;
        }

        selectionModel.clearSelection();
        selectionModel.select(treeItem);
    }

    @FxThread
    public void notifyAdded(
            final Object parent,
            final Object child,
            final int index) {
        if (child == null || parent == null) {
            return;
        }

        final TreeView<TreeNode<?>> treeView = getTreeView();
        final TreeItem<TreeNode<?>> parentItem = UIUtils.findItemForValue(treeView, parent);
        if (parentItem == null || UIUtils.findItemForValue(parentItem, child) != null) {
            return;
        }

        final TreeNode<?> childNode = FACTORY_REGISTRY.createFor(child);
        if (childNode == null) {
            return;
        }

        final TreeNode<?> parentNode = parentItem.getValue();
        parentNode.notifyChildPreAdd(childNode);

        final TreeItem<TreeNode<?>> childItem = new TreeItem<>(childNode);
        final ObservableList<TreeItem<TreeNode<?>>> children = parentItem.getChildren();

        if (index == -1) {
            children.add(childItem);
        } else {
            children.add(index, childItem);
        }

        parentItem.setExpanded(true);
        parentNode.notifyChildAdded(childNode);

        fill(childItem, false, -1);
    }
}