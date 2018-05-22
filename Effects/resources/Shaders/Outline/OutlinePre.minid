MaterialDef Edge {
    MaterialParameters {
        Texture2D Texture
    }

    Technique {
        Vertex GLSL150: MatDefs/Post/Post.vert
        Vertex GLSL150: Shaders/Outline/OutlinePre.frag
    }
}