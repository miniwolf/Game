package mini.utils.sky;

import mini.asset.AssetManager;
import mini.bounding.BoundingSphere;
import mini.material.Material;
import mini.math.Vector3f;
import mini.renderer.queue.RenderQueue;
import mini.scene.Geometry;
import mini.scene.Spatial;
import mini.scene.shape.Sphere;
import mini.textures.Image;
import mini.textures.Texture;
import mini.textures.TextureCubeMap;

import java.nio.ByteBuffer;

/**
 * <code>SkyFactory</code> is used to create {@link mini.scene.Spatial}s that can be attached to the
 * scene to display image in the background.
 */
public class SkyFactory {
    private AssetManager assetManager;

    public SkyFactory(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public Spatial createSky(Texture texture, EnvMapType envMapType) {
        return createSky(texture, Vector3f.UNIT_XYZ, envMapType);
    }

    private Spatial createSky(Texture texture, Vector3f normalScale, EnvMapType envMapType) {
        return createSky(texture, normalScale, envMapType, 10);
    }

    public Spatial createSky(Texture west, Texture east, Texture north, Texture south, Texture up,
                             Texture down) {
        return createSky(west, east, north, south, up, down, Vector3f.UNIT_XYZ);
    }

    public Spatial createSky(Texture west, Texture east, Texture north, Texture south, Texture up,
                             Texture down, Vector3f normalScale) {
        return createSky(west, east, north, south, up, down, normalScale, 10);
    }

    public Spatial createSky(Texture west, Texture east, Texture north, Texture south, Texture up,
                             Texture down, Vector3f normalScale, float sphereRadius) {
        Image westImg = west.getImage();
        Image eastImg = east.getImage();
        Image northImg = north.getImage();
        Image southImg = south.getImage();
        Image upImg = up.getImage();
        Image downImg = down.getImage();

        checkImagesForCubeMap(westImg, eastImg, northImg, southImg, upImg, downImg);

        Image cubeImage = new Image(westImg.getFormat(), westImg.getWidth(), westImg.getHeight(),
                                    null, westImg.getColorSpace());

        cubeImage.addData(westImg.getData(0));
        cubeImage.addData(eastImg.getData(0));
        cubeImage.addData(downImg.getData(0));
        cubeImage.addData(upImg.getData(0));
        cubeImage.addData(southImg.getData(0));
        cubeImage.addData(northImg.getData(0));

        TextureCubeMap cubeMap = new TextureCubeMap(cubeImage);
        return createSky(cubeMap, normalScale, EnvMapType.CubeMap, sphereRadius);
    }

    private void checkImagesForCubeMap(Image... images) {
        if (images.length == 1) {
            return;
        }

        Image.Format format = images[0].getFormat();
        int width = images[0].getWidth();
        int height = images[0].getHeight();

        ByteBuffer data = images[0].getData(0);
        int size = data != null ? data.capacity() : 0;

        checkImage(images[0]);

        for (int i = 1; i < images.length; i++) {
            Image image = images[i];
            checkImage(image);

            if (image.getFormat() != format) {
                throw new IllegalArgumentException("Images must be of the same format");
            }
            if (image.getWidth() != width || image.getHeight() != height) {
                throw new IllegalArgumentException("Images must have the same resolution");
            }
            ByteBuffer data2 = image.getData(0);
            if (data2 == null) {
                continue;
            }

            if (data2.capacity() != size) {
                throw new IllegalArgumentException("Images must have the same size");
            }
        }
    }

    private void checkImage(Image image) {
        if (image.getWidth() != image.getHeight()) {
            throw new IllegalArgumentException("Image width and height must be the same");
        }

        if (image.getMultiSamples() != 1) {
            throw new IllegalArgumentException("Multisample textures not supported");
        }
    }

    private Spatial createSky(Texture texture, Vector3f normalScale, EnvMapType envMapType,
                              float sphereRadius) {
        if (texture == null) {
            throw new IllegalArgumentException("texture cannot be null");
        }

        final Sphere sphere = new Sphere(10, 10, sphereRadius, false, true);

        Geometry sky = new Geometry("Sky", sphere);
        sky.setQueueBucket(RenderQueue.Bucket.Sky);
        sky.setCullHint(Spatial.CullHint.Never);
        sky.setModelBound(new BoundingSphere(Float.POSITIVE_INFINITY, Vector3f.ZERO));

        Material skyMaterial = new Material(assetManager, "MatDefs/Misc/Sky.minid");
        skyMaterial.setVector3("NormalScale", normalScale);

        switch (envMapType) {
            case CubeMap:
                if (!(texture instanceof TextureCubeMap)) {
                    Image image = texture.getImage();
                    texture = new TextureCubeMap();
                    texture.setImage(image);
                }
                break;
            case SphereMap:
                skyMaterial.setBoolean("SphereMap", true);
                break;
            case EquirectMap:
                skyMaterial.setBoolean("EquirectMap", true);
                break;
        }

        texture.setMagFilter(Texture.MagFilter.Bilinear);
        texture.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);
        texture.setAnisotropicFilter(0);
        texture.setWrap(Texture.WrapMode.EdgeClamp);
        skyMaterial.setTexture("Texture", texture);
        sky.setMaterial(skyMaterial);
        return sky;
    }
}
