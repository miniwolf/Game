MaterialDef Debug Normals {
    MaterialParameters {
        // For instancing
        Boolean UseInstancing
    }

    Technique {
        VertexShader GLSL330 GLSL330:   MatDefs/Misc/ShowNormals.vert
        FragmentShader GLSL330 GLSL330: MatDefs/Misc/ShowNormals.frag

        WorldParameters {
            WorldViewProjectionMatrix
            ViewProjectionMatrix
            ViewMatrix
            ProjectionMatrix
        }

        Defines {
            INSTANCING : UseInstancing
        }
    }
}