package mini.editor.ui.control.property.builder.impl;

import mini.editor.Messages;
import mini.editor.annotation.FxThread;
import mini.editor.extension.property.EditableProperty;
import mini.editor.extension.property.EditablePropertyType;
import mini.editor.extension.property.SeparatorProperty;
import mini.editor.extension.property.SimpleProperty;
import mini.editor.model.undo.editor.ModelChangeConsumer;
import mini.scene.Spatial;

import java.util.ArrayList;
import java.util.List;

public class SpatialPropertyBuilder extends EditableModelObjectPropertyBuilder {
    private static final SpatialPropertyBuilder INSTANCE = new SpatialPropertyBuilder();

    protected SpatialPropertyBuilder() {
        super(ModelChangeConsumer.class);
    }

    public static SpatialPropertyBuilder getInstance() {
        return INSTANCE;
    }

    @Override
    protected List<EditableProperty<?, ?>> getProperties(
            Object object,
            Object parent,
            ModelChangeConsumer changeConsumer) {
        if (!(object instanceof Spatial)) {
            return null;
        }

        var properties = new ArrayList<EditableProperty<?, ?>>();
        var spatial = (Spatial) object;

        properties.add(new SimpleProperty<>(
                EditablePropertyType.ENUM,
                Messages.MODEL_PROPERTY_CULL_HINT,
                spatial,
                Spatial::getCullHint,
                Spatial::setCullHint));
        properties.add(new SimpleProperty<>(
                EditablePropertyType.ENUM,
                Messages.MODEL_PROPERTY_SHADOW_MODE,
                spatial,
                Spatial::getShadowMode,
                Spatial::setShadowMode));
        properties.add(new SimpleProperty<>(
                EditablePropertyType.ENUM,
                Messages.MODEL_PROPERTY_QUEUE_BUCKET,
                spatial,
                Spatial::getLocalQueueBucket,
                Spatial::setQueueBucket));
        // TODO: Scene layer

        if (canEditTransformation(spatial)) {
            properties.add(SeparatorProperty.getInstance());

            properties.add(new SimpleProperty<>(
                    EditablePropertyType.VECTOR_3F,
                    Messages.MODEL_PROPERTY_LOCATION,
                    spatial,
                    Spatial::getLocalTranslation,
                    Spatial::setLocalTranslation));
            properties.add(new SimpleProperty<>(
                    EditablePropertyType.VECTOR_3F,
                    Messages.MODEL_PROPERTY_SCALE,
                    spatial,
                    Spatial::getLocalScale,
                    Spatial::setLocalScale));
            properties.add(new SimpleProperty<>(
                    EditablePropertyType.QUATERNION,
                    Messages.MODEL_PROPERTY_ROTATION,
                    spatial,
                    Spatial::getLocalRotation,
                    Spatial::setLocalRotation));
        }

        var userDataKeys = spatial.getUserDataKeys();
        if (userDataKeys.isEmpty()) {
            return properties;
        }

        throw new UnsupportedOperationException();
    }

    @FxThread
    private boolean canEditTransformation(Spatial spatial) {
        return true; // TODO: SceneNode and SceneLayer cannot be edited.
    }
}
