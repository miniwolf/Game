package mini.animation;

import mini.scene.Node;
import mini.scene.Spatial;
import mini.utils.TempVars;
import mini.utils.clone.Cloner;
import mini.utils.clone.MiniCloneable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The animation class updates the animation target with the tracks (takes) of a given type.
 */
public class Animation implements Cloneable, MiniCloneable {
    private final String name;
    private final float length;
    private List<Track> tracks = new ArrayList<>();

    public Animation(String name, float length) {
        this.name = name;
        this.length = length;
    }

    public void setTracks(Track[] tracksArray) {
        tracks.addAll(Arrays.asList(tracksArray));
    }

    private Spatial findModelName(String modelName, Spatial current) {
        if (current.getName().toLowerCase().equals(modelName) || current.getName().toLowerCase()
                                                                        .startsWith(
                                                                                modelName + "-")) {
            return current;
        }

        if (current instanceof Node) {
            var rootNode = (Node) current;
            if (!rootNode.getChildren().isEmpty()) {
                for (Spatial child : rootNode.getChildren()) {
                    var res = findModelName(modelName, child);
                    if (res != null) {
                        return res;
                    }
                }
            }
        }
        return null;
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

    public void setTime(float time, float blendAmount, AnimationControl control,
                        AnimationChannel channel, TempVars vars) {
        if (tracks == null) {
            return;
        }

        for (Track track : tracks) {
            track.setTime(time, blendAmount, control, channel, vars);
        }
    }

    /**
     * Adds a track to this animation
     *
     * @param track the track to add
     */
    public void addTrack(Track track) {
        tracks.add(track);
    }

    /**
     * @return the length in seconds of this animation.
     */
    public float getLength() {
        return length;
    }
}
