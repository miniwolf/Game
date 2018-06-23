package mini.editor.ui.event;

import com.ss.rlib.common.util.ClassUtils;
import com.ss.rlib.common.util.dictionary.DictionaryFactory;
import com.ss.rlib.common.util.dictionary.ObjectDictionary;
import javafx.event.Event;
import javafx.event.EventType;

public class SceneEvent extends Event {
    protected static final EventType<SceneEvent> EVENT_TYPE = new EventType<>(
            SceneEvent.class.getSimpleName());
    private ObjectDictionary<Object, Object> values;

    public SceneEvent(EventType<? extends Event> eventType) {
        super(eventType);
    }

    public SceneEvent(Object source,
                      EventType<? extends Event> eventType) {
        super(source, null, eventType);
    }

    public <T> T get(final Object key) {
        if (values == null) {
            return null;
        }

        final Object object = values.get(key);
        if (object == null) {
            return null;
        }

        return ClassUtils.unsafeCast(object);
    }

    public void set(final Object key, final Object value) {
        if (values == null) {
            values = DictionaryFactory.newObjectDictionary();
        }

        values.put(key, value);
    }

    public void remove(final Object key) {
        if (values == null) {
            return;
        }

        values.remove(key);
    }
}
