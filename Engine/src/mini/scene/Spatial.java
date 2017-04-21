package mini.scene;

/**
 * Created by miniwolf on 15-04-2017.
 */
public class Spatial {
    /**
     * This spatial's name.
     */
    protected String name;
    /**
     * Spatial's parent, or null if it has none.
     */
    protected transient Entity parent;

    /**
     * Constructor instantiates a new <code>Spatial</code> object setting the
     * rotation, translation and scale value to defaults.
     *
     * @param name
     *            the name of the scene element. This is required for
     *            identification and comparison purposes.
     */
    protected Spatial(String name) {
        this.name = name;
//        localTransform = new Transform();
//        worldTransform = new Transform();
//
//        localLights = new LightList(this);
//        worldLights = new LightList(this);
//
//        localOverrides = new SafeArrayList<>(MatParamOverride.class);
//        worldOverrides = new SafeArrayList<>(MatParamOverride.class);
//        refreshFlags |= RF_BOUND;
    }

    /**
     * <code>getParent</code> retrieves this node's parent. If the parent is
     * null this is the root node.
     *
     * @return the parent of this node.
     */
    public Entity getParent() {
        return parent;
    }

    /**
     * Called by {@link Entity#attachChild(Spatial)} and
     * {@link Entity#detachChild(Spatial)} - don't call directly.
     * <code>setParent</code> sets the parent of this node.
     *
     * @param parent
     *            the parent of this node.
     */
    protected void setParent(Entity parent) {
        this.parent = parent;
    }
}
