package mini.scene;

import mini.light.PointLight;
import mini.scene.control.LightControl;
import mini.utils.clone.Cloner;

/**
 * <code>LightNode</code> is used to link together a {@link mini.light.Light} object with a
 * {@link Node} object.
 */
public class LightNode extends Node {
    private LightControl control;

    public LightNode(String name, PointLight light) {
        this(name, new LightControl(light));
    }

    public LightNode(String name, LightControl control) {
        super(name);
        this.control = control;
        addControl(control);
    }

    @Override
    public void cloneFields(Cloner cloner, Object original) {
        super.cloneFields(cloner, original);

        control = cloner.clone(control);
    }
}
