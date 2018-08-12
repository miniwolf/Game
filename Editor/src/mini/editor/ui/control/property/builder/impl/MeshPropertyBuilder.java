package mini.editor.ui.control.property.builder.impl;

import javafx.scene.layout.VBox;
import mini.editor.annotation.FxThread;
import mini.editor.model.undo.editor.ModelChangeConsumer;
import mini.scene.Mesh;

public class MeshPropertyBuilder extends AbstractPropertyBuilder<ModelChangeConsumer> {
    private static final MeshPropertyBuilder INSTANCE = new MeshPropertyBuilder();

    private MeshPropertyBuilder() {
        super(ModelChangeConsumer.class);
    }

    public static MeshPropertyBuilder getInstance() {
        return INSTANCE;
    }

    @Override
    @FxThread
    protected void buildForImpl(
            final Object object,
            final Object parent,
            final VBox container,
            final ModelChangeConsumer changeConsumer) {
        if (!(object instanceof Mesh)) {
            return;
        }

        throw new UnsupportedOperationException();
    }
}
