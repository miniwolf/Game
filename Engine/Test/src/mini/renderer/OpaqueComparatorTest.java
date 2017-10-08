package mini.renderer;

import mini.asset.AssetManager;
import mini.material.Material;
import mini.material.TechniqueDef;
import mini.math.ColorRGBA;
import mini.renderer.queue.GeometryList;
import mini.renderer.queue.OpaqueComparator;
import mini.scene.Geometry;
import mini.scene.Mesh;
import mini.scene.shape.Box;
import mini.system.TestUtil;
import mini.textures.Image;
import mini.textures.Image.Format;
import mini.textures.Texture;
import mini.textures.Texture2D;
import mini.textures.image.ColorSpace;
import mini.utils.BufferUtils;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

public class OpaqueComparatorTest {

    private final Mesh mesh = new Box(1, 1, 1);
    private Camera cam = new Camera(1, 1);
    private RenderManager renderManager;
    private AssetManager assetManager;
    private OpaqueComparator comparator = new OpaqueComparator();

    @Before
    public void setUp() {
        assetManager = TestUtil.createAssetManager();
        renderManager = TestUtil.createRenderManager();
        comparator.setCamera(cam);
    }

    /**
     * Given a correctly sorted list of materials, check if the
     * opaque comparator can sort a reversed list of them.
     * <p>
     * Each material will be cloned so that none of them will be equal to each other
     * in reference, forcing the comparator to compare the material sort ID.
     * <p>
     * E.g. for a list of materials A, B, C, the following list will be generated:
     * <pre>C, B, A, C, B, A, C, B, A</pre>, it should result in
     * <pre>A, A, A, B, B, B, C, C, C</pre>.
     *
     * @param materials The pre-sorted list of materials to check sorting for.
     */
    private void testSort(Material... materials) {
        GeometryList gl = new GeometryList(comparator);
        for (int g = 0; g < 5; g++) {
            for (int i = materials.length - 1; i >= 0; i--) {
                Geometry geom = new Geometry("geom", mesh);
                Material clonedMaterial = materials[i].clone();

                if (materials[i].getActiveTechnique() != null) {
                    String techniqueName = materials[i].getActiveTechnique().getDef().getName();
                    clonedMaterial.selectTechnique(techniqueName, renderManager);
                } else {
                    clonedMaterial
                            .selectTechnique(TechniqueDef.DEFAULT_TECHNIQUE_NAME, renderManager);
                }

                geom.setMaterial(clonedMaterial);
                gl.add(geom);
            }
        }
        gl.sort();

        for (int i = 0; i < gl.size(); i++) {
            Material mat = gl.get(i).getMaterial();
            String sortId = Integer.toHexString(mat.getSortId()).toUpperCase();
            System.out.print(sortId + "\t");
            System.out.println(mat);
        }

        Set<String> alreadySeen = new HashSet<>();
        Material current = null;
        for (int i = 0; i < gl.size(); i++) {
            Material mat = gl.get(i).getMaterial();
            if (current == null) {
                current = mat;
            } else if (!current.getName().equals(mat.getName())) {
                assert !alreadySeen.contains(mat.getName());
                alreadySeen.add(current.getName());
                current = mat;
            }
        }

        for (int i = 0; i < materials.length; i++) {
            for (int g = 0; g < 5; g++) {
                int index = i * 5 + g;
                Material mat1 = gl.get(index).getMaterial();
                Material mat2 = materials[i];
                assert mat1.getName().equals(mat2.getName()) :
                        mat1.getName() + " != " + mat2.getName() + " for index " + index;
            }
        }
    }

    @Test
    public void testSortByMaterialDef() {
        Material lightingMat = new Material(assetManager, "MatDefs/Light/Lighting.minid");
        Material particleMat = new Material(assetManager, "MatDefs/Misc/Particle.minid");
        Material unshadedMat = new Material(assetManager, "MatDefs/Misc/Unshaded.minid");
        Material skyMat = new Material(assetManager, "MatDefs/Misc/Sky.minid");

        lightingMat.setName("MatLight");
        particleMat.setName("MatParticle");
        unshadedMat.setName("MatUnshaded");
        skyMat.setName("MatSky");
        testSort(skyMat, lightingMat, particleMat, unshadedMat);
    }

