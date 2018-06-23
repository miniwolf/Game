package mini.editor.ui.dialog.file.chooser;

import javafx.scene.layout.VBox;

import java.nio.file.Path;
import java.util.function.Consumer;

public class OpenExternalFolderEditorDialog extends ExternalFileEditorDialog {
    public OpenExternalFolderEditorDialog(final Consumer<Path> consumer) {
        super(consumer);
    }

    @Override
    protected void createContent(final VBox root) {
        super.createContent(root);

        getResourceTree().setOnlyFolders(true);
    }
}
