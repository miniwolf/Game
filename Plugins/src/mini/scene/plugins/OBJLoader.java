package mini.scene.plugins;

import mini.asset.AssetInfo;
import mini.asset.AssetKey;
import mini.asset.AssetLoader;
import mini.asset.AssetManager;
import mini.asset.ModelKey;
import mini.material.Material;
import mini.math.Vector2f;
import mini.math.Vector3f;
import mini.renderer.queue.RenderQueue;
import mini.scene.Geometry;
import mini.scene.Mesh;
import mini.scene.Node;
import mini.scene.Spatial;
import mini.scene.VertexBuffer;
import mini.scene.mesh.IndexBuffer;
import mini.scene.mesh.IndexIntBuffer;
import mini.scene.mesh.IndexShortBuffer;
import mini.utils.BufferUtils;
import mini.utils.MyFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Reads OBJ format models.
 */
public final class OBJLoader implements AssetLoader {

    protected final List<Vector3f> vertices = new ArrayList<>();
    protected final List<Vector2f> texCoords = new ArrayList<>();
    protected final List<Vector3f> normals = new ArrayList<>();

    protected final List<Face> faces = new ArrayList<>();
    protected final Map<String, List<Face>> matFaces = new HashMap<>();

    protected String currentMatName;
    protected String currentObjectName;

    protected final Map<Vertex, Integer> vertIndexMap = new HashMap<>(100);
    protected final Map<Integer, Vertex> indexVertMap = new HashMap<>(100);
    protected int currentIndex = 0;
    protected int objectIndex = 0;
    protected int geometryIndex = 0;

    protected Scanner scan;
    protected ModelKey key;
    protected Map<String, Material> matList;

    protected String objName;
    protected Node objNode;
    private List<Vertex> vertexList = new ArrayList<>();
    private AssetManager assetManager;

    protected static class Face {
        Vertex[] vertices;
    }

    public Object load(AssetInfo info) throws IOException {
        reset();

        if (!(info.getKey() instanceof ModelKey)) {
            throw new IllegalArgumentException("Model assets must be loaded using a ModelKey");
        }

        key = (ModelKey) info.getKey();
        assetManager = info.getManager();
        objName = key.getName();

        String folderName = key.getFolder();
        String ext = key.getExtension();
        objName = objName.substring(0, objName.length() - ext.length() - 1);
        if (folderName != null && folderName.length() > 0 && objName.startsWith(folderName)) {
            objName = objName.substring(folderName.length());
        }

        objNode = new Node(objName + "-objnode");

        try (InputStream in = info.openStream()) {
            scan = new Scanner(in);
            scan.useLocale(Locale.US);

            while (readLine()) {
            }
        }

        if (matFaces.size() > 0) {
            for (Map.Entry<String, List<Face>> entry : matFaces.entrySet()) {
                List<Face> materialFaces = entry.getValue();
                if (materialFaces.size() > 0) {
                    Geometry geom = createGeometry(materialFaces, entry.getKey());
                    objNode.attachChild(geom);
                }
            }
        } else if (faces.size() > 0) {
            // Generate final geometry.
            Geometry geom = createGeometry(faces, null);
            objNode.attachChild(geom);
        }

        // Only 1 geometry, so no need to send node.
        return objNode.getQuantity() == 1 ? objNode.getChild(0) : objNode;
    }

    public void reset() {
        vertices.clear();
        texCoords.clear();
        normals.clear();
        faces.clear();
        matFaces.clear();

        vertIndexMap.clear();
        indexVertMap.clear();

        currentMatName = null;
        matList = null;
        currentIndex = 0;
        geometryIndex = 0;
        scan = null;
    }

    private void findVertexIndex(Vertex vertex) {
        Integer index = vertIndexMap.get(vertex);

        if (index != null) {
            vertex.index = index;
            return;
        }

        vertex.index = currentIndex++;
        vertIndexMap.put(vertex, vertex.index);
        indexVertMap.put(vertex.index, vertex);
    }

