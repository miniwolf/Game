package mini.animation;

import mini.math.Matrix3f;
import mini.math.Matrix4f;
import mini.math.Quaternion;
import mini.math.Transform;
import mini.math.Vector3f;
import mini.utils.TempVars;
import mini.utils.clone.Cloner;
import mini.utils.clone.MiniCloneable;

import java.util.ArrayList;
import java.util.List;

/**
 * <code>Bone</code> describes a bone in the bone-weight skeletal animation system. A bone contains
 * a name and an index, as well as relevant transformation data.
 * <p>
 * A bone has 3 sets of transforms:
 * 1. The bind transforms, that are the transforms of the bone when the skeleton is in its rest pose
 * (known as bind pose or T pose in literature). The bind transforms are expressed in Local space
 * meaning relative to the parent bone.
 * <p>
 * 2. The local transforms, that are the transforms of the bone once animation or user transforms
 * has been applied to the bind pose. The local transforms are expressed in Local space meaning
 * relative to the parent bone.
 * <p>
 * 3. The model transforms, that are the transforms of the bone relatives to the rootBone of the
 * skeleton. Those transforms are what is needed to apply skinning to the mesh the skeleton
 * controls.
 * Note: there can be several rootBones in a skeleton. The one considered for these transforms is
 * the one that is the ancestor of this bone.
 */
public class Bone implements MiniCloneable {
    private final String name;
    private Bone parent;

    /**
     * Bind transform is the local bind transform of this bone. (local space)
     */
    private Vector3f bindPosition = new Vector3f();
    private Quaternion bindRotation = new Quaternion();
    private Vector3f bindScale = new Vector3f();

    /**
     * The local animated or user transform combined with the local bind transform
     */
    private Vector3f localPosition = new Vector3f();
    private Quaternion localRotation = new Quaternion();
    private Vector3f localScale = new Vector3f();

    /**
     * The model transforms of this bone
     */
    private Vector3f modelPosition = new Vector3f();
    private Quaternion modelRotation = new Quaternion();
    private Vector3f modelScale = new Vector3f();

    /**
     * The inverse bind transforms of this bone expressed in model space
     */
    private Vector3f modelBindInversePosition = new Vector3f();
    private Quaternion modelBindInverseRotation = new Quaternion();
    private Vector3f modelBindInverseScale = new Vector3f();

    private List<Bone> children = new ArrayList<>();

    /**
     * Used to handle blending from one animation to another.
     * See {@link #blendAnimTransforms(mini.math.Vector3f, mini.math.Quaternion, mini.math.Vector3f, float)}
     * on how this variable is used.
     */
    private float currentWeightSum = -1;

    public Bone(String name) {
        this.name = name;
    }

    @Override
    public Object miniClone() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cloneFields(Cloner cloner, Object original) {
        throw new UnsupportedOperationException();
    }

    /**
     * Blends the given animation transform onto the bone's local transform.
     * <p>
     * Subsequent calls of this method stack up, with the final transformation
     * of the bone computed at {@link #updateModelTransforms() } which resets
     * the stack.
     * <p>
     * E.g. a single transform blend with weight = 0.5 followed by an
     * updateModelTransforms() call will result in final transform = transform * 0.5.
     * Two transform blends with weight = 0.5 each will result in the two
     * transforms blended together (nlerp) with blend = 0.5.
     *
     * @param translation The translation to blend in
     * @param rotation    The rotation to blend in
     * @param scale       The scale to blend in
     * @param weight      The weight of the transform to apply. Set to 1.0 to prevent
     *                    any other transform from being applied until updateModelTransforms().
     */
    void blendAnimTransforms(Vector3f translation, Quaternion rotation, Vector3f scale,
                             float weight) {
//        if (userControl) {
//            return;
//        }

        if (weight == 0) {
            // Do not apply this transform at all.
            return;
        }

        if (currentWeightSum == 1) {
            return; // More than 2 transforms are being blended
        } else if (currentWeightSum == -1 || currentWeightSum == 0) {
            // Set the transform fully
            localPosition.set(bindPosition).addLocal(translation);
            localRotation.set(bindRotation).multLocal(rotation);
            if (scale != null) {
                localScale.set(bindScale).multLocal(scale);
            }
            // Set the weight. It will be applied in updateModelTransforms().
            currentWeightSum = weight;
        } else {
            // The weight is already set.
            // Blend in the new transform.
            TempVars vars = TempVars.get();

            Vector3f tmpV = vars.vect1;
            Vector3f tmpV2 = vars.vect2;
            Quaternion tmpQ = vars.quat1;

            tmpV.set(bindPosition).addLocal(translation);
            localPosition.interpolateLocal(tmpV, weight);

            tmpQ.set(bindRotation).multLocal(rotation);
            localRotation.nlerp(tmpQ, weight);

            if (scale != null) {
                tmpV2.set(bindScale).multLocal(scale);
                localScale.interpolateLocal(tmpV2, weight);
            }

            // Ensures no new weights will be blended in the future.
            currentWeightSum = 1;

            vars.release();
        }
    }

