package mini.editor.manager;

import com.ss.rlib.common.util.Utils;
import com.ss.rlib.common.util.dictionary.DictionaryFactory;
import com.ss.rlib.common.util.dictionary.ObjectDictionary;
import mini.editor.annotation.FromAnyThread;
import mini.editor.config.EditorConfig;
import mini.editor.model.workspace.Workspace;
import mini.editor.util.EditorUtil;

import java.nio.file.Files;
import java.nio.file.Path;

public class WorkspaceManager {
    public static final String FOLDER_EDITOR = ".miniEditor";
    public static final String FILE_WORKSPACE = "workspace";

    private static WorkspaceManager instance;

    private final ObjectDictionary<Path, Workspace> workspaces;
    private Workspace currentWorkspace;

    public WorkspaceManager() {
        InitializationManager.valid(getClass());
        workspaces = DictionaryFactory.newObjectDictionary();
    }

    public static WorkspaceManager getInstance() {
        if (instance == null) {
            instance = new WorkspaceManager();
        }

        return instance;
    }

    @FromAnyThread
    public Workspace getCurrentWorkspace() {
        final EditorConfig editorConfig = EditorConfig.getInstance();
        final Path currentAsset = editorConfig.getCurrentAsset();
        if (currentAsset == null) {
            return null;
        }

        return getWorkspace(currentAsset);
    }

    public void setCurrentWorkspace(Workspace currentWorkspace) {
        this.currentWorkspace = currentWorkspace;
    }

    private synchronized Workspace getWorkspace(final Path assetFolder) {
        final ObjectDictionary<Path, Workspace> workspaces = getWorkspaces();
        final Workspace exists = workspaces.get(assetFolder);
        if (exists != null) {
            return exists;
        }

        final Path workspaceFile = assetFolder.resolve(FOLDER_EDITOR).resolve(FILE_WORKSPACE);

        if (!Files.exists(workspaceFile)) {
            final Workspace workspace = new Workspace();
            workspace.notifyRestored();
            workspace.setAssetFolder(assetFolder);
            workspaces.put(assetFolder, workspace);
            return workspace;
        }

        Workspace workspace;
        try {
            workspace = EditorUtil.deserialize(Utils.get(workspaceFile, Files::readAllBytes));
        } catch (final RuntimeException e) {
            workspace = new Workspace();
        }

        workspace.notifyRestored();
        workspace.setAssetFolder(assetFolder);
        workspaces.put(assetFolder, workspace);

        return workspace;
    }

    public ObjectDictionary<Path, Workspace> getWorkspaces() {
        return workspaces;
    }
}
