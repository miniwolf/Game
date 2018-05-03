package mini.animation;

import mini.utils.TempVars;

import java.util.BitSet;

public class AnimationChannel {
    private static final float DEFAULT_BLEND_TIME = 0.15f;

    private AnimationControl control;
    private Animation animation;
    private Animation blendFrom;
    private BitSet affectedBones;

    private float time;
    private float speed;
    private float timeBlendFrom;
    private float speedBlendFrom;
    private float blendAmount = 1f;
    private float blendRate = 0;
    private float blendTime;
    private boolean notified;
    private LoopMode loopMode, loopModeBlendFrom;

    public AnimationChannel(AnimationControl control) {
        this.control = control;
    }

    public void setAnim(String name) {
        setAnim(name, DEFAULT_BLEND_TIME);
    }

    /**
     * This resets the time to zero, and optionally blends the animation over <code>blendTime</code>
     * seconds with the currently playing animation.
     * Notice that this method will reset the control's speed to 1.0
     *
     * @param name      The name of the animation to play
     * @param blendTime The blend time over which to blend the new animation with the old one. If
     *                  zero, then no blending will occur and the new animation will be applied
     *                  instantly.
     */
    public void setAnim(String name, float blendTime) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }

        if (blendTime < 0f) {
            throw new IllegalArgumentException("blendTime cannot be less than zero");
        }

        Animation anim = control.getAnimation(name);
        if (anim == null) {
            throw new IllegalArgumentException("Cannot find animation named: '" + name + "'");
        }

        control.notifyAnimationChange(this, name);

        if (animation != null && blendTime > 0f) {
            this.blendTime = blendTime;

            blendTime = Math.min(blendTime, anim.getLength() / speed);
            blendFrom = animation;
            timeBlendFrom = time;
            speedBlendFrom = speed;
            loopModeBlendFrom = loopMode;
            blendAmount = 0f;
            blendRate = 1f / blendTime;
        } else {
            blendFrom = null;
        }

        this.animation = anim;
        this.time = 0;
        this.speed = 1f;
        this.notified = false;
        this.loopMode = LoopMode.Loop;
    }

    /**
     * Add a single bone to be influenced by this animation channel.
     */
    public void addBone(String name) {
        addBone(control.getSkeleton().getBone(name));
    }

    /**
     * Add a single bone to be influenced by this animation channel.
     */
    public void addBone(Bone bone) {
        int boneIndex = control.getSkeleton().getBoneIndex(bone);
        if (affectedBones == null) {
            affectedBones = new BitSet(control.getSkeleton().getBoneCount());
        }
        affectedBones.set(boneIndex);
    }

    public void update(float tpf, TempVars vars) {
        if (animation == null) {
            return;
        }

        if (blendFrom != null && blendAmount != 1.0f) {
            // The blendFrom anim is set, the actual animation
            // playing will be set
//            blendFrom.setTime(timeBlendFrom, 1f, control, this, vars);
            blendFrom.setTime(timeBlendFrom, 1f - blendAmount, control, this, vars);

            timeBlendFrom += tpf * speedBlendFrom;
            timeBlendFrom = AnimationUtils.clampWrapTime(timeBlendFrom,
                                                         blendFrom.getLength(),
                                                         loopModeBlendFrom);
            if (timeBlendFrom < 0) {
                timeBlendFrom = -timeBlendFrom;
                speedBlendFrom = -speedBlendFrom;
            }

            blendAmount += tpf * blendRate;
            if (blendAmount > 1f) {
                blendAmount = 1f;
                blendFrom = null;
            }
        }

        animation.setTime(time, blendAmount, control, this, vars);
        time += tpf * speed;
        if (animation.getLength() > 0) {
            if (!notified && (time >= animation.getLength() || time < 0)) {
                if (loopMode == LoopMode.DontLoop) {
                    // Note that this flag has to be set before calling the notify
                    // since the notify may start a new animation and then unset
                    // the flag.
                    notified = true;
                }
                control.notifyAnimCycleDone(this, animation.getName());
            }
        }
        time = AnimationUtils.clampWrapTime(time, animation.getLength(), loopMode);
        if (time < 0) {
            // Negative time indicates that speed should be inverted
            // (for cycle loop mode only)
            time = -time;
            speed = -speed;
        }
    }

    BitSet getAffectedBones() {
        return affectedBones;
    }
}
