package mini.editor.ui.component.tab;

import javafx.geometry.Side;
import javafx.scene.control.SplitPane;
import mini.editor.ui.css.CssIds;

/**
 * The component to contain global bottom tool components
 */
public class GlobalBottomToolComponent extends TabToolComponent {
    public GlobalBottomToolComponent(final SplitPane pane) {
        super(pane);
        setId(CssIds.GLOBAL_BOTTOM_TOOL_COMPONENT);
        setSide(Side.BOTTOM);
    }

    @Override
    protected void bindCollapsedProperty() {
        this.collapsed.bind(heightProperty().lessThanOrEqualTo(minHeightProperty()));
    }

    @Override
    protected int getCollapsePosition() {
        return 1;
    }

    @Override
    protected void processExpandOrCollapse() {
        final double minHeight = getMinHeight();
        final double height = getHeight();

        if (height <= minHeight) {
            expand();
        } else {
            collapse();
        }
    }

    @Override
    protected void setExpandSize() {
        maxHeightProperty().unbind();
        setMaxHeight(USE_COMPUTED_SIZE);
    }

    @Override
    protected void setCollapseSize() {
        maxHeightProperty().bind(minHeightProperty());
    }
}
