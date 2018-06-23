package mini.editor.injfx;

import javafx.scene.Node;
import javafx.scene.image.ImageView;
import mini.editor.injfx.transfer.FrameTransfer;
import mini.editor.injfx.transfer.ImageFrameTransfer;
import mini.textures.FrameBuffer;

public class ImageViewFrameTransferSceneProcessor
        extends AbstractFrameTransferSceneProcessor<ImageView> {

    @Override
    protected void bindDestination(MiniToJavaFXApplication application, ImageView destination,
                                   Node inputNode) {
        super.bindDestination(application, destination, inputNode);
        destination.setScaleY(-1.0);
    }

    @Override
    protected boolean isRatioPreserved() {
        return getDestination().isPreserveRatio();
    }

    @Override
    protected int getDestinationHeight() {
        return (int) getDestination().getFitHeight();
    }

    @Override
    protected int getDestinationWidth() {
        return (int) getDestination().getFitWidth();
    }

    @Override
    protected FrameTransfer createFrameTransfer(FrameBuffer frameBuffer, int width, int height) {
        return new ImageFrameTransfer(getDestination(), getTransferMode(),
                                      isMain() ? null : frameBuffer, width, height);
    }
}
