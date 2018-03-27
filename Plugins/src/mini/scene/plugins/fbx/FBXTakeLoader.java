package mini.scene.plugins.fbx;

import mini.animation.SpatialTrack;
import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.scene.plugins.fbx.anim.FBXTake;
import mini.scene.plugins.fbx.file.FBXElement;

import java.util.ArrayList;
import java.util.List;

public class FBXTakeLoader implements FBXElementLoader<List<SpatialTrack>> {
    private final AssetManager assetManager;
    private final AssetKey key;

    public FBXTakeLoader(AssetManager assetManager, AssetKey key) {
        this.assetManager = assetManager;
        this.key = key;
    }

    @Override
    public List<SpatialTrack> load(FBXElement element) {
        List<SpatialTrack> tracks = new ArrayList<>();
        FBXElement fbxElement = element.getChildren().get(1);
        FBXAnimation fbxAnimation = new FBXAnimation(assetManager, key);
        new FBXTake(assetManager, key, (String) fbxElement.getProperties().get(0));
        for (FBXElement child : fbxElement.getChildren()) {
            switch (child.name) {
                case "LocalTime":
                    long l = Long.parseLong(child.getProperties().get(0).toString());
                    fbxAnimation.setTime(l, (long) child.getProperties().get(1));
                    break;
                case "Model":
                    FbxSpatialTrack spatialTrack = new FbxSpatialTrack(assetManager, key);
                    spatialTrack.fromElement(child);
                    tracks.add(spatialTrack.toImplObject());
                    break;
                default:
                    System.err.println(child.name);
            }
        }
        return tracks;
    }
}
