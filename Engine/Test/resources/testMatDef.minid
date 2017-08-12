MaterialDef Simple {
    MaterialParameters {
        Color Color
    }
    Technique {
        WorldParameters {
            WorldViewProjectionMatrix
        }
        VertexShaderNodes {
            ShaderNode CommonVert {
                Definition : CommonVert : MatDefs/ShaderNodes/Common/CommonVert.j3sn
                InputMappings {
                    worldViewProjectionMatrix = WorldParam.WorldViewProjectionMatrix
                    modelPosition = Global.position.xyz
                }
                OutputMappings {
                    Global.position = projPosition
                }
            }
        }
        FragmentShaderNodes {
            ShaderNode ColorMult {
                Definition : ColorMult : MatDefs/ShaderNodes/Basic/ColorMult.j3sn
                InputMappings {
                    color1 = MatParam.Color
                    color2 = Global.color
                }
                OutputMappings {
                    Global.color = outColor
                }
            }
        }
    }
}