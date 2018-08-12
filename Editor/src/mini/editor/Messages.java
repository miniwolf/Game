package mini.editor;

import com.ss.rlib.common.util.PropertyLoader;

import java.util.ResourceBundle;

public class Messages {
    public static final String BUNDLE_NAME = "messages/messages";

    public static final String LOG_VIEW_TITLE;

    public static final String SCENE_FILE_EDITOR_NAME;
    public static final String SCENE_FILE_EDITOR_ACTION_SELECTION;
    public static final String SCENE_FILE_EDITOR_ACTION_GRID;
    public static final String SCENE_FILE_EDITOR_ACTION_MOVE_TOOL;
    public static final String SCENE_FILE_EDITOR_ACTION_ROTATION_TOOL;
    public static final String SCENE_FILE_EDITOR_ACTION_SCALE_TOOL;
    public static final String SCENE_FILE_EDITOR_TOOL_OBJECTS;
    public static final String SCENE_FILE_EDITOR_TOOL_PAINTING;
    public static final String SCENE_FILE_EDITOR_TOOL_SCRIPTING;

    public static final String EDITOR_TOOL_ASSET;
    public static final String EDITOR_AREA_SAVE_FILE_QUESTION;

    public static final String EDITOR_MENU_FILE;
    public static final String EDITOR_MENU_FILE_OPEN_ASSET;
    public static final String EDITOR_MENU_FILE_OPEN_ASSET_DIRECTORY_CHOOSER;
    public static final String EDITOR_MENU_FILE_EXIT;
    public static final String EDITOR_MENU_OTHER;
    public static final String EDITOR_MENU_HELP;

    public static final String MODEL_FILE_EDITOR_NAME;
    public static final String MODEL_FILE_EDITOR_NODE_DIRECTION_LIGHT;
    public static final String MODEL_FILE_EDITOR_FAST_SKY;
    public static final String MODEL_FILE_EDITOR_TRANSFORM_MODE;
    public static final String MODEL_NODE_TREE_ACTION_REMOVE;

    public static final String SIMPLE_DIALOG_BUTTON_OK;
    public static final String SIMPLE_DIALOG_BUTTON_CLOSE;
    public static final String SIMPLE_DIALOG_BUTTON_CANCEL;

    public static final String ASSET_COMPONENT_RESOURCE_TREE_CONTEXT_MENU_OPEN_FILE;

    public static final String BOUNDING_VOLUME_MODEL_PROPERTY_CONTROL_NAME;
    public static final String BOUNDING_VOLUME_MODEL_PROPERTY_CONTROL_SPHERE;
    public static final String BOUNDING_VOLUME_MODEL_PROPERTY_CONTROL_SPHERE_RADIUS;
    public static final String BOUNDING_VOLUME_MODEL_PROPERTY_CONTROL_BOX;

    public static final String MATERIAL_MODEL_PROPERTY_CONTROL_NO_MATERIAL;
    public static final String ABSTRACT_ELEMENT_PROPERTY_CONTROL_NO_ELEMENT;

    public static final String MODEL_PROPERTY_MATERIAL;
    public static final String MODEL_PROPERTY_CULL_HINT;
    public static final String MODEL_PROPERTY_SHADOW_MODE;
    public static final String MODEL_PROPERTY_QUEUE_BUCKET;
    public static final String MODEL_PROPERTY_LOCATION;
    public static final String MODEL_PROPERTY_SCALE;
    public static final String MODEL_PROPERTY_ROTATION;
    public static final String MODEL_PROPERTY_TRANSFORMATION;

    public static final String FILE_EDITOR_ACTION_SAVE;

