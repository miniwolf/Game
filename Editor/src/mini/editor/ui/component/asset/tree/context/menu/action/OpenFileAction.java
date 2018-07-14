package mini.editor.ui.component.asset.tree.context.menu.action;

import javafx.event.ActionEvent;
import mini.editor.Messages;
import mini.editor.annotation.FxThread;
import mini.editor.ui.component.asset.tree.resource.ResourceElement;
import mini.editor.ui.event.RequestedOpenFileEvent;

public class OpenFileAction extends FileAction {

    public OpenFileAction(ResourceElement resourceElement) {
        super(resourceElement);
    }

    @Override
    @FxThread
    protected String getName() {
        return Messages.ASSET_COMPONENT_RESOURCE_TREE_CONTEXT_MENU_OPEN_FILE;
    }

    @Override
    protected void execute(ActionEvent event) {
        FX_EVENT_MANAGER.notify(new RequestedOpenFileEvent(getElement().getFile()));
    }
}
