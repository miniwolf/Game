package mini.scene.plugins.fbx.mesh;

import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.math.Vector3f;
import mini.scene.Mesh;
import mini.scene.plugins.fbx.anim.FBXSkinDeformer;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.node.FBXNodeAttribute;
import mini.scene.plugins.fbx.obj.FBXObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FBXMesh extends FBXNodeAttribute<Map<Integer, Mesh>> {
    private Vector3f[] vertices;
    private int lastMaterialId = 0;
    private FBXPolygon[] polygons;
    private FBXLayerElement[] layerElements;
    private FBXLayer[] layers;
    private FBXSkinDeformer skinDeformer;

    public FBXMesh(AssetManager assetManager, AssetKey key) {
        super(assetManager, key);
    }

    @Override
    public void fromElement(FBXElement element) {
        super.fromElement(element);

        List<FBXLayerElement> layerElementList = new ArrayList<>();
        List<FBXLayer> layerList = new ArrayList<>();

        for (FBXElement fbxElement : element.getChildren()) {
            if (fbxElement.getName().equals("Vertices")) {
                setPositions(FBXMeshReader.getDoubleArray(fbxElement));
            } else if (fbxElement.getName().equals("PolygonVertexIndex")) {
                setPolygonVertexIndices(FBXMeshReader.getIntArray(fbxElement));
            } else if (fbxElement.getName().startsWith("LayerElement")) {
                layerElementList.add(FBXLayerElement.fromElement(fbxElement));
            } else if (fbxElement.getName().equals("Layer")) {
                layerList.add(FBXLayer.fromElement(fbxElement));
            }
        }

        layerList.forEach(layer -> layer.setLayerElements(layerElementList));

        layerElements = new FBXLayerElement[layerElementList.size()];
        layerElementList.toArray(layerElements);

        layers = new FBXLayer[layerList.size()];
        layerList.toArray(layers);
    }

    private void setPolygonVertexIndices(int[] polygonVertexIndices) {
        List<FBXPolygon> polygonList = new ArrayList<>();

        List<Integer> vertexIndices = new ArrayList<>();

        for (int polygonVertexIndex : polygonVertexIndices) {
            // Indices contains negative numbers to define polygon last index
            // Check indices strides to be sure we have triangles or quad
            if (polygonVertexIndex < 0) {
                vertexIndices.add(0);
                polygonList.add(FBXPolygon.fromIndices(vertexIndices));
                vertexIndices.clear();
            } else {
                vertexIndices.add(polygonVertexIndex);
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
            System.out.println(obj);
        }
    }

    @Override
    public void link(FBXObject obj, String propertyName) {
        System.out.println(obj);
    }
}
