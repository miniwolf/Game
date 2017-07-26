package mini.light;

import mini.scene.Spatial;
import mini.utils.Camera;
import mini.utils.TempVars;

/**
 * Represents a point light.
 * A point light emits light from a given position into all directions in space.
 * E.g a lamp or a bright effect. Point light positions are in world space.
 * <p>
 * In addition to a position, point lights also have a radius which
 * can be used to attenuate the influence of the light depending on the
 * distance between the light and the effected object.
 *
 */
public class PointLight extends Light {
    private float radius = 0;
    private float invRadius = 0;

    /**
     * Set the radius of the light influence.
     * <p>
     * Setting a non-zero radius indicates the light should use attenuation.
     * If a pixel's distance to this light's position
     * is greater than the light's radius, then the pixel will not be
     * effected by this light, if the distance is less than the radius, then
     * the magnitude of the influence is equal to distance / radius.
     *
     * @param radius the radius of the light influence.
     *
     * @throws IllegalArgumentException If radius is negative
     */
    public final void setRadius(float radius) {
        if (radius < 0) {
            throw new IllegalArgumentException("Light radius cannot be negative");
        }
        this.radius = radius;
        if (radius != 0f) {
            this.invRadius = 1f / radius;
        } else {
            this.invRadius = 0;
        }
    }

    @Override
    public Light.Type getType() {
        return Light.Type.Point;
    }

//    @Override
//    public void computeLastDistance(Spatial owner) {
//        if (owner.getWorldBound() != null) {
//            BoundingVolume bv = owner.getWorldBound();
//            lastDistance = bv.distanceSquaredTo(position);
//        } else {
//            lastDistance = owner.getWorldTranslation().distanceSquared(position);
//        }
//    }
//
//    @Override
//    public boolean intersectsFrustum(Camera camera, TempVars vars) {
//        if (this.radius == 0) {
//            return true;
//        } else {
//            for (int i = 5; i >= 0; i--) {
//                if (camera.getWorldPlane(i).pseudoDistance(position) <= -radius) {
//                    return false;
//                }
//            }
//            return true;
//        }
//    }
}
