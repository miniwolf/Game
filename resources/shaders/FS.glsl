#version 400 core

in vec2 pass_texCoords;
in vec3 surfaceNormal;
in vec3 toLightVector[4];
in vec3 toCameraVector;
in float visibility;

out vec4 out_color;

uniform sampler2D textureSampler;
uniform vec3 lightColor[4];
uniform vec3 attenuation[4];
uniform float shineDamper;
uniform float reflectivity;
uniform vec3 skyColor;

void main(void) {
    vec3 unitNormal = normalize(surfaceNormal);
    vec3 unitVectorToCamera = normalize(toCameraVector);

    vec3 totalDiffuse = vec3(0.0);
    vec3 totalSpecular = vec3(0.0);

    for ( int i = 0; i < 4; i++ ) {
        vec3 unitLightVector = normalize(toLightVector[i]);
        vec3 lightDirection = -unitLightVector;
        vec3 reflectedLightDirection = reflect(lightDirection, unitNormal);
        float specularFactor = max(dot(reflectedLightDirection, unitVectorToCamera), 0);
        float brightness = max(dot(unitNormal, unitLightVector), 0.0);

        vec3 att = attenuation[i];
        float distance = length(toLightVector[i]);
        float attFactor = att.x + (att.y * distance) + (att.z * distance * distance);

        vec3 color = lightColor[i];
        //float brightness = 100 / distance;
        totalDiffuse += (brightness * color) / attFactor;
        float dampedFactor = pow(specularFactor, shineDamper);
        totalSpecular += (dampedFactor * reflectivity * color) / attFactor;
    }

    totalDiffuse = max(totalDiffuse, 0.2);

    vec4 textureColor = texture(textureSampler, pass_texCoords);

    if ( textureColor.a < 0.5 ) discard;

    out_color = vec4(totalDiffuse, 1.0) * textureColor + vec4(totalSpecular, 1.0);
    out_color.rgb *= visibility;
}
