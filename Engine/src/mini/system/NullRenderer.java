package mini.system;

import mini.light.LightList;
import mini.material.RenderState;
import mini.math.ColorRGBA;
import mini.math.Matrix4f;
import mini.renderer.Caps;
import mini.renderer.Limits;
import mini.renderer.Renderer;
import mini.scene.Mesh;
import mini.scene.VertexBuffer;
import mini.shaders.Shader;
import mini.shaders.ShaderSource;
import mini.textures.FrameBuffer;
import mini.textures.Image;
import mini.textures.Texture;

import java.nio.ByteBuffer;
import java.util.EnumMap;
import java.util.EnumSet;

public class NullRenderer implements Renderer {
    private final EnumSet<Caps> caps = EnumSet.allOf(Caps.class);
    private final EnumMap<Limits, Integer> limits = new EnumMap<>(Limits.class);

    public void initialize() {
        for (Limits limit : Limits.values()) {
            limits.put(limit, Integer.MAX_VALUE);
        }
    }

    @Override
    public EnumMap<Limits, Integer> getLimits() {
        return limits;
    }

    public EnumSet<Caps> getCaps() {
        return caps;
    }

    public void invalidateState(){
    }

    public void clearBuffers(boolean color, boolean depth, boolean stencil) {
    }

    public void setBackgroundColor(ColorRGBA color) {
    }

    public void applyRenderState(RenderState state) {
    }

    public void setDepthRange(float start, float end) {
    }

    public void postFrame() {
    }

    public void setWorldMatrix(Matrix4f worldMatrix) {
    }

    public void setViewProjectionMatrices(Matrix4f viewMatrix, Matrix4f projMatrix) {
    }

    public void setViewPort(int x, int y, int width, int height) {
    }

    public void setClipRect(int x, int y, int width, int height) {
    }

    public void clearClipRect() {
    }

    public void setLighting(LightList lights) {
    }

    public void setShader(Shader shader) {
    }

    public void deleteShader(Shader shader) {
    }

    public void deleteShaderSource(ShaderSource source) {
    }

    public void copyFrameBuffer(FrameBuffer src, FrameBuffer dst) {
    }

    public void copyFrameBuffer(FrameBuffer src, FrameBuffer dst, boolean copyDepth) {
    }

    public void setMainFrameBufferOverride(FrameBuffer fb) {
    }

    public void setFrameBuffer(FrameBuffer fb) {
    }

    public void readFrameBuffer(FrameBuffer fb, ByteBuffer byteBuf) {
    }

    public void deleteFrameBuffer(FrameBuffer fb) {
    }

    public void setTexture(int unit, Texture tex) {
    }

    public void modifyTexture(Texture tex, Image pixels, int x, int y) {
    }

    public void updateBufferData(VertexBuffer vb) {
    }

    public void deleteBuffer(VertexBuffer vb) {
    }

    public void renderMesh(Mesh mesh, int lod, int count, VertexBuffer[] instanceData) {
    }

    public void resetGLObjects() {
    }

    public void cleanup() {
    }

    public void deleteImage(Image image) {
    }

    public void setAlphaToCoverage(boolean value) {
    }

    public void setMainFrameBufferSrgb(boolean srgb) {
    }

    public void setLinearizeSrgbImages(boolean linearize) {
    }

    public void readFrameBufferWithFormat(FrameBuffer fb, ByteBuffer byteBuf, Image.Format format) {
    }

    @Override
    public void setDefaultAnisotropicFilter(int level) {
    }
}

