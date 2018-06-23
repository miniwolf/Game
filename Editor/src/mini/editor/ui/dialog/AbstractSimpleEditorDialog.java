package mini.editor.ui.dialog;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mini.editor.Messages;
import mini.editor.annotation.FromAnyThread;
import mini.editor.annotation.FxThread;
import mini.editor.ui.css.CssClasses;

public abstract class AbstractSimpleEditorDialog extends EditorDialog {
    private Button okButton;
    private Button closeButton;

    @Override
    @FxThread
    protected void createActions(VBox root) {
        final HBox container = new HBox();

        createBeforeActions(container);

        okButton = new Button(getButtonOKText());
        okButton.setOnAction(event -> processOK());
        okButton.getStyleClass().add(CssClasses.DIALOG_BUTTON);

        closeButton = new Button(getButtonCloseText());
        closeButton.setOnAction(event -> processClose());
        closeButton.getStyleClass().add(CssClasses.DIALOG_BUTTON);

        container.getChildren().addAll(okButton, closeButton);

        createAdditionalActions(container);

        if (!container.getChildren().isEmpty()) {
            root.getChildren().add(container);
            container.getStyleClass().add(CssClasses.DEF_HBOX);
        }
    }

    @FxThread
    protected void processOK() {
        hide();
    }

    @FxThread
    protected void processClose() {
        hide();
    }

    @FxThread
    protected abstract void createAdditionalActions(final HBox container);

    @FxThread
    protected abstract void createBeforeActions(final HBox container);

    @FxThread
    protected Button getOKButton() {
        return okButton;
    }

    @FxThread
    public Button getCloseButton() {
        return closeButton;
    }

    @FromAnyThread
    protected String getButtonOKText() {
        return Messages.SIMPLE_DIALOG_BUTTON_OK;
    }

    public String getButtonCloseText() {
        return Messages.SIMPLE_DIALOG_BUTTON_CLOSE;
    }
}
