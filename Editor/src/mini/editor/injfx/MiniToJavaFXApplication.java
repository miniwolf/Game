package mini.editor.injfx;

import mini.app.SimpleApplication;
import mini.editor.executor.impl.EditorThreadExecutor;
import mini.post.FilterPostProcessor;

public class MiniToJavaFXApplication extends SimpleApplication {
    private static final EditorThreadExecutor EXECUTOR = EditorThreadExecutor.getInstance();

    private FilterPostProcessor postProcessor;

    @Override
    public void simpleInitApp() {
        postProcessor = new FilterPostProcessor(assetManager);
        postProcessor.initialize(renderManager, viewPort);
        viewPort.addProcessor(postProcessor);
    }

    @Override
    public void update() {
        EXECUTOR.execute();
        super.update();
    }
}
