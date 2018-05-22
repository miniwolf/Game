in vec2 texCoord;

uniform sampler2D m_Texture;

void main() {
    gl_FragColor = texture2D(m_Texture, texCoord);
}