package mini.scene.plugins.fbx.mesh;

import mini.scene.plugins.fbx.file.FBXElement;

public class FBXMeshReader {
    public static double[] getDoubleArray(FBXElement fbxElement) {
        if (fbxElement.getPropertyTypes()[0] == 'd') {
            return (double[]) fbxElement.getProperties().get(0);
        }
        throw new UnsupportedOperationException("Other version let's have a look at it");
    }

    public static int[] getIntArray(FBXElement fbxElement) {
        if (fbxElement.getPropertyTypes()[0] == 'i') {
            return (int[]) fbxElement.getProperties().get(0);
        }
        throw new UnsupportedOperationException("Other version let's have a look at it");
    }
}
