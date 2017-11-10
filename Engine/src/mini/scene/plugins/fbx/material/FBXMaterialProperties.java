package mini.scene.plugins.fbx.material;

import mini.math.ColorRGBA;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.objects.FBXTexture;

import java.util.HashMap;
import java.util.Map;

public class FBXMaterialProperties {
    private static final Map<String, FBXMaterialProperty> propertyMetaMap = new HashMap<>();

    static {
        // Lighting -> Ambient
        defineProperty("AmbientColor", Type.Color);
        defineProperty("AmbientFactor", Type.Factor);
        defineAlias("Ambient", "AmbientColor");

        // Lighting -> DiffuseMap/Diffuse
        defineProperty("DiffuseColor", Type.Texture2DOrColor);
        defineProperty("DiffuseFactor", Type.Factor);
        defineAlias("Diffuse", "DiffuseColor");

        // Lighting -> SpecularMap/Specular
        defineProperty("SpecularColor", Type.Texture2DOrColor);
        defineProperty("SpecularFactor", Type.Factor);
        defineAlias("Specular", "SpecularColor");

        // Lighting -> AlphaMap/Diffuse
        defineProperty("TransparentColor", Type.Texture2DOrAlpha);

        // Lighting -> Diffuse
        defineProperty("TransparencyFactor", Type.Alpha);
        defineAlias("Opacity", "TransparencyFactor");

        // Lighting -> GlowMap/GlowColor
        defineProperty("EmissiveColor", Type.Texture2DOrColor);
        defineProperty("EmissiveFactor", Type.Factor);
        defineAlias("Emissive", "EmissiveColor");

        // Lighting -> Shininess
        defineProperty("Shininess", Type.Factor);
        defineAlias("ShininessExponent", "Shininess");

        // Lighting -> EnvMap
        defineProperty("ReflectionColor", Type.Texture2DOrColor);

        // Lighting -> FresnelParameters
        defineProperty("Reflectivity", Type.Factor);
        defineAlias("ReflectionFactor", "Reflectivity");

        // Lighting -> NormalMap
        defineProperty("NormalMap", Type.Texture2D);
        defineAlias("Normal", "NormalMap");

        // MultiLayer materials aren't supported anyway...
        defineProperty("MultiLayer", Type.Ignore);

        defineProperty("Bump", Type.Texture2DOrColor);

        defineProperty("BumpFactor", Type.Factor);
        defineProperty("DisplacementColor", Type.Color);
        defineProperty("DisplacementFactor", Type.Factor);

        // Shading model should not be specified under properties element.
        defineProperty("ShadingModel", Type.Ignore);
    }

    private final Map<String, Object> propertyValueMap = new HashMap<>();

    private static void defineProperty(String name, Type type) {
        propertyMetaMap.put(name, new FBXMaterialProperty(name, type));
    }

    private static void defineAlias(String alias, String name) {
        propertyMetaMap.put(alias, propertyMetaMap.get(name));
    }

    public static Type getPropertyType(String name) {
        FBXMaterialProperty property = propertyMetaMap.get(name);
        if (property == null) {
            return null;
        }
        return property.type;
    }

    public void setPropertyFromElement(FBXElement fbxElement) {
        String name = (String) fbxElement.getProperties().get(0);
        FBXMaterialProperty property = propertyMetaMap.get(name);

        if (property == null) {
            System.err.println("Unknown FBX material property '" + name + "'");
            return;
        }

        switch (property.type) {
            case Alpha:
            case Factor:
            case Texture2DOrAlpha:
            case Texture2DOrFactor:
                double value = (Double) fbxElement.getProperties().get(4);
                propertyValueMap.put(property.name, (float) value);
                break;
            case Color:
            case Texture2DOrColor:
                double x = (Double) fbxElement.getProperties().get(4);
                double y = (Double) fbxElement.getProperties().get(5);
                double z = (Double) fbxElement.getProperties().get(6);
                ColorRGBA color = new ColorRGBA((float) x, (float) y, (float) z, 1.0f);
                propertyValueMap.put(property.name, color);
                break;
            default:
                System.err.println("FBX material property '" + name + "' requires a texture.");
        }
    }

    public void setPropertyTexture(String propertyName, FBXTexture texture) {
        FBXMaterialProperty property = propertyMetaMap.get(propertyName);

        if (property == null) {
            System.err.println("Unknown FBX material property '" + propertyName + "'");
            return;
        }

        if (propertyValueMap.get(propertyName) instanceof FBXTexture) {
            // This can happen for Multiple / layered textures ...
            // Just write into the 2nd slot for now (could be an idea for lightmaps).
            propertyName = propertyName + "2";
        }

        propertyValueMap.put(propertyName, texture);
    }

    public Object getProperty(String name) {
        return propertyValueMap.get(name);
    }

    private enum Type {
        Color,
        Alpha,
        Factor,
        Texture2DOrColor,
        Texture2DOrAlpha,
        Texture2DOrFactor,
        Texture2D,
        TextureCubeMap,
        Ignore
    }

    private static class FBXMaterialProperty {
        private final String name;
        private final Type type;

        FBXMaterialProperty(String name, Type type) {
            this.name = name;
            this.type = type;
        }
    }
}
