package mini.editor.util;

import com.ss.rlib.common.util.ref.ReferenceFactory;
import com.ss.rlib.common.util.ref.ReferenceType;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

public class ExtObjectInputStream extends ObjectInputStream {
    public ExtObjectInputStream(InputStream in) throws IOException {
        super(in);
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc)
            throws IOException, ClassNotFoundException {
        try {
            return super.resolveClass(desc);
        } catch (ClassNotFoundException e) {
            var name = desc.getName();

            try (var ref = ReferenceFactory.takeFromTLPool(ReferenceType.OBJECT)) {

                // TODO: Maybe handle this with a custom loader through a PluginManager?
                // plugin.getClassLoader, classLoader.loadClass(name)
                // if expection thrown, goto next.

                if (ref.getObject() != null) {
                    return (Class<?>) ref.getObject();
                }
            }
            throw e;
        }

    }
}
