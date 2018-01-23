package mini.scene.plugins.fbx.utils;

import mini.math.FastMath;
import mini.math.Quaternion;
import mini.math.Vector3f;

public class RotationOrder {
    public static Quaternion rotate(float x, float y, float z) {
        return fromEuler(x * FastMath.DEG_TO_RAD, y * FastMath.DEG_TO_RAD, z * FastMath.DEG_TO_RAD);
    }

    private static Quaternion fromEuler(float x, float y, float z) {
        Quaternion q1 = new Quaternion().fromAngleNormalAxis(x, Vector3f.UNIT_X);
        Quaternion q2 = new Quaternion().fromAngleNormalAxis(y, Vector3f.UNIT_Y);
        Quaternion q3 = new Quaternion().fromAngleNormalAxis(z, Vector3f.UNIT_Z);

        return q1.multLocal(q2).multLocal(q3);
    }
}
