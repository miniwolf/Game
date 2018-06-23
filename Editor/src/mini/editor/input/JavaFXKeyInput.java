package mini.editor.input;

import com.ss.rlib.common.util.linkedlist.LinkedList;
import com.ss.rlib.common.util.linkedlist.LinkedListFactory;
import mini.editor.injfx.MiniOffscreenSurfaceContext;
import mini.input.events.KeyInputEvent;

public class JavaFXKeyInput extends JavaFXInput {

    private final LinkedList<KeyInputEvent> keyInputEvents;

    public JavaFXKeyInput(MiniOffscreenSurfaceContext context) {
        super(context);
        keyInputEvents = LinkedListFactory.newLinkedList(KeyInputEvent.class);
    }

    @Override
    protected void initializeImplementation() {
    }

    @Override
    protected void updateImpl() {
        var listener = getListener();
        while (!keyInputEvents.isEmpty()) {
            listener.onKeyEvent(keyInputEvents.poll());
        }
    }
}
