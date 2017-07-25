package mini.light;

import mini.math.ColorRGBA;

/**
 * Created by miniwolf on 06-05-2017.
 */
public class Light {
    protected ColorRGBA color = new ColorRGBA(ColorRGBA.White);

    /**
     * Sets the light color.
     *
     * @param color the light color.
     */
    public void setColor(ColorRGBA color){
        this.color.set(color);
    }
}
