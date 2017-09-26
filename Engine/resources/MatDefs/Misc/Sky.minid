MaterialDef Sky Plane {
    MaterialParameters {
        TextureCubeMap Texture
        Boolean SphereMap
        Boolean EquirectMap
        Vector3f NormalScale
    }
    Technique {
        VertexShader GLSL100 GLSL150:   MatDefs/Misc/Sky.vert
        FragmentShader GLSL100 GLSL150: MatDefs/Misc/Sky.frag

        WorldParameters {
            ViewMatrix
            ProjectionMatrix
            WorldMatrix
        }

        Defines {
            SPHERE_MAP : SphereMap
            EQUIRECT_MAP : EquirectMap
        }

        RenderState {
            DepthWrite Off
            DepthFunc Equal
        }
    }
}