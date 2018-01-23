package mini.textures.plugins;

import mini.asset.AssetInfo;
import mini.asset.AssetLoader;
import mini.asset.TextureKey;
import mini.textures.Image;
import mini.utils.BufferUtils;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class TGALoader implements AssetLoader<Image> {
    // 0 - no image data
    public static final int TYPE_NO_IMAGE = 0;
    // 1 - uncompressed, color-mapped image
    public static final int TYPE_COLORMAPPED = 1;
    // 2 - uncompressed, true-color image
    public static final int TYPE_TRUECOLOR = 2;
    // 3 - uncompressed, black and white image
    public static final int TYPE_BLACKANDWHITE = 3;
    // 9 - run-length encoded, color-mapped image
    public static final int TYPE_COLORMAPPED_RLE = 9;
    // 10 - run-length encoded, tru-color image
    public static final int TYPE_TRUECOLOR_RLE = 10;
    // 11 - run-length encoded, black and white image
    public static final int TYPE_BLACKANDWHITE_RLE = 11;


    @Override
    public Image load(AssetInfo assetInfo) throws IOException {
        if (!(assetInfo.getKey() instanceof TextureKey)) {
            throw new IllegalArgumentException("Texture assets must be loaded using a TextureKey");
        }

        boolean flip = ((TextureKey) assetInfo.getKey()).isFlipY();
        try (InputStream stream = assetInfo.openStream()) {
            return load(stream, flip);
        }
    }

    private static short flipEndian(short signedShort) {
        int input = signedShort & 0xFFFF;
        return (short) (input << 8 | (input & 0xFFFF) >>> 8);
    }

    private Image load(InputStream stream, boolean flip) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(stream));

        int lengthOfImageID = dataInputStream.readUnsignedByte();
        int colorMapType = dataInputStream.readUnsignedByte();
        int imageType = dataInputStream.readUnsignedByte();

        // Skipping color map specification (5 bytes)
        dataInputStream.readShort();

        short colorMapEntries = dataInputStream.readShort();
        int colorMapDepth = dataInputStream.readUnsignedByte();

        // Skipping image specification (10 bytes)
        dataInputStream.readShort();
        dataInputStream.readShort();

        int imageWidth = flipEndian(dataInputStream.readShort());
        int imageHeight = flipEndian(dataInputStream.readShort());
        int imagePixelDepth = dataInputStream.readUnsignedByte();
        int imageDescriptor = dataInputStream.readUnsignedByte();
        if ((imageDescriptor & 32) != 0) { // bit 5 : if 1, flip top/bottom ordering
            flip = !flip;
        }
