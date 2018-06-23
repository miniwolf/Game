package mini.editor.ui.component.log;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.function.Consumer;

public class OutputStreamWrapper extends PrintStream {
    private final Consumer<String> consumer;

    public OutputStreamWrapper(final OutputStream out,
                               final Consumer<String> consumer) {
        super(out);
        this.consumer = consumer;
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        consumer.accept(new String(buf, off, len));
        super.write(buf, off, len);
    }
}
