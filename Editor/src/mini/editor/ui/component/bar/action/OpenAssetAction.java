package mini.editor.ui.component.bar.action;

import javafx.scene.control.MenuItem;
import mini.editor.Messages;
import mini.editor.annotation.FxThread;
import mini.editor.config.EditorConfig;
import mini.editor.ui.dialog.file.chooser.OpenExternalFolderEditorDialog;
import mini.editor.ui.event.FXEventManager;
import mini.editor.ui.event.impl.ChangedCurrentAssetFolderEvent;

import java.nio.file.Path;
import java.nio.file.Paths;

public class OpenAssetAction extends MenuItem {
    private static final FXEventManager FX_EVENT_MANAGER = FXEventManager.getInstance();

    public OpenAssetAction() {
        super(Messages.EDITOR_MENU_FILE_OPEN_ASSET);
        setOnAction(event -> process());
    }

    /**
     * Selecting an asset folder
     */
    @FxThread
    private void process() {
        final EditorConfig config = EditorConfig.getInstance();

        // TODO: Native opening asset
        openAsset();
    }

    /**
     * Open an asset folder using custom file chooser.
     */
    private void openAsset() {
        final OpenExternalFolderEditorDialog dialog = new OpenExternalFolderEditorDialog(
                this::openAssetFolder);
        dialog.setTitleText(Messages.EDITOR_MENU_FILE_OPEN_ASSET_DIRECTORY_CHOOSER);

        final EditorConfig config = EditorConfig.getInstance();
        final Path currentAsset = config.getCurrentAsset();

        if (currentAsset == null) {
            dialog.setInitDirectory(Paths.get(System.getProperty("user.home")));
        } else {
            dialog.setInitDirectory(currentAsset);
        }

        dialog.show();
    }

    /**
     * Open the asset folder
     */
    public void openAssetFolder(final Path newAsset) {
        final EditorConfig config = EditorConfig.getInstance();
        final Path currentAsset = config.getCurrentAsset();
        if (newAsset.equals(currentAsset)) {
            return;
        }

        config.addOpenedAsset(newAsset);
        config.setCurrentAsset(newAsset);
        config.save();

        FX_EVENT_MANAGER.notify(new ChangedCurrentAssetFolderEvent(newAsset));
    }
}
