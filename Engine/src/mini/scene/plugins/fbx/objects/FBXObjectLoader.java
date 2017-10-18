package mini.scene.plugins.fbx.objects;

import mini.asset.AssetManager;
import mini.scene.plugins.fbx.FBXElementLoader;
import mini.scene.plugins.fbx.file.FBXElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FBXObjectLoader implements FBXElementLoader<List<FBXObject>> {
    private Map<Long, FBXMesh> geometryMap = new HashMap<>();
    private Map<Long, FBXNode> modelMap = new HashMap<>();
    private AssetManager assetManager;

    public FBXObjectLoader(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    @Override
    public List<FBXObject> load(FBXElement element) {
        Set<String> unusedElements = new HashSet<>();

        List<FBXObject> gatheredObjects = new ArrayList<>();
        FBXObject obj;
        for (FBXElement fbxElement : element.getChildren()) {
            switch (fbxElement.getName()) {
                case "Geometry":
                    obj = new FBXMesh(fbxElement);
                    geometryMap.put(obj.id, (FBXMesh) obj);
                    break;
                case "Model":
                    obj = new FBXNode(fbxElement);
                    modelMap.put(obj.id,
                                 (FBXNode) obj); // TODO: Do I need to distinguish between limbNode and other nodes?
                    break;
                case "Material":
                    obj = new FBXMaterial(fbxElement, assetManager);
                    break;
                case "Texture":
                    obj = new FBXTexture(fbxElement);
                    break;
                default:
                    obj = null;
                    unusedElements.add(fbxElement.getName());
            }

            if (obj != null) {
                gatheredObjects.add(obj);
            }
        }
        System.out.println("FBXObjectLoader unusedElements: ");
        unusedElements.forEach(System.out::println);

        System.out.println("FBXMesh unusedElements: ");
        FBXMesh.getUnusedElements().forEach(System.out::println);
        return gatheredObjects;
    }
}
