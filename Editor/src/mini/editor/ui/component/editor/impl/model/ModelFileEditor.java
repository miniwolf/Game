package mini.editor.ui.component.editor.impl.model;

import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import javafx.scene.layout.StackPane;
import mini.asset.ModelKey;
import mini.editor.FileExtensions;
import mini.editor.Messages;
import mini.editor.annotation.FromAnyThread;
import mini.editor.part3d.editor.impl.model.ModelEditor3DPart;
import mini.editor.ui.component.editor.EditorDescription;
import mini.editor.ui.component.editor.impl.scene.AbstractSceneFileEditor;
import mini.editor.ui.component.editor.state.EditorState;
import mini.editor.ui.component.editor.state.impl.EditorModelEditorState;
import mini.editor.ui.component.tab.ScrollableEditorToolComponent;
import mini.editor.util.EditorUtil;
import mini.editor.util.NodeUtils;
import mini.editor.util.ObjectsUtil;
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
    protected void processChangeTool(Number oldValue, Number newValue) {
    }

    @Override
    protected void createToolComponents(ScrollableEditorToolComponent container, StackPane root) {
    }

    @Override
    @FromAnyThread
    public EditorDescription getDescription() {
        return DESCRIPTION;
    }
}
