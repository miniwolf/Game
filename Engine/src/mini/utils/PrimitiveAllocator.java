package mini.utils;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * This class contains a primitve allocator with no special logic, should work
 * on any jvm
 */
public final class PrimitiveAllocator implements BufferAllocator {
    @Override
    public void destroyDirectBuffer(Buffer toBeDestroyed) {
        // no exception by intent, as this way naivly written java7/8
        // applications wont crash on 9 assuming they can dispose buffers
        System.err.println("Warning destroyBuffer not supported");
    }

    @Override
    public ByteBuffer allocate(int size) {
        return ByteBuffer.allocateDirect(size);
    }
}
