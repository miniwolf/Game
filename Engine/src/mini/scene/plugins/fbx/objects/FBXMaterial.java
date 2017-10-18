package mini.scene.plugins.fbx.objects;

import mini.asset.AssetManager;
import mini.material.Material;
import mini.material.RenderState;
import mini.math.Vector3f;
import mini.scene.plugins.fbx.file.FBXElement;

public class FBXMaterial extends FBXObject {
    private AssetManager assetManager;
    private Material material;

    public FBXMaterial(FBXElement element, AssetManager assetManager) {
        super(element);
        this.assetManager = assetManager;
        initializeElement();
    }

    protected void initializeElement() {
        MaterialInformation information = readMaterialElement();
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

    private MaterialInformation readMaterialElement() {
        MaterialInformation information = new MaterialInformation();
        for (FBXElement element : element.getChildren()) {
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
