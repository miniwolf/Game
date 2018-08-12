package mini.editor.model.undo.editor;

import mini.scene.Spatial;

/**
 * Notify about any changes of the models.
 */
public interface ModelChangeConsumer extends ChangeConsumer {
    /**
     * @return the current model of the editor
     */
    Spatial getCurrentModel();
}
