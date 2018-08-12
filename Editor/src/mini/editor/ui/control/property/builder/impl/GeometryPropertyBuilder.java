package mini.editor.ui.control.property.builder.impl;

import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import javafx.scene.layout.VBox;
import mini.asset.AssetKey;
import mini.asset.MaterialKey;
import mini.bounding.BoundingBox;
import mini.bounding.BoundingSphere;
import mini.bounding.BoundingVolume;
import mini.editor.Messages;
import mini.editor.model.undo.editor.ModelChangeConsumer;
import mini.editor.ui.control.property.builder.PropertyBuilder;
import mini.editor.ui.control.property.impl.DefaultPropertyControl;
import mini.editor.ui.control.property.impl.MaterialKeyPropertyControl;
import mini.editor.ui.control.property.impl.MaterialPropertyControl;
import mini.editor.util.EditorUtil;
import mini.material.Material;
import mini.material.MaterialDef;
import mini.scene.Geometry;

import java.util.function.Function;
import java.util.function.Predicate;

public class GeometryPropertyBuilder extends AbstractPropertyBuilder<ModelChangeConsumer> {

    private static final Array<Predicate<Geometry>> CAN_EDIT_MATERIAL_CHECKERS = ArrayFactory.newArray(Predicate.class);

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

    private static final PropertyBuilder INSTANCE = new GeometryPropertyBuilder();

    protected GeometryPropertyBuilder() {
        super(ModelChangeConsumer.class);
    }

    public static PropertyBuilder getInstance() {
        return INSTANCE;
    }

    @Override
    protected void buildForImpl(Object object, Object parent, VBox container,
                                ModelChangeConsumer changeConsumer) {
        if (!(object instanceof Geometry)) {
            return;
        }

        final Geometry geometry = (Geometry) object;
        final BoundingVolume modelBound = geometry.getModelBound();

        final DefaultPropertyControl<ModelChangeConsumer, Geometry, BoundingVolume> boundingVolumeControl =
                new DefaultPropertyControl<>(
                        modelBound,
                        Messages.BOUNDING_VOLUME_MODEL_PROPERTY_CONTROL_NAME,
                        changeConsumer);

        boundingVolumeControl.setToStringFunction(BOUNDING_VOLUME_TO_STRING);
        boundingVolumeControl.reload();
        boundingVolumeControl.setEditObject(geometry);

        if (canEditMaterial(geometry)) {
            final Material material = geometry.getMaterial();
            final MaterialKey materialKey = (MaterialKey) material.getKey();

            final MaterialKeyPropertyControl<ModelChangeConsumer, Geometry> materialControl =
                    new MaterialKeyPropertyControl<>(materialKey, Messages.MODEL_PROPERTY_MATERIAL, changeConsumer);
            // TODO: Implement these handlers
//            materialControl.setApplyHandler(MATERIAL_APPLY_HANDLER);
//            materialControl.setSyncHandler(MATERIAL_SYNC_HANDLER);
            materialControl.setEditObject(geometry);

            container.getChildren().add(materialControl);
        }

        container.getChildren().add(boundingVolumeControl);
    }

    private boolean canEditMaterial(Geometry geometry) {
        return CAN_EDIT_MATERIAL_CHECKERS.search(geometry, Predicate::test) == null;
    }
}
