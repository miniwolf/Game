package mini.editor.ui.component.editor.scripting;

import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import com.ss.rlib.common.util.dictionary.DictionaryFactory;
import com.ss.rlib.common.util.dictionary.ObjectDictionary;
import javafx.scene.layout.GridPane;
import mini.scene.Spatial;

public class EditorScriptingComponent extends GridPane {
    private final ObjectDictionary<String, Object> variables;
    private final Array<String> imports;

    public EditorScriptingComponent(final Runnable applyHandler) {
        variables = DictionaryFactory.newObjectDictionary();
        imports = ArrayFactory.newArray(String.class);
    }

    public void addVariable(
            final String name,
            final Object value) {
        variables.put(name, value);
        addImport(value.getClass());
    }

    public void addImport(final Class<?> type) {
        final String name = type.getName();
        if (!imports.contains(name)) {
            imports.add(name);
        }
    }

    public void setExampleCode(final String example) {
        // TODO: implement GroovyEditor
    }

    public void buildHeader() {
        final StringBuilder result = new StringBuilder();
        imports.forEach(result, (type, stringBuilder) -> stringBuilder.append("import ").append(type).append('\n'));

        result.append('\n');

        variables.forEach((name, value) -> result.append(value.getClass().getSimpleName())
            .append(' ')
            .append(name)
            .append(" = load_")
            .append(name)
            .append("();\n"));

        //headerComponent.setCode(result.toString());
    }
}
