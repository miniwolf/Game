package obj;

import entities.Face;
import model.RawModel;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import rendering.Loader;
import rendering.Material;
import texture.ModelTexture;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author miniwolf
 */
public class OBJLoader {
    private static List<Material> materials = new ArrayList<>();

    private static Material currentMat = null;

    private static final String RES_LOC = "resources/obj/";

    public static ModelData loadOBJ(String objFileName) {
        FileReader isr = null;
        File objFile = new File(RES_LOC + objFileName + ".obj");
        try {
            isr = new FileReader(objFile);
        } catch (FileNotFoundException e) {
            System.err.println("File not found in res; don't use any extention");
        }
        assert isr != null;
        BufferedReader reader = new BufferedReader(isr);
        String line;
        List<Vertex> vertices = new ArrayList<>();
        List<Vector2f> textures = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        try {
            while (true) {
                line = reader.readLine().replace("  ", " ");
                if (line.startsWith("v ")) {
                    String[] currentLine = line.split(" ");
                    Vector3f vertex = new Vector3f(Float.valueOf(currentLine[1]),
                                                   Float.valueOf(currentLine[2]),
                                                   Float.valueOf(currentLine[3]));
                    Vertex newVertex = new Vertex(vertices.size(), vertex);
                    vertices.add(newVertex);

                } else if (line.startsWith("vt ")) {
                    String[] currentLine = line.split(" ");
                    Vector2f texture = new Vector2f(Float.valueOf(currentLine[1]), Float.valueOf(currentLine[2]));
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
            while ( (line = reader.readLine()) != null && line.startsWith("f ") ) {
                String[] currentLine = line.split(" ");
                for ( int i = 1; i < currentLine.length; i++ ) {
                    String[] vertex = currentLine[i].split("/");
                    processVertex(vertex, vertices, indices);
                }
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Error reading the file");
        }
        removeUnusedVertices(vertices);
        float[] verticesArray = new float[vertices.size() * 3];
        float[] texturesArray = new float[vertices.size() * 2];
        float[] normalsArray = new float[vertices.size() * 3];
        float furthest = convertDataToArrays(vertices, textures, normals, verticesArray,
                texturesArray, normalsArray);
        int[] indicesArray = convertIndicesListToArray(indices);
        return new ModelData(verticesArray, texturesArray, normalsArray, indicesArray,
                furthest);
    }

    public static RawModel loadModel2(String file, Loader loader) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(new File("resources/obj/" + file + ".obj")));
        } catch (FileNotFoundException e) {
            System.err.println("Couldn't load file!");
            e.printStackTrace();
        }

        RawModel model = new RawModel(0, 0);

        String line;
        try {
            assert reader != null;
            while ( (line = reader.readLine()) != null ) {
                line = line.replace("  ", " ");
                String[] split = line.split(" ");
                if ( line.startsWith("v ") )
                    model.addVertex(new Vector3f(Float.parseFloat(split[1]), Float.parseFloat(split[2]), Float.parseFloat(split[3])));
                if ( line.startsWith("vt ") )
                    model.addTexture(new Vector2f(Float.parseFloat(split[1]), Float.parseFloat(split[2])));
                if ( line.startsWith("vn ") )
                    model.addNormal(new Vector3f(Float.parseFloat(split[1]), Float.parseFloat(split[2]), Float.parseFloat(split[3])));
                if ( line.startsWith("f ") ) processFace(split, model);
                if ( line.startsWith("mtllib ") )
                    materials.addAll(parseMtl(split[2], loader));
                if ( line.startsWith("usemtl ") ) {
                    String name = split[1];
                    for ( Material material : materials ) {
                        if ( material.getTexture(name) != null ) {
                            currentMat = material;
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return model;
    }

    private static List<Material> parseMtl(String mtllib, Loader loader) {
        List<Material> mats = new ArrayList<>();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(new File("resources/material/" + mtllib + ".mtl")));
        } catch (FileNotFoundException e) {
            System.err.println("Couldn't load file!");
            e.printStackTrace();
        }

        Material mtl = null;
        String line;
        try {
            assert reader != null;
            while ( (line = reader.readLine()) != null) {
                String[] split = line.split(" ");
                if ( line.startsWith("newmtl ") ) {
                    // If this is not the first material,
                    // Add the current material to mats list
                    // Before creating a new one
                    if ( mtl != null )
                        mats.add(mtl);
                    mtl = new Material();
                    mtl.addTexture(split[1], new ModelTexture(loader.loadTexture(split[1])));
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mats.add(mtl);
        return mats;
    }

    /*private static RawModel readFace(String[] split, Loader loader) {
        String[] vertex1 = split[1].split("/");
        String[] vertex2 = split[2].split("/");
        String[] vertex3 = split[3].split("/");

        float[] verticesArray = new float[3];
        float[] normalsArray = new float[3];
        float[] textureArray = new float[2];

        processVertex(vertex1, indices, texCoords, normals, textureArray, normalsArray);
        processVertex(vertex2, indices, texCoords, normals, textureArray, normalsArray);
        processVertex(vertex3, indices, texCoords, normals, textureArray, normalsArray);

        int[] indicesArray = new int[indices.size()];
        int vertexPointer = 0;
        for ( Vector3f vertex : vertices ) {
            verticesArray[vertexPointer++] = vertex.x;
            verticesArray[vertexPointer++] = vertex.y;
            verticesArray[vertexPointer++] = vertex.z;
        }

        for ( int i = 0; i < indices.size(); i++ ) indicesArray[i] = indices.get(i);

        return loader.loadToVAO(verticesArray, textureArray, normalsArray, indicesArray);
    }*/

    private static void processFace(String[] split, RawModel model) {
        //If face is triangulated
        if ( split.length == 4 ) {
            Vector3f vertexIndices = new Vector3f(
                    Float.valueOf(split[1].split("/")[0]),
                    Float.valueOf(split[2].split("/")[0]),
                    Float.valueOf(split[3].split("/")[0]));


            //Instantiate as null for scope reasons
            Vector3f texCoords = null;

            if ( !split[1].split("/")[1].equals("")&& split[1].split("/")[1] != null )
                texCoords = new Vector3f(
                    Float.valueOf(split[1].split("/")[1]),
                    Float.valueOf(split[2].split("/")[1]),
                    Float.valueOf(split[3].split("/")[1]));

            Vector3f normalIndices = new Vector3f(
                    Float.valueOf(split[1].split("/")[2]),
                    Float.valueOf(split[2].split("/")[2]),
                    Float.valueOf(split[3].split("/")[2]));

            Face mf = new Face();

            //Instantiate all the arrays
            mf.setNormals(new Vector3f[3]);
            mf.setVertices(new Vector3f[3]);

            //// SETUP NORMALS ////
            List<Vector3f> mNormals = model.getNormals();
            Vector3f n1 = mNormals.get((int) normalIndices.x - 1);
            Vector3f[] normals = mf.getNormals();
            normals[0] = n1;
            Vector3f n2 = mNormals.get((int) normalIndices.y - 1);
            normals[1] = n2;
            Vector3f n3 = mNormals.get((int) normalIndices.z - 1);
            normals[2] = n3;
            mf.setNormals(normals);

            //// SETUP VERTICIES ////
            List<Vector3f> mVertices = model.getVertices();
            Vector3f v1 = mVertices.get((int)vertexIndices.x - 1);
            Vector3f[] vertices = mf.getVertices();
            vertices[0] = v1;
            Vector3f v2 = mVertices.get((int)vertexIndices.y - 1);
            vertices[1] = v2;
            Vector3f v3 = mVertices.get((int)vertexIndices.z - 1);
            vertices[2] = v3;
            mf.setVertices(vertices);

            //// SETUP TEXTURE COORDS ////
            if ( texCoords != null ) {

                List<Vector2f> mTextureCoords = model.getTexCoords();

                Vector2f[] textureCoords = new Vector2f[3];
                Vector2f t1 = mTextureCoords.get((int)texCoords.x - 1);
                textureCoords[0] = t1;
                Vector2f t2 = mTextureCoords.get((int)texCoords.y - 1);
                textureCoords[1] = t2;
                Vector2f t3 = mTextureCoords.get((int)texCoords.z - 1);
                textureCoords[2] = t3;
                mf.setTexCoords(textureCoords);
            }

            //Set the face's material to the current material
            if ( currentMat != null ) {
                mf.setMaterial(currentMat);
                //if(cur.texture!=null)
                //m.texture = cur.texture;
            }

            model.addFace(mf);
        }
    }

    private static void processVertex(String[] vertex, List<Vertex> vertices, List<Integer> indices) {
        int index = Integer.parseInt(vertex[0]) - 1;
        Vertex currentVertex = vertices.get(index);
        int textureIndex = Integer.parseInt(vertex[1]) - 1;
        int normalIndex = Integer.parseInt(vertex[2]) - 1;
        if ( !currentVertex.isSet() ) {
            currentVertex.setTextureIndex(textureIndex);
            currentVertex.setNormalIndex(normalIndex);
            indices.add(index);
        } else {
            dealWithAlreadyProcessedVertex(currentVertex, textureIndex, normalIndex, indices, vertices);
        }
    }

    private static int[] convertIndicesListToArray(List<Integer> indices) {
        return indices.stream().mapToInt(i -> i).toArray();
    }

    private static float convertDataToArrays(List<Vertex> vertices, List<Vector2f> textures,
                                             List<Vector3f> normals, float[] verticesArray, float[] texturesArray,
                                             float[] normalsArray) {
        float furthestPoint = 0;
        for ( int i = 0; i < vertices.size(); i++ ) {
            Vertex currentVertex = vertices.get(i);
            if ( currentVertex.getLength() > furthestPoint ) furthestPoint = currentVertex.getLength();
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
                                                       int newNormalIndex, List<Integer> indices, List<Vertex> vertices) {
        if ( previousVertex.hasSameTextureAndNormal(newTextureIndex, newNormalIndex) ) {
            indices.add(previousVertex.getIndex());
        } else {
            Vertex anotherVertex = previousVertex.getDuplicateVertex();
            if ( anotherVertex != null ) {
                dealWithAlreadyProcessedVertex(anotherVertex, newTextureIndex, newNormalIndex,
                        indices, vertices);
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
        vertices.stream().filter(vertex -> !vertex.isSet()).forEach(vertex -> {
            vertex.setTextureIndex(0);
            vertex.setNormalIndex(0);
        });
    }
}

