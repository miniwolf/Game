package mini.editor.ui.event.impl;

import javafx.event.Event;
import javafx.event.EventType;
import mini.editor.ui.event.SceneEvent;
import mini.editor.util.ObjectsUtil;

import java.nio.file.Path;

/**
 * Event for when a file is changed.
 */
public class FileChangedEvent extends SceneEvent {
    public static final EventType<FileChangedEvent> EVENT_TYPE;
    private static final String FILE = "file";

    static {
        synchronized (Event.class) {
            EVENT_TYPE = new EventType<>(SceneEvent.EVENT_TYPE,
                                         FileChangedEvent.class.getSimpleName());
        }
    }

    public FileChangedEvent() {
        super(EVENT_TYPE);
    }

    /**
     * @return the file
     */
    public Path getFile() {
        return ObjectsUtil.notNull(get(FILE), "Cannot find a file");
    }

    /**
     * Set the file
     */
    public void setFile(Path file) {
        set(FILE, file);
    }
}
