package mini.utils;

import mini.math.Matrix4f;
import mini.math.Vector3f;

public interface Camera {
    void update();

    Vector3f getPosition();

    Matrix4f getViewMatrix();

    void reflect(float height);

    Matrix4f getProjectionMatrix();

    Matrix4f getProjectionViewMatrix();
}
