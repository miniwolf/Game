package mini.utils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.nio.IntBuffer;

public class Screenshots {
    public static void convertScreenShot(IntBuffer bgraBuf, BufferedImage out) {
        WritableRaster wr = out.getRaster();
        DataBufferInt db = (DataBufferInt) wr.getDataBuffer();

        int[] cpuArray = db.getData();

        bgraBuf.clear();
        bgraBuf.get(cpuArray);
    }
}
