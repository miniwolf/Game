package mini.light;

import mini.scene.Geometry;

/**
 * This is the interface to implement if you want to make your own LightProbe blending strategy.
 * The strategy sets the way multiple LightProbes will be handled for a given object.
 */
public interface LightProbeBlendingStrategy {
    /**
     * Registers a probe with this strategy
     *
     * @param probe
     */
    void registerProbe(LightProbe probe);

    /**
     * Populates the resulting light probes into the given light list.
     *
     * @param g         the geometry for wich the light list is computed
     * @param lightList the result light list
     */
    void populateProbes(Geometry g, LightList lightList);
}

