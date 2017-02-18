#version 150

in vec3 in_position;
in vec2 in_textureCoords;
in vec3 in_normal;

out vec3 pass_normal;
out vec2 pass_textureCoords;
out vec3 pass_reflectVector;
out vec3 pass_viewVector;

uniform mat4 projectionViewMatrix;
uniform vec3 cameraPosition;

void main(void) {
    vec4 worldPosition = vec4(in_position, 1.0);
    gl_Position = projectionViewMatrix * worldPosition;

    pass_textureCoords = in_textureCoords;
    pass_normal = in_normal;

    vec3 unitNormal = normalize(in_normal);
    vec3 viewVector = normalize(worldPosition.xyz - cameraPosition);
    pass_reflectVector = reflect(viewVector, unitNormal);
    pass_viewVector = viewVector;
}