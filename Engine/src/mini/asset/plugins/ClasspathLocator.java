package mini.asset.plugins;

import mini.asset.AssetInfo;
import mini.asset.AssetKey;
import mini.asset.AssetLocator;
import mini.asset.AssetManager;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * The <code>ClasspathLocator</code> looks up an asset in the classpath.
 * <p>
 * This locator is used by default in all projects (unless
 * {@link AssetManager#registerLocator(Class, String)} is used to overwrite the locator).
 * <p>
 * Unlike Java's default resource loading mechanism, the <code>ClasspathLocator</code> enforces
 * case-sensitivity on platforms which do not have it such as Windows. Therefore it is critical to
 * provide a path matching the case of the filesystem. This also ensures the file can be loaded if
 * it was later included in a <code>.jar</code> file instead of a folder.
 */
public class ClasspathLocator implements AssetLocator {
    private String root = "";

    @Override
    public void setRootPath(String rootPath) {
        this.root = rootPath;
        if ("/".equals(root)) {
            root = "";
        } else if (root.length() > 1) {
            if (root.startsWith("/")) {
                root = root.substring(1);
            }
            if (!root.endsWith("/")) {
                root += "/";
            }
        }
    }

    @Override
    public AssetInfo locate(AssetManager manager, AssetKey key) {
        String name = key.getName();
        if (name.startsWith("/")) {
            name = name.substring(1);
        }
        name = root + name;

        URL url = ClasspathLocator.class.getResource("/" + name);
        if (url == null) {
            return null;
        }

        if (url.getProtocol().equals("file")) {
            try {
                String path = new File(url.toURI()).getCanonicalPath();

                // convert to / for windows
                if (File.separatorChar == '\\') {
                    path = path.replace('\\', '/');
                }

                // compare path
                if (!path.endsWith(name)) {
                    throw new RuntimeException("Asset name doesn't match requirements.\n" +
                                               "\"" + path + "\" doesn't match \"" + name + "\"");
                }
            } catch (URISyntaxException ex) {
                throw new RuntimeException("Error converting URL to URI", ex);
            } catch (IOException ex) {
                throw new RuntimeException("Failed to get canonical path for " + url, ex);
            }
        }

        try {
            return UrlAssetInfo.create(manager, key, url);
        } catch (IOException ex) {
            // This is different handling than URL locator
            // since classpath locating would return null at the getResource()
            // call, otherwise there's a more critical error...
            throw new RuntimeException("Failed to read URL " + url, ex);
        }
    }

    @Override
    public void setRootPath(String rootPath) {
        root = rootPath;
        if (root.equals("/")) {
            root = "";
        } else if (root.length() > 1) {
            if (root.startsWith("/")) {
                root = root.substring(1);
            }
            if (!root.endsWith("/")) {
                root += "/";
            }
        }
    }
}
