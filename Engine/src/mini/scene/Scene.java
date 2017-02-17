package mini.scene;

import mini.math.Vector3f;
import mini.skybox.Skybox;
import mini.textures.Texture;
import mini.utils.Camera;
import mini.water.WaterTile;

import java.util.ArrayList;
import java.util.List;

public class Scene {
    // storing the entities multiple times in these various lists isn't great, but
    // it's okay for this very simple mini.scene with just a few object.

    private List<Entity> standardEntities = new ArrayList<>();
    private List<Entity> reflectableEntities = new ArrayList<>();
    private List<Entity> underwaterEntities = new ArrayList<>();
    private List<Entity> importantEntities = new ArrayList<>();
    private List<Entity> shinyEntities = new ArrayList<>();

    private List<WaterTile> waterTiles = new ArrayList<>();

    private Camera camera;
    private Vector3f lightDirection = new Vector3f(0, -1, 0);
    private Skybox sky;

    private Texture environmentMap;

    private float waterHeight = -0.1f;//should set elsewhere

    public Scene(Camera camera, Skybox sky) {
        this.camera = camera;
        this.sky = sky;
        environmentMap = Texture.newEmptyCubeMap(128);
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

    public void addTerrain(Entity terrain) {
        standardEntities.add(terrain);
        importantEntities.add(terrain);
        reflectableEntities.add(terrain);
        underwaterEntities.add(terrain);
    }

    public void addShiny(Entity entity) {
        if (entity.isSeenUnderWater()) {
            underwaterEntities.add(entity);
        }
        if (entity.hasReflection()) {
            reflectableEntities.add(entity);
        }
        shinyEntities.add(entity);
    }

    public void addEntity(Entity entity) {
        standardEntities.add(entity);
        if (entity.isSeenUnderWater()) {
            underwaterEntities.add(entity);
        }
        if (entity.hasReflection()) {
            reflectableEntities.add(entity);
        }
        if (entity.isImportant()) {
            importantEntities.add(entity);
        }
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

    public List<Entity> getReflectedEntities() {
        return reflectableEntities;
    }

    public List<Entity> getImportantEntities() {
        return importantEntities;
    }

    public List<Entity> getShinyEntities() {
        return shinyEntities;
    }

    public List<Entity> getUnderwaterEntities() {
        return underwaterEntities;
    }

    public List<Entity> getAllEntities() {
        return standardEntities;
    }

    public void delete() {
        sky.delete();
        for (Entity entity : standardEntities) {
            entity.delete();
        }
        environmentMap.delete();
    }
}
