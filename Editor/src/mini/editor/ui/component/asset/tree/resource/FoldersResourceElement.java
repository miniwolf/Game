package mini.editor.ui.component.asset.tree.resource;

import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayCollectors;

import java.nio.file.Path;
import java.util.Objects;

public class FoldersResourceElement extends ResourceElement {
    private final Array<Path> folders;

    public FoldersResourceElement(final Array<Path> folders) {
        super(folders.first());
        this.folders = folders;
    }

    @Override
    public Array<ResourceElement> getChildren(Array<String> extensionsFilter,
                                              boolean onlyFolders) {
        return folders.stream().map(ResourceElementFactory::createFor)
                      .collect(ArrayCollectors.toArray(ResourceElement.class));
    }

    @Override
    public boolean hasChildren(Array<String> extensionsFilter, boolean onlyFolders) {
        return !folders.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        FoldersResourceElement that = (FoldersResourceElement) o;
        return Objects.equals(folders, that.folders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), folders);
    }

    @Override
    public String toString() {
        return "FoldersResourceElement";
    }
}
