#import "ShaderLib/GLSLCompat.glsllib"
#import "ShaderLib/Optics.glsllib"

uniform ENVMAP m_Texture;

in vec3 direction;

out vec4 color;

void main() {
    vec3 dir = normalize(direction);
    color = Optics_GetEnvColor(m_Texture, dir);
}

