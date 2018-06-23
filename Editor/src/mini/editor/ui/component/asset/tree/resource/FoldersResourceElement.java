package mini.editor.ui.component.asset.tree.resource;

import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayCollectors;

import java.nio.file.Path;

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

//    @Override
//    public String toString() {
//        return "FoldersResourceElement";
//    }
}
