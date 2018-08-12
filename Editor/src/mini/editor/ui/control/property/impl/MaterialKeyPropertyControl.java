package mini.editor.ui.control.property.impl;

import javafx.event.ActionEvent;
import javafx.scene.input.DragEvent;
import mini.asset.AssetKey;
import mini.asset.MaterialKey;
import mini.editor.annotation.FxThread;
import mini.editor.model.undo.editor.ChangeConsumer;
import mini.editor.model.undo.editor.ModelChangeConsumer;
import mini.editor.ui.control.property.PropertyControl;
import mini.editor.util.EditorUtil;

import java.nio.file.Path;

public class MaterialKeyPropertyControl<C extends ChangeConsumer, T> extends MaterialPropertyControl<C, T, MaterialKey> {
    private Object propertyValue;

    public MaterialKeyPropertyControl(
            MaterialKey key,
            String paramName,
            C changeConsumer) {
        super(key, paramName, changeConsumer);
    }

    @Override
    @FxThread
    protected void openToEdit(ActionEvent actionEvent) {
        EditorUtil.openInEditor(getPropertyValue());
    }

    @Override
    protected void changeMaterial(ActionEvent actionEvent) {
    }

    @Override
    protected void addMaterial(Path toPath) {
    }

    @Override
    protected void handleDragExitedEvent(DragEvent dragEvent) {
    }

    @Override
    protected void reload() {
    }

    @Override
    protected void apply() {
    }
}
