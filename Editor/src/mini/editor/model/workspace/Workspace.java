package mini.editor.model.workspace;

import com.ss.rlib.common.util.ClassUtils;
import com.ss.rlib.common.util.ObjectUtils;
import com.ss.rlib.common.util.StringUtils;
import mini.editor.annotation.FromAnyThread;
import mini.editor.annotation.FxThread;
import mini.editor.manager.WorkspaceManager;
import mini.editor.ui.component.editor.EditorDescription;
import mini.editor.ui.component.editor.FileEditor;
import mini.editor.ui.component.editor.state.EditorState;
import mini.editor.util.EditorUtil;
import mini.editor.util.ObjectsUtil;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class Workspace implements Serializable {
    private final AtomicInteger changes;

    private volatile Map<String, String> openedFiles;
    private volatile transient Path assetFolder;
    private volatile String currentEditedFile;
    private volatile Map<String, EditorState> editorStateMap;

    public Workspace() {
        changes = new AtomicInteger();
    }

    /**
     * Get the path to the file from the asset folder.
     *
     * @return the relative path.
     */
    @FromAnyThread
    private static Path getAssetFile(Path assetFolder, Path file) {
        return assetFolder.relativize(file);
    }

    public synchronized Path getAssetFolder() {
        return ObjectUtils.notNull(assetFolder);
    }

    public void setAssetFolder(final Path assetFolder) {
        this.assetFolder = assetFolder;
    }

    @FromAnyThread
    public synchronized void notifyRestored() {
        if (openedFiles == null) {
            openedFiles = new HashMap<>();
        }

        if (editorStateMap == null) {
            editorStateMap = new HashMap<>();
        } else {
            editorStateMap.forEach((key, editorState) ->
                                           editorState.setChangeHandler(this::incrementChanges));
        }
    }

    public synchronized String getCurrentEditedFile() {
        return currentEditedFile;
    }

    public synchronized void updateCurrentEditedFile(final Path file) {
        if (file == null) {
            this.currentEditedFile = null;
            return;
        }

        final Path assetFile = getAssetFile(getAssetFolder(), file);
        this.currentEditedFile = EditorUtil.toAssetPath(assetFile);
    }

    /**
     * @return the table of opened files.
     */
    public synchronized Map<String, String> getOpenedFiles() {
        return ObjectUtils.notNull(openedFiles);
    }

    @FromAnyThread
    public void addOpenedFiles(final Path file,
                               final FileEditor fileEditor) {
        final Path assetFile = getAssetFile(getAssetFolder(), file);
        final String assetPath = EditorUtil.toAssetPath(assetFile);

        final EditorDescription description = fileEditor.getDescription();

        final Map<String, String> openedFiles = getOpenedFiles();
        final String previous = openedFiles.put(assetPath, description.getEditorId());
        if (StringUtils.equals(previous, description.getEditorId())) {
            return;
        }

        incrementChanges();
    }

    @FromAnyThread
    private void incrementChanges() {
        changes.incrementAndGet();
    }

    /**
     * @param <T>          type of the editor state
     * @param file         the edited file
     * @param stateFactory the state factory
     * @return the state of the editor
     */
    @FromAnyThread
    public synchronized <T extends EditorState> T getEditorState(
            final Path file,
            final Supplier<EditorState> stateFactory) {
        final Path assetFile = getAssetFile(getAssetFolder(), file);
        final String assetPath = EditorUtil.toAssetPath(assetFile);

        final Map<String, EditorState> editorStateMap = getEditorStateMap();

        if (!editorStateMap.containsKey(assetPath)) {
            final EditorState editorState = stateFactory.get();
            editorState.setChangeHandler(this::incrementChanges);
            editorStateMap.put(assetPath, editorState);
            incrementChanges();
        }

        return ClassUtils.unsafeCast(editorStateMap.get(assetPath));
    }

    /**
     * @return the table with states of editors.
     */
    @FxThread
    private synchronized Map<String, EditorState> getEditorStateMap() {
        return ObjectsUtil.notNull(editorStateMap);
    }

    public void save(boolean force) {
        if (!force && changes.get() == 0) {
            return;
        }

        final Path assetFolder = getAssetFolder();
        if (!Files.exists(assetFolder)) {
            return;
        }

        final Path workspaceFile = assetFolder.resolve(WorkspaceManager.FOLDER_EDITOR)
                                              .resolve(WorkspaceManager.FILE_WORKSPACE);

        try {
            if (!Files.exists(workspaceFile)) {
                Files.createDirectories(workspaceFile.getParent());
                Files.createFile(workspaceFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            final Boolean hidden = (Boolean) Files
                    .getAttribute(workspaceFile,
                                  "dos:hidden",
                                  LinkOption.NOFOLLOW_LINKS);

            if (hidden != null && !hidden) {
                Files.setAttribute(workspaceFile, "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        changes.set(0);
        try {
            Files.write(workspaceFile, EditorUtil.serialize(this));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FromAnyThread
    public synchronized void removeOpenedFile(final Path file) {
        final Path assetFile = getAssetFile(getAssetFolder(), file);
        final String assetPath = EditorUtil.toAssetPath(assetFile);

        final Map<String, String> openedFiles = getOpenedFiles();
        openedFiles.remove(assetPath);

        incrementChanges();
    }
}
