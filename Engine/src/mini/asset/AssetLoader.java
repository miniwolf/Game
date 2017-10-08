package mini.asset;

import java.io.IOException;

/**
 * An interface for asset loaders. An <code>AssetLoader</code> is responsible for loading a certain
 * type of asset associated with file extension(s).
 * The loader will load the data in the provided {@link AssetInfo} object by calling
 * {@link AssetInfo#openStream()}, returning an object representing the parsed data.
 */
public interface AssetLoader<T> {
    /**
     * Load asset from the given input stream, parsing it into an application-usable obejct.
     *
     * @param assetInfo Describe the asset information that we want to load
     * @return An object representing the resource.
     */
    T load(AssetInfo assetInfo) throws IOException;
}
