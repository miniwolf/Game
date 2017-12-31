#import "ShaderLib/GLSLCompat.glsllib"
#import "ShaderLib/Instancing.glsllib"

in vec3 inPosition;

out vec4 worldPosition;

void main() {
    worldPosition = vec4(inPosition, 1.0);
    gl_Position = TransformWorldViewProjection(worldPosition);
}