package mini.scene.control;

import mini.light.Light;
import mini.light.PointLight;
import mini.renderer.RenderManager;
import mini.renderer.ViewPort;
import mini.scene.Spatial;
import mini.utils.clone.Cloner;
import mini.utils.clone.MiniCloneable;

/**
 * This
 */
public class LightControl implements Control, MiniCloneable {
    private boolean enabled = true;
    private Spatial spatial;
    private Light light;

    public LightControl(Light light) {
        this.light = light;
    }

    @Override
    public void update(float tpf) {
        if (!enabled) {
            return;
        }

        controlUpdate(tpf);
    }

    @Override
    public void render(RenderManager renderManager, ViewPort vp) {
        if (!enabled) {
            return;
        }

        controlRender(renderManager, vp);
    }

    private void controlRender(RenderManager renderManager, ViewPort vp) {
        // Nothing to do.
    }

    @Override
    public void setSpatial(Spatial spatial) {
        if (this.spatial != null && spatial != null && spatial != this.spatial) {
            throw new IllegalStateException("This control has already been attached to a Spatial");
        }
        this.spatial = spatial;
    }

    private void controlUpdate(float tpf) {
        if (spatial == null || light == null) {
            return;
        }

        spatialToLight(light);
    }

    private void spatialToLight(Light light) {
        if (light instanceof PointLight) {
            ((PointLight) light).setPosition(spatial.getWorldTranslation());
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public Object miniClone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Can't clone control for spatial", e);
        }
    }

    @Override
    public void cloneFields(Cloner cloner, Object original) {
        this.spatial = cloner.clone(spatial);
    }
}
