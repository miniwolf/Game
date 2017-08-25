package mini.helloworld;

import mini.app.SimpleApplication;
import mini.asset.TextureKey;
import mini.light.DirectionalLight;
import mini.material.Material;
import mini.material.RenderState.BlendMode;
import mini.math.ColorRGBA;
import mini.math.Vector3f;
import mini.renderEngine.queue.RenderQueue;
import mini.scene.Geometry;
import mini.scene.shape.Box;
import mini.scene.shape.Sphere;
import mini.textures.Texture;
import mini.textures.plugins.AWTLoader;
import mini.utils.TangentBinormalGenerator;

/**
 * Sample 6 - how to give an object's surface a material and texture.
 * How to make objects transparent. How to make bumpy and shiny surfaces.
 */
public class HelloMaterial extends SimpleApplication {
    public static void main(String[] args) {
        System.setProperty("org.lwjgl.librarypath",
                           "C:/Users/miniwolf/Engine/Engine/lib/native/windows/");
        HelloMaterial app = new HelloMaterial();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        /** A simple textured cube -- in good MIP map quality. */
        Box cube1Mesh = new Box(1f, 1f, 1f);
        Geometry cube1Geo = new Geometry("My Textured Box", cube1Mesh);
        cube1Geo.setLocalTranslation(new Vector3f(-3f, 1.1f, 0f));
        Material cube1Mat = new Material("MatDefs/Misc/Unshaded.minid");
        TextureKey key = new TextureKey("Interface/Logo/Monkey.jpg", true);
        key.setGenerateMips(true);
        Texture cube1Tex = (Texture) AWTLoader.load(key);
        cube1Mat.setTexture("ColorMap", cube1Tex);
        cube1Geo.setMaterial(cube1Mat);
        rootNode.attachChild(cube1Geo);

        /** A translucent/transparent texture, similar to a window frame. */
        Box cube2Mesh = new Box(1f, 1f, 0.01f);
        Geometry cube2Geo = new Geometry("window frame", cube2Mesh);
        Material cube2Mat = new Material("MatDefs/Misc/Unshaded.minid");
        cube2Mat.setTexture("ColorMap",
                            (Texture) AWTLoader
                                    .load(new TextureKey("Textures/ColoredTex/Monkey.png")));
        cube2Mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);  // activate transparency
        cube2Geo.setQueueBucket(RenderQueue.Bucket.Transparent);
        cube2Geo.setMaterial(cube2Mat);
        rootNode.attachChild(cube2Geo);

        /** A bumpy rock with a shiny light effect. To make bumpy objects you must create a NormalMap. */
        Sphere sphereMesh = new Sphere(32, 32, 2f);
        Geometry sphereGeo = new Geometry("Shiny rock", sphereMesh);
        sphereMesh.setTextureMode(Sphere.TextureMode.Projected); // better quality on spheres
        TangentBinormalGenerator.generate(sphereMesh);           // for lighting effect
        Material sphereMat = new Material("MatDefs/Light/Lighting.minid");
        sphereMat.setTexture("DiffuseMap",
                             (Texture) AWTLoader
                                     .load(new TextureKey("Textures/Terrain/Pond/Pond.jpg")));
        sphereMat.setTexture("NormalMap",
                             (Texture) AWTLoader.load(new TextureKey(
                                     "Textures/Terrain/Pond/Pond_normal.png")));
        sphereMat.setBoolean("UseMaterialColors", true);
        sphereMat.setColor("Diffuse", ColorRGBA.White);
        sphereMat.setColor("Specular", ColorRGBA.White);
        sphereMat.setFloat("Shininess", 64f); // [0,128]
        sphereGeo.setMaterial(sphereMat);
        sphereGeo.setLocalTranslation(0, 2, -2); // Move it a bit
        sphereGeo.rotate(1.6f, 0, 0);          // Rotate it a bit
        rootNode.attachChild(sphereGeo);

        /** Must add a light to make the lit object visible! */
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(1, 0, -2).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);
    }
}
