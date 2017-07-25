package main;

import loaders.LoaderSettings;
import loaders.SceneLoader;
import loaders.SceneLoaderFactory;
import mini.input.Keyboard;
import mini.input.KeyboardKey;
import mini.input.KeyboardListener;
import mini.input.Mouse;
import mini.input.MouseButton;
import mini.input.MouseListener;
import mini.math.Vector3f;
import mini.renderEngine.RenderEngine;
import mini.scene.Scene;
import mini.utils.DisplayManager;
import mini.utils.MyFile;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Callback;

import static java.awt.SystemColor.window;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_DEBUG_CONTEXT;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;

public class MainApp {
    public static void main(String[] args) {
        System.setProperty("org.lwjgl.librarypath",
                           "C:/Users/miniwolf/Engine/Engine/lib/lwjgl-natives-windows/");

        // before context creation
        RenderEngine engine = RenderEngine.init();
        SceneLoader loader = SceneLoaderFactory.createSceneLoader();
        //Scene scene = loader.loadScene(new MyFile(LoaderSettings.RES_FOLDER, "Socuwan Scene"));
        /*
        MyFile textureFile = new MyFile(LoaderSettings.RES_FOLDER
                                        + "/Socuwan Scene/Gui Texture/LxpnJ.png");
        scene.addGUITexture(new GUITexture(Texture.newTexture(textureFile).create().textureId,
                                           new Vector2f(0.5f, 0.5f), new Vector2f(0.05f,0.05f)));
        */

        // TODO: Seriously fix this
        //engine.renderEnvironmentMap(scene.getEnvironmentMap(), scene, new Vector3f(0, 2, 0));

        Mouse.addMouseListener(new MouseListener() {
            @Override
            public void onClick(MouseButton btn, double x, double y) {
                System.out.println("Mouse clicked");
                System.out.println(btn);
                System.out.println(x);
                System.out.println(y);
            }

            @Override
            public void onRelease(MouseButton btn, double x, double y) {
                System.out.println("Mouse released");
                System.out.println(btn);
                System.out.println(x);
                System.out.println(y);
            }

            @Override
            public void onScroll(double offset) {
                System.out.println("Scrolling");
                System.out.println(offset);
            }
        });

        Keyboard.addListener(new KeyboardListener() {
            @Override
            public void onClick(KeyboardKey key, int mods) {
                System.out.println("Keyboard button clicked");
                System.out.println(key);
            }

            @Override
            public void onRelease(KeyboardKey key, int mods) {
                System.out.println("Keyboard button released");
                System.out.println(key);
            }
        });

//        while (!DisplayManager.isCloseRequested()) {
//            scene.getCamera().update(); // move camera
//            engine.renderScene(scene); // render from new camera point
//            engine.update(); // updates display
//        }
//
//        scene.delete();
        engine.close();
    }
}
