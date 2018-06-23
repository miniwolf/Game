package mini.editor.ui.component.asset;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

/**
 * The toolbar of the {@link AssetComponent} with actions.
 */
public class AssetBarComponent extends HBox {

    public AssetBarComponent() {
        final Button refreshAction = new Button();
        // TODO: Icon and action

        getChildren().add(refreshAction);
    }
}