//        boolean flipH = false;
//        if ((imageDescriptor & 16) != 0) { // bit 4 : if 1, flip left/right ordering
//            flipH = true;
//        }

        // Skip image ID
        if (lengthOfImageID > 0) {
            dataInputStream.skip(lengthOfImageID);
        }

        if (colorMapType != 0) {
            readColorMapEntries(dataInputStream, colorMapDepth, colorMapEntries, imageType);
        }

        byte[] rawData;
        int dl;
        if (imagePixelDepth == 32) {
            rawData = new byte[imageWidth * imageHeight * 4];
            dl = 4;
        } else {
            rawData = new byte[imageWidth * imageHeight * 3];
            dl = 3;
        }

        Image.Format format;
        switch (imageType) {
            case TYPE_TRUECOLOR:
                format = readTrueColor(dataInputStream, rawData, imagePixelDepth, imageWidth,
                                       imageHeight, flip, dl);
                break;
            case TYPE_COLORMAPPED:
                throw new UnsupportedOperationException();
                //format = readColorMapped(dataInputStream, rawData, imagePixelDepth, imageWidth, imageHeight, flip, dl);
                //break;
            default:
                throw new UnsupportedOperationException(String.valueOf(imageType));
        }

        ByteBuffer scratch = BufferUtils.createByteBuffer(rawData.length);
        scratch.clear();
        scratch.put(rawData);
        scratch.rewind();

        Image textureImage = new Image();
        textureImage.setFormat(format);
        textureImage.setWidth(imageWidth);
        textureImage.setHeight(imageHeight);
        textureImage.setData(scratch);
        return textureImage;
    }

    private Image.Format readColorMapped(DataInputStream dataInputStream, byte[] rawData,
                                         int imagePixelDepth, int imageWidth, int imageHeight,
                                         boolean flip,
                                         int dl) throws IOException {
        int bytesPerIndex = imagePixelDepth / 8;
        int rawDataIndex = 0;

        if (bytesPerIndex == 1) {
            for (int i = 0; i < imageHeight; i++) {
                if (!flip) {
                    rawDataIndex = (imageHeight - 1 - i) * imageWidth * dl;
                }
                for (int j = 0; j < imageWidth; j++) {
                    int index = dataInputStream.readUnsignedByte();
                    // TODO: Refer to colorMapEntries from readColorMapEntries
                }
            }
        }
        return dl == 4 ? Image.Format.RGBA8 : Image.Format.RGB8;
    }

    private Image.Format readTrueColor(DataInputStream dataInputStream, byte[] rawData,
                                       int imagePixelDepth, int imageWidth, int imageHeight,
                                       boolean flip, int dl) throws IOException {
        int rawDataIndex = 0;
        byte red, green, blue, alpha;
        if (imagePixelDepth == 16) {
            byte[] data = new byte[2];
            float scalar = 255f / 31f;
            for (int i = 0; i < imageHeight; i++) {
                if (!flip) {
                    rawDataIndex = (imageHeight - 1 - i) * imageWidth * dl;
                }
                for (int j = 0; j < imageWidth; j++) {
                    data[1] = dataInputStream.readByte();
                    data[0] = dataInputStream.readByte();
                    rawData[rawDataIndex++] = (byte) (getBitsAsBytes(data, 1, 5) * scalar);
                    rawData[rawDataIndex++] = (byte) (getBitsAsBytes(data, 6, 5) * scalar);
                    rawData[rawDataIndex++] = (byte) (getBitsAsBytes(data, 11, 5) * scalar);
                    if (dl == 4) {
                        alpha = getBitsAsBytes(data, 0, 1);
                        if (alpha == 1) {
                            alpha = (byte) 255;
                        }
                        rawData[rawDataIndex++] = alpha;
                    }
                }
            }
            return dl == 4 ? Image.Format.RGBA8 : Image.Format.RGB8;
        } else if (imagePixelDepth == 24) {
            for (int i = 0; i < imageHeight; i++) {
                if (!flip) {
                    rawDataIndex = (imageHeight - 1 - i) * imageWidth * dl;
                } else {
                    rawDataIndex = i * imageWidth * dl;
                }
                dataInputStream.readFully(rawData, rawDataIndex, imageWidth * dl);
            }
            return Image.Format.BGR8;
        } else if (imagePixelDepth == 32) {
            for (int i = 0; i < imageHeight; i++) {
                if (!flip) {
                    rawDataIndex = (imageHeight - 1 - i) * imageWidth * dl;
                }

                for (int j = 0; j < imageWidth; j++) {
                    blue = dataInputStream.readByte();
                    green = dataInputStream.readByte();
                    red = dataInputStream.readByte();
                    alpha = dataInputStream.readByte();
                    rawData[rawDataIndex++] = red;
                    rawData[rawDataIndex++] = green;
                    rawData[rawDataIndex++] = blue;
                    rawData[rawDataIndex++] = alpha;
                }
            }
            return Image.Format.RGBA8;
        }
        return null;
    }

    private byte getBitsAsBytes(byte[] data, int offset, int length) {
        int offsetBytes = offset / 8;
        int indexBits = offset % 8;
        int rVal = 0;

        for (int i = length; --i > 0; ) {
            byte b = data[offsetBytes];
            int test = indexBits == 7 ? 1 : 2 << (6 - indexBits);
            if ((b & test) != 0) {
                if (i == 0) {
                    rVal++;
                } else {
                    rVal += (2 << i - 1);
                }
            }
            indexBits++;
            if (indexBits == 8) {
                indexBits = 0;
                offsetBytes++;
            }
        }

        return (byte) rVal;
    }

    private byte[] readColorMapEntries(DataInputStream dataInputStream, int colorMapDepth,
                                       short colorMapEntries, int imageType) throws IOException {
        int bytesInColorMap = (colorMapDepth * colorMapEntries);
        int bitsPerColor = Math.min(colorMapDepth / 3, 8);

        byte[] colorMapData = new byte[bytesInColorMap];
        dataInputStream.read(colorMapData);

        if (imageType != TYPE_COLORMAPPED && imageType != TYPE_COLORMAPPED_RLE) {
            return null;
        }

        throw new UnsupportedOperationException();
    }
}
