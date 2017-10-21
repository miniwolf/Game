package mini.asset.plugins;

import mini.asset.AssetInfo;
import mini.asset.AssetKey;
import mini.asset.AssetLocator;
import mini.asset.AssetManager;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class ClasspathLocator implements AssetLocator {
    private String root = "";

    @Override
    public AssetInfo locate(AssetManager manager, AssetKey key) {
        URL url;
        String name = key.getName();
        if (name.startsWith("/")) {
            name = name.substring(1);
        }

        name = root + name;
//        if (!name.startsWith(root)){
//            name = root + name;
//        }

        url = ClasspathLocator.class.getResource("/" + name);

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
