package mini.objConverter;

import mini.material.Material;
import mini.math.Vector2f;
import mini.math.Vector3f;
import mini.utils.MyFile;
import scala.collection.immutable.Map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class OBJFileLoader {
    private static java.util.Map<String, List<Face>> matFaces = new HashMap<>();
    private static final List<Face> faces = new ArrayList<>();
    private static List<Vertex> vertList = new ArrayList<>();
    private static List<Vertex> vertices = new ArrayList<>();
    private static List<Vector2f> textures = new ArrayList<>();
    private static List<Vector3f> normals = new ArrayList<>();
    private static Map<String, Material> matList;
    private static String currentMatName;

    protected static class Face {
        Vertex[] vertices;
    }

    public static ModelData loadOBJ(MyFile objFile) {
        List<String> lines = null;
        matFaces = new HashMap<>();
        try {
            lines = objFile.getLines();
        } catch (Exception e1) {
            e1.printStackTrace();
            System.err.println("Couldn't find model file: " + objFile);
            System.exit(-1);
        }
        List<Integer> indices = new ArrayList<>();
        Iterator<String> iterator = lines.iterator();
        String line;
        for (line = iterator.next(); iterator.hasNext(); line = iterator.next()) {
            if (line.startsWith("v ")) {
                String[] currentLine = line.split(" ");
                Vector3f vertex = new Vector3f(Float.valueOf(currentLine[1]),
                                               Float.valueOf(currentLine[2]),
                                               Float.valueOf(currentLine[3]));
                Vertex newVertex = new Vertex(vertices.size(), vertex);
                vertices.add(newVertex);

            } else if (line.startsWith("vt ")) {
                String[] currentLine = line.split(" ");
                Vector2f texture = new Vector2f(Float.valueOf(currentLine[1]),
                                                Float.valueOf(currentLine[2]));
                textures.add(texture);
            } else if (line.startsWith("vn ")) {
                String[] currentLine = line.split(" ");
                Vector3f normal = new Vector3f(Float.valueOf(currentLine[1]),
                                               Float.valueOf(currentLine[2]),
                                               Float.valueOf(currentLine[3]));
                normals.add(normal);
            } else if (line.startsWith("mtllib")) {
                processMaterialLib(line, objFile.getDirectory());
            } else if (line.startsWith("usemtl")) {
                currentMatName = line.substring("usemtl".length()).trim();
            } else if (line.startsWith("f ")) {
                readFace(line);
            }
        }
        do {
// TODO: This could be necessary to move into the loop when doing more complex iterations
            String[] currentLine = line.split(" ");
            String[] vertex1 = currentLine[1].split("/");
            String[] vertex2 = currentLine[2].split("/");
            String[] vertex3 = currentLine[3].split("/");
            processVertex(vertex1, vertices, indices);
            processVertex(vertex2, vertices, indices);
            processVertex(vertex3, vertices, indices);
            line = iterator.next();
        } while (iterator.hasNext() && line.startsWith("f "));

        removeUnusedVertices(vertices);
        float[] verticesArray = new float[vertices.size() * 3];
        float[] texturesArray = new float[vertices.size() * 2];
        float[] normalsArray = new float[vertices.size() * 3];
        int[] indicesArray = indices.stream().mapToInt(i -> i).toArray();
        return new ModelData(verticesArray, texturesArray, normalsArray, indicesArray);
    }

    private static void readFace(String line) {
        Face f = new Face();
        vertList.clear();

        String[] vertIndices = line.substring("f".length()).trim().split("\\s+");
        for (String vertex : vertIndices) {
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
                v = vertices.size() + v + 1;
            }
            if (vt < 0) {
                vt = textures.size() + vt + 1;
            }
            if (vn < 0) {
                vn = normals.size() + vn + 1;
            }

            Vertex vx = vertices.get(v - 1);

            if (vt > 0) {
                vx.setTexCoord(textures.get(vt - 1));
            }

            if (vn > 0) {
                vx.setNormal(normals.get(vn - 1));
            }

            vertList.add(vx);
        }

        if (vertList.size() > 4 || vertList.size() <= 2) {
            System.err.println("Edge or polygon detected in OBJ. Ignored.");
            return;
        }

        f.vertices = new Vertex[vertList.size()];
        for (int i = 0; i < vertList.size(); i++) {
            f.vertices[i] = vertList.get(i);
        }

        if (matList != null && matFaces.containsKey(currentMatName)) {
            matFaces.get(currentMatName).add(f);
        } else {
            faces.add(f); // faces that belong to the default material
        }
    }

    private static void processMaterialLib(String line, String path) {
        String[] libnames = line.substring("mtllib".length()).trim().split(" ");

        for (String libname : libnames) {
            matList = MTLFileLoader.load(new MyFile(path, libname));
            matList.map;
            matList.keySet().forEach(matName -> matFaces.put(matName, new ArrayList<>()));
        }
    }

    private static void processVertex(String[] vertex, List<Vertex> vertices,
                                      List<Integer> indices) {
        int textureIndex = Integer.parseInt(vertex[1]) - 1;
        int normalIndex = Integer.parseInt(vertex[2]) - 1;
        int index = Integer.parseInt(vertex[0]) - 1;
        Vertex currentVertex = vertices.get(index);
        if (!currentVertex.isSet()) {
            currentVertex.setTextureIndex(textureIndex);
            currentVertex.setNormalIndex(normalIndex);
            indices.add(index);
        } else {
            dealWithAlreadyProcessedVertex(currentVertex, textureIndex, normalIndex, indices,
                                           vertices);
        }
    }

    private static void dealWithAlreadyProcessedVertex(Vertex previousVertex, int newTextureIndex,
                                                       int newNormalIndex,
                                                       List<Integer> indices,
                                                       List<Vertex> vertices) {
        if (previousVertex.hasSameTextureAndNormal(newTextureIndex, newNormalIndex)) {
            indices.add(previousVertex.getIndex());
        } else {
            Vertex anotherVertex = previousVertex.getDuplicateVertex();
            if (anotherVertex != null) {
                dealWithAlreadyProcessedVertex(anotherVertex, newTextureIndex,
                                               newNormalIndex, indices,
                                               vertices);
            } else {
                Vertex duplicateVertex = new Vertex(vertices.size(), previousVertex.getPosition());
                duplicateVertex.setTextureIndex(newTextureIndex);
                duplicateVertex.setNormalIndex(newNormalIndex);
                previousVertex.setDuplicateVertex(duplicateVertex);
                vertices.add(duplicateVertex);
                indices.add(duplicateVertex.getIndex());
            }
        }
    }

    private static void removeUnusedVertices(List<Vertex> vertices) {
        vertices.stream().parallel().forEach(vertex -> {
            vertex.averageTangents();
            if (!vertex.isSet()) {
                vertex.setTextureIndex(0);
                vertex.setNormalIndex(0);
            }
        });
    }
}
