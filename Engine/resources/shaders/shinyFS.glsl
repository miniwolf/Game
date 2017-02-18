#version 150

in vec2 pass_textureCoords;
in vec3 pass_normal;
in vec3 pass_reflectVector;
in vec3 pass_viewVector;

out vec4 out_colour;

uniform sampler2D diffuseMap;
uniform samplerCube enviroMap;
uniform vec3 lightDirection;

const float specularDamper = 5.0;
const float reflectivity = 0.3;

void main(void) {
    vec3 normalizedNormal = normalize(pass_normal);
    vec3 reflectedLight = reflect(lightDirection, normalizedNormal); //

    float diffuseLight = max(dot(-lightDirection, normalizedNormal), 0.0) * 0.8 + 0.5;
    float specularLight = pow(max(dot(reflectedLight, normalize(-pass_viewVector)), 0.0), specularDamper)*0.8;

    out_colour = texture(diffuseMap, pass_textureCoords) * diffuseLight;

    vec3 reflectVector = reflect(pass_viewVector, normalizedNormal);
    vec4 reflectedColour = texture(enviroMap, reflectVector);

    out_colour = mix(out_colour, reflectedColour, reflectivity) + specularLight;
}