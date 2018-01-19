package mini.post.niftygui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.spi.time.impl.AccurateTimeProvider;
import de.lessvoid.nifty.tools.resourceloader.ResourceLocation;
import mini.asset.AssetInfo;
import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.asset.AssetNotFoundException;
import mini.audio.niftygui.SoundDeviceMini;
import mini.input.InputManager;
import mini.input.niftygui.InputSystemMini;
import mini.post.SceneProcessor;
import mini.renderer.RenderManager;
import mini.renderer.Renderer;
import mini.renderer.ViewPort;
import mini.renderer.niftygui.RenderDeviceMini;
import mini.renderer.queue.RenderQueue;
import mini.textures.FrameBuffer;

import java.io.InputStream;
import java.net.URL;

public class NiftyMiniDisplay implements SceneProcessor {
    private RenderDeviceMini renderDevice;
    private SoundDeviceMini soundDevice;
    private InputSystemMini inputSystem;
    private AssetManager assetManager;
    private ViewPort viewPort;
    private int height;
    private int width;
    private RenderManager renderManager;
    private InputManager inputManager;
    private ViewPort vp;
    private Renderer renderer;
    private Nifty nifty;

    public NiftyMiniDisplay(AssetManager assetManager, ViewPort viewPort,
                            InputManager inputManager) {
        this.assetManager = assetManager;
        this.inputManager = inputManager;
        this.viewPort = viewPort;
        initializeNifty(inputManager);

        nifty = new Nifty(renderDevice, soundDevice, inputSystem, new AccurateTimeProvider());
        inputSystem.setNifty(nifty);

        nifty.getResourceLoader().removeAllResourceLocations();
        nifty.getResourceLoader().addResourceLocation(new ResourceLocationMini());
    }

    public Nifty getNifty() {
        return nifty;
    }

    private void initializeNifty(final InputManager inputManager) {
        renderDevice = new RenderDeviceMini(this);
        soundDevice = new SoundDeviceMini(this);
        inputSystem = new InputSystemMini(inputManager);

        height = viewPort.getCamera().getHeight();
        width = viewPort.getCamera().getWidth();
    }

    @Override
    public void initialize(RenderManager rm, ViewPort vp) {
        this.renderManager = rm;

        renderDevice.setRenderManager(rm);

        if (inputManager != null) {
            inputManager.addRawInputListener(inputSystem);
        }
        this.vp = vp;
        this.renderer = rm.getRenderer();

        inputSystem.reset();
        inputSystem.setHeight(vp.getCamera().getHeight());
    }

    @Override
    public boolean isInitialized() {
        return vp != null;
    }

    @Override
    public void reshape(ViewPort vp, int w, int h) {
        this.width = w;
        this.height = h;
        inputSystem.setHeight(h);
        nifty.resolutionChanged();
    }

    @Override
    public void postQueue(RenderQueue rq) {
        renderManager.setCamera(vp.getCamera(), true);
        nifty.render(false);
        renderManager.setCamera(vp.getCamera(), false);
    }

    @Override
    public void preFrame(float tpf) {
    }

    @Override
    public void cleanup() {
        inputSystem.reset();
        if (inputManager != null) {
            inputManager.removeRawInputListener(inputSystem);
        }
    }

    @Override
    public void postFrame(FrameBuffer out) {
    }

    private class ResourceLocationMini implements ResourceLocation {

        @Override
        public InputStream getResourceAsStream(String path) {
            AssetKey<Object> key = new AssetKey<>(path);
            AssetInfo info = assetManager.locateAsset(key);
            if (info != null) {
                return info.openStream();
            } else {
                throw new AssetNotFoundException(path);
            }
        }

        @Override
        public URL getResource(String s) {
            throw new UnsupportedOperationException();
        }
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
