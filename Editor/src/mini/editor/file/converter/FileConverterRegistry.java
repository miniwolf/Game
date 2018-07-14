package mini.editor.file.converter;

public class FileConverterRegistry {
    private static FileConverterRegistry INSTANCE = new FileConverterRegistry();

    public static FileConverterRegistry getInstance() {
        return INSTANCE;
    }
}
