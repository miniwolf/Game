package mini.editor.ui.component.bar.action;

import javafx.application.Platform;
import javafx.scene.control.MenuItem;
import mini.editor.Messages;

public class ExitAction extends MenuItem {

    public ExitAction() {
        super(Messages.EDITOR_MENU_FILE_EXIT);
        setOnAction(event -> process());
    }

    private void process() {
        Platform.exit();
    }
}
