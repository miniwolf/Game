package mini.scene.debug;

import mini.animation.Skeleton;
import mini.renderer.queue.RenderQueue;
import mini.scene.Geometry;
import mini.scene.Node;
import mini.utils.clone.Cloner;

import java.util.Map;

/**
 * The class that creates a mesh to display how bones behave.
 * If it is supplied with the bones' lengths it will show exactly how the bones look like on the scene.
 * If not then only connections between each bone heads will be shown.
 */
public class SkeletonDebugger extends Node {
    /**
     * The lines of the bones or the wires between their heads.
     */
    private SkeletonWire wires;
    /**
     * The heads and tails points of the bones or only heads if no length data is available.
     */
    private SkeletonPoints points;
    /**
     * The dotted lines between a bone's tail and the had of its children. Not available if the length data was not provided.
     */
    private SkeletonInterBoneWire interBoneWires;

    public SkeletonDebugger() {
    }

    /**
     * Creates a debugger with no length data. The wires will be a connection between the bones' heads only.
     * The points will show the bones' heads only and no dotted line of inter bones connection will be visible.
     *
     * @param name     the name of the debugger's node
     * @param skeleton the skeleton that will be shown
     */
    public SkeletonDebugger(String name, Skeleton skeleton) {
        this(name, skeleton, null);
    }

    /**
     * Creates a debugger with bone lengths data. If the data is supplied then the wires will show each full bone (from head to tail),
     * the points will display both heads and tails of the bones and dotted lines between bones will be seen.
     *
     * @param name        the name of the debugger's node
     * @param skeleton    the skeleton that will be shown
     * @param boneLengths a map between the bone's index and the bone's length
     */
    public SkeletonDebugger(String name, Skeleton skeleton, Map<Integer, Float> boneLengths) {
        super(name);

        wires = new SkeletonWire(skeleton, boneLengths);
        points = new SkeletonPoints(skeleton, boneLengths);

        this.attachChild(new Geometry(name + "_wires", wires));
        this.attachChild(new Geometry(name + "_points", points));
        if (boneLengths != null) {
            interBoneWires = new SkeletonInterBoneWire(skeleton, boneLengths);
            this.attachChild(new Geometry(name + "_interwires", interBoneWires));
        }

        this.setQueueBucket(RenderQueue.Bucket.Transparent);
    }

    @Override
    public void updateLogicalState(float tpf) {
        super.updateLogicalState(tpf);
        wires.updateGeometry();
        points.updateGeometry();
        if (interBoneWires != null) {
            interBoneWires.updateGeometry();
        }
    }

    /**
     * @return the skeleton points
     */
    public SkeletonPoints getPoints() {
        return points;
    }

    /**
     * @return the skeleton wires
     */
    public SkeletonWire getWires() {
        return wires;
    }

    /**
     * @return the dotted line between bones (can be null)
     */
    public SkeletonInterBoneWire getInterBoneWires() {
        return interBoneWires;
    }

    @Override
    public void cloneFields(Cloner cloner, Object original) {
        super.cloneFields(cloner, original);

        this.wires = cloner.clone(wires);
        this.points = cloner.clone(points);
        this.interBoneWires = cloner.clone(interBoneWires);
    }
}
