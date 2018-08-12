package mini.editor.ui.control.property.builder.impl;

import javafx.scene.layout.VBox;
import mini.editor.annotation.FxThread;
import mini.editor.extension.property.EditableProperty;
import mini.editor.model.undo.editor.ModelChangeConsumer;
import mini.editor.ui.control.property.impl.LightElementModelPropertyControl;
import mini.editor.ui.control.property.impl.SpatialElementModelPropertyControl;
import mini.light.DirectionalLight;
import mini.scene.Spatial;

import java.util.List;

public class EditableModelObjectPropertyBuilder extends EditableObjectPropertyBuilder<ModelChangeConsumer>  {
    protected EditableModelObjectPropertyBuilder(Class<? extends ModelChangeConsumer> type) {
        super(type);
    }

    @Override
    @FxThread
    public void buildFor(
            VBox container,
            ModelChangeConsumer changeConsumer,
            EditableProperty<?, ?> description) {
        super.buildFor(container, changeConsumer, description);

        var type = description.getType();
        switch (type) {
            case DIRECTION_LIGHT_FROM_SCENE: {
                EditableProperty<DirectionalLight, ?> property = cast(description);

                var value = property.getValue();
                var propertyControl = new LightElementModelPropertyControl<DirectionalLight, EditableProperty<DirectionalLight, ?>>(
                        DirectionalLight.class,
                        value,
                        property.getName(),
                        changeConsumer);

                addControl(container, property, propertyControl);
                break;
            }
            case SPATIAL_FROM_SCENE:
                EditableProperty<Spatial, ?> property = cast(description);

                var value = property.getValue();
                var propertyControl = new SpatialElementModelPropertyControl<Spatial, EditableProperty<Spatial, ?>>(
                        Spatial.class,
                        value,
                        property.getName(),
                        changeConsumer);
                break;
            case NODE_FROM_SCENE:
        }
    }

    @Override
    protected List<EditableProperty<?, ?>> getProperties(Object object) {
        return null;
    }
}