    @Test
    public void testSortByTechnique() {
        Material lightingMatDefault = new Material(assetManager, "MatDefs/Light/Lighting.minid");
        Material lightingPreShadow = new Material(assetManager, "MatDefs/Light/Lighting.minid");
        Material lightingPostShadow = new Material(assetManager, "MatDefs/Light/Lighting.minid");
        Material lightingMatPreNormalPass = new Material(assetManager,
                                                         "MatDefs/Light/Lighting.minid");
        Material lightingMatGBuf = new Material(assetManager, "MatDefs/Light/Lighting.minid");
        Material lightingMatGlow = new Material(assetManager, "MatDefs/Light/Lighting.minid");

        lightingMatDefault.setName("TechDefault");
        lightingMatDefault.selectTechnique(TechniqueDef.DEFAULT_TECHNIQUE_NAME, renderManager);

        lightingPostShadow.setName("TechPostShad");
        lightingPostShadow.selectTechnique("PostShadow", renderManager);

        lightingPreShadow.setName("TechPreShad");
        lightingPreShadow.selectTechnique("PreShadow", renderManager);

        lightingMatPreNormalPass.setName("TechNorm");
        lightingMatPreNormalPass.selectTechnique("PreNormalPass", renderManager);

        lightingMatGlow.setName("TechGlow");
        lightingMatGlow.selectTechnique("Glow", renderManager);

        testSort(lightingMatGlow, lightingPreShadow, lightingMatPreNormalPass,
                 lightingMatDefault, lightingPostShadow);
    }

    @Test(expected = AssertionError.class)
    public void testNoSortByParam() {
        Material sameMat1 = new Material(assetManager, "MatDefs/Misc/Unshaded.minid");
        Material sameMat2 = new Material(assetManager, "MatDefs/Misc/Unshaded.minid");

        sameMat1.setName("MatRed");
        sameMat1.setColor("Color", ColorRGBA.Red);

        sameMat2.setName("MatBlue");
        sameMat2.setColor("Color", ColorRGBA.Blue);

        testSort(sameMat1, sameMat2);
    }

    private Texture createTexture(String name) {
        ByteBuffer bb = BufferUtils.createByteBuffer(3);
        Image image = new Image(Format.RGB8, 1, 1, bb, ColorSpace.sRGB);
        Texture2D texture = new Texture2D(image);
        texture.setName(name);
        return texture;
    }

    @Test
    public void testSortByTexture() {
        Material texture1Mat = new Material(assetManager, "MatDefs/Misc/Unshaded.minid");
        Material texture2Mat = new Material(assetManager, "MatDefs/Misc/Unshaded.minid");
        Material texture3Mat = new Material(assetManager, "MatDefs/Misc/Unshaded.minid");

        Texture tex1 = createTexture("A");
        tex1.getImage().setId(1);

        Texture tex2 = createTexture("B");
        tex2.getImage().setId(2);

        Texture tex3 = createTexture("C");
        tex3.getImage().setId(3);

        texture1Mat.setName("TexA");
        texture1Mat.setTexture("ColorMap", tex1);

        texture2Mat.setName("TexB");
        texture2Mat.setTexture("ColorMap", tex2);

        texture3Mat.setName("TexC");
        texture3Mat.setTexture("ColorMap", tex3);

        testSort(texture1Mat, texture2Mat, texture3Mat);
    }

