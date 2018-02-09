//package mini.physics;
//
//import mini.app.SimpleApplication;
//import mini.asset.TextureKey;
//import mini.bullet.BulletApplicationState;
//import mini.bullet.control.RigidBodyControl;
//import mini.font.BitmapText;
//import mini.input.MouseInput;
//import mini.input.controls.ActionListener;
//import mini.input.controls.MouseButtonTrigger;
//import mini.material.Material;
//import mini.math.Vector2f;
//import mini.math.Vector3f;
//import mini.scene.Geometry;
//import mini.scene.shape.Box;
//import mini.scene.shape.Sphere;
//import mini.textures.Texture;
//
//public class HelloPhysics extends SimpleApplication {
//    /**
//     * Dimensions used for bricks and wall
//     */
//    private static final float brickLength = 0.48f;
//    private static final float brickWidth = 0.24f;
//    private static final float brickHeight = 0.12f;
//
//    private static final Box box;
//    private static final Box floor;
//    private static final Sphere sphere;
//
//    static {
//        sphere = new Sphere(32, 32, 0.4f, true, false);
//        sphere.setTextureMode(Sphere.TextureMode.Projected);
//
//        box = new Box(brickLength, brickHeight, brickWidth);
//        box.scaleTextureCoordinates(new Vector2f(1f, .5f));
//
//        floor = new Box(10f, 0.1f, 5f);
//        floor.scaleTextureCoordinates(new Vector2f(3, 6));
//    }
//
//    private BulletApplicationState bulletApplicationState;
//
//    private Material wallMaterial;
//    private Material floorMaterial;
//    private Material ballMaterial;
//    private ActionListener actionListener = (name, isPressed, tpf) -> {
//        if (name.equals("shoot") && !isPressed) {
//            makeCannonBall();
//        }
//    };
//
//    public static void main(String[] args) {
//        HelloPhysics app = new HelloPhysics();
//        app.start();
//    }
//
//    @Override
//    public void simpleInitApp() {
//        bulletApplicationState = new BulletApplicationState();
//        stateManager.attach(bulletApplicationState);
//        flyCam.setMoveSpeed(20);
//
//        initInputs();
//        initMaterials();
//        initWall();
//        initFloor();
//        initCrossHair();
//    }
//
//    private void initInputs() {
//        inputManager.addMapping("shoot", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
//        inputManager.addListener(actionListener, "shoot");
//    }
//
//    private void initMaterials() {
//        {
//            wallMaterial = new Material(assetManager, "MatDefs/Misc/Unshaded.minid");
//            TextureKey key = new TextureKey("Textures/Terrain/Brick/BrickWall.jpg");
//            key.setGenerateMips(true);
//            Texture texture = assetManager.loadTexture(key);
//            wallMaterial.setTexture("ColorMap", texture);
//        }
//
//        {
//            floorMaterial = new Material(assetManager, "MatDefs/Misc/Unshaded.minid");
//            TextureKey key = new TextureKey("Textures/Terrain/Pond/Pond.jpg");
//            key.setGenerateMips(true);
//            Texture texture = assetManager.loadTexture(key);
//            texture.setWrap(Texture.WrapMode.Repeat);
//            floorMaterial.setTexture("ColorMap", texture);
//        }
//
//        {
//            ballMaterial = new Material(assetManager, "MatDefs/Misc/Unshaded.minid");
//            TextureKey key = new TextureKey("Textures/Terrain/Rock/Rock.PNG");
//            key.setGenerateMips(true);
//            Texture texture = assetManager.loadTexture(key);
//            ballMaterial.setTexture("ColorMap", texture);
//        }
//    }
//
//    private void initCrossHair() {
//        BitmapText character = new BitmapText(guiFont);
//        character.setSize(guiFont.getCharSet().getRenderedSize() * 2);
//        character.setText("+"); // Making a fake crosshair
//        character.setLocalTranslation(1280 / 2, 768 / 2,
//                                      0); // TODO: Faking center. Try to use some setting for this.
//        guiNode.attachChild(character);
//    }
//
//    private void makeCannonBall() {
//        Geometry ballGeometry = new Geometry("Cannon Ball", sphere);
//        ballGeometry.setMaterial(ballMaterial);
//        ballGeometry.setLocalTranslation(cam.getLocation());
//        rootNode.attachChild(ballGeometry);
//
//        // TODO: Add physics
//        RigidBodyControl ballPhysics = new RigidBodyControl(1f);
//        ballGeometry.addControl(ballPhysics);
//        bulletApplicationState.getPhysicsSpace().add(ballPhysics);
//        ballPhysics.setLinearVelocity(cam.getDirection().mult(25));
//    }
//
//    private void initFloor() {
//        Geometry floorGeometry = new Geometry("Floor", floor);
//        floorGeometry.setMaterial(floorMaterial);
//        floorGeometry.setLocalTranslation(0, -0.1f, 0);
//        rootNode.attachChild(floorGeometry);
//        // TODO: Add physics
//        RigidBodyControl floorPhysics = new RigidBodyControl(0f);
//        floorGeometry.addControl(floorPhysics);
//
//        bulletApplicationState.getPhysicsSpace().add(floorPhysics);
//    }
//
//    private void initWall() {
//        float startPoint = brickLength / 4;
//        float height = 0;
//        for (int i = 0; i < 15; i++) {
//            for (int j = 0; j < 6; j++) {
//                Vector3f vertex = new Vector3f(j * brickLength * 2 + startPoint,
//                                               brickHeight + height, 0);
//                makeBrick(vertex);
//            }
//            startPoint = -startPoint;
//            height += 2 * brickHeight;
//        }
//    }
//
//    private void makeBrick(Vector3f location) {
//        Geometry brickGeometry = new Geometry("brick", box);
//        brickGeometry.setMaterial(wallMaterial);
//        brickGeometry.setLocalTranslation(location);
//        rootNode.attachChild(brickGeometry);
//
//        // Create brick mass
//        RigidBodyControl brickPhysics = new RigidBodyControl(2f);
//        brickGeometry.addControl(brickPhysics);
//        bulletApplicationState.getPhysicsSpace().add(brickPhysics);
//    }
//}
