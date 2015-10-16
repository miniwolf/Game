package entities;

import model.TexturedModel;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;
import terrain.Terrain;

/**
 * @author miniwolf
 */
public class Player extends Entity {
    private static final float RUN_SPEED = 20f;
    private static final float TURN_SPEED = 160f;
    private static final float GRAVITY = -50f;
    private static final float JUMP_POWER = 30f;

    private float currentRunSpeed = 0.0f;
    private float currentTurnSpeed = 0.0f;
    private float upwardsSpeed = 0.0f;
    private boolean jumping = false;

    public Player(TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale) {
        super(model, position, rotX, rotY, rotZ, scale);
    }

    public void translate(float delta, Terrain terrain) {
        checkInput();
        super.rotate(0, currentTurnSpeed * delta, 0);
        float distance = currentRunSpeed * delta;
        double v = Math.toRadians(super.getRotY());
        float dx = (float) (distance * Math.sin(v));
        float dz = (float) (distance * Math.cos(v));
        super.translate(dx, 0, dz);

        upwardsSpeed += GRAVITY * delta;
        super.translate(0, upwardsSpeed * delta, 0);
        Vector3f position = getPosition();
        float TERRAIN_HEIGHT = terrain.getHeightOfTerrain(position.x, position.z);

        if ( position.y < TERRAIN_HEIGHT ) {
            upwardsSpeed = 0;
            jumping = false;
            position.y = TERRAIN_HEIGHT;
        }
    }

    private void checkInput() {
        currentRunSpeed = Keyboard.isKeyDown(Keyboard.KEY_W) ? RUN_SPEED : Keyboard.isKeyDown(Keyboard.KEY_S) ? -RUN_SPEED : 0;
        currentTurnSpeed = Keyboard.isKeyDown(Keyboard.KEY_A) ? TURN_SPEED : Keyboard.isKeyDown(Keyboard.KEY_D) ? -TURN_SPEED : 0;
        if ( Keyboard.isKeyDown(Keyboard.KEY_SPACE) && !jumping ) {
            jumping = true;
            upwardsSpeed = JUMP_POWER;
        }
        if ( Keyboard.isKeyDown(Keyboard.KEY_P) ) {
            System.out.println(getPosition());
        }
    }
}
