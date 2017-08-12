package loaders;

import mini.renderEngine.Camera;
import mini.scene.Node;
import mini.scene.Scene;
import mini.skybox.Skybox;
import mini.utils.MyFile;

import java.util.List;

public class SceneLoader {
    private EntityLoader entityLoader;
    private SkyboxLoader skyLoader;

    public SceneLoader(EntityLoader entityLoader, SkyboxLoader skyLoader) {
        this.entityLoader = entityLoader;
        this.skyLoader = skyLoader;
    }

    public Node loadScene(MyFile sceneFile) {
        MyFile sceneList = new MyFile(sceneFile, LoaderSettings.ENTITY_LIST_FILE);
        List<String> lines = getLines(sceneList);
        assert lines != null;
        MyFile[] terrainFiles = readEntityFiles(lines.get(0), sceneFile);
        MyFile[] shinyFiles = readEntityFiles(lines.get(1), sceneFile);
        MyFile[] entityFiles = readEntityFiles(lines.get(2), sceneFile);
        Skybox sky = skyLoader.loadSkyBox(new MyFile(sceneFile, LoaderSettings.SKYBOX_FOLDER));
        return createScene(terrainFiles, entityFiles, shinyFiles, sky);
    }

    private Node createScene(MyFile[] terrainFiles, MyFile[] entityFiles, MyFile[] shinyFiles,
                              Skybox sky) {
        Camera camera = new Camera();
        Node scene = new Node("rootnode");
        //scene.attachChild(camera);
        //(camera, sky);
//        scene.setLightDirection(WorldSettings.LIGHT_DIR);
//        addEntities(scene, entityFiles);
//        addShinyEntities(scene, shinyFiles);
//        addTerrains(scene, terrainFiles);
        return scene;
    }

    private void addEntities(Scene scene, MyFile[] entityFiles) {
        for (MyFile file : entityFiles) {
            Node entity = entityLoader.loadEntity(file);
            //scene.(entity);
        }
    }

//    private void addShinyEntities(Scene scene, MyFile[] entityFiles) {
//        for (MyFile file : entityFiles) {
//            Node entity = entityLoader.loadEntity(file);
//            scene.addShiny(entity);
//        }
//    }
//
//    private void addTerrains(Scene scene, MyFile[] terrainFiles) {
//        for (MyFile file : terrainFiles) {
//            Entity entity = entityLoader.loadEntity(file);
//            scene.addTerrain(entity);
//        }
//    }

    private List<String> getLines(MyFile file) {
        try {
            return file.getLines();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Couldn't find mini.scene file: " + file);
            System.exit(-1);
            return null;
        }
    }

    private MyFile[] readEntityFiles(String line, MyFile sceneFile) {
        String[] names = line.split(LoaderSettings.SEPARATOR);
        MyFile[] files = new MyFile[names.length];
        for (int i = 0; i < files.length; i++) {
            files[i] = new MyFile(sceneFile, names[i]);
        }
        return files;
    }
}
