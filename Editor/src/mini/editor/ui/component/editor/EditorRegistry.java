package mini.editor.ui.component.editor;

import com.ss.rlib.common.util.FileUtils;
import com.ss.rlib.common.util.ObjectUtils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import com.ss.rlib.common.util.dictionary.DictionaryFactory;
import com.ss.rlib.common.util.dictionary.ObjectDictionary;
import mini.editor.ui.component.editor.impl.model.ModelFileEditor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

public class EditorRegistry {
    private static final EditorRegistry INSTANCE = new EditorRegistry();
    private static final String ALL_FORMATS = "*";

    private final ObjectDictionary<String, EditorDescription> editorIdToDescription;
    private final ObjectDictionary<String, Array<EditorDescription>> editorDescriptions;

    public EditorRegistry() {
        editorIdToDescription = DictionaryFactory.newObjectDictionary();
        editorDescriptions = DictionaryFactory.newObjectDictionary();
        loadDescriptions();
    }

    public static EditorRegistry getInstance() {
        return INSTANCE;
    }

    private void loadDescriptions() {
        //register(SceneFileEditor.DESCRIPTION);
        register(ModelFileEditor.DESCRIPTION);
    }

    private void register(final EditorDescription description) {
        final ObjectDictionary<String, Array<EditorDescription>> editorDescriptions
                = getEditorDescriptions();

        final Array<String> extensions = description.getExtensions();
        extensions.forEach(extension -> register(description, extension, editorDescriptions));

        final ObjectDictionary<String, EditorDescription> editorIdToDescription
                = getEditorIdToDescription();
        editorIdToDescription.put(description.getEditorId(), description);
    }

    private void register(final EditorDescription description,
                          final String extension,
                          final ObjectDictionary<String, Array<EditorDescription>> editorDescriptions) {
        final Array<EditorDescription> descriptions = editorDescriptions
                .get(extension, () -> ArrayFactory.newArray(EditorDescription.class));
        ObjectUtils.notNull(descriptions);
        descriptions.add(description);
    }

    public EditorDescription getDescription(final String editorId) {
        final ObjectDictionary<String, EditorDescription> editorIdToDescription
                = getEditorIdToDescription();
        return editorIdToDescription.get(editorId);
    }

    public ObjectDictionary<String, EditorDescription> getEditorIdToDescription() {
        return editorIdToDescription;
    }

    public ObjectDictionary<String, Array<EditorDescription>> getEditorDescriptions() {
        return editorDescriptions;
    }

    public FileEditor createEditorFor(Path file) {
        if (Files.isDirectory(file)) {
            return null;
        }

        final String extension = FileUtils.getExtension(file);
        final ObjectDictionary<String, Array<EditorDescription>> editorDescriptions
                = getEditorDescriptions();

        Array<EditorDescription> descriptions = editorDescriptions.get(extension);
        EditorDescription description;

        if (descriptions == null) {
            descriptions = editorDescriptions.get(ALL_FORMATS);
            description = descriptions == null ? null : descriptions.first();
        } else {
            description = descriptions.first();
        }

        if (description == null) {
            return null;
        }

        final Callable<FileEditor> constructor = description.getConstructor();
        try {
            return constructor.call();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public FileEditor createEditorFor(EditorDescription description) {
        final Callable<FileEditor> constructor = description.getConstructor();

        try {
            return constructor.call();
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
