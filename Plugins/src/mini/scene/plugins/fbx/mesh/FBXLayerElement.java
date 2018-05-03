package mini.scene.plugins.fbx.mesh;

import mini.math.ColorRGBA;
import mini.math.Vector2f;
import mini.math.Vector3f;
import mini.scene.plugins.fbx.file.FBXElement;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FBXLayerElement {
    private static final Set<String> indexTypes = new HashSet<>();

    static {
        indexTypes.add("UVIndex");
        indexTypes.add("NormalsIndex");
        indexTypes.add("TangentsIndex");
        indexTypes.add("BinormalsIndex");
        indexTypes.add("Smoothing");
        indexTypes.add("Materials");
        indexTypes.add("TextureId");
        indexTypes.add("PolygonGroup");
        indexTypes.add("ColorIndex");
    }

    private int[] dataIndices;
    private Object[] data;
    private Integer index;
    private Type type;
    private String name;
    private MappingInformationType mapInfoType;
    private ReferenceInformationType refInfoType;

    static Vector3f[] toVector3f(double[] data) {
        Vector3f[] vectors = new Vector3f[data.length / 3];
        for (int i = 0; i < vectors.length; i++) {
            float x = (float) data[i * 3];
            float y = (float) data[i * 3 + 1];
            float z = (float) data[i * 3 + 2];
            vectors[i] = new Vector3f(x, y, z);
        }
        return vectors;
    }

    private static Vector2f[] toVector2f(double[] data) {
        Vector2f[] vectors = new Vector2f[data.length / 2];
        for (int i = 0; i < vectors.length; i++) {
            float x = (float) data[i * 2];
            float y = (float) data[i * 2 + 1];
            vectors[i] = new Vector2f(x, y);
        }
        return vectors;
    }

    private static ColorRGBA[] toColorRGBA(double[] data) {
        ColorRGBA[] colors = new ColorRGBA[data.length / 4];
        for (int i = 0; i < colors.length; i++) {
            float r = (float) data[i * 4];
            float g = (float) data[i * 4 + 1];
            float b = (float) data[i * 4 + 2];
            float a = (float) data[i * 4 + 3];
            colors[i] = new ColorRGBA(r, g, b, a);
        }
        return colors;
    }

    public static FBXLayerElement fromElement(FBXElement fbxElement) {
        FBXLayerElement layerElement = new FBXLayerElement();
        layerElement.index = (int) fbxElement.getProperties().get(0);

        String elementType = fbxElement.name.substring("LayerElement".length());
        layerElement.type = Type.valueOf(elementType);

        for (FBXElement element : fbxElement.getChildren()) {
            if (element.name.equals("MappingInformationType")) {
                String mapInfoTypeValue = (String) element.getProperties().get(0);
                if (mapInfoTypeValue.equals("ByVertice")) {
                    mapInfoTypeValue = "ByVertex";
                }
                layerElement.mapInfoType = MappingInformationType.valueOf(mapInfoTypeValue);
            } else if (element.name.equals("ReferenceInformationType")) {
                String refInfoTypeValue = (String) element.getProperties().get(0);
                if (refInfoTypeValue.equals("Index")) {
                    refInfoTypeValue = "IndexToDirect";
                }
                layerElement.refInfoType = ReferenceInformationType.valueOf(refInfoTypeValue);
            } else if (element.name.equals("Name")) {
                layerElement.name = (String) element.getProperties().get(0);
            } else if (indexTypes.contains(element.name)) {
                layerElement.dataIndices = FBXMeshReader.getIntArray(element);
            } else if (element.name.equals("Normals")) {
                layerElement.data = toVector3f(FBXMeshReader.getDoubleArray(element));
            } else if (element.name.equals("UV")) {
                layerElement.data = toVector2f(FBXMeshReader.getDoubleArray(element));
            } else if (element.name.equals("Colors")) {
                layerElement.data = toColorRGBA(FBXMeshReader.getDoubleArray(element));
            }
        }

        if (layerElement.data == null) {
            // This could happen on Materials where data = dataIndices
            layerElement.refInfoType = ReferenceInformationType.Direct;
            layerElement.data = new Integer[layerElement.dataIndices.length];
            Arrays.setAll(layerElement.data, i -> layerElement.dataIndices[i]);
            layerElement.dataIndices = null;
        }
        return layerElement;
    }

    public Object getVertexData(int polygonIndex, int polygonVertexIndex, int vertexIndex,
                                int edgeIndex) {
        switch (refInfoType) {
            case Direct:
                return getVertexDataDirect(polygonIndex, polygonVertexIndex, vertexIndex,
                                           edgeIndex);
            case IndexToDirect:
                return getVertexDataIndexToDirect(polygonIndex, polygonVertexIndex, vertexIndex,
                                                  edgeIndex);
            default:
                return null;
        }
    }

    private Object getVertexDataIndexToDirect(int polygonIndex, int polygonVertexIndex,
                                              int vertexIndex, int edgeIndex) {
        switch (mapInfoType) {
            case AllSame:
                return data[dataIndices[0]];
            case ByPolygon:
                return data[dataIndices[polygonIndex]];
            case ByPolygonVertex:
                return data[dataIndices[polygonVertexIndex]];
            case ByVertex:
                return data[dataIndices[vertexIndex]];
            case ByEdge:
                return data[dataIndices[edgeIndex]];
            default:
                throw new UnsupportedOperationException();
        }
    }

    private Object getVertexDataDirect(int polygonIndex, int polygonVertexIndex, int vertexIndex,
                                       int edgeIndex) {
        switch (mapInfoType) {
            case AllSame:
                return data[0];
            case ByPolygon:
                return data[polygonIndex];
            case ByPolygonVertex:
                return data[polygonVertexIndex];
            case ByVertex:
                return data[vertexIndex];
            case ByEdge:
                return data[edgeIndex];
            default:
                throw new UnsupportedOperationException();
        }
    }

    public Integer getIndex() {
        return index;
    }

    public enum Type {
        Position,
        BoneIndex,
        BoneWeight,
        Texture, // TODO: Implement this...
        Normal, // Vector3f
        Binormal,
        Tangent,
        UV, // Vector2f
        Material, // Integer
        Smoothing,
        Color // ColorRGBA
    }

    public enum MappingInformationType {
        ByPolygonVertex,
        ByVertex,
        ByPolygon,
        ByEdge,
        NoMappingInformation,
        AllSame
    }

    public enum ReferenceInformationType {
        Direct,
        IndexToDirect
    }

    public Type getType() {
        return type;
    }
}
