package mini.material.logic;

import mini.light.LightList;
import mini.material.MatParam;
import mini.material.RenderState;
import mini.renderEngine.Caps;
import mini.renderEngine.RenderManager;
import mini.scene.Geometry;
import mini.shaders.DefineList;
import mini.shaders.ShaderProgram;
import mini.shaders.Uniform;
import mini.shaders.UniformBinding;
import mini.textures.Texture;

import java.util.EnumSet;

/**
 * <code>TechniqueDefLogic</code> is used to customize how
 * a material should be rendered.
 *
 * Typically used to implement {@link LightMode lighting modes}.
 * Implementations can register
 * {@link TechniqueDef#addShaderUnmappedDefine(java.lang.String) unmapped defines}
 * in their constructor and then later set them based on the geometry
 * or light environment being rendered.
 *
 * @author miniwolf
 */
public interface TechniqueDefLogic {
    /**
     * Determine the shader to use for the given geometry / material combination.
     *
     * @param renderManager The render manager for which rendering is to be performed.
     * @param lights The lights with which the geometry shall be rendered. This
     * list must not include culled lights.
     * @param defines The define list used by the technique, any
     * {@link TechniqueDef#addShaderUnmappedDefine(java.lang.String) unmapped defines}
     * should be set here to change shader behavior.
     *
     * @return The shader to use for rendering.
     */
    ShaderProgram makeCurrent(RenderManager renderManager, EnumSet<Caps> rendererCaps, LightList lights, DefineList defines);

    /**
     * Requests that the <code>TechniqueDefLogic</code> renders the given geometry.
     *
     * Fixed material functionality such as {@link RenderState},
     * {@link MatParam material parameters}, and
     * {@link UniformBinding uniform bindings}
     * have already been applied by the material, however,
     * {@link RenderState}, {@link Uniform uniforms}, {@link Texture textures},
     * can still be overriden.
     *
     * @param renderManager The render manager to perform the rendering against.
     * @param shader The shader
     * @param geometry The geometry to render
     */
    void render(RenderManager renderManager, ShaderProgram shader, Geometry geometry,
                LightList lights, int lastTexUnit);
}
