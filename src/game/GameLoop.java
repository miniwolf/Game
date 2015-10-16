package game;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;
import obj.ModelData;
import obj.OBJLoader;
import rendering.*;
import terrain.Terrain;
import texture.ModelTexture;
import model.RawModel;
import model.TexturedModel;
import texture.TerrainTexture;
import texture.TerrainTexturePack;

/**
 * @author miniwolf
 */
public class GameLoop {
    public static void main(String[] args) {
        System.setProperty("org.lwjgl.librarypath", new File("native/windows").getAbsolutePath());
        DisplayManager.create();

        Loader loader = new Loader();

        ModelTexture texture = new ModelTexture(loader.loadTexture("white"));
        texture.setShineDamper(10);
        texture.setReflectivity(1);

        //ModelData sponzaData = OBJLoader.loadOBJ("sponza");

        //RawModel sponzaModel = loader.loadToVAO(sponzaData.getVertices(), sponzaData.getTexCoords(), sponzaData.getNormals(), sponzaData.getIndices());

        //TexturedModel texturedModel = new TexturedModel(sponzaModel, texture);
        //Entity sponza = new Entity(texturedModel, new Vector3f(0,0,0),0,0,0,2);

        ModelData fernData = OBJLoader.loadOBJ("fern");

        RawModel fernModel = loader.loadToVAO(fernData.getVertices(), fernData.getTexCoords(),
                                              fernData.getNormals(), fernData.getIndices());

        TexturedModel fern = new TexturedModel(fernModel, new ModelTexture(loader.loadTexture("fern")));
        fern.getTexture().setHasTransparency(false);

        ModelData pineData = OBJLoader.loadOBJ("pine");

        RawModel pineModel = loader.loadToVAO(pineData.getVertices(), pineData.getTexCoords(),
                                              pineData.getNormals(), pineData.getIndices());

        TexturedModel tree = new TexturedModel(pineModel, new ModelTexture(loader.loadTexture("pine")));

        TerrainTexture grassTexture = new TerrainTexture(loader.loadTexture("grass"));
        TerrainTexture dirtTexture = new TerrainTexture(loader.loadTexture("dirt"));
        TerrainTexture flowerTexture = new TerrainTexture(loader.loadTexture("grassFlowers"));
        TerrainTexture concreteTexture = new TerrainTexture(loader.loadTexture("concrete"));

        TerrainTexturePack texturePack = new TerrainTexturePack(grassTexture, dirtTexture, flowerTexture, concreteTexture);
        TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("blendMap"));

        List<Terrain> terrains = new ArrayList<>();
        terrains.add(new Terrain(0,-1, loader, texturePack, blendMap, "heightmap"));
        //for ( int x = -9; x < 10; x++ ) for (int z = -9; z < 10; z++) terrains.add(new Terrain(x, z, loader, texturePack, blendMap, "heightmap"));

        List<Entity> entities = new ArrayList<>();
        entities.add(new Entity(fern, new Vector3f(185, 0, -293), 0, 0, 0, 0.05f));
        entities.add(new Entity(fern, new Vector3f(370, 0, -300), 0, 0, 0, 0.05f));
        entities.add(new Entity(fern, new Vector3f(293, 0, -305), 0, 0, 0, 0.05f));
        Random random = new Random();
        for ( int i = 0; i < 400; i++ ) {
            float x = random.nextFloat() * 800 - 400;
            float z = random.nextFloat() * -600;
            float y = terrains.get(0).getHeightOfTerrain(x, z);
            entities.add(new Entity(fern, new Vector3f(x, y, z), 0, random.nextFloat() * 360, 0, 0.05f));
            if ( i % 2 == 0 ) {
                x = random.nextFloat() * 800 - 400;
                z = random.nextFloat() * -600;
                y = terrains.get(0).getHeightOfTerrain(x, z);
                entities.add(new Entity(tree, new Vector3f(x, y, z), 0, random.nextFloat() * 360, 0, 1));

            }
            //entities.add(new Entity(tree, new Vector3f(new Random().nextFloat() * 800, 0, new Random().nextFloat() * -600), 0, 0, 0, 0.2f));
        }
        //entities.add(sponza);
        Light sun = new Light(new Vector3f(0, 1000, -7000), new Vector3f(1f,1f,1f));
        List<Light> lights = new ArrayList<>();
        lights.add(sun);
        //lights.add(new Light(new Vector3f(100, -40, -80), new Vector3f(6,0,0), new Vector3f(1, 0.01f, 0.002f)));
        //lights.add(new Light(new Vector3f(370, 17, -300), new Vector3f(0,4,4), new Vector3f(1, 0.01f, 0.002f)));
        //lights.add(new Light(new Vector3f(293, 7, -305), new Vector3f(4,4,0), new Vector3f(1, 0.01f, 0.002f)));

        Renderer renderer = new Renderer(loader);
        ModelData bunnyData = OBJLoader.loadOBJ("bunny");

        RawModel bunnyModel = loader.loadToVAO(bunnyData.getVertices(), bunnyData.getTexCoords(),
                bunnyData.getNormals(), bunnyData.getIndices());

        TexturedModel bunny = new TexturedModel(bunnyModel, new ModelTexture(loader.loadTexture("white")));

        Player player = new Player(bunny, new Vector3f(0, 0, 0), 0, 0, 0, 1);

        //ModelData hairData = OBJLoader.loadOBJ("hairball");

        //RawModel hairModel = loader.loadToVAO(hairData.getVertices(), hairData.getTexCoords(),
//                                              hairData.getNormals(), hairData.getIndices());
//        TexturedModel hair = new TexturedModel(hairModel, new ModelTexture(loader.loadTexture("white")));
//        Player player = new Player(hair, new Vector3f(100, 0, -50), 0, 0, 0, 1);
        Camera camera = new Camera(player);
        while ( !Display.isCloseRequested() ) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
            camera.translate();
            player.translate(DisplayManager.getFrameTimeSeconds(), terrains.get(0));
            renderer.processEntity(player);
            terrains.forEach(renderer::processTerrain);
            entities.forEach(renderer::processEntity);
            renderer.render(lights, camera, false);

            DisplayManager.update();
        }

        renderer.cleanUp();
        loader.cleanUp();
        DisplayManager.close();
    }
}
