package skybox;

import entities.Camera;
import entities.Light;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import shaders.Shader;
import tools.Maths;

import java.util.List;

/**
 * @author miniwolf
 */
public class SkyboxShader extends Shader {
    private static final String VERTEX_FILE = "resources/shaders/skyboxVS.glsl";
    private static final String FRAGMENT_FILE = "resources/shaders/skyboxFS.glsl";

    private int location_projectionMatrix;
    private int location_viewMatrix;
    private int location_fogColor;

    public SkyboxShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    @Override
    protected void getAllUniformLocations() {
        location_projectionMatrix = getUniformLocation("projectionMatrix");
        location_viewMatrix = getUniformLocation("viewMatrix");
    }

    @Override
    protected void bindAttributes() {
        bindAttributes(0, "position");
    }

    @Override
    public void connectTextureUnits() {

    }

    @Override
    public void loadSkyColor(float r, float g, float b) {
    }

    @Override
    public void loadFakeLightingVariable(boolean useFake) {

    }

    @Override
    public void loadTransformationMatrix(Matrix4f matrix) {

    }

    @Override
    public void loadProjectionMatrix(Matrix4f matrix) {
        loadMatrix(location_projectionMatrix, matrix);
    }

    @Override
    public void loadViewMatrix(Camera camera) {
        Matrix4f matrix = Maths.createViewMatrix(camera);
        matrix.m30 = 0;
        matrix.m31 = 0;
        matrix.m32 = 0;
        loadMatrix(location_viewMatrix, matrix);
    }

    @Override
    public void loadLights(List<Light> lights) {

    }

    @Override
    public void loadShineVariables(float damper, float reflectivity) {

    }
}
