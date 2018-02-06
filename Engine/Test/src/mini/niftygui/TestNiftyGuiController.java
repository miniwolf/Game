package mini.niftygui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.Button;
import de.lessvoid.nifty.controls.ButtonClickedEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import mini.app.SimpleApplication;
import mini.post.niftygui.NiftyMiniDisplay;

public class TestNiftyGuiController extends SimpleApplication implements ScreenController {
    private Button applyButton;
    private Nifty nifty;

    public static void main(String[] args) {
        TestNiftyGuiController app = new TestNiftyGuiController();
        app.start();
    }

    @Override
    public void simpleInitApp() {

        NiftyMiniDisplay niftyDisplay = new NiftyMiniDisplay(assetManager, guiViewPort,
                                                             inputManager);
        nifty = niftyDisplay.getNifty();
        nifty.fromXml("minitest/nifty/Button.xml", "Screen", this);
        guiViewPort.addProcessor(niftyDisplay);
        inputManager.setCursorVisible(true);
        flyCam.setEnabled(false);
    }

    @NiftyEventSubscriber(id = "ApplyButton")
    public void onButton(String id, ButtonClickedEvent event) {
        System.err.println("It works " + id);
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        applyButton = screen.findNiftyControl("ApplyButton", Button.class);
    }

    @Override
    public void onStartScreen() {
    }

    @Override
    public void onEndScreen() {
    }
}
