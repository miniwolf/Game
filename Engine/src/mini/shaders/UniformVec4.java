package mini.shaders;

import mini.math.Vector4f;
import org.lwjgl.opengl.GL20;

public class UniformVec4 extends Uniform {
    public UniformVec4(String name) {
        super(name);
    }

    public void loadVec4(Vector4f vector) {
        loadVec4(vector.x, vector.y, vector.z, vector.w);
    }

    public void loadVec4(float x, float y, float z, float w) {
        GL20.glUniform4f(super.getLocation(), x, y, z, w);
    }
}
