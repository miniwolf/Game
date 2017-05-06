package mini.environment;

import mini.textures.Image;
import mini.textures.Texture;
import mini.textures.TextureCubeMap;
import mini.textures.image.ColorSpace;

/**
 * This class holds several utility method useful for Physically Based
 * Rendering. It allows to compute useful pre filtered maps from an env map.
 *
 * @author miniwolf
 */
public class EnvMapUtils {
    /**
     * Creates a cube map from 6 images
     *
     * @param leftImg  west side image, also called negative x (negX) or left image
     * @param rightImg east side image, also called positive x (posX) or right image
     * @param downImg  bottom side image, also called negative y (negY) or down image
     * @param upImg    up side image, also called positive y (posY) or up image
     * @param backImg  south side image, also called positive z (posZ) or back image
     * @param frontImg north side image, also called negative z (negZ) or front image
     * @param format   format of the image
     * @return a cube map
     */
    public static TextureCubeMap makeCubeMap(Image rightImg, Image leftImg, Image upImg,
                                             Image downImg, Image backImg, Image frontImg,
                                             Image.Format format) {
        Image cubeImage = new Image(format, leftImg.getWidth(), leftImg.getHeight(), null,
                                    ColorSpace.Linear);

        cubeImage.addData(rightImg.getData(0));
        cubeImage.addData(leftImg.getData(0));

        cubeImage.addData(upImg.getData(0));
        cubeImage.addData(downImg.getData(0));

        cubeImage.addData(backImg.getData(0));
        cubeImage.addData(frontImg.getData(0));

        TextureCubeMap cubeMap = new TextureCubeMap(cubeImage);
        cubeMap.setMagFilter(Texture.MagFilter.Bilinear);
        cubeMap.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);
        cubeMap.setWrap(Texture.WrapMode.EdgeClamp);

        return cubeMap;
    }
}
