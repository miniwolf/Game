package mini.editor.util;

import com.ss.rlib.common.util.array.ArrayFactory;
import mini.scene.Node;
import mini.scene.Spatial;

import java.util.function.Predicate;
import java.util.stream.Stream;

public class NodeUtils {
    /**
     * Force update world bound of the spatial and its children
     */
    public static void updateWorldBounds(Spatial spatial) {
        children(spatial).forEach(sp -> sp.forceRefresh(true, true, false));
        children(spatial).forEach(Spatial::getWorldBound);
    }

    /**
     * Create stream of the spatial's children including itself.
     */
    private static Stream<Spatial> children(Spatial spatial) {
        var result = ArrayFactory.<Spatial>newArray(Spatial.class);

        visitSpatial(spatial, sp -> {
            result.add(sp);
            return true;
        });

        return result.stream();
    }

    private static void visitSpatial(Spatial spatial, Predicate<Spatial> handler) {
        if (!handler.test(spatial)) {
            return;
        } else if (!(spatial instanceof Node)) {
            return;
        }

        var node = (Node) spatial;
        for (var child: node.getChildren()) {
            visitSpatial(child, handler);
        }
    }
}
