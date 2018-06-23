package mini.editor.ui.component.asset;

import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import javafx.scene.layout.VBox;
import mini.editor.annotation.FromAnyThread;
import mini.editor.ui.component.asset.tree.ResourceTree;
import mini.editor.ui.css.CssClasses;
import mini.editor.ui.css.CssIds;

import java.nio.file.Path;

/**
 * Component to work with asset tree
 */
public class AssetComponent extends VBox {

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

        // TODO: setup event handlers
    }

    private void createComponents() {
        setIgnoreExpanded(true);

        barComponent = new AssetBarComponent();

        resourceTree = new ResourceTree();
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
}
