package mini.editor.ui.component.tab;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.css.PseudoClass;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

public class TabToolComponent extends TabPane {
    private static final PseudoClass COLLAPSED_PSEUDO_CALL = PseudoClass
            .getPseudoClass("tool-collapsed");

    protected final BooleanProperty collapsed = new BooleanPropertyBase() {

        @Override
        protected void invalidated() {
            pseudoClassStateChanged(COLLAPSED_PSEUDO_CALL, get());
        }

        @Override
        public Object getBean() {
            return TabToolComponent.this;
        }

        @Override
        public String getName() {
            return "collapsed";
        }
    };

    private SplitPane pane;
    private boolean changingTab;
    private double expandPosition;

    public TabToolComponent(final SplitPane pane) {
        this.pane = pane;
        bindCollapsedProperty();
        addEventHandler(MouseEvent.MOUSE_CLICKED, this::processMouseClicked);
        getSelectionModel().selectedItemProperty()
                           .addListener(((observable, oldValue, newValue) -> changedTab(oldValue)));
    }

    private void changedTab(final Tab oldValue) {
        if (isCollapsed()) {
            expand();
            setChangingTab(oldValue != null);
        }
    }

    /**
     * Add a new component to this tool container
     *
     * @param component the component
     * @param name      the name
     */
    public void addComponent(final Region component,
                             final String name) {
        final Tab tab = new Tab(name);
        tab.setContent(component);
        tab.setClosable(false);

        getTabs().add(tab);

        component.minHeightProperty().bind(heightProperty());
        component.maxHeightProperty().bind(heightProperty());
    }

    private void processMouseClicked(final MouseEvent event) {
        final EventTarget target = event.getTarget();
        if (!(target instanceof Node)) {
            return;
        }

        final Node node = (Node) target;
        if (!(node instanceof Text) || node.getStyleClass().contains("tab-container")) {
            return;
        }

        final Parent label = node.getParent();
        if (!(label instanceof Label)) {
            return;
        }

        final Parent tabContainer = label.getParent();
        if (tabContainer.getStyleClass().contains("tab-container")) {
            return;
        }

        if (isChangingTab()) {
            setChangingTab(false);
            return;
        }

        processExpandOrCollapse();
    }

    protected void processExpandOrCollapse() {
        final double minWidth = getMinWidth();
        final double width = getWidth();

        if (width <= minWidth) {
            expand();
        } else {
            collapse();
        }
    }

    public void collapse() {
        setCollapseSize();
        pane.setDividerPosition(getDividerIndex(), getCollapsePosition());
    }

    protected void setCollapseSize() {
        maxWidthProperty().bind(minWidthProperty());
    }

    protected void expand() {
        final int dividerIndex = getDividerIndex();
        final double expandPosition = getExpandPosition();
        expand(dividerIndex, expandPosition);
    }

    private void expand(final int dividerIndex, final double position) {
        setExpandSize();
        pane.setDividerPosition(dividerIndex, position);
    }

    protected void setExpandSize() {
        maxWidthProperty().unbind();
        setMaxWidth(USE_COMPUTED_SIZE);
    }

    /**
     * Bind collapsed property to minimum width property.
     */
    protected void bindCollapsedProperty() {
        this.collapsed.bind(widthProperty().lessThanOrEqualTo(minWidthProperty()));
    }

    public boolean isChangingTab() {
        return changingTab;
    }

    public void setChangingTab(boolean changingTab) {
        this.changingTab = changingTab;
    }

    protected int getDividerIndex() {
        return 0;
    }

    protected int getCollapsePosition() {
        return 0;
    }

    public boolean isCollapsed() {
        return collapsed.get();
    }

    /**
     * @return the last expand position
     */
    public double getExpandPosition() {
        return expandPosition;
    }

    public void setExpandPosition(final double expandPosition) {
        this.expandPosition = expandPosition;
    }
}
