package mini.editor.model.undo.editor.model.undo.impl;

import com.ss.rlib.common.util.array.Array;
import mini.editor.annotation.EditorThread;
import mini.editor.manager.ExecutorManager;
import mini.editor.model.undo.editor.ModelChangeConsumer;
import mini.light.Light;
import mini.scene.Node;
import mini.scene.Spatial;
import mini.scene.control.Control;

public class RemoveElementsOperation extends AbstractEditorOperation<ModelChangeConsumer> {
    private static final ExecutorManager EXECUTOR_MANAGER = ExecutorManager.getInstance();
    private Array<Element> elements;

    public RemoveElementsOperation(Array<Element> elements) {
        this.elements = elements;
    }

    @Override
    protected void redoImpl(ModelChangeConsumer editor) {
        EXECUTOR_MANAGER.addEditorTask(() -> {
            for (final Element element : elements) {
                final Object toRemove = element.getElement();

                if (toRemove instanceof Spatial) {
                    removeSpatial(element, (Spatial) toRemove);
                } else if (toRemove instanceof Light) {
                    removeLight(element, (Light) toRemove);
                } else if (toRemove instanceof Control) {
                    removeControl(element, (Control) toRemove);
                }
            }
        });

        EXECUTOR_MANAGER.addFXTask(() -> elements.forEach(editor, ((element, consumer) ->
                consumer.notifyJavaFXRemovedChild(element.getParent(), element.getElement()))));
    }

    private void removeControl(final Element element, final Control toRemove) {
        final Spatial parent = (Spatial) element.getParent();
        parent.removeControl(toRemove);
    }

    @EditorThread
    private void removeLight(final Element element,
                             final Light toRemove) {
        final Spatial parent = (Spatial) element.getParent();
        parent.removeLight(toRemove);
    }

    @EditorThread
    private void removeSpatial(final Element element,
                               final Spatial toRemove) {
        final Node parent = (Node) element.getParent();
        element.setIndex(parent.getChildIndex(toRemove));
        parent.detachChild(toRemove);
    }

    public static class Element {
        private final Object element;
        private final Object parent;
        private int index;

        public Element(Object element, Object parent) {
            this.element = element;
            this.parent = parent;
            index = -1;
        }

        public Object getElement() {
            return element;
        }

        public Object getParent() {
            return parent;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }
}
