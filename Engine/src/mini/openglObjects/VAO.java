package mini.openglObjects;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class VAO {
    private static final int BYTES_PER_FLOAT = 4;

    private final int id;
    private int indexCount;
    private VBO dataVbo;
    private VBO indexVbo;

    public static VAO create() {
        int id = GL30.glGenVertexArrays();
        return new VAO(id);
    }

    private VAO(int id) {
        this.id = id;
    }

    public int getIndexCount() {
        return indexCount;
    }

    private void bind() {
        GL30.glBindVertexArray(id);
    }

    public void bind(int... attributes) {
        bind();
        for (int i : attributes) {
            GL20.glEnableVertexAttribArray(i);
        }
    }

    private void unbind() {
        GL30.glBindVertexArray(0);
    }

    public void unbind(int... attributes) {
        for (int i : attributes) {
            GL20.glDisableVertexAttribArray(i);
        }
        unbind();
    }

    public void storeData(int[] indices, int vertexCount, float[]... data) {
        bind();
        storeData(vertexCount, data);
        createIndexBuffer(indices);
        unbind();
    }

    public void storeUnindexedData(int vertexCount, float[]... data) {
        bind();
        storeData(vertexCount, data);
        unbind();
    }

    public void delete() {
        GL30.glDeleteVertexArrays(id);
        dataVbo.delete();
        indexVbo.delete();
    }

    private void createIndexBuffer(int[] indices) {
        this.indexVbo = VBO.create(GL15.GL_ELEMENT_ARRAY_BUFFER);
        indexVbo.bind();
        indexVbo.storeData(indices);
        this.indexCount = indices.length;
    }

    private void storeData(int vertexCount, float[]... data) {
        float[] interleavedData = interleaveFloatData(vertexCount, data);
        int[] lengths = getAttributeLengths(data, vertexCount);
        storeInterleavedData(interleavedData, lengths);
    }

    private int[] getAttributeLengths(float[][] data, int vertexCount) {
        int[] lengths = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            lengths[i] = data[i].length / vertexCount;
        }
        return lengths;
    }

    private void storeInterleavedData(float[] data, int... lengths) {
        dataVbo = VBO.create(GL15.GL_ARRAY_BUFFER);
        dataVbo.bind();
        dataVbo.storeData(data);
        int bytesPerVertex = calculateBytesPerVertex(lengths);
        linkVboDataToAttributes(lengths, bytesPerVertex);
        dataVbo.unbind();
    }

    private void linkVboDataToAttributes(int[] lengths, int bytesPerVertex) {
        int total = 0;
        for (int i = 0; i < lengths.length; i++) {
            GL20.glVertexAttribPointer(i, lengths[i], GL11.GL_FLOAT, false, bytesPerVertex,
                                       BYTES_PER_FLOAT * total);
            total += lengths[i];
        }
    }

    private int calculateBytesPerVertex(int[] lengths) {
        int total = 0;
        for (int length : lengths) {
            total += length;
        }
        return BYTES_PER_FLOAT * total;
    }

    private float[] interleaveFloatData(int count, float[]... data) {
        int totalSize = 0;
        int[] lengths = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            int elementLength = data[i].length / count;
            lengths[i] = elementLength;
            totalSize += data[i].length;
        }
        float[] interleavedBuffer = new float[totalSize];
        int pointer = 0;
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < data.length; j++) {
                int elementLength = lengths[j];
                for (int k = 0; k < elementLength; k++) {
                    interleavedBuffer[pointer++] = data[j][i * elementLength + k];
                }
            }
        }
        return interleavedBuffer;
    }

}
