package mini.animation;

import mini.utils.TempVars;

public interface Track {
    /**
     * Internally, the track will retrieve objects from the control and modify them accordingly to
     * the properties of the channel and the given paramters.
     *
     * @param time The time in seconds of the animation
     */
    void setTime(float time, float weight, AnimationControl control, AnimationChannel channel,
                 TempVars vars);
}
