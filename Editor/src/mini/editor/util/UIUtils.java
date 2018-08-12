package mini.editor.util;

import com.ss.rlib.common.util.ClassUtils;
import com.ss.rlib.common.util.FileUtils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.EventTarget;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import mini.editor.annotation.FxThread;
import mini.editor.ui.component.editor.state.impl.BaseEditorSceneEditorState;
import org.reactfx.util.TriConsumer;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public abstract class UIUtils {

    private static final PseudoClass FOCUSED_PSEUDO_CLASS = PseudoClass.getPseudoClass("focused");

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
            final TreeView<T> treeView,
            final Object object) {
        return findItemForValue(treeView.getRoot(), object);
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

    /**
     * Collect all elements of a tree item.
     *
     * @return the list containing all items.
     */
    @FxThread
    public static <T> Stream<TreeItem<T>> allItems(TreeItem<T> root) {
        Array<TreeItem<T>> container = ArrayFactory.newArray(TreeItem.class);
        collectAllItems(container, root);
        return container.stream();
    }

    private static <T> void collectAllItems(Array<TreeItem<T>> container, TreeItem<T> root) {
        container.add(root);

        final ObservableList<TreeItem<T>> children = root.getChildren();

        for (final TreeItem<T> child : children) {
            collectAllItems(container, child);
        }
    }

    public static void updateEditedCell(Labeled cell) {
        final Node graphic = cell.getGraphic();
        if (graphic instanceof HBox) {
            final HBox hBox = (HBox) graphic;
            hBox.setAlignment(Pos.CENTER_LEFT);
            hBox.setMinHeight(cell.getMinHeight());
        }
    }

    public static BooleanProperty addFocusBinding(Pane pane, Control... controls) {
        var focused = new BooleanPropertyBase(true) {
            @Override
            protected void invalidated() {
                pane.pseudoClassStateChanged(FOCUSED_PSEUDO_CLASS, get());
            }

            @Override
            public Object getBean() {
                return pane;
            }

            @Override
            public String getName() {
                return "focused";
            }
        };

        ChangeListener<Boolean> listener = (observable, oldValue, newValue) ->
                focused.setValue(newValue || Arrays.stream(controls).anyMatch(Node::isFocused));

        for (var control : controls) {
            control.focusedProperty().addListener(listener);
            control.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> control.requestFocus());
        }

        focused.setValue(Arrays.stream(controls).anyMatch(Node::isFocused));

        return focused;
    }

    public static boolean isNotHotKey(final KeyEvent event) {
        if (event == null) {
            return false;
        }

        final String text = event.getText();
        if (text.isEmpty()) return false;

        final KeyCode code = event.getCode();
        final EventTarget target = event.getTarget();

        if (code == KeyCode.TAB && !(target instanceof TextInputControl)) {
            return false;
        }

        if (event.isControlDown()) {
            return false;
        } else {
            return !event.isShiftDown();
        }
    }
}
