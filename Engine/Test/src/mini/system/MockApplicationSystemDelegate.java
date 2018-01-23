package mini.system;

import java.io.OutputStream;
import java.net.URL;
import java.nio.ByteBuffer;

public class MockApplicationSystemDelegate extends ApplicationSystemDelegate {
    @Override
    public void showErrorDialog(String message) {
    }

    @Override
    public void writeImageFile(OutputStream outStream, String format, ByteBuffer imageData,
                               int width, int height) {
    }

    @Override
    public URL getPlatformAssetConfigURL() {
        return Thread.currentThread().getContextClassLoader().getResource("mini/asset/General.cfg");
    }

    @Override
    public ApplicationContext newContext() {
        return null;
    }
}
