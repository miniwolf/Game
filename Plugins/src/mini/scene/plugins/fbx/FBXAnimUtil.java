package mini.scene.plugins.fbx;

public class FBXAnimUtil {
    /**
     * Conversion factor from FBX animation time unit to seconds.
     */

    public final static long FBX_TC_MILLISECOND = 46186158;
    public final static long FBX_TC_SECOND = FBX_TC_MILLISECOND * 1000;
    public final static double SECONDS_PER_UNIT = 1 / (double) FBX_TC_SECOND;
}
