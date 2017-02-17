package main;

import loaders.LoaderSettings;
import loaders.SceneLoader;
import loaders.SceneLoaderFactory;
import mini.math.Vector3f;
import mini.renderEngine.RenderEngine;
import mini.scene.Scene;
import mini.utils.DisplayManager;
import mini.utils.MyFile;

public class MainApp {
    public static void main(String[] args) {
        System.setProperty("org.lwjgl.librarypath", "C:/Users/miniwolf/Engine/Engine/lib/lwjgl-natives-windows/");
        RenderEngine engine = RenderEngine.init();
        SceneLoader loader = SceneLoaderFactory.createSceneLoader();
        Scene scene = loader.loadScene(new MyFile(LoaderSettings.RES_FOLDER, "Socuwan Scene"));

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
