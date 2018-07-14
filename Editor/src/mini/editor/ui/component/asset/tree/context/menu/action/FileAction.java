package mini.editor.ui.component.asset.tree.context.menu.action;

import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import mini.editor.annotation.FxThread;
import mini.editor.ui.component.asset.tree.resource.ResourceElement;
import mini.editor.ui.event.FXEventManager;
import mini.editor.util.ObjectsUtil;

public abstract class FileAction extends MenuItem {

    protected static final FXEventManager FX_EVENT_MANAGER = FXEventManager.getInstance();

    /**
     * Action element
     */
    private final ResourceElement element;

    public FileAction(ResourceElement element) {
        this.element = element;

        setText(getName());
        setOnAction(this::execute);

        // TODO: Icon
    }

    protected abstract void execute(final ActionEvent event);

    /**
     * @return the name of this action
     */
    @FxThread
    protected String getName() {
        return "Unknown";
    }

    /**
     * @return the file element.
     */
    @FxThread
    protected ResourceElement getElement() {
        return ObjectsUtil.notNull(element);
    }
}
