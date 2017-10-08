package mini.scene.plugins;

import mini.asset.AssetInfo;
import mini.asset.AssetKey;
import mini.asset.AssetLoader;
import mini.asset.AssetManager;
import mini.asset.TextureKey;
import mini.material.Material;
import mini.material.RenderState;
import mini.math.ColorRGBA;
import mini.textures.Texture;
import mini.utils.MyFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class MTLLoader implements AssetLoader {
    private Scanner scan;
    private Map<String, Material> matList;
    //protected Material material;
    private String folderName;
    protected AssetKey key;

    private Texture diffuseMap, normalMap, specularMap, alphaMap;
    protected ColorRGBA ambient = new ColorRGBA();
    protected ColorRGBA diffuse = new ColorRGBA();
    protected ColorRGBA specular = new ColorRGBA();
    private float shininess = 16;
    private boolean shadeless;
    private String matName;
    protected float alpha = 1;
    private boolean transparent = false;
    private AssetManager assetManager;

    public void reset() {
        scan = null;
        matList = null;
//        material = null;

        resetMaterial();
    }

    private ColorRGBA readColor() {
        ColorRGBA v = new ColorRGBA();
        v.set(scan.nextFloat(), scan.nextFloat(), scan.nextFloat(), 1.0f);
        return v;
    }

    private String nextStatement() {
        scan.useDelimiter("\n");
        String result = scan.next();
        scan.useDelimiter("\\p{javaWhitespace}+");
        return result;
    }

    private boolean skipLine() {
        try {
            scan.skip(".*\r?\n");
            return true;
        } catch (NoSuchElementException ex) {
            // EOF
            return false;
        }
    }

    private void resetMaterial() {
        ambient.set(ColorRGBA.DarkGray);
        diffuse.set(ColorRGBA.LightGray);
        specular.set(ColorRGBA.Black);
        shininess = 16;
        shadeless = false;
        transparent = false;
        matName = null;
        diffuseMap = null;
        specularMap = null;
        normalMap = null;
        alphaMap = null;
        alpha = 1;
    }

    private void createMaterial() {
        Material material;

        if (alpha < 1f && transparent) {
            diffuse.a = alpha;
        }

        if (shadeless) {
            material = new Material(assetManager, "MatDefs/Misc/Unshaded.minid");
            material.setColor("Color", diffuse.clone());
            material.setTexture("ColorMap", diffuseMap);
            // TODO: Add handling for alpha map?
        } else {
            material = new Material(assetManager, "MatDefs/Light/Lighting.minid");
            material.setBoolean("UseMaterialColors", true);
            material.setColor("Ambient", ambient.clone());
            material.setColor("Diffuse", diffuse.clone());
            material.setColor("Specular", specular.clone());
            material.setFloat("Shininess", shininess); // prevents "premature culling" bug

            if (diffuseMap != null) material.setTexture("DiffuseMap", diffuseMap);
            if (specularMap != null) material.setTexture("SpecularMap", specularMap);
            if (normalMap != null) material.setTexture("NormalMap", normalMap);
            if (alphaMap != null) material.setTexture("AlphaMap", alphaMap);
        }

        if (transparent) {
            material.setTransparent(true);
            material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            material.setFloat("AlphaDiscardThreshold", 0.01f);
        }

        matList.put(matName, material);
    }

    private void startMaterial(String name) {
        if (matName != null) {
            // material is already in cache, generate it
            createMaterial();
        }

        // now, reset the params and set the name to start a new material
        resetMaterial();
        matName = name;
    }

    private Texture loadTexture(String path) {
        String[] split = path.trim().split("\\p{javaWhitespace}+");

        // will crash if path is an empty string
        path = split[split.length - 1];

        String name = new MyFile(path).getName();
        TextureKey texKey = new TextureKey(folderName + name);
        texKey.setGenerateMips(true);
        Texture texture = null;
        try {
            texture = assetManager.loadTexture(texKey);
            texture.setWrap(Texture.WrapMode.Repeat);
        } catch (Exception ex) {
            System.err.println("Cannot locate " + texKey + " for material " + key);
//            texture = new Texture2D(PlaceholderAssets.getPlaceholderImage(assetManager));
//            texture.setWrap(Texture.WrapMode.Repeat);
//            texture.setKey(key);
        }
        return texture;
    }

    protected boolean readLine() {
        if (!scan.hasNext()) {
            return false;
        }

        String cmd = scan.next().toLowerCase();
        if (cmd.startsWith("#")) {
            // skip entire comment until next line
            return skipLine();
        } else if (cmd.equals("newmtl")) {
            String name = scan.next();
            startMaterial(name);
        } else if (cmd.equals("ka")) {
            ambient.set(readColor());
        } else if (cmd.equals("kd")) {
            diffuse.set(readColor());
        } else if (cmd.equals("ks")) {
            specular.set(readColor());
        } else if (cmd.equals("ns")) {
            float shiny = scan.nextFloat();
            if (shiny >= 1) {
                shininess = shiny; /* (128f / 1000f)*/
                if (specular.equals(ColorRGBA.Black)) {
                    specular.set(ColorRGBA.White);
                }
            } else {
                // For some reason blender likes to export Ns 0 statements
                // Ignore Ns 0 instead of setting it
            }

        } else if (cmd.equals("d") || cmd.equals("tr")) {
            float tempAlpha = scan.nextFloat();
            if (tempAlpha > 0.0f && tempAlpha < 1.0f) {
                alpha = tempAlpha;
                transparent = true;
            }
        } else if (cmd.equals("map_ka")) {
            // ignore it for now
            return skipLine();
        } else if (cmd.equals("map_kd")) {
            String path = nextStatement();
            diffuseMap = loadTexture(path);
        } else if (cmd.equals("map_bump") || cmd.equals("bump")) {
            if (normalMap == null) {
                String path = nextStatement();
                normalMap = loadTexture(path);
            }
        } else if (cmd.equals("map_ks")) {
            String path = nextStatement();
            specularMap = loadTexture(path);
            if (specularMap != null) {
                // NOTE: since specular color is modulated with specmap
                // make sure we have it set
                if (specular.equals(ColorRGBA.Black)) {
                    specular.set(ColorRGBA.White);
                }
            }
        } else if (cmd.equals("map_d")) {
            String path = scan.next();
            alphaMap = loadTexture(path);
            transparent = true;
        } else if (cmd.equals("illum")) {
            int mode = scan.nextInt();

            switch (mode) {
                case 0:
                    // no lighting
                    shadeless = true;
                    break;
                case 1:
                    break;
                case 2:
                case 3:
                case 5:
                case 8:
                    break;
                case 4:
                case 6:
                case 7:
                case 9:
                    // Enable transparency
                    // Works best if diffuse map has an alpha channel
                    transparent = true;
                    break;
            }
        } else if (cmd.equals("ke") || cmd.equals("ni")) {
            // Ni: index of refraction - unsupported
            // Ke: emission color
            return skipLine();
        } else {
            System.err.println("Unknown statement in MTL! " + cmd);
            return skipLine();
        }

        return true;
    }

    @SuppressWarnings("empty-statement")
    public Object load(AssetInfo info) throws IOException {
        reset();

        this.assetManager = info.getManager();

        key = info.getKey();
        folderName = key.getFolder();
        matList = new HashMap<>();

        InputStream in = null;
        try {
            in = info.openStream();
            scan = new Scanner(in);
            scan.useLocale(Locale.US);

            while (readLine()) {
                ;
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }

        if (matName != null) {
            // still have a material in the vars
            createMaterial();
            resetMaterial();
        }

        return matList;
    }
}