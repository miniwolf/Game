package mini.editor.ui.control.property;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mini.editor.annotation.FromAnyThread;
import mini.editor.ui.css.CssClasses;

public abstract class PropertyControl extends VBox {

    public static final double CONTROL_WIDTH_PERCENTAGE = 0.4;
    public static final double CONTROL_WIDTH_PERCENTAGE_2 = 0.6;

    private String propertyName;
    private Label propertyNameLabel;

    public PropertyControl(String propertyName) {
        this.propertyName = propertyName;

        reload();
        createComponents();
    }

    private void createComponents() {
        setAlignment(isSingleRow() ? Pos.CENTER_RIGHT : Pos.CENTER);

        var container = new HBox();
        container.setAlignment(getAlignment());

        propertyNameLabel = new Label(getPropertyName() + ":");
        if (isSingleRow()) {
            propertyNameLabel.maxWidthProperty()
                             .bind(widthProperty().multiply(1F - CONTROL_WIDTH_PERCENTAGE));
        }

        container.getStyleClass().add(CssClasses.DEF_HBOX);
        propertyNameLabel.getStyleClass().add(getLabelCssClass());

        createComponents(container);
    }

    private String getLabelCssClass() {
        return isSingleRow()
               ? CssClasses.ABSTRACT_PARAM_CONTROL_PARAM_NAME_SINGLE_ROW
               : CssClasses.ABSTRACT_PARAM_CONTROL_PARAM_NAME;

    }

    protected abstract void createComponents(HBox container);

    protected abstract void reload();

    @FromAnyThread
    protected boolean isSingleRow() {
        return false;
    }

    public String getPropertyName() {
        return propertyName;
    }
}
