package mini.animation;

import mini.renderer.RenderManager;
import mini.renderer.ViewPort;
import mini.scene.AbstractControl;
import mini.utils.TempVars;
import mini.utils.clone.Cloner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimationControl extends AbstractControl {
    private List<AnimationChannel> channels = new ArrayList<>();
    private Map<String, Animation> animationMap = new HashMap<>();
    private Skeleton skeleton;

    public AnimationControl(Skeleton skeleton) {
        this.skeleton = skeleton;
        reset();
    }

    public AnimationControl() {
    }

    private void reset() {
        if (skeleton != null) {
            skeleton.resetAndUpdate();
        }
    }

    @Override
    protected void controlRender(RenderManager renderManager, ViewPort vp) {
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (skeleton != null) {
            skeleton.reset();
        }

        try (var vars = TempVars.get()) {
            for (AnimationChannel channel : channels) {
                channel.update(tpf, vars);
            }
        }

        if (skeleton != null) {
            skeleton.updateWorldVectors();
        }
    }

    @Override
    public Object miniClone() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cloneFields(Cloner cloner, Object original) {
        throw new UnsupportedOperationException();
    }

    public AnimationChannel createChannel() {
        AnimationChannel animationChannel = new AnimationChannel(this);
        channels.add(animationChannel);
        return animationChannel;
    }

    public Animation getAnimation(String name) {
        return animationMap.get(name);
    }

    public void addAnimation(Animation animation) {
        animationMap.put(animation.getName(), animation);
    }

    public void notifyAnimationChange(AnimationChannel animationChannel, String name) {
        // TODO: Listeners.onAnimationChange(this, animationChannel, name)
    }

    void notifyAnimCycleDone(AnimationChannel channel, String name) {
        // TODO: Listeners.onAnimCycleDone(this, channel, name)
    }

    public Skeleton getSkeleton() {
        return skeleton;
    }

    public void setSkeleton(Skeleton skeleton) {
        this.skeleton = skeleton;
    }
}
