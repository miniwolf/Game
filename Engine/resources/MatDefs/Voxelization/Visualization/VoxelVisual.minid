MaterialDef Voxel Visual {
    MaterialParameters {
        Texture2D textureBack
        Texture2D textureFront
        Texture3D texture3D
    }

    Technique {
        VertexShader GLSL450 GLSL450:   MatDefs/Voxelization/Visualization/VoxelVisual.vert
        FragmentShader GLSL450 GLSL450: MatDefs/Voxelization/Visualization/VoxelVisual.frag

        WorldParameters {
            WorldViewProjectionMatrix
        }
    }
}