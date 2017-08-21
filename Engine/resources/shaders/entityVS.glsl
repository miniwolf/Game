#version 150

in vec3 in_position;
//in vec2 in_textureCoords;
//in vec3 in_normal;

//out vec2 pass_textureCoords;
//out vec3 pass_normal;
//out vec2 pass_pos;

uniform mat4 g_ViewProjectionMatrix;
//uniform vec4 plane;

void main(void) {
    vec4 worldPosition = vec4(in_position, 1.0);
    //pass_pos = worldPosition.xz;
    gl_Position = worldPosition;
    //gl_ClipDistance[0] = dot(worldPosition, plane);
    //pass_textureCoords = in_textureCoords;
    //pass_normal = in_normal;
}
