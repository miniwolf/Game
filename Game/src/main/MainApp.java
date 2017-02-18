package main;

import loaders.LoaderSettings;
import loaders.SceneLoader;
import loaders.SceneLoaderFactory;
import mini.math.Vector2f;
import mini.math.Vector3f;
import mini.renderEngine.RenderEngine;
import mini.scene.Scene;
import mini.textures.GUITexture;
import mini.textures.Texture;
import mini.textures.TextureBuilder;
import mini.utils.DisplayManager;
import mini.utils.MyFile;

public class MainApp {
    public static void main(String[] args) {
        System.setProperty("org.lwjgl.librarypath",
                           "C:/Users/miniwolf/Engine/Engine/lib/lwjgl-natives-windows/");
        RenderEngine engine = RenderEngine.init();
        SceneLoader loader = SceneLoaderFactory.createSceneLoader();
        Scene scene = loader.loadScene(new MyFile(LoaderSettings.RES_FOLDER, "Socuwan Scene"));

        MyFile textureFile = new MyFile(LoaderSettings.RES_FOLDER
                                        + "/Socuwan Scene/Gui Texture/LxpnJ.png");
        scene.addGUITexture(new GUITexture(Texture.newTexture(textureFile).create().textureId,
                                           new Vector2f(0.5f, 0.5f), new Vector2f(0.05f,0.05f)));

        engine.renderEnvironmentMap(scene.getEnvironmentMap(), scene, new Vector3f(0, 2, 0));

        while (!DisplayManager.isCloseRequested()) {
            scene.getCamera().update(); // move camera
            engine.renderScene(scene); // render from new camera point
            engine.update(); // updates display
        }

        scene.delete();
        engine.close();
    }
}
