package mini.editor.ui.control.property.impl;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import mini.editor.FileExtensions;
import mini.editor.Messages;
import mini.editor.annotation.FxThread;
import mini.editor.model.undo.editor.ChangeConsumer;
import mini.editor.ui.control.property.PropertyControl;
import mini.editor.ui.css.CssClasses;
import mini.editor.util.EditorUtil;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * Controller for the properties of a material.
 *
 * @param <T> the type of the spatial
 * @param <V> the type of the material property
 */
public abstract class MaterialPropertyControl<C extends ChangeConsumer, T, V> extends PropertyControl<C, T, V> {
    protected static final String NO_MATERIAL = Messages.MATERIAL_MODEL_PROPERTY_CONTROL_NO_MATERIAL;

    private Label materialLabel;

    public MaterialPropertyControl(
            V element,
            String propertyName,
            C changeConsumer) {
        super(element, propertyName, changeConsumer);
        setOnDragOver(this::handleDragOverEvent);
        setOnDragDropped(this::handleDragDroppedEvent);
        setOnDragExited(this::handleDragExitedEvent);
    }

    private void handleDragDroppedEvent(DragEvent dragEvent) {
        List<File> files = EditorUtil.getFiles(dragEvent.getDragboard());
        if (files.size() != 1) {
            return;
        }

        var file = files.get(0);
        if (!file.getName().endsWith(FileExtensions.MINI_MATERIAL)) {
            return;
        }

        addMaterial(file.toPath());
    }

    private void handleDragOverEvent(DragEvent dragEvent) {
        var dragboard = dragEvent.getDragboard();
        List<File> files = EditorUtil.getFiles(dragboard);
        if (files.size() != 1) {
            return;
        }

        var file = files.get(0);
        if (!file.getName().endsWith(FileExtensions.MINI_MATERIAL)) {
            return;
        }

        var transferModes = dragboard.getTransferModes();
        var isCopy = transferModes.contains(TransferMode.COPY);

        dragEvent.acceptTransferModes(isCopy ? TransferMode.COPY : TransferMode.MOVE);
        dragEvent.consume();
    }

    @Override
    @FxThread
    protected void createComponents(HBox container) {
        materialLabel = new Label(NO_MATERIAL);

        var changeButton = new Button();
        changeButton.setOnAction(this::changeMaterial);

        var editButton = new Button();
        editButton.disableProperty().bind(materialLabel.textProperty().isEqualTo(NO_MATERIAL));
        editButton.setOnAction(this::openToEdit);

        materialLabel.prefWidthProperty().bind(
                widthProperty()
                        .subtract(changeButton.widthProperty())
                        .subtract(editButton.widthProperty()));

        container.getStyleClass().addAll(
                CssClasses.TEXT_INPUT_CONTAINER,
                CssClasses.ABSTRACT_PARAM_CONTROL_INPUT_CONTAINER);
        materialLabel.getStyleClass().add(CssClasses.ABSTRACT_PARAM_CONTROL_ELEMENT_LABEL);
        changeButton.getStyleClass().addAll(CssClasses.FLAT_BUTTON, CssClasses.INPUT_CONTROL_TOOLBAR_BUTTON);
        editButton.getStyleClass().addAll(CssClasses.FLAT_BUTTON, CssClasses.INPUT_CONTROL_TOOLBAR_BUTTON);

        container.getChildren().addAll(materialLabel, changeButton, editButton);
    }

    /**
     * Choose another material to load.
     */
    protected abstract void openToEdit(ActionEvent actionEvent);

    /**
     * Opens dialog to change the properties of a material.
     */
    protected abstract void changeMaterial(ActionEvent actionEvent);

    /**
     * Add the new material to the property list.
     */
    protected abstract void addMaterial(Path toPath);

    protected abstract void handleDragExitedEvent(DragEvent dragEvent);
}
