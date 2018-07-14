package mini.editor.ui.component.asset.tree.resource;

import com.ss.rlib.common.util.array.Array;
import javafx.scene.control.Tooltip;
import mini.editor.annotation.FromAnyThread;
import mini.editor.annotation.FxThread;

import java.nio.file.Path;
import java.util.Objects;

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

    @Override
    public String toString() {
        return "ResourceElement{" +
               "file=" + file +
               '}';
    }

    @Override
    public int compareTo(ResourceElement o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Path) {
            var path = (Path) o;
            if (path.toString().endsWith("\\.")) {
                path = path.getParent();
            }
            return file.equals(path);
        }
        var element = (ResourceElement) o;
        return Objects.equals(file, element.file);
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }

    /**
     * @return a tooltip to preview this element.
     */
    @FxThread
    public Tooltip createToolTip() {
        return null;
    }
}
