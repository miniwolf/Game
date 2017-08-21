MaterialDef Default GUI {

    MaterialParameters {
        Texture2D Texture
        Color Color (Color)
        Boolean VertexColor (UseVertexColor)
    }

    Technique {
        VertexShader GLSL150:   MatDefs/Gui/Gui.vert
        FragmentShader GLSL150: MatDefs/Gui/Gui.frag

        WorldParameters {
            WorldViewProjectionMatrix
        }

        Defines {
            TEXTURE : Texture
            VERTEX_COLOR : VertexColor
        }
    }

    Technique {
        VertexShader GLSL100:   MatDefs/Gui/Gui.vert
        FragmentShader GLSL100: MatDefs/Gui/Gui.frag

        WorldParameters {
            WorldViewProjectionMatrix
        }

        Defines {
            TEXTURE : Texture
            VERTEX_COLOR : VertexColor
        }
    }

}