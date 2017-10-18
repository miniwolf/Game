package mini.scene;

import mini.asset.AssetManager;
import mini.math.Transform;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.file.FBXFile;
import mini.scene.plugins.fbx.file.FBXReader;
import mini.scene.plugins.fbx.objects.FBXMesh;
import mini.scene.plugins.fbx.objects.FBXNode;
import mini.scene.plugins.fbx.objects.FBXObject;
import mini.scene.plugins.fbx.objects.FBXObjectLoader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class FBXObjectTests {
    private static FBXElement objectElement;
    private FBXObjectLoader loader;

    @BeforeClass
    public static void initialze() throws IOException {
        InputStream stream = FBXReader.class
                .getResourceAsStream("/Models/Chloe/Chloe Price (No Jacket Episode 2).FBX");
        FBXFile fbxFile = new FBXReader().readFBX(stream);
        objectElement = getObjectElement(fbxFile);
    }

    private static FBXElement getObjectElement(FBXFile file) {
        return file.getElements().stream().filter(element -> element.getName().equals("Objects"))
                   .findFirst().get();
    }

    @Before
    public void initialize() {
        AssetManager assetManager = new AssetManager();
        loader = new FBXObjectLoader(assetManager);
    }

    @Test
    public void LoadingObjectFromObjects_AreNotNull() {
        List<FBXObject> load = loader.load(objectElement);
        Assert.assertNotNull(load);
        Assert.assertNotNull(load.get(0).getElement());
    }

    @Test
    public void CreatingGeometry_WillNotReturnNull() {
        FBXElement geometry = objectElement.getChildren().stream()
                                           .filter(element -> element.getName().equals("Geometry"))
                                           .findFirst().get();
        FBXMesh fbxMesh = new FBXMesh(geometry);
        List<Geometry> geometries = fbxMesh.CreateGeometry();
        Assert.assertEquals(896, geometries.size());
    }

    @Test
    public void CreatingModel_WillNotReturnNull() {
        FBXElement modelElement = objectElement.getChildren().stream()
                                               .filter(element -> element.getName().equals("Model"))
                                               .findFirst().get();
        FBXNode fbxMesh = new FBXNode(modelElement);
        Node node = fbxMesh.getNode();
        Assert.assertNotNull(node);
        Assert.assertNotEquals(node.getLocalTransform(), Transform.IDENTITY);
    }
}
