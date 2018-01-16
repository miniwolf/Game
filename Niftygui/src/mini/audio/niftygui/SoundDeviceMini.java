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
        return null;
    }

    @Override
    public SoundHandle loadMusic(SoundSystem soundSystem, String s) {
        return null;
    }

    @Override
    public void update(int i) {
    }
}
