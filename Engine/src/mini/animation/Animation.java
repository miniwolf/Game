package mini.animation;

import mini.utils.clone.Cloner;
import mini.utils.clone.MiniCloneable;

/**
 * The animation class updates the animation target with the tracks (takes) of a given type.
 */
public class Animation implements Cloneable, MiniCloneable {
    private final String name;
    private final float length;
    private SpatialTrack[] tracks;

    public Animation(String name, float length) {

        this.name = name;
        this.length = length;
    }

    public void setTracks(SpatialTrack[] tracks) {
        this.tracks = tracks;
    }

    public String getName() {
        return name;
    }

    @Override
    protected Object clone() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object miniClone() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cloneFields(Cloner cloner, Object original) {
        throw new UnsupportedOperationException();
    }

    public void setTime(float time, AnimationControl control, AnimationChannel channel) {
        if (tracks == null) {
            return;
        }

        for (SpatialTrack track : tracks) {
            track.setTime(time, control, channel);
        }
    }
}
