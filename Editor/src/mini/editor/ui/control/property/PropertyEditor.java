package mini.editor.ui.control.property;

import com.ss.rlib.common.util.ObjectUtils;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import mini.editor.annotation.FxThread;
import mini.editor.model.undo.editor.ChangeConsumer;
import mini.editor.ui.FxConstants;
import mini.editor.ui.control.property.builder.PropertyBuilderRegistry;
import mini.editor.ui.css.CssClasses;

public class PropertyEditor<T extends ChangeConsumer> extends ScrollPane {
    private static final PropertyBuilderRegistry
            BUILDER_REGISTRY = PropertyBuilderRegistry.getInstance();

    private Object currentObject;
    private Object currentParent;
    private T changeConsumer;
    private VBox container;

    public PropertyEditor(T changeConsumer) {
        this.changeConsumer = changeConsumer;
        createComponents();
    }

    @FxThread
    private void createComponents() {
        container = new VBox();
        container.prefWidthProperty()
                 .bind(widthProperty().subtract(FxConstants.PROPERTY_LIST_OFFSET));
        var wrapper = new VBox(container);

        getStyleClass().add(CssClasses.PROPERTY_EDITOR);
        wrapper.getStyleClass().addAll(CssClasses.DEF_VBOX, CssClasses.PROPERTY_EDITOR_CONTAINER);
        container.getStyleClass().addAll(CssClasses.DEF_VBOX, CssClasses.PROPERTY_EDITOR_CONTAINER);

        setContent(wrapper);
    }

    public void buildFor(
            Object object,
            Object parent) {
        if (getCurrentObject() == object) {
            return;
        }

        var container = getContainer();
        var children = container.getChildren();
        children.clear();

        if (object != null) {
            BUILDER_REGISTRY.buildFor(object, parent, container, changeConsumer);
        }

        container.setDisable(object == null || !canEdit(object, parent));

        setCurrentObject(object);
        setCurrentParent(parent);
    }

    /**
     * @param object object to edit properties for
     * @return whether we can edit the properties of the object
     */
    @FxThread
    private boolean canEdit(Object object, Object parent) {
        return true;
    }

    private void setCurrentParent(Object currentParent) {
        this.currentParent = currentParent;
    }

    private Object getCurrentObject() {
        return currentObject;
    }

    private void setCurrentObject(Object currentObject) {
        this.currentObject = currentObject;
    }

    public VBox getContainer() {
        return ObjectUtils.notNull(container);
    }
}
