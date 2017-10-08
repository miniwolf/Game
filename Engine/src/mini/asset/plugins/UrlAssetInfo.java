package mini.asset.plugins;

import mini.asset.AssetInfo;
import mini.asset.AssetKey;
import mini.asset.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class UrlAssetInfo extends AssetInfo {
    private URL url;
    private InputStream in;

    private UrlAssetInfo(AssetManager assetManager, AssetKey key, URL url, InputStream in) {
        super(assetManager, key);
        this.url = url;
        this.in = in;
    }

    public static UrlAssetInfo create(AssetManager assetManager, AssetKey key, URL url) throws
                                                                                        IOException {
        // Check if URL can be reached. This will throw
        // IOException which calling code will handle.
        URLConnection conn = url.openConnection();
        conn.setUseCaches(false);
        InputStream in = conn.getInputStream();

        // For some reason url cannot be reached?
        if (in == null) {
            return null;
        } else {
            return new UrlAssetInfo(assetManager, key, url, in);
        }
    }

    @Override
    public InputStream openStream() {
        if (in != null) {
            // Reuse the already existing stream (only once)
            InputStream in2 = in;
            in = null;
            return in2;
        } else {
            // Create a new stream for subsequent invocations.
            try {
                URLConnection conn = url.openConnection();
                conn.setUseCaches(false);
                return conn.getInputStream();
            } catch (IOException ex) {
                throw new RuntimeException("Failed to read URL " + url, ex);
            }
        }
    }
}