    private Face[] quadToTriangle(Face f) {
        assert f.vertices.length == 4;

        Face[] t = new Face[] { new Face(), new Face() };

        Vertex v0 = f.vertices[0];
        Vertex v1 = f.vertices[1];
        Vertex v2 = f.vertices[2];
        Vertex v3 = f.vertices[3];

        // Find the pair of verticies that is closest to each other.
        // v0 and v2 OR v1 and v3.
        float d1 = v0.v.distanceSquared(v2.v);
        float d2 = v1.v.distanceSquared(v3.v);
        if (d1 < d2) {
            // Put an edge in v0, v2.
            t[0].vertices = new Vertex[] { v0, v1, v3 };
            t[1].vertices = new Vertex[] { v1, v2, v3 };
        } else {
            // Put an edge in v1, v3.
            t[0].vertices = new Vertex[] { v0, v1, v2 };
            t[1].vertices = new Vertex[] { v0, v2, v3 };
        }

        return t;
    }

    private void readFace() {
        Face f = new Face();
        vertexList.clear();

        String line = scan.nextLine().trim();
        String[] verticies = line.split("\\s+");
        for (String vertex : verticies) {
            int v = 0;
            int vt = 0;
            int vn = 0;

            String[] split = vertex.split("/");
            if (split.length == 1) {
                v = readVertex(split[0]);
            } else if (split.length == 2) {
                v = readVertex(split[0]);
                vt = readVertex(split[1]);
            } else if (split.length == 3 && !split[1].equals("")) {
                v = readVertex(split[0]);
                vt = readVertex(split[1]);
                vn = readVertex(split[2]);
            } else if (split.length == 3) {
                v = readVertex(split[0]);
                vn = readVertex(split[2]);
            }

            if (v < 0) {
                v = vertices.size() + v + 1;
            }
            if (vt < 0) {
                vt = texCoords.size() + vt + 1;
            }
            if (vn < 0) {
                vn = normals.size() + vn + 1;
            }

            Vertex vx = new Vertex();
            vx.v = vertices.get(v - 1);

            if (vt > 0) {
                vx.vt = texCoords.get(vt - 1);
            }

            if (vn > 0) {
                vx.vn = normals.get(vn - 1);
            }

            vertexList.add(vx);
        }

        if (vertexList.size() > 4 || vertexList.size() <= 2) {
            System.err.println("Edge or polygon detected in OBJ. Ignored.");
            return;
        }

        f.vertices = new Vertex[vertexList.size()];
        for (int i = 0; i < vertexList.size(); i++) {
            f.vertices[i] = vertexList.get(i);
        }

        if (matList != null && matFaces.containsKey(currentMatName)) {
            matFaces.get(currentMatName).add(f);
            return;
        }

        faces.add(f); // Faces that belong to the default material.
    }

    private Integer readVertex(String vertex) {
        return Integer.parseInt(vertex.trim());
    }

    private Vector3f readVector3() {
        Float x = Float.parseFloat(scan.next());
        Float y = Float.parseFloat(scan.next());
        Float z = Float.parseFloat(scan.next());

        return new Vector3f(x, y, z);
    }

    private Vector2f readVector2() {
        String[] split = scan.nextLine().trim().split("\\s+");
        Float x = Float.parseFloat(split[0].trim());
        Float y = Float.parseFloat(split[1].trim());

        return new Vector2f(x, y);
    }

    private void loadMtlLib(String name) throws IOException {
        if (!name.toLowerCase().endsWith(".mtl")) {
            throw new IOException("Expected .mtl file! Got: " + name);
        }

        // NOTE: Cut off any relative/absolute paths
        name = new MyFile(name).getName();
        AssetKey<Map<String, Material>> mtlKey = new AssetKey<>(key.getFolder() + name);
        try {
            matList = assetManager.loadAsset(mtlKey);
        } catch (Exception ex) {
            System.err.println("Cannot locate " + name + " for model " + key);
        }

        if (matList != null) {
            // Create face lists for every material.
            for (String matName : matList.keySet()) {
                matFaces.put(matName, new ArrayList<>());
            }
        }
    }

    private boolean nextStatement() {
        try {
            scan.skip(".*\r?\n");
            return true;
        } catch (NoSuchElementException ex) {
            // EOF
            return false;
        }
    }

