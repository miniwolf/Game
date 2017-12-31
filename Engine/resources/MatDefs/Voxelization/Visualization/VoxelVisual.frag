#define INV_STEP_LENGTH (1.0f/STEP_LENGTH)
#define STEP_LENGTH 0.005f

uniform vec3 g_CameraPosition; // World camera position.

uniform sampler2D m_textureBack; // Unit cube back FBO.
uniform sampler2D m_textureFront; // Unit cube front FBO.
uniform sampler3D m_texture3D; // Texture in which voxelization is stored.
uniform int state = 0; // Decides mipmap sample level.

in vec2 texCoord;

// Scales and bias a given vector (i.e. from [-1, 1] to [0, 1]).
vec3 scaleAndBias(vec3 p) { return 0.5f * p + vec3(0.5f); }

// Returns true if p is inside the unity cube (+ e) centered on (0, 0, 0).
bool isInsideCube(vec3 p, float e) { return abs(p.x) < 1 + e && abs(p.y) < 1 + e && abs(p.z) < 1 + e; }

void main() {
    const float mipmapLevel = state;

    // Initialize ray.
    const vec3 origin = isInsideCube(g_CameraPosition, 0.2f) ? 
        g_CameraPosition : texture(m_textureFront, texCoord).xyz;
    vec3 direction = texture(m_textureBack, texCoord).xyz - origin;
    const uint numberOfSteps = uint(INV_STEP_LENGTH * length(direction));
    direction = normalize(direction);

    // Trace.
    gl_FragColor = vec4(0.0f);
    for(uint step = 0; step < numberOfSteps && gl_FragColor.a < 0.99f; ++step) {
        const vec3 currentPoint = origin + STEP_LENGTH * step * direction;
        vec3 coordinate = scaleAndBias(currentPoint);
        vec4 currentSample = textureLod(m_texture3D, scaleAndBias(currentPoint), mipmapLevel);
        gl_FragColor += (1.0f - gl_FragColor.a) * currentSample;
    }
    gl_FragColor.rgb = pow(gl_FragColor.rgb, vec3(1.0 / 2.2));
    gl_FragColor = vec4(1.0);
}