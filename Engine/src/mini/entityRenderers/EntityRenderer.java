package mini.entityRenderers;

import mini.material.Material;
import mini.math.Vector3f;
import mini.math.Vector4f;
import mini.renderEngine.CameraImpl;
import mini.renderEngine.RenderManager;
import mini.scene.Geometry;
import mini.scene.Node;
import mini.scene.Spatial;
import mini.shaders.VarType;
import mini.utils.OpenGlUtils;

import java.util.List;

public class EntityRenderer {
    private EntityShader shader;

    public EntityRenderer() {
        this.shader = new EntityShader();
    }

    public void render(List<Node> entities, CameraImpl camera, Vector3f lightDir, Vector4f clipPlane,
                       RenderManager rendererManager) {
        prepare(camera, lightDir, clipPlane, rendererManager);
        for (Node entity : entities) {
            for (Spatial spatial : entity.getChildren()) {
                if (spatial instanceof Geometry) {
                    Geometry geom = (Geometry) spatial;
                    Material material = geom.getMaterial();
                    //prepareSkin(material);
                    //material.render(geom, shader, rendererManager);
                    /*Geometry geom = (Geometry) spatial;
                    prepareSkin(geom.getMaterial());
                    VAO model = geom.getVao();
                    model.bind(0, 1, 2);
                    GL11.glDrawElements(GL11.GL_TRIANGLES, model.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
                    model.unbind(0, 1, 2);*/
                }
            }
        }
        finish();
    }

    public void cleanUp() {
        shader.cleanUp();
    }

    private void prepare(CameraImpl camera, Vector3f lightDir, Vector4f clipPlane,
                         RenderManager manager) {
        shader.start();
        manager.setCamera(camera);
        manager.setLightDir(lightDir);
        shader.getUniform("plane").setValue(VarType.Vector4, clipPlane);
        manager.updateUniformBindings(shader);
        //OpenGlUtils.antialias(true);
        //OpenGlUtils.disableBlending();
        //OpenGlUtils.enableDepthTesting(true);
    }

    private void finish() {
        shader.stop();
    }

    private void prepareSkin(Material material) {
        // TODO: Should be taken care of by material internal rendering
//        material.getDiffuseTexture().bindToUnit(0);
//        if (material.hasExtraMap()) {
//            material.getExtraInfoMap().bindToUnit(1);
//        }
        shader.getUniform("hasExtraMap").setValue(VarType.Boolean, material.hasExtraMap());
        OpenGlUtils.cullBackFaces(!material.hasTransparency());
    }
}
