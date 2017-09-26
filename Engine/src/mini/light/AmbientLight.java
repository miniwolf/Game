package mini.light;

import mini.math.ColorRGBA;
import mini.renderer.Camera;
import mini.scene.Spatial;
import mini.utils.TempVars;

/**
 * An ambient light adds a constant color to the scene.
 * <p>
 * Ambient lights are unaffected by the surface normal, and are constant
 * regardless of the model's location. The material's ambient color is
 * multiplied by the ambient light color to get the final ambient color of
 * an object.
 */
public class AmbientLight extends Light {
    public AmbientLight() {
    }

    public AmbientLight(ColorRGBA color) {
        super(color);
    }

    public boolean intersectsFrustum(Camera camera, TempVars vars) {
        return true;
    }

    public void computeLastDistance(Spatial owner) {
        // ambient lights must always be before directional lights.
        lastDistance = -2;
    }

    @Override
    public Type getType() {
        return Type.Ambient;
    }
}
