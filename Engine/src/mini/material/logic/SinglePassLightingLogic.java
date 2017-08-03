package mini.material.logic;

import mini.light.DirectionalLight;
import mini.light.Light;
import mini.light.LightList;
import mini.light.PointLight;
import mini.material.RenderState;
import mini.material.TechniqueDef;
import mini.math.ColorRGBA;
import mini.math.Vector3f;
import mini.math.Vector4f;
import mini.renderEngine.Caps;
import mini.renderEngine.RenderManager;
import mini.renderEngine.opengl.GLDebug;
import mini.renderEngine.opengl.GLRenderer;
import mini.scene.Geometry;
import mini.shaders.DefineList;
import mini.shaders.ShaderProgram;
import mini.shaders.Uniform;
import mini.shaders.VarType;
import mini.utils.TempVars;

import java.util.EnumSet;

public final class SinglePassLightingLogic extends DefaultTechniqueDefLogic {

    private static final String DEFINE_SINGLE_PASS_LIGHTING = "SINGLE_PASS_LIGHTING";
    private static final String DEFINE_NB_LIGHTS = "NB_LIGHTS";
    private static final RenderState ADDITIVE_LIGHT = new RenderState();

    private final ColorRGBA ambientLightColor = new ColorRGBA(0, 0, 0, 1);

    static {
        ADDITIVE_LIGHT.setBlendMode(RenderState.BlendMode.AlphaAdditive);
        ADDITIVE_LIGHT.setDepthWrite(false);
    }

    private final int singlePassLightingDefineId;
    private final int nbLightsDefineId;

    public SinglePassLightingLogic(TechniqueDef techniqueDef) {
        super(techniqueDef);
        singlePassLightingDefineId = techniqueDef.addShaderUnmappedDefine(DEFINE_SINGLE_PASS_LIGHTING, VarType.Boolean);
        nbLightsDefineId = techniqueDef.addShaderUnmappedDefine(DEFINE_NB_LIGHTS, VarType.Int);
    }

    @Override
    public ShaderProgram makeCurrent(RenderManager renderManager, EnumSet<Caps> rendererCaps,
                                     LightList lights, DefineList defines) {
        defines.set(nbLightsDefineId, renderManager.getSinglePassLightBatchSize() * 3);
        defines.set(singlePassLightingDefineId, true);
        return super.makeCurrent(renderManager, rendererCaps, lights, defines);
    }

    /**
     * Uploads the lights in the light list as two uniform arrays.<br/><br/> *
     * <p>
     * <code>uniform vec4 g_LightColor[numLights];</code><br/> //
     * g_LightColor.rgb is the diffuse/specular color of the light.<br/> //
     * g_Lightcolor.a is the type of light, 0 = Directional, 1 = Point, <br/> //
     * 2 = Spot. <br/> <br/>
     * <code>uniform vec4 g_LightPosition[numLights];</code><br/> //
     * g_LightPosition.xyz is the position of the light (for point lights)<br/>
     * // or the direction of the light (for directional lights).<br/> //
     * g_LightPosition.w is the inverse radius (1/r) of the light (for
     * attenuation) <br/> </p>
     */
    protected int updateLightListUniforms(ShaderProgram shader, Geometry g, LightList lightList, int numLights, RenderManager rm, int startIndex) {
        if (numLights == 0) { // this shader does not do lighting, ignore.
            return 0;
        }

        Uniform lightData = shader.getUniform("g_LightData");
        lightData.setVector4fLength(numLights * 3);//8 lights * max 3
        Uniform ambientColor = shader.getUniform("g_AmbientLightColor");


        if (startIndex != 0) {
            // apply additive blending for 2nd and future passes
            rm.getRenderer().applyRenderState(ADDITIVE_LIGHT);
            ambientColor.setValue(VarType.Vector4f, ColorRGBA.Black);
        } else {
            ambientColor.setValue(VarType.Vector4f, getAmbientColor(lightList, true, ambientLightColor));
        }

        int lightDataIndex = 0;
        TempVars vars = TempVars.get();
        Vector4f tmpVec = vars.vect4f1;
        int curIndex;
        int endIndex = numLights + startIndex;
        for (curIndex = startIndex; curIndex < endIndex && curIndex < lightList.size(); curIndex++) {

            Light l = lightList.get(curIndex);
            if (l.getType() == Light.Type.Ambient) {
                endIndex++;
                continue;
            }
            ColorRGBA color = l.getColor();
            //Color
            lightData.setVector4fInArray(color.getRed(),
                                        color.getGreen(),
                                        color.getBlue(),
                                        l.getType().getId(),
                                        lightDataIndex);
            lightDataIndex++;

            switch (l.getType()) {
                case Directional:
                    DirectionalLight dl = (DirectionalLight) l;
                    Vector3f dir = dl.getDirection();
                    //Data directly sent in view space to avoid a matrix mult for each pixel
                    tmpVec.set(dir.getX(), dir.getY(), dir.getZ(), 0.0f);
                    rm.getCurrentCamera().getViewMatrix().mult(tmpVec, tmpVec);
//                        tmpVec.divideLocal(tmpVec.w);
//                        tmpVec.normalizeLocal();
                    lightData.setVector4fInArray(tmpVec.getX(), tmpVec.getY(), tmpVec.getZ(), -1, lightDataIndex);
                    lightDataIndex++;
                    //PADDING
                    lightData.setVector4fInArray(0, 0, 0, 0, lightDataIndex);
                    lightDataIndex++;
                    break;
                case Point:
                    PointLight pl = (PointLight) l;
                    Vector3f pos = pl.getPosition();
                    float invRadius = pl.getInvRadius();
                    tmpVec.set(pos.getX(), pos.getY(), pos.getZ(), 1.0f);
                    rm.getCurrentCamera().getViewMatrix().mult(tmpVec, tmpVec);
                    //tmpVec.divideLocal(tmpVec.w);
                    lightData.setVector4fInArray(tmpVec.getX(), tmpVec.getY(), tmpVec.getZ(), invRadius, lightDataIndex);
                    lightDataIndex++;
                    //PADDING
                    lightData.setVector4fInArray(0, 0, 0, 0, lightDataIndex);
                    lightDataIndex++;
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown type of light: " + l.getType());
            }
        }
        vars.release();
        //Padding of unsued buffer space
        while(lightDataIndex < numLights * 3) {
            lightData.setVector4fInArray(0f, 0f, 0f, 0f, lightDataIndex);
            lightDataIndex++;
        }
        return curIndex;
    }

    @Override
    public void render(RenderManager renderManager, ShaderProgram shader, Geometry geometry, LightList lights, int lastTexUnit) {
        int nbRenderedLights = 0;
        GLRenderer renderer = renderManager.getRenderer();
        int batchSize = renderManager.getSinglePassLightBatchSize();
        if (lights.size() == 0) {
            updateLightListUniforms(shader, geometry, lights, batchSize, renderManager, 0);
            renderer.setShader(shader);
            renderMeshFromGeometry(renderer, geometry);
        } else {
            while (nbRenderedLights < lights.size()) {
                nbRenderedLights = updateLightListUniforms(shader, geometry, lights, batchSize, renderManager, nbRenderedLights);
                renderer.setShader(shader);
                renderMeshFromGeometry(renderer, geometry);
            }
        }
    }
}
