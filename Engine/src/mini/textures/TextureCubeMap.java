package mini.textures;

import mini.textures.image.ColorSpace;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Describes a cubemap texture.
 * The image specified by setImage must contain 6 data units,
 * each data contains a 2D image representing a cube's face.
 * The slices are specified in this order:<br/>
 * <br/>
 * 0 => Positive X (+x)<br/>
 * 1 => Negative X (-x)<br/>
 * 2 => Positive Y (+y)<br/>
 * 3 => Negative Y (-y)<br/>
 * 4 => Positive Z (+z)<br/>
 * 5 => Negative Z (-z)<br/>
 *
 * @author Joshua Slack
 */
public class TextureCubeMap extends Texture {

    private WrapMode wrapS = WrapMode.EdgeClamp;
    private WrapMode wrapT = WrapMode.EdgeClamp;
    private WrapMode wrapR = WrapMode.EdgeClamp;

    /**
     * Face of the Cubemap as described by its directional offset from the
     * origin.
     */
    public enum Face {

        PositiveX, NegativeX, PositiveY, NegativeY, PositiveZ, NegativeZ;
    }

    public TextureCubeMap() {
        super();
    }

    public TextureCubeMap(Image img) {
        super();
        setImage(img);
    }

    public TextureCubeMap(int width, int height, Image.Format format) {
        this(createEmptyLayeredImage(width, height, 6, format));
    }

    private static Image createEmptyLayeredImage(int width, int height,
                                                 int layerCount, Image.Format format) {
        ArrayList<ByteBuffer> layers = new ArrayList<>();
        for (int i = 0; i < layerCount; i++) {
            layers.add(null);
        }
        return new Image(format, width, height, 0, layers, ColorSpace.Linear);
    }

    /**
     * <code>setWrap</code> sets the wrap mode of this texture for a
     * particular axis.
     *
     * @param axis the texture axis to define a wrapmode on.
     * @param mode the wrap mode for the given axis of the texture.
     * @throws IllegalArgumentException if axis or mode are null
     */
    public void setWrap(WrapAxis axis, WrapMode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("mode can not be null.");
        } else if (axis == null) {
            throw new IllegalArgumentException("axis can not be null.");
        }
        switch (axis) {
            case S:
                this.wrapS = mode;
                break;
            case T:
                this.wrapT = mode;
                break;
            case R:
                this.wrapR = mode;
                break;
        }
    }

    /**
     * <code>setWrap</code> sets the wrap mode of this texture for all axis.
     *
     * @param mode the wrap mode for the given axis of the texture.
     * @throws IllegalArgumentException if mode is null
     */
    public void setWrap(WrapMode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("mode can not be null.");
        }
        this.wrapS = mode;
        this.wrapT = mode;
        this.wrapR = mode;
    }

    /**
     * <code>getWrap</code> returns the wrap mode for a given coordinate axis
     * on this texture.
     *
     * @param axis the axis to return for
     * @return the wrap mode of the texture.
     * @throws IllegalArgumentException if axis is null
     */
    public WrapMode getWrap(WrapAxis axis) {
        switch (axis) {
            case S:
                return wrapS;
            case T:
                return wrapT;
            case R:
                return wrapR;
        }
        throw new IllegalArgumentException("invalid WrapAxis: " + axis);
    }

    @Override
    public Type getType() {
        return Type.CubeMap;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof TextureCubeMap)) {
            return false;
        }
        TextureCubeMap that = (TextureCubeMap) other;
        if (this.getWrap(WrapAxis.S) != that.getWrap(WrapAxis.S)) {
            return false;
        }
        if (this.getWrap(WrapAxis.T) != that.getWrap(WrapAxis.T)) {
            return false;
        }
        if (this.getWrap(WrapAxis.R) != that.getWrap(WrapAxis.R)) {
            return false;
        }
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 53 * hash + (this.wrapS != null ? this.wrapS.hashCode() : 0);
        hash = 53 * hash + (this.wrapT != null ? this.wrapT.hashCode() : 0);
        hash = 53 * hash + (this.wrapR != null ? this.wrapR.hashCode() : 0);
        return hash;
    }
}
