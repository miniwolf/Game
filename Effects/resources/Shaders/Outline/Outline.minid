MaterialDef Edge {
    MaterialParameters {
        Texture2D Texture
        Texture2D OutlineDepthTexture
        Texture2D DepthTexture
        Vector2f Resolution
        Color OutlineColor
        Float OutlineWidth
    }

    Technique {
        VertexShader GLSL150:   MatDefs/Post/Post.vert
        FragmentShader GLSL100: Shaders/Outline/Outline.frag

        WorldParameters {
        }
    }
}