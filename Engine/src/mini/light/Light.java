package mini.light;

import mini.math.ColorRGBA;
import mini.renderer.Camera;
import mini.scene.Spatial;
import mini.utils.TempVars;

/**
 * Abstract class for representing a light source.
 * <p>
 * All light source types have a color.
 */
public abstract class Light implements Cloneable {
    /**
     * Describes the light type.
     */
    public enum Type {

        /**
         * Directional light
         *
         * @see DirectionalLight
         */
        Directional(0),

        /**
         * Point light
         *
         * @see PointLight
         */
        Point(1),

        /**
         * Spot light.
         *
         * @see SpotLight
         */
        Spot(2),

        /**
         * Ambient light
         *
         * @see AmbientLight
         */
        Ambient(3),

        /**
         * Light probe
         *
         * @see LightProbe
         */
        Probe(4);

        private int typeId;

        Type(int type) {
            this.typeId = type;
        }

        /**
         * @return an index for the light type
         */
        public int getId() {
            return typeId;
        }
    }

    protected ColorRGBA color = new ColorRGBA(ColorRGBA.White);

    /**
     * Used in LightList for caching the distance
     * to the owner spatial. Should be reset after the sorting.
     */
    protected transient float lastDistance = -1;

    protected boolean enabled = true;

    /**
     * The light name.
     */
    protected String name;

    boolean frustumCheckNeeded = true;
    boolean intersectsFrustum = false;

    protected Light() {
    }

    protected Light(ColorRGBA color) {
        setColor(color);
    }

    /**
     * Sets the light color.
     *
     * @param color the light color.
     */
    public void setColor(ColorRGBA color) {
        this.color.set(color);
    }

    /**
     * @return The color of the light.
     */
    public ColorRGBA getColor() {
        return color;
    }

    /**
     * @return the light name
     */
    public String getName() {
        return name;
    }

    /**
     * @return whether this light is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Used internally to compute the last distance value.
     */
    protected abstract void computeLastDistance(Spatial owner);

    /**
     * @return the light type
     * @see Type
     */
    public abstract Type getType();

    /**
     * Determines if the light intersects with the given camera frustum.
     * <p>
     * For non-local lights, such as {@link DirectionalLight directional lights},
     * {@link AmbientLight ambient lights}, or {@link PointLight point lights}
     * without influence radius, this method should always return true.
     *
     * @param camera The camera frustum to check intersection against.
     * @param vars   TempVars in case it is needed.
     * @return True if the light intersects the frustum, false otherwise.
     */
    public abstract boolean intersectsFrustum(Camera camera, TempVars vars);

    @Override
    public Light clone() {
        try {
            Light l = (Light) super.clone();
            l.color = color.clone();
            return l;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }
}
