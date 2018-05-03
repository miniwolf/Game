package mini.scene.plugins;

import mini.math.ColorRGBA;
import mini.math.Vector2f;
import mini.math.Vector3f;
import mini.math.Vector4f;

public class IrVertex {

    public Vector3f pos;
    public Vector3f norm;
    public Vector3f tang;
    public Vector3f bitang;
    public Vector2f uv0;
    public ColorRGBA color;
    public Integer material;
    public Integer smoothing;
    public Vector2f uv1;
    public Vector4f tang4d;
    public IrBoneWeightIndex[] boneWeightsIndices;
}
