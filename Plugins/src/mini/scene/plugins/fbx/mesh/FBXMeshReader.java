package mini.scene.plugins.fbx.mesh;

import mini.scene.plugins.fbx.file.FBXElement;

public class FBXMeshReader {
    public static double[] getDoubleArray(FBXElement fbxElement) {
        if (fbxElement.getPropertyTypes()[0] == 'd') {
            return (double[]) fbxElement.getProperties().get(0);
        } else if (fbxElement.getPropertyTypes()[0] == 'D') {
            double[] doubles = new double[fbxElement.getPropertyTypes().length];
            for (int i = 0; i < doubles.length; i++) {
                doubles[i] = (double) fbxElement.getProperties().get(i);
            }
            return doubles;
        }
        throw new UnsupportedOperationException("Other version let's have a look at it");
    }

    public static int[] getIntArray(FBXElement fbxElement) {
        if (fbxElement.getPropertyTypes()[0] == 'i') {
            return (int[]) fbxElement.getProperties().get(0);
        } else if (fbxElement.getPropertyTypes()[0] == 'I') {
            int[] ints = new int[fbxElement.getPropertyTypes().length];
            for (int i = 0; i < ints.length; i++) {
                ints[i] = (int) fbxElement.getProperties().get(i);
            }
            return ints;
        }
        throw new UnsupportedOperationException("Other version let's have a look at it");
    }
}
