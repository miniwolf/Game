package mini.editor.ui.component.log;

import mini.editor.annotation.FromAnyThread;
import mini.editor.ui.css.CssIds;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * The view for showing log message from the editor
 */
public class LogView extends CodeArea {
    private static final LogView INSTANCE = new LogView();
    private static final int MAX_LENGTH = 8000;
    private final StringBuilder currentLog;

    public LogView() {
        currentLog = new StringBuilder();

        setId(CssIds.LOG_VIEW);
        setWrapText(true);
        setEditable(false);
        richChanges().filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
                     .subscribe(change -> setStyleSpans(0, computeHighLighting(getText())));

        System.setErr(new OutputStreamWrapper(System.err, externalAppendText()));
    }

    public static LogView getInstance() {
        return INSTANCE;
    }

    @FromAnyThread
    private Consumer<String> externalAppendText() {
        return this::appendLog;
    }

    /**
     * Update log content
     *
     * @param text the new information
     */
    @FromAnyThread
    private void appendLog(final String text) {
        final int resultLength = currentLog.length() + text.length();

        if (resultLength <= MAX_LENGTH) {
            currentLog.append(text);
        } else {
            final int toRemove = resultLength - MAX_LENGTH;
            currentLog.delete(0, toRemove);
            currentLog.append(text);
        }
    }

    private StyleSpans<? extends Collection<String>> computeHighLighting(final String text) {
        return null;
    }
}
