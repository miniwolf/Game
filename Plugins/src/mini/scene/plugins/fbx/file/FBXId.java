package mini.scene.plugins.fbx.file;

import java.util.Objects;

public abstract class FBXId {
    public static final FBXId ROOT = new LongFBXId(0);

    public static FBXId create(Object obj) {
        if (obj instanceof Long) {
            return new LongFBXId((Long) obj);
        } else if (obj instanceof String) {
            return new StringFBXId((String) obj);
        } else {
            throw new UnsupportedOperationException(
                    "Unsupported ID object type: " + obj.getClass());
        }
    }

    public static FBXId getObjectId(FBXElement element) {
        if (element.getPropertyTypes().length == 2
            && element.getPropertyTypes()[0] == 'S'
            && element.getPropertyTypes()[1] == 'S') {
            return new StringFBXId((String) element.getProperties().get(0));
        } else if (element.getPropertyTypes().length == 3
                   && element.getPropertyTypes()[0] == 'L'
                   && element.getPropertyTypes()[1] == 'S'
                   && element.getPropertyTypes()[2] == 'S') {
            return new LongFBXId((Long) element.getProperties().get(0));
        } else {
            return null;
        }
    }

    public abstract boolean isNull();

    public static final class StringFBXId extends FBXId {
        private final String id;

        public StringFBXId(String id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            return this == o || o != null && getClass() == o.getClass()
                                && Objects.equals(id, ((StringFBXId) o).id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public String toString() {
            return id;
        }

        @Override
        public boolean isNull() {
            return id.equals("Scene\u0000\u0001Model");
        }
    }

    public static final class LongFBXId extends FBXId {
        private long id;

        public LongFBXId(long id) {
            this.id = id;
        }

        public long getId() {
            return id;
        }// TODO: Delete, debugging only

        @Override
        public boolean equals(Object o) {
            return this == o || o != null && getClass() == o.getClass() && id == ((LongFBXId) o).id;
        }

        @Override
        public int hashCode() {
            return (int) (this.id ^ (this.id >>> 32));
        }

        @Override
        public String toString() {
            return Long.toString(id);
        }

        @Override
        public boolean isNull() {
            return id == 0;
        }
    }
}
