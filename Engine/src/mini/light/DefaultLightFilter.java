package mini.light;


import mini.renderer.Camera;
import mini.scene.Geometry;
import mini.utils.TempVars;

import java.util.HashSet;
import java.util.Set;

public final class DefaultLightFilter implements LightFilter {

    private Camera camera;
    private final Set<Light> processedLights = new HashSet<>();
    private final LightProbeBlendingStrategy probeBlendStrat;

    public DefaultLightFilter() {
        probeBlendStrat = new BasicProbeBlendingStrategy();
    }

    public DefaultLightFilter(LightProbeBlendingStrategy probeBlendStrat) {
        this.probeBlendStrat = probeBlendStrat;
    }

    @Override
    public void setCamera(Camera camera) {
        this.camera = camera;
        for (Light light : processedLights) {
            light.frustumCheckNeeded = true;
        }
    }

    @Override
    public void filterLights(Geometry geometry, LightList filteredLightList) {
        TempVars vars = TempVars.get();
        try {
            LightList worldLights = geometry.getWorldLightList();

            for (int i = 0; i < worldLights.size(); i++) {
                Light light = worldLights.get(i);

                // If this light is not enabled it will be ignored.
                if (!light.isEnabled()) {
                    continue;
                }

                if (light.frustumCheckNeeded) {
                    processedLights.add(light);
                    light.frustumCheckNeeded = false;
                    light.intersectsFrustum = light.intersectsFrustum(camera, vars);
                }

                if (!light.intersectsFrustum) {
                    continue;
                }

                if (light.getType() == Light.Type.Probe) {
                    probeBlendStrat.registerProbe((LightProbe) light);
                } else {
                    filteredLightList.add(light);
                }

            }

            probeBlendStrat.populateProbes(geometry, filteredLightList);

        } finally {
            vars.release();
        }
    }

}
