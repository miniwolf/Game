layout(triangles) in;
layout(triangle_strip, max_vertices = 3) out;

in vData {
    vec3 vPos;
    vec2 texCoord;
} verts[];

out fData {
    vec3 vPos;
    vec2 texCoord;
} frag;

void setValues(int i) {
    frag.vPos = verts[i].vPos;
}

void main() {
#ifndef VOXELIZATION
    for (int i = 0; i < 3; i++) {
        setValues();
        gl_Position = gl_in[i].gl_Position;
        EmitVertex();
    }
  EndPrimitive();
#else
    vec3 p1 = vPos[1] - vPos[0];
    vec3 p2 = vPos[2] - vPos[0];
    vec3 p = abs(cross(p1, p2));
    for (int i = 0; i < 3; ++i) {
        setValues();
        if (p.z > p.x && p.z > p.y) {
            gl_Position = vec4(gPos.x, gPos.y, 0, 1);
        } else if (p.x > p.y && p.x > p.z) {
            gl_Position = vec4(gPos.y, gPos.z, 0, 1);
        } else {
            gl_Position = vec4(gPos.x, gPos.z, 0, 1);
        }
        EmitVertex();
    }
    EndPrimitive();
#endif
}