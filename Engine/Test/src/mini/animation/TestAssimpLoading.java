package mini.animation;

import assimp.AiMaterial;
import assimp.AiMesh;
import assimp.AiNode;
import assimp.AiScene;
import assimp.Importer;
import assimp.postProcess.TriangulateProcess;
import glm_.mat4x4.Mat4;
import glm_.vec3.Vec3;
import mini.app.SimpleApplication;
import mini.asset.TextureKey;
import mini.input.KeyboardKey;
import mini.input.controls.ActionListener;
import mini.input.controls.KeyTrigger;
import mini.light.AmbientLight;
import mini.light.DirectionalLight;
import mini.material.Material;
import mini.material.RenderState;
import mini.math.ColorRGBA;
import mini.math.Matrix4f;
import mini.math.Transform;
import mini.math.Vector3f;
import mini.post.FilterPostProcessor;
import mini.post.filters.CartoonEdgeFilter;
import mini.scene.Geometry;
import mini.scene.Mesh;
import mini.scene.Node;
import mini.scene.Spatial;
import mini.scene.VertexBuffer;
import mini.scene.mesh.IndexBuffer;
import mini.scene.mesh.IndexIntBuffer;
import mini.scene.mesh.IndexShortBuffer;
import mini.textures.Image;
import mini.textures.Texture;
import mini.textures.Texture2D;
import mini.utils.BufferUtils;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class TestAssimpLoading extends SimpleApplication implements ActionListener {

    public static void main(String[] args) {
        var app = new TestAssimpLoading();
        app.start();
    }

    AiScene scene;
    Spatial bumpy;

    @Override
    public void simpleInitApp() {
        setupLights();
        setupScene();
        setupKey();
        //setupFilters();
        //makeToonish(bumpy);
    }

    private void setupFilters() {
        FilterPostProcessor filterPostProcessor = new FilterPostProcessor(assetManager);
        int numSamples = getContext().getSettings().getSamples();
        if (numSamples > 0) {
            filterPostProcessor.setNumSamples(numSamples);
        }

        CartoonEdgeFilter toon = new CartoonEdgeFilter();
        toon.setEdgeColor(ColorRGBA.Yellow);
        filterPostProcessor.addFilter(toon);
        viewPort.addProcessor(filterPostProcessor);
    }

    private void makeToonish(Spatial child) {
        if (child instanceof Geometry) {
            Geometry geom = (Geometry) child;
            Material material = geom.getMaterial();
            if (material.getMaterialDef().getMaterialParam("UseMaterialColors") != null) {
                Texture texture = assetManager.loadTexture("Textures/ColorRamp/toon.png");
                material.setTexture("ColorRamp", texture);
                material.setBoolean("UseMaterialColors", true);
                material.setColor("Specular", ColorRGBA.Black);
                material.setColor("Diffuse", ColorRGBA.White);
                material.setBoolean("VertexLighting", true);
            }
        } else if (child instanceof Node) {
            Node node = (Node) child;
            for (Spatial spatial : node.getChildren()) {
                makeToonish(spatial);
            }
        }
    }

    private void setupScene() {
        File file = new File(
                //"C:/Users/miniwolf/Engine/Engine/Test/resources/Models/car/L200-FBX/L200-FBX.fbx" unsupported
                //"C:/Users/miniwolf/Engine/Engine/Test/resources/Models/chest/Models/chest.fbx"
                "C:/Users/miniwolf/Engine/Engine/Test/resources/Models/animation/demo/DefaultAvatar.fbx"
        );
        Spatial spatial = assetManager.loadModel("Models/animation/demo/animation/06_13_conv.fbx");
        File fileWithAnimation = new File(
                "C:/Users/miniwolf/Engine/Engine/Test/resources/Models/animation/demo/animation/06_13_conv.fbx");
        rootNode.setCullHint(Spatial.CullHint.Never);
        AiScene sceneWithAnimation;
        if (file.exists()) {
            scene = new Importer().readFile(file.getAbsolutePath());
            sceneWithAnimation = new Importer().readFile(fileWithAnimation.getAbsolutePath());
        } else {
            return;
        }
        assert scene != null;
        bumpy = constructSceneGraph(scene);
        bumpy.setLocalScale(0.01f);
        rootNode.attachChild(bumpy);
        var animations = sceneWithAnimation.getAnimations();
        animations.size();
    }

    private void setupLights() {
        AmbientLight al = new AmbientLight();
        rootNode.addLight(al);

        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(Vector3f.UNIT_XYZ.negate());
        rootNode.addLight(dl);
    }

    private void setupKey() {
        inputManager.addMapping("GotoBumpy", new KeyTrigger(KeyboardKey.KEY_F));
        inputManager.addMapping("GotoTeapot", new KeyTrigger(KeyboardKey.KEY_T));
        inputManager.addListener(this, "GotoBumpy");
        inputManager.addListener(this, "GotoTeapot");
    }

    public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("GotoBumpy")) {
            flyCam.jumpTo(bumpy.getLocalTranslation());
        }
    }

    private Node constructSceneGraph(AiScene scene) {
        AiNode rootNode = scene.getRootNode();

        return createScene(rootNode);
    }

    private Node createScene(AiNode rootNode) {
        Node node = toImplObject(rootNode);
        attachChildren(node, rootNode);
        if (rootNode.getNumMeshes() > 0) {
            List<AiMesh> meshes = Arrays.stream(rootNode.getMeshes())
                                        .mapToObj(index -> scene.getMeshes().get(index)).collect(
                            Collectors.toList());
            attachMesh(node, rootNode.getName(), meshes);
        }
        return node;
    }

    private void attachChildren(Node node, AiNode rootNode) {
        for (AiNode child : rootNode.getChildren()) {
            Node childNode = createScene(child);
            node.attachChild(childNode);
        }
    }

    private void attachMesh(Node node, String name, List<AiMesh> meshes) {
        for (AiMesh mesh : meshes) {
            Mesh miniMesh = new Mesh();
            new TriangulateProcess().triangulateMesh(mesh);

            FloatBuffer posBuffer = null;
            FloatBuffer normBuffer = null;
            FloatBuffer uv0Buffer = null;
            ByteBuffer colBuffer = null;
            IndexBuffer indexBuffer;

            if (mesh.getNumVertices() > 0) {
                posBuffer = BufferUtils.createVector3Buffer(mesh.getNumVertices());
                miniMesh.setBuffer(VertexBuffer.Type.Position, 3, posBuffer);
            }
            if (mesh.getHasNormals()) {
                normBuffer = BufferUtils.createVector3Buffer(mesh.getNumVertices());
                miniMesh.setBuffer(VertexBuffer.Type.Normal, 3, normBuffer);
            }
            if (mesh.hasTextureCoords(0)) {
                uv0Buffer = BufferUtils.createVector2Buffer(mesh.getNumVertices());
                miniMesh.setBuffer(VertexBuffer.Type.TexCoord, 2, uv0Buffer);
            }
            if (mesh.hasVertexColors(0)) {
                colBuffer = BufferUtils.createByteBuffer(mesh.getNumVertices() * 4);
                miniMesh.setBuffer(VertexBuffer.Type.Color, 4, colBuffer);
                miniMesh.getBuffer(VertexBuffer.Type.Color).setNormalized(true);
            }

            if (mesh.getNumVertices() >= 65536) {
                IntBuffer ib = BufferUtils.createIntBuffer(mesh.getFaces().size() * 3);
                miniMesh.setBuffer(VertexBuffer.Type.Index, 3, ib);
                indexBuffer = new IndexIntBuffer(ib);
            } else {
                ShortBuffer sb = BufferUtils.createShortBuffer(mesh.getFaces().size() * 3);
                miniMesh.setBuffer(VertexBuffer.Type.Index, 3, sb);
                indexBuffer = new IndexShortBuffer(sb);
            }

            miniMesh.setStatic();

            for (int i = 0; i < mesh.getNumVertices(); i++) {
                if (posBuffer != null) {
                    var pos = mesh.getVertices().get(i);
                    posBuffer.put(pos.x).put(pos.y).put(pos.z);
                }
                if (normBuffer != null) {
                    var norm = mesh.getNormals().get(i);
                    normBuffer.put(norm.x).put(norm.y).put(norm.z);
                }
                if (uv0Buffer != null) {
                    var uv0 = mesh.getTextureCoords().get(0).get(i);
                    uv0Buffer.put(uv0[0]).put(uv0[1]);
                }
                if (colBuffer != null) {
                    var color = mesh.getColors().get(0).get(i);
                    colBuffer.putInt(new ColorRGBA(color.x, color.y, color.z, color.z).asIntABGR());
                }
            }

            var i = 0;
            for (List<Integer> face : mesh.getFaces()) {
                indexBuffer.put(i++, face.get(0));
                indexBuffer.put(i++, face.get(1));
                indexBuffer.put(i++, face.get(2));
            }

            miniMesh.updateCounts();
            miniMesh.updateBound();

            Geometry geometry = new Geometry(name + "-mesh", miniMesh);
            var mat = toImplObject(scene.getMaterials().get(mesh.getMaterialIndex()));
            geometry.setMaterial(mat);
            node.attachChild(geometry);
        }
    }

    private Material toImplObject(AiMaterial aiMaterial) {
        var mat = new Material(assetManager, "MatDefs/Light/Lighting.minid");
        mat.setName(aiMaterial.getName());
        mat.setReceivesShadows(true);
        mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

        AtomicReference<Texture> diffuseMap = new AtomicReference<>();
        AtomicReference<Texture> specularMap = new AtomicReference<>();
        AtomicReference<Texture> ambientMap = new AtomicReference<>();
        AtomicReference<Texture> normalMap = new AtomicReference<>();
        Vec3 diffuse = null;
        Vec3 ambient = null;
        Vec3 specular = null;
        var textures = aiMaterial.getTextures();
        textures.forEach(texture -> {
            switch (Objects.requireNonNull(texture.getType())) {
                case diffuse:
                    diffuseMap.set(toImplObject(texture));
                    break;
                case ambient:
                    ambientMap.set(toImplObject(texture));
                    break;
                case specular:
                    specularMap.set(toImplObject(texture));
                    break;
                case normals:
                    normalMap.set(toImplObject(texture));
                    break;
                case none:
                    break;
            }
        });
        AiMaterial.Color color = aiMaterial.getColor();
        if (color == null) {
            throw new IllegalArgumentException("Color cannot be null on texture");
        }
        if (specularMap.get() == null) {
            diffuse = color.getDiffuse();
        }
        if (ambientMap.get() == null) {
            ambient = color.getAmbient();
        }
        if (specularMap.get() == null) {
            specular = color.getSpecular();
        }
        if (diffuse != null || ambient != null || specular != null) {
            mat.setBoolean("UseMaterialColors", true);
            mat.setColor("Diffuse",
                         diffuse != null ? new ColorRGBA(diffuse.x, diffuse.y, diffuse.z, 1.0f) :
                         ColorRGBA.White);
            mat.setColor("Specular", specular != null ?
                                     new ColorRGBA(specular.x, specular.y, specular.z, 1.0f) :
                                     ColorRGBA.Black);
            mat.setColor("Ambient",
                         ambient != null ? new ColorRGBA(ambient.x, ambient.y, ambient.z, 1.0f) :
                         ColorRGBA.White);
        }

        if (color.getEmissive() != null) {
            var emissive = color.getEmissive();
            mat.setColor("GlowColor", new ColorRGBA(emissive.x, emissive.y, emissive.z, 1.0f));
        }

        if (aiMaterial.getShininess() > 1f) {
            // Convert shininess from Phong (FBX Shading model) to Blinn (Our shading model)
            float blinnShininess = (aiMaterial.getShininess() * 5.1f) + 1f;
            mat.setFloat("Shininess", blinnShininess);
        }

        if (diffuseMap.get() != null) {
            mat.setTexture("DiffuseMap", diffuseMap.get());
        }
        if (specularMap.get() != null) {
            mat.setTexture("SpecularMap", specularMap.get());
        }
        if (normalMap.get() != null) {
            mat.setTexture("NormalMap", normalMap.get());
        }

        return mat;

    }

    private Texture toImplObject(AiMaterial.Texture texture) {
        String fileName = texture.getFile();
        if (fileName == null) {
            System.err.println("Cannot locate image for texture: " + texture.toString());
            return null;
        }
        TextureKey key = new TextureKey(
                "Models/animation/demo/" + fileName); // TODO: use key.getFolder from AssetKey
        key.setGenerateMips(true);
        Image image = assetManager.loadTexture(key).getImage();

        Texture2D tex = new Texture2D(image);
        tex.setName(fileName);
        tex.setMinFilter(Texture.MinFilter.Trilinear);
        tex.setMagFilter(Texture.MagFilter.Bilinear);
        if (texture.getMapModeU() == null) {
            tex.setWrap(Texture.WrapAxis.S, Texture.WrapMode.Repeat);
        }
        if (texture.getMapModeV() == null) {
            tex.setWrap(Texture.WrapAxis.T, Texture.WrapMode.Repeat);
        }
        return tex;
    }

    private Node toImplObject(AiNode rootNode) {
        Node node = new Node(rootNode.getName() + "-node");
        node.setLocalTransform(convertTransform(rootNode.getTransformation()));
        return node;
    }

    private Transform convertTransform(Mat4 t) {
        var transform = new Transform();
        var m = new Matrix4f(
                t.v00(), t.v01(), t.v02(), t.v03(),
                t.v10(), t.v11(), t.v12(), t.v13(),
                t.v20(), t.v21(), t.v22(), t.v23(),
                t.v30(), t.v31(), t.v32(), t.v33());
        transform.fromTransformMatrix(m);
        return transform;
    }
}
