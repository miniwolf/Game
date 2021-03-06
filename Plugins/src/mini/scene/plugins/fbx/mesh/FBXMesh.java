package mini.scene.plugins.fbx.mesh;

import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.math.ColorRGBA;
import mini.math.Vector2f;
import mini.math.Vector3f;
import mini.scene.Mesh;
import mini.scene.plugins.IrBoneWeightIndex;
import mini.scene.plugins.IrMesh;
import mini.scene.plugins.IrPolygon;
import mini.scene.plugins.IrUtils;
import mini.scene.plugins.IrVertex;
import mini.scene.plugins.fbx.anim.FBXCluster;
import mini.scene.plugins.fbx.anim.FBXSkinDeformer;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.node.FBXNodeAttribute;
import mini.scene.plugins.fbx.obj.FBXObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FBXMesh extends FBXNodeAttribute<Map<Integer, Mesh>> {
    private Vector3f[] vertices;
    private int lastMaterialId = 0;
    private FBXPolygon[] polygons;
    private FBXLayerElement[] layerElements;
    private FBXLayer[] layers;
    private FBXSkinDeformer skinDeformer;

    private ArrayList<Integer>[] boneIndices;
    private ArrayList<Float>[] boneWeights;

    public FBXMesh(AssetManager assetManager, AssetKey key) {
        super(assetManager, key);
    }

    @Override
    protected void fromElementOverride(FBXElement element) {

        List<FBXLayerElement> layerElementList = new ArrayList<>();
        List<FBXLayer> layerList = new ArrayList<>();

        for (FBXElement fbxElement : element.getChildren()) {
            if (fbxElement.name.equals("Vertices")) {
                setPositions(FBXMeshReader.getDoubleArray(fbxElement));
            } else if (fbxElement.name.equals("PolygonVertexIndex")) {
                setPolygonVertexIndices(FBXMeshReader.getIntArray(fbxElement));
            } else if (fbxElement.name.startsWith("LayerElement")) {
                layerElementList.add(FBXLayerElement.fromElement(fbxElement));
            } else if (fbxElement.name.equals("Layer")) {
                layerList.add(FBXLayer.fromElement(fbxElement));
            }
        }

        layerList.forEach(layer -> layer.setLayerElements(layerElementList));

        layerElements = new FBXLayerElement[layerElementList.size()];
        layerElementList.toArray(layerElements);

        layers = new FBXLayer[layerList.size()];
        layerList.toArray(layers);
    }

    @Override
    protected Map<Integer, Mesh> toImplObject() {
        if (skinDeformer != null) {
            for (FBXCluster cluster : skinDeformer.getImplObject()) {
                applyCluster(cluster);
            }
        }

        IrMesh irMesh = toIRMesh();

        // Trim bone weight to 4 weights per vertex
        IrUtils.trimBoneWeights(irMesh);

        // Convert tangents / binormals to tangents with parity
        IrUtils.toTangentWithParity(irMesh);

        // Triangulate quads
        IrUtils.triangulate(irMesh);

        // Split meshes by material indices
        Map<Integer, IrMesh> irMeshMap = IrUtils.splitByMaterial(irMesh);

        // Create internal Mesh for each material index.
        Map<Integer, Mesh> meshes = new HashMap<>();
        for (Map.Entry<Integer, IrMesh> irMeshEntry : irMeshMap.entrySet()) {
            Mesh mesh = IrUtils.convertIrMeshToMesh(irMeshEntry.getValue());
            meshes.put(irMeshEntry.getKey(), mesh);
        }

        if (meshes.size() == 0) {
            // TODO: Will this happen? Not sure
            System.err.println("Warning: Empty FBX mesh found (unusual)");
        }

        // NOTE: If we have a -1 entry, those are triangles with no material indices.
        // It makes sense only if the mesh uses a single material!
        if (meshes.containsKey(-1) && meshes.size() > 1) {
            System.err.println("Warning: Mesh has polygons with no material indices (unusual) -"
                               + " they will use material index 0.");
        }
        return meshes;
    }

    private IrMesh toIRMesh() {
        IrMesh newMesh = new IrMesh();
        newMesh.polygons = new IrPolygon[polygons.length];

        int polygonVertexIndex = 0;
        int vertexIndex = 0;

        FBXLayer layer0 = layers[0];
        FBXLayer layer1 = layers.length > 1 ? layers[1] : null;

        for (int i = 0; i < polygons.length; i++) {
            FBXPolygon polygon = polygons[i];
            IrPolygon irPolygon = new IrPolygon();
            irPolygon.vertices = new IrVertex[polygon.getIndices().length];

            for (int j = 0; j < polygon.getIndices().length; j++) {
                vertexIndex = polygon.getIndices()[j];

                IrVertex irVertex = new IrVertex();
                irVertex.pos = vertices[vertexIndex];

                if (layer0 != null) {
                    irVertex.norm =
                            (Vector3f) layer0.getVertexData(FBXLayerElement.Type.Normal, i,
                                                            polygonVertexIndex, vertexIndex, 0);
                    irVertex.tang =
                            (Vector3f) layer0.getVertexData(FBXLayerElement.Type.Tangent, i,
                                                            polygonVertexIndex, vertexIndex, 0);
                    irVertex.bitang =
                            (Vector3f) layer0.getVertexData(FBXLayerElement.Type.Binormal, i,
                                                            polygonVertexIndex, vertexIndex, 0);
                    irVertex.uv0 =
                            (Vector2f) layer0.getVertexData(FBXLayerElement.Type.UV, i,
                                                            polygonVertexIndex, vertexIndex, 0);
                    irVertex.color =
                            (ColorRGBA) layer0.getVertexData(FBXLayerElement.Type.Color, i,
                                                             polygonVertexIndex, vertexIndex, 0);
                    irVertex.material =
                            (Integer) layer0.getVertexData(FBXLayerElement.Type.Material, i,
                                                           polygonVertexIndex, vertexIndex, 0);
                    irVertex.smoothing =
                            (Integer) layer0.getVertexData(FBXLayerElement.Type.Smoothing, i,
                                                           polygonVertexIndex, vertexIndex, 0);
                }

                if (layer1 != null) {
                    irVertex.uv1 = (Vector2f) layer1
                            .getVertexData(FBXLayerElement.Type.UV, i, polygonVertexIndex,
                                           vertexIndex, 0);
                }

                if (boneIndices != null) {
                    ArrayList<Integer> boneIndicesForVertex = boneIndices[vertexIndex];
                    ArrayList<Float> boneWeightsForVertex = boneWeights[vertexIndex];
                    if (boneIndicesForVertex != null) {
                        irVertex.boneWeightsIndices = toBoneWeightIndices(boneIndicesForVertex,
                                                                          boneWeightsForVertex);
                    }
                }

                irPolygon.vertices[j] = irVertex;
                polygonVertexIndex++;
            }
            newMesh.polygons[i] = irPolygon;
        }

        // Ensure "inspection vertex" specifies that mesh has bone indices / weights
        if (boneIndices != null && newMesh.polygons[0].vertices[0] != null) {
            newMesh.polygons[0].vertices[0].boneWeightsIndices = new IrBoneWeightIndex[0];
        }

        return newMesh;
    }

    private IrBoneWeightIndex[] toBoneWeightIndices(List<Integer> boneIndices,
                                                    List<Float> boneWeights) {
        var boneWeightIndices = new IrBoneWeightIndex[boneIndices.size()];
        for (int i = 0; i < boneIndices.size(); i++) {
            boneWeightIndices[i] = new IrBoneWeightIndex(boneIndices.get(i), boneWeights.get(i));
        }
        return boneWeightIndices;
    }

    private void applyCluster(FBXCluster cluster) {
        if (cluster == null || cluster.getIndexes() == null || cluster.getWeights() == null) {
            return;
        }
        if (boneIndices == null) {
            boneIndices = new ArrayList[vertices.length];
            boneWeights = new ArrayList[vertices.length];
        }

        var limb = cluster.getLimb();
        var bone = limb.getBone();
        var skeleton = limb.getSkeletonHolder().getSkeleton();
        int boneIndex = skeleton.getBoneIndex(bone);

        Integer[] positionIndices = cluster.getIndexes();
        Double[] weights = cluster.getWeights();

        for (int i = 0; i < positionIndices.length; i++) {
            var positionIndex = positionIndices[i];
            var boneWeight = weights[i].floatValue();

            var boneIndicesForVertex = boneIndices[positionIndex];
            var boneWeightsForVertex = boneWeights[positionIndex];

            if (boneIndicesForVertex == null) {
                boneIndicesForVertex = new ArrayList<>();
                boneWeightsForVertex = new ArrayList<>();
                boneIndices[positionIndex] = boneIndicesForVertex;
                boneWeights[positionIndex] = boneWeightsForVertex;
            }

            boneIndicesForVertex.add(boneIndex);
            boneWeightsForVertex.add(boneWeight);
        }
    }

    private void setPolygonVertexIndices(int[] polygonVertexIndices) {
        List<FBXPolygon> polygonList = new ArrayList<>();

        boolean finishPolygon = false;
        List<Integer> vertexIndices = new ArrayList<>();

        for (int polygonVertexIndex : polygonVertexIndices) {
            int vertexIndex = polygonVertexIndex;

            if (vertexIndex < 0) {
                vertexIndex ^= -1;
                finishPolygon = true;
            }

            vertexIndices.add(vertexIndex);

            if (finishPolygon) {
                finishPolygon = false;
                polygonList.add(FBXPolygon.fromIndices(vertexIndices));
                vertexIndices.clear();
            }
        }

        polygons = new FBXPolygon[polygonList.size()];
        polygonList.toArray(polygons);
    }

    private void setPositions(double[] positions) {
        vertices = FBXLayerElement.toVector3f(positions);
    }

    @Override
    public void link(FBXObject obj) {
        if (obj instanceof FBXSkinDeformer) {
            skinDeformer = (FBXSkinDeformer) obj;
        } else {
            unsupportedConnectObject(obj);
        }
    }

    @Override
    public void link(FBXObject obj, String propertyName) {
        unsupportedConnectObjectProperty(obj, propertyName);
    }

    public Vector3f[] getVertices() {
        return vertices;
    }
}
