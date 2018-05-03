package mini.animation;

import assimp.AiScene;
import assimp.Importer;
import mini.app.SimpleApplication;

import java.io.File;

public class TestAssimpLoading extends SimpleApplication {

    public static void main(String[] args) {
        var app = new TestAssimpLoading();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        File file = new File(
                "C:/Users/miniwolf/Engine/Engine/Test/resources/Models/Chloe/Chloe Price (No Jacket Episode 2).FBX");
        AiScene scene;
        if (file.exists()) {
            scene = new Importer().readFile(file.getAbsolutePath());
        } else {
            return;
        }
        assert scene != null;
        var animations = scene.getAnimations();
        animations.size();
    }
}
