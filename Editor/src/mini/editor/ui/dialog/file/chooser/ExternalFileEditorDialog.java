package mini.editor.ui.dialog.file.chooser;

import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import mini.editor.annotation.FxThread;
import mini.editor.manager.ExecutorManager;
import mini.editor.ui.component.asset.tree.ResourceTree;
import mini.editor.ui.component.asset.tree.resource.ResourceElement;
import mini.editor.ui.css.CssClasses;
import mini.editor.ui.dialog.AbstractSimpleEditorDialog;
import mini.editor.util.ObjectsUtil;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class ExternalFileEditorDialog extends AbstractSimpleEditorDialog {
    private static final ExecutorManager EXECUTOR_MANAGER = ExecutorManager.getInstance();

    /**
     * Function to handle a selected folder.
     */
    protected final Consumer<Path> consumer;

    protected ResourceTree resourceTree;

    private Path initDirectory;

    public ExternalFileEditorDialog(final Consumer<Path> consumer) {
        this.consumer = consumer;
    }

    @Override
    @FxThread
    protected void createContent(final VBox root) {
        resourceTree = new ResourceTree(this::processOpen, false);
        resourceTree.prefHeightProperty().bind(heightProperty());
        resourceTree.prefWidthProperty().bind(widthProperty());
        resourceTree.setLazyMode(true);
        resourceTree.setShowRoot(false);
        resourceTree.getSelectionModel()
                    .selectedItemProperty()
                    .addListener((observable, oldValue, newValue) -> processSelected(newValue));

        root.getChildren().add(resourceTree);
        root.getStyleClass().add(CssClasses.OPEN_EXTERNAL_FOLDER_EDITOR_DIALOG);
    }

    @Override
    protected void createContent(GridPane container) {
    }

    @Override
    protected void processOK() {
        super.processOK();

        final ResourceElement element = getResourceTree()
                .getSelectionModel()
                .getSelectedItem()
                .getValue();

        consumer.accept(element.getFile());
    }

    @FxThread
    private void processOpen(final ResourceElement element) {
        final Button okButton = getOKButton();
        if (okButton == null || okButton.isDisabled()) {
            return;
        }

        hide();
        consumer.accept(element.getFile());
    }

    @Override
    public void show(Window owner) {
        super.show(owner);

        Path initDirectory = getInitDirectory();
        if (initDirectory == null) {
            initDirectory = Paths.get(System.getProperty("user.home"));
        }

        final Path toExpand = initDirectory;
        final Array<Path> rootFolders = ArrayFactory.newArray(Path.class);

        final FileSystem fileSystem = FileSystems.getDefault();
        fileSystem.getRootDirectories().forEach(rootFolders::add);

        final ResourceTree resourceTree = getResourceTree();
        resourceTree.setOnLoadHandler(finished -> {
            if (finished) {
                resourceTree.expandTo(toExpand, true);
            }
        });

        if (rootFolders.size() < 2) {
            resourceTree.fill(ObjectsUtil.notNull(rootFolders.first()));
        } else {
            resourceTree.fill(rootFolders);
        }

        EXECUTOR_MANAGER.addFXTask(resourceTree::requestFocus);
    }

    @FxThread
    private void processSelected(final TreeItem<ResourceElement> newValue) {
        final ResourceElement element = newValue.getValue();
        final Path file = element.getFile();
        final Button okButton = ObjectsUtil.notNull(getOKButton());
        okButton.setDisable(file == null || !Files.isWritable(file));
    }

    public Path getInitDirectory() {
        return initDirectory;
    }

    public void setInitDirectory(final Path initDirectory) {
        this.initDirectory = initDirectory;
    }

    protected ResourceTree getResourceTree() {
        return ObjectsUtil.notNull(resourceTree);
    }

    @Override
    protected void createAdditionalActions(HBox container) {
    }

    @Override
    protected void createBeforeActions(HBox container) {
    }
}
