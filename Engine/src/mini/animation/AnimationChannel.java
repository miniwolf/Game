package mini.animation;

public class AnimationChannel {
    private static final float DEFAULT_BLEND_TIME = 0.15f;

    private AnimationControl control;
    private Animation animation;
    private Animation blendFrom;

    private float time;
    private float speed;
    private boolean notified;
    private LoopMode loopMode;

    public AnimationChannel(AnimationControl control) {
        this.control = control;
    }

    public void setAnim(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }

        Animation animation = control.getAnimation(name);
        if (animation == null) {
            throw new IllegalArgumentException("Cannot find animation named: '" + name + "'");
        }

        control.notifyAnimationChange(this, name);

        this.animation = animation;
        this.time = 0;
        this.speed = 1f;
        this.notified = false;
        this.loopMode = LoopMode.Loop;
    }

    public void update(float tpf) {
        if (animation == null) {
            return;
        }

        animation.setTime(time, control);
        time += tpf * speed;
    }
}
