package mini.renderEngine.opengl;

import mini.material.RenderState;
import mini.math.ColorRGBA;
import mini.math.Quaternion;
import mini.math.Vector2f;
import mini.math.Vector3f;
import mini.math.Vector4f;
import mini.renderEngine.IDList;
import mini.renderEngine.Limits;
import mini.renderEngine.RenderContext;
import mini.scene.Geometry;
import mini.scene.Mesh;
import mini.scene.VertexBuffer;
import mini.shaders.Attribute;
import mini.shaders.ShaderProgram;
import mini.shaders.ShaderSource;
import mini.shaders.Uniform;
import mini.shaders.tools.ShaderDebug;
import mini.textures.Image;
import mini.textures.Texture;
import mini.textures.image.LastTextureState;
import mini.utils.MipMapGenerator;
import org.lwjgl.opengl.ARBDrawBuffers;
import org.lwjgl.opengl.ARBInstancedArrays;
import org.lwjgl.opengl.EXTFramebufferMultisample;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.EXTTextureArray;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL41;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author miniwolf
 */
public class GLRenderer {
    private final RenderContext context = new RenderContext();
    private final EnumMap<Limits, Integer> limits = new EnumMap<>(Limits.class);
    private final TextureUtil texUtil = new TextureUtil();
    private boolean linearizeSrgbImages;
    private HashSet<String> extensions = new HashSet<>();

    private int convertFormat(VertexBuffer.Format format) {
        switch (format) {
            case Byte:
                return GL11.GL_BYTE;
            case UnsignedByte:
                return GL11.GL_UNSIGNED_BYTE;
            case Short:
                return GL11.GL_SHORT;
            case UnsignedShort:
                return GL11.GL_UNSIGNED_SHORT;
            case Int:
                return GL11.GL_INT;
            case UnsignedInt:
                return GL11.GL_UNSIGNED_INT;
            case Float:
                return GL11.GL_FLOAT;
            case Double: // TODO: Not sure this is viable on a GPU
                return GL11.GL_DOUBLE;
            default:
                throw new UnsupportedOperationException("Unknown buffer format.");
        }
    }

    private int convertUsage(VertexBuffer.Usage usage) {
        switch (usage) {
            case Static:
                return GL15.GL_STATIC_DRAW;
            case Dynamic:
                return GL15.GL_DYNAMIC_DRAW;
            case Stream:
                return GL15.GL_STREAM_DRAW;
            default:
                throw new UnsupportedOperationException("Unknown usage type.");
        }
    }

    private int convertElementMode(Mesh.Mode mode) {
        switch (mode) {
            case Triangles:
                return GL11.GL_TRIANGLES;
            default:
                throw new UnsupportedOperationException("Unrecognized mesh mode: " + mode);
        }
    }

    private int convertTextureType(Texture.Type type, int samples, int face) {
        switch (type) {
            case TwoDimensional:
                if (samples > 1) {
                    return GL32.GL_TEXTURE_2D_MULTISAMPLE;
                } else {
                    return GL11.GL_TEXTURE_2D;
                }
            case TwoDimensionalArray:
                if (samples > 1) {
                    return GL32.GL_TEXTURE_2D_MULTISAMPLE_ARRAY;
                } else {
                    return EXTTextureArray.GL_TEXTURE_2D_ARRAY_EXT;
                }
            case ThreeDimensional:
                return GL12.GL_TEXTURE_3D;
            case CubeMap:
                if (face < 0) {
                    return GL13.GL_TEXTURE_CUBE_MAP;
                } else if (face < 6) {
                    return GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + face;
                } else {
                    throw new UnsupportedOperationException("Invalid cube map face index: " + face);
                }
            default:
                throw new UnsupportedOperationException("Unknown texture type: " + type);
        }
    }

    private int convertMagFilter(Texture.MagFilter filter) {
        switch (filter) {
            case Bilinear:
                return GL11.GL_LINEAR;
            case Nearest:
                return GL11.GL_NEAREST;
            default:
                throw new UnsupportedOperationException("Unknown mag filter: " + filter);
        }
    }

    private int convertMinFilter(Texture.MinFilter filter, boolean haveMips) {
        if (haveMips){
            switch (filter) {
                case Trilinear:
                    return GL11.GL_LINEAR_MIPMAP_LINEAR;
                case BilinearNearestMipMap:
                    return GL11.GL_LINEAR_MIPMAP_NEAREST;
                case NearestLinearMipMap:
                    return GL11.GL_NEAREST_MIPMAP_LINEAR;
                case NearestNearestMipMap:
                    return GL11.GL_NEAREST_MIPMAP_NEAREST;
                case BilinearNoMipMaps:
                    return GL11.GL_LINEAR;
                case NearestNoMipMaps:
                    return GL11.GL_NEAREST;
                default:
                    throw new UnsupportedOperationException("Unknown min filter: " + filter);
            }
        } else {
            switch (filter) {
                case Trilinear:
                case BilinearNearestMipMap:
                case BilinearNoMipMaps:
                    return GL11.GL_LINEAR;
                case NearestLinearMipMap:
                case NearestNearestMipMap:
                case NearestNoMipMaps:
                    return GL11.GL_NEAREST;
                default:
                    throw new UnsupportedOperationException("Unknown min filter: " + filter);
            }
        }
    }

    private int convertWrapMode(Texture.WrapMode mode) {
        switch (mode) {
            case EdgeClamp:
                // Falldown intentional.
                return GL12.GL_CLAMP_TO_EDGE;
            case Repeat:
                return GL11.GL_REPEAT;
            case MirroredRepeat:
                return GL14.GL_MIRRORED_REPEAT;
            default:
                throw new UnsupportedOperationException("Unknown wrap mode: " + mode);
        }
    }

    private int convertShaderType(ShaderProgram.ShaderType type) {
        switch (type) {
            case Fragment:
                return GL20.GL_FRAGMENT_SHADER;
            case Vertex:
                return GL20.GL_VERTEX_SHADER;
            case Geometry:
                return GL32.GL_GEOMETRY_SHADER;
            case TessellationControl:
                return GL40.GL_TESS_CONTROL_SHADER;
            case TessellationEvaluation:
                return GL40.GL_TESS_EVALUATION_SHADER;
            default:
                throw new UnsupportedOperationException("Unrecognized shader type.");
        }
    }

