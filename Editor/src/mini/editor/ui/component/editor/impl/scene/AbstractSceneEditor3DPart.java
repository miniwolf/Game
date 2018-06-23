package mini.editor.ui.component.editor.impl.scene;

import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import com.ss.rlib.common.util.array.ArrayIterator;
import com.ss.rlib.common.util.dictionary.DictionaryFactory;
import com.ss.rlib.common.util.dictionary.ObjectDictionary;
import mini.bounding.BoundingBox;
import mini.bounding.BoundingSphere;
import mini.bounding.BoundingVolume;
import mini.editor.annotation.FromAnyThread;
import mini.editor.annotation.MiniThread;
import mini.editor.control.transform.EditorTransformSupport;
import mini.editor.model.EditorCamera;
import mini.editor.model.scene.VisibleOnlyWhenSelected;
import mini.editor.model.undo.editor.ModelChangeConsumer;
import mini.editor.plugin.api.editor.part3d.Advanced3DEditorPart;
import mini.editor.util.EditorUtil;
import mini.editor.util.LocalObjects;
import mini.editor.util.NodeUtils;
import mini.material.Material;
import mini.math.Transform;
import mini.math.Vector3f;
import mini.renderer.Camera;
import mini.scene.Geometry;
import mini.scene.Mesh;
import mini.scene.Spatial;
import mini.scene.debug.WireBox;

