package mini.ui;

import mini.asset.AssetManager;
import mini.material.Material;
import mini.material.RenderState;
import mini.math.ColorRGBA;
import mini.math.Vector3f;
import mini.renderer.queue.RenderQueue;
import mini.scene.Geometry;
import mini.scene.shape.Quad;
import mini.textures.Texture2D;

/**
 * A <code>Picture</code> represents a 2D image drawn on the screen. It can be used to represent
 * sprites or other background elements.
 */
public class Picture extends Geometry {
    private float width;
    private float height;

    /**
     * Create a named picture.
     * <p>
     * By default a picture's width and height are 1 and its position is 0, 0.
     * The image texture coordinates will not be flipped.
     *
     * @param name the name of the picture in the scene graph.
     */
    public Picture(String name) {
        this(name, false);
    }

    /**
     * Create a named picture.
     * <p>
     * By default a picture's width and height are 1 and its position is 0, 0.
     *
     * @param name  the name of the picture in the scene graph.
     * @param flipY Whether to flip the Y coordinates of the texture
     */
    public Picture(String name, boolean flipY) {
        super(name, new Quad(1, 1, flipY));
        setQueueBucket(RenderQueue.Bucket.Gui);
        setCullHint(CullHint.Never);
    }

    /**
     * Set the texture to put on the picture.
     *
     * @param assetManager The {@link AssetManager} to use to load the material.
     * @param texture      The texture
     * @param useAlpha     If true, the picture will appear transparent and allow objects behind it to
     *                     appear through. If false, the transparent portions will be the immage's color
     *                     at that pixel.
     */
    public void setTexture(AssetManager assetManager, Texture2D texture, boolean useAlpha) {
        if (getMaterial() == null) {
            Material material = new Material(assetManager, "MatDefs/Gui/Gui.minid");
            material.setColor("Color", ColorRGBA.White);
            setMaterial(material);
        }
        material.getAdditionalRenderState().setBlendMode(useAlpha
                                                         ? RenderState.BlendMode.Alpha
                                                         : RenderState.BlendMode.Off);
        material.setTexture("Texture", texture);
    }

    /**
     * Set the position of the picture in pixels. The origin (0, 0) is at the bottom-left of the
     * screen.
     *
     * @param x The x coordinate
     * @param y The x coordinate
     */
    public void setPosition(float x, float y) {
        float z = getLocalTranslation().getZ();
        setLocalTranslation(x, y, z);
    }

    /**
     * Set the width in pixels of the picture, if the width does not match the texture's width, then
     * the the texture will be scaled to fit the picture.
     *
     * @param width the width to set
     */
    public void setWidth(float width) {
        this.width = width;
        setLocalScale(new Vector3f(width, height, 1f));
    }

    /**
     * Set the height in pixels of the picture, if the height does not match the texture's height,
     * then the the texture will be scaled to fit the picture.
     *
     * @param width the width to set
     */
    public void setHeight(float height) {
        this.height = height;
        setLocalScale(new Vector3f(width, height, 1f));
    }
}
