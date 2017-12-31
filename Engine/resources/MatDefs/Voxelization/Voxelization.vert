#import "ShaderLib/GLSLCompat.glsllib"
#import "ShaderLib/Instancing.glsllib"

#version 450 core

in vec3 inPosition;
in vec3 inNormal;

out vData {
    vec3 worldPositionGeom;
    vec3 normalGeom;
} vert;


void main(){
    vec4 worldPosition = vec4(position, 1);
    vert.worldPositionGeom = vec3(g_WorldMatrix * worldPosition);
    vert.normalGeom = normalize(mat3(transpose(inverse(g_WorldMatrix))) * inNormal);
    gl_Position = TransformWorldViewProjection(worldPosition);
}