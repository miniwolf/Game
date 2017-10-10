package mini.scene.plugins.fbx.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.zip.InflaterInputStream;

public class FBXReader {
    private static final int NULL_RECORD_LENGTH = 13;
    private static final byte[] BLOCK_SENTINEL_DATA = new byte[NULL_RECORD_LENGTH];

    /**
     * String at the start of an FBX File:
     * "Kaydara FBX Binary\x20\x20\x00\x1a\x00"
     */
    public static final byte[] FBX_HEADER = new byte[]{0x4b, 0x61, 0x79, 0x64, 0x61, 0x72, 0x61,
                                                       0x20, 0x46, 0x42, 0x58, 0x20, 0x42, 0x69,
                                                       0x6e, 0x61, 0x72, 0x79, 0x20, 0x20, 0x00,
                                                       0x1a, 0x00};

    public FBXFile readFBX(InputStream in) throws IOException {
        FBXFile file = new FBXFile();
        ByteBuffer byteBuffer = readToByteBuffer(in);
        in.close(); // No longer need to retain input stream

        checkHeaderIsFBX(byteBuffer);

        file.setVersion(getUInt(byteBuffer));
        FBXElement e;
        while ((e = readFBXElement(byteBuffer)) != null) {
            file.addElement(e);
        }
        return file;
    }

    private FBXElement readFBXElement(ByteBuffer byteBuffer) throws IOException {
        long endOffset = getUInt(byteBuffer);
        if (endOffset == 0) {
            return null;
        }
        long numProperties = getUInt(byteBuffer);
        long propertyListLength = getUInt(byteBuffer);

        FBXElement fbxElement = new FBXElement((int) numProperties);
        byte[] name = getBytes(byteBuffer, getUByte(byteBuffer));
        fbxElement.setName(new String(name));

        for (int i = 0; i < numProperties; ++i) {
            char dataType = readDataType(byteBuffer);
            fbxElement.addProperty(readData(byteBuffer, dataType));
            fbxElement.addPropertyType(dataType, i);
        }

        if (byteBuffer.position() < endOffset) { // Elements left to read
            while (byteBuffer.position() < (endOffset - NULL_RECORD_LENGTH)) {
                fbxElement.addChild(readFBXElement(byteBuffer));
            }

            if (!Arrays.equals(BLOCK_SENTINEL_DATA, getBytes(byteBuffer, NULL_RECORD_LENGTH))) {
                throw new IOException("Failed to read null record, expected 13 zero bytes.");
            }
        }
        return fbxElement;
    }

    private Object readData(ByteBuffer byteBuffer, char dataType) throws IOException {
        switch (dataType) {
            case 'Y': // 2 byte signed Integer
                return byteBuffer.getShort();
            case 'C': //1 bit boolean (1: true, 0: false) encoded as the LSB of a 1 Byte value.
                return byteBuffer.get() == 1;
            case 'I': //4 byte signed Integer
                return byteBuffer.getInt();
            case 'F': //4 byte single-precision IEEE 754 number
                return byteBuffer.getFloat();
            case 'D': //8 byte double-precision IEEE 754 number
                return byteBuffer.getDouble();
            case 'L': //8 byte signed Integer
                return byteBuffer.getLong();
            case 'f': // Array of 4 byte single-precision IEEE 754 number
                return readArray(byteBuffer, 'f', 4);
            case 'i': // Array of 4 byte signed Integer
                return readArray(byteBuffer, 'i', 4);
            case 'd': // Array of 8 byte double-precision IEEE 754 number
                return readArray(byteBuffer, 'd', 8);
            case 'l': // Array of 8 byte signed Integer
                return readArray(byteBuffer, 'l', 8);
            case 'b': // Array of 1 byte Booleans (always 0 or 1)
                return readArray(byteBuffer, 'b', 1);
            case 'R': // raw binary data
                return getBytes(byteBuffer, (int) getUInt(byteBuffer));
            case 'S': // String
                return new String(getBytes(byteBuffer, (int) getUInt(byteBuffer)));
        }
        throw new IOException("Unknown data type: " + dataType);
    }

    private Object readArray(ByteBuffer byteBuffer, char type, int bytes) throws IOException {
        int arrayLength = (int) getUInt(byteBuffer);
        int encoding = (int) getUInt(byteBuffer);
        int compressedLength = (int) getUInt(byteBuffer);
        byte[] data;
        if (encoding == 1) {
            data = inflate(getBytes(byteBuffer, compressedLength));
        } else if (encoding == 0) {
            data = getBytes(byteBuffer, arrayLength * bytes);
        } else {
            throw new IOException("Weird encoding not supported: " + encoding);
        }

        if (data.length != arrayLength * bytes) {
            throw new IOException("Wrong data length. Expected: " + arrayLength * bytes
                                  + ", got: " + data.length);
        }

        ByteBuffer dataInputStream = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        switch (type) {
            case 'f': {
                float[] arr = new float[arrayLength];
                for (int i = 0; i < arrayLength; ++i) {
                    arr[i] = dataInputStream.getFloat();
                }
                return arr;
            }
            case 'i': {
                int[] arr = new int[arrayLength];
                for (int i = 0; i < arrayLength; ++i) {
                    arr[i] = dataInputStream.getInt();
                }
                return arr;
            }
            case 'd': {
                double[] arr = new double[arrayLength];
                for (int i = 0; i < arrayLength; ++i) {
                    arr[i] = dataInputStream.getDouble();
                }
                return arr;
            }
            case 'l': {
                long[] arr = new long[arrayLength];
                for (int i = 0; i < arrayLength; ++i) {
                    arr[i] = dataInputStream.getLong();
                }
                return arr;
            }
            case 'b': {
                boolean[] arr = new boolean[arrayLength];
                for (int i = 0; i < arrayLength; ++i) {
                    arr[i] = dataInputStream.get() == 1;
                }
                return arr;
            }
        }
        throw new IOException("Unknown array data type: " + type);
    }

    private byte[] inflate(byte[] data) throws IOException {
        InflaterInputStream gzip = new InflaterInputStream(new ByteArrayInputStream(data));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (gzip.available() > 0) {
            int l = gzip.read(buffer);
            if (l > 0) {
                out.write(buffer, 0, l);
            }
        }
        return out.toByteArray();
    }

    private char readDataType(ByteBuffer byteBuffer) {
        return (char) byteBuffer.get();
    }

    private int getUByte(ByteBuffer byteBuffer) {
        return byteBuffer.get() & 0xFF;
    }

    private long getUInt(ByteBuffer byteBuffer) {
        return byteBuffer.getInt() & 0x00000000ffffffffL;
    }

    private void checkHeaderIsFBX(ByteBuffer byteBuffer) throws IOException {
        byte[] headerBytes = getBytes(byteBuffer, FBX_HEADER.length);
        if (!Arrays.equals(FBX_HEADER, headerBytes)) {
            throw new IOException("Only binary FBX files are supported");
        }
    }

    private byte[] getBytes(ByteBuffer buffer, int size) {
        byte[] b = new byte[size];
        buffer.get(b);
        return b;
    }

    private ByteBuffer readToByteBuffer(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(2048);
        byte[] tmpBuffer = new byte[2048];
        int r;
        while ((r = in.read(tmpBuffer)) != -1) {
            out.write(tmpBuffer, 0, r);
        }
        return ByteBuffer.wrap(out.toByteArray()).order(ByteOrder.LITTLE_ENDIAN);
    }
}
