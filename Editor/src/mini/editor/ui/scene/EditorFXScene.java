package mini.editor.ui.scene;

import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import mini.editor.annotation.FxThread;
import mini.editor.manager.ExecutorManager;
import mini.editor.ui.component.ScreenComponent;
import mini.editor.ui.css.CssIds;
import mini.editor.util.FXUtils;

import java.util.concurrent.atomic.AtomicInteger;

public class EditorFXScene extends Scene {
    private static final ExecutorManager EXECUTOR_MANAGER = ExecutorManager.getInstance();

    /**
     * The container of this scene
     */
    private final StackPane container;
    private final StackPane hideLayer;
    /**
     * View to draw Engine Scene
     */
    private final ImageView canvas;
    private final VBox loadingLayer;

    private final AtomicInteger loadingCount;

    private final Array<ScreenComponent> components;

    private Node focused;
    private ProgressIndicator progreessIndicator;

    public EditorFXScene(Group root) {
        super(root);

        this.canvas = new EditorFXImageView();
        canvas.setMouseTransparent(true);

        loadingCount = new AtomicInteger();

        this.components = ArrayFactory.newArraySet(ScreenComponent.class);
        this.container = new StackPane();
        this.container.setPickOnBounds(false);

        this.hideLayer = new StackPane();
        this.hideLayer.setVisible(false);

        this.loadingLayer = new VBox();
        this.loadingLayer.setId(CssIds.EDITOR_LOADING_LAYER);
        this.loadingLayer.setVisible(false);

        var background = new Pane();
        background.setId(CssIds.ROOT);

        root.getChildren().addAll(hideLayer, background, container, loadingLayer);

        FXUtils.bindFixed(background, widthProperty(), heightProperty());
        FXUtils.bindFixed(container, widthProperty(), heightProperty());
        FXUtils.bindFixed(loadingLayer, widthProperty(), heightProperty());
        FXUtils.setFixedSize(hideLayer, 300, 300);

        hideCanvas();
    }

    @FxThread
    public void hideCanvas() {
        var children = hideLayer.getChildren();
        if (children.contains(canvas)) {
            return;
        }

        children.add(canvas);
    }

    public void notifyFinishBuild() {
        var components = getComponents();
        fillComponents(components, getContainer());
        components.forEach(ScreenComponent::notifyFinishBuild);

    }

    private void fillComponents(final Array<ScreenComponent> container,
                                final Node node) {
        if (node instanceof ScreenComponent) {
            container.add((ScreenComponent) node);
        }

        if (node instanceof SplitPane) {
            final ObservableList<Node> items = ((SplitPane) node).getItems();
            items.forEach(child -> fillComponents(container, child));
        } else if (node instanceof TabPane) {
            ObservableList<Tab> tabs = ((TabPane) node).getTabs();
            tabs.forEach(tab -> fillComponents(container, tab.getContent()));
        }

        if (!(node instanceof Parent)) {
            return;
        }

        final ObservableList<Node> nodes = ((Parent) node).getChildrenUnmodifiable();
        nodes.forEach(child -> fillComponents(container, child));
    }

    /**
     * @return list of components
     */
    public Array<ScreenComponent> getComponents() {
        return components;
    }

    /**
     * @return the container of this scene
     */
    public StackPane getContainer() {
        return container;
    }

    /**
     * @return the view to draw Engine Scene
     */
    public ImageView getCanvas() {
        return canvas;
    }

    public synchronized void incrementLoading() {
        if (loadingCount.incrementAndGet() == 1) {
            showLoading();
        }
    }

    @FxThread
    public synchronized void decrementLoading() {
        if (loadingCount.decrementAndGet() == 0) {
            hideLoading();
        }
    }

    private void hideLoading() {
        var loadingLayer = getLoadingLayer();
        loadingLayer.setVisible(false);
        loadingLayer.setManaged(false);
        loadingLayer.getChildren().clear();

        progreessIndicator = null;

        var container = getContainer();
        container.setDisable(false);

        if (focused != null) {
            EXECUTOR_MANAGER.addFXTask(() -> {
                focused.requestFocus();
                focused = null;
            });
        }
    }

    private void showLoading() {
        focused = getFocusOwner();

        var loadingLayer = getLoadingLayer();
        loadingLayer.setVisible(true);
        loadingLayer.setManaged(true);
        loadingLayer.toFront();

        progreessIndicator = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
        progreessIndicator.setId(CssIds.EDITOR_LOADING_PROGRESS);

        loadingLayer.getChildren().add(progreessIndicator);

        var container = getContainer();
        container.setDisable(true);
    }

    @FxThread
    public VBox getLoadingLayer() {
        return loadingLayer;
    }
}
