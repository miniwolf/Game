MaterialDef World Pos {

    Technique {
        VertexShader GLSL450 GLSL450:   MatDefs/Voxelization/Visualization/WorldPosShader.vert
        FragmentShader GLSL450 GLSL450: MatDefs/Voxelization/Visualization/WorldPosShader.frag

        WorldParameters {
            WorldViewProjectionMatrix
        }
    }
}