package mini.light;

import mini.math.ColorRGBA;
import mini.math.Vector3f;
import mini.scene.Spatial;
import mini.utils.Camera;
import mini.utils.TempVars;

import java.io.IOException;

/**
 * <code>DirectionalLight</code> is a light coming from a certain direction in world space.
 * E.g sun or moon light.
 * <p>
 * Directional lights have no specific position in the scene, they always
 * come from their direction regardless of where an object is placed.
 */
public class DirectionalLight extends Light {

    protected Vector3f direction = new Vector3f(0f, -1f, 0f);

    /**
     * Creates a DirectionalLight
     */
    public DirectionalLight() {
    }

    /**
     * Creates a DirectionalLight with the given direction
     *
     * @param direction the light's direction
     */
    public DirectionalLight(Vector3f direction) {
        setDirection(direction);
    }

    /**
     * Creates a DirectionalLight with the given direction and the given color
     *
     * @param direction the light's direction
     * @param color     the light's color
     */
    public DirectionalLight(Vector3f direction, ColorRGBA color) {
        super(color);
        setDirection(direction);
    }

    public void computeLastDistance(Spatial owner) {
        // directional lights are after ambient lights
        // but before all other lights.
        lastDistance = -1;
    }

    /**
     * Returns the direction vector of the light.
     *
     * @return The direction vector of the light.
     * @see DirectionalLight#setDirection(com.jme3.math.Vector3f)
     */
    public Vector3f getDirection() {
        return direction;
    }

    /**
     * Sets the direction of the light.
     * <p>
     * Represents the direction the light is shining.
     * (1, 0, 0) would represent light shining in the +X direction.
     *
     * @param dir the direction of the light.
     */
    public final void setDirection(Vector3f dir) {
        direction.set(dir);
        if (!direction.isUnitVector()) {
            direction.normalizeLocal();
        }
    }

    public boolean intersectsFrustum(Camera camera, TempVars vars) {
        return true;
    }

    @Override
    public Type getType() {
        return Type.Directional;
    }

    @Override
    public DirectionalLight clone() {
        DirectionalLight l = (DirectionalLight) super.clone();
        l.direction = direction.clone();
        return l;
    }
}
