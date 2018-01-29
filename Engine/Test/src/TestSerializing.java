import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class TestSerializing implements Externalizable {
    private static final long serialVersionUID = 3375159358757648L;
    private static final int INTERNAL_VERSION_ID = 1;

    private int value;

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(INTERNAL_VERSION_ID);
        out.writeInt(value);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        int version = in.readInt();
        switch (version) {
            case 1:
                value = in.readInt();
        }
    }
}

class TestSerializing2 implements Externalizable {
    private static final long serialVersionUID = 3375159358757648L;
    private static final int INTERNAL_VERSION_ID = 2;

    private long value;

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(INTERNAL_VERSION_ID);
        out.writeLong(value);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        int version = in.readInt();
        switch (version) {
            case 1:
                value = in.readInt();
            case 2:
                value = in.readLong();
        }
    }
}
