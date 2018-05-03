package mini.scene.plugins;

public class IrBoneWeightIndex implements Cloneable, Comparable<IrBoneWeightIndex> {
    public final int boneIndex;
    public final float boneWeight;

    public IrBoneWeightIndex(int boneIndex, float boneWeight) {
        this.boneIndex = boneIndex;
        this.boneWeight = boneWeight;
    }

    @Override
    public int compareTo(IrBoneWeightIndex o) {
        return Float.compare(o.boneWeight, boneWeight);
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError(ex);
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + this.boneIndex;
        hash = 23 * hash + Float.floatToIntBits(this.boneWeight);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IrBoneWeightIndex other = (IrBoneWeightIndex) obj;
        if (this.boneIndex != other.boneIndex) {
            return false;
        }
        return Float.floatToIntBits(this.boneWeight) == Float.floatToIntBits(other.boneWeight);
    }
}
