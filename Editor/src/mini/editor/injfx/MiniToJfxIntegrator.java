package mini.editor.injfx;

import javafx.scene.image.ImageView;
import mini.renderer.ViewPort;
import mini.system.ApplicationSettings;

public class MiniToJfxIntegrator {

    public static ApplicationSettings prepareSettings(ApplicationSettings settings,
                                                      int frameRate) {
        settings.setFullscreen(false);
        settings.setFrameRate(Math.max(1, Math.min(100, frameRate)));
        settings.setCustomRenderer(MiniOffscreenSurfaceContext.class);
        return settings;
    }

    public static ImageViewFrameTransferSceneProcessor bind(
            MiniToJavaFXApplication application,
            ImageView imageView,
            ViewPort viewPort) {
        var processor = new ImageViewFrameTransferSceneProcessor();
        processor.bind(imageView, application, viewPort);

        return processor;
    }
}
