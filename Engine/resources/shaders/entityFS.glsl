#version 150

in vec2 pass_textureCoords;
//in vec3 pass_normal;
//in vec2 pass_pos;

out vec4 out_colour;

//uniform sampler2D m_diffuseMap;
//uniform sampler2D extraMap;
//uniform bool hasExtraMap;
//uniform vec3 g_LightDirection;

//uniform vec4 m_Ambient;
//uniform vec4 m_Diffuse;
//uniform vec4 m_Specular;
//uniform float m_Shininess;

const vec2 center = vec2(-2.53, 3.42);

//I want to use the glsl smoothstep function, but for some unknown reason it doesn't work on my laptop, but only when exported as a jar. Works fine in Eclipse!
float smoothlyStep(float edge0, float edge1, float x) {
    float t = clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0);
    return t * t * (3.0 - 2.0 * t);
}

void main(void) {
//    vec4 diffuseColour = texture(m_diffuseMap, pass_textureCoords);
//    if (diffuseColour.a < 0.5) {
//        discard;
//    }
//
//    float brightness = max(dot(-g_LightDirection, normalize(pass_normal)), 0.0) * 0.8 + 0.5;
//    if (hasExtraMap) {
//        float isGlowing = step(0.5, texture(extraMap, pass_textureCoords).g);
//        brightness = mix(brightness, 1.0, isGlowing);
//    }
//
//    out_colour = diffuseColour * brightness;
//    float disFactor = smoothlyStep(15.0, 16.0, distance(center, pass_pos));
//    out_colour.rgb = mix(out_colour.rgb, vec3(1.0), disFactor);
    out_colour = vec4(1,0,0,1);
}
