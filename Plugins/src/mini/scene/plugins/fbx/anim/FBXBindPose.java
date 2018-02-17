package mini.scene.plugins.fbx.anim;

import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.math.Matrix4f;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.file.FBXId;
import mini.scene.plugins.fbx.obj.FBXObject;

import java.util.HashMap;
import java.util.Map;

public class FBXBindPose extends FBXObject<Map<FBXId, Matrix4f>> {
    private final Map<FBXId, Matrix4f> bindPose = new HashMap<>();

    public FBXBindPose(AssetManager assetManager, AssetKey key) {
        super(assetManager, key);
    }

    @Override
    protected Map<FBXId, Matrix4f> toImplObject() {
        return bindPose;
    }

    @Override
    protected void fromElementOverride(FBXElement element) {
        for (FBXElement fbxElement : element.getChildren()) {
            if (!fbxElement.getName().equals("PoseNode")) {
                continue;
            }

            FBXId node = null;
            float[] matrixData = null;

            for (FBXElement child : fbxElement.getChildren()) {
                if ("Node".equals(child.getName())) {
                    node = FBXId.create(child.getProperties().get(0));
                } else if ("Matrix".equals(child.getName())) {
                    double[] matrixDataDoubles;
                    if (child.getProperties().size() == 16) {
                        // TODO: Performance over this and array conversion. Maybe a helper function
                        // can be a better idea?
                        matrixDataDoubles = child.getProperties().stream()
                                                 .mapToDouble(d -> (double) d).toArray();
                    } else {
                        matrixDataDoubles = (double[]) child.getProperties().get(0);
                    }

                    if (matrixDataDoubles.length != 16) {
                        throw new UnsupportedOperationException("Bind pose matrix must have 16"
                                                                + "doubles, but it had "
                                                                + matrixDataDoubles.length);
                    }

                    matrixData = new float[16];
                    for (int i = 0; i < matrixDataDoubles.length; i++) {
                        matrixData[i] = (float) matrixDataDoubles[i];
                    }
                }
            }

            if (node != null) {
                Matrix4f matrix = new Matrix4f(matrixData);
                bindPose.put(node, matrix);
            }
        }
    }

    @Override
    public void link(FBXObject obj) {
        unsupportedConnectObject(obj);
    }

    @Override
    public void link(FBXObject obj, String propertyName) {
        unsupportedConnectObjectProperty(obj, propertyName);
    }
}
