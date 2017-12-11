MaterialDef Pre Shadow {
    Technique {
        VertexShader GLSL100 GLSL150 : MatDefs/Shadow/PreShadow.vert
        FragmentShader GLSL100 GLSL150 : MatDefs/Shadow/PreShadow.frag

        WorldParameters {
            WorldViewProjectionMatrix
            WorldMatrix
        }

        Defines {
            FaceCull Off
            DepthTest On
            DepthWrite On
            PolyOffset 5 3
            ColorWrite Off
        }
    }
}