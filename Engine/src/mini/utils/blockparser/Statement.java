package mini.utils.blockparser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miniwolf on 07-05-2017.
 */
public class Statement {
    protected int lineNumber;
    protected String line;
    protected List<Statement> contents = new ArrayList<>();

    protected Statement(int lineNumber, String line) {
        this.lineNumber = lineNumber;
        this.line = line;
    }

    protected void addStatement(Statement statement) {
        contents.add(statement);
    }

    protected void addStatement(int index, Statement statement) {
        contents.add(index, statement);
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getLine() {
        return line;
    }

    public List<Statement> getContents() {
        return contents;
    }

    protected String getIndent(int indent) {
        return "                               ".substring(0, indent);
    }

    protected String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent(indent));
        sb.append(line);
        if (contents != null) {
            sb.append(" {\n");
            for (Statement statement : contents) {
                sb.append(statement.toString(indent + 4));
                sb.append("\n");
            }
            sb.append(getIndent(indent));
            sb.append("}");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return toString(0);
    }
}
