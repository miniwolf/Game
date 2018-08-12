package mini.editor.extension.property;

import mini.light.DirectionalLight;
import mini.math.ColorRGBA;
import mini.math.Quaternion;
import mini.math.Vector3f;
import mini.scene.Geometry;
import mini.scene.Node;
import mini.scene.Spatial;

import java.nio.file.Path;

public enum EditablePropertyType {
    BOOLEAN(Boolean.class, boolean.class),
    INTEGER(Integer.class, int.class),
    FLOAT(Float.class, float.class),
    COLOR(ColorRGBA.class),
    ENUM(Enum.class),
    VECTOR_3F(Vector3f.class),
    QUATERNION(Quaternion.class),
    STRING(String.class),
    READ_ONLY_STRING(String.class),
    STRING_FROM_LIST(String.class),
    RESOURCE_FROM_CLASSPATH(String.class),
    DIRECTION_LIGHT_FROM_SCENE(DirectionalLight.class),
    SPATIAL_FROM_ASSET_FOLDER(Spatial.class),
    SPATIAL_FROM_SCENE(Spatial.class),
    NODE_FROM_ASSET_FOLDER(Node.class),
    NODE_FROM_SCENE(Node.class),
    GEOMETRY_FROM_ASSET_FOLDER(Geometry.class),
    GEOMETRY_FROM_SCENE(Geometry.class),
    OBJECT_FROM_LIST(Object.class),
    SEPARATOR(Void.class),
    FILE_FROM_ASSET_FOLDER(Path.class),
    FOLDER_FROM_ASSET_FOLDER(Path.class),
    EXTERNAL_FILE(Path.class);

    private Class<?>[] types;

    EditablePropertyType(Class<?>... types) {
        this.types = types;
    }
}
