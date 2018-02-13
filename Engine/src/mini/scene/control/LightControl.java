package mini.scene.control;

import mini.light.Light;
import mini.light.PointLight;
import mini.renderer.RenderManager;
import mini.renderer.ViewPort;
import mini.scene.AbstractControl;

/**
 * This
 */
public class LightControl extends AbstractControl {
    private Light light;

    public LightControl(Light light) {
        this.light = light;
    }

    @Override
    protected void controlRender(RenderManager renderManager, ViewPort vp) {
        // Nothing to do.
    }

    @Override
    protected void controlUpdate(float tpf) {
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
}
