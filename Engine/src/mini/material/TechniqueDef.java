package mini.material;

import mini.material.logic.TechniqueDefLogic;

/**
 * Created by miniwolf on 30-04-2017.
 */
public class TechniqueDef {
    private RenderState renderState;
    private TechniqueDefLogic logic;

    /**
     * Returns the render state that this technique is using
     *
     * @return the render state that this technique is using
     * @see #setRenderState(RenderState)
     */
    public RenderState getRenderState() {
        return renderState;
    }

    /**
     * Sets the render state that this technique is using.
     *
     * @param renderState the render state that this technique is using.
     * @see RenderState
     */
    public void setRenderState(RenderState renderState) {
        this.renderState = renderState;
    }

    public void setLogic(TechniqueDefLogic logic) {
        this.logic = logic;
    }

    public TechniqueDefLogic getLogic() {
        return logic;
    }
}
