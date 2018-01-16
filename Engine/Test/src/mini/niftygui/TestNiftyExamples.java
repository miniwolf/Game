package mini.niftygui;

import de.lessvoid.nifty.Nifty;
import mini.app.SimpleApplication;
import mini.post.niftygui.NiftyMiniDisplay;

public class TestNiftyExamples extends SimpleApplication {
    public static void main(String[] args) {
        TestNiftyExamples app = new TestNiftyExamples();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        NiftyMiniDisplay niftyMiniDisplay = new NiftyMiniDisplay(assetManager, guiViewPort,
                                                                 inputManager);
        Nifty nifty = niftyMiniDisplay.getNifty();

        nifty.fromXml("all/intro.xml", "start");

        guiViewPort.addProcessor(niftyMiniDisplay);

        flyCam.setEnabled(false);
    }
}
