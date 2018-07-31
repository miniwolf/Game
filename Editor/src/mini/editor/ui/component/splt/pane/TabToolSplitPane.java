package mini.editor.ui.component.splt.pane;

import com.ss.rlib.common.util.ObjectUtils;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import mini.editor.ui.component.tab.TabToolComponent;

import java.util.Objects;

public class TabToolSplitPane<T> extends SplitPane {
    protected final Scene scene;
    private final T config;
    protected TabToolComponent toolComponent;

    private int size;
    private boolean collapsed;

    public TabToolSplitPane(Scene scene, T config) {
        this.scene = scene;
        this.config = config;
        if (config != null) {
            this.size = loadSize();
            this.collapsed = loadCollapsed();
        }
    }

    /**
     * Load stored collapsed state of this pane.
     *
     * @return Whether this pane was collapsed.
     */
    protected boolean loadCollapsed() {
        return false;
    }

    private boolean isCollapsed() {
        return collapsed;
    }

    /**
     * Load stored size of this pane.
     *
     * @return the stored size of this pane.
     */
    protected int loadSize() {
        return 1;
    }

    public T getConfig() {
        return Objects.requireNonNull(config);
    }

    /**
     * init this split pane for the tool component
     *
     * @param toolComponent
     * @param another
     */
    public void initFor(TabToolComponent toolComponent, Node another) {
        this.toolComponent = toolComponent;
        addElements(toolComponent, another);
        //TODO: addListeners(toolComponent);
        bindToScene();
        update();
    }

    protected void update() {
        if (config == null) {
            return;
        }

        if (isCollapsed()) {
            getToolComponent().collapse();
        }

        handleSceneChanged(getSceneSize());
    }

    private TabToolComponent getToolComponent() {
        return ObjectUtils.notNull(toolComponent);
    }

    protected void bindToScene() {
        scene.widthProperty().addListener(((observable, oldValue, newValue) -> {
            handleSceneChanged(getSceneSize());
        }));
    }

    private void handleSceneChanged(Number newSize) {
        if (config == null) {
            return;
        }

        if (isCollapsed()) {
            setDividerPosition(getDividerIndex(), getCollapsedPosition());
        }

        setDividerPosition(getDividerIndex(), getExpandPosition(size, newSize.doubleValue()));
    }

    /**
     * @param toolSize  the tools size
     * @param sceneSize the scene size
     * @return the expand position
     */
    protected double getExpandPosition(int toolSize, double sceneSize) {
        return Math.min(1, Math.max(0.1, toolSize / sceneSize));
    }

    protected double getCollapsedPosition() {
        return 0;
    }

    protected int getDividerIndex() {
        return 0;
    }

    protected double getSceneSize() {
        return scene.getWidth();
    }

    /**
     * Add the tool component to this pane.
     *
     * @param toolComponent the tool component
     * @param other         the element
     */
    protected void addElements(TabToolComponent toolComponent, Node other) {
        getItems().addAll(toolComponent, other);
    }
}
