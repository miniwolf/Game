package main;

import mini.app.SimpleApplication;
import mini.bounding.BoundingVolume;
import mini.font.BitmapFont;
import mini.font.BitmapText;
import mini.input.KeyboardKey;
import mini.input.controls.AnalogListener;
import mini.input.controls.KeyTrigger;
import mini.material.Material;
import mini.math.ColorRGBA;
import mini.math.FastMath;
import mini.math.Quaternion;
import mini.math.Vector3f;
import mini.scene.Geometry;
import mini.scene.Node;
import mini.scene.Spatial;
import mini.scene.shape.Box;
import mini.scene.shape.Dome;

import java.util.ArrayList;
import java.util.List;

public class MainApp extends SimpleApplication implements AnalogListener {
    private BitmapText pressStart, fpsScoreText;
    private float score;
    private Geometry firstCube;
    private Node player;
    private Material playerMaterial;
    private Material floorMaterial;
    private List<Geometry> cubeField = new ArrayList<>();
    private List<ColorRGBA> obstacleColors = new ArrayList<>();
    private int difficulty = 20;

    private float fpsRate = 1000f / 1f;
    private float playerSpeed = 0.04f;
    private float cameraAngle = 0;
    private float timeToColorChange = 20f;
    private int colorIndex;
    private boolean solidBox = true;

    public static void main(String[] args) {
        MainApp app = new MainApp();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        BitmapFont defaultFont = assetManager.loadFont("Interface/Fonts/Default.fnt");

        flyCam.setEnabled(false);

        setupPlayerInput();

        pressStart = new BitmapText(defaultFont);
        fpsScoreText = new BitmapText(defaultFont);

        loadText(pressStart, "PRESS ENTER TO START THE GAME", defaultFont, 0, 5, 0);
        loadText(fpsScoreText, "Current Score: 0", defaultFont, 0, 2, 0);

        player = createPlayer();
        rootNode.attachChild(player);

        gameReset();
    }

    private void setupPlayerInput() {
        inputManager.addMapping("Left", new KeyTrigger(KeyboardKey.KEY_LEFT));
        inputManager.addMapping("Right", new KeyTrigger(KeyboardKey.KEY_RIGHT));
        inputManager.addListener(this, "Left", "Right");
    }

    private Geometry createCube() {
        Vector3f location = player.getLocalTranslation().clone();
        location.addLocal(4, 0, 0);
        Box box = new Box(1, 1, 1);
        Geometry boxMesh = new Geometry("box", box);
        boxMesh.setLocalTranslation(location);

        Material mat = new Material(assetManager, "MatDefs/Misc/Unshaded.minid");
        mat.setColor("Color", ColorRGBA.Blue);
        boxMesh.setMaterial(mat);

        return boxMesh;
    }

    private Node createPlayer() {
        Dome dome = new Dome(Vector3f.ZERO, 10, 1000, 1);
        Geometry playerMesh = new Geometry("player", dome);

        playerMaterial = new Material(assetManager, "MatDefs/Misc/Unshaded.minid");
        playerMaterial.setColor("Color", ColorRGBA.Red);
        playerMesh.setMaterial(playerMaterial);
        playerMesh.setName("player");

        Geometry floorMesh = createFloor(playerMesh);

        Node playerNode = new Node();
        playerNode.attachChild(playerMesh);
        playerNode.attachChild(floorMesh);
        return playerNode;
    }

    private Geometry createFloor(Geometry playerMesh) {
        Box floor = new Box(100, 0, 100);
        Geometry floorMesh = new Geometry("Box", floor);

        Vector3f translation = Vector3f.ZERO.add(playerMesh.getLocalTranslation().getX(),
                                                 playerMesh.getLocalTranslation().getY() - 1, 0);

        floorMesh.setLocalTranslation(translation);

        floorMaterial = new Material(assetManager, "MatDefs/Misc/Unshaded.minid");
        floorMaterial.setColor("Color", ColorRGBA.LightGray);
        floorMesh.setMaterial(floorMaterial);
        floorMesh.setName("floor");
        return floorMesh;
    }

    @Override
    public void simpleUpdate(float tpf) {
        cameraLogic(tpf);
        gameLogic(tpf);
        colorLogic();
    }

