package mini.editor.ui.component.asset.tree;

import com.ss.rlib.common.function.IntObjectConsumer;
import com.ss.rlib.common.util.StringUtils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayComparator;
import com.ss.rlib.common.util.array.ArrayFactory;
import com.ss.rlib.common.util.array.ConcurrentArray;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import mini.editor.annotation.BackgroundThread;
import mini.editor.annotation.FromAnyThread;
import mini.editor.annotation.FxThread;
import mini.editor.config.EditorConfig;
import mini.editor.manager.ExecutorManager;
import mini.editor.ui.FxConstants;
import mini.editor.ui.component.asset.tree.context.menu.action.OpenFileAction;
import mini.editor.ui.component.asset.tree.resource.FolderResourceElement;
import mini.editor.ui.component.asset.tree.resource.FoldersResourceElement;
import mini.editor.ui.component.asset.tree.resource.LoadingResourceElement;
import mini.editor.ui.component.asset.tree.resource.ResourceElement;
import mini.editor.ui.component.asset.tree.resource.ResourceElementFactory;
import mini.editor.util.ObjectsUtil;
import mini.editor.util.UIUtils;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class ResourceTree extends TreeView<ResourceElement> {
    private static final ExecutorManager EXECUTOR_MANAGER = ExecutorManager.getInstance();

    private static final ArrayComparator<ResourceElement> NAME_COMPARATOR = (first, second) -> {
        var firstLevel = getLevel(first);
        var secondLevel = getLevel(second);

        if (firstLevel != secondLevel) {
            return firstLevel - secondLevel;
        }

        return StringUtils.compareIgnoreCase(getNameToSort(first), getNameToSort(second));
    };
    private static final Consumer<ResourceElement> DEFAULT_OPEN_FUNCTION =
            element -> new OpenFileAction(element)
                    .getOnAction()
                    .handle(null);
    private static final AssetTreeContextMenuFillerRegistry CONTEXT_MENU_FILLER_REGISTRY
            = AssetTreeContextMenuFillerRegistry.getInstance();

    private final Consumer<ResourceElement> openFunction;
    private final boolean readonly;
    private final ConcurrentArray<ResourceElement> selectedElements;
    private final EventHandler<TreeItem.TreeModificationEvent<Object>> treeItemEventHandler;

    /**
     * List of filtered extensions.
     */
    private Array<String> extensionFilter;
    /**
     * The post loading handler
     */
    private Consumer<Boolean> onLoadHandler;
    private IntObjectConsumer<ResourceTree> expandHandler;

    private boolean lazyMode;
    private boolean needCleanup;
    private boolean onlyFolders;

    public ResourceTree(boolean readonly) {
        this(DEFAULT_OPEN_FUNCTION, readonly);
    }

    public ResourceTree(Consumer<ResourceElement> openFunction, boolean readonly) {
        this.openFunction = openFunction;
        this.readonly = readonly;
        extensionFilter = ArrayFactory.newArray(String.class, 0);
        selectedElements = ArrayFactory.newConcurrentAtomicARSWLockArray(ResourceElement.class);
        treeItemEventHandler = this::processChangedExpands;

        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        setFixedCellSize(FxConstants.RESOURCE_TREE_CELL_HEIGHT);
        setCellFactory(param -> new ResourceTreeCell());
        setOnKeyPressed(this::processKey);
        setShowRoot(true);
        setContextMenu(new ContextMenu());
        setFocusTraversable(true);

        rootProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.removeEventHandler(TreeItem.treeNotificationEvent(), treeItemEventHandler);
            }
            if (newValue != null) {
                newValue.addEventHandler(TreeItem.treeNotificationEvent(), treeItemEventHandler);
            }
        });
    }

    private static String getNameToSort(ResourceElement element) {
        var file = ObjectsUtil.notNull(element).getFile();
        var fileName = file.getFileName();
        return fileName == null ? file.toString() : fileName.toString();
    }

    @FromAnyThread
    private static int getLevel(ResourceElement element) {
        return element instanceof FolderResourceElement ? 1 : 2;
    }

    /**
     * @return whether only folders should be shown.
     */
    public boolean isOnlyFolders() {
        return onlyFolders;
    }

    /**
     * @param onlyFolders whether to show only folders.
     */
    public void setOnlyFolders(boolean onlyFolders) {
        this.onlyFolders = onlyFolders;
    }

    /**
     * Expand the file in the tree
     */
    @FxThread
    public void expandTo(Path file, boolean needSelect) {
        if (isLazyMode()) {
            var targetItem = UIUtils.findItemForValue(getRoot(), file);
            if (targetItem == null) {
                TreeItem<ResourceElement> parentItem = null;
                var parent = file.getParent();

                while (parent != null) {
                    parentItem = UIUtils.findItemForValue(getRoot(), parent);
                    if (parentItem != null) {
                        break;
                    }

                    parent = parent.getParent();
                }

                if (parentItem == null) {
                    parentItem = getRoot();
                }

                var toLoad = parentItem;
                EXECUTOR_MANAGER.addBackgroundTask(
                        () -> lazyLoadChildren(toLoad, item -> expandTo(file, needSelect)));
                return;
            }

            var children = targetItem.getChildren();
            if (children.size() == 1 && children.get(0).getValue() == LoadingResourceElement
                    .getInstance()) {
                EXECUTOR_MANAGER.addBackgroundTask(
                        () -> lazyLoadChildren(targetItem, item -> expandTo(file, needSelect)));
                return;
            }
        }

        var element = ResourceElementFactory.createFor(file);
        var treeItem = UIUtils.findItemForValue(getRoot(), element);
        if (treeItem == null) {
            return;
        }

        var parent = treeItem;
        while (parent != null) {
            parent.setExpanded(true);
            parent = parent.getParent();
        }

        if (needSelect) {
            scrollToAndSelect(treeItem);
        }
    }

    @FxThread
    private void lazyLoadChildren() {
        Array<TreeItem<ResourceElement>> expanded = ArrayFactory.newArray(TreeItem.class);

        UIUtils.allItems(getRoot())
               .filter(TreeItem::isExpanded)
               .filter(item -> !item.isLeaf())
               .filter(item -> item.getChildren().size() == 1)
               .filter(item -> item.getChildren().get(0).getValue() == LoadingResourceElement
                       .getInstance())
               .forEach(expanded::add);

        for (var treeItem : expanded) {
            EXECUTOR_MANAGER.addBackgroundTask(() -> lazyLoadChildren(treeItem, null));
        }
    }

    /**
     * Load children of the tree item in the background
     */
    @BackgroundThread
    private void lazyLoadChildren(
            TreeItem<ResourceElement> treeItem,
            Consumer<TreeItem<ResourceElement>> callback) {
        var element = treeItem.getValue();
        var children = element.getChildren(extensionFilter, isOnlyFolders());
        if (children == null) {
            return;
        }

        children.sort(NAME_COMPARATOR);

        EXECUTOR_MANAGER.addFXTask(() -> lazyLoadChildren(treeItem, children, callback));
    }

    private void lazyLoadChildren(
            TreeItem<ResourceElement> treeItem,
            Array<ResourceElement> children,
            Consumer<TreeItem<ResourceElement>> callback) {
        var items = treeItem.getChildren();
        if (items.size() != 1 || items.get(0).getValue() != LoadingResourceElement.getInstance()) {
            if (callback != null) {
                callback.accept(treeItem);
            }
            return;
        }

        children.forEach(child -> items.add(new TreeItem<>(child)));

        items.remove(0);
        items.forEach(this::fill);

        if (isNeedCleanup()) {
            //cleanup(treeItem);
        }

        if (callback != null) {
            callback.accept(treeItem);
        }
    }

    @FromAnyThread
    private void scrollToAndSelect(TreeItem<ResourceElement> treeItem) {
        EXECUTOR_MANAGER.addFXTask(() -> {
            var selectionModel = getSelectionModel();
            selectionModel.clearSelection();
            selectionModel.select(treeItem);
            scrollTo(getRow(treeItem));
        });
    }

    protected ContextMenu getContextMenu(ResourceElement element) {
        if (isReadonly()) {
            return null;
        }

        var contextMenu = new ContextMenu();
        var items = contextMenu.getItems();

        var selectionModel = getSelectionModel();
        var selectedItems = selectionModel.getSelectedItems();

        if (selectedItems.size() == 1) {
            for (var filler : CONTEXT_MENU_FILLER_REGISTRY.getSingleFillers()) {
                filler.fill(element, items);
            }
        }

        if (selectedItems.size() > 1) {
            throw new UnsupportedOperationException();
        }

        if (items.isEmpty()) {
            return null;
        }

        return contextMenu;
    }

    /**
     * Fill the tree using a single root folder
     */
    @FxThread
    public void fill(Path rootFolder) {
        prepareToFill();
        EXECUTOR_MANAGER.addBackgroundTask(() -> startBackgroundFill(rootFolder));
    }

    /**
     * Fill the tree using a list of root folders
     *
     * @param rootFolders
     */
    @FxThread
    public void fill(Array<Path> rootFolders) {
        prepareToFill();
        EXECUTOR_MANAGER.addBackgroundTask(() -> startBackgroundFill(rootFolders));
    }

    @FxThread
    private void prepareToFill() {
        var onLoadHandler = getOnLoadHandler();
        if (onLoadHandler != null) {
            onLoadHandler.accept(false);
        }

        var currentRoot = getRoot();
        if (currentRoot != null) {
            setRoot(null);
        }

        showLoading();
    }

    @FxThread
    private void showLoading() {
        setRoot(new TreeItem<>(LoadingResourceElement.getInstance()));
    }

    /**
     * Fill the node
     */
    @FxThread
    private void fill(TreeItem<ResourceElement> treeItem) {
        var element = treeItem.getValue();
        var extensionsFilter = getExtensionFilter();

        if (!element.hasChildren(extensionsFilter, isOnlyFolders())) {
            return;
        }

        var items = treeItem.getChildren();

        if (isLazyMode()) {
            items.add(new TreeItem<>(LoadingResourceElement.getInstance()));
        } else {
            var children = element.getChildren(extensionsFilter, isOnlyFolders());
            if (children == null) {
                return;
            }

            children.sort(NAME_COMPARATOR);
            children.forEach(child -> items.add(new TreeItem<>(child)));

            items.forEach(this::fill);
        }
    }

    @FxThread
    private void processChangedExpands(TreeItem.TreeModificationEvent<?> event) {
        if (!(event.wasExpanded() || event.wasCollapsed())) {
            return;
        }

        if (isLazyMode()) {
            EXECUTOR_MANAGER.addFXTask(this::lazyLoadChildren);
        }

        getExpandHandler().ifPresent(handler ->
                                             handler.accept(getExpandedItemCount(), this));

        // repaint
    }

    @FxThread
    private void processKey(KeyEvent event) {
        var editorConfig = EditorConfig.getInstance();
        var currentAsset = editorConfig.getCurrentAsset();
        if (currentAsset == null) {
            return;
        }

        updateSelectedElements();

        var selectedElements = getSelectedElements();
        if (selectedElements.isEmpty()) {
            return;
        }

        var firstElement = selectedElements.first();
        if (firstElement instanceof LoadingResourceElement) {
            return;
        }

        boolean onlyFiles = true;
        boolean selectedAsset = false;

        for (var element : selectedElements.array()) {
            if (element == null) {
                break;
            }

            if (element instanceof FolderResourceElement) {
                onlyFiles = false;
            }

            if (Objects.equals(currentAsset, element.getFile())) {
                selectedAsset = true;
            }
        }

        var keyCode = event.getCode();
        var controlDown = event.isControlDown();

        if (!currentAsset.equals(firstElement.getFile())) {
            if (controlDown) {
                if (keyCode == KeyCode.C) {
                    throw new UnsupportedOperationException();
                }
                // key code C then copy action
                if (keyCode == KeyCode.X) {
                    throw new UnsupportedOperationException();
                }
                // key code X then cut action
            } else if (keyCode == KeyCode.DELETE) {
                throw new UnsupportedOperationException();
            }
        }

        if (controlDown && keyCode == KeyCode.V) {
            throw new UnsupportedOperationException();
        }
    }

    @FxThread
    private void updateSelectedElements() {
        var elements = getSelectedElements();

        var stamp = elements.writeLock();

        try {
            elements.clear();

            getSelectionModel()
                    .getSelectedItems()
                    .forEach(item -> elements.add(item.getValue()));
        } finally {
            elements.writeUnlock(stamp);
        }
    }

    /**
     * Start the background process of filling
     */
    @BackgroundThread
    private void startBackgroundFill(Path path) {
        var rootElement = ResourceElementFactory.createFor(path);
        var newRoot = new TreeItem<>(rootElement);
        newRoot.setExpanded(true);

        fill(newRoot);

        if (!isLazyMode() && isNeedCleanup()) {
            // cleanup(newRoot);
        }

        EXECUTOR_MANAGER.addFXTask(() -> applyNewRoot(newRoot));
    }

    /**
     * @param paths
     */
    @BackgroundThread
    private void startBackgroundFill(Array<Path> paths) {
        var rootElement = new FoldersResourceElement(paths);
        var newRoot = new TreeItem<ResourceElement>(rootElement);
        newRoot.setExpanded(true);

        fill(newRoot);

        if (!isLazyMode() && isNeedCleanup()) {
            // cleanup(newRoot);
        }

        EXECUTOR_MANAGER.addFXTask(() -> applyNewRoot(newRoot));
    }

    private void applyNewRoot(TreeItem<ResourceElement> newRoot) {
        setRoot(newRoot);

        var onLoadHandler = getOnLoadHandler();
        if (onLoadHandler != null) {
            onLoadHandler.accept(true);
        }

        // TODO: Maybe repaint?
    }

    /**
     * @return the post loading handler.
     */
    @FromAnyThread
    private Consumer<Boolean> getOnLoadHandler() {
        return onLoadHandler;
    }

    @FromAnyThread
    public void setOnLoadHandler(Consumer<Boolean> onLoadHandler) {
        this.onLoadHandler = onLoadHandler;
    }

    /**
     * @return the list of filtered extensions.
     */
    @FromAnyThread
    private Array<String> getExtensionFilter() {
        return extensionFilter;
    }

    /**
     * @return the open resource function
     */
    public Consumer<ResourceElement> getOpenFunction() {
        return openFunction;
    }

    @FromAnyThread
    public ConcurrentArray<ResourceElement> getSelectedElements() {
        return selectedElements;
    }

    @FromAnyThread
    public Optional<IntObjectConsumer<ResourceTree>> getExpandHandler() {
        return Optional.ofNullable(expandHandler);
    }

    public void setExpandHandler(IntObjectConsumer<ResourceTree> expandHandler) {
        this.expandHandler = expandHandler;
    }

    public boolean isLazyMode() {
        return lazyMode;
    }

    public void setLazyMode(boolean lazyMode) {
        this.lazyMode = lazyMode;
    }

    public boolean isNeedCleanup() {
        return needCleanup;
    }

    public boolean isReadonly() {
        return readonly;
    }
}
