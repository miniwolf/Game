package mini.editor.ui.component.asset.tree;

import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import mini.editor.ui.component.asset.tree.context.menu.filler.AssetTreeSingleContextMenuFiller;

/**
 * Collect all available context menu fillers
 */
public class AssetTreeContextMenuFillerRegistry {

    private static AssetTreeContextMenuFillerRegistry INSTANCE
            = new AssetTreeContextMenuFillerRegistry();
    private final Array<AssetTreeSingleContextMenuFiller> singleFillers;

    private AssetTreeContextMenuFillerRegistry() {

        singleFillers = ArrayFactory.newArray(AssetTreeSingleContextMenuFiller.class);
    }

    public static AssetTreeContextMenuFillerRegistry getInstance() {
        return INSTANCE;
    }

    public Array<AssetTreeSingleContextMenuFiller> getSingleFillers() {
        return singleFillers;
    }
}
