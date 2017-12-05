package mini.post;

import mini.material.Material;

/**
 * Filters are 2D effects applied to the rendered scene. The filter is fed with the rendered scene
 * image rendered in an offscreen frame buffer. This texture is applied on a full-screen quad with a
 * special material. This material uses a shader that applies the desired effect to the scene
 * texture.
 * <p>
 * This class is abstract, any <code>Filter</code> must extend it. Any filter holds a
 * <code>FrameBuffer</code> and a <code>Texture</code>.
 */
public class Filter {
    protected Material material;
    private String name;
    private boolean enabled;

    public Filter(String name) {
        this.name = name;
    }

    /**
     * Enable or disable this filter
     *
     * @param enabled true to enable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
