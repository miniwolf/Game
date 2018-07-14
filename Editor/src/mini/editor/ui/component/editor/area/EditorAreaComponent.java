package mini.editor.ui.component.editor.area;

import com.ss.rlib.common.util.ArrayUtils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import com.ss.rlib.common.util.array.ConcurrentArray;
import com.ss.rlib.common.util.dictionary.ConcurrentObjectDictionary;
import com.ss.rlib.common.util.dictionary.DictionaryFactory;
import com.ss.rlib.common.util.dictionary.DictionaryUtils;
import com.ss.rlib.common.util.dictionary.ObjectDictionary;
import javafx.event.Event;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import mini.editor.Messages;
import mini.editor.MiniEditor;
import mini.editor.annotation.BackgroundThread;
import mini.editor.annotation.FxThread;
import mini.editor.manager.ExecutorManager;
import mini.editor.manager.WorkspaceManager;
import mini.editor.ui.component.ScreenComponent;
import mini.editor.ui.component.editor.EditorRegistry;
import mini.editor.ui.component.editor.FileEditor;
import mini.editor.ui.dialog.ConfirmDialog;
import mini.editor.ui.event.FXEventManager;
import mini.editor.ui.event.RequestedOpenFileEvent;
import mini.editor.ui.event.impl.ChangedCurrentAssetFolderEvent;
import mini.editor.util.EditorUtil;
import mini.editor.util.UIUtils;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The component for containing editors.
 */
public class EditorAreaComponent extends TabPane implements ScreenComponent {

    private static final WorkspaceManager WORKSPACE_MANAGER = WorkspaceManager.getInstance();
    private static final ExecutorManager EXECUTOR_MANAGER = ExecutorManager.getInstance();
    private static final EditorRegistry EDITOR_REGISTRY = EditorRegistry.getInstance();
    private static final FXEventManager FX_EVENT_MANAGER = FXEventManager.getInstance();
    private static final String KEY_EDITOR = "editor";

    private final ConcurrentObjectDictionary<Path, Tab> openedEditors;
    private final ConcurrentArray<Path> openingFiles;

    private boolean ignoredOpenedFiles;

    public EditorAreaComponent() {
        openedEditors = DictionaryFactory.newConcurrentAtomicObjectDictionary();
        openingFiles = ArrayFactory.newConcurrentStampedLockArray(Path.class);

        FX_EVENT_MANAGER.addEventHandler(ChangedCurrentAssetFolderEvent.EVENT_TYPE,
                                         event -> processEvent(
                                                 (ChangedCurrentAssetFolderEvent) event));
        FX_EVENT_MANAGER.addEventHandler(RequestedOpenFileEvent.EVENT_TYPE,
                                         event -> processOpenFile((RequestedOpenFileEvent) event));
    }

    private void processEvent(ChangedCurrentAssetFolderEvent event) {
        setIgnoredOpenedFiles(true);
        try {
            getTabs().clear();
            loadOpenedFiles();
        } finally {
            setIgnoredOpenedFiles(false);
        }
    }

    @Override
    public void notifyFinishBuild() {
        loadOpenedFiles();
    }

    private void loadOpenedFiles() {
        var workspace = WORKSPACE_MANAGER.getCurrentWorkspace();
        if (workspace == null) {
            return;
        }

        var assetFolder = workspace.getAssetFolder();
        var editFile = workspace.getCurrentEditedFile();

        var openedFiles = workspace.getOpenedFiles();
        openedFiles.forEach((assetPath, editorId) -> {
            var description = EDITOR_REGISTRY.getDescription(editorId);
            if (description == null) {
                return;
            }

            var file = assetFolder.resolve(assetPath);
            if (!Files.exists(file)) {
                return;
            }

            var event = new RequestedOpenFileEvent(file);
            event.setDescription(description);
            event.setNeedShow(editFile.equals(assetPath));

            processOpenFile(event);
        });
    }

    private void processOpenFile(RequestedOpenFileEvent event) {
        var file = event.getFile();

        var openedEditors = getOpenedEditors();
        var tab = DictionaryUtils.getInReadLock(openedEditors, file, ObjectDictionary::get);

        if (tab != null) {
            getSelectionModel().select(tab);
            return;
        }

        var openingFiles = getOpeningFiles();
        var stamp = openingFiles.writeLock();
        try {
            if (openingFiles.contains(file)) {
                return;
            }

            openingFiles.add(file);
            UIUtils.incrementLoading();

            EXECUTOR_MANAGER.addBackgroundTask(() -> processOpenFileImpl(event, file));
        } finally {
            openingFiles.writeUnlock(stamp);
        }
    }

    @BackgroundThread
    private void processOpenFileImpl(RequestedOpenFileEvent event, Path file) {
        var scene = EditorUtil.getFXScene();

        var description = event.getDescription();

        FileEditor editor = description == null
                            ? EDITOR_REGISTRY.createEditorFor(file)
                            : EDITOR_REGISTRY.createEditorFor(description);

        if (editor == null) {
            EXECUTOR_MANAGER.addFXTask(scene::decrementLoading);
            ArrayUtils.runInWriteLock(getOpeningFiles(), file, Array::fastRemove);
            return;
        }

        var editorApplication = MiniEditor.getInstance();
        var stamp = editorApplication.asyncLock();
        try {
            editor.openFile(file);
        } finally {
            editorApplication.asyncUnlock(stamp);
        }
        EXECUTOR_MANAGER.addFXTask(() -> addEditor(editor, event.isNeedShow()));
    }

    /**
     * Add and open the new file editor
     */
    @FxThread
    private void addEditor(FileEditor editor, boolean needShow) {
        var editFile = editor.getEditFile();

        var tab = new Tab(editor.getFileName());
        tab.setContent(editor.getPage());
        tab.setOnCloseRequest(event -> handleRequestToCloseEditor(editor, tab, event));
        tab.getProperties().put(KEY_EDITOR, editor);

        editor.dirtyProperty().addListener(((observable, oldValue, newValue) ->
                tab.setText(newValue ? "*" + editor.getFileName() : editor.getFileName())));

        getTabs().add(tab);

        if (needShow) {
            getSelectionModel().select(tab);
        }

        DictionaryUtils.runInWriteLock(getOpenedEditors(), editFile, tab, ObjectDictionary::put);
        ArrayUtils.runInWriteLock(getOpeningFiles(), editFile, Array::fastRemove);

        UIUtils.decrementLoading();

        var workspace = WORKSPACE_MANAGER.getCurrentWorkspace();
        if (workspace != null) {
            workspace.addOpenedFiles(editFile, editor);
        }
    }

    private void handleRequestToCloseEditor(FileEditor editor, Tab tab, Event event) {
        if (!editor.isDirty()) {
            return;
        }

        var question = Messages.EDITOR_AREA_SAVE_FILE_QUESTION
                .replace("%file_name%", editor.getFileName());

        var dialog = new ConfirmDialog(result -> {
            if (result == null) {
                return;
            }

            if (result) {
                editor.save(fileEditor -> getTabs().remove(tab));
            } else {
                getTabs().remove(tab);
            }
        }, question);

        dialog.show();
        event.consume();
    }

    public ConcurrentObjectDictionary<Path, Tab> getOpenedEditors() {
        return openedEditors;
    }

    public ConcurrentArray<Path> getOpeningFiles() {
        return openingFiles;
    }

    public boolean isIgnoredOpenedFiles() {
        return ignoredOpenedFiles;
    }

    public void setIgnoredOpenedFiles(boolean ignoredOpenedFiles) {
        this.ignoredOpenedFiles = ignoredOpenedFiles;
    }
}
