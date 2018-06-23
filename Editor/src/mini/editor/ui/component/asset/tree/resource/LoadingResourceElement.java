package mini.editor.ui.component.asset.tree.resource;

import java.nio.file.Paths;

public class LoadingResourceElement extends ResourceElement {
    private static final LoadingResourceElement INSTANCE = new LoadingResourceElement();

    public LoadingResourceElement() {
        super(Paths.get("./"));
    }

    public static LoadingResourceElement getInstance() {
        return INSTANCE;
    }

    @Override
    public String toString() {
        return "LoadingResourceElement";
    }
}
