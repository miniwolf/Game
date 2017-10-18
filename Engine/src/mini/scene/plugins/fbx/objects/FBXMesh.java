package mini.scene.plugins.fbx.objects;

import mini.scene.Geometry;
import mini.scene.Mesh;
import mini.scene.VertexBuffer;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.utils.BufferUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FBXMesh extends FBXObject {
    private static Set<String> unusedElements = new HashSet<>();
    private double[] vertices;
    private double[] normals;
    private double[] uv;
    private int[] uvIndex;
    private int[] indices;
    private int[] materials;

    private List<Integer> vertexList;
    private List<Integer> indexList;
    private List<double[]> uvs = new ArrayList<>();
    private List<int[]> uvIndices = new ArrayList<>();

    private String normalMappingType;
    private String uvMappingType;
    private String materialMappingType;
    private List<Geometry> geometries;

    public FBXMesh(FBXElement element) {
        super(element);
        initializeElement();
    }

    private static int[] toArray(Integer[] arr) {
        int[] ret = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            ret[i] = arr[i];
        }
        return ret;
    }

    public static Set<String> getUnusedElements() {
        return unusedElements;
    }

    private void initializeElement() {
        for (FBXElement fbxElement : element.getChildren()) {
            switch (fbxElement.getName()) {
                case "Vertices":
                    vertices = (double[]) fbxElement.getProperties().get(0);
                    break;
                case "PolygonVertexIndex":
                    indices = (int[]) fbxElement.getProperties().get(0);
                    break;
                case "LayerElementNormal":
                    for (FBXElement normalElement : fbxElement.getChildren()) {
                        switch (normalElement.getName()) {
                            case "MappingInformationType":
                                normalMappingType = (String) normalElement.getProperties().get(0);
                                break;
                            case "Normals":
                                normals = (double[]) normalElement.getProperties().get(0);
                                break;
                        }
                    }
                    break;
                case "LayerElementMaterial":
                    for (FBXElement materialElement : fbxElement.getChildren()) {
                        switch (materialElement.getName()) {
                            case "Materials":
                                materials = (int[]) materialElement.getProperties().get(0);
                                break;
                            case "MappingInformationType":
                                materialMappingType = (String) materialElement.getProperties()
                                                                              .get(0);
                                break;
                        }
                    }
                    break;
                case "LayerElementUV":
                    for (FBXElement uvElement : fbxElement.getChildren()) {
                        switch (uvElement.getName()) {
                            case "UV":
                                uv = (double[]) uvElement.getProperties().get(0);
                                uvs.add(uv);
                                break;
                            case "UVIndex":
                                uvIndex = (int[]) uvElement.getProperties().get(0);
                                uvIndices.add(uvIndex);
                                break;
                            case "MappingInformationType":
                                uvMappingType = (String) uvElement.getProperties().get(0);
                                break;
                        }
                    }
                    break;
                default:
                    unusedElements.add(fbxElement.getName());
            }
        }

        geometries = CreateGeometry();
    }

    public List<Geometry> CreateGeometry() {
        Mesh mesh = new Mesh();
        mesh.setMode(Mesh.Mode.Triangles);

        if (indices != null) {
            int indexCount = indices.length;

            // Count number of vertices to be produced
            int vert = countVertices();

            unrollIndexArray(indexCount, vert);
            unrollVerticesDataArray(mesh, vert);
            unrollNormalDataArray(mesh, vert);
            unrollUVData(mesh, vert);
            return createGeometries(mesh);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private List<Geometry> createGeometries(Mesh mesh) {
        List<Geometry> geometries = new ArrayList<>();
        if (materialMappingType.equals("ByPolygon")) {
            Map<Integer, List<Integer>> indexBuffers = new HashMap<>();
            for (int polygon = 0; polygon < materials.length; polygon++) {
                int material = materials[polygon];
                List<Integer> list = indexBuffers.get(material);
                if (list == null) {
                    list = new ArrayList<>();
                    indexBuffers.put(material, list);
                }
                list.add(polygon * 3);
                list.add(polygon * 3 + 1);
                list.add(polygon * 3 + 2);
                for (Integer materialId : indexBuffers.keySet()) {
                    List<Integer> indices = indexBuffers.get(materialId);

                    Mesh newmesh = mesh.clone();
                    newmesh.setBuffer(VertexBuffer.Type.Index, 3,
                                      toArray(indices.toArray(new Integer[indices.size()])));
                    newmesh.setStatic();
                    newmesh.updateCounts();
                    Geometry geometry = new Geometry();
                    geometry.setMesh(newmesh);
                    geometries.add(geometry);
                    geometry.setUserData("FBXMaterial", materialId);
                }
            }
        } else {
            mesh.setStatic();
            mesh.updateCounts();
            Geometry geom = new Geometry();
            geom.setMesh(mesh);
            geometries.add(geom);
        }
        return geometries;
    }

    private void unrollUVData(Mesh mesh, int vert) {
        for (int uvLayer = 0; uvLayer < uvs.size(); uvLayer++) {
            int[] uvIndex = uvIndices.size() > uvLayer ? uvIndices.get(uvLayer) : null;
            List<Integer> uvIndexList = vertexList;
            if (uvIndex != null) {
                int uvIndexSourceCount = uvIndex.length;
                if (uvIndexSourceCount != vert) {
                    throw new RuntimeException("Invalid number of texcoord index data.");
                }

                unrollUVIndexArray(uvIndexList, vert);
            }

            double[] uv = uvs.get(uvLayer);
            unrollUVDataArray(mesh, uv, vert, uvLayer, uvIndexList);
        }
    }

    private void unrollUVDataArray(Mesh mesh, double[] uv, int vert, int uvLayer,
                                   List<Integer> uvIndexList) {
        FloatBuffer texCoordBuf = BufferUtils.createFloatBuffer(vert * 2);
        VertexBuffer.Type type = VertexBuffer.Type.TexCoord;
        switch (uvLayer) {
            case 1:
                type = VertexBuffer.Type.TexCoord2;
                break;
            case 2:
                type = VertexBuffer.Type.TexCoord3;
                break;
            case 3:
                type = VertexBuffer.Type.TexCoord4;
                break;
            case 4:
                type = VertexBuffer.Type.TexCoord5;
                break;
            case 5:
                type = VertexBuffer.Type.TexCoord6;
                break;
            case 6:
                type = VertexBuffer.Type.TexCoord7;
                break;
            case 7:
                type = VertexBuffer.Type.TexCoord8;
                break;
        }
        mesh.setBuffer(type, 2, texCoordBuf);
        int sourceCount = uv.length / 2;
        for (int i = 0; i < vert; i++) {
            int index = uvIndexList.get(i);
            if (index > sourceCount) {
                throw new RuntimeException("Invalid texCoord mapping. Unexpected lookup.");
            }

            float u = (index >= 0) ? (float) uv[2 * index] : 0;
            float v = (index >= 0) ? (float) uv[2 * index + 1] : 0;
            texCoordBuf.put(u).put(v);
        }
    }

    private void unrollUVIndexArray(List<Integer> uvIndexList, int vert) {
        int polyVertCount = 0;
        for (int i = 0; i < vert; i++) {
            int index = indices[i];
            polyVertCount++;
            if (index >= 0) {
                continue;
            }

            if (polyVertCount == 3) {
                uvIndexList.add(uvIndex[i - 2]);
                uvIndexList.add(uvIndex[i - 1]);
                uvIndexList.add(uvIndex[i]);
            } else {
                uvIndexList.add(uvIndex[i - 3]);
                uvIndexList.add(uvIndex[i - 2]);
                uvIndexList.add(uvIndex[i - 1]);
                uvIndexList.add(uvIndex[i - 3]);
                uvIndexList.add(uvIndex[i - 1]);
                uvIndexList.add(uvIndex[i]);
            }
            polyVertCount = 0;
        }
    }

    private void unrollNormalDataArray(Mesh mesh, int vert) {
        if (normals == null) {
            return;
        }

        FloatBuffer normBuf = BufferUtils.createFloatBuffer(vert * 3);
        mesh.setBuffer(VertexBuffer.Type.Normal, 3, normBuf);
        List<Integer> mapping;
        if ("ByPolygonVertex".equals(normalMappingType)) {
            mapping = indexList;
        } else {
            throw new RuntimeException("Unknown normals mapping type: " + normalMappingType);
        }

        int sourceCount = normals.length / 3;
        for (int i = 0; i < vert; i++) {
            int index = mapping.get(i);
            if (index > sourceCount) {
                throw new RuntimeException("Invalid normal mapping. Unexpected lookup normals");
            }

            float x = (float) normals[3 * index];
            float y = (float) normals[3 * index + 1];
            float z = (float) normals[3 * index + 2];
            normBuf.put(x).put(y).put(z);
        }
    }

    private void unrollVerticesDataArray(Mesh mesh, int vert) {
        if (vertices == null) {
            return;
        }

        FloatBuffer posBuf = BufferUtils.createFloatBuffer(vert * 3);
        mesh.setBuffer(VertexBuffer.Type.Position, 3, posBuf);
        int sourceCount = vertices.length / 3;
        for (int i = 0; i < vert; i++) {
            int index = vertexList.get(i);
            if (index > sourceCount) {
                throw new RuntimeException("Invalid vertex mapping. Unexpected lookup");
            }
            float x = (float) vertices[3 * index];
            float y = (float) vertices[3 * index + 1];
            float z = (float) vertices[3 * index + 2];
            posBuf.put(x).put(y).put(z);
        }
    }

    private void unrollIndexArray(int indexCount, int vert) {
        // Indices contains negative numbers to define polygon last index
        // Check indices strides to be sure we have triangles or quad
        int polyVertCount = 0;
        vertexList = new ArrayList<>(vert);
        indexList = new ArrayList<>(vert);
        for (int i = 0; i < indexCount; i++) {
            int index = indices[i];
            polyVertCount++;
            if (index < 0) {
                int lastIndex = -(index + 1);
                if (polyVertCount == 3) {
                    vertexList.add(indices[i - 2]);
                    vertexList.add(indices[i - 1]);
                    vertexList.add(lastIndex);
                    indexList.add(i - 2);
                    indexList.add(i - 1);
                    indexList.add(i);
                } else {
                    vertexList.add(indices[i - 3]);
                    vertexList.add(indices[i - 2]);
                    vertexList.add(indices[i - 1]);
                    vertexList.add(indices[i - 3]);
                    vertexList.add(indices[i - 1]);
                    vertexList.add(lastIndex);
                    indexList.add(i - 3);
                    indexList.add(i - 2);
                    indexList.add(i - 1);
                    indexList.add(i - 3);
                    indexList.add(i - 1);
                    indexList.add(i);
                }
                polyVertCount = 0;
            }
        }
    }

    private int countVertices() {
        // Indices contains negative numbers to define polygon last index
        // Check indices strides to be sure we have triangles or quad
        int polyVertCount = 0;
        int vert = 0;
        for (int index : indices) {
            polyVertCount++;
            if (index >= 0) {
                continue;
            }

            if (polyVertCount == 3) {
                vert += 3; // A triangle
            } else if (polyVertCount == 4) {
                vert += 6; // A quad produce two triangles
            } else {
                throw new RuntimeException("Unsupported PolygonVertexIndex stride.");
            }
            polyVertCount = 0;
        }
        return vert;
    }
}
