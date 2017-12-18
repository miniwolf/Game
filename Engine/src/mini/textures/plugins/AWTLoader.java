package mini.textures.plugins;

import mini.asset.AssetInfo;
import mini.asset.AssetLoadException;
import mini.asset.AssetLoader;
import mini.asset.TextureKey;
import mini.textures.Image;
import mini.textures.image.ColorSpace;
import mini.utils.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @author miniwolf
 */
public class AWTLoader implements AssetLoader {
    private Object extractImageData(BufferedImage img) {
        DataBuffer buf = img.getRaster().getDataBuffer();
        switch (buf.getDataType()) {
            case DataBuffer.TYPE_BYTE:
                DataBufferByte byteBuf = (DataBufferByte) buf;
                return byteBuf.getData();
            case DataBuffer.TYPE_USHORT:
                DataBufferUShort shortBuf = (DataBufferUShort) buf;
                return shortBuf.getData();
        }
        return null;
    }

    private void flipImage(byte[] img, int width, int height, int bpp) {
        int scSz = (width * bpp) / 8;
        byte[] sln = new byte[scSz];
        int j;
        for (int i = 0; i < height * 0.5; i++) {
            j = height - i - 1;
            System.arraycopy(img, i * scSz, sln, 0, scSz);
            System.arraycopy(img, j * scSz, img, i * scSz, scSz);
            System.arraycopy(sln, 0, img, j * scSz, scSz);
        }
    }

    public Image load(BufferedImage img, boolean flipY) {
        int width = img.getWidth();
        int height = img.getHeight();

        switch (img.getType()) {
            case BufferedImage.TYPE_4BYTE_ABGR: { // most common in PNG images w/ alpha
                return getImage(img, flipY, width, height, 32, Image.Format.ABGR8, 4);
            }
            case BufferedImage.TYPE_3BYTE_BGR: {// most common in JPEG images
                return getImage(img, flipY, width, height, 24, Image.Format.BGR8, 3);
            }
            case BufferedImage.TYPE_BYTE_GRAY: {// grayscale fonts
                return getImage(img, flipY, width, height, 8, Image.Format.Luminance8, 1);
            }
            default:
                break;
        }

        if (img.getTransparency() == Transparency.OPAQUE) {
            ByteBuffer data = BufferUtils.createByteBuffer(img.getWidth() * img.getHeight() * 3);
            // no alpha
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = img.getRGB(x, y);
                    byte r = (byte) ((rgb & 0x00FF0000) >> 16);
                    byte g = (byte) ((rgb & 0x0000FF00) >> 8);
                    byte b = (byte) ((rgb & 0x000000FF));
                    data.put(r).put(g).put(b);
                }
            }
            data.flip();
            return new Image(Image.Format.RGB8, width, height, data, null, ColorSpace.sRGB);
        } else {
            ByteBuffer data = BufferUtils.createByteBuffer(img.getWidth() * img.getHeight() * 4);
            // alpha
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = img.getRGB(x, y);
                    byte a = (byte) ((rgb & 0xFF000000) >> 24);
                    byte r = (byte) ((rgb & 0x00FF0000) >> 16);
                    byte g = (byte) ((rgb & 0x0000FF00) >> 8);
                    byte b = (byte) ((rgb & 0x000000FF));
                    data.put(r).put(g).put(b).put(a);
                }
            }
            data.flip();
            return new Image(Image.Format.RGBA8, width, height, data, null, ColorSpace.sRGB);
        }
    }

    private Image getImage(BufferedImage img, boolean flipY, int width, int height, int bpp,
                           Image.Format format, int bufferMultiplier) {
        byte[] dataBuf = (byte[]) extractImageData(img);
        if (flipY) {
            flipImage(dataBuf, width, height, bpp);
        }

        ByteBuffer data = BufferUtils
                .createByteBuffer(img.getWidth() * img.getHeight() * bufferMultiplier);
        data.put(dataBuf);
        return new Image(format, width, height, data, null, ColorSpace.sRGB);
    }

    public Object load(AssetInfo info) throws IOException {
        if (ImageIO.getImageReadersBySuffix(info.getKey().getExtension()) == null) {
            throw new AssetLoadException("The extension " + info.getKey().getExtension()
                                         + " is not supported");
        }

        try (InputStream in = info.openStream()) {
            boolean flipY = ((TextureKey) info.getKey()).isFlipY();

            Image img = load(in, flipY);
            if (img == null) {
                throw new AssetLoadException("The given image cannot be loaded "
                                             + info.getKey().getName());
            }
            return img;
        }
    }

    private Image load(InputStream in, boolean flipY) throws IOException {
        ImageIO.setUseCache(false);
        BufferedImage img = ImageIO.read(in);
        if (img == null) {
            return null;
        }
        return load(img, flipY);
    }
}
