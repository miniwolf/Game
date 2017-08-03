MaterialDef Unshaded {

    MaterialParameters {
        Texture2D ColorMap
        Texture2D LightMap
        Color Color (Color)
        Boolean VertexColor (UseVertexColor)
        Float PointSize : 1.0
        Boolean SeparateTexCoord

        // Texture of the glowing parts of the material
        Texture2D GlowMap
        // The glow color of the object
        Color GlowColor

        // For instancing
        Boolean UseInstancing

        // For hardware skinning
        Int NumberOfBones
        Matrix4fArray BoneMatrices

        // Alpha threshold for fragment discarding
        Float AlphaDiscardThreshold (AlphaTestFallOff)

        //Shadows
        Int FilterMode
        Boolean HardwareShadows

        Texture2D ShadowMap0
        Texture2D ShadowMap1
        Texture2D ShadowMap2
        Texture2D ShadowMap3
        //pointLights
        Texture2D ShadowMap4
        Texture2D ShadowMap5

        Float ShadowIntensity
        Vector4f Splits
        Vector2f FadeInfo

        Matrix4f LightViewProjectionMatrix0
        Matrix4f LightViewProjectionMatrix1
        Matrix4f LightViewProjectionMatrix2
        Matrix4f LightViewProjectionMatrix3
        //pointLight
        Matrix4f LightViewProjectionMatrix4
        Matrix4f LightViewProjectionMatrix5
        Vector3f LightPos
        Vector3f LightDir

        Float PCFEdge

        Float ShadowMapSize

        Boolean BackfaceShadows: true
    }

    Technique {
        VertexShader GLSL100 GLSL150:   Engine/MatDefs/Misc/Unshaded.vert
        FragmentShader GLSL100 GLSL150: Engine/MatDefs/Misc/Unshaded.frag

        WorldParameters {
            WorldViewProjectionMatrix
            ViewProjectionMatrix
            ViewMatrix
        }

        Defines {
            INSTANCING : UseInstancing
            SEPARATE_TEXCOORD : SeparateTexCoord
            HAS_COLORMAP : ColorMap
            HAS_LIGHTMAP : LightMap
            HAS_VERTEXCOLOR : VertexColor
            HAS_POINTSIZE : PointSize
            HAS_COLOR : Color
            NUM_BONES : NumberOfBones
            DISCARD_ALPHA : AlphaDiscardThreshold
        }
    }
}