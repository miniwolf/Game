package mini.objConverter;

import mini.math.Vector2f;
import mini.math.Vector3f;
import mini.utils.MyFile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OBJFileLoader {
    public static ModelData loadOBJ(MyFile objFile) {
        List<String> lines = null;
        try {
            lines = objFile.getLines();
        } catch (Exception e1) {
            e1.printStackTrace();
            System.err.println("Couldn't find model file: " + objFile);
            System.exit(-1);
        }
        List<Vertex> vertices = new ArrayList<>();
        List<Vector2f> textures = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();
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
            } else if (line.startsWith("f ")) {
                break;
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
        float furthest = convertDataToArrays(vertices, textures, normals, verticesArray,
                                             texturesArray, normalsArray);
        int[] indicesArray = indices.stream().mapToInt(i -> i).toArray();
        return new ModelData(verticesArray, texturesArray, normalsArray, indicesArray, furthest);
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

    private static float convertDataToArrays(List<Vertex> vertices, List<Vector2f> textures,
                                             List<Vector3f> normals,
                                             float[] verticesArray, float[] texturesArray,
                                             float[] normalsArray) {
        float furthestPoint = 0;
        for (int i = 0; i < vertices.size(); i++) {
            Vertex currentVertex = vertices.get(i);
            if (currentVertex.getLength() > furthestPoint) {
                furthestPoint = currentVertex.getLength();
            }
            Vector3f position = currentVertex.getPosition();
            Vector2f textureCoord = textures.get(currentVertex.getTextureIndex());
            Vector3f normalVector = normals.get(currentVertex.getNormalIndex());
            verticesArray[i * 3] = position.x;
            verticesArray[i * 3 + 1] = position.y;
            verticesArray[i * 3 + 2] = position.z;
            texturesArray[i * 2] = textureCoord.x;
            texturesArray[i * 2 + 1] = 1 - textureCoord.y;
            normalsArray[i * 3] = normalVector.x;
            normalsArray[i * 3 + 1] = normalVector.y;
            normalsArray[i * 3 + 2] = normalVector.z;

        }
        return furthestPoint;
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
