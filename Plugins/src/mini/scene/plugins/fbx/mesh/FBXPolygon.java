package mini.scene.plugins.fbx.mesh;

import java.util.List;

public class FBXPolygon {
    private int[] indices;

    public static FBXPolygon fromIndices(List<Integer> indices) {
        FBXPolygon polygon = new FBXPolygon();
        polygon.setIndices(indices.stream().mapToInt(index -> index).toArray());
        return polygon;
    }

    public int[] getIndices() {
        return indices;
    }

    public void setIndices(int[] indices) {
        this.indices = indices;
    }
}
