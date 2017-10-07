package mini.material.plugins;

import mini.asset.MaterialKey;
import mini.asset.TextureKey;
import mini.material.MatParamTexture;
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

    private MiniLoader loader;

    @Mock
    private MiniLoader mockedMiniLoader;

    @Mock
    private AWTLoader mockedAWTLoader;

    @Before
    public void setUp() {
        try {
            when(mockedMiniLoader
                         .load(ArgumentMatchers.any(MaterialKey.class))).thenReturn(materialDef);
        } catch (IOException e) {
            e.printStackTrace();
        }
        loader = new MiniLoader();
    }

    @Test
    public void noDefaultTechnique_shouldBeSupported() throws IOException {
        //when(assetInfo.()).thenReturn(MiniLoader.class.getResourceAsStream("/no-default-technique.j3md"));
        MaterialDef def = (MaterialDef) loader.load(new MaterialKey("no-default-technique.minid"));
        assertEquals(1, def.getTechniqueDefs("Test").size());
    }

    @Test
    public void fixedPipelineTechnique_shouldBeIgnored() throws IOException {
//        when(assetInfo.openStream()).thenReturn(MiniLoader.class.getResourceAsStream("/no-shader-specified.j3md"));
        MaterialDef def = (MaterialDef) loader.load(new MaterialKey("no-shader-specified.minid"));
        assertEquals(null, def.getTechniqueDefs("A"));
        assertEquals(1, def.getTechniqueDefs("B").size());
    }

    @Test
    public void multipleSameNamedTechniques_shouldBeSupported() throws IOException {
//        when(assetInfo.openStream()).thenReturn(MiniLoader.class.getResourceAsStream("/same-name-technique.j3md"));
        MaterialDef def = (MaterialDef) loader.load(new MaterialKey("same-name-technique.minid"));
        assertEquals(2, def.getTechniqueDefs("Test").size());
        assertEquals(EnumSet.of(Caps.GLSL150),
                     def.getTechniqueDefs("Test").get(0).getRequiredCaps());
        assertEquals(EnumSet.of(Caps.GLSL100),
                     def.getTechniqueDefs("Test").get(1).getRequiredCaps());
    }

    @Test
    public void oldStyleTextureParameters_shouldBeSupported() throws Exception {
//        when(assetInfo.openStream()).thenReturn(J3MLoader.class.getResourceAsStream("/texture-parameters-oldstyle.j3m"));

        final Texture textureOldStyle = Mockito.mock(Texture.class);
        final Texture textureOldStyleUsingQuotes = Mockito.mock(Texture.class);

        final TextureKey textureKeyUsingQuotes = setupMockForTexture("OldStyleUsingQuotes",
                                                                     "old style using quotes/texture.png",
                                                                     true,
                                                                     textureOldStyleUsingQuotes);
        final TextureKey textureKeyOldStyle = setupMockForTexture("OldStyle",
                                                                  "old style/texture.png", true,
                                                                  textureOldStyle);

        loader.load(new MaterialKey("texture-parameters-oldstyle.mini"), mockedMiniLoader,
                    mockedAWTLoader);

        verify(mockedAWTLoader).load(textureKeyUsingQuotes);
        verify(mockedAWTLoader).load(textureKeyOldStyle);
        verify(textureOldStyle).setWrap(Texture.WrapMode.Repeat);
        verify(textureOldStyleUsingQuotes).setWrap(Texture.WrapMode.Repeat);
    }

    @Test
    public void newStyleTextureParameters_shouldBeSupported() throws Exception {
//        when(assetInfo.openStream()).thenReturn(J3MLoader.class.getResourceAsStream("/texture-parameters-newstyle.j3m"));

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

        loader.load(new MaterialKey("texture-parameters-newstyle.mini"), mockedMiniLoader,
                    mockedAWTLoader);

        verify(mockedAWTLoader).load(textureKeyNoParameters);
        verify(mockedAWTLoader).load(textureKeyFlip);

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
        when(mockedAWTLoader.load(textureKey)).thenReturn(texture);

        return textureKey;
    }
}
