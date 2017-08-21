package mini.textures.image;

import mini.textures.Image;

import java.nio.ByteBuffer;
import java.util.EnumMap;
import mini.textures.Image.Format;

/**
 * Created by miniwolf on 25-04-2017.
 */
public abstract class ImageCodec {
    public static final int FLAG_F16 = 1, FLAG_F32 = 2, FLAG_GRAY = 4;
    private static final EnumMap<Image.Format, ImageCodec> params =
            new EnumMap<>(Image.Format.class);

    protected final int bpp, type, maxAlpha, maxRed, maxGreen, maxBlue;
    protected final boolean isGray;

    public ImageCodec(int bpp, int flags, int maxAlpha, int maxRed, int maxGreen, int maxBlue) {
        this.bpp = bpp;
        this.isGray = (flags & FLAG_GRAY) != 0;
        this.type = flags & ~FLAG_GRAY;
        this.maxAlpha = maxAlpha;
        this.maxRed = maxRed;
        this.maxGreen = maxGreen;
        this.maxBlue = maxBlue;
    }

    static {
        // == RGB ==
        params.put(Format.BGR8, new ByteOffsetImageCodec(3, 0, -1, 2, 1, 0));

        params.put(Format.RGB8, new ByteOffsetImageCodec(3, 0, -1, 0, 1, 2));

        params.put(Format.RGB16F, new ByteAlignedImageCodec(6, FLAG_F16,
                                                            0, 2, 2, 2,
                                                            0, 0, 2, 4));

        params.put(Format.RGB32F, new ByteAlignedImageCodec(12, FLAG_F32,
                                                            0, 4, 4, 4,
                                                            0, 0, 4, 8));


        // == RGBA ==

        params.put(Format.ABGR8, new ByteOffsetImageCodec(4, 0, 0, 3, 2, 1));

        params.put(Format.ARGB8, new ByteOffsetImageCodec(4, 0, 0, 1, 2, 3));

        params.put(Format.BGRA8, new ByteOffsetImageCodec(4, 0, 3, 2, 1, 0));

        params.put(Format.RGBA8, new ByteOffsetImageCodec(4, 0, 3, 0, 1, 2));

        params.put(Format.RGBA16F, new ByteAlignedImageCodec(8, FLAG_F16,
                                                             2, 2, 2, 2,
                                                             6, 0, 2, 4));

        params.put(Format.RGBA32F, new ByteAlignedImageCodec(16, FLAG_F32,
                                                             4,  4, 4, 4,
                                                             12, 0, 4, 8));
    }


    public abstract void readComponents(ByteBuffer buf, int x, int y, int width, int offset, int[] components, byte[] tmp);

    public abstract void writeComponents(ByteBuffer buf, int x, int y, int width, int offset, int[] components, byte[] tmp);

    /**
     * Looks up the format in the codec registry.
     * The codec will be able to decode the given format.
     *
     * @param format The format to lookup.
     * @return The codec capable of decoding it, or null if not found.
     */
    public static ImageCodec lookup(Image.Format format) {
        ImageCodec codec = params.get(format);
        if (codec == null) {
            throw new UnsupportedOperationException("The format " + format + " is not supported");
        }
        return codec;
    }
}
