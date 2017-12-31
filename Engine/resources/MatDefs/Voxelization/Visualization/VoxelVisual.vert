in vec3 inPosition;

out vec2 texCoord;

// Scales and bias a given vector (i.e. from [-1, 1] to [0, 1]).
vec2 scaleAndBias(vec2 p) { return 0.5f * p + vec2(0.5f); }

void main(){
    texCoord = scaleAndBias(inPosition.xy);
    gl_Position = vec4(inPosition, 1);
}