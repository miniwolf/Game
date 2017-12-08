package mini.app;

import mini.app.state.AbstractApplicationState;
import mini.app.state.ApplicationStateManager;
import mini.material.Material;
import mini.renderer.RenderManager;
import mini.scene.Geometry;
import mini.scene.shape.Box;
import org.mockito.InOrder;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

/**
 * Test the application state lifecycles.
 */
public class TestApplicationStateLifeCycle extends SimpleApplication {
    private TestState mock;
    private InOrder inOrder;

    public static void main(String[] args) {
        TestApplicationStateLifeCycle app = new TestApplicationStateLifeCycle();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Box b = new Box(1, 1, 1);
        Geometry geom = new Geometry("Box", b);
        Material mat = new Material(assetManager, "MatDefs/Misc/Unshaded.minid");
        mat.setTexture("ColorMap", assetManager.loadTexture("Interface/Logo/Monkey.jpg"));
        geom.setMaterial(mat);
        rootNode.attachChild(geom);

        mock = Mockito.mock(TestState.class);
        when(mock.isEnabled()).thenReturn(true);
        inOrder = inOrder(mock);

        System.out.println("Attaching test state");
        stateManager.attach(mock);
    }

    @Override
    public void simpleDestroy() {
        inOrder.verify(mock).stateAttached(any(ApplicationStateManager.class));
        inOrder.verify(mock).initialize(any(ApplicationStateManager.class), any(Application.class));
        inOrder.verify(mock).update(any());
        inOrder.verify(mock).stateDetached(any(ApplicationStateManager.class));
        inOrder.verify(mock).cleanup();
        super.simpleDestroy();
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (stateManager.getState(TestState.class) != null) {
            System.out.println("Detaching test state.");
            stateManager.detach(stateManager.getState(TestState.class));
            System.out.println("Done");
        }
    }

    public class TestState extends AbstractApplicationState {
        @Override
        public void initialize(ApplicationStateManager manager, Application app) {
            super.initialize(manager, app);
            System.out.println("Initialized");
        }

        @Override
        public void stateAttached(ApplicationStateManager stateManager) {
            super.stateAttached(stateManager);
            System.out.println("Attached");
        }

        @Override
        public void update(float tpf) {
            super.update(tpf);
            System.out.println("Update");
        }

        @Override
        public void render(RenderManager renderManager) {
            super.render(renderManager);
            System.out.println("Render");
        }

        @Override
        public void postRender() {
            super.postRender();
            System.out.println("Postrender");
        }

        @Override
        public void cleanup() {
            super.cleanup();
            System.out.println("Cleanup");
        }

        @Override
        public void stateDetached(ApplicationStateManager stateManager) {
            super.stateDetached(stateManager);
        }
    }
}
