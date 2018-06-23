package mini.niftygui;

import de.lessvoid.nifty.Nifty;
import mini.app.SimpleApplication;
import mini.input.KeyboardKey;
import mini.input.controls.AnalogListener;
import mini.input.controls.KeyTrigger;
import mini.post.niftygui.NiftyMiniDisplay;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class TestNiftyEditor extends SimpleApplication implements AnalogListener {
    private Nifty nifty;

    public static void main(String[] args) {
        TestNiftyEditor app = new TestNiftyEditor();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        NiftyMiniDisplay niftyMiniDisplay = new NiftyMiniDisplay(assetManager, guiViewPort,
                                                                 inputManager);
        nifty = niftyMiniDisplay.getNifty();

        nifty.fromXml("nifty/SceneComposerTopComponent.xml", "start");

        guiViewPort.addProcessor(niftyMiniDisplay);

        flyCam.setEnabled(false);
        setupInput();
    }

    private void setupInput() {
        inputManager.addMapping("Reload", new KeyTrigger(KeyboardKey.KEY_R));
        inputManager.addListener(this, "Reload");
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
        if (name.equals("Reload")) {
            ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
            URL resURL = ctxLoader.getResource("nifty/SceneComposerTopComponent.xml");
            InputStream resIn;
            try {
                URLConnection resConn = resURL.openConnection();
                resConn.setUseCaches(false);
                resIn = resConn.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            try {
                nifty.fromXml("nifty/SceneComposerTopComponent.xml", resIn, "start");
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
