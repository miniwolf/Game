#import "ShaderLib/MultiSample.glsllib"

uniform vec4 m_EdgeColor;

uniform float m_EdgeWidth;
uniform float m_EdgeIntensity;

uniform float m_NormalThreshold;
uniform float m_DepthThreshold;

uniform float m_NormalSensitivity;
uniform float m_DepthSensitivity;

uniform COLORTEXTURE m_Texture;
uniform sampler2D m_NormalsTexture;
uniform DEPTHTEXTURE m_DepthTexture;

uniform vec2 g_ResolutionInverse;

in vec2 texCoord;

out vec4 out_color;

vec4 fetchNormalDepth(vec2 tc) {
    vec4 normalDepth;
    normalDepth.xyz = texture2D(m_NormalsTexture, tc).rgb;
    normalDepth.w = fetchTextureSample(m_DepthTexture, tc, 0).r;
    return normalDepth;
}

void main() {
    vec3 color = getColor(m_Texture, texCoord).rgb;

    vec2 edgeOffset = vec2(m_EdgeWidth) * g_ResolutionInverse;

    vec4 n1 = fetchNormalDepth(texCoord + vec2(-1.0, -1.0) * edgeOffset);
    vec4 n2 = fetchNormalDepth(texCoord + vec2( 1.0,  1.0) * edgeOffset);
    vec4 n3 = fetchNormalDepth(texCoord + vec2(-1.0,  1.0) * edgeOffset);
    vec4 n4 = fetchNormalDepth(texCoord + vec2( 1.0, -1.0) * edgeOffset);

    vec4 diagonalDelta = abs(n1 - n2) + abs(n3 - n4);

    float normalDelta = dot(diagonalDelta.xyz, vec3(1.0));
    float depthDelta = diagonalDelta.w;

    normalDelta = clamp((normalDelta - m_NormalThreshold) * m_NormalSensitivity, 0.0, 1.0);
    depthDelta = clamp((depthDelta - m_DepthThreshold) * m_DepthSensitivity, 0.0, 1.0);

    float edgeAmount = clamp(normalDelta + depthDelta, 0.0, 1.0);

    color = mix(color, m_EdgeColor.rgb, edgeAmount);

    out_color = vec4(color, 1.0);
}