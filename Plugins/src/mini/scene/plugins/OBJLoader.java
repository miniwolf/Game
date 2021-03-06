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

    private final List<Vector3f> verts = new ArrayList<>();
    private final List<Vector2f> texCoords = new ArrayList<>();
    private final List<Vector3f> norms = new ArrayList<>();

    protected final List<Face> faces = new ArrayList<>();
    private final Map<String, List<Face>> matFaces = new HashMap<>();
    private final Map<Vertex, Integer> vertIndexMap = new HashMap<>(100);
    private String currentMatName;
    private int curIndex = 0;
    private int geomIndex = 0;

    private Scanner scan;
    protected ModelKey key;
    private Map<String, Material> matList;

    private String objName;
    private List<Vertex> vertList = new ArrayList<>();
    private AssetManager assetManager;

    protected static class Face {
        Vertex[] verticies;
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
        if (folderName != null && folderName.length() > 0 && objName
                .startsWith(folderName)) {
            objName = objName.substring(folderName.length());
        }

        Node objNode = new Node(objName + "-objnode");

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
            // generate final geometry
            Geometry geom = createGeometry(faces, null);
            objNode.attachChild(geom);
        }

        // only 1 geometry, so no need to send node
        return objNode.getQuantity() == 1 ? objNode.getChild(0) : objNode;
    }

    private void reset() {
        verts.clear();
        texCoords.clear();
        norms.clear();
        faces.clear();
        matFaces.clear();

        vertIndexMap.clear();

        currentMatName = null;
        matList = null;
        curIndex = 0;
        geomIndex = 0;
        scan = null;
    }

    private void findVertexIndex(Vertex vert) {
        Integer index = vertIndexMap.get(vert);
        if (index != null) {
            vert.index = index;
        } else {
            vert.index = curIndex++;
            vertIndexMap.put(vert, vert.index);
        }
    }

    private Face[] quadToTriangle(Face f) {
        assert f.verticies.length == 4;

        Face[] t = new Face[]{new Face(), new Face()};
        t[0].verticies = new Vertex[3];
        t[1].verticies = new Vertex[3];

        Vertex v0 = f.verticies[0];
        Vertex v1 = f.verticies[1];
        Vertex v2 = f.verticies[2];
        Vertex v3 = f.verticies[3];

        // find the pair of verticies that is closest to each over
        // v0 and v2
        // OR
        // v1 and v3
        float d1 = v0.v.distanceSquared(v2.v);
        float d2 = v1.v.distanceSquared(v3.v);
        if (d1 < d2) {
            // put an edge in v0, v2
            t[0].verticies[0] = v0;
            t[0].verticies[1] = v1;
            t[0].verticies[2] = v3;

            t[1].verticies[0] = v1;
            t[1].verticies[1] = v2;
            t[1].verticies[2] = v3;
        } else {
            // put an edge in v1, v3
            t[0].verticies[0] = v0;
            t[0].verticies[1] = v1;
            t[0].verticies[2] = v2;

            t[1].verticies[0] = v0;
            t[1].verticies[1] = v2;
            t[1].verticies[2] = v3;
        }

        return t;
    }

    private void readFace() {
        Face f = new Face();
        vertList.clear();

        String line = scan.nextLine().trim();
        String[] verticies = line.split("\\s+");
        for (String vertex : verticies) {
            int v = 0;
            int vt = 0;
            int vn = 0;

            String[] split = vertex.split("/");
            if (split.length == 1) {
                v = Integer.parseInt(split[0].trim());
            } else if (split.length == 2) {
                v = Integer.parseInt(split[0].trim());
                vt = Integer.parseInt(split[1].trim());
            } else if (split.length == 3 && !split[1].equals("")) {
                v = Integer.parseInt(split[0].trim());
                vt = Integer.parseInt(split[1].trim());
                vn = Integer.parseInt(split[2].trim());
            } else if (split.length == 3) {
                v = Integer.parseInt(split[0].trim());
                vn = Integer.parseInt(split[2].trim());
            }

            if (v < 0) {
                v = verts.size() + v + 1;
            }
            if (vt < 0) {
                vt = texCoords.size() + vt + 1;
            }
            if (vn < 0) {
                vn = norms.size() + vn + 1;
            }

            Vertex vx = new Vertex();
            vx.v = verts.get(v - 1);

            if (vt > 0) {
                vx.vt = texCoords.get(vt - 1);
            }

            if (vn > 0) {
                vx.vn = norms.get(vn - 1);
            }

            vertList.add(vx);
        }

        if (vertList.size() > 4 || vertList.size() <= 2) {
            System.err.println("Edge or polygon detected in OBJ. Ignored.");
            return;
        }

        f.verticies = new Vertex[vertList.size()];
        for (int i = 0; i < vertList.size(); i++) {
            f.verticies[i] = vertList.get(i);
        }

        if (matList != null && matFaces.containsKey(currentMatName)) {
            matFaces.get(currentMatName).add(f);
        } else {
            faces.add(f); // faces that belong to the default material
        }
    }

    private Vector3f readVector3() {
        Vector3f v = new Vector3f();

        v.set(Float.parseFloat(scan.next()),
              Float.parseFloat(scan.next()),
              Float.parseFloat(scan.next()));

        return v;
    }

    private Vector2f readVector2() {
        Vector2f v = new Vector2f();

        String line = scan.nextLine().trim();
        String[] split = line.split("\\s+");
        v.setX(Float.parseFloat(split[0].trim()));
        v.setY(Float.parseFloat(split[1].trim()));

        return v;
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
            // create face lists for every material
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
            // skip entire comment until next line
            return nextStatement();
        } else if (cmd.equals("v")) {
            // vertex position
            verts.add(readVector3());
        } else if (cmd.equals("vn")) {
            // vertex normal
            norms.add(readVector3());
        } else if (cmd.equals("vt")) {
            // texture coordinate
            texCoords.add(readVector2());
        } else if (cmd.equals("f")) {
            // face, can be triangle, quad, or polygon (unsupported)
            readFace();
        } else if (cmd.equals("usemtl")) {
            // use material from MTL lib for the following faces
            currentMatName = scan.next();
            if (!matList.containsKey(currentMatName)) {
                throw new IOException("Cannot locate material " + currentMatName + " in MTL file!");
            }

        } else if (cmd.equals("mtllib")) {
            // specify MTL lib to use for this OBJ file
            String mtllib = scan.nextLine().trim();
            loadMtlLib(mtllib);
        } else if (cmd.equals("s") || cmd.equals("g")) {
            return nextStatement();
        } else {
            // skip entire command until next line
            System.err.println("Unknown statement in OBJ! " + cmd);
            return nextStatement();
        }

        return true;
    }

    private Geometry createGeometry(List<Face> faceList, String matName) throws IOException {
        if (faceList.isEmpty()) {
            throw new IOException("No geometry data to generate mesh");
        }

        // Create mesh from the faces
        Mesh mesh = constructMesh(faceList);

        Geometry geom = new Geometry(objName + "-geom-" + (geomIndex++), mesh);

        Material material = null;
        if (matName != null && matList != null) {
            // Get material from material list
            material = matList.get(matName);
        }
        if (material == null) {
            // create default material
            material = new Material(assetManager, "MatDefs/Light/Lighting.minid");
            material.setFloat("Shininess", 64);
        }
        geom.setMaterial(material);
//        if (material.isTransparent())
//            geom.setQueueBucket(Bucket.Transparent);
//        else
        geom.setQueueBucket(RenderQueue.Bucket.Opaque);

        if (material.getMaterialDef().getName().contains("Lighting")
            && mesh.getFloatBuffer(VertexBuffer.Type.Normal) == null) {
            System.err.println("OBJ mesh " + geom.getName() + " doesn't contain normals! "
                               + "It might not display correctly");
        }

        return geom;
    }

    private Mesh constructMesh(List<Face> faceList) {
        Mesh m = new Mesh();
        m.setMode(Mesh.Mode.Triangles);

        boolean hasTexCoord = false;
        boolean hasNormals = false;

        List<Face> newFaces = new ArrayList<>(faceList.size());
        for (Face f : faceList) {
            for (Vertex v : f.verticies) {
                findVertexIndex(v);

                if (!hasTexCoord && v.vt != null) {
                    hasTexCoord = true;
                }
                if (!hasNormals && v.vn != null) {
                    hasNormals = true;
                }
            }

            if (f.verticies.length == 4) {
                Face[] t = quadToTriangle(f);
                newFaces.add(t[0]);
                newFaces.add(t[1]);
            } else {
                newFaces.add(f);
            }
        }

        FloatBuffer posBuf = BufferUtils.createFloatBuffer(vertIndexMap.size() * 3);
        FloatBuffer normBuf = null;
        FloatBuffer tcBuf = null;

        if (hasNormals) {
            normBuf = BufferUtils.createFloatBuffer(vertIndexMap.size() * 3);
            m.setBuffer(VertexBuffer.Type.Normal, 3, normBuf);
        }
        if (hasTexCoord) {
            tcBuf = BufferUtils.createFloatBuffer(vertIndexMap.size() * 2);
            m.setBuffer(VertexBuffer.Type.TexCoord, 2, tcBuf);
        }

        IndexBuffer indexBuf;
        if (vertIndexMap.size() >= 65536) {
            // too many verticies: use intbuffer instead of shortbuffer
            IntBuffer ib = BufferUtils.createIntBuffer(newFaces.size() * 3);
            m.setBuffer(VertexBuffer.Type.Index, 3, ib);
            indexBuf = new IndexIntBuffer(ib);
        } else {
            ShortBuffer sb = BufferUtils.createShortBuffer(newFaces.size() * 3);
            m.setBuffer(VertexBuffer.Type.Index, 3, sb);
            indexBuf = new IndexShortBuffer(sb);
        }

        int numFaces = newFaces.size();
        for (int i = 0; i < numFaces; i++) {
            Face f = newFaces.get(i);
            if (f.verticies.length != 3) {
                continue;
            }

            Vertex v0 = f.verticies[0];
            Vertex v1 = f.verticies[1];
            Vertex v2 = f.verticies[2];

            posBuf.position(v0.index * 3);
            posBuf.put(v0.v.x).put(v0.v.y).put(v0.v.z);
            posBuf.position(v1.index * 3);
            posBuf.put(v1.v.x).put(v1.v.y).put(v1.v.z);
            posBuf.position(v2.index * 3);
            posBuf.put(v2.v.x).put(v2.v.y).put(v2.v.z);

            if (normBuf != null) {
                if (v0.vn != null) {
                    normBuf.position(v0.index * 3);
                    normBuf.put(v0.vn.x).put(v0.vn.y).put(v0.vn.z);
                    normBuf.position(v1.index * 3);
                    normBuf.put(v1.vn.x).put(v1.vn.y).put(v1.vn.z);
                    normBuf.position(v2.index * 3);
                    normBuf.put(v2.vn.x).put(v2.vn.y).put(v2.vn.z);
                }
            }

            if (tcBuf != null) {
                if (v0.vt != null) {
                    tcBuf.position(v0.index * 2);
                    tcBuf.put(v0.vt.x).put(v0.vt.y);
                    tcBuf.position(v1.index * 2);
                    tcBuf.put(v1.vt.x).put(v1.vt.y);
                    tcBuf.position(v2.index * 2);
                    tcBuf.put(v2.vt.x).put(v2.vt.y);
                }
            }

            int index = i * 3; // current face * 3 = current index
            indexBuf.put(index, v0.index);
            indexBuf.put(index + 1, v1.index);
            indexBuf.put(index + 2, v2.index);
        }

        m.setBuffer(VertexBuffer.Type.Position, 3, posBuf);
        // index buffer and others were set on creation

        m.setStatic();
        m.updateBound();
        m.updateCounts();
        //m.setInterleaved();

        // clear data generated face statements
        // to prepare for next mesh
        vertIndexMap.clear();
        curIndex = 0;

        return m;
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
            return this.v == other.v || (this.v != null && this.v.equals(other.v)) && (
                    this.vt == other.vt || (this.vt != null && this.vt.equals(other.vt)) && (
                            this.vn == other.vn || (this.vn != null && this.vn.equals(other.vn))));
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
}