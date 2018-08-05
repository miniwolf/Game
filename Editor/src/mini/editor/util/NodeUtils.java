package mini.editor.util;

import com.ss.rlib.common.util.StringUtils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import mini.editor.annotation.FromAnyThread;
import mini.light.Light;
import mini.scene.Geometry;
import mini.scene.Node;
import mini.scene.Spatial;

import java.util.function.Consumer;
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

    public static void visitGeometry(Spatial spatial, Consumer<Geometry> consumer) {
        spatial.depthFirstTraversal(sp -> {
            if (spatial instanceof Geometry) {
                consumer.accept((Geometry) sp);
            }
        }, Spatial.DFSMode.PRE_ORDER);
    }

    /**
     * Collect all lights from the model.
     */
    @FromAnyThread
    public static void addLight(Spatial spatial, Array<Light> lights) {
        var lightList = spatial.getLocalLightList();
        lightList.forEach(lights::add);

        if (!(spatial instanceof Node)) {
            return;
        }

        var node = (Node) spatial;

        for (var child : node.getChildren()) {
            addLight(child, lights);
        }
    }

    /**
     * Collect all geometries from the model.
     */
    @FromAnyThread
    public static void addGeometry(Spatial spatial, Array<Geometry> geometries) {
        if (spatial instanceof Geometry) {
            geometries.add((Geometry) spatial);
            return;
        } else if (!(spatial instanceof Node)) {
            return;
        }

        var node = (Node) spatial;

        for (var child : node.getChildren()) {
            addGeometry(child, geometries);
        }
    }

    public static void addGeometryWithMaterial(
            Spatial spatial,
            Array<Geometry> geometries,
            String assetPath) {
        if (assetPath == null || assetPath.isEmpty()) {
            return;
        }

        if (spatial instanceof Geometry) {
            var geometry = (Geometry) spatial;
            var material = geometry.getMaterial();
            var assetName = material == null ? null : material.getAssetName();

            if (StringUtils.equals(assetName, assetPath)) {
                geometries.add(geometry);
            }

            return;
        }

        if (!(spatial instanceof Node)) {
            return;
        }

        var node = (Node) spatial;

        for (Spatial child : node.getChildren()) {
            addGeometryWithMaterial(child, geometries, assetPath);
        }
    }
}
