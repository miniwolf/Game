package mini.editor.ui.component.editor.impl.model;

import com.ss.rlib.common.util.ObjectUtils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import mini.asset.ModelKey;
import mini.editor.FileExtensions;
import mini.editor.Messages;
import mini.editor.annotation.FromAnyThread;
import mini.editor.annotation.FxThread;
import mini.editor.part3d.editor.impl.model.ModelEditor3DPart;
import mini.editor.ui.component.editor.EditorDescription;
import mini.editor.ui.component.editor.FileEditor;
import mini.editor.ui.component.editor.impl.scene.AbstractSceneFileEditor;
import mini.editor.ui.component.editor.state.EditorState;
import mini.editor.ui.component.editor.state.impl.EditorModelEditorState;
import mini.editor.ui.control.tree.action.impl.spatial.SpatialTreeNode;
import mini.editor.util.EditorUtil;
import mini.editor.util.NodeUtils;
import mini.editor.util.ObjectsUtil;
import mini.renderer.queue.RenderQueue;
import mini.scene.Geometry;
import mini.scene.Spatial;

import java.nio.file.Path;
import java.util.function.Supplier;

public class ModelFileEditor
        extends AbstractSceneFileEditor<Spatial, ModelEditor3DPart, EditorModelEditorState> {
    public static final EditorDescription DESCRIPTION = new EditorDescription();

    static {
        DESCRIPTION.setEditorName(Messages.MODEL_FILE_EDITOR_NAME);
        DESCRIPTION.setConstructor(ModelFileEditor::new);
        DESCRIPTION.setEditorId(ModelFileEditor.class.getSimpleName());
        DESCRIPTION.addExtension(FileExtensions.MINI_OBJECT);
        DESCRIPTION.addExtension(FileExtensions.OBJ);
        DESCRIPTION.addExtension(FileExtensions.MODEL_FBX);
    }

    private ModelFileEditor() {
    }

    @Override
    protected ModelEditor3DPart create3DEditorPart() {
        return new ModelEditor3DPart(this);
    }

    @Override
    protected Supplier<EditorState> getEditorStateFactory() {
        return EditorModelEditorState::new;
    }

    @Override
    protected void doOpenFile(Path file) {
        var assetFile = ObjectsUtil.notNull(EditorUtil.getAssetFile(file),
                                            "Asset file for " + file + " cannot be null.");
        var modelKey = new ModelKey(EditorUtil.toAssetPath(assetFile));

        var assetManager = EditorUtil.getAssetManager();
        var model = assetManager.loadAsset(modelKey);

        var editor3DPart = getEditor3DPart();
        editor3DPart.openModel(model);

        handleAddedObject(model);

        setCurrentModel(model);
        // TODO: Ignore listeners

        refreshTree();
    }

    @Override
    protected void loadState() {
        super.loadState();

        final EditorModelEditorState editorState = ObjectUtils.notNull(getEditorState());

    }

    @Override
    protected void handleAddedObject(final Spatial model) {
        super.handleAddedObject(model);

        final ModelEditor3DPart editor3DState = getEditor3DPart();
        final Array<Geometry> geometries = ArrayFactory.newArray(Geometry.class);

        NodeUtils.addGeometry(model, geometries);

        if (!geometries.isEmpty()) {
            geometries.forEach(geometry -> {
                // TODO: sky needs to be added.
            });
        }
    }

    @Override
    public void notifyJavaFXRemovedChild(Object parent, Object removed) {
        super.notifyJavaFXRemovedChild(parent, removed);

        final ModelEditor3DPart editor3DPart = getEditor3DPart();

        if (removed instanceof Spatial) {
            final Spatial spatial = (Spatial) removed;
            final boolean isSky = spatial.getQueueBucket() == RenderQueue.Bucket.Sky;

            if (isSky) {

            }
        }

        // TODO: Notify child processes
    }

    @Override
    public void notifyJavaFXChangedProperty(Object object, String propertyName) {
    }

    @Override
    public void notifyJavaFXAddedChild(Object parent, Object added, int index, boolean needSelect) {
        final ModelEditor3DPart editor3DPart = getEditor3DPart();

        if (added instanceof Spatial) {
            final Spatial spatial = (Spatial) added;

            // TODO: check for sky
        }
    }

    @Override
    @FxThread
    protected boolean createToolbar(HBox container) {
        super.createToolbar(container);
        final Label fastSkyLabel = new Label(Messages.MODEL_FILE_EDITOR_FAST_SKY + ":");

        // TODO: Create combo box for sky
        container.getChildren().add(fastSkyLabel);
        return true;
    }

    @Override
    @FromAnyThread
    public EditorDescription getDescription() {
        return DESCRIPTION;
    }

    @Override
    public void notifyClosed() {
    }
}