    private boolean readLine() throws IOException {
        if (!scan.hasNext()) {
            return false;
        }

        String cmd = scan.next();
        if (cmd.startsWith("#")) {
            // Skip entire comment until next line.
            return nextStatement();
        } else if (cmd.equals("v")) {
            // Vertex position.
            vertices.add(readVector3());
        } else if (cmd.equals("vn")) {
            // Vertex normal.
            normals.add(readVector3());
        } else if (cmd.equals("vt")) {
            // Texture coordinates.
            texCoords.add(readVector2());
        } else if (cmd.equals("f")) {
            // Face, can be triangle, quad, or polygon (unsupported).
            readFace();
        } else if (cmd.equals("usemtl")) {
            // Use material from MTL lib for the following faces.
            currentMatName = scan.next();
            if (!matList.containsKey(currentMatName)) {
                throw new IOException("Cannot locate material " + currentMatName + " in MTL file!");
            }
        } else if (cmd.equals("mtllib")) {
            // Specify MTL lib to use for this OBJ file.
            String mtllib = scan.nextLine().trim();
            loadMtlLib(mtllib);
        } else if (cmd.equals("s") || cmd.equals("g")) {
            return nextStatement();
        } else {
            // Skip entire command until next line.
            System.err.println("Unknown statement in OBJ! " + cmd);
            return nextStatement();
        }

        return true;
    }

    private Geometry createGeometry(List<Face> faceList, String matName) throws IOException {
        if (faceList.isEmpty()) {
            throw new IOException("No geometry data to generate mesh");
        }

        // Create mesh from the faces.
        Mesh mesh = constructMesh(faceList);
        Geometry geometry = new Geometry(objName + "-geom-" + (geometryIndex++), mesh);
        Material material = null;

        if (matName != null && matList != null) {
            // Get material from material list.
            material = matList.get(matName);
        }

        if (material == null) {
            // Create default material.
            material = new Material(assetManager, "MatDefs/Light/Lighting.minid");
            material.setFloat("Shininess", 64);
        }

        geometry.setMaterial(material);
        geometry.setQueueBucket(RenderQueue.Bucket.Opaque);

        if (material.getMaterialDef().getName().contains("Lighting")
                && mesh.getFloatBuffer(VertexBuffer.Type.Normal) == null) {
            System.err.println(
                    "OBJ mesh " + geometry.getName() + " doesn't contain normals! " + "It might not display correctly");
        }

        return geometry;
    }

    protected Mesh constructMesh(List<Face> faceList) {
        Mesh mesh = new Mesh();
        mesh.setMode(Mesh.Mode.Triangles);

        boolean hasTexCoord = false;
        boolean hasNormals = false;

        List<Face> newFaces = new ArrayList<>(faceList.size());
        for (Face f : faceList) {
            for (Vertex v : f.vertices) {
                findVertexIndex(v);

                if (!hasTexCoord && v.vt != null) {
                    hasTexCoord = true;
                }

                if (!hasNormals && v.vn != null) {
                    hasNormals = true;
                }
            }

            if (f.vertices.length == 4) {
                Face[] t = quadToTriangle(f);
                newFaces.add(t[0]);
                newFaces.add(t[1]);
            } else {
                newFaces.add(f);
            }
        }

        FloatBuffer positionBuffer = BufferUtils.createFloatBuffer(vertIndexMap.size() * 3);
        FloatBuffer normalBuffer = hasNormals ? createNormalBuffer(mesh) : null;
        FloatBuffer texCoordBuffer = hasTexCoord ? createTextureCoordinateBuffer(mesh) : null;
        IndexBuffer indexBuffer = createIndexBuffer(mesh, newFaces);

        int numFaces = newFaces.size();
        for (int i = 0; i < numFaces; i++) {
            Face f = newFaces.get(i);

            if (f.vertices.length != 3) {
                continue;
            }

            Vertex v0 = f.vertices[0];
            Vertex v1 = f.vertices[1];
            Vertex v2 = f.vertices[2];

            positionBuffer.position(v0.index * 3);
            positionBuffer.put(v0.v.x).put(v0.v.y).put(v0.v.z);
            positionBuffer.position(v1.index * 3);
            positionBuffer.put(v1.v.x).put(v1.v.y).put(v1.v.z);
            positionBuffer.position(v2.index * 3);
            positionBuffer.put(v2.v.x).put(v2.v.y).put(v2.v.z);

            if (normalBuffer != null) {
                if (v0.vn != null) {
                    normalBuffer.position(v0.index * 3);
                    normalBuffer.put(v0.vn.x).put(v0.vn.y).put(v0.vn.z);
                    normalBuffer.position(v1.index * 3);
                    normalBuffer.put(v1.vn.x).put(v1.vn.y).put(v1.vn.z);
                    normalBuffer.position(v2.index * 3);
                    normalBuffer.put(v2.vn.x).put(v2.vn.y).put(v2.vn.z);
                }
            }

            if (texCoordBuffer != null) {
                if (v0.vt != null) {
                    texCoordBuffer.position(v0.index * 2);
                    texCoordBuffer.put(v0.vt.x).put(v0.vt.y);
                    texCoordBuffer.position(v1.index * 2);
                    texCoordBuffer.put(v1.vt.x).put(v1.vt.y);
                    texCoordBuffer.position(v2.index * 2);
                    texCoordBuffer.put(v2.vt.x).put(v2.vt.y);
                }
            }

            int index = i * 3; // Current face * 3 = current index.
            indexBuffer.put(index, v0.index);
            indexBuffer.put(index + 1, v1.index);
            indexBuffer.put(index + 2, v2.index);
        }

        mesh.setBuffer(VertexBuffer.Type.Position, 3, positionBuffer);
        // Index buffer and others were set on creation.

        mesh.setStatic();
        mesh.updateBound();
        mesh.updateCounts();

        // Clear data generated face statements
        // to prepare for next mesh.
        vertIndexMap.clear();
        indexVertMap.clear();
        currentIndex = 0;

        return mesh;
    }

