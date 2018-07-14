package mini.editor.ui.css;

import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import mini.editor.annotation.FromAnyThread;
import mini.editor.util.ObjectsUtil;

import java.net.URL;

public class CssRegistry {
    private static final CssRegistry INSTANCE = new CssRegistry();
    private final Array<String> availableCssFiles;

    public CssRegistry() {
        availableCssFiles = ArrayFactory.newArray(String.class);
    }

    public static CssRegistry getInstance() {
        return INSTANCE;
    }

    @FromAnyThread
    public void register(String cssFileCustomClasses, ClassLoader classLoader) {
        register(ObjectsUtil.notNull(classLoader.getResource(cssFileCustomClasses)));
    }

    @FromAnyThread
    private void register(final URL cssFile) {
        availableCssFiles.add(cssFile.toExternalForm());
    }

    /**
     * @return the list of available css files.
     */
    public Array<String> getAvailableCssFiles() {
        return availableCssFiles;
    }
}
