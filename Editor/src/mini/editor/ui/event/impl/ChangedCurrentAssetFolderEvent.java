package mini.editor.ui.event.impl;

import javafx.event.Event;
import javafx.event.EventType;
import mini.editor.ui.event.SceneEvent;
import mini.editor.util.ObjectsUtil;

import java.nio.file.Path;

public class ChangedCurrentAssetFolderEvent extends SceneEvent {
    public static final EventType<SceneEvent> EVENT_TYPE;
    private static final String ASSET = "asset";

    static {
        synchronized (Event.class) {
            EVENT_TYPE = new EventType<>(SceneEvent.EVENT_TYPE,
                                         ChangedCurrentAssetFolderEvent.class.getSimpleName());
        }
    }

    public ChangedCurrentAssetFolderEvent(final Path assetFolder) {
        super(EVENT_TYPE);
        setNewAssetFolder(assetFolder);
    }

    public final Path getNewAssetFolder() {
        return ObjectsUtil.notNull(get(ASSET));
    }

    public void setNewAssetFolder(final Path newAssetFolder) {
        set(ASSET, newAssetFolder);
    }
}
