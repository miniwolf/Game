package mini.asset;

/**
 * <code>CloneableAssetProcessor</code> simply calls {@link Object#clone()} on assets to clone them.
 * No processing is applied.
 */
public class CloneableAssetProcessor implements AssetProcessor {
    @Override
    public Object createClone(Object obj) {
        CloneableSmartAsset asset = (CloneableSmartAsset) obj;
        return asset.clone();
    }
}
