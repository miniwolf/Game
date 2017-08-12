package mini.light;

import mini.math.Vector3f;
import mini.renderEngine.Camera;
import mini.scene.Node;
import mini.scene.Spatial;
import mini.textures.TextureCubeMap;
import mini.utils.TempVars;

/**
 * A LightProbe is not exactly a light. It holds environment map information used for Image Based Lighting.
 * This is used for indirect lighting in the Physically Based Rendering pipeline.
 *
 * A light probe has a position in world space. This is the position from where the Environment Map are rendered.
 * There are two environment maps held by the LightProbe :
 * - The irradiance map (used for indirect diffuse lighting in the PBR pipeline).
 * - The prefiltered environment map (used for indirect specular lighting and reflection in the PBE pipeline).
 * Note that when instanciating the LightProbe, both those maps are null.
 * To render them see {@link LightProbeFactory#makeProbe(mini.environment.EnvironmentCamera, mini.scene.Node)}
 * and {@link EnvironmentCamera}.
 *
 * The light probe has an area of effect that is a bounding volume centered on its position. (for now only Bounding spheres are supported).
 *
 * A LightProbe will only be taken into account when it's marked as ready.
 * A light probe is ready when it has valid environment map data set.
 * Note that you should never call setReady yourself.
 *
 * @see LightProbeFactory
 * @see EnvironmentCamera
 */
public class LightProbe extends Light {

    private TextureCubeMap irradianceMap;
    private TextureCubeMap prefilteredEnvMap;
    private boolean ready = false;
    private Vector3f position = new Vector3f();
    private Node debugNode;

    /**
     * Empty constructor used for serialization.
     * You should never call it,
     * use {@link LightProbeFactory#makeProbe(mini.environment.EnvironmentCamera, mini.scene.Node)} instead
     */
    public LightProbe() {
    }

    /**
     * returns the irradiance map texture of this Light probe.
     * Note that this Texture may not have image data yet if the LightProbe is not ready
     * @return the irradiance map
     */
    public TextureCubeMap getIrradianceMap() {
        return irradianceMap;
    }

    /**
     * Sets the irradiance map
     * @param irradianceMap the irradiance map
     */
    public void setIrradianceMap(TextureCubeMap irradianceMap) {
        this.irradianceMap = irradianceMap;
    }

    /**
     * returns the prefiltered environment map texture of this light probe
     * Note that this Texture may not have image data yet if the LightProbe is not ready
     * @return the prefiltered environment map
     */
    public TextureCubeMap getPrefilteredEnvMap() {
        return prefilteredEnvMap;
    }

    /**
     * Sets the prefiltered environment map
     * @param prefileteredEnvMap the prefiltered environment map
     */
    public void setPrefilteredMap(TextureCubeMap prefileteredEnvMap) {
        this.prefilteredEnvMap = prefileteredEnvMap;
    }


    /**
     * return true if the LightProbe is ready, meaning the Environment maps have
     * been loaded or rnedered and are ready to be used by a material
     * @return the LightProbe ready state
     */
    public boolean isReady() {
        return ready;
    }

    /**
     * Don't call this method directly.
     * It's meant to be called by additional systems that will load or render
     * the Environment maps of the LightProbe
     * @param ready the ready state of the LightProbe.
     */
    public void setReady(boolean ready) {
        this.ready = ready;
    }

    /**
     * Returns the position of the LightProbe in world space
     * @return the wolrd space position
     */
    public Vector3f getPosition() {
        return position;
    }

    /**
     * Sets the position of the LightProbe in world space
     * @param position the world space position
     */
    public void setPosition(Vector3f position) {
        this.position.set(position);
    }

    @Override
    public boolean intersectsFrustum(Camera camera, TempVars vars) {
        return true;
    }

    @Override
    protected void computeLastDistance(Spatial owner) {
//        if (owner.getWorldBound() != null) {
//            BoundingVolume bv = owner.getWorldBound();
//            lastDistance = bv.distanceSquaredTo(position);
//        } else {
            lastDistance = owner.getWorldTranslation().distanceSquared(position);
//        }
    }

    @Override
    public Type getType() {
        return Type.Probe;
    }

    @Override
    public String toString() {
        return "Light Probe : " + name + " at " + position;
    }
}
