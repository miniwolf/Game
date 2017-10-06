package mini.material.logic;

import mini.light.AmbientLight;
import mini.light.DirectionalLight;
import mini.light.Light;
import mini.light.LightList;
import mini.light.PointLight;
import mini.material.RenderState;
import mini.material.TechniqueDef;
import mini.math.ColorRGBA;
import mini.math.Quaternion;
import mini.math.Vector3f;
import mini.renderer.RenderManager;
import mini.renderer.Renderer;
import mini.scene.Geometry;
import mini.shaders.Shader;
import mini.shaders.Uniform;
import mini.shaders.VarType;
import mini.utils.TempVars;

public final class MultiPassLightingLogic extends DefaultTechniqueDefLogic {

    private static final RenderState ADDITIVE_LIGHT = new RenderState();
    private static final Quaternion NULL_DIR_LIGHT = new Quaternion(0, -1, 0, -1);

    private final ColorRGBA ambientLightColor = new ColorRGBA(0, 0, 0, 1);

    static {
        ADDITIVE_LIGHT.setBlendMode(RenderState.BlendMode.AlphaAdditive);
        ADDITIVE_LIGHT.setDepthWrite(false);
    }

    public MultiPassLightingLogic(TechniqueDef techniqueDef) {
        super(techniqueDef);
    }

    @Override
    public void render(RenderManager renderManager, Shader shader, Geometry geometry, LightList lights, int lastTexUnit) {
        Renderer r = renderManager.getRenderer();
        Uniform lightDir = shader.getUniform("g_LightDirection");
        Uniform lightColor = shader.getUniform("g_LightColor");
        Uniform lightPos = shader.getUniform("g_LightPosition");
        Uniform ambientColor = shader.getUniform("g_AmbientLightColor");
        boolean isFirstLight = true;
        boolean isSecondLight = false;

        getAmbientColor(lights, false, ambientLightColor);

        for (int i = 0; i < lights.size(); i++) {
            Light l = lights.get(i);
            if (l instanceof AmbientLight) {
                continue;
            }

            if (isFirstLight) {
                // set ambient color for first light only
                ambientColor.setValue(VarType.Vector4f, ambientLightColor);
                isFirstLight = false;
                isSecondLight = true;
            } else if (isSecondLight) {
                ambientColor.setValue(VarType.Vector4f, ColorRGBA.Black);
                // apply additive blending for 2nd and future lights
                r.applyRenderState(ADDITIVE_LIGHT);
                isSecondLight = false;
            }

            TempVars vars = TempVars.get();
            Quaternion tmpLightDirection = vars.quat1;
            Quaternion tmpLightPosition = vars.quat2;
            ColorRGBA tmpLightColor = vars.color;

            ColorRGBA color = l.getColor();
            tmpLightColor.set(color);
            tmpLightColor.a = l.getType().getId();
            lightColor.setValue(VarType.Vector4f, tmpLightColor);

            switch (l.getType()) {
                case Directional:
                    DirectionalLight dl = (DirectionalLight) l;
                    Vector3f dir = dl.getDirection();
                    //FIXME : there is an inconstency here due to backward
                    //compatibility of the lighting shader.
                    //The directional light direction is passed in the
                    //LightPosition uniform. The lighting shader needs to be
                    //reworked though in order to fix this.
                    tmpLightPosition.set(dir.getX(), dir.getY(), dir.getZ(), -1);
                    lightPos.setValue(VarType.Vector4f, tmpLightPosition);
                    tmpLightDirection.set(0, 0, 0, 0);
                    lightDir.setValue(VarType.Vector4f, tmpLightDirection);
                    break;
                case Point:
                    PointLight pl = (PointLight) l;
                    Vector3f pos = pl.getPosition();
                    float invRadius = pl.getInvRadius();

                    tmpLightPosition.set(pos.getX(), pos.getY(), pos.getZ(), invRadius);
                    lightPos.setValue(VarType.Vector4f, tmpLightPosition);
                    tmpLightDirection.set(0, 0, 0, 0);
                    lightDir.setValue(VarType.Vector4f, tmpLightDirection);
                    break;
                case Probe:
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown type of light: " + l.getType());
            }
            vars.release();
            r.setShader(shader);
            renderMeshFromGeometry(r, geometry);
        }

        if (isFirstLight) {
            // Either there are no lights at all, or only ambient lights.
            // Render a dummy "normal light" so we can see the ambient color.
            ambientColor.setValue(VarType.Vector4f, getAmbientColor(lights, false, ambientLightColor));
            lightColor.setValue(VarType.Vector4f, ColorRGBA.BlackNoAlpha);
            lightPos.setValue(VarType.Vector4f, NULL_DIR_LIGHT);
            r.setShader(shader);
            renderMeshFromGeometry(r, geometry);
        }
    }
}
