package mini.system;

import mini.utils.Screenshots;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.ByteBuffer;

/**
 */
public class ApplicationDesktopSystem extends ApplicationSystemDelegate {
    @Override
    public void showErrorDialog(String message) {
        if (!GraphicsEnvironment.isHeadless()) {
            final String msg = message;
            EventQueue.invokeLater(() -> ErrorDialog.showDialog(msg));
        } else {
            System.err.println("[ERROR] " + message);
        }
    }

    private static BufferedImage verticalFlip(BufferedImage original) {
        AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
        tx.translate(0, -original.getHeight());
        AffineTransformOp transformOp = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        BufferedImage awtImage = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_BGR);
        Graphics2D g2d = awtImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g2d.drawImage(original, transformOp, 0, 0);
        g2d.dispose();
        return awtImage;
    }

    @Override
    public void writeImageFile(OutputStream outStream, String format, ByteBuffer imageData, int width, int height) throws IOException {
        BufferedImage awtImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
        Screenshots.convertScreenShot(imageData.asIntBuffer(), awtImage);

        ImageWriter writer = ImageIO.getImageWritersByFormatName(format).next();
        ImageWriteParam writeParam = writer.getDefaultWriteParam();

        if (format.equals("jpg")) {
            JPEGImageWriteParam jpegParam = (JPEGImageWriteParam) writeParam;
            jpegParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpegParam.setCompressionQuality(0.95f);
        }

        awtImage = verticalFlip(awtImage);

        ImageOutputStream imgOut = new MemoryCacheImageOutputStream(outStream);
        writer.setOutput(imgOut);
        IIOImage outputImage = new IIOImage(awtImage, null, null);
        try {
            writer.write(null, outputImage, writeParam);
        } finally {
            imgOut.close();
            writer.dispose();
        }
    }

    @Override
    public URL getPlatformAssetConfigURL() {
        return Thread.currentThread().getContextClassLoader().getResource("mini/asset/General.cfg");
    }

    @SuppressWarnings("unchecked")
    private ApplicationContext newContextLwjgl() {
        try {
            Class<? extends ApplicationContext> ctxClazz =
                    (Class<? extends ApplicationContext>) Class.forName("mini.system.lwjgl.LwjglDisplay");
            return ctxClazz.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            System.err.println("Failed to create context" + ex.getMessage());
        } catch (ClassNotFoundException ex) {
            System.err.println("CRITICAL ERROR: Context class is missing!\n"
                               + "Make sure context class is on the classpath." + ex);
        }

        return null;
    }

    @Override
    public ApplicationContext newContext() {
        initialize();
        return newContextLwjgl();
    }

    private void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;

        if (!lowPermissions) {
            if (NativeLibraryLoader.isUsingNativeBullet()) {
                NativeLibraryLoader.loadNativeLibrary("bulletmini", true);
            }
        }
    }
}