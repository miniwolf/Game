package mini.renderer.niftygui;

import de.lessvoid.nifty.render.BlendMode;
import de.lessvoid.nifty.spi.render.MouseCursor;
import de.lessvoid.nifty.spi.render.RenderDevice;
import de.lessvoid.nifty.spi.render.RenderFont;
import de.lessvoid.nifty.spi.render.RenderImage;
import de.lessvoid.nifty.tools.Color;
import de.lessvoid.nifty.tools.resourceloader.NiftyResourceLoader;
import mini.font.BitmapText;
import mini.material.Material;
import mini.material.RenderState;
import mini.math.ColorRGBA;
import mini.math.Matrix4f;
import mini.post.niftygui.NiftyMiniDisplay;
import mini.renderer.RenderManager;
import mini.renderer.Renderer;
import mini.scene.Geometry;
import mini.scene.VertexBuffer;
import mini.scene.shape.Quad;
import mini.textures.Texture2D;
import mini.utils.BufferUtils;
import mini.utils.TempVars;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

public class RenderDeviceMini implements RenderDevice {
    private final NiftyMiniDisplay display;
    private final Quad quad = new Quad(1, -1, true);
    private final Geometry quadGeometry = new Geometry("Nifty-quad", quad);

    private Renderer renderer;
    private RenderManager renderManager;
    private RenderState renderState = new RenderState();
    private Material textureColorMaterial;
    private Material vertexColorMaterial;
    private Material colorMaterial;

    private boolean clipWasSet;
    private Map<CachedTextKey, BitmapText> textCachedLastFrame = new HashMap<>();
    private Map<CachedTextKey, BitmapText> textCachedCurrentFrame = new HashMap<>();

    private VertexBuffer quadDefaultTexcoord = quad.getBuffer(VertexBuffer.Type.TexCoord);
    private VertexBuffer quadModTexcoord = quadDefaultTexcoord.clone();
    private VertexBuffer quadColor;

    public RenderDeviceMini(NiftyMiniDisplay display) {
        this.display = display;

        quadColor = new VertexBuffer(VertexBuffer.Type.Color);
        quadColor.setNormalized(true);
        ByteBuffer buffer = BufferUtils.createByteBuffer(4 * 4);
        quadColor.setupData(VertexBuffer.Usage.Stream, 4, VertexBuffer.Format.UnsignedByte, buffer);
        quad.setBuffer(quadColor);

        quadModTexcoord.setUsage(VertexBuffer.Usage.Stream);

        textureColorMaterial = new Material(display.getAssetManager(),
                                            "MatDefs/Misc/Unshaded.minid");
        vertexColorMaterial = new Material(display.getAssetManager(),
                                           "MatDefs/Misc/Unshaded.minid");
        vertexColorMaterial.setBoolean("VertexColor", true);

        colorMaterial = new Material(display.getAssetManager(),
                                     "MatDefs/Misc/Unshaded.minid");

        renderState.setDepthTest(false);
        renderState.setDepthWrite(false);
    }

