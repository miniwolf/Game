package mini.scene.debug;

import mini.scene.Mesh;
import mini.scene.VertexBuffer;
import mini.utils.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Grid extends Mesh {
    public Grid(int xLines, int yLines, float lineDistance) {
        int lineCount = xLines + yLines + 4;
        FloatBuffer fpb = BufferUtils.createFloatBuffer(6 * lineCount);
        ShortBuffer sib = BufferUtils.createShortBuffer(2 * lineCount);

        float xLineLength = (xLines + 1) * lineDistance;
        float yLineLength = (yLines + 1) * lineDistance;
        int currentIndex = 0;

        for (int i = 0; i < xLines + 2; i++) {
            float y = (i) * lineDistance;

            fpb.put(0).put(0).put(y);
            fpb.put(xLineLength).put(0).put(y);

            sib.put((short) currentIndex++);
            sib.put((short) currentIndex++);
        }

        for (int i = 0; i < yLines + 2; i++) {
            float x = (i) * lineDistance;

            fpb.put(x).put(0).put(0);
            fpb.put(x).put(0).put(yLineLength);

            sib.put((short) currentIndex++);
            sib.put((short) currentIndex++);
        }

        fpb.flip();
        sib.flip();

        setBuffer(VertexBuffer.Type.Position, 3, fpb);
        setBuffer(VertexBuffer.Type.Index, 2, sib);

        setMode(Mode.Lines);

        updateBound();
        updateCounts();
        setStatic();
    }
}
