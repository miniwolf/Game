package mini.renderEngine.opengl;

import mini.entityRenderers.EntityShader;
import mini.renderEngine.RenderContext;
import mini.scene.Geometry;
import mini.scene.Mesh;
import mini.scene.VertexBuffer;
import mini.shaders.Attribute;
import mini.shaders.ShaderProgram;
import mini.textures.Texture;
import mini.utils.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL33;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * @author miniwolf
 */
public class GLRenderer {
    private final IntBuffer intBuf1 = BufferUtils.createIntBuffer(1);
    private final RenderContext context = new RenderContext();
    private ShaderProgram shader;

    private int convertFormat(VertexBuffer.Format format) {
        switch (format) {
            case Byte:
                return GL11.GL_BYTE;
            case UnsignedByte:
                return GL11.GL_UNSIGNED_BYTE;
            case Short:
                return GL11.GL_SHORT;
            case UnsignedShort:
                return GL11.GL_UNSIGNED_SHORT;
            case Int:
                return GL11.GL_INT;
            case UnsignedInt:
                return GL11.GL_UNSIGNED_INT;
            case Float:
                return GL11.GL_FLOAT;
            case Double: // TODO: Not sure this is viable on a GPU
                return GL11.GL_DOUBLE;
            default:
                throw new UnsupportedOperationException("Unknown buffer format.");
        }
    }

    private int convertUsage(VertexBuffer.Usage usage) {
        switch (usage) {
            case Static:
                return GL15.GL_STATIC_DRAW;
            case Dynamic:
                return GL15.GL_DYNAMIC_DRAW;
            case Stream:
                return GL15.GL_STREAM_DRAW;
            default:
                throw new UnsupportedOperationException("Unknown usage type.");
        }
    }

    public int convertElementMode(Mesh.Mode mode) {
        switch (mode) {
            case Triangles:
                return GL11.GL_TRIANGLES;
            default:
                throw new UnsupportedOperationException("Unrecognized mesh mode: " + mode);
        }
    }

    public void renderMeshFromGeometry(Geometry geometry) {
        Mesh mesh = geometry.getMesh();
        VertexBuffer indices = mesh.getBuffer(VertexBuffer.Type.Index);

        for (VertexBuffer vb : mesh.getBufferList()) {
            if (vb.getBufferType() == VertexBuffer.Type.Index) {
                continue;
            }
            setVertexAttrib(vb);
        }

        if (indices != null) {
            drawTriangleList(indices, mesh);
        } else {
            drawTriangleArray(mesh.getMode(), mesh.getVertexCount());
        }
    }

    public void drawTriangleArray(Mesh.Mode mode, int vertCount) {
        GL11.glDrawArrays(convertElementMode(mode), 0, vertCount);
    }

    public void drawTriangleList(VertexBuffer indexBuf, Mesh mesh) {
        if (indexBuf.getBufferType() != VertexBuffer.Type.Index) {
            throw new IllegalArgumentException("Only index buffers are allowed as triangle lists.");
        }

        // TODO: Maybe check for supported indexBuf.FORMAT unsignedInt may not be supported always

        if (indexBuf.isUpdateNeeded()) {
            updateBufferData(indexBuf);
        }

        int bufId = indexBuf.getId();
        assert bufId != -1;

        if (context.getBoundElementArrayVBO() != bufId) {
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, bufId);
            context.setBoundElementArrayVBO(bufId);
        }