    @Test
    public void testSortByShaderDefines() {
        Material lightingMat = new Material(assetManager, "MatDefs/Light/Lighting.minid");
        Material lightingMatVColor = new Material(assetManager, "MatDefs/Light/Lighting.minid");
        Material lightingMatVLight = new Material(assetManager, "MatDefs/Light/Lighting.minid");
        Material lightingMatTC = new Material(assetManager, "MatDefs/Light/Lighting.minid");
        Material lightingMatVColorLight = new Material(assetManager,
                                                       "MatDefs/Light/Lighting.minid");
        Material lightingMatTCVColorLight = new Material(assetManager,
                                                         "MatDefs/Light/Lighting.minid");

        lightingMat.setName("DefNone");

        lightingMatVColor.setName("DefVC");
        lightingMatVColor.setBoolean("UseVertexColor", true);

        lightingMatVLight.setName("DefVL");
        lightingMatVLight.setBoolean("VertexLighting", true);

        lightingMatTC.setName("DefTC");
        lightingMatTC.setBoolean("SeparateTexCoord", true);

        lightingMatVColorLight.setName("DefVCVL");
        lightingMatVColorLight.setBoolean("UseVertexColor", true);
        lightingMatVColorLight.setBoolean("VertexLighting", true);

        lightingMatTCVColorLight.setName("DefVCVLTC");
        lightingMatTCVColorLight.setBoolean("UseVertexColor", true);
        lightingMatTCVColorLight.setBoolean("VertexLighting", true);
        lightingMatTCVColorLight.setBoolean("SeparateTexCoord", true);

        testSort(lightingMat, lightingMatVColor, lightingMatVLight,
                 lightingMatVColorLight, lightingMatTC, lightingMatTCVColorLight);
    }

    @Test
    public void testSortByAll() {
        Material matBase1 = new Material(assetManager, "MatDefs/Light/Lighting.minid");
        Material matBase2 = new Material(assetManager, "MatDefs/Misc/Unshaded.minid");

        Texture texBase = createTexture("BASE");
        texBase.getImage().setId(1);
        Texture tex1 = createTexture("1");
        tex1.getImage().setId(2);
        Texture tex2 = createTexture("2");
        tex2.getImage().setId(3);

        matBase1.setName("BASE");
        matBase1.selectTechnique(TechniqueDef.DEFAULT_TECHNIQUE_NAME, renderManager);
        matBase1.setBoolean("UseVertexColor", true);
        matBase1.setTexture("DiffuseMap", texBase);

        Material mat1100 = matBase1.clone();
        mat1100.setName("1100");
        mat1100.selectTechnique("PreShadow", renderManager);

        Material mat1101 = matBase1.clone();
        mat1101.setName("1101");
        mat1101.selectTechnique("PreShadow", renderManager);
        mat1101.setTexture("DiffuseMap", tex1);

        Material mat1102 = matBase1.clone();
        mat1102.setName("1102");
        mat1102.selectTechnique("PreShadow", renderManager);
        mat1102.setTexture("DiffuseMap", tex2);

        Material mat1110 = matBase1.clone();
        mat1110.setName("1110");
        mat1110.selectTechnique("PreShadow", renderManager);
        mat1110.setFloat("AlphaDiscardThreshold", 2f);

        Material mat1120 = matBase1.clone();
        mat1120.setName("1120");
        mat1120.selectTechnique("PreShadow", renderManager);
        mat1120.setBoolean("UseInstancing", true);

        Material mat1121 = matBase1.clone();
        mat1121.setName("1121");
        mat1121.selectTechnique("PreShadow", renderManager);
        mat1121.setBoolean("UseInstancing", true);
        mat1121.setTexture("DiffuseMap", tex1);

        Material mat1122 = matBase1.clone();
        mat1122.setName("1122");
        mat1122.selectTechnique("PreShadow", renderManager);
        mat1122.setBoolean("UseInstancing", true);
        mat1122.setTexture("DiffuseMap", tex2);

        Material mat1140 = matBase1.clone();
        mat1140.setName("1140");
        mat1140.selectTechnique("PreShadow", renderManager);
        mat1140.setFloat("AlphaDiscardThreshold", 2f);
        mat1140.setBoolean("UseInstancing", true);

        Material mat1200 = matBase1.clone();
        mat1200.setName("1200");
        mat1200.selectTechnique("PostShadow", renderManager);

        Material mat1210 = matBase1.clone();
        mat1210.setName("1210");
        mat1210.selectTechnique("PostShadow", renderManager);
        mat1210.setFloat("AlphaDiscardThreshold", 2f);

        Material mat1220 = matBase1.clone();
        mat1220.setName("1220");
        mat1220.selectTechnique("PostShadow", renderManager);
        mat1220.setBoolean("UseInstancing", true);

        Material mat2000 = matBase2.clone();
        mat2000.setName("2000");

        testSort(mat1100, mat1101, mat1102, mat1110,
                 mat1120, mat1121, mat1122, mat1140,
                 mat1200, mat1210, mat1220, mat2000);
    }
}
