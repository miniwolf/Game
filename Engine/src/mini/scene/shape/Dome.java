package mini.scene.shape;

import mini.math.FastMath;
import mini.math.Vector3f;
import mini.scene.Mesh;
import mini.scene.VertexBuffer;
import mini.utils.BufferUtils;
import mini.utils.TempVars;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Represents a hemisphere.
 */
public class Dome extends Mesh {
    private Vector3f center;

    /**
     * Constructs a dome visible from the inside, e.g. for use as a sky dome. All geometry data
     * buffers are updated automatically.
     * <p>
     * For a cone, set planes=2. For a pyramid, set radialSamples=4 and planes=2. Increasing planes
     * and radialSamples increases the quality of the dome.
     *
     * @param center        Center of the dome.
     * @param planes        The number of planes along the Z-axis. Must be >= 2. Influences how round the
     *                      arch of the dome is.
     * @param radialSamples The number of samples along the radial. Influences how round the base of
     *                      the dome is.
     * @param radius        The radius of the dome.
     */
    public Dome(Vector3f center, int planes, int radialSamples, float radius) {
        super();
        updateGeometry(center, planes, radialSamples, radius, true);
    }

    private void updateGeometry(Vector3f center, int planes, int radialSamples, float radius,
                                boolean insideView) {
        this.center = center != null ? center : new Vector3f(0, 0, 0);

        int vertexCount = (planes - 1) * (radialSamples + 1) + 1;
        FloatBuffer vertexBuffer = BufferUtils.createVector3Buffer(vertexCount);
        FloatBuffer normalBuffer = BufferUtils.createVector3Buffer(vertexCount);
        FloatBuffer texCoordBuffer = BufferUtils.createVector2Buffer(vertexCount);

        setBuffer(VertexBuffer.Type.Position, 3, vertexBuffer);
        setBuffer(VertexBuffer.Type.Normal, 3, normalBuffer);
        setBuffer(VertexBuffer.Type.TexCoord, 2, texCoordBuffer);

        float fInvRS = 1.0f / radialSamples;
        float fYFactor = 1.0f / (planes - 1);

        // Points on a unit circle
        float[] afSin = new float[radialSamples];
        float[] afCos = new float[radialSamples];
        for (int iR = 0; iR < radialSamples; iR++) {
            float fAngle = FastMath.TWO_PI * fInvRS * iR;
            afCos[iR] = FastMath.cos(fAngle);
            afSin[iR] = FastMath.sin(fAngle);
        }

        GenerateDome(center, planes, radialSamples, radius, insideView, vertexBuffer, normalBuffer,
                     texCoordBuffer, fInvRS, fYFactor, afSin, afCos);

        // Pole
        vertexBuffer.put(this.center.x).put(this.center.y + radius).put(this.center.z);
        normalBuffer.put(0).put(insideView ? -1 : 1).put(0);
        texCoordBuffer.put(0.5f).put(1.0f);

        int triCount = (planes - 2) * radialSamples * 2 + radialSamples;
        ShortBuffer indexBuffer = BufferUtils.createShortBuffer(3 * triCount);
        setBuffer(VertexBuffer.Type.Index, 3, indexBuffer);

        // Middle planes
        int index = 0;
        for (int plane = 1; plane < (planes - 1); plane++) {
            int bottomPlaneStart = ((plane - 1) * (radialSamples + 1));
            int topPlaneStart = (plane * (radialSamples + 1));
            for (int sample = 0; sample < radialSamples; sample++, index += 6) {
                if (insideView) {
                    indexBuffer.put((short) (bottomPlaneStart + sample));
                    indexBuffer.put((short) (bottomPlaneStart + sample + 1));
                    indexBuffer.put((short) (topPlaneStart + sample));
                    indexBuffer.put((short) (bottomPlaneStart + sample + 1));
                    indexBuffer.put((short) (topPlaneStart + sample + 1));
                    indexBuffer.put((short) (topPlaneStart + sample));
                } else {
                    indexBuffer.put((short) (bottomPlaneStart + sample));
                    indexBuffer.put((short) (topPlaneStart + sample));
                    indexBuffer.put((short) (bottomPlaneStart + sample + 1));
                    indexBuffer.put((short) (bottomPlaneStart + sample + 1));
                    indexBuffer.put((short) (topPlaneStart + sample));
                    indexBuffer.put((short) (topPlaneStart + sample + 1));
                }
            }
        }

        // Pole triangles
        int bottomPlaneStart = (planes - 2) * (radialSamples + 1);
        for (int samples = 0; samples < radialSamples; samples++, index += 3) {
            if (insideView) {
                indexBuffer.put((short) (bottomPlaneStart + samples));
                indexBuffer.put((short) (bottomPlaneStart + samples + 1));
                indexBuffer.put((short) (vertexCount - 1));
            } else {
                indexBuffer.put((short) (bottomPlaneStart + samples));
                indexBuffer.put((short) (vertexCount - 1));
                indexBuffer.put((short) (bottomPlaneStart + samples + 1));
            }
        }

        updateBound();
    }

    private void GenerateDome(Vector3f center, int planes, int radialSamples, float radius,
                              boolean insideView, FloatBuffer vertexBuffer,
                              FloatBuffer normalBuffer, FloatBuffer texCoordBuffer, float fInvRS,
                              float fYFactor, float[] afSin, float[] afCos) {
        TempVars vars = TempVars.get();
        Vector3f tempVa = vars.vect3;
        Vector3f tempVb = vars.vect2;
        Vector3f tempVc = vars.vect1;

        // Generate dome
        int i = 0;
        for (int iY = 0; iY < (planes - 1); iY++, i++) {
            float fYFraction = fYFactor * iY; // In [0;1]
            float fY = radius * fYFraction;

            Vector3f kSliceCenter = tempVb.set(center);
            kSliceCenter.y += fY;

            float fSliceRadius = FastMath.sqrt(FastMath.abs(radius * radius - fY * fY));

            Vector3f kNormal;
            int iSave = i;
            for (int iR = 0; iR < radialSamples; iR++, i++) {
                float fRadialFraction = iR * fInvRS; // in [0;1]
                Vector3f kRadial = tempVc.set(afCos[iR], 0, afSin[iR]);
                kRadial.mult(fSliceRadius, tempVa);
                vertexBuffer.put(kSliceCenter.x + tempVa.x).put(kSliceCenter.y + tempVa.y)
                            .put(kSliceCenter.z + tempVa.z);

                BufferUtils.populateFromBuffer(tempVa, vertexBuffer, i);
                kNormal = tempVa.subtractLocal(center);
                kNormal.normalizeLocal();
                if (!insideView) {
                    normalBuffer.put(kNormal.x).put(kNormal.y).put(kNormal.z);
                } else {
                    normalBuffer.put(-kNormal.x).put(-kNormal.y).put(-kNormal.z);
                }

                texCoordBuffer.put(fRadialFraction).put(fYFraction);
            }

            BufferUtils.copyInternalVector3(vertexBuffer, iSave, i);
            BufferUtils.copyInternalVector3(normalBuffer, iSave, i);
            texCoordBuffer.put(1.0f).put(fYFraction);
        }

        vars.release();
    }
}
