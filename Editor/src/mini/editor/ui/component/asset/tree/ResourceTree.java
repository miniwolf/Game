package mini.editor.ui.component.asset.tree;

import com.ss.rlib.common.util.StringUtils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayComparator;
import com.ss.rlib.common.util.array.ArrayFactory;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import mini.editor.annotation.BackgroundThread;
import mini.editor.annotation.FromAnyThread;
import mini.editor.annotation.FxThread;
import mini.editor.manager.ExecutorManager;
import mini.editor.ui.component.asset.tree.resource.FolderResourceElement;
import mini.editor.ui.component.asset.tree.resource.FoldersResourceElement;
import mini.editor.ui.component.asset.tree.resource.LoadingResourceElement;
import mini.editor.ui.component.asset.tree.resource.ResourceElement;
import mini.editor.ui.component.asset.tree.resource.ResourceElementFactory;
import mini.editor.util.ObjectsUtil;
import mini.editor.util.UIUtils;

import java.nio.file.Path;
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
    private boolean onlyFolders;
    /**
     * List of filtered extensions.
     */
    private Array<String> extensionFilter;
    /**
     * The post loading handler
     */
    private Consumer<Boolean> onLoadHandler;

    public ResourceTree() {
        extensionFilter = ArrayFactory.newArray(String.class, 0);
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

    @FromAnyThread
    private void scrollToAndSelect(TreeItem<ResourceElement> treeItem) {
        EXECUTOR_MANAGER.addFXTask(() -> {
            var selectionModel = getSelectionModel();
            selectionModel.clearSelection();
            selectionModel.select(treeItem);
            scrollTo(getRow(treeItem));
        });
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

        var children = element.getChildren(extensionsFilter, isOnlyFolders());
        if (children == null) {
            return;
        }

        children.sort(NAME_COMPARATOR);
        children.forEach(child -> items.add(new TreeItem<>(child)));

        items.forEach(this::fill);
    }

    private void startBackgroundFill(Array<Path> paths) {
        var rootElement = new FoldersResourceElement(paths);
        var newRoot = new TreeItem<ResourceElement>(rootElement);
        newRoot.setExpanded(true);

        fill(newRoot);

        EXECUTOR_MANAGER.addFXTask(() -> applyNewRoot(newRoot));
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
}
