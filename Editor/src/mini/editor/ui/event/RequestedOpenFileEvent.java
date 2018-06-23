package mini.editor.ui.event;

import com.ss.rlib.common.util.ObjectUtils;
import javafx.event.Event;
import javafx.event.EventType;
import mini.editor.ui.component.editor.EditorDescription;

import java.nio.file.Path;

public class RequestedOpenFileEvent extends SceneEvent {
    private static final EventType<SceneEvent> EVENT_TYPE;

    private static final String NEED_SHOW = "need_show";
    private static final String FILE = "file";
    private static final String EDITOR = "editor";

    static {
        synchronized (Event.class) {
            EVENT_TYPE = new EventType<>(SceneEvent.EVENT_TYPE,
                                         RequestedOpenFileEvent.class.getSimpleName());
        }
    }

    public RequestedOpenFileEvent(Path file) {
        super(EVENT_TYPE);
        setNeedShow(true);
        setFile(file);
    }

    public Path getFile() {
        return ObjectUtils.notNull(get(FILE));
    }

    public void setFile(Path file) {
        set(FILE, file);
    }

    public boolean isNeedShow() {
        return get(NEED_SHOW);
    }

    /**
     * Set whether to show the editor
     */
    public void setNeedShow(boolean needShow) {
        set(NEED_SHOW, needShow);
    }

    public EditorDescription getDescription() {
        return get(EDITOR);
    }

    public void setDescription(EditorDescription description) {
        if (description == null) {
            remove(EDITOR);
        } else {
            set(EDITOR, description);
        }
    }
}
