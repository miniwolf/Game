#version 400 core

in vec2 pass_texCoords;
in vec3 surfaceNormal;
in vec3 toLightVector[4];
in vec3 toCameraVector;
in float visibility;

out vec4 out_color;

uniform sampler2D backgroundTexture;
uniform sampler2D rTexture;
uniform sampler2D gTexture;
uniform sampler2D bTexture;
uniform sampler2D blendMap;

uniform vec3 lightColor[4];
uniform vec3 attenuation[4];
uniform vec3 skyColor;
uniform float shineDamper;
uniform float reflectivity;

void main(void) {
    vec4 blendColor = texture(blendMap, pass_texCoords);

    float backTextureAmount = 1 - (blendColor.r + blendColor.g + blendColor.b);
    vec2 tiledCoords = pass_texCoords * 40.0;
    vec4 backgroundTextureColor = texture(backgroundTexture, tiledCoords) * backTextureAmount;
    vec4 rTextureColor = texture(rTexture, tiledCoords) * blendColor.r;
    vec4 gTextureColor = texture(gTexture, tiledCoords) * blendColor.g;
    vec4 bTextureColor = texture(bTexture, tiledCoords) * blendColor.b;

    vec4 totalColor = backgroundTextureColor + rTextureColor + gTextureColor + bTextureColor;

    if ( totalColor.a < 0.5 ) discard;

    // Normalized interpolated normal
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
        if ( brightness > 0.0 ) {
            vec3 att = attenuation[i];
            float distance = length(toLightVector[i]);
            float attFactor = att.x + (att.y * distance) + (att.z * distance * distance);

            vec3 color = lightColor[i];
            totalDiffuse += (brightness * color) / attFactor;
            float dampedFactor = pow(specularFactor, shineDamper);
            totalSpecular += (dampedFactor * reflectivity * color) / attFactor;
        }
    }
    totalDiffuse = max(totalDiffuse, 0.2);

    out_color = vec4(totalDiffuse, 1.0) * totalColor + vec4(totalSpecular, 1.0);
    out_color = mix(vec4(skyColor, 1.0), out_color, visibility);
}
