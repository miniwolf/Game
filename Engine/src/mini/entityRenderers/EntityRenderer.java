package mini.entityRenderers;

import mini.material.Material;
import mini.math.Vector3f;
import mini.math.Vector4f;
import mini.renderEngine.opengl.GLRenderer;
import mini.scene.Entity;
import mini.scene.Geometry;
import mini.scene.Spatial;
import mini.utils.Camera;
import mini.utils.OpenGlUtils;

import java.util.List;

public class EntityRenderer {
    private EntityShader shader;

    public EntityRenderer() {
        this.shader = new EntityShader();
    }

    public void render(List<Entity> entities, Camera camera, Vector3f lightDir,
                       Vector4f clipPlane, GLRenderer renderer) {
        prepare(camera, lightDir, clipPlane, renderer);
        for (Entity entity : entities) {
            for (Spatial spatial : entity.getChildren()) {
                if (spatial instanceof Geometry) {
                    Geometry geom = (Geometry) spatial;
                    geom.getMaterial().render(renderer);
                    renderer.renderMeshFromGeometry(geom);
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

    private void prepare(Camera camera, Vector3f lightDir, Vector4f clipPlane, GLRenderer renderer) {
        shader.start();
        shader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
        shader.lightDirection.loadVec3(lightDir);
        shader.plane.loadVec4(clipPlane);
        OpenGlUtils.antialias(true);
        OpenGlUtils.disableBlending();
        OpenGlUtils.enableDepthTesting(true);
        renderer.setShader(shader);
    }

    private void finish() {
        shader.stop();
    }

    private void prepareSkin(Material material) {
        material.getDiffuseTexture().bindToUnit(0);
        if (material.hasExtraMap()) {
            material.getExtraInfoMap().bindToUnit(1);
        }
        shader.hasExtraMap.loadBoolean(material.hasExtraMap());
        OpenGlUtils.cullBackFaces(!material.hasTransparency());
    }
}
