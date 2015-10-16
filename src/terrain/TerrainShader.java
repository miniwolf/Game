package terrain;

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
public class TerrainShader extends Shader {
    private static String VERTEX_FILE = "resources/shaders/TerrainVS.glsl";
    private static String FRAGMENT_FILE = "resources/shaders/TerrainFS.glsl";

    private int location_transformationMatrix;
    private int location_projectionMatrix;
    private int location_viewMatrix;
    private int location_lightPosition[];
    private int location_lightColor[];
    private int location_attenuation[];
    private int location_shineDamper;
    private int location_reflectivity;
    private int location_skyColor;
    private int location_backgroundTexture;
    private int location_rTexture;
    private int location_gTexture;
    private int location_bTexture;
    private int location_blendMap;

    public TerrainShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    @Override
    protected void bindAttributes() {
        super.bindAttributes(0, "position");
        super.bindAttributes(1, "texCoord");
        super.bindAttributes(2, "normal");
    }

    @Override
    protected void getAllUniformLocations() {
        location_transformationMatrix = super.getUniformLocation("transformationMatrix");
        location_projectionMatrix = super.getUniformLocation("projectionMatrix");
        location_viewMatrix = super.getUniformLocation("viewMatrix");
        location_shineDamper = super.getUniformLocation("shineDamper");
        location_reflectivity = super.getUniformLocation("reflectivity");
        location_skyColor = super.getUniformLocation("skyColor");
        location_backgroundTexture = super.getUniformLocation("backgroundTexture");
        location_rTexture = super.getUniformLocation("rTexture");
        location_gTexture = super.getUniformLocation("gTexture");
        location_bTexture = super.getUniformLocation("bTexture");
        location_blendMap = super.getUniformLocation("blendMap");

        location_lightPosition = new int[MAX_LIGHTS];
        location_lightColor = new int[MAX_LIGHTS];
        location_attenuation = new int[MAX_LIGHTS];
        for (int i = 0; i < MAX_LIGHTS; i++) {
            location_lightPosition[i] = super.getUniformLocation("lightPosition[" + i + "]");
            location_lightColor[i] = super.getUniformLocation("lightColor[" + i + "]");
            location_attenuation[i] = super.getUniformLocation("attenuation[" + i + "]");
        }
    }

    @Override
    public void connectTextureUnits() {
        super.loadInt(location_backgroundTexture, 0);
        super.loadInt(location_rTexture, 1);
        super.loadInt(location_gTexture, 2);
        super.loadInt(location_bTexture, 3);
        super.loadInt(location_blendMap, 4);
    }

    @Override
    public void loadSkyColor(float r, float g, float b) {
        super.loadVector(location_skyColor, new Vector3f(r, g, b));
    }

    @Override
    public void loadFakeLightingVariable(boolean useFake) {}

    @Override
    public void loadTransformationMatrix(Matrix4f matrix) {
        super.loadMatrix(location_transformationMatrix, matrix);
    }

    @Override
    public void loadProjectionMatrix(Matrix4f projection) {
        super.loadMatrix(location_projectionMatrix, projection);
    }

    @Override
    public void loadViewMatrix(Camera camera) {
        Matrix4f viewMatrix = Maths.createViewMatrix(camera);
        super.loadMatrix(location_viewMatrix, viewMatrix);
    }

    @Override
    public void loadLights(List<Light> lights) {
        for (int i = 0; i < MAX_LIGHTS; i++) {
            if ( i < lights.size() ) {
                super.loadVector(location_lightPosition[i], lights.get(i).getPosition());
                super.loadVector(location_lightColor[i], lights.get(i).getColor());
                super.loadVector(location_attenuation[i], lights.get(i).getAttenuation());
            } else {
                // TODO: Optimize maybe check length i VS
                super.loadVector(location_lightPosition[i], new Vector3f(0,0,0));
                super.loadVector(location_lightColor[i], new Vector3f(0,0,0));
                super.loadVector(location_attenuation[i], new Vector3f(1,0,0));
            }
        }
    }

    @Override
    public void loadShineVariables(float damper, float reflectivity) {
        super.loadFloat(location_shineDamper, damper);
        super.loadFloat(location_reflectivity, reflectivity);
    }
}
