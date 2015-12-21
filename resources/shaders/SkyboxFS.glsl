#version 400

in vec3 texCoord;
out vec4 color;

uniform samplerCube cubeMap;
uniform vec3 fogColor;

const float lowerLimit = 0.0;
const float upperLimit = 30.0;

void main(void) {
    vec4 finalColor = texture(cubeMap, texCoord);

    float factor = (texCoord.y - lowerLimit) / (upperLimit - lowerLimit);
    factor = clamp(factor, 0.0, 1.0);
    //color = mix(vec4(fogColor, 1.0), finalColor, factor);
    color = finalColor;
}