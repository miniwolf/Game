package mini.editor.ui.component.editor.impl;

import com.ss.rlib.common.util.FileUtils;
import com.ss.rlib.common.util.Utils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import mini.editor.MiniEditor;
import mini.editor.annotation.FxThread;
import mini.editor.manager.ExecutorManager;
import mini.editor.part3d.editor.Editor3DPart;
import mini.editor.ui.component.editor.EditorDescription;
import mini.editor.ui.component.editor.FileEditor;
import mini.editor.ui.event.FXEventManager;
import mini.editor.ui.event.impl.FileChangedEvent;
import mini.editor.util.ObjectsUtil;
import mini.editor.util.UIUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;
import java.util.function.Consumer;

public abstract class AbstractFileEditor<R extends Pane> implements FileEditor {

    protected static final FXEventManager FX_EVENT_MANAGER = FXEventManager.getInstance();

    protected static final ExecutorManager EXECUTOR_MANAGER = ExecutorManager.getInstance();

    private final EventHandler<FileChangedEvent> fileChangedEvent;
    private final Array<Editor3DPart> editor3DParts;
    private final BooleanProperty dirtyProperty;
    private boolean saving;
    private Consumer<FileEditor> saveCallback;
    /**
     * The edited file
     */
    private Path file;
    /**
     * The root element of this editor
     */
    private R root;
    /**
     * The time when this editor was shown
     */
    private volatile LocalTime showedTime;

    public AbstractFileEditor() {
        fileChangedEvent = this::processChangedFile;
        dirtyProperty = new SimpleBooleanProperty(this, "dirty", false);
        editor3DParts = ArrayFactory.newArray(Editor3DPart.class);
        createContent();
    }

    private void createContent() {
        final VBox container = new VBox();
        final StackPane page = new StackPane(container);
        page.setPickOnBounds(true);

        root = createRoot();

        createContent(root);

        container.getChildren().add(root);

        // TODO: Tool bar
        root.prefHeightProperty().bind(container.heightProperty());

        root.prefWidthProperty().bind(container.widthProperty());
    }

    protected abstract void createContent(R root);

    /**
     * @return created new root
     */
    protected abstract R createRoot();

    protected abstract void handleExternalChanges();

    /**
     * Save new changes
     *
     * @param toStore the file to store
     */
    protected abstract void doSave(Path toStore) throws IOException;

    @Override
    @FxThread
    public void save(final Consumer<FileEditor> callback) {
        if (isSaving()) {
            return;
        }

        this.saveCallback = callback;
        notifyStartSaving();

        EXECUTOR_MANAGER.addBackgroundTask(() -> {
            final EditorDescription description = getDescription();
            final String editorId = description.getEditorId();

            final Path tempFile = Utils
                    .get(editorId, prefix -> Files.createTempFile(prefix, "toSave.tmp"));
            final MiniEditor editorApplication = MiniEditor.getInstance();
            final long stamp = editorApplication.asyncLock();

            try {
                final Path editFile = getEditFile();
                doSave(tempFile);
                try (final OutputStream out = Files
                        .newOutputStream(editFile, StandardOpenOption.TRUNCATE_EXISTING)) {
                    Files.copy(tempFile, out);
                } finally {
                    FileUtils.delete(tempFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
                EXECUTOR_MANAGER.addFXTask(this::notifyFinishSaving);
            } finally {
                editorApplication.asyncUnlock(stamp);
            }
        });
    }

    /**
     * Handle a changed file
     */
    protected void processChangedFile(final FileChangedEvent event) {
        final Path file = event.getFile();
        final Path editFile = getEditFile();

        if (!file.equals(editFile)) {
            return;
        }

        if (isSaving()) {
            notifyFinishSaving();
            return;
        }

        handleExternalChanges();
    }

    /**
     * Add the new 3D part of this editor
     *
     * @param editorPart
     */
    @FxThread
    protected void addEditor3DPart(final Editor3DPart editorPart) {
        editor3DParts.add(editorPart);
    }

    @Override
    @FxThread
    public void openFile(final Path file) {
        FX_EVENT_MANAGER.addEventHandler(FileChangedEvent.EVENT_TYPE, getFileChangedHandler());

        this.file = file;
    }

    /**
     * @return the file changes listener
     */
    @FxThread
    private EventHandler<FileChangedEvent> getFileChangedHandler() {
        return fileChangedEvent;
    }

    @Override
    @FxThread
    public Pane getPage() {
        final R pane = ObjectsUtil.notNull(root);
        return (Pane) pane.getParent().getParent();
    }

    @Override
    @FxThread
    public Path getEditFile() {
        return ObjectsUtil.notNull(file);
    }

    @Override
    @FxThread
    public String getFileName() {
        final Path editFile = getEditFile();
        final Path fileName = editFile.getFileName();
        return fileName.toString();
    }

    @Override
    @FxThread
    public BooleanProperty dirtyProperty() {
        return dirtyProperty;
    }

    @Override
    @FxThread
    public boolean isDirty() {
        return dirtyProperty.get();
    }

    protected void setDirty(final boolean dirty) {
        this.dirtyProperty.setValue(dirty);
    }

    @Override
    @FxThread
    public Array<Editor3DPart> get3DStates() {
        return editor3DParts;
    }

    /**
     * @return whether a file is being saved
     */
    protected boolean isSaving() {
        return saving;
    }

    /**
     * Sets saving
     */
    protected void setSaving(final boolean saving) {
        this.saving = saving;
    }

    private void notifyStartSaving() {
        UIUtils.incrementLoading();
        setSaving(true);
    }

    private void notifyFinishSaving() {
        setSaving(false);
        UIUtils.decrementLoading();
        if (saveCallback != null) {
            saveCallback.accept(this);
            saveCallback = null;
        }
    }

    @Override
    public void notifyShowed() {
        showedTime = LocalTime.now();
    }
}
