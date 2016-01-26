package entities;

import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

/**
 * @author miniwolf
 */

public class Camera {
    private float distanceFromPlayer = 50;
    private float angleAroundPlayer = 0;

    private Vector3f position = new Vector3f(50,50,0);
    private float pitch;
    private float yaw;
    private float roll;

    private Player player;

    private float rotationSpeedX = 0.3f;
    private float rotationSpeedY = 0.1f;

    public Camera(Player player) {
        this.player = player;
    }

    public void translate() {
        zoom();
        calculate();
        float horizontal = (float) (distanceFromPlayer * Math.cos(Math.toRadians(pitch)));
        float theta = player.getRotY() + angleAroundPlayer;
        position.x = player.getPosition().x - (float) (horizontal * Math.sin(Math.toRadians(theta)));
        position.y = player.getPosition().y + (float) (distanceFromPlayer * Math.sin(Math.toRadians(pitch)));
        position.z = player.getPosition().z - (float) (horizontal * Math.cos(Math.toRadians(theta)));
        yaw = 180 - theta;
    }

    public Vector3f getPosition() {
        return position;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public float getRoll() {
        return roll;
    }

    private void zoom() {
        distanceFromPlayer -= Mouse.getDWheel() * 0.1f;
    }

    private void calculate() {
        if ( Mouse.isButtonDown(0) ) {
            pitch = Math.max(Math.min(pitch - Mouse.getDY() * rotationSpeedY, 180), 0);

            angleAroundPlayer -= Mouse.getDX() * rotationSpeedX;
            if ( angleAroundPlayer < 0 ) {
                angleAroundPlayer = 360 + angleAroundPlayer;
            } else if ( angleAroundPlayer > 360 ) {
                angleAroundPlayer = 360 - angleAroundPlayer;
            }
        }
    }
}
