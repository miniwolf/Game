package mini.font;

import mini.material.Material;
import mini.scene.Geometry;
import mini.scene.Mesh;
import mini.scene.VertexBuffer;
import mini.textures.Texture2D;
import mini.utils.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.LinkedList;

/**
 * One page per BitmapText Font Texture.
 *
 * @author Lim, YongHoon
 */
class BitmapTextPage extends Geometry {

    private final float[] pos;
    private final float[] tc;
    private final short[] idx;
    private final byte[] color;
    private final int page;
    private final Texture2D texture;
    private final LinkedList<LetterQuad> pageQuads = new LinkedList<>();

    BitmapTextPage(BitmapFont font, int page) {
        super("BitmapFont", new Mesh());
        setRequiresUpdates(false);
        setBatchHint(BatchHint.Never);
        if (font == null) {
            throw new IllegalArgumentException("font cannot be null.");
        }

        this.page = page;

        Material mat = font.getPage(page);
        if (mat == null) {
            throw new IllegalStateException("The font's texture was not found!");
        }

        setMaterial(mat);
        this.texture = (Texture2D) mat.getTextureParam("ColorMap").getTextureValue();

        // initialize buffers
        Mesh m = getMesh();
        m.setBuffer(VertexBuffer.Type.Position, 3, new float[0]);
        m.setBuffer(VertexBuffer.Type.TexCoord, 2, new float[0]);
        m.setBuffer(VertexBuffer.Type.Color, 4, new byte[0]);
        m.setBuffer(VertexBuffer.Type.Index, 3, new short[0]);

        // scale colors from 0 - 255 range into 0 - 1
        m.getBuffer(VertexBuffer.Type.Color).setNormalized(true);

        pos = new float[4 * 3];  // 4 verticies * 3 floats
        tc = new float[4 * 2];  // 4 verticies * 2 floats
        idx = new short[2 * 3];  // 2 triangles * 3 indices
        color = new byte[4 * 4];   // 4 verticies * 4 bytes
    }

    BitmapTextPage(BitmapFont font) {
        this(font, 0);
    }

    Texture2D getTexture() {
        return texture;
    }

    @Override
    public BitmapTextPage clone() {
        BitmapTextPage clone = (BitmapTextPage) super.clone();
        //clone.mesh = mesh.deepClone();
        return clone;
    }

    // Here is where one might add JmeCloneable related stuff except
    // the old clone() method doesn't actually bother to clone anything.
    // The arrays and the pageQuads are shared across all BitmapTextPage
    // clones and it doesn't seem to bother anything.  That means the
    // fields could probably just as well be static... but this code is
    // all very fragile.  I'm not tipping that particular boat today. -pspeed

    void assemble(Letters quads) {
        pageQuads.clear();
        quads.rewind();

        while (quads.nextCharacter()) {
            if (quads.isPrintable()) {
                if (quads.getCharacterSetPage() == page) {
                    pageQuads.add(quads.getQuad());
                }
            }
        }

        Mesh m = getMesh();
        int vertCount = pageQuads.size() * 4;
        int triCount = pageQuads.size() * 2;

        VertexBuffer pb = m.getBuffer(VertexBuffer.Type.Position);
        VertexBuffer tb = m.getBuffer(VertexBuffer.Type.TexCoord);
        VertexBuffer ib = m.getBuffer(VertexBuffer.Type.Index);
        VertexBuffer cb = m.getBuffer(VertexBuffer.Type.Color);

        FloatBuffer fpb = (FloatBuffer) pb.getData();
        FloatBuffer ftb = (FloatBuffer) tb.getData();
        ShortBuffer sib = (ShortBuffer) ib.getData();
        ByteBuffer bcb = (ByteBuffer) cb.getData();

        // increase capacity of buffers as needed
        fpb.rewind();
        fpb = BufferUtils.ensureLargeEnough(fpb, vertCount * 3);
        fpb.limit(vertCount * 3);
        pb.updateData(fpb);

        ftb.rewind();
        ftb = BufferUtils.ensureLargeEnough(ftb, vertCount * 2);
        ftb.limit(vertCount * 2);
        tb.updateData(ftb);

        bcb.rewind();
        bcb = BufferUtils.ensureLargeEnough(bcb, vertCount * 4);
        bcb.limit(vertCount * 4);
        cb.updateData(bcb);

        sib.rewind();
        sib = BufferUtils.ensureLargeEnough(sib, triCount * 3);
        sib.limit(triCount * 3);
        ib.updateData(sib);

        m.updateCounts();

        // go for each quad and append it to the buffers
        if (pos != null) {
            for (int i = 0; i < pageQuads.size(); i++) {
                LetterQuad fq = pageQuads.get(i);
                fq.storeToArrays(pos, tc, idx, color, i);
                fpb.put(pos);
                ftb.put(tc);
                sib.put(idx);
                bcb.put(color);
            }
        } else {
            for (int i = 0; i < pageQuads.size(); i++) {
                LetterQuad fq = pageQuads.get(i);
                fq.appendPositions(fpb);
                fq.appendTexCoords(ftb);
                fq.appendIndices(sib, i);
                fq.appendColors(bcb);
            }
        }

        fpb.rewind();
        ftb.rewind();
        sib.rewind();
        bcb.rewind();

        //updateModelBound();
    }
}

