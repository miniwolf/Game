package mini.editor.plugin.api.editor;

import javafx.event.Event;
import javafx.geometry.Point2D;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import mini.editor.annotation.FxThread;
import mini.editor.part3d.editor.impl.AbstractEditor3DPart;
import mini.editor.ui.component.editor.state.impl.Editor3DEditorState;

import java.awt.event.MouseEvent;

public abstract class Base3DFileEditor<T extends AbstractEditor3DPart, S extends Editor3DEditorState>
        extends BaseFileEditor<S> {

    /**
     * The 3D part of this editor
     */
    private final T editor3DPart;
    /**
     * The pane of the 3D editor area.
     */
    private BorderPane editor3DArea;

    public Base3DFileEditor() {
        this.editor3DPart = create3DEditorPart();
        addEditor3DPart(editor3DPart);
    }

    /**
     * Create 3D part of this editor
     *
     * @return the 3D part
     */
    protected abstract T create3DEditorPart();

    /**
     * @return the 3D part of this editor
     */
    public T getEditor3DPart() {
        return editor3DPart;
    }

    @Override
    @FxThread
    public boolean isInside(final double sceneX,
                            final double sceneY,
                            Class<? extends Event> eventType) {
        final Pane editorPage = getPage();
        final Pane editor3DPage = get3DArea() == null ? editorPage : get3DArea();

        final boolean only3D = eventType.isAssignableFrom(MouseEvent.class) ||
                               eventType.isAssignableFrom(ScrollEvent.class);
        final Pane page = only3D ? editor3DPage : editorPage;

        final Point2D point2D = page.sceneToLocal(sceneX, sceneY);
        return page.contains(point2D);
    }
}
