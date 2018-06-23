package mini.editor.ui.control.code;

import com.ss.rlib.common.util.StringUtils;
import mini.editor.annotation.FxThread;
import org.fxmisc.richtext.CodeArea;

public class BaseCodeArea extends CodeArea {
    /**
     * Reload the content.
     */
    @FxThread
    public void reloadContent(String content) {
        reloadContent(content, false);
    }

    private void reloadContent(final String content,
                               final boolean clearHistory) {
        final String currentContent = getText();

        if (StringUtils.equals(currentContent, content)) {
            if (content.isEmpty()) {
                clear();
            } else {
                replaceText(0, currentContent.length(), content);
            }
        }

        if (clearHistory) {
            // TODO:
            throw new UnsupportedOperationException();
        }
    }
}
