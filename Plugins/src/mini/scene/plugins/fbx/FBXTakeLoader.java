package mini.scene.plugins.fbx;

import mini.animation.SpatialTrack;
import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.scene.plugins.fbx.anim.FBXTake;
import mini.scene.plugins.fbx.file.FBXElement;

import java.util.HashMap;
import java.util.Map;

public class FBXTakeLoader implements FBXElementLoader<FBXTake> {
    private final AssetManager assetManager;
    private final AssetKey key;
    private String takeName;

    public FBXTakeLoader(AssetManager assetManager, AssetKey key) {
        this.assetManager = assetManager;
        this.key = key;
    }

    public String getTakeName() {
        return takeName;
    }

    @Override
    public FBXTake load(FBXElement element) {
        FBXElement fbxElement = element.getChildren().get(1);
        FBXTake fbxTake = new FBXTake(assetManager, key, (String) fbxElement.getProperties().get(0));
        takeName = (String) fbxElement.getProperties().get(0);

        for (FBXElement child : fbxElement.getChildren()) {
            switch (child.name) {
                case "LocalTime":
                    fbxTake.setLocalTime((long)child.getProperties().get(0), (long) child.getProperties().get(1));
                    break;
                case "ReferenceTime":
                    fbxTake.setReferenceTime((long) child.getProperties().get(0), (long) child.getProperties().get(1));
                    break;
                case "Model":
                    FbxSpatialTrack spatialTrack = new FbxSpatialTrack(assetManager, key);
                    spatialTrack.fromElement(child);
                    fbxTake.AddTrack((String) child.getProperties().get(0), spatialTrack.toImplObject());
                    break;
                default:
                    System.err.println(child.name);
            }
        }
        return fbxTake;
    }
}
