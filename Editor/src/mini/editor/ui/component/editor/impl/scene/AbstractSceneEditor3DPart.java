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
import mini.editor.Messages;
import mini.editor.annotation.EditorThread;
import mini.editor.annotation.FromAnyThread;
import mini.editor.control.transform.EditorTransformSupport;
import mini.editor.extension.property.SimpleProperty;
import mini.editor.model.EditorCamera;
import mini.editor.model.scene.EditorLightNode;
import mini.editor.model.scene.EditorPresentableNode;
import mini.editor.model.scene.VisibleOnlyWhenSelected;
import mini.editor.model.undo.editor.ModelChangeConsumer;
import mini.editor.plugin.api.editor.part3d.Advanced3DEditorPart;
import mini.editor.util.*;
import mini.light.Light;
import mini.material.Material;
import mini.material.RenderState;
import mini.math.*;
import mini.renderer.Camera;
import mini.renderer.RenderManager;
import mini.renderer.queue.RenderQueue;
import mini.scene.Geometry;
import mini.scene.Mesh;
import mini.scene.Node;
import mini.scene.Spatial;
import mini.scene.debug.Grid;
import mini.scene.debug.WireBox;
import mini.scene.shape.Line;
import mini.scene.shape.Quad;

import java.util.function.Consumer;

