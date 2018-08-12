package mini.editor.ui.control.property.impl;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import mini.editor.Messages;
import mini.editor.annotation.FxThread;
import mini.editor.model.undo.editor.ChangeConsumer;
import mini.editor.ui.control.property.PropertyControl;
import mini.editor.ui.css.CssClasses;
import mini.editor.util.ObjectsUtil;

public abstract class ElementPropertyControl<C extends ChangeConsumer, D, T> extends PropertyControl<C, D, T> {
    protected static final String NO_ELEMENT = Messages.ABSTRACT_ELEMENT_PROPERTY_CONTROL_NO_ELEMENT;

    protected final Class<T> type;

    /**
     * Contains the name of the element.
     */
    private Label elementLabel;

    public ElementPropertyControl(
            Class<T> type,
            T propertyValue,
            String propertyName,
            C changeConsumer) {
        super(propertyValue, propertyName, changeConsumer);
        this.type = type;
    }

    @Override
    @FxThread
    protected void createComponents(HBox container) {
        elementLabel = new Label(NO_ELEMENT);
        elementLabel.prefWidthProperty().bind(container.widthProperty());

        var changeButton = new Button();
        changeButton.setOnAction(event -> addElement());

        var editButton = new Button();
        editButton.setOnAction(event -> removeElement());
        editButton.disableProperty().bind(elementLabel.textProperty().isEqualTo(NO_ELEMENT));

        container.getStyleClass().addAll(
                CssClasses.TEXT_INPUT_CONTAINER,
                CssClasses.ABSTRACT_PARAM_CONTROL_INPUT_CONTAINER);
        elementLabel.getStyleClass().add(CssClasses.ABSTRACT_PARAM_CONTROL_ELEMENT_LABEL);
        changeButton.getStyleClass().addAll(
                CssClasses.FLAT_BUTTON,
                CssClasses.INPUT_CONTROL_TOOLBAR_BUTTON);
        editButton.getStyleClass().addAll(
                CssClasses.FLAT_BUTTON,
                CssClasses.INPUT_CONTROL_TOOLBAR_BUTTON);

        container.getChildren().addAll(
                elementLabel,
                changeButton,
                editButton
        );
    }

    @FxThread
    private void removeElement() {
        changed(null, getPropertyValue());
    }

    @FxThread
    protected abstract void addElement();

    @FxThread
    protected Label getElementLabel() {
        return ObjectsUtil.notNull(elementLabel);
    }
}
