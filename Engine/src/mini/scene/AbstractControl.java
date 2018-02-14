package mini.scene;

import mini.renderer.RenderManager;
import mini.renderer.ViewPort;
import mini.scene.control.Control;
import mini.utils.clone.Cloner;
import mini.utils.clone.MiniCloneable;

public abstract class AbstractControl implements Control, MiniCloneable {
    protected boolean enabled = true;
    protected Spatial spatial;

    @Override
    public void setSpatial(Spatial spatial) {
        if (this.spatial != null && spatial != null && spatial != this.spatial) {
            throw new IllegalStateException("This control has already been attached to a Spatial");
        }
        this.spatial = spatial;
    }

    @Override
    public Spatial getSpatial() {
        return spatial;
    }

    @Override
    public void render(RenderManager renderManager, ViewPort vp) {
        if (!enabled) {
            return;
        }

        controlRender(renderManager, vp);
    }

    @Override
    public void update(float tpf) {
        if (!enabled) {
            return;
        }

        controlUpdate(tpf);
    }

    protected abstract void controlRender(RenderManager renderManager, ViewPort vp);

    protected abstract void controlUpdate(float tpf);

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
