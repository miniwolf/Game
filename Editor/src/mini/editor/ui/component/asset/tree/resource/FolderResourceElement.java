package mini.editor.ui.component.asset.tree.resource;

import com.ss.rlib.common.util.FileUtils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import mini.editor.annotation.FromAnyThread;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FolderResourceElement extends ResourceElement {
    public FolderResourceElement(Path file) {
        super(file);
    }

    @Override
    @FromAnyThread
    public Array<ResourceElement> getChildren(
            final Array<String> extensionsFilter,
            boolean onlyFolders) {
        if (!Files.isDirectory(file)) {
            return null;
        }

        final Array<ResourceElement> elements = ArrayFactory.newArray(ResourceElement.class);

        try (var stream = Files.newDirectoryStream(file)) {
            for (Path child: stream) {
                var fileName = child.getFileName().toString();
                if (fileName.startsWith(".")) {
                    continue;
                } else if (Files.isDirectory(child)) {
                    elements.add(ResourceElementFactory.createFor(child));
                    continue;
                }

                if (onlyFolders) {
                    continue;
                }

                var extension = FileUtils.getExtension(child);

                if (extensionsFilter.isEmpty() || extensionsFilter.contains(extension)) {
                    elements.add(ResourceElementFactory.createFor(child));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return elements;
    }

    @Override
    @FromAnyThread
    public boolean hasChildren(Array<String> extensionsFilter, boolean onlyFolders) {
        if (!Files.isDirectory(file)) {
            return false;
        }

        try (var stream = Files.newDirectoryStream(file)) {
            for (Path path: stream) {
                var fileName = path.getFileName().toString();
                if (fileName.startsWith(".")) {
                    continue;
                } else if (Files.isDirectory(path)) {
                    return true;
                }

                if (onlyFolders) {
                    continue;
                }

                var extension = FileUtils.getExtension(path);
                if (extensionsFilter.isEmpty() || extensionsFilter.contains(extension)) {
                    return true;
                }
            }
        } catch (AccessDeniedException e) {
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String toString() {
        return "FolderResourceElement{" +
               "file=" + file +
               '}';
    }
}
