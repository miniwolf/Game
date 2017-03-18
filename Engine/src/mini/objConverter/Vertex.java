package mini.objConverter;

import mini.math.Vector2f;
import mini.math.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Vertex {
    private static final int NO_INDEX = -1;

    private int textureIndex = NO_INDEX;
    private int normalIndex = NO_INDEX;
    private int index;
    private float length;
    private Vertex duplicateVertex = null;
    private Vector2f texCoord;
    private Vector3f position;
    private Vector3f normal;
    private Vector3f averagedTangent = new Vector3f(0, 0, 0);
    private List<Vector3f> tangents = new ArrayList<>();

    public Vertex(int index, Vector3f position) {
        this.index = index;
        this.position = position;
        this.length = position.length();
    }

    public void addTangent(Vector3f tangent) {
        tangents.add(tangent);
    }

    public void averageTangents() {
        if (tangents.isEmpty()) {
            return;
        }
        for (Vector3f tangent : tangents) {
            averagedTangent = averagedTangent.add(tangent);
        }
        averagedTangent.normalizeLocal();
    }

    public Vector3f getAverageTangent() {
        return averagedTangent;
    }

    public int getIndex() {
        return index;
    }

    public float getLength() {
        return length;
    }

    public boolean isSet() {
        return textureIndex != NO_INDEX && normalIndex != NO_INDEX;
    }

    public boolean hasSameTextureAndNormal(int textureIndexOther, int normalIndexOther) {
        return textureIndexOther == textureIndex && normalIndexOther == normalIndex;
    }

    public void setTextureIndex(int textureIndex) {
        this.textureIndex = textureIndex;
    }

    public void setNormalIndex(int normalIndex) {
        this.normalIndex = normalIndex;
    }

    public Vector3f getPosition() {
        return position;
    }

    public int getTextureIndex() {
        return textureIndex;
    }

    public int getNormalIndex() {
        return normalIndex;
    }

    public Vertex getDuplicateVertex() {
        return duplicateVertex;
    }

    public void setDuplicateVertex(Vertex duplicateVertex) {
        this.duplicateVertex = duplicateVertex;
    }

    public void setTexCoord(Vector2f texCoord) {
        this.texCoord = texCoord;
    }

    public Vector2f getTexCoord() {
        return texCoord;
    }

    public void setNormal(Vector3f normal) {
        this.normal = normal;
    }
}