        int vertCount = mesh.getVertexCount();
        GL12.glDrawRangeElements(convertElementMode(mesh.getMode()),
                                 0,
                                 vertCount,
                                 indexBuf.getData().limit(),
                                 convertFormat(indexBuf.getFormat()),
                                 0);
    }

    private void setVertexAttrib(VertexBuffer vb) {
        if (vb.getBufferType() == VertexBuffer.Type.Index) {
            throw new IllegalArgumentException("Index buffers not allowed to be set to vertex attrib");
        }

        Attribute attrib = shader.getAttribute(vb.getBufferType());
        int loc = attrib.getLocation();
        if (loc == -1) {
            return; // not defined
        }
        if (loc == -2) {
            loc = GL20.glGetAttribLocation(shader.getId(), "in" + vb.getBufferType().name());

            // not really the name of it in the shader (inPosition) but
            // the internal name of the enum (Position).
            if (loc < 0) {
                attrib.setLocation(-1);
                return; // not available in shader.
            } else {
                attrib.setLocation(loc);
            }
        }

        int slotsRequired = 1;
        if (vb.getNumComponents() > 4) {
            if (vb.getNumComponents() % 4 != 0) {
                throw new RuntimeException("Number of components in multi-slot "
                                           + "buffers must be divisible by 4");
            }
            slotsRequired = vb.getNumComponents() / 4;
        }

        if (vb.isUpdateNeeded()) {
            updateBufferData(vb);
        }

        VertexBuffer[] attribs = context.getBoundAttribs();
        for (int i = 0; i < slotsRequired; i++) {
            if (!context.getAttribIndexList().moveToNew(loc + i)) {
                GL20.glEnableVertexAttribArray(loc + i);
            }
        }
        if (attribs[loc] != vb) {
            // NOTE: Use id from interleaved buffer if specified
            int bufId = vb.getId();
            assert bufId != -1;
            if (context.getBoundArrayVBO() != bufId) {
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufId);
                context.setBoundArrayVBO(bufId);
            }

            if (slotsRequired == 1) {
                GL20.glVertexAttribPointer(loc,
                                           vb.getNumComponents(),
                                           convertFormat(vb.getFormat()),
                                           vb.isNormalized(),
                                           vb.getStride(),
                                           vb.getOffset());
            } else {
                for (int i = 0; i < slotsRequired; i++) {
                    // The pointer maps the next 4 floats in the slot.
                    // E.g.
                    // P1: XXXX____________XXXX____________
                    // P2: ____XXXX____________XXXX________
                    // P3: ________XXXX____________XXXX____
                    // P4: ____________XXXX____________XXXX
                    // stride = 4 bytes in float * 4 floats in slot * num slots
                    // offset = 4 bytes in float * 4 floats in slot * slot index
                    GL20.glVertexAttribPointer(loc + i,
                                               4,
                                               convertFormat(vb.getFormat()),
                                               vb.isNormalized(),
                                               4 * 4 * slotsRequired,
                                               4 * 4 * i);
                }
            }

            for (int i = 0; i < slotsRequired; i++) {
                int slot = loc + i;
                if (vb.isInstanced() && (attribs[slot] == null || !attribs[slot].isInstanced())) {
                    // non-instanced -> instanced
                    GL33.glVertexAttribDivisor(slot, vb.getInstanceSpan());
                }
                attribs[slot] = vb;
            }
        }
    }

    private void updateBufferData(VertexBuffer vb) {
        int bufId = vb.getId();
        if (bufId == -1) {
            // create buffer
            GL15.glGenBuffers(intBuf1);
            bufId = intBuf1.get(0);
            vb.setId(bufId);
        }

        // bind buffer
        int target;
        if (vb.getBufferType() == VertexBuffer.Type.Index) {
            target = GL15.GL_ELEMENT_ARRAY_BUFFER;
            if (context.getBoundElementArrayVBO() != bufId) {
                GL15.glBindBuffer(target, bufId);
                context.setBoundElementArrayVBO(bufId);
            }
        } else {
            target = GL15.GL_ARRAY_BUFFER;
            if (context.getBoundArrayVBO() != bufId) {
                GL15.glBindBuffer(target, bufId);
                context.setBoundArrayVBO(bufId);
            }
        }

        int usage = convertUsage(vb.getUsage());
        vb.getData().rewind();

        switch (vb.getFormat()) {
            case Byte:
            case UnsignedByte:
                GL15.glBufferData(target, (ByteBuffer) vb.getData(), usage);
                break;
            case Short:
            case UnsignedShort:
                GL15.glBufferData(target, (ShortBuffer) vb.getData(), usage);
                break;
            case Int:
            case UnsignedInt:
                GL15.glBufferData(target, (IntBuffer) vb.getData(), usage);
                break;
            case Float:
                GL15.glBufferData(target, (FloatBuffer) vb.getData(), usage);
                break;
            default:
                throw new UnsupportedOperationException("Unknown buffer format.");
        }

        vb.clearUpdateNeeded();
    }

    public void setShader(ShaderProgram shader) {
        this.shader = shader;
    }

    public ShaderProgram getShader() {
        return shader;
    }

    public void setTexture(int unit, Texture tex) {
        tex.bindToUnit(unit);
    }
}
