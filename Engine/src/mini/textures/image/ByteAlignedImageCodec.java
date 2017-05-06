package mini.textures.image;

import java.nio.ByteBuffer;

/**
 * Created by miniwolf on 26-04-2017.
 */
public class ByteAlignedImageCodec extends ImageCodec {
    private final int ap, az, rp, rz, gp, gz, bp, bz;

    public ByteAlignedImageCodec(int bpp, int flags, int az, int rz, int gz, int bz, int ap, int rp, int gp, int bp) {
        // Cast to long to compute max vals, since some components could be as high as 32 bits.
        super(bpp, flags,
              (int)(((long)1 << (az << 3)) - 1),
              (int)(((long)1 << (rz << 3)) - 1),
              (int)(((long)1 << (gz << 3)) - 1),
              (int)(((long)1 << (bz << 3)) - 1));

        this.ap = ap;
        this.az = az;
        this.rp = rp;
        this.rz = rz;

        this.gp = gp;
        this.gz = gz;
        this.bp = bp;
        this.bz = bz;
    }

    private static void readPixelRaw(ByteBuffer buf, int idx, int bpp, byte[] result) {
        buf.position(idx);
        buf.get(result, 0, bpp);
    }

    private static void writePixelRaw(ByteBuffer buf, int idx, byte[] pixel, int bpp) {
        buf.position(idx);
        buf.put(pixel, 0, bpp);
    }

    private static int readComponent(byte[] encoded, int position, int size) {
        try {
            int component = 0;
            for (int i = size - 1; i >= 0; i--) {
                component = (component << 8) | (encoded[position + i] & 0xff);
            }
            return component;
        } catch (ArrayIndexOutOfBoundsException ex){
            ex.printStackTrace();
            return 0;
        }
    }

    private void writeComponent(int component, int position, int size, byte[] result) {
        for (int i = 0; i < size; i++) {
            byte bt = (byte) ((component >> (i * 8)) & 0xff);
            result[position++] = bt;
        }
    }

    public void readComponents(ByteBuffer buf, int x, int y, int width, int offset, int[] components, byte[] tmp) {
        readPixelRaw(buf, (x + y * width + offset) * bpp + offset, bpp, tmp);
        components[0] = readComponent(tmp, ap, az);
        components[1] = readComponent(tmp, rp, rz);
        components[2] = readComponent(tmp, gp, gz);
        components[3] = readComponent(tmp, bp, bz);
    }

    public void writeComponents(ByteBuffer buf, int x, int y, int width, int offset, int[] components, byte[] tmp) {
        writeComponent(components[0], ap, az, tmp);
        writeComponent(components[1], rp, rz, tmp);
        writeComponent(components[2], gp, gz, tmp);
        writeComponent(components[3], bp, bz, tmp);
        writePixelRaw(buf, (x + y * width) * bpp + offset, tmp, bpp);
    }
}
