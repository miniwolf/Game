package mini.editor.ui.component.asset;

import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import javafx.scene.layout.VBox;
import mini.editor.annotation.FromAnyThread;
import mini.editor.annotation.FxThread;
import mini.editor.config.EditorConfig;
import mini.editor.ui.component.asset.tree.ResourceTree;
import mini.editor.ui.css.CssClasses;
import mini.editor.ui.css.CssIds;
import mini.editor.ui.event.FXEventManager;
import mini.editor.ui.event.impl.ChangedCurrentAssetFolderEvent;

import java.nio.file.Path;

/**
 * Component to work with asset tree
 */
public class AssetComponent extends VBox {

    private static final FXEventManager FX_EVENT_MANAGER = FXEventManager.getInstance();

    /**
     * The list of waited files to be selected
     */
    private final Array<Path> waitedFilesToSelect;
    /**
     * For ignoring expand changes
     */
    private boolean ignoreExpanded;
    /**
     * The toolbar of this component
     */
    private AssetBarComponent barComponent;
    private ResourceTree resourceTree;

    public AssetComponent() {
        waitedFilesToSelect = ArrayFactory.newArray(Path.class);
        setId(CssIds.ASSET_COMPONENT);
        createComponents();

        FX_EVENT_MANAGER.addEventHandler(ChangedCurrentAssetFolderEvent.EVENT_TYPE,
                                         event -> switchAssetFolder());
    }

    @FxThread
    private void switchAssetFolder() {
        loadAssetFolder();
    }

    @FxThread
    private void loadAssetFolder() {
        var editorConfig = EditorConfig.getInstance();
        var currentAsset = editorConfig.getCurrentAsset();
        if (currentAsset == null) {
            return;
        }

        getResourceTree().fill(currentAsset);
    }

    private void createComponents() {
        setIgnoreExpanded(true);

        barComponent = new AssetBarComponent();

        resourceTree = new ResourceTree(false);
        // TODO: Setup handlers, add barComponent to resource tree
        resourceTree.prefHeightProperty().bind(heightProperty());
        resourceTree.getStyleClass().add(CssClasses.TRANSPARENT_LIST_VIEW);
        getChildren().add(resourceTree);
    }

    @FromAnyThread
    public boolean isIgnoreExpanded() {
        return ignoreExpanded;
    }

    @FromAnyThread
    public void setIgnoreExpanded(boolean ignoreExpanded) {
        this.ignoreExpanded = ignoreExpanded;
    }

    @FxThread
    private ResourceTree getResourceTree() {
        return resourceTree;
    }
}
