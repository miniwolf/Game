package mini.scene.plugins.fbx.material;

import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.material.Material;
import mini.material.RenderState;
import mini.math.ColorRGBA;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.obj.FBXObject;
import mini.scene.plugins.fbx.obj.FBXUnknownObject;
import mini.textures.Texture;
import mini.textures.image.ColorSpace;

public class FBXMaterial extends FBXObject<Material> {
    private AssetManager assetManager;
    private FBXMaterialProperties properties = new FBXMaterialProperties();

    public FBXMaterial(AssetManager assetManager, AssetKey key) {
        super(assetManager, key);
        this.assetManager = assetManager;
    }

    @Override
    public void fromElement(FBXElement element) {
        super.fromElement(element);
        for (FBXElement fbxElement : element.getFBXProperties()) {
            properties.setPropertyFromElement(fbxElement);
        }
//        MaterialInformation information = readMaterialElement(element);
//        material = createMaterial(information);
    }

    @Override
    protected Material toImplObject() {
        ColorRGBA diffuse = null;
        ColorRGBA specular = null;
        ColorRGBA transparent = null;
        ColorRGBA emissive = null;
        ColorRGBA ambient = null;
        float shininess = 1f;

        Texture diffuseMap = null;
        Texture specularMap = null;
        Texture transparentMap = null;
        Texture emitMap = null;
        Texture normalMap = null;
        FBXTexture fbxDiffuseMap = null;

        Object diffuseColor = properties.getProperty("DiffuseColor");
        if (diffuseColor != null) {
            if (diffuseColor instanceof ColorRGBA) {
                diffuse = ((ColorRGBA) diffuseColor).clone();
            } else if (diffuseColor instanceof FBXTexture) {
                FBXTexture texture = (FBXTexture) diffuseColor;
                fbxDiffuseMap = texture;
                diffuseMap = texture.getImplObject();
                diffuseMap.getImage().setColorSpace(ColorSpace.sRGB);
            }
        }

        Object diffuseFactor = properties.getProperty("DiffuseFactor");
        diffuse = setColorFromFactor(diffuse, diffuseFactor);

        Object specularColor = properties.getProperty("SpecularColor");
        if (specularColor != null) {
            if (specularColor instanceof ColorRGBA) {
                specular = ((ColorRGBA) specularColor).clone();
            } else if (specularColor instanceof FBXTexture) {
                FBXTexture texture = (FBXTexture) specularColor;
                specularMap = texture.getImplObject();
                specularMap.getImage().setColorSpace(ColorSpace.sRGB);
            }
        }

        Object specularFactor = properties.getProperty("SpecularFactor");
        specular = setColorFromFactor(specular, specularFactor);

        Object transparentColor = properties.getProperty("TransparentColor");
        if (transparentColor != null) {
            if (transparentColor instanceof ColorRGBA) {
                transparent = ((ColorRGBA) transparentColor);
            } else if (transparentColor instanceof FBXTexture) {
                FBXTexture texture = (FBXTexture) transparentColor;
                transparentMap = texture.getImplObject();
                transparentMap.getImage().setColorSpace(ColorSpace.sRGB);
            }
        }

        Object transparentFactor = properties.getProperty("TransparentFactor");
        transparent = setColorFromFactor(transparent, transparentFactor);

        Object emissiveColor = properties.getProperty("EmissiveColor");
        if (emissiveColor != null) {
            if (emissiveColor instanceof ColorRGBA) {
                emissive = ((ColorRGBA) emissiveColor).clone();
            } else if (emissiveColor instanceof FBXTexture) {
                FBXTexture texture = (FBXTexture) emissiveColor;
                emitMap = texture.getImplObject();
                emitMap.getImage().setColorSpace(ColorSpace.sRGB);
            }
        }

        Object emissiveFactor = properties.getProperty("EmissiveFactor");
        emissive = setColorFromFactor(emissive, emissiveFactor);

        Object ambientColor = properties.getProperty("AmbientColor");
        if (ambientColor instanceof ColorRGBA) {
            ambient = ((ColorRGBA) ambientColor).clone();
        }

        Object ambientFactor = properties.getProperty("AmbientFactor");
        ambient = setColorFromFactor(ambient, ambientFactor);

        Object bumpMap = properties.getProperty("NormalMap");
        if (bumpMap instanceof FBXTexture) {
            FBXTexture texture = (FBXTexture) bumpMap;
            normalMap = texture.getImplObject();
            normalMap.getImage().setColorSpace(ColorSpace.sRGB);
        }

        Object shininessFactor = properties.getProperty("Shininess");
        if (shininessFactor instanceof Float) {
            shininess = (Float) shininessFactor;
        }

        boolean alphaBlend = false;
        if (diffuseMap != null && diffuseMap == transparentMap) {
            // already using alpha from diffuseMap
            // if alpha blend is enabled
            // TODO: Maybe reset transparentMap == null
            alphaBlend = true;
        } else if (diffuseMap != null && transparentMap != null && diffuseMap != transparentMap) {
            // Alpha from diffuse may leak unintentionally
            alphaBlend = true;
        } else if (transparentMap != null) {
            alphaBlend = true;
        }

        if (transparent != null && transparent.a != 1f) {
            // Consolidate transparency into diffuse
            // We do not use a separate alpha color

            // Alpha from diffuse may leak unintentionally
            alphaBlend = true;
            if (diffuse != null) {
                diffuse.a = transparent.a;
            } else {
                diffuse = transparent;
            }
        }

        Material material = new Material(assetManager, "MatDefs/Light/Lighting.minid");
        material.setName(name);
        // TODO: Load this from the FBX Material
        material.setReceivesShadows(true);

        if (alphaBlend) {
            // TODO: This might be transparency or translucent model, gotta guess...
            material.setTransparent(true);
            material.setFloat("AlphaDiscardThreshold", 0.01f);
            material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        }

        material.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

        // Set colors.
        if (ambient != null || diffuse != null || specular != null) {
            // If either of those are set, we have to set them all.
            // NOTE: default specular is black unless it is set explicitly.
            material.setBoolean("UseMaterialColors", true);
            material.setColor("Ambient", ColorRGBA.White); // Reasons
            material.setColor("Diffuse", diffuse != null ? diffuse : ColorRGBA.White);
            material.setColor("Specular", specular != null ? specular : ColorRGBA.Black);
        }

        if (emissive != null) {
            material.setColor("GlowColor", emissive);
        }

        // Set shininess
        if (shininess > 1f) {
            // Convert shininess from Phong (FBX Shading model) to Blinn (Our shading model)
            float blinnShininess = (shininess * 5.1f) + 1f;
            material.setFloat("Shininess", blinnShininess);
        }

        // Set textures.
        if (diffuseMap != null) {
            material.setTexture("DiffuseMap", diffuseMap);
        }
        if (specularMap != null) {
            material.setTexture("SpecularMap", specularMap);
        }
        if (normalMap != null) {
            material.setTexture("NormalMap", normalMap);
        }
        if (emitMap != null) {
            material.setTexture("GlowMap", emitMap);
        }

        return material;
    }

    private ColorRGBA setColorFromFactor(ColorRGBA color, Object specularFactor) {
        if (specularFactor instanceof Float) {
            float factor = (Float) specularFactor;
            if (color != null) {
                multRGB(color, factor);
            } else {
                color = new ColorRGBA(factor, factor, factor, 1f);
            }
        }
        return color;
    }

    private void multRGB(ColorRGBA color, float factor) {
        color.r *= factor;
        color.g *= factor;
        color.b *= factor;
    }

    @Override
    public void link(FBXObject obj, String propertyName) {
        if (obj instanceof FBXTexture) {
            properties.setPropertyTexture(propertyName, (FBXTexture) obj);
        } else if (!(obj instanceof FBXUnknownObject)) {
            unsupportedConnectObjectProperty(obj, propertyName);
        }
    }

    @Override
    public void link(FBXObject obj) {
        unsupportedConnectObject(obj);
    }
}
