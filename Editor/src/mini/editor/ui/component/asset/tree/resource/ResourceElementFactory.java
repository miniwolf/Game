package mini.editor.ui.component.asset.tree.resource;

import java.nio.file.Files;
import java.nio.file.Path;

public class ResourceElementFactory {

    public static ResourceElement createFor(final Path file) {
        if (Files.isDirectory(file)) {
            return new FolderResourceElement(file);
        } /*else if (JavaFXImageManager.isImage(file)) {

        }*/ else {
            return new FileResourceElement(file);
        }
    }
}
