MaterialDef Phong Lighting {

    MaterialParameters {

        // Compute vertex lighting in the shader
        // For better performance
        Boolean VertexLighting

        // Alpha threshold for fragment discarding
        Float AlphaDiscardThreshold

        // Use the provided ambient, diffuse, and specular colors
        Boolean UseMaterialColors

        // Use vertex color as an additional diffuse color.
        Boolean UseVertexColor

        // Ambient color
        Color Ambient

        // Diffuse color
        Color Diffuse

        // Specular color
        Color Specular

        // Specular power/shininess
        Float Shininess : 1

        // Diffuse map
        Texture2D DiffuseMap

        // Normal map
        Texture2D NormalMap -LINEAR

        // Specular/gloss map
        Texture2D SpecularMap

        // Parallax/height map
        Texture2D ParallaxMap -LINEAR

        //Set to true is parallax map is stored in the alpha channel of the normal map
        Boolean PackedNormalParallax

        //Sets the relief height for parallax mapping
        Float ParallaxHeight : 0.05

        //Set to true to activate Steep Parallax mapping
        Boolean SteepParallax

        // Texture that specifies alpha values
        Texture2D AlphaMap -LINEAR

        // Color ramp, will map diffuse and specular values through it.
        Texture2D ColorRamp

        // Texture of the glowing parts of the material
        Texture2D GlowMap

        // Set to Use Lightmap
        Texture2D LightMap

        // Set to use TexCoord2 for the lightmap sampling
        Boolean SeparateTexCoord

        // The glow color of the object
        Color GlowColor

        // Parameters for fresnel
        // X = bias
        // Y = scale
        // Z = power
        Vector3f FresnelParams

        // Env Map for reflection
        TextureCubeMap EnvMap

        // the env map is a spheremap and not a cube map
        Boolean EnvMapAsSphereMap

        //shadows
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

        // For hardware skinning
        Int NumberOfBones
        Matrix4fArray BoneMatrices

        //For instancing
        Boolean UseInstancing

        Boolean BackfaceShadows : false
    }

    Technique {
        LightMode SinglePass

        VertexShader GLSL100 GLSL150:   MatDefs/Light/SPLighting.vert
        FragmentShader GLSL100 GLSL150: MatDefs/Light/SPLighting.frag

        WorldParameters {
            WorldViewProjectionMatrix
            NormalMatrix
            WorldViewMatrix
            ViewMatrix
            CameraPosition
            WorldMatrix
            ViewProjectionMatrix
        }

        Defines {
            VERTEX_COLOR : UseVertexColor
            VERTEX_LIGHTING : VertexLighting
            MATERIAL_COLORS : UseMaterialColors
            DIFFUSEMAP : DiffuseMap
            NORMALMAP : NormalMap
            SPECULARMAP : SpecularMap
            PARALLAXMAP : ParallaxMap
            NORMALMAP_PARALLAX : PackedNormalParallax
            STEEP_PARALLAX : SteepParallax
            ALPHAMAP : AlphaMap
            COLORRAMP : ColorRamp
            LIGHTMAP : LightMap
            SEPARATE_TEXCOORD : SeparateTexCoord
            DISCARD_ALPHA : AlphaDiscardThreshold
            USE_REFLECTION : EnvMap
            SPHERE_MAP : EnvMapAsSphereMap
            NUM_BONES : NumberOfBones
            INSTANCING : UseInstancing
        }
    }
}
