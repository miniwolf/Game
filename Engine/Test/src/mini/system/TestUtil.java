package mini.system;

import mini.asset.AssetManager;
import mini.renderer.RenderManager;

public class TestUtil {
    static {
        ApplicationSystem.setSystemDelegate(new MockApplicationSystemDelegate());
    }
    
    public static RenderManager createRenderManager() {
        return new RenderManager(new NullRenderer());
    }

    public static AssetManager createAssetManager() {
        return new AssetManager();
    }
}
