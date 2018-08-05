package mini.editor.ui.component.editor.impl.scene;

import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import com.ss.rlib.common.util.array.ArrayIterator;
import com.ss.rlib.common.util.dictionary.DictionaryFactory;
import com.ss.rlib.common.util.dictionary.ObjectDictionary;
import mini.asset.AssetManager;
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
import mini.light.Light;
import mini.material.Material;
import mini.math.Transform;
import mini.math.Vector3f;
import mini.renderer.Camera;
import mini.renderer.RenderManager;
import mini.scene.Geometry;
import mini.scene.Mesh;
import mini.scene.Node;
import mini.scene.Spatial;
import mini.scene.debug.WireBox;

public abstract class AbstractSceneEditor3DPart<T extends AbstractSceneFileEditor & ModelChangeConsumer, M extends Spatial>
        extends Advanced3DEditorPart<T> implements EditorTransformSupport {
    public static final String KEY_SHAPE_CENTER = "mini.sceneEditor.shapeCenter";
    public static final String KEY_SHAPE_INIT_SCALE = "mini.sceneEditor.shapeInitScale";
    public static final String KEY_MODEL_NODE = "mini.sceneEditor.modelNode";

    private static final ObjectDictionary<Light.Type, Node> LIGHT_MODEL_TABLE;

    protected final Array<Spatial> selected;

    private final ObjectDictionary<Spatial, Geometry> selectionShape;

    static {
        final AssetManager assetManager = EditorUtil.getAssetManager();

        LIGHT_MODEL_TABLE = DictionaryFactory.newObjectDictionary();
        //LIGHT_MODEL_TABLE.put(Light.Type.Point, assetManager.loadModel("graphics/models/light/point_light.minio"));
    }

    private Transform transformCenter;
    private Spatial toTransform;
    private Material selectionMaterial;

    private final Node modelNode;
    private M currentModel;
    private boolean paintingMode;

    public AbstractSceneEditor3DPart(T fileEditor) {
        super(fileEditor);

        selected = ArrayFactory.newArray(Spatial.class);
        selectionShape = DictionaryFactory.newObjectDictionary();

        modelNode = new Node("TreeNode");
        modelNode.setUserData(KEY_MODEL_NODE, true);
    }

    @Override
    protected void preCameraUpdate() {
        // TODO: Move, Rotate, and Scale Tool.
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

    protected Node getModelNode() {
        return modelNode;
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

    @FromAnyThread
    public void openModel(final M model) {
        EXECUTOR_MANAGER.addEditorTask(() -> openModelImpl(model));
    }

    @FromAnyThread
    public void addLight(final Light light) {
        EXECUTOR_MANAGER.addEditorTask(() -> addLightImpl(light));
    }

    @MiniThread
    private void addLightImpl(final Light light) {
        throw new UnsupportedOperationException();
    }

    @FromAnyThread
    public void removeLight(final Light light) {
        EXECUTOR_MANAGER.addEditorTask(() -> removeLightImpl(light));
    }

    @MiniThread
    private void removeLightImpl(final Light light) {
        throw new UnsupportedOperationException();
    }

    @MiniThread
    private void openModelImpl(final M model) {
        final Node modelNode = getModelNode();
        final M currentModel = getCurrentModel();

        if (currentModel != null) {
            throw new UnsupportedOperationException(); // Detach the previous model
        }

        NodeUtils.visitGeometry(model, geometry -> {
            final RenderManager renderManager = EditorUtil.getRenderManager();
            renderManager.preloadScene(geometry);
        });

        attachModel(model, modelNode);

        setCurrentModel(model);
    }

    protected void attachModel(final M model, final Node modelNode) {
        modelNode.attachChild(model);
    }

    public M getCurrentModel() {
        return currentModel;
    }

    public void setCurrentModel(M currentModel) {
        this.currentModel = currentModel;
    }

    @FromAnyThread
    public void changePaintingMode(final boolean paintingMode) {
        EXECUTOR_MANAGER.addEditorTask(() -> changePaintingModeImpl(paintingMode));
    }

    @MiniThread
    private void changePaintingModeImpl(final boolean paintingMode) {
        setPaintingMode(paintingMode);

        // TODO: Attach cursor, markers and transform tool nodes to the tool node
    }

    public boolean isPaintingMode() {
        return paintingMode;
    }

    public void setPaintingMode(boolean paintingMode) {
        this.paintingMode = paintingMode;
    }
}
