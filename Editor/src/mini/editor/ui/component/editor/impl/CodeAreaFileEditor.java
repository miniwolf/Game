package mini.editor.ui.component.editor.impl;

import com.ss.rlib.common.util.FileUtils;
import javafx.scene.layout.VBox;
import mini.editor.annotation.FxThread;
import mini.editor.ui.control.code.BaseCodeArea;
import mini.editor.ui.css.CssClasses;
import mini.editor.util.ObjectsUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class CodeAreaFileEditor extends AbstractFileEditor<VBox> {
    /**
     * The original content of the opened file.
     */
    private String originalContent;

    private BaseCodeArea codeArea;

    @Override
    protected void createContent(VBox root) {
        codeArea = createCodeArea();
        codeArea.textProperty()
                .addListener(((observable, oldValue, newValue) -> updateDirty(newValue)));
        codeArea.prefHeightProperty().bind(root.heightProperty());
        codeArea.prefWidthProperty().bind(root.widthProperty());

        root.getChildren().add(codeArea);
        codeArea.getStyleClass().add(CssClasses.TEXT_EDITOR_TEXT_AREA);
    }

    @Override
    protected VBox createRoot() {
        return new VBox();
    }

    @Override
    protected void handleExternalChanges() {
        final String newContent = FileUtils.read(getEditFile());

        final BaseCodeArea codeArea = getCodeArea();
        final String currentContent = codeArea.getText();
        codeArea.reloadContent(newContent);

        setOriginalContent(currentContent);
        updateDirty(newContent);
    }

    @Override
    protected void doSave(Path toStore) throws IOException {
        final BaseCodeArea codeArea = getCodeArea();
        final String newContent = codeArea.getText();

        try (final PrintWriter out = new PrintWriter(Files.newOutputStream(toStore))) {
            out.print(newContent);
        }
    }

    private void updateDirty(String newContent) {
        setDirty(!getOriginalContent().equals(newContent));
    }

    protected abstract BaseCodeArea createCodeArea();

    /**
     * @return the code area.
     */
    public BaseCodeArea getCodeArea() {
        return ObjectsUtil.notNull(codeArea);
    }

    /**
     * @return the original content of the opened file
     */
    private String getOriginalContent() {
        return ObjectsUtil.notNull(originalContent);
    }

    /**
     * @param originalContent the original content of the opened file.
     */
    @FxThread
    private void setOriginalContent(final String originalContent) {
        this.originalContent = originalContent;
    }
}
