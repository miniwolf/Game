package mini.editor.util;

import com.ss.rlib.common.util.ClassUtils;
import com.ss.rlib.common.util.FileUtils;
import com.ss.rlib.common.util.array.Array;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import mini.editor.annotation.FxThread;
import mini.editor.ui.component.editor.state.impl.BaseEditorSceneEditorState;
import org.reactfx.util.TriConsumer;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public abstract class UIUtils {
    @FxThread
    public static void incrementLoading() {
        EditorUtil.getFXScene().incrementLoading();
    }

    @FxThread
    public static void decrementLoading() {
        EditorUtil.getFXScene().decrementLoading();
    }

    public static void acceptIfHasFile(final DragEvent dragEvent,
                                       final Array<String> extensions) {
        final Dragboard dragboard = dragEvent.getDragboard();
        if (!isHasFile(dragboard, extensions)) {
            return;
        }

        final Set<TransferMode> transferModes = dragboard.getTransferModes();
        final boolean isCopy = transferModes.contains(TransferMode.COPY);

        dragEvent.acceptTransferModes(isCopy ? TransferMode.COPY : TransferMode.MOVE);
        dragEvent.consume();
    }

    private static <ES extends BaseEditorSceneEditorState> boolean isHasFile(
            final Dragboard dragboard,
            final Array<String> extensions) {
        final List<File> files = ClassUtils.unsafeCast(dragboard.getContent(DataFormat.FILES));

        if (files == null || files.size() != 1) {
            return false;
        }

        final File file = files.get(0);
        final String extension = FileUtils.getExtension(file.getName(), true);

        return extensions.contains(extension);
    }

    public static <F, S> void handleDroppedFile(
            final DragEvent dragEvent,
            final String targetExtension,
            final F firstArg,
            final S secondArg,
            TriConsumer<F, S, Path> handler) {
        handleDroppedFile(dragEvent.getDragboard(), targetExtension, firstArg, secondArg, handler);
    }

    public static <F, S> void handleDroppedFile(
            final Dragboard dragboard,
            final String targetExtension,
            final F firstArg,
            final S secondArg,
            TriConsumer<F, S, Path> handler) {

        final List<File> files = ClassUtils.unsafeCast(dragboard.getContent(DataFormat.FILES));

        if (files == null || files.size() != 1) {
            return;
        }

        final File file = files.get(0);
        final String extension = FileUtils.getExtension(file.getName(), false);

        if (targetExtension.equalsIgnoreCase(extension)) {
            return;
        }

        handler.accept(firstArg, secondArg, file.toPath());
    }

    public static <T> TreeItem<T> findItemForValue(
            final TreeItem<T> root,
            final Object object) {
        if (object == null) {
            return null;
        } else if (Objects.equals(root.getValue(), object)) {
            return root;
        }

        final ObservableList<TreeItem<T>> children = root.getChildren();
        if (children.isEmpty()) {
            return null;
        }

        for (final TreeItem<T> treeItem: children) {
            final TreeItem<T> result = findItemForValue(treeItem, object);
            if (result != null) {
                return result;
            }
        }

        return null;
    }
}
