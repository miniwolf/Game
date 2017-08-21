package mini.utils;

import mini.math.ColorRGBA;
import mini.math.FastMath;
import mini.textures.Image;
import mini.textures.image.ImageRaster;

import java.nio.ByteBuffer;

/**
 * Created by miniwolf on 25-04-2017.
 */
public class MipMapGenerator {
    public static Image scaleImage(Image inputImage, int outputWidth, int outputHeight) {
        int size = outputWidth * outputHeight * inputImage.getFormat().getBitsPerPixel() / 8;
        ByteBuffer buffer = BufferUtils.createByteBuffer(size);
        Image outputImage = new Image(inputImage.getFormat(),
                                      outputWidth,
                                      outputHeight,
                                      buffer,
                                      inputImage.getColorSpace());

        ImageRaster input = ImageRaster.create(inputImage, 0, 0, false);
        ImageRaster output = ImageRaster.create(outputImage, 0, 0, false);

        float xRatio = ((float)(input.getWidth()  - 1)) / output.getWidth();
        float yRatio = ((float)(input.getHeight() - 1)) / output.getHeight();

        ColorRGBA outputColor = new ColorRGBA();
        ColorRGBA bottomLeft = new ColorRGBA();
        ColorRGBA bottomRight = new ColorRGBA();
        ColorRGBA topLeft = new ColorRGBA();
        ColorRGBA topRight = new ColorRGBA();

        for (int y = 0; y < outputHeight; y++) {
            for (int x = 0; x < outputWidth; x++) {
                float x2f = x * xRatio;
                float y2f = y * yRatio;

                int x2 = (int)x2f;
                int y2 = (int)y2f;

                float xDiff = x2f - x2;
                float yDiff = y2f - y2;

                input.getPixel(x2,     y2,     bottomLeft);
                input.getPixel(x2 + 1, y2,     bottomRight);
                input.getPixel(x2,     y2 + 1, topLeft);
                input.getPixel(x2 + 1, y2 + 1, topRight);

                bottomLeft.multLocal( (1f - xDiff) * (1f - yDiff) );
                bottomRight.multLocal((xDiff)      * (1f - yDiff) );
                topLeft.multLocal(    (1f - xDiff) * (yDiff) );
                topRight.multLocal(   (xDiff)      * (yDiff) );

                outputColor.set(bottomLeft).addLocal(bottomRight)
                           .addLocal(topLeft).addLocal(topRight);

                output.setPixel(x, y, outputColor);
            }
        }
        return outputImage;
    }

    public static Image resizeToPowerOf2(Image original){
        int potWidth = FastMath.nearestPowerOfTwo(original.getWidth());
        int potHeight = FastMath.nearestPowerOfTwo(original.getHeight());
        return scaleImage(original, potWidth, potHeight);
    }
}
