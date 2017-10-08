package mini.material.plugins;

import mini.asset.AssetInfo;
import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.asset.TextureKey;
import mini.material.MatParamTexture;
import mini.material.Material;
import mini.material.MaterialDef;
import mini.renderer.Caps;
import mini.shaders.VarType;
import mini.textures.Texture;
import mini.textures.plugins.AWTLoader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AWTLoader.class})
public class MiniLoaderTest {
    @Mock
    private MaterialDef materialDef;
    @Mock
    private AssetInfo assetInfo;
    @Mock
    private AssetKey<Material> assetKey;
    @Mock
    private AssetManager assetManager;

    private MiniLoader loader;

    @Before
    public void setUp() {
        when(assetKey.getExtension()).thenReturn(".mini");
        when(assetInfo.getManager()).thenReturn(assetManager);
        when(assetInfo.getKey()).thenReturn(assetKey);
        when(assetManager.loadAsset(ArgumentMatchers.any(AssetKey.class))).thenReturn(materialDef);
        loader = new MiniLoader();
    }

    @Test
    public void noDefaultTechnique_shouldBeSupported() throws IOException {
        when(assetInfo.openStream())
                .thenReturn(MiniLoader.class.getResourceAsStream("/no-default-technique.minid"));
        MaterialDef def = (MaterialDef) loader.load(assetInfo);
        assertEquals(1, def.getTechniqueDefs("Test").size());
    }

    @Test
    public void fixedPipelineTechnique_shouldBeIgnored() throws IOException {
        when(assetInfo.openStream())
                .thenReturn(MiniLoader.class.getResourceAsStream("/no-shader-specified.minid"));
        MaterialDef def = (MaterialDef) loader.load(assetInfo);
        assertEquals(null, def.getTechniqueDefs("A"));
        assertEquals(1, def.getTechniqueDefs("B").size());
    }

    @Test
    public void multipleSameNamedTechniques_shouldBeSupported() throws IOException {
        when(assetInfo.openStream())
                .thenReturn(MiniLoader.class.getResourceAsStream("/same-name-technique.minid"));
        MaterialDef def = (MaterialDef) loader.load(assetInfo);
        assertEquals(2, def.getTechniqueDefs("Test").size());
        assertEquals(EnumSet.of(Caps.GLSL150),
                     def.getTechniqueDefs("Test").get(0).getRequiredCaps());
        assertEquals(EnumSet.of(Caps.GLSL100),
                     def.getTechniqueDefs("Test").get(1).getRequiredCaps());
    }

    @Test
    public void oldStyleTextureParameters_shouldBeSupported() throws Exception {
        when(assetInfo.openStream()).thenReturn(
                MiniLoader.class.getResourceAsStream("/texture-parameters-oldstyle.mini"));

        final Texture textureOldStyle = Mockito.mock(Texture.class);
        final Texture textureOldStyleUsingQuotes = Mockito.mock(Texture.class);

        final TextureKey textureKeyUsingQuotes = setupMockForTexture("OldStyleUsingQuotes",
                                                                     "old style using quotes/texture.png",
                                                                     true,
                                                                     textureOldStyleUsingQuotes);
        final TextureKey textureKeyOldStyle = setupMockForTexture("OldStyle",
                                                                  "old style/texture.png", true,
                                                                  textureOldStyle);

        loader.load(assetInfo);

        verify(assetManager).loadTexture(textureKeyUsingQuotes);
        verify(assetManager).loadTexture(textureKeyOldStyle);
        verify(textureOldStyle).setWrap(Texture.WrapMode.Repeat);
        verify(textureOldStyleUsingQuotes).setWrap(Texture.WrapMode.Repeat);
    }

    @Test
    public void newStyleTextureParameters_shouldBeSupported() throws Exception {
        when(assetInfo.openStream()).thenReturn(
                MiniLoader.class.getResourceAsStream("/texture-parameters-newstyle.mini"));

        final Texture textureNoParameters = Mockito.mock(Texture.class);
        final Texture textureFlip = Mockito.mock(Texture.class);
        final Texture textureRepeat = Mockito.mock(Texture.class);
        final Texture textureRepeatAxis = Mockito.mock(Texture.class);
        final Texture textureMin = Mockito.mock(Texture.class);
        final Texture textureMag = Mockito.mock(Texture.class);
        final Texture textureCombined = Mockito.mock(Texture.class);
        final Texture textureLooksLikeOldStyle = Mockito.mock(Texture.class);

        final TextureKey textureKeyNoParameters = setupMockForTexture("Empty", "empty.png", false,
                                                                      textureNoParameters);
        final TextureKey textureKeyFlip = setupMockForTexture("Flip", "flip.png", true,
                                                              textureFlip);
        setupMockForTexture("Repeat", "repeat.png", false, textureRepeat);
        setupMockForTexture("RepeatAxis", "repeat-axis.png", false, textureRepeatAxis);
        setupMockForTexture("Min", "min.png", false, textureMin);
        setupMockForTexture("Mag", "mag.png", false, textureMag);
        setupMockForTexture("Combined", "combined.png", true, textureCombined);
        setupMockForTexture("LooksLikeOldStyle", "oldstyle.png", true, textureLooksLikeOldStyle);

        loader.load(assetInfo);

        verify(assetManager).loadTexture(textureKeyNoParameters);
        verify(assetManager).loadTexture(textureKeyFlip);

        verify(textureRepeat).setWrap(Texture.WrapMode.Repeat);
        verify(textureRepeatAxis).setWrap(Texture.WrapAxis.T, Texture.WrapMode.Repeat);
        verify(textureMin).setMinFilter(Texture.MinFilter.Trilinear);
        verify(textureMag).setMagFilter(Texture.MagFilter.Bilinear);

        verify(textureCombined).setMagFilter(Texture.MagFilter.Nearest);
        verify(textureCombined).setMinFilter(Texture.MinFilter.BilinearNoMipMaps);
        verify(textureCombined).setWrap(Texture.WrapMode.Repeat);
    }

    private TextureKey setupMockForTexture(final String paramName, final String path,
                                           final boolean flipY, final Texture texture) {
        when(materialDef.getMaterialParam(paramName))
                .thenReturn(new MatParamTexture(VarType.Texture2D, paramName, texture, null));

        final TextureKey textureKey = new TextureKey(path, flipY);
        textureKey.setGenerateMips(true);

        when(assetManager.loadTexture(textureKey)).thenReturn(texture);

        return textureKey;
    }
}
