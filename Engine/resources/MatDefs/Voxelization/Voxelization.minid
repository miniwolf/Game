MaterialDef Voxel Visual {
    MaterialParameters {
    }

    Technique {
        VertexShader GLSL450 GLSL450:   MatDefs/Voxelization/Voxelization.vert
        GeometryShader GLSL330 GLSL330: MatDefs/Voxelization/Voxelization.geom
        FragmentShader GLSL450 GLSL450: MatDefs/Voxelization/Voxelization.frag

        WorldParameters {
            WorldViewProjectionMatrix
            ViewProjectionMatrix
            ViewMatrix
        }
    }
}