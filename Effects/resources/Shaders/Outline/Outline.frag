uniform sampler2D m_Texture;
uniform sampler2D m_OutlineDepthTexture;
uniform sampler2D m_DepthTexture;

in vec2 texCoord;

uniform vec2 m_Resolution;
uniform vec4 m_OutlineColor;
uniform float m_OutlineWidth;

void main() {
    vec4 depth = texture2D(m_OutlineDepthTexture, texCoord);

    vec4 color = texture2D(m_Texture, texCoord);
    if (depth == vec4(0)) {
        gl_FragColor = m_OutlineColor;
    } else {
        gl_FragColor = color;
    }
}