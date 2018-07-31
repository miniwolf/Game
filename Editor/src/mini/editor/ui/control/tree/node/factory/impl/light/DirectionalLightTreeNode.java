package mini.editor.ui.control.tree.node.factory.impl.light;

import mini.editor.Messages;
import mini.editor.ui.control.tree.node.impl.light.LightTreeNode;
import mini.light.DirectionalLight;

public class DirectionalLightTreeNode extends LightTreeNode<DirectionalLight> {
    public DirectionalLightTreeNode(
            final DirectionalLight element,
            final long objectId) {
        super(element, objectId);
    }

    @Override
    public String getName() {
        final DirectionalLight element = getElement();
        final String name = element.getName();
        return name == null || name.isEmpty() ? Messages.MODEL_FILE_EDITOR_NODE_DIRECTION_LIGHT :
               name;
    }
}
