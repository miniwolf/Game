package mini.audio.niftygui;

import de.lessvoid.nifty.sound.SoundSystem;
import de.lessvoid.nifty.spi.sound.SoundDevice;
import de.lessvoid.nifty.spi.sound.SoundHandle;
import de.lessvoid.nifty.tools.resourceloader.NiftyResourceLoader;
import mini.post.niftygui.NiftyMiniDisplay;

public class SoundDeviceMini implements SoundDevice {
    public SoundDeviceMini(NiftyMiniDisplay niftyMiniDisplay) {

    }

    @Override
    public void setResourceLoader(NiftyResourceLoader niftyResourceLoader) {
    }

    @Override
    public SoundHandle loadSound(SoundSystem soundSystem, String s) {
        return new SoundHandle() {
            @Override
            public void play() {

            }

            @Override
            public void stop() {

            }

            @Override
            public void setVolume(float v) {

            }

            @Override
            public float getVolume() {
                return 0;
            }

            @Override
            public boolean isPlaying() {
                return false;
            }

            @Override
            public void dispose() {

            }
        };
    }

    @Override
    public SoundHandle loadMusic(SoundSystem soundSystem, String s) {
        return new SoundHandle() {
            @Override
            public void play() {

            }

            @Override
            public void stop() {

            }

            @Override
            public float getVolume() {
                return 0;
            }

            @Override
            public void setVolume(float v) {

            }

            @Override
            public boolean isPlaying() {
                return false;
            }

            @Override
            public void dispose() {

            }
        };
    }

    @Override
    public void update(int i) {
    }
}