public class AbstractSceneEditor3DPart<T extends AbstractSceneFileEditor & ModelChangeConsumer, M extends Spatial>
        extends Advanced3DEditorPart<T> implements EditorTransformSupport {
    public static final String KEY_SHAPE_CENTER = "mini.sceneEditor.shapeCenter";
    public static final String KEY_SHAPE_INIT_SCALE = "mini.sceneEditor.shapeInitScale";

    protected final Array<Spatial> selected;

    private final ObjectDictionary<Spatial, Geometry> selectionShape;
    private Transform transformCenter;
    private Spatial toTransform;
    private Material selectionMaterial;

    public AbstractSceneEditor3DPart() {
        selected = ArrayFactory.newArray(Spatial.class);
        selectionShape = DictionaryFactory.newObjectDictionary();
    }

    public void select(final Array<Spatial> objects) {
        EXECUTOR_MANAGER.addEditorTask(() -> selectImpl(objects));
    }

    public void select(final Spatial spatial) {
        EXECUTOR_MANAGER.addEditorTask(() -> {
            final Array<Spatial> toSelect = LocalObjects.get().nextSpatialArray();
            toSelect.add(spatial);

            selectImpl(toSelect);
        });
    }

    @MiniThread
    private void selectImpl(final Array<Spatial> objects) {
        final Array<Spatial> selected = getSelected();

        if (objects.isEmpty()) {
            selected.forEach(this,
                             (spatial, editorPart) -> editorPart.removeFromSelection(spatial));
            selected.clear();
            updateToTransform();
            return;
        }

        for (final ArrayIterator<Spatial> iterator = selected.iterator(); iterator.hasNext(); ) {
            final Spatial spatial = iterator.next();
            if (objects.contains(spatial)) {
                continue;
            }

            removeFromSelection(spatial);
            iterator.fastRemove();
        }

        for (Spatial spatial: objects) {
            if (!selected.contains(spatial)) {
                addToSelection(spatial);
            }
        }
        updateToTransform();
    }

    @MiniThread
    private void addToSelection(Spatial spatial) {
        if (spatial instanceof VisibleOnlyWhenSelected) {
            spatial.setCullHint(Spatial.CullHint.Dynamic);
        }

        final Array<Spatial> selected = getSelected();
        selected.add(spatial);

        // TODO: No Selection elements if instanceof return

        Geometry shape;
        // TODO: ParticleEmitter
        if (spatial instanceof Geometry) {
            shape = buildGeometrySelection((Geometry) spatial);
        } else {
            shape = buildBoxSelection(spatial);
        }

        if (shape == null) {
            return;
        }

        // TODO: IfShowSelection ToolNode

        getSelectionShape().put(spatial, shape);
    }

    /**
     * Build the selection box for the spatial
     */
    private Geometry buildBoxSelection(final Spatial spatial) {
        NodeUtils.updateWorldBounds(spatial);

        final BoundingVolume bound = spatial.getWorldBound();

        if (bound instanceof BoundingBox) {
            final BoundingBox boundingBox = (BoundingBox) bound;
            final Vector3f center = boundingBox.getCenter().subtract(spatial.getWorldTranslation());
            final Vector3f initScale = spatial.getLocalScale().clone();

            final Geometry geometry = WireBox.makeGeometry(boundingBox);
            geometry.setName("SelectionShape");
            geometry.setMaterial(getSelectionMaterial());
            geometry.setUserData(KEY_SHAPE_CENTER, center);
            geometry.setUserData(KEY_SHAPE_INIT_SCALE, initScale);

            final Vector3f position = geometry.getLocalTranslation();
            position.addLocal(center);

            geometry.setLocalTranslation(position);
            return geometry;
        } else if (bound instanceof BoundingSphere) {
            throw new UnsupportedOperationException();
        }

        final Geometry geometry = WireBox.makeGeometry(new BoundingBox(Vector3f.ZERO, 1, 1, 1));
        geometry.setName("SelectionShape");
        geometry.setMaterial(getSelectionMaterial());
        geometry.setLocalTranslation(spatial.getWorldTranslation());
        return geometry;
    }

    /**
     * Build selection grid for the geometry
     */
    @MiniThread
    private Geometry buildGeometrySelection(final Geometry geom) {
        final Mesh mesh = geom.getMesh();
        if (mesh == null) {
            return null;
        }

        final Geometry geometry = new Geometry("SelectionShape", mesh);
        geometry.setMaterial(getSelectionMaterial());
        geometry.setLocalTransform(geom.getWorldTransform());

        return geometry;
    }

    private void updateToTransform() {
        setToTransform(getSelected().first());
    }

    private void removeFromSelection(final Spatial spatial) {
        setTransformCenter(null);
        setToTransform(null);

        final ObjectDictionary<Spatial, Geometry> selectionShape = getSelectionShape();

        final Spatial shape = selectionShape.remove(spatial);
        if (shape != null) {
            shape.removeFromParent();
        }

        if (spatial instanceof VisibleOnlyWhenSelected) {
            spatial.setCullHint(Spatial.CullHint.Always);
        }
    }

    /**
     * @return the array of the selected models.
     */
    @FromAnyThread
    public Array<Spatial> getSelected() {
        return selected;
    }

    /**
     * @return the selection models of selected models.
     */
    private ObjectDictionary<Spatial, Geometry> getSelectionShape() {
        return selectionShape;
    }

    private void setTransformCenter(final Transform transformCenter) {
        this.transformCenter = transformCenter;
    }

    private void setToTransform(final Spatial toTransform) {
        this.toTransform = toTransform;
    }

    /**
     * @return the material of selection
     */
    public Material getSelectionMaterial() {
        return selectionMaterial;
    }

    @Override
    @MiniThread
    public Camera getCamera() {
        return EditorUtil.getGlobalCamera();
    }

    /**
     * Look at the spatial
     */
    public void cameraLookAt(final Spatial spatial) {
        EXECUTOR_MANAGER.addEditorTask(() -> {
            final EditorCamera editorCamera = getEditorCamera();

            final LocalObjects local = LocalObjects.get();

            final BoundingVolume worldBound = spatial.getWorldBound();
            float distance;

            if (worldBound != null) {
                distance = worldBound.getVolume();
                if (worldBound instanceof BoundingBox) {
                    final BoundingBox boundingBox = (BoundingBox) worldBound;
                    distance = boundingBox.getXExtent();
                    distance = Math.max(distance, boundingBox.getYExtent());
                    distance = Math.max(distance, boundingBox.getZExtent());
                    distance *= 2F;
                } else if (worldBound instanceof BoundingSphere) {
                    distance = ((BoundingSphere) worldBound).getRadius() * 2F;
                }
            } else {
                distance = getCamera().getLocation().distance(spatial.getWorldTranslation());
            }

            editorCamera.setTargetDistance(distance);

            final Vector3f position = local.nextVector().set(spatial.getWorldTranslation());

            getNodeForCamera().setLocalTranslation(position);
        });
    }
}
