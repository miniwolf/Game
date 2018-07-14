package mini.editor;

import com.ss.rlib.common.util.PropertyLoader;

import java.util.ResourceBundle;

public class Messages {
    public static final String BUNDLE_NAME = "messages/messages";

    public static final String EDITOR_TOOL_ASSET;
    public static final String LOG_VIEW_TITLE;
    public static final String SCENE_FILE_EDITOR_NAME;
    public static final String MODEL_FILE_EDITOR_NAME;
    public static final String EDITOR_AREA_SAVE_FILE_QUESTION;

    public static final String EDITOR_MENU_FILE;
    public static final String EDITOR_MENU_FILE_OPEN_ASSET;
    public static final String EDITOR_MENU_FILE_OPEN_ASSET_DIRECTORY_CHOOSER;
    public static final String EDITOR_MENU_FILE_EXIT;
    public static final String EDITOR_MENU_OTHER;
    public static final String EDITOR_MENU_HELP;

    public static final String SIMPLE_DIALOG_BUTTON_OK;
    public static final String SIMPLE_DIALOG_BUTTON_CLOSE;
    public static final String SIMPLE_DIALOG_BUTTON_CANCEL;
    public static final String ASSET_COMPONENT_RESOURCE_TREE_CONTEXT_MENU_OPEN_FILE;
    public static final String MODEL_NODE_TREE_ACTION_REMOVE;

    static {
        try {
            final ResourceBundle bundle = ResourceBundle
                    .getBundle(BUNDLE_NAME, PropertyLoader.getInstance());

            EDITOR_TOOL_ASSET = bundle.getString("EditorToolAsset");
            LOG_VIEW_TITLE = bundle.getString("LogViewTitle");

            EDITOR_AREA_SAVE_FILE_QUESTION = bundle.getString("EditorAreaSaveFileQuestion");

            SCENE_FILE_EDITOR_NAME = bundle.getString("SceneFileEditorName");

            MODEL_FILE_EDITOR_NAME = bundle.getString("ModelFileEditorName");
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

            ASSET_COMPONENT_RESOURCE_TREE_CONTEXT_MENU_OPEN_FILE = bundle
                    .getString("AssetComponentResourceTreeContextMenuOpenFile");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
