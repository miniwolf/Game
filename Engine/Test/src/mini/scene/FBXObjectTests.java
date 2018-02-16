package mini.scene;

import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.file.FBXFile;
import mini.scene.plugins.fbx.mesh.FBXMesh;
import mini.scene.plugins.fbx.node.FBXNode;
import mini.scene.plugins.fbx.objects.FBXObjectLoader;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class FBXObjectTests {
    private static FBXElement objectElement;
    private FBXObjectLoader loader;
    private AssetManager assetManager;
    private AssetKey key;

    @BeforeClass
    public static void ctor() {
//        InputStream stream = FBXReader.class
//                .getResourceAsStream("/Models/Chloe/Chloe Price (No Jacket Episode 2).FBX");
//        FBXFile fbxFile = new FBXReader().readFBX(stream);
//        objectElement = getObjectElement(fbxFile);
    }

    private static FBXElement getObjectElement(FBXFile file) {
        return file.getElements().stream().filter(element -> element.getName().equals("Objects"))
                   .findFirst().get();
    }

    @Before
    public void initialize() {
        assetManager = new AssetManager();
        key = Mockito.mock(AssetKey.class);
        Mockito.when(key.getFolder()).thenReturn("Models");
        loader = new FBXObjectLoader(assetManager, key);
    }

    @Test
    public void LoadingObjectFromObjects_AreNotNull() {
//        Map<FBXId, FBXObject> load = loader.load(objectElement);
//        Assert.assertNotNull(load);
//        Assert.assertNotNull(load.values().iterator().next());
    }

    @Test
    public void CreatingGeometry_WillNotReturnNull() {
        FBXElement geometry = objectElement.getChildren().stream()
                                           .filter(element -> element.getName().equals("Geometry"))
                                           .findFirst().get();
        FBXMesh fbxMesh = new FBXMesh(assetManager, key);
        fbxMesh.fromElement(geometry);
    }

    @Test
    public void CreatingModel_WillNotReturnNull() {
        FBXElement modelElement = objectElement.getChildren().stream()
                                               .filter(element -> element.getName().equals("Model"))
                                               .findFirst().get();
        FBXNode fbxMesh = new FBXNode(assetManager, key);
        fbxMesh.fromElement(modelElement);
        //Node node = fbxMesh.getNode();
//        Assert.assertNotNull(node);
//        Assert.assertNotEquals(node.getLocalTransform(), Transform.IDENTITY);
    }
}
