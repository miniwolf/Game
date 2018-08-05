package mini.editor.ui.control.property.builder.impl;

import javafx.scene.layout.VBox;
import mini.bounding.BoundingBox;
import mini.bounding.BoundingSphere;
import mini.bounding.BoundingVolume;
import mini.editor.Messages;
import mini.editor.model.undo.editor.ModelChangeConsumer;
import mini.editor.ui.control.property.impl.DefaultPropertyControl;
import mini.editor.util.EditorUtil;
import mini.scene.Geometry;

import java.util.function.Function;

public class GeometryPropertyBuilder extends AbstractPropertyBuilder<ModelChangeConsumer> {

    private static final Function<BoundingVolume, String> BOUNDING_VOLUME_TO_STRING
            = boundingVolume -> {
        if (boundingVolume instanceof BoundingSphere) {
            final BoundingSphere boundingSphere = (BoundingSphere) boundingVolume;
            return Messages.BOUNDING_VOLUME_MODEL_PROPERTY_CONTROL_SPHERE + ": [" +
                   Messages.BOUNDING_VOLUME_MODEL_PROPERTY_CONTROL_SPHERE_RADIUS + "=" +
                   boundingSphere.getRadius() + "]";
        } else if (boundingVolume instanceof BoundingBox) {
            final BoundingBox boundingBox = (BoundingBox) boundingVolume;

            final float xExtent = EditorUtil.clipNumber(boundingBox.getXExtent(), 100);
            final float yExtent = EditorUtil.clipNumber(boundingBox.getYExtent(), 100);
            final float zExtent = EditorUtil.clipNumber(boundingBox.getZExtent(), 100);

            return Messages.BOUNDING_VOLUME_MODEL_PROPERTY_CONTROL_BOX +
                   ": [x=" + xExtent + ", y=" + yExtent + ", z=" + zExtent + "]";
        }
        return "";
    };

    protected GeometryPropertyBuilder() {
        super(ModelChangeConsumer.class);
    }

    @Override
    protected void buildForImpl(Object object, Object parent, VBox container,
                                ModelChangeConsumer changeConsumer) {
        if (!(object instanceof Geometry)) {
            return;
        }

        final Geometry geometry = (Geometry) object;
        final BoundingVolume modelBound = geometry.getModelBound();

        final DefaultPropertyControl<ModelChangeConsumer, BoundingVolume> boundingVolumeControl =
                new DefaultPropertyControl<>(
                        modelBound,
                        Messages.BOUNDING_VOLUME_MODEL_PROPERTY_CONTROL_NAME,
                        changeConsumer);

        boundingVolumeControl.setToStringFunction(BOUNDING_VOLUME_TO_STRING);
        boundingVolumeControl.reload();
        boundingVolumeControl.setEditObject(geometry);

        if (canEditMaterial(geometry)) {

        }

    }
}
