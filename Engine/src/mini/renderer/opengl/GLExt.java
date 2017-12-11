/*
 * Copyright (c) 2009-2014 jMonkeyEngine
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
package mini.renderer.opengl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * GL functions provided by extensions.
 * <p>
 * Always must check against a renderer capability prior to using those.
 */
public interface GLExt {

    int GL_ALREADY_SIGNALED = 0x911A;
    int GL_COMPRESSED_RGB8_ETC2 = 0x9274;
    int GL_COMPRESSED_RGBA_S3TC_DXT1_EXT = 0x83F1;
    int GL_COMPRESSED_RGBA_S3TC_DXT3_EXT = 0x83F2;
    int GL_COMPRESSED_RGBA_S3TC_DXT5_EXT = 0x83F3;
    int GL_COMPRESSED_RGB_S3TC_DXT1_EXT = 0x83F0;
    int GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT1_EXT = 0x8C4D;
    int GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT3_EXT = 0x8C4E;
    int GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT5_EXT = 0x8C4F;
    int GL_COMPRESSED_SRGB_S3TC_DXT1_EXT = 0x8C4C;
    int GL_CONDITION_SATISFIED = 0x911C;
    int GL_DEPTH_COMPONENT32F = 0x8CAC;
    int GL_DEPTH24_STENCIL8_EXT = 0x88F0;
    int GL_DEPTH_STENCIL_EXT = 0x84F9;
    int GL_ETC1_RGB8_OES = 0x8D64;
    int GL_FRAMEBUFFER_SRGB_CAPABLE_EXT = 0x8DBA;
    int GL_FRAMEBUFFER_SRGB_EXT = 0x8DB9;
    int GL_HALF_FLOAT_ARB = 0x140B;
    int GL_LUMINANCE16F_ARB = 0x881E;
    int GL_LUMINANCE32F_ARB = 0x8818;
    int GL_LUMINANCE_ALPHA16F_ARB = 0x881F;
    int GL_MAX_COLOR_TEXTURE_SAMPLES = 0x910E;
    int GL_MAX_DEPTH_TEXTURE_SAMPLES = 0x910F;
    int GL_MAX_DRAW_BUFFERS_ARB = 0x8824;
    int GL_MAX_SAMPLES_EXT = 0x8D57;
    int GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT = 0x84FF;
    int GL_MULTISAMPLE_ARB = 0x809D;
    int GL_NUM_PROGRAM_BINARY_FORMATS = 0x87FE;
    int GL_PIXEL_PACK_BUFFER_ARB = 0x88EB;
    int GL_PIXEL_UNPACK_BUFFER_ARB = 0x88EC;
    int GL_R11F_G11F_B10F_EXT = 0x8C3A;
    int GL_RGBA8 = 0x8058;
    int GL_RGB16F_ARB = 0x881B;
    int GL_RGB32F_ARB = 0x8815;
    int GL_RGB9_E5_EXT = 0x8C3D;
    int GL_RGBA16F_ARB = 0x881A;
    int GL_RGBA32F_ARB = 0x8814;
    int GL_SAMPLES_ARB = 0x80A9;
    int GL_SAMPLE_ALPHA_TO_COVERAGE_ARB = 0x809E;
    int GL_SAMPLE_BUFFERS_ARB = 0x80A8;
    int GL_SAMPLE_POSITION = 0x8E50;
    int GL_SLUMINANCE8_ALPHA8_EXT = 0x8C45;
    int GL_SLUMINANCE8_EXT = 0x8C47;
    int GL_SRGB8_ALPHA8_EXT = 0x8C43;
    int GL_SRGB8_EXT = 0x8C41;
    int GL_SYNC_FLUSH_COMMANDS_BIT = 0x1;
    int GL_SYNC_GPU_COMMANDS_COMPLETE = 0x9117;
    int GL_TEXTURE_2D_ARRAY_EXT = 0x8C1A;
    int GL_TEXTURE_2D_MULTISAMPLE = 0x9100;
    int GL_TEXTURE_2D_MULTISAMPLE_ARRAY = 0x9102;
    int GL_TEXTURE_CUBE_MAP_SEAMLESS = 0x884F;
    int GL_TEXTURE_MAX_ANISOTROPY_EXT = 0x84FE;
    int GL_TIMEOUT_EXPIRED = 0x911B;
    int GL_UNSIGNED_INT_10F_11F_11F_REV_EXT = 0x8C3B;
    int GL_UNSIGNED_INT_24_8_EXT = 0x84FA;
    int GL_UNSIGNED_INT_5_9_9_9_REV_EXT = 0x8C3E;
    int GL_WAIT_FAILED = 0x911D;

    void glBufferData(int target, IntBuffer data, int usage);

    void glBufferSubData(int target, long offset, IntBuffer data);

    int glClientWaitSync(Object sync, int flags, long timeout);

    void glDeleteSync(Object sync);

    void glDrawArraysInstancedARB(int mode, int first, int count, int primcount);

    void glDrawBuffers(IntBuffer bufs);

    void glDrawElementsInstancedARB(int mode, int indices_count, int type,
                                    long indices_buffer_offset, int primcount);

    Object glFenceSync(int condition, int flags);

    void glGetMultisample(int pname, int index, FloatBuffer val);

    void glTexImage2DMultisample(int target, int samples, int internalformat, int width,
                                 int height, boolean fixedsamplelocations);

    void glVertexAttribDivisorARB(int index, int divisor);
}
