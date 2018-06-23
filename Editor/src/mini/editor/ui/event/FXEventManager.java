package mini.editor.ui.event;

import com.ss.rlib.common.util.ClassUtils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import com.ss.rlib.common.util.dictionary.DictionaryFactory;
import com.ss.rlib.common.util.dictionary.ObjectDictionary;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import mini.editor.annotation.FromAnyThread;
import mini.editor.annotation.FxThread;
import mini.editor.manager.ExecutorManager;

public class FXEventManager {
    private static final FXEventManager INSTANCE = new FXEventManager();

    private final ObjectDictionary<EventType<? extends Event>, Array<EventHandler<? extends Event>>>
            eventHandlers;

    public FXEventManager() {
        eventHandlers = DictionaryFactory.newObjectDictionary();
    }

    @FromAnyThread
    public static FXEventManager getInstance() {
        return INSTANCE;
    }

    public <T extends Event> void addEventHandler(
            final EventType<T> eventType,
            final EventHandler<T> eventHandler) {
        getEventHandlers().get(eventType, () -> ArrayFactory.newArray(EventHandler.class))
                          .add(eventHandler);
    }

    public ObjectDictionary<EventType<? extends Event>, Array<EventHandler<? extends Event>>> getEventHandlers() {
        return eventHandlers;
    }

    @FromAnyThread
    public void notify(Event event) {
        if (Platform.isFxApplicationThread()) {
            notifyImpl(event);
        } else {
            var executorManager = ExecutorManager.getInstance();
            executorManager.addFXTask(() -> notifyImpl(event));
        }
    }

    @FxThread
    private void notifyImpl(Event event) {
        var eventHandlers = getEventHandlers();

        for (EventType<? extends Event> eventType = event.getEventType();
             eventType != null;
             eventType = eventType.getSuperType()) {
            var handlers = eventHandlers.get(eventType);
            if (handlers == null || handlers.isEmpty()) {
                continue;
            }

            handlers.forEach(event, (handler, toHandle) ->
                    handler.handle(ClassUtils.unsafeCast(event)));
        }

//        if (!event.isConsumed()) { // && event instanceof ConsumableEvent
//
//        }
    }
}
