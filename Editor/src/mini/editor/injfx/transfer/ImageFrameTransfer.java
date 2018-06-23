package mini.editor.injfx.transfer;

import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import mini.editor.injfx.processor.FrameTransferSceneProcessor;
import mini.editor.util.JavaFXPlatform;
import mini.textures.FrameBuffer;

public class ImageFrameTransfer extends AbstractFrameTransfer<ImageView> {
    private WritableImage writableImage;

    public ImageFrameTransfer(ImageView imageView,
                              FrameTransferSceneProcessor.TransferMode transferMode,
                              FrameBuffer frameBuffer,
                              int width,
                              int height) {
        super(imageView, transferMode, frameBuffer, width, height);
        JavaFXPlatform.runInFXThread(() -> imageView.setImage(writableImage));
    }

    @Override
    protected PixelWriter getPixelWriter(
            ImageView destination,
            FrameBuffer frameBuffer,
            int width,
            int height) {
        writableImage = new WritableImage(width, height);
        return writableImage.getPixelWriter();
    }
}