    private int convertBlendFunc(RenderState.BlendFunc blendFunc) {
        switch (blendFunc) {
            case Zero:
                return GL11.GL_ZERO;
            case One:
                return GL11.GL_ONE;
            case Src_Color:
                return GL11.GL_SRC_COLOR;
            case One_Minus_Src_Color:
                return GL11.GL_ONE_MINUS_SRC_COLOR;
            case Dst_Color:
                return GL11.GL_DST_COLOR;
            case One_Minus_Dst_Color:
                return GL11.GL_ONE_MINUS_DST_COLOR;
            case Src_Alpha:
                return GL11.GL_SRC_ALPHA;
            case One_Minus_Src_Alpha:
                return GL11.GL_ONE_MINUS_SRC_ALPHA;
            case Dst_Alpha:
                return GL11.GL_DST_ALPHA;
            case One_Minus_Dst_Alpha:
                return GL11.GL_ONE_MINUS_DST_ALPHA;
            case Src_Alpha_Saturate:
                return GL11.GL_SRC_ALPHA_SATURATE;
            default:
                throw new UnsupportedOperationException("Unrecognized blend function operation: " + blendFunc);
        }
    }

    private int convertTestFunction(RenderState.TestFunction testFunc) {
        switch (testFunc) {
            case Never:
                return GL11.GL_NEVER;
            case Less:
                return GL11.GL_LESS;
            case LessOrEqual:
                return GL11.GL_LEQUAL;
            case Greater:
                return GL11.GL_GREATER;
            case GreaterOrEqual:
                return GL11.GL_GEQUAL;
            case Equal:
                return GL11.GL_EQUAL;
            case NotEqual:
                return GL11.GL_NOTEQUAL;
            case Always:
                return GL11.GL_ALWAYS;
            default:
                throw new UnsupportedOperationException("Unrecognized test function: " + testFunc);
        }
    }

    private int convertBlendEquation(RenderState.BlendEquation blendEquation) {
        switch (blendEquation) {
            case Add:
                return GL14.GL_FUNC_ADD;
            case Subtract:
                return GL14.GL_FUNC_SUBTRACT;
            case ReverseSubtract:
                return GL14.GL_FUNC_REVERSE_SUBTRACT;
            case Min:
                return GL14.GL_MIN;
            case Max:
                return GL14.GL_MAX;
            default:
                throw new UnsupportedOperationException("Unrecognized blend operation: " + blendEquation);
        }
    }

    private int convertBlendEquationAlpha(RenderState.BlendEquationAlpha blendEquationAlpha) {
        //Note: InheritColor mode should already be handled, that is why it does not belong the the switch case.
        switch (blendEquationAlpha) {
            case Add:
                return GL14.GL_FUNC_ADD;
            case Subtract:
                return GL14.GL_FUNC_SUBTRACT;
            case ReverseSubtract:
                return GL14.GL_FUNC_REVERSE_SUBTRACT;
            case Min:
                return GL14.GL_MIN;
            case Max:
                return GL14.GL_MAX;
            default:
                throw new UnsupportedOperationException("Unrecognized alpha blend operation: " + blendEquationAlpha);
        }
    }

    private int convertStencilOperation(RenderState.StencilOperation stencilOp) {
        switch (stencilOp) {
            case Keep:
                return GL11.GL_KEEP;
            case Zero:
                return GL11.GL_ZERO;
            case Replace:
                return GL11.GL_REPLACE;
            case Increment:
                return GL11.GL_INCR;
            case IncrementWrap:
                return GL14.GL_INCR_WRAP;
            case Decrement:
                return GL11.GL_DECR;
            case DecrementWrap:
                return GL14.GL_DECR_WRAP;
            case Invert:
                return GL11.GL_INVERT;
            default:
                throw new UnsupportedOperationException("Unrecognized stencil operation: " + stencilOp);
        }
    }

    private HashSet<String> loadExtensions() {
        HashSet<String> extensionSet = new HashSet<>(64);
        int extensionCount = GL30.glGetIntegeri(GL30.GL_NUM_EXTENSIONS, 0);
        for (int i = 0; i < extensionCount; i++) {
            String extension = GL30.glGetStringi(GL11.GL_EXTENSIONS, i);
            extensionSet.add(extension);
        }
        return extensionSet;
    }

    private int getInteger(int en) {
        return GL11.glGetInteger(en);
    }

    private boolean hasExtension(String extensionName) {
        return extensions.contains(extensionName);
    }

    private void loadCapabilities() {
        extensions = loadExtensions();

        limits.put(Limits.VertexTextureUnits, getInteger(GL20.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS));
        limits.put(Limits.FragmentTextureUnits, getInteger(GL20.GL_MAX_TEXTURE_IMAGE_UNITS));

        limits.put(Limits.FragmentUniformVectors, getInteger(
                GL41.GL_MAX_FRAGMENT_UNIFORM_VECTORS));
        limits.put(Limits.VertexUniformVectors, getInteger(GL41.GL_MAX_VERTEX_UNIFORM_VECTORS));

        limits.put(Limits.VertexAttributes, getInteger(GL20.GL_MAX_VERTEX_ATTRIBS));
        limits.put(Limits.TextureSize, getInteger(GL11.GL_MAX_TEXTURE_SIZE));
        limits.put(Limits.CubemapSize, getInteger(GL13.GL_MAX_CUBE_MAP_TEXTURE_SIZE));

        // == texture format extensions ==
        if (hasExtension("GL_EXT_texture_filter_anisotropic")) {
            limits.put(Limits.TextureAnisotropy, getInteger(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT));
        }

        if (hasExtension("GL_EXT_framebuffer_object")) {
            limits.put(Limits.RenderBufferSize, getInteger(EXTFramebufferObject.GL_MAX_RENDERBUFFER_SIZE_EXT));
            limits.put(Limits.FrameBufferAttachments, getInteger(EXTFramebufferObject.GL_MAX_COLOR_ATTACHMENTS_EXT));


            if (hasExtension("GL_EXT_framebuffer_multisample")) {
                limits.put(Limits.FrameBufferSamples, getInteger(EXTFramebufferMultisample.GL_MAX_SAMPLES_EXT));
            }

            if (hasExtension("GL_ARB_texture_multisample")) {
                limits.put(Limits.ColorTextureSamples, getInteger(GL32.GL_MAX_COLOR_TEXTURE_SAMPLES));
                limits.put(Limits.DepthTextureSamples, getInteger(GL32.GL_MAX_DEPTH_TEXTURE_SAMPLES));
                if (!limits.containsKey(Limits.FrameBufferSamples)) {
                    // In case they want to query samples on main FB ...
                    limits.put(Limits.FrameBufferSamples, limits.get(Limits.ColorTextureSamples));
                }
            }

            if (hasExtension("GL_ARB_draw_buffers")) {
                limits.put(Limits.FrameBufferMrtAttachments,
                           getInteger(ARBDrawBuffers.GL_MAX_DRAW_BUFFERS_ARB));
            }
        }
    }

