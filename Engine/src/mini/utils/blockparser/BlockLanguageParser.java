package mini.utils.blockparser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class BlockLanguageParser {
    private List<Statement> statementStack = new ArrayList<>();
    private Statement lastStatement;
    private int lineNumber = 1;

    private BlockLanguageParser() {
    }

    private void reset() {
        statementStack.clear();
        statementStack.add(new Statement(0, "<root>"));
        lastStatement = null;
        lineNumber = 1;
    }

    private void pushStatement(StringBuilder buffer) {
        String content = buffer.toString().trim();
        if (content.length() > 0) {
            // push last statement onto the list
            lastStatement = new Statement(lineNumber, content);

            Statement parent = statementStack.get(statementStack.size() - 1);
            parent.addStatement(lastStatement);

            buffer.setLength(0);
        }
    }

    private void load(InputStream in) throws IOException {
        reset();

        Reader reader = new InputStreamReader(in, "UTF-8");

        StringBuilder buffer = new StringBuilder();
        boolean insideComment = false;
        char lastChar = '\0';

        while (true) {
            int ci = reader.read();
            char c = (char) ci;
            if (c == '\r') {
                continue;
            }
            if (insideComment && c == '\n') {
                insideComment = false;
            } else if (c == '/' && lastChar == '/') {
                buffer.deleteCharAt(buffer.length() - 1);
                insideComment = true;
                pushStatement(buffer);
                lastChar = '\0';
                lineNumber++;
            } else if (!insideComment) {
                if (ci == -1 || c == '{' || c == '}' || c == '\n' || c == ';') {
                    pushStatement(buffer);
                    lastChar = '\0';
                    if (c == '{') {
                        // push last statement onto the stack
                        statementStack.add(lastStatement);
                    } else if (c == '}') {
                        // pop statement from stack
                        statementStack.remove(statementStack.size() - 1);
                    } else if (c == '\n') {
                        lineNumber++;
                    } else if (ci == -1) {
                        break;
                    }
                } else {
                    buffer.append(c);
                    lastChar = c;
                }
            }
        }
    }

    public static List<Statement> parse(InputStream in) throws IOException {
        BlockLanguageParser parser = new BlockLanguageParser();
        parser.load(in);
        return parser.statementStack.get(0).getContents();
    }
}