    private FloatBuffer createNormalBuffer(Mesh mesh) {
        FloatBuffer normalBuffer = BufferUtils.createFloatBuffer(vertIndexMap.size() * 3);
        mesh.setBuffer(VertexBuffer.Type.Normal, 3, normalBuffer);
        return normalBuffer;
    }

    private FloatBuffer createTextureCoordinateBuffer(Mesh mesh) {
        FloatBuffer texCoordBuffer = BufferUtils.createFloatBuffer(vertIndexMap.size() * 2);
        mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, texCoordBuffer);
        return texCoordBuffer;
    }

    private IndexBuffer createIndexBuffer(Mesh mesh, List<Face> faces) {
        IndexBuffer indexBuffer;
        // Use shortbuffer if number of vertices is small enough.
        if (vertIndexMap.size() <= 65535) {
            ShortBuffer shortBuffer = BufferUtils.createShortBuffer(faces.size() * 3);
            mesh.setBuffer(VertexBuffer.Type.Index, 3, shortBuffer);
            indexBuffer = new IndexShortBuffer(shortBuffer);
        } else {
            IntBuffer intBuffer = BufferUtils.createIntBuffer(faces.size() * 3);
            mesh.setBuffer(VertexBuffer.Type.Index, 3, intBuffer);
            indexBuffer = new IndexIntBuffer(intBuffer);
        }
        return indexBuffer;
    }

    protected static class Vertex {

        Vector3f v;
        Vector2f vt;
        Vector3f vn;
        int index;

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            final Vertex other = (Vertex) obj;

            boolean verticesAreEqual = this.v == other.v || (this.v != null && this.v.equals(other.v));
            boolean tangentsAreEqual = this.vt == other.vt || (this.vt != null && this.vt.equals(other.vt));
            boolean normalsAreEqual = this.vn == other.vn || (this.vn != null && this.vn.equals(other.vn));

            return verticesAreEqual && (tangentsAreEqual && normalsAreEqual);
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 53 * hash + (this.v != null ? this.v.hashCode() : 0);
            hash = 53 * hash + (this.vt != null ? this.vt.hashCode() : 0);
            hash = 53 * hash + (this.vn != null ? this.vn.hashCode() : 0);
            return hash;
        }
    }

    protected class ObjectGroup {

        final String objectName;

        public ObjectGroup(String objectName) {
            this.objectName = objectName;
        }

        public Spatial createGeometry() {
            Node groupNode = new Node(objectName);
            if (objectName == null) {
                groupNode.setName("Model");
            }
            return groupNode;
        }
    }
}