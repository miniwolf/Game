package mini.renderEngine;

import mini.scene.Geometry;
import mini.scene.VertexBuffer;

/**
 * Created by miniwolf on 16-04-2017.
 */
public class RenderContext {
    /**
     * Currently bound element array vertex buffer.
     *
     * @see mini.entityRenderers.EntityRenderer#renderMeshFromGeometry(Geometry)
     */
    private int boundElementArrayVBO;

    /**
     * Vertex attribs currently bound and enabled. If a slot is null, then
     * it is disabled.
     */
    private VertexBuffer[] boundAttribs = new VertexBuffer[16];

    /**
     * IDList for vertex attributes
     */
    private IDList attribIndexList = new IDList();

    /**
     * Currently bound array vertex buffer.
     *
     * @see Renderer#renderMesh(Mesh, int, int)
     */
    private int boundArrayVBO;

    public int getBoundElementArrayVBO() {
        return boundElementArrayVBO;
    }

    public void setBoundElementArrayVBO(int boundElementArrayVBO) {
        this.boundElementArrayVBO = boundElementArrayVBO;
    }

    public VertexBuffer[] getBoundAttribs() {
        return boundAttribs;
    }

    public int getBoundArrayVBO() {
        return boundArrayVBO;
    }

    public void setBoundArrayVBO(int boundArrayVBO) {
        this.boundArrayVBO = boundArrayVBO;
    }

    public IDList getAttribIndexList() {
        return attribIndexList;
    }
}
