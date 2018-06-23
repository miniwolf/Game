package mini.editor.ui.component.tab;

import javafx.geometry.Side;
import javafx.scene.control.SplitPane;
import mini.editor.ui.css.CssIds;

/**
 * The component to contain global left tool components.
 */
public class GlobalLeftToolComponent extends TabToolComponent {
    public GlobalLeftToolComponent(SplitPane pane) {
        super(pane);
        setId(CssIds.GLOBAL_LEFT_TOOL_COMPONENT);
        setSide(Side.LEFT);
    }
}
