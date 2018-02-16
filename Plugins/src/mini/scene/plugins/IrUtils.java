package mini.scene.plugins;

import mini.math.Vector4f;
import mini.scene.Mesh;
import mini.scene.VertexBuffer;
import mini.scene.mesh.IndexBuffer;
import mini.scene.mesh.IndexIntBuffer;
import mini.scene.mesh.IndexShortBuffer;
import mini.utils.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IrUtils {

    /**
     * Convert mesh from quads / triangles to triangles only.
     *
     * @param irMesh
     */
    public static void triangulate(IrMesh irMesh) {
        List<IrPolygon> newPolygons = new ArrayList<>(irMesh.polygons.length);
        for (IrPolygon polygon : irMesh.polygons) {
            if (polygon.vertices.length == 4) {
                IrPolygon[] tris = quadToTri(polygon);
                newPolygons.add(tris[0]);
                newPolygons.add(tris[1]);
            } else if (polygon.vertices.length == 3) {
                newPolygons.add(polygon);
            } else {
                newPolygons.addAll(NGonToTri(polygon));
                // N-gon. Have to ignore it...
//                System.err.println("Warning: N-gon encountered, ignoring. The mesh may not appear "
//                                   + "correctly. Triangulate your model prior to export. Quads are"
//                                   + "acceptable to.");
            }
        }
        irMesh.polygons = new IrPolygon[newPolygons.size()];
        newPolygons.toArray(irMesh.polygons);
    }

    private static List<IrPolygon> NGonToTri(IrPolygon polygon) {
        List<IrVertex> verticesToTriangulate = new ArrayList<>(Arrays.asList(polygon.vertices));

        List<IrPolygon> newPolygons = new ArrayList<>();

        while (verticesToTriangulate.size() > 3) {
            IrPolygon irPolygon = new IrPolygon();

            IrVertex index0 = verticesToTriangulate.get(0);
            verticesToTriangulate.remove(index0);
            IrVertex index1 = findClosestVertex(verticesToTriangulate, index0);
            IrVertex index2 = findClosestVertex(verticesToTriangulate, index0);

            irPolygon.vertices = new IrVertex[]{index0, index1, index2};
            newPolygons.add(irPolygon);
        }
        assert verticesToTriangulate.size() == 3;
        IrPolygon irPolygon = new IrPolygon();
        irPolygon.vertices = verticesToTriangulate.toArray(new IrVertex[3]);
        newPolygons.add(irPolygon);
        return newPolygons;
    }

    private static IrVertex findClosestVertex(List<IrVertex> verticesToTriangulate,
                                              IrVertex index0) {
        float minDistance = Float.MAX_VALUE;
        IrVertex result = null;
        for (IrVertex v2 : verticesToTriangulate) {
            float d = v2.pos.distance(index0.pos);
            if (d < minDistance) {
                result = v2;
                minDistance = d;
            }
        }
        if (verticesToTriangulate.size() > 3) {
            verticesToTriangulate.remove(result);
        }

        return result;
    }

    private static IrPolygon[] quadToTri(IrPolygon quad) {
        if (quad.vertices.length == 3) {
            throw new IllegalStateException("Already a triangle");
        }

        IrPolygon[] t = new IrPolygon[]{new IrPolygon(), new IrPolygon()};
        t[0].vertices = new IrVertex[3];
        t[1].vertices = new IrVertex[3];

        IrVertex v0 = quad.vertices[0];
        IrVertex v1 = quad.vertices[1];
        IrVertex v2 = quad.vertices[2];
        IrVertex v3 = quad.vertices[3];

        // Find the pair of vertices that is closest to each other
        // v0 and v2
        // OR
        // v1 and v3
        float d1 = v0.pos.distanceSquared(v2.pos);
        float d2 = v1.pos.distanceSquared(v3.pos);
        if (d1 < d2) {
            // v0 is closer to v2
            // put and edge in v0, v2
            t[0].vertices[0] = v0;
            t[0].vertices[1] = v1;
            t[0].vertices[2] = v3;

            t[1].vertices[0] = v1;
            t[1].vertices[1] = v2;
            t[1].vertices[2] = v3;
        } else {
            // put an edge in v1, v3
            t[0].vertices[0] = v0;
            t[0].vertices[1] = v1;
            t[0].vertices[2] = v2;

            t[1].vertices[0] = v0;
            t[1].vertices[1] = v2;
            t[1].vertices[2] = v3;
        }

        return t;
    }

    /**
     * Separate mesh with multiple materials into multiple meshes each with one material each.
     * <p>
     * Polygons without a material will be added to key -1.
     *
     * @param irMesh Mesh to be separated
     * @return Map from material index to Mesh
     */
    public static Map<Integer, IrMesh> splitByMaterial(IrMesh irMesh) {
        Map<Integer, List<IrPolygon>> materialToPolygonList = new HashMap<>();
        for (IrPolygon polygon : irMesh.polygons) {
            int materialIndex = -1;
            for (IrVertex vertex : polygon.vertices) {
                if (vertex.material == null) {
                    continue;
                }

                if (materialIndex == -1) {
                    materialIndex = vertex.material;
                } else if (materialIndex != vertex.material) {
                    throw new UnsupportedOperationException("Multiple materials assigned to the "
                                                            + "same polygon");
                }
            }

            List<IrPolygon> polygonList = materialToPolygonList
                    .computeIfAbsent(materialIndex, k -> new ArrayList<>());
            polygonList.add(polygon);
        }

        Map<Integer, IrMesh> materialToMesh = new HashMap<>();
        for (Map.Entry<Integer, List<IrPolygon>> entry : materialToPolygonList.entrySet()) {
            int key = entry.getKey();
            List<IrPolygon> polygons = entry.getValue();
            if (polygons.size() <= 0) {
                continue;
            }
            IrMesh newMesh = new IrMesh();
            newMesh.polygons = new IrPolygon[polygons.size()];
            polygons.toArray(newMesh.polygons);
            materialToMesh.put(key, newMesh);
        }

        return materialToMesh;
    }

    /**
     * Convert IrMesh to real irMesh.
     *
     * @param value irMesh to convert
     * @return Converted mesh
     */
    public static Mesh convertIrMeshToMesh(IrMesh irMesh) {
        // TODO: Think about this in terms of DOD, pack the position and so on together and put
        // on one buffer at a time
        Map<IrVertex, Integer> vertexToVertexIndex = new HashMap<>();
        List<Integer> indexes = new ArrayList<>();
        List<IrVertex> vertices = new ArrayList<>();

        int vertexIndex = 0;
        for (IrPolygon polygon : irMesh.polygons) {
            if (polygon.vertices.length != 3) {
                throw new UnsupportedOperationException("IrMesh must be triangulated first");
            }

            for (IrVertex vertex : polygon.vertices) {
                // Is this vertex already indexed?
                Integer existingIndex = vertexToVertexIndex.get(vertex);
                if (existingIndex == null) {
                    // Not indexed yet, allocate index.
                    indexes.add(vertexIndex);
                    vertexToVertexIndex.put(vertex, vertexIndex);
                    vertices.add(vertex);
                    vertexIndex++;
                } else {
                    // Index already allocated for this vertex, reuse it.
                    indexes.add(existingIndex);
                }
            }
        }
        FloatBuffer posBuffer = null;
        FloatBuffer normBuffer = null;
        FloatBuffer tangBuffer = null;
        FloatBuffer uv0Buffer = null;
        FloatBuffer uv1Buffer = null;
        ByteBuffer colBuffer = null;
        IndexBuffer indexBuffer;

        Mesh mesh = new Mesh();
        mesh.setMode(Mesh.Mode.Triangles);

        IrVertex inspectionVertex = vertices.get(0);
        if (inspectionVertex.pos != null) {
            posBuffer = BufferUtils.createVector3Buffer(vertices.size());
            mesh.setBuffer(VertexBuffer.Type.Position, 3, posBuffer);
        }
        if (inspectionVertex.norm != null) {
            normBuffer = BufferUtils.createVector3Buffer(vertices.size());
            mesh.setBuffer(VertexBuffer.Type.Normal, 3, normBuffer);
        }
        if (inspectionVertex.tang4d != null) {
            tangBuffer = BufferUtils.createFloatBuffer(vertices.size() * 4);
            mesh.setBuffer(VertexBuffer.Type.Tangent, 4, tangBuffer);
        }
        if (inspectionVertex.tang != null || inspectionVertex.bitang != null) {
            throw new IllegalStateException(
                    "Mesh is using 3D tangents, must be converted to 4D tangents.");
        }
        if (inspectionVertex.uv0 != null) {
            uv0Buffer = BufferUtils.createVector2Buffer(vertices.size());
            mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, uv0Buffer);
        }
        if (inspectionVertex.uv1 != null) {
            uv1Buffer = BufferUtils.createVector2Buffer(vertices.size());
            mesh.setBuffer(VertexBuffer.Type.TexCoord2, 2, uv1Buffer);
        }
        if (inspectionVertex.color != null) {
            colBuffer = BufferUtils.createByteBuffer(vertices.size() * 4);
            mesh.setBuffer(VertexBuffer.Type.Color, 4, colBuffer);
            mesh.getBuffer(VertexBuffer.Type.Color).setNormalized(true);
        }

        if (vertices.size() >= 65536) {
            // Too many vertices : use intbuffer instead of shortbuffer
            IntBuffer ib = BufferUtils.createIntBuffer(indexes.size());
            mesh.setBuffer(VertexBuffer.Type.Index, 3, ib);
            indexBuffer = new IndexIntBuffer(ib);
        } else {
            ShortBuffer sb = BufferUtils.createShortBuffer(indexes.size());
            mesh.setBuffer(VertexBuffer.Type.Index, 3, sb);
            indexBuffer = new IndexShortBuffer(sb);
        }

        mesh.setStatic();

        for (IrVertex vertex : vertices) {
            if (posBuffer != null) {
                posBuffer.put(vertex.pos.x).put(vertex.pos.y).put(vertex.pos.z);
            }
            if (normBuffer != null) {
                normBuffer.put(vertex.norm.x).put(vertex.norm.y).put(vertex.norm.z);
            }
            if (tangBuffer != null) {
                tangBuffer.put(vertex.tang.x).put(vertex.tang.y).put(vertex.tang.z);
            }
            if (uv0Buffer != null) {
                uv0Buffer.put(vertex.uv0.x).put(vertex.uv0.y);
            }
            if (uv1Buffer != null) {
                uv1Buffer.put(vertex.uv1.x).put(vertex.uv1.y);
            }
            if (colBuffer != null) {
                colBuffer.putInt(vertex.color.asIntABGR());
            }
        }

        for (int i = 0; i < indexes.size(); i++) {
            indexBuffer.put(i, indexes.get(i));
        }

        mesh.updateCounts();
        mesh.updateBound();
        return mesh;
    }

    public static void toTangentWithParity(IrMesh irMesh) {
        for (IrPolygon polygon : irMesh.polygons) {
            for (IrVertex vertex : polygon.vertices) {
                toTangentWithParity(vertex);
            }
        }
    }

    private static void toTangentWithParity(IrVertex vertex) {
        if (vertex.tang == null || vertex.bitang == null) {
            return;
        }

        float wCoord = vertex.norm.cross(vertex.tang).dot(vertex.bitang) < 0f ? -1f : 1f;
        vertex.tang4d = new Vector4f(vertex.tang.x, vertex.tang.y, vertex.tang.z, wCoord);
        vertex.tang = null;
        vertex.bitang = null;
    }
}
