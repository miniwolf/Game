package mini.animation;

public class AnimationUtils {
    /**
     * Clamps the time according to duration and loopMode
     *
     * @param time
     * @param duration
     * @param loopMode
     * @return
     */
    public static float clampWrapTime(float time, float duration, LoopMode loopMode) {
        if (time == 0) {
            return 0; // prevent division by 0 errors
        }
        switch (loopMode) {
            case Cycle:
                boolean sign = ((int) (time / duration) % 2) != 0;
                return sign ? -(duration - (time % duration)) : time % duration;
            case DontLoop:
                return time > duration ? duration : (time < 0 ? 0 : time);
            case Loop:
                return time % duration;
        }
        return time;
    }
}
