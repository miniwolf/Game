package mini.editor.part3d.editor.impl.model;

import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import mini.app.Application;
import mini.app.state.ApplicationStateManager;
import mini.editor.annotation.EditorThread;
import mini.editor.ui.component.editor.impl.model.ModelFileEditor;
import mini.editor.ui.component.editor.impl.scene.AbstractSceneEditor3DPart;
import mini.editor.util.EditorUtil;
import mini.environment.generation.JobProgressAdapter;
import mini.light.LightProbe;
import mini.renderer.RenderManager;
import mini.scene.Node;
import mini.scene.Spatial;

public class ModelEditor3DPart extends AbstractSceneEditor3DPart<ModelFileEditor, Spatial> {
    private final JobProgressAdapter<LightProbe> probeHandler = new JobProgressAdapter<>() {
        @Override
        public void done(LightProbe result) {
            if (!isInitialized()) {
                return;
            }

            notifyProbeCompleted();
        }
    };

    private final Node customSkyNode;
    private final Array<Spatial> customSky;
    private boolean lightEnabled;

    private int frame;

    public ModelEditor3DPart(final ModelFileEditor fileEditor) {
        super(fileEditor);

        customSkyNode = new Node("Custom Sky");
        customSky = ArrayFactory.newArray(Spatial.class);

        final Node stateNode = getStateNode();
        stateNode.attachChild(getCustomSkyNode());

        setLightEnabled(true);
    }

    public Node getCustomSkyNode() {
        return customSkyNode;
    }

    public void setLightEnabled(final boolean lightEnabled) {
        this.lightEnabled = lightEnabled;
    }

    public boolean isLightEnabled() {
        return lightEnabled;
    }

    @Override
    @EditorThread
    public void initialize(ApplicationStateManager manager, Application app) {
        super.initialize(manager, app);
        frame = 0;
    }

    @Override
    @EditorThread
    public void cleanup() {
        super.cleanup();

        final Node stateNode = getStateNode();
        stateNode.detachChild(getModelNode());
        stateNode.detachChild(getToolNode());
    }

    @Override
    public void stateAttached(ApplicationStateManager stateManager) {
    }

    @Override
    public void stateDetached(ApplicationStateManager stateManager) {
    }

    @Override
    @EditorThread
    public void update(float tpf) {
        super.update(tpf);

        if (frame == 2) {
            EditorUtil.updateGlobalLightProbe(probeHandler);
        }

        frame++;
    }

    @EditorThread
    private void notifyProbeCompleted() {
        final Node stateNode = getStateNode();
        stateNode.attachChild(getModelNode());
        stateNode.attachChild(getToolNode());

        final Node customSkyNode = getCustomSkyNode();
        customSkyNode.detachAllChildren();

        // TODO: refresh renderFilterExtension
    }

    @Override
    public void render(RenderManager renderManager) {
    }

    @Override
    public void postRender() {
    }
}
