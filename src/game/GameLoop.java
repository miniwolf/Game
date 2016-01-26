package game;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import editor.Editor;
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
import tools.MousePicker;

/**
 * @author miniwolf
 */
public class GameLoop {
    public static void main(String[] args) {
        System.setProperty("org.lwjgl.librarypath", new File("lib/native/windows").getAbsolutePath());
        DisplayManager.create();

        Loader loader = new Loader();

        // Terrain
        TerrainTexture grassTexture = new TerrainTexture(loader.loadTexture("grass"));
        TerrainTexture dirtTexture = new TerrainTexture(loader.loadTexture("dirt"));
        TerrainTexture flowerTexture = new TerrainTexture(loader.loadTexture("grassFlowers"));
        TerrainTexture concreteTexture = new TerrainTexture(loader.loadTexture("concrete"));

        TerrainTexturePack texturePack = new TerrainTexturePack(grassTexture, dirtTexture, flowerTexture, concreteTexture);
        TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("blendMap"));

        List<Terrain> terrains = new ArrayList<>();
        terrains.add(new Terrain(0, 0, loader, texturePack, blendMap, "heightmap"));

        // Fern
        ModelData fernData = OBJLoader.loadOBJ("fern").get(0);
        RawModel fernModel = loader.loadToVAO(fernData.getVertices(), fernData.getTexCoords(),
                                              fernData.getNormals(), fernData.getIndices());

        TexturedModel fern = new TexturedModel(fernModel, new ModelTexture(loader.loadTexture("fern")));

        // Pine
        ModelData pineData = OBJLoader.loadOBJ("pine").get(0);
        RawModel pineModel = loader.loadToVAO(pineData.getVertices(), pineData.getTexCoords(),
                                              pineData.getNormals(), pineData.getIndices());

        TexturedModel tree = new TexturedModel(pineModel, new ModelTexture(loader.loadTexture("pine")));

        // Sponza
        List<ModelData> modelDatas = OBJLoader.loadOBJ("sponza");

        ModelData sponzaData = modelDatas.get(0);
        RawModel sponzaModel = loader.loadToVAO(sponzaData.getVertices(), sponzaData.getTexCoords(),
                                                sponzaData.getNormals(), sponzaData.getIndices());

        TexturedModel sponza = new TexturedModel(sponzaModel, new ModelTexture(loader.loadTexture("bricks")));


        List<Entity> entities = new ArrayList<>();
        entities.add(new Entity(sponza, new Vector3f(0, 10, 0), 0,0,0, 1.0f));
        entities.add(new Entity(tree, new Vector3f(100, 40, -80), 0, 0, 0, 0.05f, GL11.GL_TRIANGLES));
        entities.add(new Entity(tree, new Vector3f(370, 35, -300), 0, 0, 0, 0.05f, GL11.GL_TRIANGLES));
        entities.add(new Entity(tree, new Vector3f(293, 20, -305), 0, 0, 0, 0.05f, GL11.GL_TRIANGLES));
        entities.add(new Entity(tree, new Vector3f(0, 100, -700), 0, 0, 0, 1f));
        Random random = new Random();
        for ( int i = 0; i < 400; i++ ) {
            float x = random.nextFloat() * 800;
            float z = random.nextFloat() * 600;
            float y = terrains.get(0).getHeightOfTerrain(x, z);
            entities.add(new Entity(fern, new Vector3f(x, y, z), 0, random.nextFloat() * 360, 0, 0.05f));
            if ( i % 2 == 0 ) {
                x = random.nextFloat() * 800;
                z = random.nextFloat() * 600;
                y = terrains.get(0).getHeightOfTerrain(x, z);
                entities.add(new Entity(tree, new Vector3f(x, y, z), 0, random.nextFloat() * 360, 0, 1));
            }
        }
        Light sun = new Light(new Vector3f(0, 1000, -7000), new Vector3f(1f,1f,1f));
        List<Light> lights = new ArrayList<>();
        lights.add(sun);
        //lights.add(new Light(new Vector3f(100, 40, -80), new Vector3f(1,0,0), new Vector3f(1, 0.01f, 0.002f)));
        //lights.add(new Light(new Vector3f(370, 35, -300), new Vector3f(0,1,0), new Vector3f(1, 0.01f, 0.002f)));
        //lights.add(new Light(new Vector3f(293, 20, -305), new Vector3f(0,0,1), new Vector3f(1, 0.01f, 0.002f)));

        ModelData bunnyData = OBJLoader.loadOBJ("bunny").get(0);
        RawModel bunnyModel = loader.loadToVAO(bunnyData.getVertices(), bunnyData.getTexCoords(),
                                               bunnyData.getNormals(), bunnyData.getIndices());

        TexturedModel bunny = new TexturedModel(bunnyModel, new ModelTexture(loader.loadTexture("white")));

        Player player = new Player(bunny, new Vector3f(5, 0, 0), 0, 0, 0, 1);
        Camera camera = new Camera(player);

        // Prerender
        Renderer renderer = new Renderer(loader);
        prerender(renderer, player, terrains, entities);

        MousePicker picker = new MousePicker(renderer.getProjectionMatrix(), camera, terrains);

        Editor editor = new Editor();
        //editor.add(new Entity(tree, new Vector3f(0, 0, 0), 0, 0, 0, 1f));
        //editor.add(new Entity(fern, new Vector3f(0, 0, 0), 0, 0, 0, 0.05f));
        editor.add(new Entity(sponza, new Vector3f(0, 0, 0), 0, 0, 0, 5f));

        // Render
        while ( !Display.isCloseRequested() ) {
            update(camera, player, terrains, picker);

            updatePlacer(picker, editor, renderer);

            // Debug transition light
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
            renderer.render(lights, camera, true);

            DisplayManager.update();
        }

        // Cleanup
        renderer.cleanUp();
        loader.cleanUp();
        DisplayManager.close();
    }

    private static void updatePlacer(MousePicker picker, Editor editor, Renderer renderer) {
        Vector3f terrainPoint = picker.getCurrentTerrainPoint();

        if ( terrainPoint != null ) {
            editor.update(terrainPoint, renderer);
        }
    }

    private static void update(Camera camera, Player player, List<Terrain> terrains, MousePicker picker) {
        camera.translate();
        player.translate(DisplayManager.getFrameTimeSeconds(), terrains.get(0));
        picker.update();
    }

    private static void prerender(Renderer renderer, Player player, List<Terrain> terrains, List<Entity> entities) {
        //renderer.processEntity(player);   TODO: Debug the placement of objects
        terrains.forEach(renderer::processTerrain);
        entities.forEach(renderer::processEntity);
    }
}
