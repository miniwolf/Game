package mini.editor.ui.component.painting;

import com.ss.rlib.common.util.array.Array;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mini.editor.annotation.FxThread;
import mini.editor.model.editor.Editor3DProvider;
import mini.editor.model.undo.editor.ModelChangeConsumer;
import mini.editor.ui.FxConstants;
import mini.editor.ui.control.property.PropertyControl;
import mini.editor.util.FXUtils;

public class PaintingComponentContainer extends ScrollPane {
    private static final double LABEL_PERCENTAGE = 1D - PropertyControl.CONTROL_WIDTH_PERCENTAGE_2;
    private static final double FIELD_PERCENTAGE = PropertyControl.CONTROL_WIDTH_PERCENTAGE_2;

    private final ModelChangeConsumer changeConsumer;
    private final Editor3DProvider provider;

    /**
     * Container of painting components
     */
    private final VBox container;
    private final ComboBox<PaintingComponent> componentBox;
    private final Array<PaintingComponent> components;
    protected boolean showed;
    private Object paintedObject;
    private PaintingComponent currentComponent;

    public PaintingComponentContainer(ModelChangeConsumer changeConsumer,
                                      Editor3DProvider provider) {
        this.changeConsumer = changeConsumer;
        this.provider = provider;
        this.container = new VBox();
        container.prefWidthProperty()
                 .bind(widthProperty().subtract(FxConstants.PROPERTY_LIST_OFFSET));

        var toolTypeContainer = new HBox();
        toolTypeContainer.prefWidthProperty()
                         .bind(widthProperty().subtract(FxConstants.PROPERTY_LIST_OFFSET));

        var label = new Label("PaintingComponent :"); // TODO: Put in messages
        label.maxWidthProperty().bind(toolTypeContainer.widthProperty().multiply(LABEL_PERCENTAGE));

        componentBox = new ComboBox<>();
        componentBox.setCellFactory(PaintComponentListCell::new);
        componentBox.setPromptText("No tools");
        componentBox.prefWidthProperty()
                    .bind(toolTypeContainer.widthProperty().multiply(FIELD_PERCENTAGE));
        componentBox.getSelectionModel().selectedItemProperty().addListener(this::activate);

        VBox resultContainer = new VBox();

        setContent(resultContainer);

        FXUtils.addChild(toolTypeContainer, label, componentBox);
        FXUtils.addChild(resultContainer, toolTypeContainer, container);

        var registry = PaintingComponentRegistry.getInstance();
        this.components = registry.createComponents(this);
    }

    private void activate(ObservableValue<? extends PaintingComponent> observable,
                          PaintingComponent oldValue,
                          PaintingComponent newValue) {
        var items = getContainer().getChildren();
        if (oldValue != null) {
            oldValue.notifyHiding();
            oldValue.stopPainting();
            items.remove(oldValue);
        }

        var paintedObject = getPaintedObject();

        if (newValue != null) {
            if (paintedObject != null) {
                newValue.startPainting(paintedObject);
            }

            if (isShowed()) {
                newValue.notifyShowed();
            }

            items.add((Node) newValue);
        }

        setCurrentComponent(newValue);
    }

    private boolean isShowed() {
        return showed;
    }

    public void setShowed(boolean showed) {
        this.showed = showed;
    }

    /**
     * Prepare painting components to work with the element.
     */
    public void prepareFor(Object element) {
        setPaintedObject(element);

        throw new UnsupportedOperationException();
    }

    public Object getPaintedObject() {
        return paintedObject;
    }

    public void setPaintedObject(Object paintedObject) {
        this.paintedObject = paintedObject;
    }

    /**
     * @return the container
     */
    protected VBox getContainer() {
        return container;
    }

    /**
     * @return the current painting component.
     */
    @FxThread
    private PaintingComponent getCurrentComponent() {
        return currentComponent;
    }

    /**
     * Set the current painting component.
     */
    @FxThread
    private void setCurrentComponent(PaintingComponent currentComponent) {
        this.currentComponent = currentComponent;
    }
}
