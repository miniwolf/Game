MaterialDef Post Shadow {
    MaterialParameters {
        Int FilterMode
        Boolean HardwareShadows
        Boolean BackfaceShadows: false

        Texture2D ShadowMap0
        Texture2D ShadowMap1
        Texture2D ShadowMap2
        Texture2D ShadowMap3
        //PointLights
        Texture2D ShadowMap4
        Texture2D ShadowMap5

        Float ShadowIntensity
        Float PCFEdge
        Float ShadowMapSize

        Vector4f Splits

        Matrix4f LightViewProjectionMatrix0
        Matrix4f LightViewProjectionMatrix1
        Matrix4f LightViewProjectionMatrix2
        Matrix4f LightViewProjectionMatrix3
        //PointLight
        Matrix4f LightViewProjectionMatrix4
        Matrix4f LightViewProjectionMatrix5
        Vector3f LightPos
        Vector3f LightDir
    }

    Technique {
        VertexShader GLSL100 GLSL150 : MatDefs/Shadow/PostShadow.vert
        FragmentShader GLSL100 GLSL150 : MatDefs/Shadow/PostShadow.frag

        WorldParameters {
            WorldViewProjectionMatrix
            WorldMatrix
        }

        Defines {
            HARDWARE_SHADOWS : HardwareShadows
            FILTER_MODE : FilterMode
            PCFEDGE : PCFEdge
            SHADOWMAP_SIZE : ShadowMapSize
            FADE : FadeInfo
            PSSM : Splits
            POINTLIGHT : LightViewProjectionMatrix5
            BACKFACE_SHADOWS: BackfaceShadows
        }

        RenderState {
            Blend Modulate
            DepthWrite Off
            PolyOffset -0.1 0
        }
    }
}