package mini.editor.ui.control.property.builder.impl;

import javafx.scene.layout.VBox;
import mini.editor.model.undo.editor.ModelChangeConsumer;

public class CollisionShapePropertyBuilder extends AbstractPropertyBuilder<ModelChangeConsumer> {
    private static final CollisionShapePropertyBuilder INSTANCE = new CollisionShapePropertyBuilder();

    private CollisionShapePropertyBuilder() {
        super(ModelChangeConsumer.class);
    }

    public static CollisionShapePropertyBuilder getInstance() {
        return INSTANCE;
    }

    @Override
    protected void buildForImpl(
            final Object object,
            final Object parent,
            final VBox container,
            final ModelChangeConsumer changeConsumer) {
        // TODO: This is for Bullet when this works
    }
}
