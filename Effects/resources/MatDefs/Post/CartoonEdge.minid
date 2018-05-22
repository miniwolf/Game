MaterialDef Cartoon Edge {
    MaterialParameters {
        Int NumSamples
        Int NumSamplesDepth
        Texture2D Texture
        Texture2D NormalsTexture
        Texture2D DepthTexture
        Color EdgeColor
        Float EdgeWidth
        Float EdgeIntensity
        Float NormalThreshold
        Float DepthThreshold
        Float NormalSensitivity
        Float DepthSensitivity
    }

    Technique {
        VertexShader GLSL330: MatDefs/Post/Post.vert
        FragmentShader GLSL330: MatDefs/Post/CartoonEdge.frag

        WorldParameters {
            WorldViewMatrix
            ResolutionInverse
        }

        Defines {
            RESOLVE_MS : NumSamples
            RESOLVE_DEPTH_MS : NumSamplesDepth
        }
    }
}