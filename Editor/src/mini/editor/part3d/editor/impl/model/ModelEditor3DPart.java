package mini.editor.part3d.editor.impl.model;

import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import mini.app.state.ApplicationStateManager;
import mini.editor.ui.component.editor.impl.model.ModelFileEditor;
import mini.editor.ui.component.editor.impl.scene.AbstractSceneEditor3DPart;
import mini.renderer.RenderManager;
import mini.scene.Node;
import mini.scene.Spatial;

public class ModelEditor3DPart extends AbstractSceneEditor3DPart<ModelFileEditor, Spatial> {
    private final Node customSkyNode;
    private final Array<Spatial> customSky;
    private boolean lightEnabled;

    public ModelEditor3DPart(final ModelFileEditor fileEditor) {
        super(fileEditor);

        customSkyNode = new Node("Custom Sky");
        customSky = ArrayFactory.newArray(Spatial.class);

        final Node stateNode = getStateNode();
        stateNode.attachChild(getCustomSkyNode());

        setLightEnabled(true);
    }

    public Spatial getCustomSkyNode() {
        return customSkyNode;
    }

    public void setLightEnabled(final boolean lightEnabled) {
        this.lightEnabled = lightEnabled;
    }

    public boolean isLightEnabled() {
        return lightEnabled;
    }

    @Override
    public void stateAttached(ApplicationStateManager stateManager) {
    }

    @Override
    public void stateDetached(ApplicationStateManager stateManager) {
    }

    @Override
    public void render(RenderManager renderManager) {
    }

    @Override
    public void postRender() {
    }
}
