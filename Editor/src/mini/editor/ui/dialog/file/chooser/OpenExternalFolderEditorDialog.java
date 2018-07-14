package mini.editor.ui.dialog.file.chooser;

import javafx.scene.layout.VBox;
import mini.editor.annotation.FxThread;

import java.nio.file.Path;
import java.util.function.Consumer;

public class OpenExternalFolderEditorDialog extends ExternalFileEditorDialog {
    public OpenExternalFolderEditorDialog(final Consumer<Path> consumer) {
        super(consumer);
    }

    @Override
    @FxThread
    protected void createContent(final VBox root) {
        super.createContent(root);
        getResourceTree().setOnlyFolders(true);
    }
}