    public void initialize() {
        loadCapabilities();
    }

    public void renderMesh(Mesh mesh) {
        if (mesh.getVertexCount() == 0 || mesh.getTriangleCount() == 0) {
            return;
        }

        renderMeshDefault(mesh);
    }

    private void renderMeshDefault(Mesh mesh) {

        // Here while count is still passed in.  Can be removed when/if
        // the method is collapsed again.  -pspeed

        VertexBuffer interleavedData = mesh.getBuffer(VertexBuffer.Type.InterleavedData);
        if (interleavedData != null && interleavedData.isUpdateNeeded()) {
            updateBufferData(interleavedData);
        }

        VertexBuffer indices = mesh.getBuffer(VertexBuffer.Type.Index);

        for (VertexBuffer vb : mesh.getBufferList()) {
            if (vb.getBufferType() == VertexBuffer.Type.InterleavedData
                || vb.getUsage() == VertexBuffer.Usage.CpuOnly // ignore cpu-only buffers
                || vb.getBufferType() == VertexBuffer.Type.Index) {
                continue;
            }

            // not interleaved
            setVertexAttrib(vb);
        }

        clearVertexAttribs();

        if (indices != null) {
            drawTriangleList(indices, mesh);
        } else {
            drawTriangleArray(mesh.getMode(), mesh.getVertexCount());
        }
    }

    public void renderMeshFromGeometry(Geometry geometry) {
        Mesh mesh = geometry.getMesh();
        VertexBuffer indices = mesh.getBuffer(VertexBuffer.Type.Index);

        for (VertexBuffer vb : mesh.getBufferList()) {
            if (vb.getBufferType() == VertexBuffer.Type.Index) {
                continue;
            }
            setVertexAttrib(vb);
        }

        if (indices != null) {
            drawTriangleList(indices, mesh);
        } else {
            drawTriangleArray(mesh.getMode(), mesh.getVertexCount());
        }
    }

    private void drawTriangleArray(Mesh.Mode mode, int vertCount) {
        GL11.glDrawArrays(convertElementMode(mode), 0, vertCount);
    }

    private void drawTriangleList(VertexBuffer indexBuf, Mesh mesh) {
        if (indexBuf.getBufferType() != VertexBuffer.Type.Index) {
            throw new IllegalArgumentException("Only index buffers are allowed as triangle lists.");
        }

        // TODO: Maybe check for supported indexBuf.FORMAT unsignedInt may not be supported always

        if (indexBuf.isUpdateNeeded()) {
            updateBufferData(indexBuf);
        }

        int bufId = indexBuf.getId();
        assert bufId != -1;

        if (context.boundElementArrayVBO != bufId) {
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, bufId);
            context.boundElementArrayVBO = bufId;
        }

