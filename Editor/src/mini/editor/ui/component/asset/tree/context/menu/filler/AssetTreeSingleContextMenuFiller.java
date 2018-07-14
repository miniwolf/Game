package mini.editor.ui.component.asset.tree.context.menu.filler;

import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import mini.editor.ui.component.asset.tree.resource.ResourceElement;

public interface AssetTreeSingleContextMenuFiller {

    /**
     * Fill the context menu of the resource element.
     *
     * @param element the resource element.
     * @param items   the container of items of a context menu.
     */
    void fill(ResourceElement element, ObservableList<MenuItem> items);
}