public abstract class AbstractSceneEditor3DPart<T extends AbstractSceneFileEditor & ModelChangeConsumer, M extends Spatial>
        extends Advanced3DEditorPart<T> implements EditorTransformSupport {
    public static final String KEY_SHAPE_CENTER = "mini.sceneEditor.shapeCenter";
    public static final String KEY_SHAPE_INIT_SCALE = "mini.sceneEditor.shapeInitScale";
    public static final String KEY_MODEL_NODE = "mini.sceneEditor.modelNode";
    private static final Array<Consumer<Spatial>> PRE_TRANSFORM_HANDLERS = ArrayFactory.newArray(Consumer.class);
    private static final Array<Consumer<Spatial>> POST_TRANSFORM_HANDLERS = ArrayFactory.newArray(Consumer.class);

    private static final ObjectDictionary<Light.Type, Node> LIGHT_MODEL_TABLE;

    static {
        final AssetManager assetManager = EditorUtil.getAssetManager();

        LIGHT_MODEL_TABLE = DictionaryFactory.newObjectDictionary();
        //LIGHT_MODEL_TABLE.put(Light.Type.Point, assetManager.loadModel("graphics/models/light/point_light.minio"));
    }

    private final ObjectDictionary<Spatial, Geometry> selectionShape;
    private final ObjectDictionary<Object, Object> cachedPresentableObjects;
    private final Node modelNode;
    private final Node toolNode;
    private final Node transformToolNode;
    private final Node lightNode;
    private final Node presentableNode;
    private final Array<Spatial> selected;
    private final Array<EditorLightNode> lightNodes;
    private final Array<Object> presentableNodes;

    private Transform transformCenter;
    private Spatial toTransform;
    private Material selectionMaterial;

    private Node grid;
    private Node collisionPlane;
    private M currentModel;
    private boolean paintingMode;
    private boolean showSelection;
    private boolean showGrid;
    private TransformationMode transformMode;
    private TransformType transformType;

    public AbstractSceneEditor3DPart(T fileEditor) {
        super(fileEditor);

        cachedPresentableObjects = DictionaryFactory.newObjectDictionary();
        modelNode = new Node("TreeNode");
        modelNode.setUserData(KEY_MODEL_NODE, true);
        selected = ArrayFactory.newArray(Spatial.class);
        selectionShape = DictionaryFactory.newObjectDictionary();
        toolNode = new Node("ToolNode");
        transformToolNode = new Node("TransformToolNode");

        lightNodes = ArrayFactory.newArray(EditorLightNode.class);
        presentableNodes = ArrayFactory.newArray(EditorPresentableNode.class);

        lightNode = new Node("Lights");
        presentableNode = new Node("Presentable nodes");

        modelNode.attachChild(lightNode);
        modelNode.attachChild(presentableNode);

        createCollisionPlane();
        createToolElements();
        setShowSelection(true);
        setShowGrid(true);
        setTransformMode(TransformationMode.GLOBAL);
        setTransformType(TransformType.MOVE_TOOL);
    }

    @FromAnyThread
    private void createCollisionPlane() {
        final AssetManager assetManager = EditorUtil.getAssetManager();

        final Material material = new Material(assetManager, "MatDefs/Misc/Unshaded.minid");
        final RenderState renderState = material.getAdditionalRenderState();
        renderState.setFaceCullMode(RenderState.FaceCullMode.Off);
        renderState.setWireframe(true);

        final float size = 20000;

        final Geometry geometry = new Geometry("plane", new Quad(size, size));
        geometry.setMaterial(material);
        geometry.setLocalTranslation(-size / 2, -size / 2, 0);

        collisionPlane = new Node();
        collisionPlane.attachChild(geometry);
    }

    @Override
    @FromAnyThread
    protected boolean needEditorCamera() {
        return true;
    }

    @FromAnyThread
    private void createToolElements() {
        selectionMaterial = createColorMaterial(new ColorRGBA(1F, 170 / 255F, 64 / 255F, 1F));
        grid = createGrid();

        final Node toolNode = getToolNode();
        toolNode.attachChild(grid);
    }

    private Node createGrid() {
        final Node gridNode = new Node("GridNode");

        final ColorRGBA gridColor = new ColorRGBA(0.4f, 0.4f, 0.4f, 0.5f);
        final ColorRGBA xColor = new ColorRGBA(1.0f, 0.1f, 0.1f, 0.5f);
        final ColorRGBA zColor = new ColorRGBA(0.1f, 1.0f, 0.1f, 0.5f);

        final Material gridMaterial = createColorMaterial(gridColor);
        gridMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        final Material xMaterial = createColorMaterial(xColor);
        xMaterial.getAdditionalRenderState().setLineWidth(5);

        final Material zMaterial = createColorMaterial(zColor);
        zMaterial.getAdditionalRenderState().setLineWidth(5);

        final int gridSize = getGridSize();

        final Geometry grid = new Geometry("grid", new Grid(gridSize, gridSize, 1.0f));
        grid.setMaterial(gridMaterial);
        grid.setQueueBucket(RenderQueue.Bucket.Transparent);
        grid.setShadowMode(RenderQueue.ShadowMode.Off);
        grid.setCullHint(Spatial.CullHint.Never);
        grid.setLocalTranslation(gridSize * -0.5f, 0, gridSize * -0.5f);

        final Quad quad = new Quad(gridSize, gridSize);
        final Geometry gridCollision = new Geometry("collision", quad);
        gridCollision.setMaterial(createColorMaterial(gridColor));
        gridCollision.setQueueBucket(RenderQueue.Bucket.Transparent);
        gridCollision.setShadowMode(RenderQueue.ShadowMode.Off);
        gridCollision.setCullHint(Spatial.CullHint.Always);
        gridCollision.setLocalTranslation(gridSize * -0.5f, 0, gridSize * -0.5f);
        gridCollision.setLocalRotation(new Quaternion().fromAngles(90 * FastMath.DEG_TO_RAD, 0, 0));

        gridNode.attachChild(grid);
        gridNode.attachChild(gridCollision);

        // Red line for X Axis
        final Line xAxis = new Line(new Vector3f(-gridSize * 0.5f, 0, 0), new Vector3f(gridSize * 0.5f - 1, 0, 0));

        final Geometry gxAxis = new Geometry("XAxis", xAxis);
        gxAxis.setModelBound(new BoundingBox());
        gxAxis.setShadowMode(RenderQueue.ShadowMode.Off);
        gxAxis.setCullHint(Spatial.CullHint.Never);
        gxAxis.setMaterial(zMaterial);

        gridNode.attachChild(gxAxis);

        // Blue line for Z Axis
        final Line zAxis = new Line(new Vector3f(0, 0, -gridSize * 0.5f), new Vector3f(0, 0, gridSize * 0.5f - 1));

        final Geometry gzAxis = new Geometry("XAxis", zAxis);
        gzAxis.setModelBound(new BoundingBox());
        gzAxis.setShadowMode(RenderQueue.ShadowMode.Off);
        gzAxis.setCullHint(Spatial.CullHint.Never);
        gzAxis.setMaterial(zMaterial);

        gridNode.attachChild(gzAxis);

        return gridNode;
    }

    @FromAnyThread
    private Material createColorMaterial(final ColorRGBA color) {
        final Material material = new Material(EditorUtil.getAssetManager(), "MatDefs/Misc/Unshaded.minid");
        material.getAdditionalRenderState().setWireframe(true);
        material.setColor("Color", color);
        return material;
    }

    @FromAnyThread
    protected int getGridSize() {
        return 20;
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

    @EditorThread
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

    @EditorThread
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

        if (isShowSelection()) {
            getToolNode().attachChild(shape);
        }

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
    @EditorThread
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
    @EditorThread
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

    @EditorThread
    public void notifyPropertyPreChanged(final Object object, final String name) {
        if (!(object instanceof Spatial)) {
            return;
        }

        if (isTranformationProperty(name)) {
            PRE_TRANSFORM_HANDLERS.forEach((Spatial) object, Consumer::accept);
        }
    }

    public void notifyPropertyChanged(
            Object object,
            final String name) {
        if (object instanceof SimpleProperty) {
            object = ((SimpleProperty) object).getObject();
        }

        if (object instanceof Light) {
            final EditorLightNode node = getLightNode((Light) object);
            if (node != null) {
                node.sync();
            }
        }

        if (object instanceof Spatial) {
            if (isTranformationProperty(name)) {
                POST_TRANSFORM_HANDLERS.forEach((Spatial) object, Consumer::accept);
            }
        }
    }

    private EditorLightNode getLightNode(final Light light) {
        return getLightNodes().search(light, (node, toCheck) -> node.getLight() == toCheck);
    }

    private boolean isTranformationProperty(final String name) {
        return Messages.MODEL_PROPERTY_LOCATION.equals(name) ||
                Messages.MODEL_PROPERTY_SCALE.equals(name) ||
                Messages.MODEL_PROPERTY_ROTATION.equals(name);
        // TODO: Also include Transformation
    }

    @FromAnyThread
    public void openModel(final M model) {
        EXECUTOR_MANAGER.addEditorTask(() -> openModelImpl(model));
    }

    @FromAnyThread
    public void addLight(final Light light) {
        EXECUTOR_MANAGER.addEditorTask(() -> addLightImpl(light));
    }

    @EditorThread
    private void addLightImpl(final Light light) {
        throw new UnsupportedOperationException();
    }

    @FromAnyThread
    public void removeLight(final Light light) {
        EXECUTOR_MANAGER.addEditorTask(() -> removeLightImpl(light));
    }

    @EditorThread
    private void removeLightImpl(final Light light) {
        throw new UnsupportedOperationException();
    }

    @EditorThread
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
    public Array<EditorLightNode> getLightNodes() {
        return lightNodes;
    }

    @FromAnyThread
    public void changePaintingMode(final boolean paintingMode) {
        EXECUTOR_MANAGER.addEditorTask(() -> changePaintingModeImpl(paintingMode));
    }

    @EditorThread
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

    @FromAnyThread
    public void updateShowSelection(final boolean showSelection) {
        EXECUTOR_MANAGER.addEditorTask(() -> updateShowSelectionImpl(showSelection));
    }

    @EditorThread
    private void updateShowSelectionImpl(final boolean showSelection) {
        if (isShowSelection() == showSelection) {
            return;
        }

        final ObjectDictionary<Spatial, Geometry> selectionShape = getSelectionShape();
        final Node toolNode = getToolNode();

        if (showSelection && !selectionShape.isEmpty()) {
            selectionShape.forEach(toolNode::attachChild);
        } else if (!showSelection && !selectionShape.isEmpty()) {
            selectionShape.forEach(toolNode::detachChild);
        }

        setShowSelection(showSelection);
    }

    @EditorThread
    private boolean isShowSelection() {
        return showSelection;
    }

    @EditorThread
    private void setShowSelection(boolean showSelection) {
        this.showSelection = showSelection;
    }

    @FromAnyThread
    protected Node getToolNode() {
        return toolNode;
    }

    @FromAnyThread
    public void updateShowGrid(final boolean showGrid) {
        EXECUTOR_MANAGER.addEditorTask(() -> updateShowGridImpl(showGrid));
    }

    private void updateShowGridImpl(final boolean showGrid) {
        if (isShowGrid() == showGrid) {
            return;
        }

        final Node toolNode = getToolNode();
        final Node grid = getGrid();

        if (showGrid) {
            toolNode.attachChild(grid);
        } else {
            toolNode.detachChild(grid);
        }

        setShowGrid(showGrid);
    }


    public boolean isShowGrid() {
        return showGrid;
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
    }

    public void setTransformMode(TransformationMode transformMode) {
        this.transformMode = transformMode;
    }

    public TransformationMode getTransformMode() {
        return transformMode;
    }

    public void setTransformType(TransformType transformType) {
        this.transformType = transformType;
    }

    public TransformType getTransformType() {
        return transformType;
    }

    /**
     * @return grid of the scene
     */
    private Node getGrid() {
        return ObjectsUtil.notNull(grid);
    }

    public Vector3f getScenePosByScreenPos(float screenX, float screenY) {
        final Camera camera = getCamera();
        final M currentModel = getCurrentModel();

        final Vector3f modelPoint = GeomUtils.getContactPointFromScreenPos(currentModel, camera, screenX, screenY);
        final Vector3f gridPoint = GeomUtils.getContactPointFromScreenPos(getGrid(), camera, screenX, screenY);

        if (modelPoint == null) {
            return gridPoint == null ? Vector3f.ZERO : gridPoint;
        } else if (gridPoint == null) {
            return modelPoint;
        }

        final float distance = modelPoint.distance(camera.getLocation());
        if (gridPoint.distance(camera.getLocation()) < distance) {
            return gridPoint;
        } else {
            return modelPoint;
        }
    }
}
