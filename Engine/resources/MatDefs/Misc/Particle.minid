MaterialDef Point Sprite {

    MaterialParameters {
        Texture2D Texture
        Float Quadratic
        Boolean PointSprite

        //only used for soft particles
        Texture2D DepthTexture
        Float Softness
        Int NumSamplesDepth

        // Texture of the glowing parts of the material
        Texture2D GlowMap
        // The glow color of the object
        Color GlowColor
    }

    Technique {

        // The GLSL100 technique is used in two cases:
        // - When the driver doesn't support GLSL 1.2
        // - When running on OpenGL ES 2.0
        // Point sprite should be used if running on ES2, but crash
        // if on desktop (because its not supported by HW)

        VertexShader   GLSL100 GLSL100 GLSL150 : MatDefs/Misc/Particle.vert
        FragmentShader GLSL100 GLSL120 GLSL150 : MatDefs/Misc/Particle.frag

        WorldParameters {
            WorldViewProjectionMatrix
            WorldViewMatrix
            WorldMatrix
            CameraPosition
        }

        RenderState {
            Blend AlphaAdditive
            DepthWrite Off
            PointSprite On
        }

        Defines {
            USE_TEXTURE : Texture
            POINT_SPRITE : PointSprite
        }
    }

    Technique PreShadow {

        VertexShader GLSL100 GLSL150 :   MatDefs/Shadow/PreShadow.vert
        FragmentShader GLSL100 GLSL150 : MatDefs/Shadow/PreShadow.frag

        WorldParameters {
            WorldViewProjectionMatrix
            WorldViewMatrix
            ViewProjectionMatrix
            ViewMatrix
        }

        Defines {
            COLOR_MAP : Texture
        }

        ForcedRenderState {
            FaceCull Off
            DepthTest On
            DepthWrite On
            PolyOffset 5 3
            ColorWrite Off
        }

    }

    Technique SoftParticles{

        VertexShader  GLSL100 GLSL150 : MatDefs/Misc/SoftParticle.vert
        FragmentShader GLSL120 GLSL150 : MatDefs/Misc/SoftParticle.frag

        WorldParameters {
            WorldViewProjectionMatrix
            WorldViewMatrix
            WorldMatrix
            CameraPosition
        }

        RenderState {
            Blend AlphaAdditive
            DepthWrite Off
            PointSprite On
        }

        Defines {
            USE_TEXTURE : Texture
            POINT_SPRITE : PointSprite
            RESOLVE_DEPTH_MS : NumSamplesDepth
        }
    }

   Technique Glow {

        VertexShader GLSL100 GLSL150:   MatDefs/Misc/Unshaded.vert
        FragmentShader GLSL100 GLSL150: MatDefs/Light/Glow.frag

        WorldParameters {
            WorldViewProjectionMatrix
        }

        Defines {
            NEED_TEXCOORD1
            HAS_GLOWMAP : GlowMap
            HAS_GLOWCOLOR : GlowColor
        }

        RenderState {
            PointSprite On
            Blend AlphaAdditive
            DepthWrite Off
        }
    }
}