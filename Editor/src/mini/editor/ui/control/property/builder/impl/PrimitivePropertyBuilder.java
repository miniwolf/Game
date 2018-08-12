package mini.editor.ui.control.property.builder.impl;

import javafx.scene.layout.VBox;
import mini.editor.model.undo.editor.ModelChangeConsumer;
import mini.math.Vector3f;
import mini.scene.VertexBuffer;

import java.nio.Buffer;

public class PrimitivePropertyBuilder extends AbstractPropertyBuilder<ModelChangeConsumer> {
    private static final PrimitivePropertyBuilder INSTANCE = new PrimitivePropertyBuilder();

    protected PrimitivePropertyBuilder() {
        super(ModelChangeConsumer.class);
    }

    public static PrimitivePropertyBuilder getInstance() {
        return INSTANCE;
    }

    @Override
    protected void buildForImpl(Object object, Object parent, VBox container, ModelChangeConsumer changeConsumer) {
        if (object instanceof Vector3f) {
            throw new UnsupportedOperationException();
        } else if (object instanceof VertexBuffer){
            throw new UnsupportedOperationException();
        } else if (object instanceof Buffer) {
            throw new UnsupportedOperationException();
        }
    }
}
