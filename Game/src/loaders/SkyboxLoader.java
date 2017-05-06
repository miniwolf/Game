package loaders;

import mini.environment.EnvMapUtils;
import mini.skybox.Skybox;
import mini.textures.Image;
import mini.textures.Texture;
import mini.textures.plugins.AWTLoader;
import mini.utils.MyFile;

public class SkyboxLoader {
    protected Skybox loadSkyBox(MyFile skyboxFolder) {
        MyFile[] textureFiles = getSkyboxTexFiles(skyboxFolder);
        Image[] cubeMapImages = new Image[6];
        for (int i = 0; i < 6; ++i) {
            cubeMapImages[i] = (Image) AWTLoader.load(textureFiles[i]);
        }
        Texture cubeMap = EnvMapUtils.makeCubeMap(cubeMapImages[0], cubeMapImages[1],
                                                  cubeMapImages[2], cubeMapImages[3],
                                                  cubeMapImages[4], cubeMapImages[5],
                                                  Image.Format.RGB8);
        return new Skybox(cubeMap, LoaderSettings.SKYBOX_SIZE);
    }

    private MyFile[] getSkyboxTexFiles(MyFile skyboxFolder) {
        MyFile[] files = new MyFile[LoaderSettings.SKYBOX_TEX_FILES.length];
        for (int i = 0; i < files.length; i++) {
            files[i] = new MyFile(skyboxFolder, LoaderSettings.SKYBOX_TEX_FILES[i]);
        }
        return files;
    }
}
