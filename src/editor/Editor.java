package editor;

import entities.Entity;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;
import rendering.Renderer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author miniwolf
 */
public class Editor {
    Entity ce;
    private List<Entity> entityList = new ArrayList<>();
    private int index = 0;

    public void update(Vector3f terrainPoint, Renderer renderer) {
        while ( Mouse.next() ) {
            if ( !Mouse.getEventButtonState() && Mouse.getEventButton() == 0 ) {
                System.out.println("Processing entity");
                renderer.processEntity(new Entity(ce.getModel(), ce.getPosition(), ce.getRotX(), ce.getRotY(), ce.getRotZ(), ce.getScale()));
            }
        }

        if ( Keyboard.isKeyDown(Keyboard.KEY_N) ) {
            ce = next();
        } else if ( Keyboard.isKeyDown(Keyboard.KEY_P)) {
            ce = prev();
        }
        renderer.processEntity(ce);
        ce.setPosition(terrainPoint);
    }

    public void add(Entity entity) {
        if ( ce == null ) {
            ce = entity;
        }
        entityList.add(entity);
    }

    private Entity next() {
        if ( index + 1 < entityList.size() ) {
            index++;
        } else {
            index = 0;
        }
        return entityList.get(index);
    }

    private Entity prev() {
        if ( index - 1 >= 0 ) {
            index--;
        } else {
            index = entityList.size() - 1;
        }
        return entityList.get(index);
    }
}
