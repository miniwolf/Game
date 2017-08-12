package mini.environmentMapRenderer;

import mini.math.Vector3f;
import mini.renderEngine.MasterRenderer;
import mini.scene.Scene;
import mini.textures.Texture;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

public class EnvironMapRenderer {
    public static void renderEnvironmentMap(Texture cubeMap, Scene scene, Vector3f center,
                                            MasterRenderer renderer) {
        CubeMapCamera camera = new CubeMapCamera(center);

        //create fbo
        int fbo = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
        GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);

        //attach depth buffer
        int depthBuffer = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthBuffer);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL14.GL_DEPTH_COMPONENT24,
                                   cubeMap.getImage().getWidth(),
                                   cubeMap.getImage().getHeight());
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT,
                                       GL30.GL_RENDERBUFFER,
                                       depthBuffer);

        //indicate that we want to render to the entire face
        GL11.glViewport(0, 0, cubeMap.getImage().getWidth(), cubeMap.getImage().getHeight());

        //loop faces
        for (int i = 0; i < 6; i++) {
            //attach face to fbo as color attachment 0
            // TODO: Attach this using GLRenderer
//            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0,
//                                        GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, cubeMap.textureId,
//                                        0);

            //point camera in the right direction
            camera.switchToFace(i);

            //render mini.scene to fbo, and therefore to the current face of the cubemap
            // TODO render environment mapping
            //renderer.renderLowQualityScene(scene, camera);
        }

        //stop rendering to fbo
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
//        GL11.glViewport(0, 0, DisplayManager.WIDTH, DisplayManager.HEIGHT);

        //delete fbo
        GL30.glDeleteRenderbuffers(depthBuffer);
        GL30.glDeleteFramebuffers(fbo);
    }
}
