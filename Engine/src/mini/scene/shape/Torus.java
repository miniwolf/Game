package mini.scene.shape;

import mini.math.FastMath;
import mini.math.Vector3f;
import mini.scene.Mesh;
import mini.scene.VertexBuffer;
import mini.utils.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * An ordinary (single holed) torus.
 * <p>
 * The center is by default the origin.
 */
public class Torus extends Mesh {
    private int circleSamples;
    private int radialSamples;
    private float innerRadius;
    private float outerRadius;

    /**
     * Constructs a new Torus. Center is the origin, but the Torus may be transformed.
     *
     * @param circleSamples The number of samples along the circles.
     * @param radialSamples The number of samples along the radial.
     * @param innerRadius   Minor radius of the torus.
     * @param outerRadius   Major radius of the torus.
     */
    public Torus(int circleSamples, int radialSamples, float innerRadius, float outerRadius) {
        super();
        updateGeometry(circleSamples, radialSamples, innerRadius, outerRadius);
    }

    public void updateGeometry(int circleSamples, int radialSamples, float innerRadius,
                               float outerRadius) {

        this.circleSamples = circleSamples;
        this.radialSamples = radialSamples;
        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
        setGeometryData();
        setIndexData();
        updateBound();
        updateCounts();
    }

    private void setIndexData() {
        int triCount = 2 * circleSamples * radialSamples;

        ShortBuffer indexBuffer = BufferUtils.createShortBuffer(3 * triCount);
        setBuffer(VertexBuffer.Type.Index, 3, indexBuffer);

        int i;

        // Connectivity
        int connectionStart = 0;
        int index = 0;
        for (int circleCount = 0; circleCount < circleSamples; circleCount++) {
            int i0 = connectionStart;
            int i1 = i0 + 1;
            connectionStart += radialSamples + 1;
            int i2 = connectionStart;
            int i3 = i2 + 1;
            for (i = 0; i < radialSamples; i++, index += 6) {
                indexBuffer.put((short) i0++).put((short) i2).put((short) i1);
                indexBuffer.put((short) i1++).put((short) i2++).put((short) i3++);
            }
        }
    }

    private void setGeometryData() {
        int vertCount = (circleSamples + 1) * (radialSamples + 1);

        FloatBuffer floatPositionBuffer = BufferUtils.createVector3Buffer(vertCount);
        setBuffer(VertexBuffer.Type.Position, 3, floatPositionBuffer);

        FloatBuffer floatNormalBuffer = BufferUtils.createVector3Buffer(vertCount);
        setBuffer(VertexBuffer.Type.Normal, 3, floatNormalBuffer);

        FloatBuffer floatTextureBuffer = BufferUtils.createVector2Buffer(vertCount);
        setBuffer(VertexBuffer.Type.TexCoord, 2, floatTextureBuffer);

        int i = 0;
        // Generate geometry
        float inverseCircleSamples = 1.0f / circleSamples;
        float inverseRadialSamples = 1.0f / radialSamples;
        Vector3f radialAxis = new Vector3f(), torusMiddle = new Vector3f(), tempNormal
                = new Vector3f();
        for (int circleCount = 0; circleCount < circleSamples; circleCount++) {
            // Compute center point
            float circleFraction = circleCount * inverseCircleSamples;
            float theta = FastMath.TWO_PI * circleFraction;
            float cosTheta = FastMath.cos(theta);
            float sinTheta = FastMath.sin(theta);
            radialAxis.set(cosTheta, sinTheta, 0);
            radialAxis.mult(outerRadius, torusMiddle);

            int iSave = i;
            for (int radialCount = 0; radialCount < radialSamples; radialCount++) {
                float radialFraction = radialCount * inverseRadialSamples;
                float phi = FastMath.TWO_PI * radialFraction;
                float cosPhi = FastMath.cos(phi);
                float sinPhi = FastMath.sin(phi);
                tempNormal.set(radialAxis).multLocal(cosPhi);
                tempNormal.z += sinPhi;
                floatNormalBuffer.put(tempNormal.x).put(tempNormal.y).put(tempNormal.z);
                tempNormal.multLocal(innerRadius).addLocal(torusMiddle);
                floatPositionBuffer.put(tempNormal.x).put(tempNormal.y).put(tempNormal.z);

                floatTextureBuffer.put(radialFraction).put(circleFraction);
                i++;
            }
            BufferUtils.copyInternalVector3(floatPositionBuffer, iSave, i);
            BufferUtils.copyInternalVector3(floatNormalBuffer, iSave, i);

            floatTextureBuffer.put(1.0f).put(circleFraction);

            i++;
        }

        // Duplicate the cylinder ends to form the torus
        for (int iR = 0; iR <= radialSamples; iR++, i++) {
            BufferUtils.copyInternalVector3(floatPositionBuffer, iR, i);
            BufferUtils.copyInternalVector3(floatNormalBuffer, iR, i);
            BufferUtils.copyInternalVector2(floatTextureBuffer, iR, i);
            floatTextureBuffer.put(i * 2 + 1, 1.0f);
        }
    }
}
