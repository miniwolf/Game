package mini.editor.file.converter.impl;

import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import mini.editor.FileExtensions;

public class FBXToMinioFileConverter {
    private static final Array<String> EXTENSIONS = ArrayFactory.newArray(String.class);

    static {
        EXTENSIONS.add(FileExtensions.MODEL_FBX);
        EXTENSIONS.asUnsafe().trimToSize();
    }
}
