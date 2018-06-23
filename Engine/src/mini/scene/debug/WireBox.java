package mini.scene.debug;

import mini.bounding.BoundingBox;
import mini.scene.Geometry;
import mini.scene.Mesh;
import mini.scene.VertexBuffer;
import mini.utils.BufferUtils;

import java.nio.FloatBuffer;

public class WireBox extends Mesh {
    public WireBox(float xExt, float yExt, float zExt) {
        updatePositions(xExt, yExt, zExt);
        setBuffer(VertexBuffer.Type.Index, 2,
                  new short[]{
                          0, 1,
                          1, 2,
                          2, 3,
                          3, 0,

                          4, 5,
                          5, 6,
                          6, 7,
                          7, 4,

                          0, 4,
                          1, 5,
                          2, 6,
                          3, 7
                  });
        setMode(Mode.Lines);
        updateCounts();
    }

    /**
     * Create a geometry suitable for visualizing the specified bounding box.
     *
     * @param bbox the bounding box (not null)
     * @return a new Geometry instance in world space
     */
    public static Geometry makeGeometry(BoundingBox bbox) {
        float xExtent = bbox.getXExtent();
        float yExtent = bbox.getYExtent();
        float zExtent = bbox.getZExtent();

        WireBox mesh = new WireBox(xExtent, yExtent, zExtent);
        Geometry result = new Geometry("Bounding Box", mesh);
        result.setLocalTranslation(bbox.getCenter());
        return result;
    }

    private void updatePositions(float xExt, float yExt, float zExt) {
        VertexBuffer pvb = getBuffer(VertexBuffer.Type.Position);
        FloatBuffer pb;
        if (pvb == null) {
            pvb = new VertexBuffer(VertexBuffer.Type.Position);
            pb = BufferUtils.createVector3Buffer(8);
            pvb.setupData(VertexBuffer.Usage.Dynamic, 3, VertexBuffer.Format.Float, pb);
            setBuffer(pvb);
        } else {
            pb = (FloatBuffer) pvb.getData();
            pvb.updateData(pb); // Am I being stupid here? Is the data already there?
        }
        pb.rewind();
        pb.put(new float[]{
                -xExt, -yExt, zExt,
                xExt, -yExt, zExt,
                xExt, yExt, zExt,
                -xExt, yExt, zExt,

                -xExt, -yExt, -zExt,
                xExt, -yExt, -zExt,
                xExt, yExt, -zExt,
                -xExt, yExt, -zExt,
                }
              );
        updateBound();
    }
}
