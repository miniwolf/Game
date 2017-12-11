package mini.utils;

import mini.math.ColorRGBA;
import mini.math.FastMath;
import mini.math.Vector2f;
import mini.math.Vector3f;
import mini.scene.Geometry;
import mini.scene.Mesh;
import mini.scene.Node;
import mini.scene.Spatial;
import mini.scene.VertexBuffer;
import mini.scene.mesh.IndexBuffer;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static mini.utils.BufferUtils.destroyDirectBuffer;
import static mini.utils.BufferUtils.populateFromBuffer;
import static mini.utils.BufferUtils.setInBuffer;

public class TangentBinormalGenerator {

    private static final float ZERO_TOLERANCE = 0.0000001f;
    private static float toleranceDot;
    public static boolean debug = false;

    static {
        setToleranceAngle(45);
    }

    private static List<VertexData> initVertexData(int size) {
        List<VertexData> vertices = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            vertices.add(new VertexData());
        }
        return vertices;
    }

    //Don't remove splitmirorred boolean,It's not used right now, but i intend to
    //make this method also split vertice with rotated tangent space and I'll
    //add another splitRotated boolean
    private static List<VertexData> splitVertices(Mesh mesh, List<VertexData> vertexData,
                                                  boolean splitMirorred) {
        int nbVertices = mesh.getBuffer(VertexBuffer.Type.Position).getNumElements();
        List<VertexData> newVertices = new ArrayList<>();
        Map<Integer, Integer> indiceMap = new HashMap<>();
        FloatBuffer normalBuffer = mesh.getFloatBuffer(VertexBuffer.Type.Normal);

        for (int i = 0; i < vertexData.size(); i++) {
            List<TriangleData> triangles = vertexData.get(i).triangles;
            Vector3f givenNormal = new Vector3f();
            populateFromBuffer(givenNormal, normalBuffer, i);

            ArrayList<TriangleData> trianglesUp = new ArrayList<>();
            ArrayList<TriangleData> trianglesDown = new ArrayList<>();
            for (TriangleData triangleData : triangles) {
                if (parity(givenNormal, triangleData.normal) > 0) {
                    trianglesUp.add(triangleData);
                } else {
                    trianglesDown.add(triangleData);
                }
            }

            //if the vertex has triangles with opposite parity it has to be split
            if (!trianglesUp.isEmpty() && !trianglesDown.isEmpty()) {
                System.err.println("Splitting vertex " + i);
                //assigning triangle with the same parity to the original vertex
                vertexData.get(i).triangles.clear();
                vertexData.get(i).triangles.addAll(trianglesUp);

                //creating a new vertex
                VertexData newVert = new VertexData();
                //assigning triangles with opposite parity to it
                newVert.triangles.addAll(trianglesDown);

                newVertices.add(newVert);
                //keep vertex index to fix the index buffers later
                indiceMap.put(nbVertices, i);
                for (TriangleData tri : newVert.triangles) {
                    for (int j = 0; j < tri.index.length; j++) {
                        if (tri.index[j] == i) {
                            tri.index[j] = nbVertices;
                        }
                    }
                }
                nbVertices++;

            }

        }

        if (!newVertices.isEmpty()) {

            //we have new vertices, we need to update the mesh's buffers.
            for (VertexBuffer.Type type : VertexBuffer.Type.values()) {
                //skip tangent buffer as we're gonna overwrite it later
                if (type
                    == VertexBuffer.Type.Tangent /*|| type == VertexBuffer.Type.BindPoseTangent*/) {
                    continue;
                }
                VertexBuffer vb = mesh.getBuffer(type);
                //Some buffer (hardware skinning ones) can be there but not
                //initialized, they must be skipped.
                //They'll be initialized when Hardware Skinning is engaged
                if (vb == null || vb.getNumComponents() == 0) {
                    continue;
                }

                Buffer buffer = vb.getData();
                //IndexBuffer has special treatement, only swapping the vertex indices is needed
                if (type == VertexBuffer.Type.Index) {
                    boolean isShortBuffer = vb.getFormat() == VertexBuffer.Format.UnsignedShort;
                    for (VertexData vertex : newVertices) {
                        for (TriangleData tri : vertex.triangles) {
                            for (int i = 0; i < tri.index.length; i++) {
                                if (isShortBuffer) {
                                    ((ShortBuffer) buffer)
                                            .put(tri.triangleOffset + i, (short) tri.index[i]);
                                } else {
                                    ((IntBuffer) buffer).put(tri.triangleOffset + i, tri.index[i]);
                                }
                            }
                        }
                    }
                    vb.setUpdateNeeded();
                } else {
                    //copy the buffer in a bigger one and append nex vertices to the end
                    Buffer newVerts = VertexBuffer
                            .createBuffer(vb.getFormat(), vb.getNumComponents(), nbVertices);
                    if (buffer != null) {
                        buffer.rewind();
                        bulkPut(vb.getFormat(), newVerts, buffer);

                        int index = vertexData.size();
                        newVerts.position(vertexData.size() * vb.getNumComponents());
                        for (int j = 0; j < newVertices.size(); j++) {
                            int oldInd = indiceMap.get(index);
                            for (int i = 0; i < vb.getNumComponents(); i++) {
                                putValue(vb.getFormat(), newVerts, buffer,
                                         oldInd * vb.getNumComponents() + i);
                            }
                            index++;
                        }
                        vb.updateData(newVerts);
                        //destroy previous buffer as it's no longer needed
                        destroyDirectBuffer(buffer);
                    }
                }
            }
            vertexData.addAll(newVertices);

            mesh.updateCounts();
        }

        return vertexData;
    }

    /** Keeps track of tangent, binormal, and normal for one triangle.
     */
    public static class TriangleData {
        public final Vector3f tangent;
        public final Vector3f binormal;
        public final Vector3f normal;
        public int[] index = new int[3];
        public int triangleOffset;

        public TriangleData(Vector3f tangent, Vector3f binormal, Vector3f normal) {
            this.tangent = tangent;
            this.binormal = binormal;
            this.normal = normal;
        }
        public void setIndex(int[] index) {
            System.arraycopy(index, 0, this.index, 0, index.length);
        }
    }

    private static List<VertexInfo> linkVertices(Mesh mesh, boolean splitMirrored) {
        List<VertexInfo> vertexMap = new ArrayList<>();

        FloatBuffer vertexBuffer = mesh.getFloatBuffer(VertexBuffer.Type.Position);
        FloatBuffer normalBuffer = mesh.getFloatBuffer(VertexBuffer.Type.Normal);
        FloatBuffer texcoordBuffer = mesh.getFloatBuffer(VertexBuffer.Type.TexCoord);

        Vector3f position = new Vector3f();
        Vector3f normal = new Vector3f();
        Vector2f texCoord = new Vector2f();

        final int size = vertexBuffer.limit() / 3;
        for (int i = 0; i < size; i++) {

            populateFromBuffer(position, vertexBuffer, i);
            populateFromBuffer(normal, normalBuffer, i);
            populateFromBuffer(texCoord, texcoordBuffer, i);

            boolean found = false;
            //Nehon 07/07/2013
            //Removed this part, joining splitted vertice to compute tangent space makes no sense to me
            //separate vertice should have separate tangent space
            if (!splitMirrored) {
                for (VertexInfo vertexInfo : vertexMap) {
                    if (approxEqual(vertexInfo.position, position) &&
                        approxEqual(vertexInfo.normal, normal) &&
                        approxEqual(vertexInfo.texCoord, texCoord)) {
                        vertexInfo.indices.add(i);
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                VertexInfo vertexInfo = new VertexInfo(position.clone(), normal.clone(),
                                                       texCoord.clone());
                vertexInfo.indices.add(i);
                vertexMap.add(vertexInfo);
            }
        }

        return vertexMap;
    }

    public static void generate(Mesh mesh) {
        generate(mesh, true, false);
    }

    public static void generate(Spatial scene, boolean splitMirrored) {
        if (scene instanceof Node) {
            Node node = (Node) scene;
            for (Spatial child : node.getChildren()) {
                generate(child, splitMirrored);
            }
        } else {
            Geometry geom = (Geometry) scene;
            Mesh mesh = geom.getMesh();

            // Check to ensure mesh has texcoords and normals before generating
            if (mesh.getBuffer(VertexBuffer.Type.TexCoord) != null
                    && mesh.getBuffer(VertexBuffer.Type.Normal) != null){
                generate(geom.getMesh(),true, splitMirrored);
            }
        }
    }

    public static void generate(Spatial scene) {
        generate(scene, false);
    }

    public static void generate(Mesh mesh, boolean approxTangents, boolean splitMirrored) {
        int[] index = new int[3];
        Vector3f[] v = new Vector3f[3];
        Vector2f[] t = new Vector2f[3];
        for (int i = 0; i < 3; i++) {
            v[i] = new Vector3f();
            t[i] = new Vector2f();
        }

        if (mesh.getBuffer(VertexBuffer.Type.Normal) == null) {
            throw new IllegalArgumentException("The given mesh has no normal data!");
        }

        List<VertexData> vertices;
        switch (mesh.getMode()) {
            case Triangles:
                vertices = processTriangles(mesh, index, v, t, splitMirrored);
                if(splitMirrored){
                    splitVertices(mesh, vertices, splitMirrored);
                }
                break;
//            case TriangleStrip:
//                vertices = processTriangleStrip(mesh, index, v, t);
//                break;
//            case TriangleFan:
//                vertices = processTriangleFan(mesh, index, v, t);
//                break;
            default:
                throw new UnsupportedOperationException(
                        mesh.getMode() + " is not supported.");
        }

        processTriangleData(mesh, vertices, approxTangents,splitMirrored);

        //if the mesh has a bind pose, we need to generate the bind pose for the tangent buffer
        //TangentUtils.generateBindPoseTangentsIfNecessary(mesh);
    }

    public static void generate(Mesh mesh, boolean approxTangents) {
        generate(mesh, approxTangents, false);
    }

    private static  List<VertexData> processTriangles(Mesh mesh,
                                                      int[] index, Vector3f[] v, Vector2f[] t, boolean splitMirrored) {
        IndexBuffer indexBuffer = mesh.getIndexBuffer();
        FloatBuffer vertexBuffer = (FloatBuffer) mesh.getBuffer(VertexBuffer.Type.Position).getData();
        if (mesh.getBuffer(VertexBuffer.Type.TexCoord) == null) {
            throw new IllegalArgumentException("Can only generate tangents for "
                    + "meshes with texture coordinates");
        }

        FloatBuffer textureBuffer = (FloatBuffer) mesh.getBuffer(VertexBuffer.Type.TexCoord).getData();

        List<VertexData> vertices = initVertexData(vertexBuffer.limit() / 3);

        for (int i = 0; i < indexBuffer.size() / 3; i++) {
            for (int j = 0; j < 3; j++) {
                index[j] = indexBuffer.get(i * 3 + j);
                populateFromBuffer(v[j], vertexBuffer, index[j]);
                populateFromBuffer(t[j], textureBuffer, index[j]);
            }

            TriangleData triData = processTriangle(index, v, t);
            if(splitMirrored){
                triData.setIndex(index);
                triData.triangleOffset = i * 3 ;
            }
            vertices.get(index[0]).triangles.add(triData);
            vertices.get(index[1]).triangles.add(triData);
            vertices.get(index[2]).triangles.add(triData);
        }

        return vertices;
    }

    private static void processTriangleData(Mesh mesh, List<VertexData> vertices,
                                            boolean approxTangent, boolean splitMirrored) {
        List<VertexInfo> vertexMap = linkVertices(mesh, splitMirrored);

        FloatBuffer tangents = BufferUtils.createFloatBuffer(vertices.size() * 4);

        ColorRGBA[] cols = null;
        if (debug) {
            cols = new ColorRGBA[vertices.size()];
        }

        Vector3f tangent = new Vector3f();
        Vector3f binormal = new Vector3f();
        //Vector3f normal = new Vector3f();
        Vector3f givenNormal = new Vector3f();

        Vector3f tangentUnit = new Vector3f();
        Vector3f binormalUnit = new Vector3f();

        for (VertexInfo vertexInfo : vertexMap) {
            float wCoord = -1;

            givenNormal.set(vertexInfo.normal);
            givenNormal.normalizeLocal();

            TriangleData firstTriangle = vertices.get(vertexInfo.indices.get(0)).triangles.get(0);

            // check tangent and binormal consistency
            tangent.set(firstTriangle.tangent);
            tangent.normalizeLocal();
            binormal.set(firstTriangle.binormal);
            binormal.normalizeLocal();

            for (int i : vertexInfo.indices) {
                List<TriangleData> triangles = vertices.get(i).triangles;

                for (TriangleData triangleData : triangles) {
                    tangentUnit.set(triangleData.tangent);
                    tangentUnit.normalizeLocal();
                    if (tangent.dot(tangentUnit) < toleranceDot) {
                        System.err.println("Angle between tangents exceeds tolerance "
                                           + "for vertex " + i);
                        break;
                    }

                    if (!approxTangent) {
                        binormalUnit.set(triangleData.binormal);
                        binormalUnit.normalizeLocal();
                        if (binormal.dot(binormalUnit) < toleranceDot) {
                            System.err.println("Angle between binormals exceeds tolerance "
                                               + "for vertex " + i);
                            break;
                        }
                    }
                }
            }

            // find average tangent
            tangent.set(0, 0, 0);
            binormal.set(0, 0, 0);

            int triangleCount = 0;
            for (int i : vertexInfo.indices) {
                List<TriangleData> triangles = vertices.get(i).triangles;
                triangleCount += triangles.size();
                if (debug) {
                    cols[i] = ColorRGBA.White;
                }

                for (TriangleData triangleData : triangles) {
                    tangent.addLocal(triangleData.tangent);
                    binormal.addLocal(triangleData.binormal);

                }
            }

            int blameVertex = vertexInfo.indices.get(0);

            if (tangent.length() < ZERO_TOLERANCE) {
                System.err.println("Shared tangent is zero for vertex " + blameVertex);
                // attempt to fix from binormal
                if (binormal.length() >= ZERO_TOLERANCE) {
                    binormal.cross(givenNormal, tangent);
                    tangent.normalizeLocal();
                } // if all fails use the tangent from the first triangle
                else {
                    tangent.set(firstTriangle.tangent);
                }
            } else {
                tangent.divideLocal(triangleCount);
            }

            tangentUnit.set(tangent);
            tangentUnit.normalizeLocal();
            if (Math.abs(Math.abs(tangentUnit.dot(givenNormal)) - 1)
                < ZERO_TOLERANCE) {
                System.err.println("Normal and tangent are parallel for vertex " + blameVertex);
            }

            if (!approxTangent) {
                if (binormal.length() < ZERO_TOLERANCE) {
                    System.err.println("Shared binormal is zero for vertex " + blameVertex);
                    // attempt to fix from tangent
                    if (tangent.length() >= ZERO_TOLERANCE) {
                        givenNormal.cross(tangent, binormal);
                        binormal.normalizeLocal();
                    } // if all fails use the binormal from the first triangle
                    else {
                        binormal.set(firstTriangle.binormal);
                    }
                } else {
                    binormal.divideLocal(triangleCount);
                }

                binormalUnit.set(binormal);
                binormalUnit.normalizeLocal();
                if (Math.abs(Math.abs(binormalUnit.dot(givenNormal)) - 1)
                    < ZERO_TOLERANCE) {
                    System.err
                            .println("Normal and binormal are parallel for vertex " + blameVertex);
                }

                if (Math.abs(Math.abs(binormalUnit.dot(tangentUnit)) - 1)
                    < ZERO_TOLERANCE) {
                    System.err
                            .println("Tangent and binormal are parallel for vertex " + blameVertex);
                }
            }

            Vector3f finalTangent = new Vector3f();
            Vector3f tmp = new Vector3f();
            for (int i : vertexInfo.indices) {
                if (approxTangent) {
                    // Gram-Schmidt orthogonalize
                    finalTangent.set(tangent).subtractLocal(
                            tmp.set(givenNormal).multLocal(givenNormal.dot(tangent)));
                    finalTangent.normalizeLocal();

                    wCoord = tmp.set(givenNormal).crossLocal(tangent).dot(binormal) < 0f ? -1f : 1f;

                    tangents.put((i * 4), finalTangent.x);
                    tangents.put((i * 4) + 1, finalTangent.y);
                    tangents.put((i * 4) + 2, finalTangent.z);
                    tangents.put((i * 4) + 3, wCoord);
                } else {
                    tangents.put((i * 4), tangent.x);
                    tangents.put((i * 4) + 1, tangent.y);
                    tangents.put((i * 4) + 2, tangent.z);
                    tangents.put((i * 4) + 3, wCoord);

                    //setInBuffer(binormal, binormals, i);
                }
            }
        }
        tangents.limit(tangents.capacity());
        // If the model already had a tangent buffer, replace it with the regenerated one
        mesh.clearBuffer(VertexBuffer.Type.Tangent);
        mesh.setBuffer(VertexBuffer.Type.Tangent, 4, tangents);

//        if(mesh.isAnimated()){
//            mesh.clearBuffer(Type.BindPoseNormal);
//            mesh.clearBuffer(Type.BindPosePosition);
//            mesh.clearBuffer(Type.BindPoseTangent);
//            mesh.generateBindPose(true);
//        }

        if (debug) {
            writeColorBuffer(vertices, cols, mesh);
        }
//        mesh.updateBound();
        mesh.updateCounts();
    }

    private static void bulkPut(VertexBuffer.Format format, Buffer buf1, Buffer buf2) {
        switch (format) {
            case Byte:
            case Half:
            case UnsignedByte:
                ((ByteBuffer) buf1).put((ByteBuffer) buf2);
                break;
            case Short:
            case UnsignedShort:

                ((ShortBuffer) buf1).put((ShortBuffer) buf2);
                break;

            case Int:
            case UnsignedInt:
                ((IntBuffer) buf1).put((IntBuffer) buf2);
                break;
            case Float:

                ((FloatBuffer) buf1).put((FloatBuffer) buf2);
                break;
            case Double:
                ((DoubleBuffer) buf1).put((DoubleBuffer) buf2);
                break;

            default:
                throw new UnsupportedOperationException("Unrecoginized buffer format: " + format);
        }
    }

    private static void putValue(VertexBuffer.Format format, Buffer buf1, Buffer buf2,int index) {
        switch (format) {
            case Byte:
            case Half:
            case UnsignedByte:
                byte b = ((ByteBuffer) buf2).get(index);
                ((ByteBuffer) buf1).put(b);
                break;
            case Short:
            case UnsignedShort:
                short s = ((ShortBuffer) buf2).get(index);
                ((ShortBuffer) buf1).put(s);
                break;

            case Int:
            case UnsignedInt:
                int i = ((IntBuffer) buf2).get(index);
                ((IntBuffer) buf1).put(i);
                break;
            case Float:
                float f = ((FloatBuffer) buf2).get(index);
                ((FloatBuffer) buf1).put(f);
                break;
            case Double:
                double d = ((DoubleBuffer) buf2).get(index);
                ((DoubleBuffer) buf1).put(d);
                break;
            default:
                throw new UnsupportedOperationException("Unrecoginized buffer format: " + format);
        }
    }

    private static List<VertexData> processTriangleStrip(Mesh mesh,
                                                         int[] index, Vector3f[] v, Vector2f[] t) {
        IndexBuffer indexBuffer = mesh.getIndexBuffer();
        FloatBuffer vertexBuffer = (FloatBuffer) mesh.getBuffer(VertexBuffer.Type.Position).getData();
        FloatBuffer textureBuffer = (FloatBuffer) mesh.getBuffer(VertexBuffer.Type.TexCoord).getData();

        List<VertexData> vertices = initVertexData(vertexBuffer.limit() / 3);

        index[0] = indexBuffer.get(0);
        index[1] = indexBuffer.get(1);

        populateFromBuffer(v[0], vertexBuffer, index[0]);
        populateFromBuffer(v[1], vertexBuffer, index[1]);

        populateFromBuffer(t[0], textureBuffer, index[0]);
        populateFromBuffer(t[1], textureBuffer, index[1]);

        for (int i = 2; i < indexBuffer.size(); i++) {
            index[2] = indexBuffer.get(i);
            BufferUtils.populateFromBuffer(v[2], vertexBuffer, index[2]);
            BufferUtils.populateFromBuffer(t[2], textureBuffer, index[2]);

            boolean isDegenerate = isDegenerateTriangle(v[0], v[1], v[2]);
            TriangleData triData = processTriangle(index, v, t);

            if (!isDegenerate) {
                vertices.get(index[0]).triangles.add(triData);
                vertices.get(index[1]).triangles.add(triData);
                vertices.get(index[2]).triangles.add(triData);
            }

            Vector3f vTemp = v[0];
            v[0] = v[1];
            v[1] = v[2];
            v[2] = vTemp;

            Vector2f tTemp = t[0];
            t[0] = t[1];
            t[1] = t[2];
            t[2] = tTemp;

            index[0] = index[1];
            index[1] = index[2];
        }

        return vertices;
    }

    private static List<VertexData> processTriangleFan(Mesh mesh,
                                                       int[] index, Vector3f[] v, Vector2f[] t) {
        IndexBuffer indexBuffer = mesh.getIndexBuffer();
        FloatBuffer vertexBuffer = (FloatBuffer) mesh.getBuffer(VertexBuffer.Type.Position).getData();
        FloatBuffer textureBuffer = (FloatBuffer) mesh.getBuffer(VertexBuffer.Type.TexCoord).getData();

        List<VertexData> vertices = initVertexData(vertexBuffer.limit() / 3);

        index[0] = indexBuffer.get(0);
        index[1] = indexBuffer.get(1);

        populateFromBuffer(v[0], vertexBuffer, index[0]);
        populateFromBuffer(v[1], vertexBuffer, index[1]);

        populateFromBuffer(t[0], textureBuffer, index[0]);
        populateFromBuffer(t[1], textureBuffer, index[1]);

        for (int i = 2; i < vertexBuffer.limit() / 3; i++) {
            index[2] = indexBuffer.get(i);
            populateFromBuffer(v[2], vertexBuffer, index[2]);
            populateFromBuffer(t[2], textureBuffer, index[2]);

            TriangleData triData = processTriangle(index, v, t);
            vertices.get(index[0]).triangles.add(triData);
            vertices.get(index[1]).triangles.add(triData);
            vertices.get(index[2]).triangles.add(triData);

            Vector3f vTemp = v[1];
            v[1] = v[2];
            v[2] = vTemp;

            Vector2f tTemp = t[1];
            t[1] = t[2];
            t[2] = tTemp;

            index[1] = index[2];
        }

        return vertices;
    }

    // check if the area is greater than zero
    private static boolean isDegenerateTriangle(Vector3f a, Vector3f b, Vector3f c) {
        return (a.subtract(b).cross(c.subtract(b))).lengthSquared() == 0;
    }

    public static TriangleData processTriangle(int[] index,
                                               Vector3f[] v, Vector2f[] t) {
        TempVars tmp = TempVars.get();
        try {
            Vector3f edge1 = tmp.vect1;
            Vector3f edge2 = tmp.vect2;
            Vector2f edge1uv = tmp.vect2d;
            Vector2f edge2uv = tmp.vect2d2;

            Vector3f tangent = tmp.vect3;
            Vector3f binormal = tmp.vect4;
            Vector3f normal = tmp.vect5;

            t[1].subtract(t[0], edge1uv);
            t[2].subtract(t[0], edge2uv);
            float det = edge1uv.x * edge2uv.y - edge1uv.y * edge2uv.x;

            boolean normalize = false;
            if (Math.abs(det) < ZERO_TOLERANCE) {
                System.err.println("Colinear uv coordinates for triangle ");
//                                + "[{0}, {1}, {2}]; tex0 = [{3}, {4}], "
//                                + "tex1 = [{5}, {6}], tex2 = [{7}, {8}]",
//                        new Object[]{index[0], index[1], index[2],
//                                t[0].x, t[0].y, t[1].x, t[1].y, t[2].x, t[2].y});
                det = 1;
                normalize = true;
            }

            v[1].subtract(v[0], edge1);
            v[2].subtract(v[0], edge2);

            tangent.set(edge1);
            tangent.normalizeLocal();
            binormal.set(edge2);
            binormal.normalizeLocal();

            if (Math.abs(Math.abs(tangent.dot(binormal)) - 1)
                    < ZERO_TOLERANCE) {
                System.err.println("Vertices are on the same line "
                                + "for triangle [" + index[0] + index[1] + index[2] + "]");
            }

            float factor = 1 / det;
            tangent.x = (edge2uv.y * edge1.x - edge1uv.y * edge2.x) * factor;
            tangent.y = (edge2uv.y * edge1.y - edge1uv.y * edge2.y) * factor;
            tangent.z = (edge2uv.y * edge1.z - edge1uv.y * edge2.z) * factor;
            if (normalize) {
                tangent.normalizeLocal();
            }

            binormal.x = (edge1uv.x * edge2.x - edge2uv.x * edge1.x) * factor;
            binormal.y = (edge1uv.x * edge2.y - edge2uv.x * edge1.y) * factor;
            binormal.z = (edge1uv.x * edge2.z - edge2uv.x * edge1.z) * factor;
            if (normalize) {
                binormal.normalizeLocal();
            }

            tangent.cross(binormal, normal);
            normal.normalizeLocal();

            return new TriangleData(
                    tangent.clone(),
                    binormal.clone(),
                    normal.clone());
        } finally {
            tmp.release();
        }
    }

    public static void setToleranceAngle(float angle) {
        if (angle < 0 || angle > 179) {
            throw new IllegalArgumentException(
                    "The angle must be between 0 and 179 degrees.");
        }
        toleranceDot = FastMath.cos(angle * FastMath.DEG_TO_RAD);
    }


    private static boolean approxEqual(Vector3f u, Vector3f v) {
        float tolerance = 1E-4f;
        return (FastMath.abs(u.x - v.x) < tolerance) &&
                (FastMath.abs(u.y - v.y) < tolerance) &&
                (FastMath.abs(u.z - v.z) < tolerance);
    }

    private static boolean approxEqual(Vector2f u, Vector2f v) {
        float tolerance = 1E-4f;
        return (FastMath.abs(u.x - v.x) < tolerance) &&
                (FastMath.abs(u.y - v.y) < tolerance);
    }

    private static class VertexInfo {
        public final Vector3f position;
        public final Vector3f normal;
        public final Vector2f texCoord;
        public final List<Integer> indices = new ArrayList<>();

        public VertexInfo(Vector3f position, Vector3f normal, Vector2f texCoord) {
            this.position = position;
            this.normal = normal;
            this.texCoord = texCoord;
        }
    }

    /**
     * Collects all the triangle data for one vertex.
     */
    private static class VertexData {
        public final List<TriangleData> triangles = new ArrayList<>();

        public VertexData() {
        }
    }

    private static void writeColorBuffer(List<VertexData> vertices, ColorRGBA[] cols, Mesh mesh) {
        FloatBuffer colors = BufferUtils.createFloatBuffer(vertices.size() * 4);
        colors.rewind();
        for (ColorRGBA color : cols) {
            colors.put(color.r);
            colors.put(color.g);
            colors.put(color.b);
            colors.put(color.a);
        }
        mesh.clearBuffer(VertexBuffer.Type.Color);
        mesh.setBuffer(VertexBuffer.Type.Color, 4, colors);
    }

    private static int parity(Vector3f n1, Vector3f n) {
        if (n1.dot(n) < 0) {
            return -1;
        } else {
            return 1;
        }

    }

    public static Mesh genTbnLines(Mesh mesh, float scale) {
        if (mesh.getBuffer(VertexBuffer.Type.Tangent) == null) {
            return genNormalLines(mesh, scale);
        } else {
            return genTangentLines(mesh, scale);
        }
    }

    public static Mesh genNormalLines(Mesh mesh, float scale) {
        FloatBuffer vertexBuffer = (FloatBuffer) mesh.getBuffer(VertexBuffer.Type.Position).getData();
        FloatBuffer normalBuffer = (FloatBuffer) mesh.getBuffer(VertexBuffer.Type.Normal).getData();

        ColorRGBA originColor = ColorRGBA.White;
        ColorRGBA normalColor = ColorRGBA.Blue;

        Mesh lineMesh = new Mesh();
        lineMesh.setMode(Mesh.Mode.Lines);

        Vector3f origin = new Vector3f();
        Vector3f point = new Vector3f();

        FloatBuffer lineVertex = BufferUtils.createFloatBuffer(vertexBuffer.limit() * 2);
        FloatBuffer lineColor = BufferUtils.createFloatBuffer(vertexBuffer.limit() / 3 * 4 * 2);

        for (int i = 0; i < vertexBuffer.limit() / 3; i++) {
            populateFromBuffer(origin, vertexBuffer, i);
            populateFromBuffer(point, normalBuffer, i);

            int index = i * 2;

            setInBuffer(origin, lineVertex, index);
            setInBuffer(originColor, lineColor, index);

            point.multLocal(scale);
            point.addLocal(origin);
            setInBuffer(point, lineVertex, index + 1);
            setInBuffer(normalColor, lineColor, index + 1);
        }

        lineMesh.setBuffer(VertexBuffer.Type.Position, 3, lineVertex);
        lineMesh.setBuffer(VertexBuffer.Type.Color, 4, lineColor);

        lineMesh.setStatic();
        //lineMesh.setInterleaved();
         return lineMesh;
    }

    private static Mesh genTangentLines(Mesh mesh, float scale) {
        FloatBuffer vertexBuffer = (FloatBuffer) mesh.getBuffer(VertexBuffer.Type.Position).getData();
        FloatBuffer normalBuffer = (FloatBuffer) mesh.getBuffer(VertexBuffer.Type.Normal).getData();
        FloatBuffer tangentBuffer = (FloatBuffer) mesh.getBuffer(VertexBuffer.Type.Tangent).getData();

        FloatBuffer binormalBuffer = null;
        if (mesh.getBuffer(VertexBuffer.Type.Binormal) != null) {
            binormalBuffer = (FloatBuffer) mesh.getBuffer(VertexBuffer.Type.Binormal).getData();
        }

        ColorRGBA originColor = ColorRGBA.White;
        ColorRGBA tangentColor = ColorRGBA.Red;
        ColorRGBA binormalColor = ColorRGBA.Green;
        ColorRGBA normalColor = ColorRGBA.Blue;

        Mesh lineMesh = new Mesh();
        lineMesh.setMode(Mesh.Mode.Lines);

        Vector3f origin = new Vector3f();
        Vector3f point = new Vector3f();
        Vector3f tangent = new Vector3f();
        Vector3f normal = new Vector3f();

        IntBuffer lineIndex = BufferUtils.createIntBuffer(vertexBuffer.limit() / 3 * 6);
        FloatBuffer lineVertex = BufferUtils.createFloatBuffer(vertexBuffer.limit() * 4);
        FloatBuffer lineColor = BufferUtils.createFloatBuffer(vertexBuffer.limit() / 3 * 4 * 4);

        boolean hasParity = mesh.getBuffer(VertexBuffer.Type.Tangent).getNumComponents() == 4;
        float tangentW = 1;

        for (int i = 0; i < vertexBuffer.limit() / 3; i++) {
            populateFromBuffer(origin, vertexBuffer, i);
            populateFromBuffer(normal, normalBuffer, i);

            if (hasParity) {
                tangent.x = tangentBuffer.get(i * 4);
                tangent.y = tangentBuffer.get(i * 4 + 1);
                tangent.z = tangentBuffer.get(i * 4 + 2);
                tangentW = tangentBuffer.get(i * 4 + 3);
            } else {
                populateFromBuffer(tangent, tangentBuffer, i);
            }

            int index = i * 4;

            int id = i * 6;
            lineIndex.put(id, index);
            lineIndex.put(id + 1, index + 1);
            lineIndex.put(id + 2, index);
            lineIndex.put(id + 3, index + 2);
            lineIndex.put(id + 4, index);
            lineIndex.put(id + 5, index + 3);

            setInBuffer(origin, lineVertex, index);
            setInBuffer(originColor, lineColor, index);

            point.set(tangent);
            point.multLocal(scale);
            point.addLocal(origin);
            setInBuffer(point, lineVertex, index + 1);
            setInBuffer(tangentColor, lineColor, index + 1);

            // wvBinormal = cross(wvNormal, wvTangent) * -inTangent.w

            if (binormalBuffer == null) {
                normal.cross(tangent, point);
                point.multLocal(-tangentW);
                point.normalizeLocal();
            } else {
                populateFromBuffer(point, binormalBuffer, i);
            }

            point.multLocal(scale);
            point.addLocal(origin);
            setInBuffer(point, lineVertex, index + 2);
            setInBuffer(binormalColor, lineColor, index + 2);

            point.set(normal);
            point.multLocal(scale);
            point.addLocal(origin);
            setInBuffer(point, lineVertex, index + 3);
            setInBuffer(normalColor, lineColor, index + 3);
        }

        lineMesh.setBuffer(VertexBuffer.Type.Index, 1, lineIndex);
        lineMesh.setBuffer(VertexBuffer.Type.Position, 3, lineVertex);
        lineMesh.setBuffer(VertexBuffer.Type.Color, 4, lineColor);

        lineMesh.setStatic();
        //lineMesh.setInterleaved();
        return lineMesh;
    }
}
