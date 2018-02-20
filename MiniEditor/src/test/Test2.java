package test;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.Button;
import de.lessvoid.nifty.controls.ButtonClickedEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

public class Test2 implements ScreenController {
    private Button button;
    private Nifty nifty;

    @Override
    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        button = screen.findNiftyControl("BackButton", Button.class);
    }

    @NiftyEventSubscriber(id="BackButton")
    public void onButton(String id, ButtonClickedEvent event) {
        System.err.println("From back button " + id);
        ScreenController controller = nifty.findScreenController("test.Test");
        nifty.fromXml("test/Test.xml", "Screen", controller);
    }

    @Override
    public void onStartScreen() {

    }

    @Override
    public void onEndScreen() {

    }
}