    static {
        try {
            final ResourceBundle bundle = ResourceBundle
                    .getBundle(BUNDLE_NAME, PropertyLoader.getInstance());

            EDITOR_TOOL_ASSET = bundle.getString("EditorToolAsset");
            LOG_VIEW_TITLE = bundle.getString("LogViewTitle");

            EDITOR_AREA_SAVE_FILE_QUESTION = bundle.getString("EditorAreaSaveFileQuestion");

            SCENE_FILE_EDITOR_NAME = bundle.getString("SceneFileEditorName");
            SCENE_FILE_EDITOR_ACTION_SELECTION = bundle.getString("SceneFileEditorActionSelection");
            SCENE_FILE_EDITOR_ACTION_GRID = bundle.getString("SceneFileEditorActionGrid");
            SCENE_FILE_EDITOR_ACTION_MOVE_TOOL = bundle.getString("SceneFileEditorActionMoveTool");
            SCENE_FILE_EDITOR_ACTION_ROTATION_TOOL = bundle.getString("SceneFileEditorActionRotationTool");
            SCENE_FILE_EDITOR_ACTION_SCALE_TOOL = bundle.getString("SceneFileEditorActionScaleTool");
            SCENE_FILE_EDITOR_TOOL_OBJECTS = bundle.getString("SceneFileEditorToolObjects");
            SCENE_FILE_EDITOR_TOOL_PAINTING = bundle.getString("SceneFileEditorToolPainting");
            SCENE_FILE_EDITOR_TOOL_SCRIPTING = bundle.getString("SceneFileEditorToolScripting");

            MODEL_FILE_EDITOR_NAME = bundle.getString("ModelFileEditorName");
            MODEL_FILE_EDITOR_NODE_DIRECTION_LIGHT = bundle
                    .getString("ModelFileEditorNodeDirectionLight");
            MODEL_FILE_EDITOR_FAST_SKY = bundle.getString("ModelFileEditorFastSky");
            MODEL_FILE_EDITOR_TRANSFORM_MODE = bundle.getString("ModelFileEditorTransformMode");
            MODEL_NODE_TREE_ACTION_REMOVE = bundle.getString("ModelNodeTreeActionRemove");

            EDITOR_MENU_FILE = bundle.getString("EditorMenuFile");
            EDITOR_MENU_FILE_OPEN_ASSET = bundle.getString("EditorMenuFileOpenAsset");
            EDITOR_MENU_FILE_OPEN_ASSET_DIRECTORY_CHOOSER = bundle
                    .getString("EditorMenuFileOpenAssetDirectoryChooser");
            EDITOR_MENU_FILE_EXIT = bundle.getString("EditorMenuFileExit");
            EDITOR_MENU_OTHER = bundle.getString("EditorMenuOther");
            EDITOR_MENU_HELP = bundle.getString("EditorMenuHelp");

            SIMPLE_DIALOG_BUTTON_OK = bundle.getString("SimpleDialogButtonOK");
            SIMPLE_DIALOG_BUTTON_CLOSE = bundle.getString("SimpleDialogButtonClose");
            SIMPLE_DIALOG_BUTTON_CANCEL = bundle.getString("SimpleDialogButtonCancel");

            BOUNDING_VOLUME_MODEL_PROPERTY_CONTROL_NAME = bundle
                    .getString("BoundingVolumeModelPropertyControlName");
            BOUNDING_VOLUME_MODEL_PROPERTY_CONTROL_BOX = bundle
                    .getString("BoundingVolumeModelPropertyControlBox");
            BOUNDING_VOLUME_MODEL_PROPERTY_CONTROL_SPHERE = bundle
                    .getString("BoundingVolumeModelPropertyControlSphere");
            BOUNDING_VOLUME_MODEL_PROPERTY_CONTROL_SPHERE_RADIUS = bundle
                    .getString("BoundingVolumeModelPropertyControlSphereRadius");

            MODEL_PROPERTY_MATERIAL = bundle.getString("ModelPropertyMaterial");
            MODEL_PROPERTY_CULL_HINT = bundle.getString("ModelPropertyCullHint");
            MODEL_PROPERTY_SHADOW_MODE = bundle.getString("ModelPropertyShadowMode");
            MODEL_PROPERTY_LOCATION = bundle.getString("ModelPropertyLocation");
            MODEL_PROPERTY_QUEUE_BUCKET = bundle.getString("ModelPropertyQueueBucket");
            MODEL_PROPERTY_ROTATION = bundle.getString("ModelPropertyRotation");
            MODEL_PROPERTY_SCALE = bundle.getString("ModelPropertyScale");
            MODEL_PROPERTY_TRANSFORMATION = bundle.getString("ModelPropertyTransformation");

            FILE_EDITOR_ACTION_SAVE = bundle.getString("FileEditorActionSave");

            MATERIAL_MODEL_PROPERTY_CONTROL_NO_MATERIAL = bundle.getString("MaterialModelPropertyControlNoMaterial");

            ASSET_COMPONENT_RESOURCE_TREE_CONTEXT_MENU_OPEN_FILE = bundle
                    .getString("AssetComponentResourceTreeContextMenuOpenFile");

            ABSTRACT_ELEMENT_PROPERTY_CONTROL_NO_ELEMENT = bundle.getString("AbstractElementPropertyControlNoElement");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