    private RenderState.BlendMode convertBlendMode(BlendMode blendMode) {
        if (blendMode == null) {
            return RenderState.BlendMode.Off;
        } else if (blendMode == BlendMode.BLEND) {
            return RenderState.BlendMode.Alpha;
        } else if (blendMode == BlendMode.MULIPLY) {
            return RenderState.BlendMode.Modulate;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private ColorRGBA convertColorToRGBA(Color color) {
        return new ColorRGBA(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    private int convertColorToInt(Color color) {
        int res = 0;
        res |= ((int) (255.0 * color.getAlpha())) << 24;
        res |= ((int) (255.0 * color.getBlue())) << 16;
        res |= ((int) (255.0 * color.getGreen())) << 8;
        res |= ((int) (255.0 * color.getRed()));
        return res;
    }

    public void setRenderManager(RenderManager renderManager) {
        this.renderManager = renderManager;
        this.renderer = renderManager.getRenderer();
    }

    @Override
    public void setResourceLoader(NiftyResourceLoader niftyResourceLoader) {
    }

    @Override
    public RenderImage createImage(String fileName, boolean linear) {
        return new RenderImageMini(fileName, linear, display);
    }

    @Override
    public RenderFont createFont(String fileName) {
        return new RenderFontMini(fileName, display);
    }

    @Override
    public int getWidth() {
        return display.getWidth();
    }

    @Override
    public int getHeight() {
        return display.getHeight();
    }

    @Override
    public void beginFrame() {
    }

    @Override
    public void endFrame() {
        Map<CachedTextKey, BitmapText> temp = this.textCachedLastFrame;
        textCachedLastFrame = textCachedCurrentFrame;
        textCachedCurrentFrame = temp;
        textCachedCurrentFrame.clear();
        renderManager.setForcedRenderState(null);
    }

    @Override
    public void clear() {
    }

    @Override
    public void setBlendMode(BlendMode blendMode) {
        renderState.setBlendMode(convertBlendMode(blendMode));
    }

    @Override
    public void renderQuad(int x, int y, int width, int height, Color color) {
        if (color.getAlpha() <= 0) {
            return;
        }

        colorMaterial.setColor("Color", convertColorToRGBA(color));

        TempVars vars = TempVars.get();
        Matrix4f tempMat4 = vars.tempMat4;
        tempMat4.loadIdentity();
        tempMat4.setTranslation(x, getHeight() - y, 0);
        tempMat4.setScale(width, height, 0);

        renderManager.setWorldMatrix(tempMat4);
        vars.release();
        renderManager.setForcedRenderState(renderState);
        colorMaterial.render(quadGeometry, renderManager);
    }

    @Override
    public void renderQuad(int x, int y, int width, int height, Color topLeft, Color topRight,
                           Color bottomRight, Color bottomLeft) {
        ByteBuffer data = (ByteBuffer) quadColor.getData();
        data.rewind();

        data.putInt(convertColorToInt(topRight));
        data.putInt(convertColorToInt(topLeft));

        data.putInt(convertColorToInt(bottomLeft));
        data.putInt(convertColorToInt(bottomRight));

        data.flip();
        quadColor.updateData(data);

        TempVars vars = TempVars.get();
        Matrix4f tempMat4 = vars.tempMat4;
        tempMat4.loadIdentity();
        tempMat4.setTranslation(x, getHeight() - y, 0);
        tempMat4.setScale(width, height, 0);

        renderManager.setWorldMatrix(tempMat4);
        vars.release();
        renderManager.setForcedRenderState(renderState);
        vertexColorMaterial.render(quadGeometry, renderManager);
    }

    @Override
    public void renderImage(RenderImage renderImage, int x, int y, int width, int height,
                            Color color, float scale) {
        RenderImageMini miniImage = (RenderImageMini) renderImage;

        textureColorMaterial.setColor("Color", convertColorToRGBA(color));
        textureColorMaterial.setTexture("ColorMap", miniImage.getTexture());

        quad.clearBuffer(VertexBuffer.Type.TexCoord);
        quad.setBuffer(quadDefaultTexcoord);

        float x0 = x + 0.5f * width * (1f - scale);
        float y0 = y + 0.5f * height * (1f - scale);

        TempVars vars = TempVars.get();
        Matrix4f tempMat4 = vars.tempMat4;
        tempMat4.loadIdentity();
        tempMat4.setTranslation(x0, getHeight() - y0, 0);
        tempMat4.setScale(width * scale, height * scale, 0);

        renderManager.setWorldMatrix(tempMat4);
        vars.release();
        renderManager.setForcedRenderState(renderState);
        textureColorMaterial.render(quadGeometry, renderManager);
    }

    @Override
    public void renderImage(RenderImage renderImage, int x, int y, int width, int height, int srcX,
                            int srcY, int srcWidth, int srcHeight, Color color, float scale,
                            int centerX, int centerY) {
        RenderImageMini miniImage = (RenderImageMini) renderImage;
        Texture2D texture = miniImage.getTexture();

        textureColorMaterial.setColor("Color", convertColorToRGBA(color));
        textureColorMaterial.setTexture("ColorMap", texture);

        FloatBuffer texCoords = (FloatBuffer) quadModTexcoord.getData();

        float imageWidth = miniImage.getWidth();
        float imageHeight = miniImage.getHeight();

        float startX = srcX / imageWidth;
        float startY = srcY / imageHeight;
        float endX = startX + (srcWidth / imageWidth);
        float endY = startY + (srcHeight / imageHeight);

        startY = 1f - startY;
        endY = 1f - endY;

        texCoords.rewind();
        texCoords.put(startX).put(startY).put(endX).put(startY);
        texCoords.put(endX).put(endY).put(startX).put(endY);
        texCoords.flip();
        quadModTexcoord.updateData(texCoords);

        quad.clearBuffer(VertexBuffer.Type.TexCoord);
        quad.setBuffer(quadModTexcoord);

        float x0 = centerX + (x - centerX) * scale;
        float y0 = centerY + (y - centerY) * scale;

        TempVars vars = TempVars.get();
        Matrix4f tempMat4 = vars.tempMat4;
        tempMat4.loadIdentity();
        tempMat4.setTranslation(x0, getHeight() - y0, 0);
        tempMat4.setScale(width * scale, height * scale, 0);

        renderManager.setWorldMatrix(tempMat4);
        vars.release();
        renderManager.setForcedRenderState(renderState);
        textureColorMaterial.render(quadGeometry, renderManager);
    }

    @Override
    public void renderFont(RenderFont renderFont, String str, int x, int y, Color color,
                           float sizeX, float sizeY) {
        if (str.length() == 0) {
            return;
        }

        RenderFontMini miniFont = (RenderFontMini) renderFont;

        CachedTextKey key = new CachedTextKey(miniFont.getFont(), str);
        BitmapText bitmapText = textCachedLastFrame.get(key);
        if (bitmapText == null) {
            bitmapText = miniFont.createText();
            bitmapText.setText(str);
            bitmapText.updateLogicalState(0);
        }
        textCachedCurrentFrame.put(key, bitmapText);

        TempVars vars = TempVars.get();
        Matrix4f tempMat4 = vars.tempMat4;
        tempMat4.loadIdentity();
        tempMat4.setTranslation(x, getHeight() - y, 0);
        tempMat4.setScale(sizeX, sizeY, 0);

        renderManager.setWorldMatrix(tempMat4);
        vars.release();
        renderManager.setForcedRenderState(renderState);
        ColorRGBA colorRGBA = convertColorToRGBA(color);
        bitmapText.setColor(colorRGBA);
        bitmapText.updateLogicalState(0);
        bitmapText.render(renderManager, colorRGBA);
    }

    @Override
    public void enableClip(final int x0, final int y0, final int x1, int y1) {
        clipWasSet = true;
        renderer.setClipRect(x0, getHeight() - y1, x1 - x0, y1 - y0);
    }

    @Override
    public void disableClip() {
        if (!clipWasSet) {
            return;
        }
        renderer.clearClipRect();
        clipWasSet = false;
    }

    // TODO: Support mouse cursor
    @Override
    public MouseCursor createMouseCursor(String s, int i, int i1) {
        return new MouseCursor() {
            @Override
            public void enable() {
            }

            @Override
            public void disable() {
            }

            @Override
            public void dispose() {
            }
        };
    }

    @Override
    public void enableMouseCursor(MouseCursor mouseCursor) {
    }

    @Override
    public void disableMouseCursor() {
    }
}