    /**
     * Should be called after all of the skeleton bones' bind transforms are set in order to save them.
     */
    public void setBindTransforms(Vector3f translation, Quaternion rotation, Vector3f scale) {
        bindPosition.set(translation);
        bindRotation.set(rotation);
        bindScale.set(scale);

        localPosition.set(translation);
        localRotation.set(rotation);
        localScale.set(scale);
    }

    /**
     * INTERNAL USE ONLY
     * could corrupt the skeleton.
     */
    public void addChild(Bone bone) {
        children.add(bone);
        bone.parent = this;
    }

    public Bone getParent() {
        return parent;
    }

    public void update() {
        updateModelTransforms();

        for (int index = children.size() - 1; index >= 0; index--) {
            children.get(index).update();
        }
    }

    private void updateModelTransforms() {
        if (currentWeightSum == 1f) {
            currentWeightSum = -1;
        } else if (currentWeightSum != -1f) {
            // Apply the weight to the local transform
            if (currentWeightSum == 0) {
                localRotation.set(bindRotation);
                localPosition.set(bindPosition);
                localScale.set(bindScale);
            } else {
                float invWeightSum = 1f - currentWeightSum;
                localRotation.nlerp(bindRotation, invWeightSum);
                localPosition.interpolateLocal(bindPosition, invWeightSum);
                localScale.interpolateLocal(bindScale, invWeightSum);
            }

            // Future invocations of transform blend will start over.
            currentWeightSum = -1;
        }

        if (parent != null) {
            parent.modelRotation.mult(localRotation, modelRotation);

            parent.modelScale.mult(localScale, modelScale);

            parent.modelRotation.mult(localPosition, modelPosition);
            modelPosition.multLocal(parent.modelScale);
            modelPosition.addLocal(parent.modelPosition);
        } else {
            modelPosition.set(localPosition);
            modelRotation.set(localRotation);
            modelScale.set(localScale);
        }

        // TODO: Attachment node (like sword)
    }

    public void setBindingPose() {
        bindPosition.set(localPosition);
        bindRotation.set(localRotation);
        bindScale.set(localScale);

        modelBindInversePosition.set(modelPosition);
        modelBindInversePosition.negateLocal();

        modelBindInverseRotation.set(modelRotation);
        modelBindInverseRotation.inverseLocal();

        modelBindInverseScale.set(Vector3f.UNIT_XYZ);
        modelBindInverseScale.divideLocal(modelScale);

        for (Bone bone : children) {
            bone.setBindingPose();
        }
    }

    public void reset() {
        localPosition.set(bindPosition);
        localRotation.set(bindRotation);
        localScale.set(bindScale);

        for (int index = children.size() - 1; index >= 0; index--) {
            children.get(index).reset();
        }
    }

    /**
     * Stores the skinning transform in the specified Matrix4f.
     * The skinning transform applied the animation of the bone to a vertex.
     * <p>
     * We assume that the world transforms for the entire bone hierarchy have already been computed,
     * otherwise this method will return undefined results.
     */
    public void getOffsetTransform(Matrix4f outTransformMatrix, Quaternion tempQuat,
                                   Vector3f tempVecA,
                                   Vector3f tempVecB, Matrix3f tempMat3) {
        var scale = modelScale.mult(modelBindInverseScale, tempVecB);
        var rotation = modelRotation.mult(modelBindInverseRotation, tempQuat);
        var translation = modelPosition
                .add(rotation.mult(scale.mult(modelBindInversePosition, tempVecA), tempVecA),
                     tempVecA);

        outTransformMatrix.setTransform(translation, scale, rotation.toRotationMatrix(tempMat3));
    }

    public Transform getBindInverseTransform() {
        var t = new Transform();
        t.setTranslation(bindPosition);
        t.setRotation(bindRotation);
        if (bindScale != null) {
            t.setScale(bindScale);
        }
        return t.invert();
    }

    public Transform getBindTransform() {
        var t = new Transform();
        t.setTranslation(bindPosition);
        t.setRotation(bindRotation);
        if (bindScale != null) {
            t.setScale(bindScale);
        }
        return t;
    }

    public String getName() {
        return name;
    }

    public Quaternion getModelSpaceRotation() {
        return modelRotation;
    }

    public Vector3f getModelSpacePosition() {
        return modelPosition;
    }

    public List<Bone> getChildren() {
        return children;
    }
}
