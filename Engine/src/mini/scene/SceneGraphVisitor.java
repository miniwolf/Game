package mini.scene;

/**
 * <code>SceneGraphVisitor</code> is used to traverse the scene graph tree.
 * <p>
 * Use by calling {@link Spatial#depthFirstTraversal(SceneGraphVisitor, Spatial.DFSMode)}.
 */
public interface SceneGraphVisitor {
    /**
     * Called when a sptial is visited in the scene graph.
     *
     * @param spatial The visited spatial.
     */
    void visit(Spatial spatial);
}
