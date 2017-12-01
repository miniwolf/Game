package mini.shadow;

import mini.math.FastMath;

/**
 * Various useful shadow mapping functions.
 *
 * @see <a href="http://http.developer.nvidia.com/GPUGems3/gpugems3_ch10.html">http://http.developer.nvidia.com/GPUGems3/gpugems3_ch10.html</a>
 * for more info.
 */
public class PSSMShadowUtils {

    /**
     * Updates the frustum splits stores in <code>splits</code> using PSSM.
     *
     * @param splitsArray
     */
    public static void updateFrustumSplit(float[] splits, float near, float far, float lambda) {
        float deltaLambda = 1.0f - lambda;
        for (int i = 0; i < splits.length; i++) {
            float IDM = i / (float) splits.length;
            float log = near * FastMath.pow((far / near), IDM);
            float uniform = near + (far - near) * IDM;
            splits[i] = log * lambda + uniform * (deltaLambda);
        }

        // Used to improve correctness of the calculations. Main near- and farplanes of the camera
        // always stay the same, not matter what.
        splits[0] = near;
        splits[splits.length - 1] = far;
    }
}
