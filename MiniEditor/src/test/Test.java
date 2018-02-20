package test;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.Button;
import de.lessvoid.nifty.controls.ButtonClickedEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import mini.app.SimpleApplication;
import mini.post.niftygui.NiftyMiniDisplay;

public class Test extends SimpleApplication implements ScreenController {
    private Button applyButton;
    private Nifty nifty;
    private boolean whatever;

    public static void main(String[] args) {
        Test app = new Test();
        app.start();
    }

    @Override
    public void simpleInitApp() {

        NiftyMiniDisplay niftyDisplay = new NiftyMiniDisplay(assetManager, guiViewPort,
                inputManager);
        nifty = niftyDisplay.getNifty();
        nifty.fromXml("test/Test.xml", "Screen", this);
        guiViewPort.addProcessor(niftyDisplay);
        inputManager.setCursorVisible(true);
        flyCam.setEnabled(false);
    }

    @NiftyEventSubscriber(id="ApplyButton")
    public void onButton(String id, ButtonClickedEvent event) {
        whatever = true;

        System.err.println("It works " + id);
        ScreenController controller = nifty.findScreenController("test.Test2");
        nifty.fromXml("test/Test2.xml", "Screen", new Test2());
    }


    @Override
    public void bind(Nifty nifty, Screen screen) {
        applyButton = screen.findNiftyControl("ApplyButton", Button.class);
        System.out.println(whatever);
    }

    @Override
    public void onStartScreen() {
    }

    @Override
    public void onEndScreen() {
    }
}
