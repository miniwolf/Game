#version 140

in vec2 in_position;

out vec2 textureCoords;

uniform mat4 transformationMatrix;

void main(void) {
    gl_Position = transformationMatrix * vec4(in_position, 0.0, 1.0);
    textureCoords = vec2((in_position.x + 1.0) * 0.5, 1 - (in_position.y + 1.0) * 0.5);
}