package mini.scene.plugins.fbx.mesh;

import mini.scene.plugins.fbx.file.FBXElement;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class FBXLayer {
    private int layerIndex;
    private Map<FBXLayerElement.Type, FBXLayerElementRef> references =
            new EnumMap<>(FBXLayerElement.Type.class);
    private List<FBXLayerElement> layerElements;

    public static FBXLayer fromElement(FBXElement fbxElement) {
        FBXLayer layer = new FBXLayer();
        layer.layerIndex = (int) fbxElement.getProperties().get(0);

        for (FBXElement element : fbxElement.getChildren()) {
            if (!element.name.equals("LayerElement")) {
                continue;
            }
            addLayerElementRefIfValid(element, layer.references);
        }
        return layer;
    }

    private static void addLayerElementRefIfValid(FBXElement element,
                                                  Map<FBXLayerElement.Type, FBXLayerElementRef> references) {
        FBXLayerElementRef ref = new FBXLayerElementRef();
        for (FBXElement layerElement : element.getChildren()) {
            if (layerElement.name.equals("Type")) {
                String layerElementType = (String) layerElement.getProperties().get(0);
                layerElementType = layerElementType.substring("LayerElement".length());
                try {
                    ref.layerElementType = FBXLayerElement.Type.valueOf(layerElementType);
                } catch (IllegalArgumentException ex) {
                    System.err.println(
                            "Unsupported layer type: " + layerElementType + ". Ignoring");
                    return;
                }
            } else if (layerElement.name.equals("TypedIndex")) {
                ref.layerElementIndex = (int) layerElement.getProperties().get(0);
            }
        }
        references.put(ref.layerElementType, ref);
    }

    public void setLayerElements(List<FBXLayerElement> layerElements) {
        for (FBXLayerElement layerElement : layerElements) {
            FBXLayerElementRef reference = references.get(layerElement.getType());
            if (reference != null && reference.layerElementIndex == layerElement.getIndex()) {
                reference.layerElement = layerElement;
            }
        }
    }

    public Object getVertexData(FBXLayerElement.Type type, int polygonIndex,
                                int polygonVertexIndex, int vertexIndex, int edgeIndex) {
        FBXLayerElementRef reference = references.get(type);
        if (reference == null) {
            return null;
        } else {
            return reference.layerElement
                    .getVertexData(polygonIndex, polygonVertexIndex, vertexIndex, edgeIndex);
        }
    }

    private static class FBXLayerElementRef {
        FBXLayerElement.Type layerElementType;
        int layerElementIndex;
        FBXLayerElement layerElement;
    }
}
