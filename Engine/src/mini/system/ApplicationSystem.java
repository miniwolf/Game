package mini.system;

import mini.asset.AssetManager;
import mini.textures.Image;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ApplicationSystem {
    private static ApplicationSystemDelegate systemDelegate;

    public static ApplicationContext newContext(ApplicationContext.Type contextType) {
        checkDelegate();
        return systemDelegate.newContext();
    }

    public static Platform getPlatform() {
        checkDelegate();
        return systemDelegate.getPlatform();
    }

    /**
     * Displays an error message to the user in whichever way the context
     * feels is appropriate. If this is a headless or an offscreen surface
     * context, this method should do nothing.
     *
     * @param message The error message to display. May contain new line
     * characters.
     */
    public static void showErrorDialog(String message){
        checkDelegate();
        systemDelegate.showErrorDialog(message);
    }

    private static ApplicationSystemDelegate tryLoadDelegate(String className) throws InstantiationException, IllegalAccessException {
        try {
            return (ApplicationSystemDelegate) Class.forName(className).newInstance();
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    public static void setSystemDelegate(ApplicationSystemDelegate systemDelegate) {
        ApplicationSystem.systemDelegate = systemDelegate;
    }

    public static boolean isLowPermissions() {
        checkDelegate();
        return systemDelegate.isLowPermissions();
    }

    @SuppressWarnings("unchecked")
    private static void checkDelegate() {
        if (systemDelegate == null) {
            try {
                systemDelegate = tryLoadDelegate("mini.system.ApplicationDesktopSystem");
                if (systemDelegate == null) {
                    // None of the system delegates were found ..
                    System.err.println("Severe: Failed to find an ApplicationSystem delegate!\n"
                                       + "Ensure desktop is in the classpath.");
                }
            } catch (InstantiationException | IllegalAccessException ex ) {
                System.err.println("Severe: Failed to create ApplicationSystem delegate:\n" + ex);
            }
        }
    }

    /**
     * Compresses a raw image into a stream.
     *
     * The encoding is performed via system libraries. On desktop, the encoding
     * is performed via ImageIO, whereas on Android, is is done via the
     * Bitmap class.
     *
     * @param outStream The stream where to write the image data.
     * @param format The format to use, either "png" or "jpg".
     * @param imageData The image data in {@link Image.Format#RGBA8} format.
     * @param width The width of the image.
     * @param height The height of the image.
     * @throws IOException If outStream throws an exception while writing.
     */
    public static void writeImageFile(OutputStream outStream, String format, ByteBuffer imageData, int width, int height) throws IOException {
        checkDelegate();
        systemDelegate.writeImageFile(outStream, format, imageData, width, height);
    }

    public static AssetManager newAssetManager() {
        return systemDelegate.newAssetManager();
    }
}
