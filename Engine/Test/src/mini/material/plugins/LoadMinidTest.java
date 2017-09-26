/*
 * Copyright (c) 2009-2016 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package mini.material.plugins;

import mini.material.Material;
import mini.renderer.Caps;
import mini.renderer.RenderManager;
import mini.scene.Geometry;
import mini.scene.shape.Box;
import mini.shaders.Shader;
import mini.system.NullRenderer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class LoadMinidTest {

    private Material material;
    private final Geometry geometry = new Geometry("Geometry", new Box(1, 1, 1));
    private final EnumSet<Caps> myCaps = EnumSet.noneOf(Caps.class);
    private final RenderManager renderManager = new RenderManager(new NullRenderer() {
        @Override
        public EnumSet<Caps> getCaps() {
            return LoadMinidTest.this.myCaps;
        }
    });

    @Test
    public void testShaderNodesMaterialDefLoading() {
        supportGlsl(100);
        material("matdef.minid");
        material.selectTechnique("Default", renderManager);

        assertEquals(material.getActiveTechnique().getDef().getShaderNodes().size(), 2);
        Shader s = material.getActiveTechnique().getDef().getShader(myCaps,  material.getActiveTechnique().getDynamicDefines());
        assertEquals(s.getSources().size(), 2);
    }

    private void supportGlsl(int version) {
        switch (version) {
            case 150:
                myCaps.add(Caps.GLSL150);
            case 140:
                myCaps.add(Caps.GLSL140);
            case 130:
                myCaps.add(Caps.GLSL130);
            case 120:
                myCaps.add(Caps.GLSL120);
            case 110:
                myCaps.add(Caps.GLSL110);
            case 100:
                myCaps.add(Caps.GLSL100);
                break;
        }
    }
    private void caps(Caps... caps) {
        myCaps.addAll(Arrays.asList(caps));
    }

    private void material(String path) {
        material = new Material(path);
        geometry.setMaterial(material);
    }
}
