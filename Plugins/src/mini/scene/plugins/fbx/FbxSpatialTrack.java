package mini.scene.plugins.fbx;

import mini.animation.SpatialTrack;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.obj.FBXObject;

public class FbxSpatialTrack {

    public void fromElement(FBXElement element) {
        System.err.println(element);
    }

    protected SpatialTrack toImplObject() {
        return null;
    }

    public void link(FBXObject obj) {

    }

    public void link(FBXObject obj, String propertyName) {

    }
}
