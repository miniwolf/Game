package mini.scene;

import mini.scene.plugins.fbx.file.FBXFile;
import mini.scene.plugins.fbx.file.FBXReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class FBXReaderTests {
    private InputStream stream;
    private FBXReader reader;

    @Before
    public void initialize() {
        stream = FBXReader.class
                .getResourceAsStream("/Models/Chloe/Chloe Price (No Jacket Episode 2).FBX");
        reader = new FBXReader();
    }

    @Test
    public void LoadingAnFBXFile_CanReadTheVersionNumber() throws IOException {
        FBXFile load = reader.readFBX(stream);
        Assert.assertEquals(load.getVersion(), 7100);
    }

    @Test
    public void LoadingAnFBXFile_FileContainsMoreThanZeroElements() throws IOException {
        FBXFile load = reader.readFBX(stream);
        Assert.assertTrue(load.getElements().size() > 0);
    }

    @Test
    public void LoadingAnFBXFile_FirstElementIsNotNull() throws IOException {
        FBXFile load = reader.readFBX(stream);
        Assert.assertNotNull(load.getElements().get(0));
    }

    @Test
    public void WhenLoadingFBXFile_WeCanReadFirstElementsName() throws IOException {
        FBXFile load = reader.readFBX(stream);
        Assert.assertEquals(load.getElements().get(0).getName(), "FBXHeaderExtension");
    }

    @Test
    public void WhenLoadingFBXFile_PropertyListOfFirstElementIsNonEmpty() throws IOException {
        FBXFile load = reader.readFBX(stream);
        Assert.assertEquals("FileId", load.getElements().get(1).getName());
        Assert.assertEquals(1, load.getElements().get(1).getProperties().size());
        Assert.assertEquals(1, load.getElements().get(1).getPropertyTypes().length);
    }

    @Test
    public void LoadingAllElements_WillAddAllElement() throws IOException {
        FBXFile load = reader.readFBX(stream);
        Assert.assertEquals(11, load.getElements().size());
    }

    @Test
    public void LoadingAllElements_WillAddChildElements() throws IOException {
        FBXFile load = reader.readFBX(stream);
        Assert.assertEquals(6, load.getElements().get(0).getChildren().size());
        Assert.assertEquals('I',
                            load.getElements().get(0).getChildren().get(0).getPropertyTypes()[0]);
    }
}
