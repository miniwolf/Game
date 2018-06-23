package mini.editor.ui.component.painting;

import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import mini.editor.annotation.FromAnyThread;
import mini.editor.annotation.FxThread;

import java.util.function.Function;

public class PaintingComponentRegistry {
    private static final PaintingComponentRegistry INSTANCE = new PaintingComponentRegistry();
    private final Array<Function<PaintingComponentContainer, PaintingComponent>> constructors;

    public PaintingComponentRegistry() {
        constructors = ArrayFactory.newArray(Function.class);
    }

    @FromAnyThread
    public static PaintingComponentRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Create all available painting components
     */
    @FxThread
    public Array<PaintingComponent> createComponents(
            PaintingComponentContainer container) {
        var result = ArrayFactory.<PaintingComponent>newArray(PaintingComponent.class);

        constructors.forEach(result, container,
                             (constructor, components, cont) -> components
                                     .add(constructor.apply(cont)));

        return result;
    }
}
