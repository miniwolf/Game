package mini.editor.ui.control.property.builder;

import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import javafx.scene.layout.VBox;
import mini.editor.model.undo.editor.ChangeConsumer;
import mini.editor.ui.control.property.builder.impl.GeometryPropertyBuilder;
import mini.editor.ui.control.property.builder.impl.MaterialPropertyBuilder;

public class PropertyBuilderRegistry {
    private static PropertyBuilderRegistry INSTANCE = new PropertyBuilderRegistry();
    private final Array<PropertyBuilder> builders;

    private PropertyBuilderRegistry() {
        builders = ArrayFactory.newArray(PropertyBuilder.class);
        register(MaterialPropertyBuilder.getInstance());
        register(GeometryPropertyBuilder.getInstance());
    }

    public static PropertyBuilderRegistry getInstance() {
        return INSTANCE;
    }

    private void register(PropertyBuilder builder) {
        builders.add(builder);
        builders.sort(PropertyBuilder::compareTo);
    }

    public void buildFor(Object object, Object parent, VBox container,
                         ChangeConsumer changeConsumer) {
        for (var builder : builders) {
            builder.buildFor(object, parent, container, changeConsumer);
        }
    }
}
