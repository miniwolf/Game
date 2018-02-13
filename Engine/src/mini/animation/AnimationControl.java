package mini.animation;

import mini.renderer.RenderManager;
import mini.renderer.ViewPort;
import mini.scene.AbstractControl;
import mini.utils.clone.Cloner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimationControl extends AbstractControl {
    private List<AnimationChannel> channels = new ArrayList<>();
    private Map<String, Animation> animationMap = new HashMap<>();

    @Override
    public void update(float tpf) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void controlRender(RenderManager renderManager, ViewPort vp) {
    }

    @Override
    protected void controlUpdate(float tpf) {
        for (AnimationChannel channel : channels) {
            channel.update(tpf);
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
}
