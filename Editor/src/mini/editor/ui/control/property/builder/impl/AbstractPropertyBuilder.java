package mini.editor.ui.control.property.builder.impl;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import mini.editor.annotation.FxThread;
import mini.editor.model.undo.editor.ChangeConsumer;
import mini.editor.ui.control.property.builder.PropertyBuilder;
import mini.editor.ui.css.CssClasses;

public abstract class AbstractPropertyBuilder<C extends ChangeConsumer> implements PropertyBuilder {

    /**
     * The change consumer type
     */
    private final Class<? extends C> type;

    protected AbstractPropertyBuilder(Class<? extends C> type) {
        this.type = type;
    }

    @Override
    public void buildFor(
            Object object,
            Object parent,
            VBox container,
            ChangeConsumer changeConsumer) {
        if (type.isInstance(changeConsumer)) {
            buildForImpl(object, parent, container, type.cast(changeConsumer));
        }
    }

    protected abstract void buildForImpl(Object object, Object parent,
                                         VBox container, C changeConsumer);

    @FxThread
    protected void buildSplitLine(Pane pane) {
        var line = new HBox();
        var container = new VBox(line);

        line.getStyleClass().add(CssClasses.DEF_HBOX);
        container.getStyleClass().add(CssClasses.PROPERTY_EDITOR_CONTAINER_SPLIT_LINE);

        pane.getChildren().add(container);
    }
}
