package mini.editor.ui.component.asset.tree.resource;

import com.ss.rlib.common.util.array.Array;
import mini.editor.annotation.FromAnyThread;

import java.nio.file.Path;

public class ResourceElement implements Comparable<ResourceElement> {

    /**
     * Reference to the file.
     */
    protected final Path file;

    public ResourceElement(Path file) {
        this.file = file;
    }

    /**
     * @return the reference to the file.
     */
    public Path getFile() {
        return file;
    }

    @Override
    public int compareTo(ResourceElement o) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return whether this element has any children.
     */
    public boolean hasChildren(Array<String> extensionsFilter, boolean onlyFolders) {
        return false;
    }

    /**
     * @return list of children resource elements.
     */
    @FromAnyThread
    public Array<ResourceElement> getChildren(final Array<String> extensionsFilter,
                                              boolean onlyFolders) {
        return null;
    }

//    @Override
//    public String toString() {
//        return "ResourceElement{" +
//               "file=" + file +
//               '}';
//    }
}
