package mini.editor.ui.component.bar;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import mini.editor.Messages;
import mini.editor.ui.component.ScreenComponent;
import mini.editor.ui.component.bar.action.ExitAction;
import mini.editor.ui.component.bar.action.OpenAssetAction;
import mini.editor.ui.css.CssIds;

public class EditorMenuBarComponent extends MenuBar implements ScreenComponent {

    public EditorMenuBarComponent() {
        super();
        setId(CssIds.EDITOR_MENU_BAR_COMPONENT);
        createComponents();
    }

    private void createComponents() {
        getMenus().addAll(createFileMenu(),
                          createOtherMenu(),
                          createHelpMenu());
    }

    private Menu createHelpMenu() {
        var menu = new Menu(Messages.EDITOR_MENU_HELP);
        menu.getItems().addAll();
        return menu;
    }

    private Menu createOtherMenu() {
        var menu = new Menu(Messages.EDITOR_MENU_OTHER);
        menu.getItems().addAll();
        return menu;
    }

    private Menu createFileMenu() {
        var menu = new Menu(Messages.EDITOR_MENU_FILE);
        menu.getItems().addAll(new OpenAssetAction(),
                               new ExitAction());
        return menu;
    }
}
