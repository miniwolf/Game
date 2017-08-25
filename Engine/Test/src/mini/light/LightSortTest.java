package mini.light;

import mini.math.Vector3f;
import mini.scene.Geometry;
import mini.scene.Mesh;
import mini.scene.Node;
import org.junit.Test;

/**
 * Test light sorting (in the scene graph) for various light types.
 */
public class LightSortTest {
    
    @Test
    public void testSimpleSort() {
        Geometry g = new Geometry("test", new Mesh());
        LightList list = new LightList(g);
        
        //list.add(new SpotLight(Vector3f.ZERO, Vector3f.UNIT_X));
        list.add(new PointLight(Vector3f.UNIT_X));
        list.add(new DirectionalLight(Vector3f.UNIT_X));
        list.add(new AmbientLight());
        
        list.sort(true);
        
        assert list.get(0) instanceof AmbientLight;     // Ambients always first
        assert list.get(1) instanceof DirectionalLight; // .. then directionals
        //assert list.get(2) instanceof SpotLight;        // Spot is 0 units away from geom
        assert list.get(2) instanceof PointLight;       // .. and point is 1 unit away.
    }
    
    @Test
    public void testSceneGraphSort() {
        Node n = new Node("node");
        Geometry g = new Geometry("geom", new Mesh());
        //SpotLight spot = new SpotLight(Vector3f.ZERO, Vector3f.UNIT_X);
        PointLight point = new PointLight(Vector3f.UNIT_X);
        DirectionalLight directional = new DirectionalLight(Vector3f.UNIT_X);
        AmbientLight ambient = new AmbientLight();
        
        // Some lights are on the node
        //n.addLight(spot);
        n.addLight(point);
        
        // .. and some on the geometry.
        g.addLight(directional);
        g.addLight(ambient);
        
        n.attachChild(g);
        n.updateGeometricState();
        
        LightList list = g.getWorldLightList();
        
        // check the sorting (when geom is at 0,0,0)
        assert list.get(0) instanceof AmbientLight;
        assert list.get(1) instanceof DirectionalLight;
        //assert list.get(2) instanceof SpotLight;
        assert list.get(2) instanceof PointLight;
        
        // move the geometry closer to the point light
        g.setLocalTranslation(Vector3f.UNIT_X);
        n.updateGeometricState();
        
        assert list.get(0) instanceof AmbientLight;
        assert list.get(1) instanceof DirectionalLight;
        assert list.get(2) instanceof PointLight;
        //assert list.get(3) instanceof SpotLight;
        
        // now move the point light away from the geometry
        // and the spot light closer

        // XXX: doesn't work! can't detect that the light moved!
//        point.setPosition(Vector3f.ZERO);
//        spot.setPosition(Vector3f.UNIT_X);
//        n.updateGeometricState();
//        
//        assert list.get(0) instanceof AmbientLight;
//        assert list.get(1) instanceof DirectionalLight;
//        assert list.get(2) instanceof SpotLight;
//        assert list.get(3) instanceof PointLight;
    }
}
