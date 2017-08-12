package mini.scene;

import mini.math.Vector3f;
import mini.renderEngine.Camera;
import mini.skybox.Skybox;
import mini.textures.GUITexture;
import mini.textures.Texture;
import mini.water.WaterTile;

import java.util.ArrayList;
import java.util.List;

public class Scene {
    // storing the entities multiple times in these various lists isn't great, but
    // it's okay for this very simple mini.scene with just a few object.

    private List<Node> standardEntities = new ArrayList<>();
    private List<Node> reflectableEntities = new ArrayList<>();
    private List<Node> underwaterEntities = new ArrayList<>();
    private List<Node> importantEntities = new ArrayList<>();
    private List<Node> shinyEntities = new ArrayList<>();

    private List<WaterTile> waterTiles = new ArrayList<>();
    private List<GUITexture> guiTextures = new ArrayList<>();

    private Camera camera;
    private Vector3f lightDirection = new Vector3f(0, -1, 0);
    private Skybox sky;

    private Texture environmentMap;

    private float waterHeight = -0.1f;//should set elsewhere

    public Scene(Camera camera, Skybox sky) {
        this.camera = camera;
        this.sky = sky;
//        environmentMap = Texture.newEmptyCubeMap(128);
        waterTiles.add(new WaterTile(-20, 6, waterHeight));
        waterTiles.add(new WaterTile(-10, 6, waterHeight));
        waterTiles.add(new WaterTile(0, 6, waterHeight));
        waterTiles.add(new WaterTile(10, 6, waterHeight));
    }

    public void setLightDirection(Vector3f direction) {
        direction.normalizeLocal();
        this.lightDirection.set(direction);
    }

    public Texture getEnvironmentMap() {
        return environmentMap;
    }

    public float getWaterHeight() {
        return waterHeight;
    }

    public List<WaterTile> getWater() {
        return waterTiles;
    }

    public Skybox getSky() {
        return sky;
    }

    public Vector3f getLightDirection() {
        return lightDirection;
    }

    public Camera getCamera() {
        return camera;
    }

    public List<Node> getReflectedEntities() {
        return reflectableEntities;
    }

    public List<Node> getImportantEntities() {
        return importantEntities;
    }

    public List<Node> getShinyEntities() {
        return shinyEntities;
    }

    public List<Node> getUnderwaterEntities() {
        return underwaterEntities;
    }

    public List<Node> getAllEntities() {
        return standardEntities;
    }

    public List<GUITexture> getGUI() {
        return guiTextures;
    }

    public void delete() {
        sky.delete();
        for (Node node : standardEntities) {
            //node.delete();
        }
        //environmentMap.delete();
    }

    public void addGUITexture(GUITexture texture) {
        guiTextures.add(texture);
    }
}
