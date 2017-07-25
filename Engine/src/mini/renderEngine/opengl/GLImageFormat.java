package mini.renderEngine.opengl;

/**
 * Created by miniwolf on 25-04-2017.
 */
public class GLImageFormat {
    int internalFormat;
    int format;
    int dataType;
    boolean compressed;

    public GLImageFormat(int internalFormat, int format, int dataType, boolean compressed) {
        this.internalFormat = internalFormat;
        this.format = format;
        this.dataType = dataType;
        this.compressed = compressed;
    }
}
