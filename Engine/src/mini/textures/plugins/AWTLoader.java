package mini.textures.plugins;

import mini.textures.Image;
import mini.textures.image.ColorSpace;
import mini.utils.BufferUtils;
import mini.utils.MyFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by miniwolf on 22-04-2017.
 */
public class AWTLoader {
    private static Object extractImageData(BufferedImage img) {
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

    public static Image load(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        switch (img.getType()) {
            case BufferedImage.TYPE_4BYTE_ABGR: // most common in PNG images w/ alpha
                byte[] dataBuf1 = (byte[]) extractImageData(img);

                ByteBuffer data1 = BufferUtils
                        .createByteBuffer(img.getWidth() * img.getHeight() * 4);
                data1.put(dataBuf1);
                return new Image(Image.Format.ABGR8, width, height, data1, null, ColorSpace.sRGB);
            case BufferedImage.TYPE_3BYTE_BGR: // most common in JPEG images
                byte[] dataBuf2 = (byte[]) extractImageData(img);

                ByteBuffer data2 = BufferUtils
                        .createByteBuffer(img.getWidth() * img.getHeight() * 3);
                data2.put(dataBuf2);
                return new Image(Image.Format.BGR8, width, height, data2, null, ColorSpace.sRGB);
            case BufferedImage.TYPE_BYTE_GRAY: // grayscale fonts
                byte[] dataBuf3 = (byte[]) extractImageData(img);
                ByteBuffer data3 = BufferUtils.createByteBuffer(img.getWidth() * img.getHeight());
                data3.put(dataBuf3);
                return new Image(Image.Format.Luminance8, width, height, data3, null,
                                 ColorSpace.sRGB);
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

    public static Image load(InputStream in) throws IOException {
        ImageIO.setUseCache(false);
        BufferedImage img = ImageIO.read(in);
        if (img == null) {
            return null;
        }
        return load(img);
    }

    public static Object load(MyFile path) {
        if (ImageIO.getImageReadersBySuffix(path.getName().split("\\.")[1]) != null) {
            try {
                Image img = load(path.getInputStream());
                if (img == null) {
                    throw new RuntimeException(
                            "The given image cannot be loaded " + path.getPath());
                }
                return img;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException(
                    "The extension " + path.getName().split(".")[1] + " is not supported");
        }
        return null;
    }
}
