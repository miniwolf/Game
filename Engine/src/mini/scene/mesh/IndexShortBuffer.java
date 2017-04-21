package mini.scene.mesh;

import java.nio.Buffer;
import java.nio.ShortBuffer;

/**
 * IndexBuffer implementation for {@link ShortBuffer}s.
 *
 * @author miniwolf
 */
public class IndexShortBuffer extends IndexBuffer {

    private ShortBuffer buf;

    public IndexShortBuffer(ShortBuffer buffer) {
        buf = buffer;
        buf.rewind();
    }

    @Override
    public int get(int i) {
        return buf.get(i) & 0x0000FFFF;
    }

    @Override
    public void put(int i, int value) {
        buf.put(i, (short) value);
    }

    @Override
    public int size() {
        return buf.limit();
    }

    @Override
    public Buffer getBuffer() {
        return buf;
    }
}
