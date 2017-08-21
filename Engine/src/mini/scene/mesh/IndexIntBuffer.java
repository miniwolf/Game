package mini.scene.mesh;

import java.nio.Buffer;
import java.nio.IntBuffer;

/**
 * IndexBuffer implementation for {@link IntBuffer}s.
 *
 * @author miniwolf
 */
public class IndexIntBuffer extends IndexBuffer {

    private IntBuffer buf;

    public IndexIntBuffer(IntBuffer buffer) {
        buf = buffer;
        buf.rewind();
    }

    @Override
    public int get(int i) {
        return buf.get(i);
    }

    @Override
    public void put(int i, int value) {
        buf.put(i, value);
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
