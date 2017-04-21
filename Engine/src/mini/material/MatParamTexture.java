package mini.material;

import mini.shaders.VarType;
import mini.textures.Texture;

/**
 * Created by miniwolf on 16-04-2017.
 */
public class MatParamTexture extends MatParam {
    private Texture texture;

    public MatParamTexture(VarType type, String name, Texture texture) {
        super(type, name, texture);
        this.texture = texture;
    }

    public void setTextureValue(Texture value) {
        this.value = value;
        this.texture = value;
    }
}
