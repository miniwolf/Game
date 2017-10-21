package mini.scene.plugins.fbx.objects;

import mini.asset.AssetKey;
import mini.asset.AssetManager;
import mini.material.Material;
import mini.material.RenderState;
import mini.math.ColorRGBA;
import mini.math.Vector3f;
import mini.scene.plugins.fbx.file.FBXElement;
import mini.scene.plugins.fbx.obj.FBXObject;

public class FBXMaterial extends FBXObject<Material> {
    private AssetManager assetManager;
    private Material material;

    public FBXMaterial(AssetManager assetManager, AssetKey key) {
        super(assetManager, key);
        this.assetManager = assetManager;
    }

    @Override
    public void fromElement(FBXElement element) {
        super.fromElement(element);
        MaterialInformation information = readMaterialElement(element);
        material = createMaterial(information);
    }

    private Material createMaterial(MaterialInformation information) {
        Material material = new Material(assetManager, "MatDefs/Light/Lighting.minid");
        material.setName(name);
        material.setColor("Ambient", information.getAmbientColor());
        material.setColor("Diffuse", information.getDiffuseColor());
        material.setColor("Specular", information.getSpecularColor());
        material.setFloat("Shininess", information.getShininessExponent());
        material.setBoolean("UseMaterialColors", true);
        material.setFloat("AlphaDiscardThreshold", 0.5f);
        material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        return material;
    }

    private MaterialInformation readMaterialElement(FBXElement fbxElement) {
        MaterialInformation information = new MaterialInformation();
        for (FBXElement element : fbxElement.getChildren()) {
            switch (element.getName()) {
                case "Properties70":
                    for (FBXElement property : element.getChildren()) {
                        String propertyName = (String) property.getProperties().get(0);
                        switch (propertyName) {
                            case "AmbientColor":
                                information.setAmbientColor(readVectorFromProperty(property));
                                break;
                            case "DiffuseColor":
                                information.setDiffuseColor(readVectorFromProperty(property));
                                break;
                            case "SpecularColor":
                                information.setSpecularColor(readVectorFromProperty(property));
                                break;
                            case "ShininessExponent":
                                information.setShininessExponent(
                                        ((Double) property.getProperties().get(4)).floatValue());
                                break;
                        }
                    }
                    break;
            }
        }
        return information;
    }

    public Material getMaterial() {
        return material;
    }

    @Override
    public void link(FBXObject obj, String propertyName) {
        if (obj instanceof FBXTexture) {
            FBXTexture other = (FBXTexture) obj;
            if (other.getTexture() == null || material == null) {
                return;
            }

            switch (propertyName) {
                case "DiffuseColor":
                    material.setTexture("DiffuseMap", other.getTexture());
                    material.setColor("Diffuse", ColorRGBA.White);
                    break;
                case "SpecularColor":
                    material.setTexture("SpecularMap", other.getTexture());
                    material.setColor("Specular", ColorRGBA.White);
                    break;
                case "NormalMap":
                    material.setTexture("NormalMap", other.getTexture());
                    break;
                default:
                    System.out.println(propertyName);
            }
        } else {
            System.out.println(obj);
        }
    }

    @Override
    public void link(FBXObject obj) {
        System.out.println(obj);
    }

    private class MaterialInformation {
        private Vector3f ambientColor;
        private Vector3f diffuseColor;
        private Vector3f specularColor;
        private float shininessExponent;

        public Vector3f getAmbientColor() {
            return ambientColor;
        }

        public void setAmbientColor(Vector3f ambientColor) {
            this.ambientColor = ambientColor;
        }

        public Vector3f getDiffuseColor() {
            return diffuseColor;
        }

        public void setDiffuseColor(Vector3f diffuseColor) {
            this.diffuseColor = diffuseColor;
        }

        public Vector3f getSpecularColor() {
            return specularColor;
        }

        public void setSpecularColor(Vector3f specularColor) {
            this.specularColor = specularColor;
        }

        public float getShininessExponent() {
            return shininessExponent;
        }

        public void setShininessExponent(float shininessExponent) {
            this.shininessExponent = shininessExponent;
        }
    }
}