        int vertCount = mesh.getVertexCount();
        GL12.glDrawRangeElements(convertElementMode(mesh.getMode()),
                                 0,
                                 vertCount,
                                 indexBuf.getData().limit(),
                                 convertFormat(indexBuf.getFormat()),
                                 0);
    }

    private void clearVertexAttribs() {
        IDList attribList = context.attribIndexList;
        for (int i = 0; i < attribList.oldLen; i++) {
            int idx = attribList.oldList[i];
            GL20.glDisableVertexAttribArray(idx);
            if (context.boundAttribs[idx].isInstanced()) {
                ARBInstancedArrays.glVertexAttribDivisorARB(idx, 0);
            }
            context.boundAttribs[idx] = null;
        }
        context.attribIndexList.copyNewToOld();
    }

    private void setVertexAttrib(VertexBuffer vb) {
        if (vb.getBufferType() == VertexBuffer.Type.Index) {
            throw new IllegalArgumentException(
                    "Index buffers not allowed to be set to vertex attrib");
        }

        Attribute attrib = context.boundShader.getAttribute(vb.getBufferType());
        int loc = attrib.getLocation();
        if (loc == -1) {
            return; // not defined
        }
        if (loc == -2) {
            loc = GL20.glGetAttribLocation(context.boundShaderProgram,
                                           "in" + vb.getBufferType().name());

            // not really the name of it in the shader (inPosition) but
            // the internal name of the enum (Position).
            if (loc < 0) {
                attrib.setLocation(-1);
                return; // not available in shader.
            } else {
                attrib.setLocation(loc);
            }
        }

        int slotsRequired = 1;
        if (vb.getNumComponents() > 4) {
            if (vb.getNumComponents() % 4 != 0) {
                throw new RuntimeException("Number of components in multi-slot "
                                           + "buffers must be divisible by 4");
            }
            slotsRequired = vb.getNumComponents() / 4;
        }

        if (vb.isUpdateNeeded()) {
            updateBufferData(vb);
        }

        VertexBuffer[] attribs = context.boundAttribs;
        for (int i = 0; i < slotsRequired; i++) {
            if (!context.attribIndexList.moveToNew(loc + i)) {
                GL20.glEnableVertexAttribArray(loc + i);
            }
        }
        if (attribs[loc] != vb) {
            // NOTE: Use id from interleaved buffer if specified
            int bufId = vb.getId();
            assert bufId != -1;
            if (context.boundArrayVBO != bufId) {
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufId);
                context.boundArrayVBO = bufId;
            }

            if (slotsRequired == 1) {
                GL20.glVertexAttribPointer(loc,
                                           vb.getNumComponents(),
                                           convertFormat(vb.getFormat()),
                                           vb.isNormalized(),
                                           vb.getStride(),
                                           vb.getOffset());
            } else {
                for (int i = 0; i < slotsRequired; i++) {
                    // The pointer maps the next 4 floats in the slot.
                    // E.g.
                    // P1: XXXX____________XXXX____________
                    // P2: ____XXXX____________XXXX________
                    // P3: ________XXXX____________XXXX____
                    // P4: ____________XXXX____________XXXX
                    // stride = 4 bytes in float * 4 floats in slot * num slots
                    // offset = 4 bytes in float * 4 floats in slot * slot index
                    GL20.glVertexAttribPointer(loc + i,
                                               4,
                                               convertFormat(vb.getFormat()),
                                               vb.isNormalized(),
                                               4 * 4 * slotsRequired,
                                               4 * 4 * i);
                }
            }

            for (int i = 0; i < slotsRequired; i++) {
                int slot = loc + i;
                if (vb.isInstanced() && (attribs[slot] == null || !attribs[slot].isInstanced())) {
                    // non-instanced -> instanced
                    GL33.glVertexAttribDivisor(slot, vb.getInstanceSpan());
                }
                attribs[slot] = vb;
            }
        }
    }

    private void updateBufferData(VertexBuffer vb) {
        int bufId = vb.getId();
        if (bufId == -1) {
            // create buffer
            bufId = GL15.glGenBuffers();
            vb.setId(bufId);
        }

        // bind buffer
        int target;
        if (vb.getBufferType() == VertexBuffer.Type.Index) {
            target = GL15.GL_ELEMENT_ARRAY_BUFFER;
            if (context.boundElementArrayVBO != bufId) {
                GL15.glBindBuffer(target, bufId);
                context.boundElementArrayVBO = bufId;
            }
        } else {
            target = GL15.GL_ARRAY_BUFFER;
            if (context.boundArrayVBO != bufId) {
                GL15.glBindBuffer(target, bufId);
                context.boundArrayVBO = bufId;
            }
        }

        int usage = convertUsage(vb.getUsage());
        vb.getData().rewind();

        switch (vb.getFormat()) {
            case Byte:
            case UnsignedByte:
                GL15.glBufferData(target, (ByteBuffer) vb.getData(), usage);
                break;
            case Short:
            case UnsignedShort:
                GL15.glBufferData(target, (ShortBuffer) vb.getData(), usage);
                break;
            case Int:
            case UnsignedInt:
                GL15.glBufferData(target, (IntBuffer) vb.getData(), usage);
                break;
            case Float:
                GL15.glBufferData(target, (FloatBuffer) vb.getData(), usage);
                break;
            default:
                throw new UnsupportedOperationException("Unknown buffer format.");
        }

        vb.clearUpdateNeeded();
    }

    private void bindProgram(ShaderProgram shader) {
        int shaderId = shader.getId();
        if (context.boundShaderProgram != shaderId) {
            GL20.glUseProgram(shaderId);
            context.boundShader = shader;
            context.boundShaderProgram = shaderId;
        }
    }

    private void updateUniformLocation(ShaderProgram shader, Uniform uniform) {
        int loc = GL20.glGetUniformLocation(shader.getId(), uniform.getName());
        if (loc < 0) {
            uniform.setLocation(-1);
            // uniform is not declared in shader
            System.err.println("Uniform " + uniform.getName() + "{0} is not declared in shader"
                               + shader.getSources());
        } else {
            uniform.setLocation(loc);
        }
    }

    private void updateUniform(ShaderProgram shader, Uniform uniform) {
        assert uniform.getName() != null;
        assert shader.getId() > 0;

        bindProgram(shader);

        int loc = uniform.getLocation();
        if (loc == -1) {
            return;
        }

        if (loc == -2) {
            // get uniform location
            updateUniformLocation(shader, uniform);
            if (uniform.getLocation() == -1) {
                // not declared, ignore
                uniform.clearUpdateNeeded();
                return;
            }
            loc = uniform.getLocation();
        }

        if (uniform.getVarType() == null) {
            return; // value not set yet..
        }

        uniform.clearUpdateNeeded();
        FloatBuffer fb;
        switch (uniform.getVarType()) {
            case Float:
                Float f = (Float) uniform.getValue();
                GL20.glUniform1f(loc, f);
                break;
            case Vector2:
                Vector2f v2 = (Vector2f) uniform.getValue();
                GL20.glUniform2f(loc, v2.getX(), v2.getY());
                break;
            case Vector3:
                Vector3f v3 = (Vector3f) uniform.getValue();
                GL20.glUniform3f(loc, v3.getX(), v3.getY(), v3.getZ());
                break;
            case Vector4:
                Object val = uniform.getValue();
                if (val instanceof ColorRGBA) {
                    ColorRGBA c = (ColorRGBA) val;
                    GL20.glUniform4f(loc, c.r, c.g, c.b, c.a);
                } else if (val instanceof Vector4f) {
                    Vector4f c = (Vector4f) val;
                    GL20.glUniform4f(loc, c.x, c.y, c.z, c.w);
                } else {
                    Quaternion c = (Quaternion) uniform.getValue();
                    GL20.glUniform4f(loc, c.getX(), c.getY(), c.getZ(), c.getW());
                }
                break;
            case Boolean:
                Boolean b = (Boolean) uniform.getValue();
                GL20.glUniform1i(loc, b ? GL11.GL_TRUE : GL11.GL_FALSE);
                break;
            case Matrix4f:
                fb = uniform.getMultiData();
                assert fb.remaining() == 16;
                GL20.glUniformMatrix4fv(loc, false, fb);
                break;
            case Int:
                Integer i = (Integer) uniform.getValue();
                GL20.glUniform1i(loc, i);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported uniform type: " + uniform.getVarType());
        }
    }

    private void updateShaderUniforms(ShaderProgram shader) {
        Map<String, Uniform> uniforms = shader.getUniformMap();
        for (Uniform uniform : uniforms.values()) {
            if (uniform.isUpdateNeeded()) {
                updateUniform(shader, uniform);
            }
        }
    }

    private void resetUniformLocations(ShaderProgram shader) {
        Map<String, Uniform> uniforms = shader.getUniformMap();
        for (Uniform uniform : uniforms.values()) {
            uniform.reset(); // e.g check location again
        }
    }

    private void updateShaderSourceData(ShaderSource source) {
        int id = source.getId();
        if (id == -1) {
            // Create id
            id = GL20.glCreateShader(convertShaderType(source.getType()));
            if (id <= 0) {
                throw new RuntimeException("Invalid ID received when trying to create shader.");
            }

            source.setId(id);
        } else {
            throw new RuntimeException("Cannot recompile shader source");
        }


        // Upload shader source.
        // Merge the defines and source code.
        StringBuilder stringBuf = new StringBuilder(250);
        stringBuf.setLength(0);

        if (linearizeSrgbImages) {
            stringBuf.append("#define SRGB 1\n");
        }
        //stringBuf.append("#define ").append(source.getType().name().toUpperCase()).append("_SHADER 1\n");

        //stringBuf.append(source.getDefines());
        stringBuf.append(source.getSource());

        GL20.glShaderSource(id, stringBuf.toString());
        GL20.glCompileShader(id);

        boolean compiledOK = GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) == GL11.GL_TRUE;
        String infoLog = null;

        if (!compiledOK) {
            // even if compile succeeded, check
            // log for warnings
            int length = GL20.glGetShaderi(id, GL20.GL_INFO_LOG_LENGTH);
            if (length > 3) {
                // get infos
                infoLog = GL20.glGetShaderInfoLog(id, length);
                System.err.println(source.getName() + " compiled successfully, compiler "
                                   + "warnings: \n" + infoLog);

            }
        }

        if (compiledOK) {
            source.clearUpdateNeeded();
        } else {
            System.err.println("Bad compile of:\n" +
                               ShaderDebug.formatShaderSource(stringBuf.toString()));
            if (infoLog != null) {
                throw new RuntimeException("compile error in: " + source + "\n" + infoLog);
            } else {
                throw new RuntimeException("compile error in: " + source + "\nerror: <not provided>");
            }
        }
    }

    private void updateShaderData(ShaderProgram shader) {
        int id = shader.getId();
        if (id == -1) {
            // create program
            id = GL20.glCreateProgram();
            if (id == 0) {
                throw new RuntimeException("Invalid ID (" + id
                                           + ") received when trying to create shader program.");
            }

            shader.setId(id);
        }

        // If using GLSL 1.5, we bind the outputs for the user
        // For versions 3.3 and up, user should use layout qualifiers instead.
        boolean bindFragDataRequired = false;

        for (ShaderSource source : shader.getSources()) {
            if (source.isUpdateNeeded()) {
                updateShaderSourceData(source);
            }
            if (source.getType() == ShaderProgram.ShaderType.Fragment) {
                bindFragDataRequired = true;
            }
            GL20.glAttachShader(id, source.getId());
        }

        if (bindFragDataRequired) {
            // Check if GLSL version is 1.5 for shader
            GL30.glBindFragDataLocation(id, 0, "out_Color");
            // For MRT
//            for (int i = 0; i < limits.get(Limits.FrameBufferMrtAttachments); i++) {
//                GL30.glBindFragDataLocation(id, i, "outFragData[" + i + "]");
//            }
        }

        // Link shaders to program
        GL20.glLinkProgram(id);

        // Check link status
        boolean linkOK = GL20.glGetProgrami(id, GL20.GL_LINK_STATUS) == GL11.GL_TRUE;
        String infoLog = null;

        if (!linkOK) {
            int length = GL20.glGetProgrami(id, GL20.GL_INFO_LOG_LENGTH);
            if (length > 3) {
                // get infos
                infoLog = GL20.glGetProgramInfoLog(id, length);
            }
        }

        if (linkOK) {
            shader.clearUpdateNeeded();
            // OpenGL spec: uniform locations may change after re-link
            resetUniformLocations(shader);
        } else {
            if (infoLog != null) {
                throw new RuntimeException("Shader failed to link, shader:" + shader + "\n" + infoLog);
            } else {
                throw new RuntimeException("Shader failed to link, shader:" + shader + "\ninfo: <not provided>");
            }
        }
    }

    public void setShader(ShaderProgram shader) {
        if (shader == null) {
            throw new IllegalArgumentException("Shader cannot be null");
        } else {
            if (shader.isUpdateNeeded()) {
                updateShaderData(shader);
            }

            // NOTE: might want to check if any of the
            // sources need an update?

            assert shader.getId() > 0;

            updateShaderUniforms(shader);
            bindProgram(shader);
        }
    }

    /**
     * Uploads the given image to the GL driver.
     *
     * @param img The image to upload
     * @param type How the data in the image argument should be interpreted.
     * @param unit The texture slot to be used to upload the image, not important
     * @param scaleToPot If true, the image will be scaled to power-of-2 dimensions
     * before being uploaded.
     */
    private void updateTexImageData(Image img, Texture.Type type, int unit, boolean scaleToPot) {
        int texId = img.getId();
        if (texId == -1) {
            // create texture
            texId = GL11.glGenTextures();
            img.setId(texId);
            //objManager.registerObject(img);

            //statistics.onNewTexture();
        }

        // bind texture
        int target = convertTextureType(type, img.getMultiSamples(), -1);
        bindTextureAndUnit(target, img, unit);

        if (!img.hasMipmaps() && img.isGeneratedMipmapsRequired()) {
            // Image does not have mipmaps, but they are required.
            // Generate from base level.

            // For OpenGL3 and up.
            // We'll generate mipmaps via glGenerateMipmapEXT (see below)
        } else if (img.hasMipmaps()) {
            // Image already has mipmaps, set the max level based on the
            // number of mipmaps we have.
            GL11.glTexParameteri(target, GL12.GL_TEXTURE_MAX_LEVEL, img.getMipMapSizes().length - 1);
        } else {
            // Image does not have mipmaps and they are not required.
            // Specify that that the texture has no mipmaps.
            GL11.glTexParameteri(target, GL12.GL_TEXTURE_MAX_LEVEL, 0);
        }

        int imageSamples = img.getMultiSamples();
        if (imageSamples > 1) {
            if (img.getFormat().isDepthFormat()) {
                img.setMultiSamples(Math.min(limits.get(Limits.DepthTextureSamples), imageSamples));
            } else {
                img.setMultiSamples(Math.min(limits.get(Limits.ColorTextureSamples), imageSamples));
            }
        }

        if (target == GL13.GL_TEXTURE_CUBE_MAP) {
            // Check max texture size before upload
            int cubeSize = limits.get(Limits.CubemapSize);
            if (img.getWidth() > cubeSize || img.getHeight() > cubeSize) {
                throw new RuntimeException("Cannot upload cubemap " + img + ". The maximum supported cubemap resolution is " + cubeSize);
            }
            if (img.getWidth() != img.getHeight()) {
                throw new RuntimeException("Cubemaps must have square dimensions");
            }
        } else {
            int texSize = limits.get(Limits.TextureSize);
            if (img.getWidth() > texSize || img.getHeight() > texSize) {
                throw new RuntimeException("Cannot upload texture " + img + ". The maximum supported texture resolution is " + texSize);
            }
        }

        Image imageForUpload;
        if (scaleToPot) {
            imageForUpload = MipMapGenerator.resizeToPowerOf2(img);
        } else {
            imageForUpload = img;
        }
        if (target == GL13.GL_TEXTURE_CUBE_MAP) {
            List<ByteBuffer> data = imageForUpload.getData();
            if (data.size() != 6) {
                System.err.println("Warning: Invalid texture: img\n Cubemap textures must contain "
                                   + "6 data units.");
                return;
            }
            for (int i = 0; i < 6; i++) {
                texUtil.uploadTexture(imageForUpload, GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, i);
            }
        } else if (target == EXTTextureArray.GL_TEXTURE_2D_ARRAY_EXT) {
            List<ByteBuffer> data = imageForUpload.getData();

            // -1 index specifies prepare data for 2D Array
            texUtil.uploadTexture(imageForUpload, target, -1);

            for (int i = 0; i < data.size(); i++) {
                // upload each slice of 2D array in turn
                // this time with the appropriate index
                texUtil.uploadTexture(imageForUpload, target, i);
            }
        } else {
            texUtil.uploadTexture(imageForUpload, target, 0);
        }

        if (img.getMultiSamples() != imageSamples) {
            img.setMultiSamples(imageSamples);
        }

        if (!img.hasMipmaps() && img.isGeneratedMipmapsRequired() && img.getData(0) != null) {
            GL30.glGenerateMipmap(target);
            img.setMipmapsGenerated(true);
        }

        img.clearUpdateNeeded();
    }

    public void setTexture(int unit, Texture tex) {
        Image image = tex.getImage();
        if (image.isUpdateNeeded() || (image.isGeneratedMipmapsRequired() && !image
                .isMipmapsGenerated())) {
            // Check NPOT requirements
            boolean scaleToPot = false;

            try {
                checkNonPowerOfTwo(tex);
            } catch (UnsupportedOperationException ex) {
                scaleToPot = true;
            }

            updateTexImageData(image, tex.getType(), unit, scaleToPot);
        }

        int texId = image.getId();
        assert texId != -1;

        setupTextureParams(unit, tex);
    }

    private void setupTextureParams(int unit, Texture tex) {
        Image image = tex.getImage();
        int target = convertTextureType(tex.getType(), image != null ? image.getMultiSamples() : 1, -1);

        boolean haveMips = true;
        if (image != null) {
            haveMips = image.isGeneratedMipmapsRequired() || image.hasMipmaps();
        }

        LastTextureState curState = image.getLastTextureState();

        if (curState.magFilter != tex.getMagFilter()) {
            bindTextureAndUnit(target, image, unit);
            GL11.glTexParameteri(target, GL11.GL_TEXTURE_MAG_FILTER, convertMagFilter(tex.getMagFilter()));
            curState.magFilter = tex.getMagFilter();
        }
        if (curState.minFilter != tex.getMinFilter()) {
            bindTextureAndUnit(target, image, unit);
            GL11.glTexParameteri(target, GL11.GL_TEXTURE_MIN_FILTER, convertMinFilter(tex.getMinFilter(), haveMips));
            curState.minFilter = tex.getMinFilter();
        }

//        int desiredAnisoFilter = tex.getAnisotropicFilter() == 0
//                                 ? defaultAnisotropicFilter
//                                 : tex.getAnisotropicFilter();

//        if (caps.contains(Caps.TextureFilterAnisotropic)
//            && curState.anisoFilter != desiredAnisoFilter) {
//            bindTextureAndUnit(target, image, unit);
//            gl.glTexParameterf(target,
//                               GLExt.GL_TEXTURE_MAX_ANISOTROPY_EXT,
//                               desiredAnisoFilter);
//            curState.anisoFilter = desiredAnisoFilter;
//        }

        switch (tex.getType()) {
            case ThreeDimensional:
            case CubeMap: // cubemaps use 3D coords
                if (curState.rWrap != tex.getWrap(Texture.WrapAxis.R)) {
                    bindTextureAndUnit(target, image, unit);
                    GL11.glTexParameteri(target, GL12.GL_TEXTURE_WRAP_R, convertWrapMode(tex.getWrap(Texture.WrapAxis.R)));
                    curState.rWrap = tex.getWrap(Texture.WrapAxis.R);
                }
                // NOTE: There is no break statement on purpose here
            case TwoDimensional:
            case TwoDimensionalArray:
                if (curState.tWrap != tex.getWrap(Texture.WrapAxis.T)) {
                    bindTextureAndUnit(target, image, unit);
                    GL11.glTexParameteri(target, GL11.GL_TEXTURE_WRAP_T, convertWrapMode(tex.getWrap(Texture.WrapAxis.T)));
                    image.getLastTextureState().tWrap = tex.getWrap(Texture.WrapAxis.T);
                }
                if (curState.sWrap != tex.getWrap(Texture.WrapAxis.S)) {
                    bindTextureAndUnit(target, image, unit);
                    GL11.glTexParameteri(target, GL11.GL_TEXTURE_WRAP_S, convertWrapMode(tex.getWrap(Texture.WrapAxis.S)));
                    curState.sWrap = tex.getWrap(Texture.WrapAxis.S);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown texture type: " + tex.getType());
        }

        // If at this point we didn't bind the texture, bind it now
        bindTextureOnly(target, image, unit);
    }

    /**
     * Validates if a potentially NPOT texture is supported by the hardware.
     * <p>
     * Textures with power-of-2 dimensions are supported on all hardware, however
     * non-power-of-2 textures may or may not be supported depending on which
     * texturing features are used.
     *
     * @param tex The texture to validate.
     * @throws UnsupportedOperationException If the texture is not supported by the hardware
     */
    private void checkNonPowerOfTwo(Texture tex) {
        if (!tex.getImage().isNPOT()) {
            // Texture is power-of-2, safe to use.
            return;
        }

        throw new UnsupportedOperationException("non-power-of-2 textures are not supported yet");

//        if (caps.contains(Caps.NonPowerOfTwoTextures)) {
//            // Texture is NPOT but it is supported by video hardware.
//            return;
//        }
//
//        // Maybe we have some / partial support for NPOT?
//        if (!caps.contains(Caps.PartialNonPowerOfTwoTextures)) {
//            // Cannot use any type of NPOT texture (uncommon)
//            throw new RendererException("non-power-of-2 textures are not "
//                                        + "supported by the video hardware");
//        }
//
//        // Partial NPOT supported..
//        if (tex.getMinFilter().usesMipMapLevels()) {
//            throw new RendererException("non-power-of-2 textures with mip-maps "
//                                        + "are not supported by the video hardware");
//        }
//
//        switch (tex.getType()) {
//            case CubeMap:
//            case ThreeDimensional:
//                if (tex.getWrap(WrapAxis.R) != Texture.WrapMode.EdgeClamp) {
//                    throw new RendererException("repeating non-power-of-2 textures "
//                                                + "are not supported by the video hardware");
//                }
//                // fallthrough intentional!!!
//            case TwoDimensionalArray:
//            case TwoDimensional:
//                if (tex.getWrap(WrapAxis.S) != Texture.WrapMode.EdgeClamp
//                    || tex.getWrap(WrapAxis.T) != Texture.WrapMode.EdgeClamp) {
//                    throw new RendererException("repeating non-power-of-2 textures "
//                                                + "are not supported by the video hardware");
//                }
//                break;
//            default:
//                throw new UnsupportedOperationException("unrecongized texture type");
//        }
    }

    /**
     * Ensures that the texture is bound to the given unit
     * and that the unit is currently active (for modification).
     *
     * @param target The texture target, one of GL_TEXTURE_***
     * @param img The image texture to bind
     * @param unit At what unit to bind the texture.
     */
    private void bindTextureAndUnit(int target, Image img, int unit) {
        if (context.boundTextureUnit != unit) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
            context.boundTextureUnit = unit;
        }
        if (context.boundTextures[unit] != img) {
            GL11.glBindTexture(target, img.getId());
            context.boundTextures[unit] = img;
        }
    }

    /**
     * Ensures that the texture is bound to the given unit,
     * but does not care if the unit is active (for rendering).
     *
     * @param target The texture target, one of GL_TEXTURE_***
     * @param img The image texture to bind
     * @param unit At what unit to bind the texture.
     */
    private void bindTextureOnly(int target, Image img, int unit) {
        if (context.boundTextures[unit] == img) {
            return;
        }

        if (context.boundTextureUnit != unit) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
            context.boundTextureUnit = unit;
        }
        GL11.glBindTexture(target, img.getId());
        context.boundTextures[unit] = img;
    }

    public void applyRenderState(RenderState state) {
//        if (gl2 != null) {
//            if (state.isWireframe() && !context.wireframe) {
//                gl2.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);
//                context.wireframe = true;
//            } else if (!state.isWireframe() && context.wireframe) {
//                gl2.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
//                context.wireframe = false;
//            }
//        }

        if (state.isDepthTest() && !context.depthTestEnabled) {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            context.depthTestEnabled = true;
        } else if (!state.isDepthTest() && context.depthTestEnabled) {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            context.depthTestEnabled = false;
        }
        if (state.isDepthTest() && state.getDepthFunc() != context.depthFunc) {
            GL11.glDepthFunc(convertTestFunction(state.getDepthFunc()));
            context.depthFunc = state.getDepthFunc();
        }

        if (state.isDepthWrite() && !context.depthWriteEnabled) {
            GL11.glDepthMask(true);
            context.depthWriteEnabled = true;
        } else if (!state.isDepthWrite() && context.depthWriteEnabled) {
            GL11.glDepthMask(false);
            context.depthWriteEnabled = false;
        }

        if (state.isColorWrite() && !context.colorWriteEnabled) {
            GL11.glColorMask(true, true, true, true);
            context.colorWriteEnabled = true;
        } else if (!state.isColorWrite() && context.colorWriteEnabled) {
            GL11.glColorMask(false, false, false, false);
            context.colorWriteEnabled = false;
        }

        if (state.isPolyOffset()) {
            if (!context.polyOffsetEnabled) {
                GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
                GL11.glPolygonOffset(state.getPolyOffsetFactor(),
                                   state.getPolyOffsetUnits());
                context.polyOffsetEnabled = true;
                context.polyOffsetFactor = state.getPolyOffsetFactor();
                context.polyOffsetUnits = state.getPolyOffsetUnits();
            } else {
                if (state.getPolyOffsetFactor() != context.polyOffsetFactor
                    || state.getPolyOffsetUnits() != context.polyOffsetUnits) {
                    GL11.glPolygonOffset(state.getPolyOffsetFactor(),
                                       state.getPolyOffsetUnits());
                    context.polyOffsetFactor = state.getPolyOffsetFactor();
                    context.polyOffsetUnits = state.getPolyOffsetUnits();
                }
            }
        } else {
            if (context.polyOffsetEnabled) {
                GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
                context.polyOffsetEnabled = false;
                context.polyOffsetFactor = 0;
                context.polyOffsetUnits = 0;
            }
        }

        if (state.getFaceCullMode() != context.cullMode) {
            if (state.getFaceCullMode() == RenderState.FaceCullMode.Off) {
                GL11.glDisable(GL11.GL_CULL_FACE);
            } else {
                GL11.glEnable(GL11.GL_CULL_FACE);
            }

            switch (state.getFaceCullMode()) {
                case Off:
                    break;
                case Back:
                    GL11.glCullFace(GL11.GL_BACK);
                    break;
                case Front:
                    GL11.glCullFace(GL11.GL_FRONT);
                    break;
                case FrontAndBack:
                    GL11.glCullFace(GL11.GL_FRONT_AND_BACK);
                    break;
                default:
                    throw new UnsupportedOperationException("Unrecognized face cull mode: "
                                                            + state.getFaceCullMode());
            }

            context.cullMode = state.getFaceCullMode();
        }

        if (state.getBlendMode() != context.blendMode) {
            if (state.getBlendMode() == RenderState.BlendMode.Off) {
                GL11.glDisable(GL11.GL_BLEND);
            } else {
                if (context.blendMode == RenderState.BlendMode.Off) {
                    GL11.glEnable(GL11.GL_BLEND);
                }
                switch (state.getBlendMode()) {
                    case Off:
                        break;
                    case Additive:
                        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
                        break;
                    case AlphaAdditive:
                        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
                        break;
                    case Alpha:
                        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                        break;
                    case PremultAlpha:
                        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                        break;
                    case Modulate:
                        GL11.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_ZERO);
                        break;
                    case ModulateX2:
                        GL11.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_SRC_COLOR);
                        break;
                    case Color:
                    case Screen:
                        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_COLOR);
                        break;
                    case Exclusion:
                        GL11.glBlendFunc(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR);
                        break;
                    case Custom:
                        GL14.glBlendFuncSeparate(
                                convertBlendFunc(state.getCustomSfactorRGB()),
                                convertBlendFunc(state.getCustomDfactorRGB()),
                                convertBlendFunc(state.getCustomSfactorAlpha()),
                                convertBlendFunc(state.getCustomDfactorAlpha()));
                        break;
                    default:
                        throw new UnsupportedOperationException("Unrecognized blend mode: "
                                                                + state.getBlendMode());
                }

                if (state.getBlendEquation() != context.blendEquation || state.getBlendEquationAlpha() != context.blendEquationAlpha) {
                    int colorMode = convertBlendEquation(state.getBlendEquation());
                    int alphaMode;
                    if (state.getBlendEquationAlpha() == RenderState.BlendEquationAlpha.InheritColor) {
                        alphaMode = colorMode;
                    } else {
                        alphaMode = convertBlendEquationAlpha(state.getBlendEquationAlpha());
                    }
                    GL20.glBlendEquationSeparate(colorMode, alphaMode);
                    context.blendEquation = state.getBlendEquation();
                    context.blendEquationAlpha = state.getBlendEquationAlpha();
                }
            }

            context.blendMode = state.getBlendMode();
        }

        if (context.stencilTest != state.isStencilTest()
            || context.frontStencilStencilFailOperation != state.getFrontStencilStencilFailOperation()
            || context.frontStencilDepthFailOperation != state.getFrontStencilDepthFailOperation()
            || context.frontStencilDepthPassOperation != state.getFrontStencilDepthPassOperation()
            || context.backStencilStencilFailOperation != state.getBackStencilStencilFailOperation()
            || context.backStencilDepthFailOperation != state.getBackStencilDepthFailOperation()
            || context.backStencilDepthPassOperation != state.getBackStencilDepthPassOperation()
            || context.frontStencilFunction != state.getFrontStencilFunction()
            || context.backStencilFunction != state.getBackStencilFunction()) {

            context.frontStencilStencilFailOperation = state.getFrontStencilStencilFailOperation();   //terrible looking, I know
            context.frontStencilDepthFailOperation = state.getFrontStencilDepthFailOperation();
            context.frontStencilDepthPassOperation = state.getFrontStencilDepthPassOperation();
            context.backStencilStencilFailOperation = state.getBackStencilStencilFailOperation();
            context.backStencilDepthFailOperation = state.getBackStencilDepthFailOperation();
            context.backStencilDepthPassOperation = state.getBackStencilDepthPassOperation();
            context.frontStencilFunction = state.getFrontStencilFunction();
            context.backStencilFunction = state.getBackStencilFunction();

            if (state.isStencilTest()) {
                GL11.glEnable(GL11.GL_STENCIL_TEST);
                GL20.glStencilOpSeparate(GL11.GL_FRONT,
                                       convertStencilOperation(state.getFrontStencilStencilFailOperation()),
                                       convertStencilOperation(state.getFrontStencilDepthFailOperation()),
                                       convertStencilOperation(state.getFrontStencilDepthPassOperation()));
                GL20.glStencilOpSeparate(GL11.GL_BACK,
                                       convertStencilOperation(state.getBackStencilStencilFailOperation()),
                                       convertStencilOperation(state.getBackStencilDepthFailOperation()),
                                       convertStencilOperation(state.getBackStencilDepthPassOperation()));
                GL20.glStencilFuncSeparate(GL11.GL_FRONT,
                                         convertTestFunction(state.getFrontStencilFunction()),
                                         0, Integer.MAX_VALUE);
                GL20.glStencilFuncSeparate(GL11.GL_BACK,
                                         convertTestFunction(state.getBackStencilFunction()),
                                         0, Integer.MAX_VALUE);
            } else {
                GL11.glDisable(GL11.GL_STENCIL_TEST);
            }
        }
    }
}
