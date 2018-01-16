package mini.post.niftygui;

import de.lessvoid.nifty.Nifty;
import mini.asset.AssetManager;
import mini.post.SceneProcessor;
import mini.renderer.RenderManager;
import mini.renderer.ViewPort;
import mini.renderer.niftygui.RenderDeviceMini;
import mini.renderer.queue.RenderQueue;
import mini.textures.FrameBuffer;

public class NiftyMiniDisplay implements SceneProcessor {
    private RenderDeviceMini renderDevice;
    private AssetManager assetManager;
    private ViewPort viewPort;
    private int height;
    private int width;

    public NiftyMiniDisplay(final AssetManager assetManager, final ViewPort viewPort) {
        this.assetManager = assetManager;
        this.viewPort = viewPort;
        initializeNifty();
    }

    private void initializeNifty() {
        renderDevice = new RenderDeviceMini(this);
        new Nifty(renderDevice, );

        height = viewPort.getCamera().getHeight();
        width = viewPort.getCamera().getWidth();
    }

    @Override
    public void initialize(RenderManager rm, ViewPort vp) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isInitialized() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reshape(ViewPort vp, int w, int h) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void preFrame(float tpf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void postQueue(RenderQueue rq) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void postFrame(FrameBuffer out) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cleanup() {
        throw new UnsupportedOperationException();
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
}
