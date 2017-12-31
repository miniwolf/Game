package mini.system;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class MockApplicationSystemDelegate extends ApplicationSystemDelegate {
    @Override
    public void showErrorDialog(String message) {
    }

    @Override
    public void writeImageFile(OutputStream outStream, String format, ByteBuffer imageData, int width, int height) throws IOException {
    }

    @Override
    public ApplicationContext newContext() {
        return null;
    }
}
