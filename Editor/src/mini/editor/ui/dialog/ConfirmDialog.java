package mini.editor.ui.dialog;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mini.editor.Messages;
import mini.editor.annotation.FromAnyThread;
import mini.editor.ui.css.CssClasses;
import mini.editor.util.ObjectsUtil;

import java.util.function.Consumer;

public class ConfirmDialog extends AbstractSimpleEditorDialog {

    private final Consumer<Boolean> handler;
    private Label questionLabel;

    public ConfirmDialog(final Consumer<Boolean> handler, final String question) {
        this.handler = handler;
        final Label questionLabel = getQuestionLabel();
        questionLabel.setText(question);
    }

    @FromAnyThread
    private Label getQuestionLabel() {
        return ObjectsUtil.notNull(questionLabel);
    }

    @Override
    protected void createAdditionalActions(HBox container) {
        final Button closeButton = new Button(Messages.SIMPLE_DIALOG_BUTTON_CANCEL);
        closeButton.setOnAction(event -> processCancel());

        closeButton.getStyleClass().add(CssClasses.DIALOG_BUTTON);
        container.getChildren().add(closeButton);
    }

    private void processCancel() {
        super.processClose();
        handler.accept(null);
    }

    @Override
    protected void createContent(VBox root) {
        questionLabel = new Label();
        questionLabel.minWidthProperty().bind(widthProperty().multiply(0.9));

        root.getChildren().add(questionLabel);
        root.getStyleClass().add(CssClasses.CONFIRM_DIALOG);
    }

    @Override
    protected void createBeforeActions(HBox container) {
    }

    @Override
    protected void createContent(GridPane container) {
    }
}
