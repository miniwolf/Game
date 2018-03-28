package mini.scene.plugins.fbx.anim;

import mini.animation.SpatialTrack;
import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.obj.FBXObject;

import java.util.HashMap;
import java.util.Map;

public class FBXTake extends FBXObject<SpatialTrack> {
    private static long FBX_TC_MILLISECOND = 46186158;
    private static long FBX_TC_SECOND = FBX_TC_MILLISECOND * 1000;
    private String name;
    private Map<String, SpatialTrack> tracks = new HashMap<>();
    private float end;

    public FBXTake(AssetManager assetManager, AssetKey key, String name) {
        super(assetManager, key);
        this.name = name;
    }

    @Override
    protected void fromElementOverride(FBXElement element) {

    }

    @Override
    protected SpatialTrack toImplObject() {
        return null;
    }

    @Override
    public void link(FBXObject obj) {

    }

    @Override
    public void link(FBXObject obj, String propertyName) {

    }

    public void AddTrack(String modelName, SpatialTrack spatialTrack) {
        tracks.put(modelName, spatialTrack);
    }

    public void setLocalTime(long start, long end) {
        this.end = (float)end / FBX_TC_SECOND;
    }

    public void setReferenceTime(long start, long end) {

    }

    public float getTime() {
        return end;
    }

    public Map<String, SpatialTrack> getTracks() {
        return tracks;
    }
}