    private void colorLogic() {
        if (timer.getTimeInSeconds() > timeToColorChange) {
            colorIndex++;

            timeToColorChange = timer.getTimeInSeconds() + 20;

            switch (colorIndex) {
                case 1:
                    obstacleColors.add(ColorRGBA.Green);
                    solidBox = false;
                    renderer.setBackgroundColor(ColorRGBA.Black);
                    playerMaterial.setColor("Color", ColorRGBA.White);
                    floorMaterial.setColor("Color", ColorRGBA.Black);
                    break;
                case 2:
                    obstacleColors.set(0, ColorRGBA.Black);
                    solidBox = true;
                    renderer.setBackgroundColor(ColorRGBA.White);
                    playerMaterial.setColor("Color", ColorRGBA.Gray);
                    floorMaterial.setColor("Color", ColorRGBA.LightGray);
                    break;
                case 3:
                    obstacleColors.set(0, ColorRGBA.Pink);
                    break;
                case 4:
                    obstacleColors.set(0, ColorRGBA.Cyan);
                    obstacleColors.add(ColorRGBA.Magenta);
                    renderer.setBackgroundColor(ColorRGBA.Gray);
                    floorMaterial.setColor("Color", ColorRGBA.Gray);
                    playerMaterial.setColor("Color", ColorRGBA.White);
                    break;
                case 5:
                    obstacleColors.remove(0);
                    renderer.setBackgroundColor(ColorRGBA.Pink);
                    solidBox = false;
                    playerMaterial.setColor("Color", ColorRGBA.White);
                    break;
                case 6:
                    obstacleColors.set(0, ColorRGBA.White);
                    solidBox = true;
                    renderer.setBackgroundColor(ColorRGBA.Black);
                    playerMaterial.setColor("Color", ColorRGBA.Gray);
                    floorMaterial.setColor("Color", ColorRGBA.LightGray);
                    break;
                case 7:
                    obstacleColors.set(0, ColorRGBA.Green);
                    renderer.setBackgroundColor(ColorRGBA.Gray);
                    playerMaterial.setColor("Color", ColorRGBA.Black);
                    floorMaterial.setColor("Color", ColorRGBA.Orange);
                    break;
                case 8:
                    obstacleColors.set(0, ColorRGBA.Red);
                    floorMaterial.setColor("Color", ColorRGBA.Pink);
                    break;
                case 9:
                    obstacleColors.set(0, ColorRGBA.Orange);
                    obstacleColors.add(ColorRGBA.Red);
                    obstacleColors.add(ColorRGBA.Yellow);
                    renderer.setBackgroundColor(ColorRGBA.White);
                    playerMaterial.setColor("Color", ColorRGBA.Red);
                    floorMaterial.setColor("Color", ColorRGBA.Gray);
                    colorIndex = 0;
                    break;
            }
        }
    }

    private void cameraLogic(float tpf) {
        cam.setLocation(player.getLocalTranslation().add(-8, 2, 0));
        cam.lookAt(player.getLocalTranslation(), Vector3f.UNIT_Y);

        Quaternion rotation = new Quaternion();
        rotation.fromAngleNormalAxis(cameraAngle, Vector3f.UNIT_Z);
        cam.setRotation(cam.getRotation().mult(rotation));
        cameraAngle *= FastMath.pow(.99f, fpsRate * tpf);
    }

    private void gameLogic(float tpf) {
        score += fpsRate * tpf;
        fpsScoreText.setText("Current Score: " + (int) score);

        // TODO: Increase playerSpeed, difficulty and such

        player.move(playerSpeed * tpf * fpsRate, 0, 0);
        if (cubeField.size() > difficulty) {
            cubeField.remove(0);
        } else if (cubeField.size() != difficulty) {
            placeRandomizedCubes();
        }

        for (int i = 0; i < cubeField.size(); i++) {
            Spatial playerMesh = player.getChild(0);
            Geometry cube = cubeField.get(i);
            cube.updateGeometricState();

            BoundingVolume playerVolume = playerMesh.getWorldBound();
            BoundingVolume cubeVolume = cube.getWorldBound();

            if (playerVolume.intersects(cubeVolume)) {
                gameLost();
                return;
            }

            if (cube.getLocalTranslation().getX() + 10 < player.getLocalTranslation().getX()) {
                cube.removeFromParent();
                cubeField.remove(cube);
            }
        }
    }

    private void gameLost() {
        gameReset();
    }

    private void gameReset() {
        score = 0;
        colorIndex = 0;

        for (Geometry cube : cubeField) {
            cube.removeFromParent();
        }
        cubeField.clear();

        if (firstCube != null) {
            firstCube.removeFromParent();
        }
        firstCube = createCube();

        obstacleColors.clear();
        obstacleColors.add(ColorRGBA.Orange);
        obstacleColors.add(ColorRGBA.Red);
        obstacleColors.add(ColorRGBA.Yellow);

        floorMaterial.setColor("Color", ColorRGBA.LightGray);
        playerMaterial.setColor("Color", ColorRGBA.Red);
        solidBox = true;

        renderer.setBackgroundColor(ColorRGBA.White);
        player.setLocalTranslation(0, 0, 0);
    }

    private void placeRandomizedCubes() {
        Geometry cube = firstCube.clone();
        int playerX = (int) player.getLocalTranslation().getX();
        int playerZ = (int) player.getLocalTranslation().getZ();

        float x = FastMath.nextRandomInt(playerX + 30, playerX + 90);
        float z = FastMath.nextRandomInt(playerZ - 50, playerZ + 50);
        cube.getLocalTranslation().set(x, 0, z);

        Material mat = new Material(assetManager, "MatDefs/Misc/Unshaded.minid");
        if (!solidBox) {
            mat.getAdditionalRenderState().setWireframe(true);
        }
        mat.setColor("Color",
                     obstacleColors.get(FastMath.nextRandomInt(0, obstacleColors.size() - 1)));
        cube.setMaterial(mat);

        rootNode.attachChild(cube);
        cubeField.add(cube);
        // store these to clean them up and test collisions
    }

    private void loadText(BitmapText textField, String text, BitmapFont font, float x, float y,
                          float z) {
        textField.setText(text);
        textField.setLocalTranslation(x, textField.getHeight() * y, z);
        guiNode.attachChild(textField);
    }

    @Override
    public void onAnalog(String binding, float value, float tpf) {
        if (binding.equals("Left")) { // TODO: change camera angle
            player.move(0, 0, -(playerSpeed / 2f) * value * fpsRate);
            cameraAngle -= value * 100 * tpf;
        } else if (binding.equals("Right")) {
            player.move(0, 0, playerSpeed / 2f * value * fpsRate);
            cameraAngle += value * 100 * tpf;
        }
    }
}