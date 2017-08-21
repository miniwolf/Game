package loaders;

import mini.scene.Node;
import mini.utils.MyFile;

public class EntityLoader {
    private ModelLoader modelLoader;
    private SkinLoader skinLoader;
    private ConfigsLoader configsLoader;

    protected EntityLoader(ModelLoader modelLoader, SkinLoader skinLoader,
                           ConfigsLoader configsLoader) {
        this.modelLoader = modelLoader;
        this.skinLoader = skinLoader;
        this.configsLoader = configsLoader;
    }

    protected Node loadEntity(MyFile entityFile) {
        Node entity = modelLoader.loadModel(new MyFile(entityFile, LoaderSettings.MODEL_FILE));
        Configs configs = configsLoader
                .loadConfigs(new MyFile(entityFile, LoaderSettings.CONFIGS_FILE));
        //setEntityConfigs(entity, configs, entityFile);
        return entity;

    }

//    private Material loadSkin(MyFile entityFile, Configs configs) {
//        Material material;
//        MyFile diffuseFile = new MyFile(entityFile, LoaderSettings.DIFFUSE_FILE);
//        if (configs.hasExtraMap()) {
//            material = skinLoader
//                    .loadSkin(diffuseFile, new MyFile(entityFile, LoaderSettings.EXTRA_MAP_FILE));
//        } else {
//            material = skinLoader.loadSkin(diffuseFile);
//        }
//        material.setTransparent(configs.hasTransparency());
//        return material;
//    }

//    private void setEntityConfigs(Node entity, Configs configs, MyFile entityFile) {
//        entity.setCastsShadow(configs.castsShadow());
//        entity.setHasReflection(configs.hasReflection());
//        entity.setSeenUnderWater(configs.hasRefraction());
//        entity.setImportant(configs.isImportant());
//        if (configs.getDiffuseMaps() != null) {
//            for (int i = 0; i < configs.getDiffuseMaps().size(); i++) {
//                String diffuseMap = configs.getDiffuseMaps().get(i);
//                MyFile myFile = new MyFile(entityFile, diffuseMap);
//                Texture texture = new TextureBuilder(myFile).create();
//                Spatial spatial = entity.getChildren().get(i);
//                ((Geometry) spatial).getMaterial()
//                                    .setTextureParam("diffuseMap", VarType.Texture2D, texture);
//            }
//        }
//    }
}
