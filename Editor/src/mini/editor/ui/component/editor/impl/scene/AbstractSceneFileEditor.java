package mini.editor.ui.component.editor.impl.scene;

import com.ss.rlib.common.util.ObjectUtils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import javafx.event.Event;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.StackPane;
import mini.editor.FileExtensions;
import mini.editor.annotation.FxThread;
import mini.editor.model.editor.Editor3DProvider;
import mini.editor.model.undo.editor.ModelChangeConsumer;
import mini.editor.plugin.api.editor.Advanced3DFileEditorWithSplitRightTool;
import mini.editor.ui.component.editor.EditorDescription;
import mini.editor.ui.component.editor.state.EditorState;
import mini.editor.ui.component.editor.state.impl.BaseEditorSceneEditorState;
import mini.editor.ui.component.painting.PaintingComponentContainer;
import mini.editor.ui.control.model.ModelNodeTree;
import mini.editor.ui.control.model.ModelPropertyEditor;
import mini.editor.ui.control.tree.TreeNode;
import mini.editor.util.NodeUtils;
import mini.editor.util.ObjectsUtil;
import mini.editor.util.UIUtils;
import mini.light.Light;
import mini.math.Vector3f;
import mini.renderer.Camera;
import mini.scene.Node;
import mini.scene.Spatial;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AbstractSceneFileEditor<M extends Spatial, MA extends AbstractSceneEditor3DPart, ES extends BaseEditorSceneEditorState>
        extends Advanced3DFileEditorWithSplitRightTool<MA, ES>
        implements ModelChangeConsumer, Editor3DProvider {

    private static final Array<String> ACCEPTED_FILES = ArrayFactory.asArray(
            FileExtensions.MINI_MATERIAL,
            FileExtensions.MINI_OBJECT);

    private static final Array<Spatial> EMPTY_SELECTION = ArrayFactory.newArray(Spatial.class);

    /**
     * The opened model
     */
    private M currentModel;

    private ModelNodeTree modelNodeTree;
    private ModelPropertyEditor modelPropertyEditor;
    private PaintingComponentContainer paintingComponentContainer;
    private boolean ingoreCameraMove;
    private Consumer<Array<Object>> selectionNodeHandler;

    @Override
    protected void createContent(StackPane root) {
        this.selectionNodeHandler = this::selectNodesFromTree;

        paintingComponentContainer = new PaintingComponentContainer(this, this);

        super.createContent(root);

        final StackPane editorAreaPane = getEditorAreaPane();

        modelNodeTree = new ModelNodeTree(selectionNodeHandler, this);
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
    protected Supplier<EditorState> getEditorStateFactory() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doSave(Path toStore) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public EditorDescription getDescription() {
        return null;
    }

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
        } else {
            throw new UnsupportedOperationException();
        }

        if (spatial != null && !spatial.isVisible()) {
            spatial = null;
        }

        if (spatial != null && canSelect(spatial)) {
            editor3DPart.select(spatial);

            if (!isIngoreCameraMove() && !isVisibleOnEditor(spatial)) {
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

        getModelNodeTree();
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

    public boolean isIngoreCameraMove() {
        return ingoreCameraMove;
    }

    public void setIngoreCameraMove(boolean ingoreCameraMove) {
        this.ingoreCameraMove = ingoreCameraMove;
    }
}
