package mini.editor.ui.component.editor.impl.scene;

import com.ss.rlib.common.util.FileUtils;
import com.ss.rlib.common.util.ObjectUtils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import mini.asset.AssetManager;
import mini.asset.ModelKey;
import mini.editor.FileExtensions;
import mini.editor.Messages;
import mini.editor.annotation.FxThread;
import mini.editor.control.transform.EditorTransformSupport;
import mini.editor.extension.scene.SceneLayer;
import mini.editor.model.editor.Editor3DProvider;
import mini.editor.model.undo.editor.ModelChangeConsumer;
import mini.editor.model.undo.impl.AddChildOperation;
import mini.editor.plugin.api.editor.Advanced3DFileEditorWithSplitRightTool;
import mini.editor.ui.component.editor.scripting.EditorScriptingComponent;
import mini.editor.ui.component.editor.state.impl.BaseEditorSceneEditorState;
import mini.editor.ui.component.painting.PaintingComponent;
import mini.editor.ui.component.painting.PaintingComponentContainer;
import mini.editor.ui.component.tab.ScrollableEditorToolComponent;
import mini.editor.ui.control.model.ModelNodeTree;
import mini.editor.ui.control.model.ModelPropertyEditor;
import mini.editor.ui.control.tree.TreeNode;
import mini.editor.ui.css.CssClasses;
import mini.editor.ui.event.impl.FileChangedEvent;
import mini.editor.util.*;
import mini.light.DirectionalLight;
import mini.light.Light;
import mini.light.PointLight;
import mini.material.Material;
import mini.math.Vector3f;
import mini.renderer.Camera;
import mini.scene.AssetLinkNode;
import mini.scene.Geometry;
import mini.scene.Node;
import mini.scene.Spatial;
import mini.scene.control.Control;
import mini.textures.Texture;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public abstract class AbstractSceneFileEditor<M extends Spatial, MA extends AbstractSceneEditor3DPart, ES extends BaseEditorSceneEditorState>
        extends Advanced3DFileEditorWithSplitRightTool<MA, ES>
        implements ModelChangeConsumer, Editor3DProvider {
    private static final int OBJECTS_TOOL = 0;
    private static final int PAINTING_TOOL = 1;
    private static final int SCRIPTING_TOOL = 2;

    private static final String KEY_LOADED_MODEL = "miniEditor.sceneEditor.loadedModel";

    private static final Array<String> ACCEPTED_FILES = ArrayFactory.asArray(
            FileExtensions.MINI_MATERIAL,
            FileExtensions.MINI_OBJECT);

    private static final Array<Spatial> EMPTY_SELECTION = ArrayFactory.newArray(Spatial.class);
    private static final ObservableList<EditorTransformSupport.TransformationMode> TRANSFORMATION_MODES =
            FXCollections.observableArrayList(EditorTransformSupport.TransformationMode.values());

    /**
     * The opened model
     */
    private M currentModel;

    private ModelNodeTree modelNodeTree;
    private ModelPropertyEditor modelPropertyEditor;
    private PaintingComponentContainer paintingComponentContainer;
    private boolean ingoreCameraMove;
    private Consumer<Array<Object>> selectionNodeHandler;
    private VBox modelNodeTreeObjectsContainer;
    private VBox propertyEditorObjectsContainer;
    private VBox modelNodeTreeEditingContainer;
    private EditorScriptingComponent scriptingComponent;
    private ComboBox<EditorTransformSupport.TransformationMode> transformModeComboBox;
    private ToggleButton selectionButton;
    private ToggleButton gridButton;
    private ToggleButton moveToolButton;
    private ToggleButton rotationToolButton;
    private ToggleButton scaleToolButton;

    public AbstractSceneFileEditor() {
        processChangeTool(-1, 0);
    }

    @Override
    protected void processChangedFile(FileChangedEvent event) {
        super.processChangedFile(event);

        final Path file = event.getFile();
        final String extension = FileUtils.getExtension(file);

        if (extension.endsWith(FileExtensions.MINI_MATERIAL)) {
            EXECUTOR_MANAGER.addEditorTask(() -> updateMaterial(file));
        } else if (MaterialUtils.isShaderFile(file) || MaterialUtils.isTextureFile(file)) {
            EXECUTOR_MANAGER.addEditorTask(() -> updateMaterials(file));
        }
    }

    @Override
    protected void loadState() {
        super.loadState();

        scriptingComponent.addVariable("root", getCurrentModel());
        scriptingComponent.addVariable("assetManager", EditorUtil.getAssetManager());
        scriptingComponent.addImport(Spatial.class);
        scriptingComponent.addImport(Geometry.class);
        scriptingComponent.addImport(Control.class);
        scriptingComponent.addImport(Node.class);
        scriptingComponent.addImport(Light.class);
        scriptingComponent.addImport(DirectionalLight.class);
        scriptingComponent.addImport(PointLight.class);
        scriptingComponent.addImport(Material.class);
        scriptingComponent.addImport(Texture.class);
        scriptingComponent.setExampleCode("root.attachChild(\nnew Node(\"created from Groovy\"));");
        scriptingComponent.buildHeader();

        final ES editorState = ObjectsUtil.notNull(getEditorState());

        gridButton.setSelected(editorState.isEnabledGrid());
        selectionButton.setSelected(editorState.isEnabledSelection());
        transformModeComboBox.getSelectionModel()
                .select(EditorTransformSupport.TransformationMode.valueOf(editorState.getTransformationMode()));

        final Array<PaintingComponent> components = paintingComponentContainer.getComponents();
        components.forEach(editorState, PaintingComponent::loadState);

        final EditorTransformSupport.TransformType transformType = EditorTransformSupport.TransformType.valueOf(editorState.getTransformationType());

        switch (transformType) {
            case MOVE_TOOL: {
                moveToolButton.setSelected(true);
                break;
            }
            case ROTATE_TOOL: {
                rotationToolButton.setSelected(true);
                break;
            }
            case SCALE_TOOL: {
                scaleToolButton.setSelected(true);
                break;
            }
            default: {
                break;
            }
        }
    }

    @Override
    @FxThread
    protected void createContent(StackPane root) {
        this.selectionNodeHandler = this::selectNodesFromTree;

        propertyEditorObjectsContainer = new VBox();
        modelNodeTreeObjectsContainer = new VBox();
        modelNodeTreeEditingContainer = new VBox();

        paintingComponentContainer = new PaintingComponentContainer(this, this);

        scriptingComponent = new EditorScriptingComponent(this::refreshTree);
        scriptingComponent.prefHeightProperty()
                .bind(root.heightProperty());

        super.createContent(root);

        final StackPane editorAreaPane = getEditorAreaPane();

        modelNodeTree = new ModelNodeTree(selectionNodeHandler, this);
        modelNodeTree.prefHeightProperty().bind(root.heightProperty());

        modelPropertyEditor = new ModelPropertyEditor(this);
        modelPropertyEditor.prefHeightProperty().bind(root.heightProperty());

        modelNodeTree.getTreeView().getStyleClass().add(CssClasses.TRANSPARENT_TREE_VIEW);
    }

    @Override
    protected void processChangeTool(Number oldValue, Number newValue) {
        final ModelNodeTree modelNodeTree = getModelNodeTree();
        final ModelPropertyEditor modelPropertyEditor = getModelPropertyEditor();

        final VBox modelNodeTreeParent = (VBox) modelNodeTree.getParent();
        final VBox propertyEditorParent = (VBox) modelPropertyEditor.getParent();

        if (propertyEditorParent != null) {
            propertyEditorParent.getChildren().remove(modelPropertyEditor);
        }

        if (modelNodeTreeParent != null) {
            modelNodeTreeParent.getChildren().remove(modelNodeTree);
        }

        final int oldIndex = oldValue == null ? -1 : oldValue.intValue();
        final int newIndex = newValue.intValue();

        final VBox propertyContainer = getPropertyEditorObjectsContainer();
        final PaintingComponentContainer editingComponentContainer
                = getPaintingComponentContainer();

        if (newIndex == OBJECTS_TOOL) {
            propertyContainer.getChildren().add(modelPropertyEditor);
            getModelNodeTreeObjectsContainer().getChildren().add(modelNodeTree);
            selectNodesFromTree(modelNodeTree.getSelectedItems());
        } else if (newIndex == PAINTING_TOOL) {
            getModelNodeTreeEditingContainer().getChildren().add(modelNodeTree);
            editingComponentContainer.notifyShowed();
        }

        if (oldIndex == PAINTING_TOOL) {
            editingComponentContainer.notifyHiding();
        }

        final MA editor3DPart = getEditor3DPart();
        editor3DPart.changePaintingMode(newIndex == PAINTING_TOOL);
    }

    private void updateMaterials(final Path file) {
        final M currentModel = getCurrentModel();
        final AtomicBoolean needRefresh = new AtomicBoolean();

        NodeUtils.visitGeometry(currentModel, geometry -> {
            final Material material = geometry.getMaterial();
            final Material newMaterial = MaterialUtils.updateMaterialIdNeed(file, material);

            if (newMaterial != null) {
                geometry.setMaterial(newMaterial);
                needRefresh.set(true);
            }
        });

        if (!needRefresh.get()) {
            return;
        }

        // TODO: Requires refreshment of RenderFilters
    }

    private void updateMaterial(final Path file) {
        final Path assetFile = ObjectUtils.notNull(
                EditorUtil.getAssetFile(file),
                "Not found asset file for: " + file);
        final String assetPath = EditorUtil.toAssetPath(assetFile);

        final M currentModel = getCurrentModel();

        final Array<Geometry> geometries = ArrayFactory.newArray(Geometry.class);
        NodeUtils.addGeometryWithMaterial(currentModel, geometries, assetPath);
        if (geometries.isEmpty()) {
            return;
        }

        final AssetManager assetManager = EditorUtil.getAssetManager();
        final Material material = assetManager.loadMaterial(assetPath);
        geometries.forEach(geometry -> geometry.setMaterial(material));
    }

    @Override
    @FxThread
    protected void createToolComponents(
            final ScrollableEditorToolComponent container,
            final StackPane root) {
        container.addComponent(buildSplitComponent(
                getModelNodeTreeObjectsContainer(),
                getPropertyEditorObjectsContainer(),
                root),
                               Messages.SCENE_FILE_EDITOR_TOOL_OBJECTS);
        container.addComponent(buildSplitComponent(
                getModelNodeTreeEditingContainer(),
                getPaintingComponentContainer(),
                root),
                               Messages.SCENE_FILE_EDITOR_TOOL_PAINTING);
        container.addComponent(getScriptingComponent(),
                               Messages.SCENE_FILE_EDITOR_TOOL_SCRIPTING);
    }

    @Override
    @FxThread
    protected boolean createToolbar(final HBox container) {
        createActions(container);

        final Label transformModeLabel = new Label(Messages.MODEL_FILE_EDITOR_TRANSFORM_MODE + ":");

        transformModeComboBox = new ComboBox<>(TRANSFORMATION_MODES);
        transformModeComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener(((observable, oldValue, newValue) ->
                        changeTransformMode(newValue)));

        container.getChildren().addAll(transformModeLabel, transformModeComboBox);

        return true;
    }

    private void createActions(final HBox container) {
        var saveAction = createSaveAction();
        container.getChildren().add(saveAction);

        selectionButton = new ToggleButton();
        selectionButton.setTooltip(new Tooltip(Messages.SCENE_FILE_EDITOR_ACTION_SELECTION));
        selectionButton.setSelected(true);
        selectionButton.selectedProperty().addListener(
                ((observable, oldValue, newValue) -> changeSelectionVisible(newValue)));

        gridButton = new ToggleButton();
        gridButton.setTooltip(new Tooltip(Messages.SCENE_FILE_EDITOR_ACTION_GRID));
        gridButton.setSelected(true);
        gridButton.selectedProperty().addListener(
                ((observable, oldValue, newValue) -> changeGridVisible(newValue)));

        moveToolButton = new ToggleButton();
        moveToolButton.setTooltip(new Tooltip(Messages.SCENE_FILE_EDITOR_ACTION_MOVE_TOOL + " (G)"));
        moveToolButton.setSelected(true);
        moveToolButton.selectedProperty().addListener(
                ((observable, oldValue, newValue) ->
                        updateTransformTool(
                                EditorTransformSupport.TransformType.MOVE_TOOL,
                                newValue)));

        rotationToolButton = new ToggleButton();
        rotationToolButton.setTooltip(new Tooltip(Messages.SCENE_FILE_EDITOR_ACTION_ROTATION_TOOL + " (R)"));
        rotationToolButton.selectedProperty().addListener(
                ((observable, oldValue, newValue) ->
                        updateTransformTool(
                                EditorTransformSupport.TransformType.ROTATE_TOOL,
                                newValue)));

        scaleToolButton = new ToggleButton();
        scaleToolButton.setTooltip(new Tooltip(Messages.SCENE_FILE_EDITOR_ACTION_SCALE_TOOL + " (S)"));
        scaleToolButton.selectedProperty().addListener(
                ((observable, oldValue, newValue) ->
                        updateTransformTool(
                                EditorTransformSupport.TransformType.SCALE_TOOL,
                                newValue)));

        selectionButton.getStyleClass().add(CssClasses.FILE_EDITOR_TOOLBAR_BUTTON);
        gridButton.getStyleClass().add(CssClasses.FILE_EDITOR_TOOLBAR_BUTTON);
        moveToolButton.getStyleClass().add(CssClasses.FILE_EDITOR_TOOLBAR_BUTTON);
        rotationToolButton.getStyleClass().add(CssClasses.FILE_EDITOR_TOOLBAR_BUTTON);
        scaleToolButton.getStyleClass().add(CssClasses.FILE_EDITOR_TOOLBAR_BUTTON);

        container.getChildren().addAll(
                selectionButton,
                gridButton,
                moveToolButton,
                rotationToolButton,
                scaleToolButton);
    }

    private void updateTransformTool(
            final EditorTransformSupport.TransformType transformType,
            final boolean newValue) {
        final MA editor3DPart = getEditor3DPart();
        final ToggleButton scaleToolButton = getScaleToolButton();
        final ToggleButton moveToolButton = getMoveToolButton();
        final ToggleButton rotationToolButton = getRotationToolButton();

        if (newValue != Boolean.TRUE) {
            if (editor3DPart.getTransformType() == transformType) {
                if (transformType == EditorTransformSupport.TransformType.MOVE_TOOL) {
                    moveToolButton.setSelected(true);
                } else if (transformType == EditorTransformSupport.TransformType.ROTATE_TOOL) {
                    rotationToolButton.setSelected(true);
                } else if (transformType == EditorTransformSupport.TransformType.SCALE_TOOL) {
                    scaleToolButton.setSelected(true);
                }
            }
            return;
        }

        final ES editorState = getEditorState();
        editor3DPart.setTransformType(transformType);

        if (transformType == EditorTransformSupport.TransformType.MOVE_TOOL) {
            rotationToolButton.setSelected(false);
            scaleToolButton.setSelected(false);
        } else if (transformType == EditorTransformSupport.TransformType.ROTATE_TOOL) {
            moveToolButton.setSelected(false);
            scaleToolButton.setSelected(false);
        } else if (transformType == EditorTransformSupport.TransformType.SCALE_TOOL) {
            rotationToolButton.setSelected(false);
            moveToolButton.setSelected(false);
        }

        if (editorState != null) {
            editorState.setTransformationType(transformType.ordinal());
        }
    }

    @FxThread
    private void changeGridVisible(final boolean newValue) {
        if (isIgnoreListeners()) {
            return;
        }

        final MA editor3DPart = getEditor3DPart();
        editor3DPart.updateShowGrid(newValue);

        final ES editorState = getEditorState();
        if (editorState != null) {
            editorState.setEnableGrid(newValue);
        }
    }

    @FxThread
    private void changeSelectionVisible(boolean newValue) {
        if (isIgnoreListeners()) {
            return;
        }

        final MA editor3DPart = getEditor3DPart();
        editor3DPart.updateShowSelection(newValue);

        final ES editorState = getEditorState();
        if (editorState != null) {
            editorState.setEnableSelection(newValue);
        }
    }

    @FxThread
    private Button createSaveAction() {
        final Button action = new Button();
        action.setTooltip(new Tooltip(Messages.FILE_EDITOR_ACTION_SAVE + " (Ctrl + S)"));
        action.setOnAction(event -> save(null));
        action.disableProperty().bind(dirtyProperty().not());

        action.getStyleClass().addAll(
                CssClasses.FLAT_BUTTON,
                CssClasses.FILE_EDITOR_TOOLBAR_BUTTON);

        return action;
    }

    private void changeTransformMode(EditorTransformSupport.TransformationMode transformationMode) {
        final MA editor3DPart = getEditor3DPart();
        editor3DPart.setTransformMode(transformationMode);

        final ES editorState = getEditorState();

        if (editorState != null) {
            editorState.setTransformationMode(transformationMode.ordinal());
        }
    }

    protected void refreshTree() {
        getModelNodeTree().fill(getCurrentModel());
    }

    @Override
    protected void handleDragOverEvent(DragEvent dragEvent) {
        UIUtils.acceptIfHasFile(dragEvent, ACCEPTED_FILES);
    }

    @Override
    protected void handleDragDroppedEvent(DragEvent dragEvent) {
        UIUtils.handleDroppedFile(dragEvent, FileExtensions.MINI_OBJECT, this, dragEvent,
                                  AbstractSceneFileEditor::addNewModel);

        UIUtils.handleDroppedFile(dragEvent, FileExtensions.MINI_MATERIAL, this, dragEvent,
                                  AbstractSceneFileEditor::applyMaterial);
    }

    @Override
    protected void doSave(Path toStore) throws IOException {
        throw new UnsupportedOperationException();
    }

    @FxThread
    public void selectNodesFromTree(final Array<?> objects) {
        final MA editor3DPart = getEditor3DPart();
        editor3DPart.select(EMPTY_SELECTION);

        if (objects.size() > 1) {
            throw new UnsupportedOperationException();
            //multiSelectNodesFromTree(objects, editor3DPart);
        } else if (objects.size() == 1) {
            singleSelectNodeFromTree(objects, editor3DPart);
            return;
        } else {
            editor3DPart.select(EMPTY_SELECTION);
        }

        getModelPropertyEditor().buildFor(null, null);
        getPaintingComponentContainer().prepareFor(null);
    }

    /**
     * Handle single select nodes from tree
     */
    private void singleSelectNodeFromTree(final Array<?> objects,
                                          final MA editor3DPart) {
        Object parent = null;
        Object element;

        final Object first = objects.first();
        if (first instanceof TreeNode<?>) {
            final TreeNode treeNode = (TreeNode) first;
            final TreeNode parentNode = treeNode.getParent();
            if (parentNode != null) {
                parent = parentNode.getElement();
            }
            element = treeNode.getElement();
        } else {
            element = first;
        }

//        if (element instanceof SceneLayer) {
//            element = null;
//        } TODO:

        Spatial spatial = null;

        if (element instanceof Spatial) {
            spatial = (Spatial) element;
            parent = spatial.getParent();
        } else if (element instanceof Light) {
            throw new UnsupportedOperationException();
        }

        if (spatial != null && !spatial.isVisible()) {
            spatial = null;
        }

        if (spatial != null && canSelect(spatial)) {
            editor3DPart.select(spatial);

            if (!isIgnoreCameraMove() && !isVisibleOnEditor(spatial)) {
                editor3DPart.cameraLookAt(spatial);
            }
        }

        getModelPropertyEditor().buildFor(element, parent);
        getPaintingComponentContainer().prepareFor(element);
    }

    private boolean isVisibleOnEditor(Spatial spatial) {
        final MA editor3DPart = getEditor3DPart();
        final Camera camera = editor3DPart.getCamera();

        final Vector3f position = spatial.getWorldTranslation();
        final Vector3f coordinates = camera.getScreenCoordinates(position, new Vector3f());

        boolean isVisible = coordinates.getZ() < 0F || coordinates.getZ() > 1F;
        isVisible = isVisible || !isInside(coordinates.getX(),
                                           camera.getHeight() - coordinates.getY(), Event.class);

        return !isVisible;
    }

    @FxThread
    protected boolean canSelect(final Spatial spatial) {
        return true;
    }

    /**
     * Add a new model from an asset tree.
     */
    private void addNewModel(final DragEvent dragEvent,
                             final Path file) {
        final M currentModel = getCurrentModel();
        if (!(currentModel instanceof Node)) {
            return;
        }

        final ModelNodeTree nodeTree = getModelNodeTree();
        final Object selected = nodeTree.getSelectedObject();

        final Node parent;

        if (selected instanceof Node
                && nodeTree.getSelectedCount() == 1
                && NodeUtils.findParent((Spatial) selected, AssetLinkNode.class::isInstance) == null) {
            parent = (Node) selected;
        } else {
            parent = (Node) currentModel;
        }

        final Path assetFile = ObjectsUtil.notNull(EditorUtil.getAssetFile(file), "Not found asset file for: " + file);
        final String assetPath = EditorUtil.toAssetPath(assetFile);

        final MA editor3DPart = getEditor3DPart();
        final ModelKey modelKey = new ModelKey(assetPath);
        final Camera camera = editor3DPart.getCamera();

        final BorderPane area = get3DArea();
        final Point2D areaPoint = area.sceneToLocal(dragEvent.getSceneX(), dragEvent.getSceneY());

        EXECUTOR_MANAGER.addEditorTask(() -> {
            final SceneLayer defaultLayer = EditorUtil.getDefaultLayer(this);
            final LocalObjects local = LocalObjects.get();

            final AssetManager assetManager = EditorUtil.getAssetManager();
            final Spatial loadedModel = assetManager.loadModel(modelKey);

            final AssetLinkNode assetLinkNode = new AssetLinkNode(modelKey);
            assetLinkNode.attachLinkedChild(loadedModel, modelKey);
            assetLinkNode.setUserData(KEY_LOADED_MODEL, true);

            if (defaultLayer != null) {
                SceneLayer.setLayer(defaultLayer, assetLinkNode);
            }

            final Vector3f scenePoint = editor3DPart.getScenePosByScreenPos(
                    (float) areaPoint.getX(),
                    camera.getHeight() - (float) areaPoint.getY());
            final Vector3f result = local.nextVector(scenePoint)
                    .subtractLocal(parent.getWorldTranslation());

            assetLinkNode.setLocalTranslation(result);
            execute(new AddChildOperation(assetLinkNode, parent, false));

        });

    }

    protected void handleAddedObject(final Spatial model) {
        final MA editor3DPart = getEditor3DPart();
        final Array<Light> lights = ArrayFactory.newArray(Light.class);

        NodeUtils.addLight(model, lights);

        lights.forEach(editor3DPart, (light, part) -> part.addLight(light));
    }

    @Override
    @FxThread
    public void notifyJavaFXRemovedChild(final Object parent,
                                         final Object removed) {
        final MA editor3DPart = getEditor3DPart();
        final ModelNodeTree modelNodeTree = getModelNodeTree();
        modelNodeTree.notifyRemoved(parent, removed);

        if (removed instanceof Light) {
            editor3DPart.removeLight((Light) removed);
        } else if (removed instanceof Spatial) {
            handleRemovedObject((Spatial) removed);
        }
    }

    @Override
    public void notifyJavaFXChangedProperty(Object object, String propertyName) {
    }

    @Override
    public void notifyJavaFXAddedChild(
            final Object parent,
            final Object added,
            final int index,
            final boolean needSelect) {
        final MA editor3DPart = getEditor3DPart();
        final ModelNodeTree modelNodeTree = getModelNodeTree();
        modelNodeTree.notifyAdded(parent, added, index);

        if (added instanceof Light) {
            editor3DPart.addLight((Light) added);
        } else if (added instanceof Spatial) {
            handleAddedObject((Spatial) added);
        }

        if (needSelect) {
            EXECUTOR_MANAGER.addEditorTask(() -> EXECUTOR_MANAGER.addFXTask(() -> modelNodeTree.selectSingle(added)));
        }
    }

    @Override
    public void notifyEditorPreChangedProperty(
            final Object object,
            final String propertyName) {
        getEditor3DPart().notifyPropertyPreChanged(object, propertyName);
    }

    @Override
    public void notifyEditorChangedProperty(Object object, String propertyName) {
        getEditor3DPart().notifyPropertyChanged(object, propertyName);
    }

    private void handleRemovedObject(Spatial model) {
        final MA editor3DPart = getEditor3DPart();
        final Array<Light> lights = ArrayFactory.newArray(Light.class);

        NodeUtils.addLight(model, lights);

        lights.forEach(editor3DPart, ((light, part) -> part.removeLight(light)));
    }

    /**
     * Apply a new material from an asset tree.
     */
    private void applyMaterial(final DragEvent dragEvent,
                               final Path file) {
        throw new UnsupportedOperationException();
    }

    public M getCurrentModel() {
        return ObjectsUtil.notNull(currentModel);
    }

    /**
     * Sets current model
     *
     * @param currentModel the opened model
     */
    public void setCurrentModel(final M currentModel) {
        this.currentModel = currentModel;
    }

    public ModelNodeTree getModelNodeTree() {
        return ObjectUtils.notNull(modelNodeTree);
    }

    public ModelPropertyEditor getModelPropertyEditor() {
        return ObjectsUtil.notNull(modelPropertyEditor);
    }

    public PaintingComponentContainer getPaintingComponentContainer() {
        return ObjectsUtil.notNull(paintingComponentContainer);
    }

    public boolean isIgnoreCameraMove() {
        return ingoreCameraMove;
    }

    public void setIngoreCameraMove(boolean ingoreCameraMove) {
        this.ingoreCameraMove = ingoreCameraMove;
    }

    @FxThread
    private VBox getModelNodeTreeObjectsContainer() {
        return ObjectUtils.notNull(modelNodeTreeObjectsContainer);
    }

    @FxThread
    private VBox getPropertyEditorObjectsContainer() {
        return ObjectUtils.notNull(propertyEditorObjectsContainer);
    }

    @FxThread
    private VBox getModelNodeTreeEditingContainer() {
        return ObjectUtils.notNull(modelNodeTreeEditingContainer);
    }

    @FxThread
    private EditorScriptingComponent getScriptingComponent() {
        return ObjectUtils.notNull(scriptingComponent);
    }

    @FxThread
    private ToggleButton getScaleToolButton() {
        return ObjectsUtil.notNull(scaleToolButton);
    }

    @FxThread
    private ToggleButton getMoveToolButton() {
        return ObjectsUtil.notNull(moveToolButton);
    }

    @FxThread
    private ToggleButton getRotationToolButton() {
        return ObjectsUtil.notNull(rotationToolButton);
    }
}
