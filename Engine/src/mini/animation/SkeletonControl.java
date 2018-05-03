package mini.animation;

import mini.material.MatParam;
import mini.material.Material;
import mini.math.FastMath;
import mini.math.Matrix4f;
import mini.renderer.RenderManager;
import mini.renderer.ViewPort;
import mini.scene.AbstractControl;
import mini.scene.Geometry;
import mini.scene.Mesh;
import mini.scene.Node;
import mini.scene.Spatial;
import mini.scene.VertexBuffer;
import mini.shaders.VarType;
import mini.utils.TempVars;
import mini.utils.clone.Cloner;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SkeletonControl extends AbstractControl {
    private Skeleton skeleton;

    /**
     * List of geometries affected by this control
     */
    private List<Geometry> targets = new ArrayList<>();

    /**
     * Unique material references used for hardware skinning
     */
    private Set<Material> materials = new HashSet<>();
    private boolean meshNeedsUpdate;
    private boolean hwSkinningDesired = true;
    private boolean hwSkinningTested;
    private boolean hwSkinningSupported;
    private boolean hwSkinningEnabled;

    public SkeletonControl(Skeleton skeleton) {
        this.skeleton = skeleton;
        if (skeleton == null) {
            throw new IllegalArgumentException("skeleton cannot be null");
        }
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        updateTargetsAndMaterials();
    }

    @Override
    protected void controlRender(RenderManager renderManager, ViewPort vp) {
        if (!meshNeedsUpdate) {
            return;
        }

        updateTargetsAndMaterials();

        if (hwSkinningDesired && !hwSkinningTested) {
            hwSkinningTested = true;
            hwSkinningSupported = testHardwareSupport(renderManager);

            if (hwSkinningSupported) {
                hwSkinningEnabled = true;
            } else {
                switchToSoftwareSkinning();
            }
        } else if (hwSkinningDesired && hwSkinningSupported && !hwSkinningEnabled) {
            switchToHardwareSkinning();
            hwSkinningEnabled = true;
        } else if (!hwSkinningSupported && hwSkinningEnabled) {
            switchToSoftwareSkinning();
            hwSkinningEnabled = false;
        }

        if (hwSkinningEnabled) {
            controlRenderHardware();
        } else {
            controlRenderSoftware();
        }
        meshNeedsUpdate = false;
    }

    @Override
    protected void controlUpdate(float tpf) {
        meshNeedsUpdate = true;
    }

    private void controlRenderSoftware() {
        resetToBindPose();

        Matrix4f[] offsetMatrices = skeleton.computeSkinningMatrices();

        for (Geometry geom : targets) {
            var mesh = geom.getMesh();

            softwareSkinUpdate(mesh, offsetMatrices);
        }
    }

    private void resetToBindPose() {
        for (Geometry target : targets) {
            var mesh = target.getMesh();
            if (mesh == null || !mesh.isAnimated()) {
                continue;
            }

            var boneWeightBuffer = mesh.getBuffer(VertexBuffer.Type.BoneWeight).getData();
            var boneIndexBuffer = mesh.getBuffer(VertexBuffer.Type.BoneIndex).getData();

            if (!boneWeightBuffer.hasArray() || !boneIndexBuffer.hasArray()) {
                mesh.prepareForAnim(true);
            }

            VertexBuffer positionsBP = mesh.getBuffer(VertexBuffer.Type.BindPosePosition);
            VertexBuffer normalsBP = mesh.getBuffer(VertexBuffer.Type.BindPoseNormal);
            VertexBuffer positions = mesh.getBuffer(VertexBuffer.Type.Position);
            VertexBuffer normals = mesh.getBuffer(VertexBuffer.Type.Normal);

            var bpb = (FloatBuffer) positionsBP.getData();
            var pb = (FloatBuffer) positions.getData();
            var bnb = (FloatBuffer) normalsBP.getData();
            var nb = (FloatBuffer) normals.getData();
            bpb.clear();
            pb.clear();
            bnb.clear();
            nb.clear();

            VertexBuffer tangentsBP = mesh.getBuffer(VertexBuffer.Type.BindPoseTangent);
            if (tangentsBP != null) {
                var tangents = mesh.getBuffer(VertexBuffer.Type.Tangent);
                var tb = (FloatBuffer) tangents.getData();
                var btb = (FloatBuffer) tangentsBP.getData();
                tb.clear();
                btb.clear();
                tb.put(btb).clear();
            }

            pb.put(bpb).clear();
            nb.put(bnb).clear();
        }
    }

    private void softwareSkinUpdate(Mesh mesh, Matrix4f[] offsetMatrices) {
        var tangentBuffer = mesh.getBuffer(VertexBuffer.Type.Tangent);
        if (tangentBuffer == null) {
            applySkinning(mesh, offsetMatrices);
        } else {
            throw new UnsupportedOperationException();
            //applySkinningWithTangents(mesh, offsetMatrices, tangentBuffer);
        }
    }

    private void applySkinning(Mesh mesh, Matrix4f[] offsetMatrices) {
        var maxNumWeightsPerVert = mesh.getMaxNumWeights();
        if (maxNumWeightsPerVert <= 0) {
            throw new IllegalStateException("Max weights per vertex is incorrectly set!");
        }

        var fourMinusMaxWeights = 4 - maxNumWeightsPerVert;

        // NOTE: This code assumes the vertex buffer is in bind pose. Call resetToBindPose()
        var positionBuffer = mesh.getBuffer(VertexBuffer.Type.Position);
        var floatPositionBuffer = (FloatBuffer) positionBuffer.getData();
        floatPositionBuffer.rewind();

        var normalBuffer = mesh.getBuffer(VertexBuffer.Type.Normal);
        var floatNormalBuffer = (FloatBuffer) normalBuffer.getData();
        floatNormalBuffer.rewind();

        var indexBuffer = (ByteBuffer) mesh.getBuffer(VertexBuffer.Type.BoneIndex).getData();
        var weightBuffer = (FloatBuffer) mesh.getBuffer(VertexBuffer.Type.BoneWeight).getData();

        indexBuffer.rewind();
        weightBuffer.rewind();

        var weights = weightBuffer.array();
        var indices = indexBuffer.array();
        int weightIndex = 0;

        try (var vars = TempVars.get()) {
            var skinPosBuffer = vars.skinPositions;
            var skinNormalBuffer = vars.skinNormals;

            int iterations = (int) FastMath
                    .ceil(floatPositionBuffer.limit() / ((float) skinPosBuffer.length));
            int bufferLength;
            for (int i = iterations - 1; i >= 0; i--) {
                // Read next set of positions and normals from native buffer
                bufferLength = Math.min(skinPosBuffer.length, floatPositionBuffer.remaining());
                floatPositionBuffer.get(skinPosBuffer, 0, bufferLength);
                floatNormalBuffer.get(skinNormalBuffer, 0, bufferLength);
                int numVertices = bufferLength / 3;
                var positionIndex = 0;

                for (int vertex = numVertices - 1; vertex >= 0; vertex--) {
                    if (weights[weightIndex] == 0) {
                        positionIndex += 3;
                        weightIndex += 4;
                        continue;
                    }

                    var normx = skinNormalBuffer[positionIndex];
                    var posx = skinPosBuffer[positionIndex++];
                    var normy = skinNormalBuffer[positionIndex];
                    var posy = skinPosBuffer[positionIndex++];
                    var normz = skinNormalBuffer[positionIndex];
                    var posz = skinPosBuffer[positionIndex++];

                    float rx = 0, ry = 0, rz = 0, rnx = 0, rny = 0, rnz = 0;

                    for (int w = maxNumWeightsPerVert - 1; w >= 0; w--) {
                        var weight = weights[weightIndex];
                        var mat = offsetMatrices[indices[weightIndex++] & 0xff];

                        rx += (mat.m00 * posx + mat.m01 * posy + mat.m02 * posz + mat.m03) * weight;
                        ry += (mat.m10 * posx + mat.m11 * posy + mat.m12 * posz + mat.m13) * weight;
                        rz += (mat.m20 * posx + mat.m21 * posy + mat.m22 * posz + mat.m23) * weight;

                        rnx += (normx * mat.m00 + normy * mat.m01 + normz * mat.m02) * weight;
                        rny += (normx * mat.m10 + normy * mat.m11 + normz * mat.m12) * weight;
                        rnz += (normx * mat.m20 + normy * mat.m21 + normz * mat.m22) * weight;
                    }

                    weightIndex += fourMinusMaxWeights;

                    positionIndex -= 3;
                    skinNormalBuffer[positionIndex] = rnx;
                    skinPosBuffer[positionIndex++] = rx;
                    skinNormalBuffer[positionIndex] = rny;
                    skinPosBuffer[positionIndex++] = ry;
                    skinNormalBuffer[positionIndex] = rnz;
                    skinPosBuffer[positionIndex++] = rz;
                }

                floatPositionBuffer.position(floatPositionBuffer.position() - bufferLength);
                floatPositionBuffer.put(skinPosBuffer, 0, bufferLength);
                floatNormalBuffer.position(floatNormalBuffer.position() - bufferLength);
                floatNormalBuffer.put(skinNormalBuffer, 0, bufferLength);
            }
        }

        // TODO: Would it be better to retrieve these again?
        positionBuffer.updateData(floatPositionBuffer);
        normalBuffer.updateData(floatNormalBuffer);
    }

    private void controlRenderHardware() {
        Matrix4f[] offsetMatrices = skeleton.computeSkinningMatrices();

        for (Material material : materials) {
            MatParam boneMatrix = material.getParam("BoneMatrices");
            if (boneMatrix != null) {
                if (boneMatrix.getValue() != offsetMatrices) { // reference checking
                    System.err.println("Error: Material instances cannot be shared when hardware"
                                       + "skinning is used. Ensure all models are using unique"
                                       + "material instances.");
                }
            }
            material.setParam("BoneMatrices", VarType.Matrix4fArray, offsetMatrices);
        }
    }

    /**
     * Some of the animated meshes do not support hardware skinning, so it is not support by the
     * model.
     */
    private boolean testHardwareSupport(RenderManager renderManager) {
        for (Material material : materials) {
            if (material.getMaterialDef().getMaterialParam("NumberOfBones") == null) {
                System.err.println("Warning: Not using hardware skinning for" + spatial +
                                   "because material " + material.getMaterialDef().getName() +
                                   " does not support it");
                return false;
            }
        }

        switchToHardwareSkinning();

        renderManager.preloadScene(spatial);
        return true;
    }

    private void switchToHardwareSkinning() {
        // Take the next full 10 bones
        int numBones = ((skeleton.getBoneCount() / 10) + 1) * 10;
        for (Material material : materials) {
            material.setInt("NumberOfBones", numBones);
        }

        for (Geometry geom : targets) {
            var mesh = geom.getMesh();
            if (mesh != null && mesh.isAnimated()) {
                mesh.prepareForAnim(false);
            }
        }
    }

    private void switchToSoftwareSkinning() {
        for (Material material : materials) {
            if (material.getParam("NumberOfBones") != null) {
                material.clearParam("NumberOfBones");
            }
        }

        for (Geometry geom : targets) {
            var mesh = geom.getMesh();
            if (mesh != null && mesh.isAnimated()) {
                mesh.prepareForAnim(true);
            }
        }
    }

    private void updateTargetsAndMaterials() {
        targets.clear();
        materials.clear();

        if (spatial instanceof Node) {
            findTargets((Node) spatial);
        } else if (spatial instanceof Geometry) {
            findTargets((Geometry) spatial);
        }
    }

    private void findTargets(Geometry geom) {
        var mesh = geom.getMesh();
        if (mesh != null && mesh.isAnimated()) {
            targets.add(geom);
            materials.add(geom.getMaterial());
        }
    }

    private void findTargets(Node node) {
        for (Spatial child : node.getChildren()) {
            if (child instanceof Geometry) {
                findTargets((Geometry) child);
            } else if (child instanceof Node) {
                findTargets((Node) child);
            }
        }
    }

    /**
     * If it is preferred and support by GPU, it should be enabled.
     * If it is not preferred, or not supported by GPU, then it should be disabled.
     *
     * @param preferred
     */
    public void setHardwareSkinningPreferred(boolean preferred) {
        hwSkinningDesired = preferred;
    }

    public Skeleton getSkeleton() {
        return skeleton;
    }

    @Override
    public void cloneFields(Cloner cloner, Object original) {
        super.cloneFields(cloner, original);

        throw new UnsupportedOperationException();
    }

}
