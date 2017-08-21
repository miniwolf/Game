/*
 * Copyright (c) 2009-2012 jMonkeyEngine
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
package mini.input;

/**
 * A specific API for interfacing with the keyboard.
 */
public interface KeyInput extends Input {
    /**
     * unmapped key.
     */
    int KEY_UNKNOWN = 0x00;

    /**
     * escape key.
     */
    int KEY_ESCAPE = 0x01;
    /**
     * 1 key.
     */
    int KEY_1 = 0x02;
    /**
     * 2 key.
     */
    int KEY_2 = 0x03;
    /**
     * 3 key.
     */
    int KEY_3 = 0x04;
    /**
     * 4 key.
     */
    int KEY_4 = 0x05;
    /**
     * 5 key.
     */
    int KEY_5 = 0x06;
    /**
     * 6 key.
     */
    int KEY_6 = 0x07;
    /**
     * 7 key.
     */
    int KEY_7 = 0x08;
    /**
     * 8 key.
     */
    int KEY_8 = 0x09;
    /**
     * 9 key.
     */
    int KEY_9 = 0x0A;
    /**
     * 0 key.
     */
    int KEY_0 = 0x0B;
    /**
     * - key.
     */
    int KEY_MINUS = 0x0C;
    /**
     * = key.
     */
    int KEY_EQUALS = 0x0D;
    /**
     * back key.
     */
    int KEY_BACK = 0x0E;
    /**
     * tab key.
     */
    int KEY_TAB = 0x0F;
    /**
     * q key.
     */
    int KEY_Q = 0x10;
    /**
     * w key.
     */
    int KEY_W = 0x11;
    /**
     * e key.
     */
    int KEY_E = 0x12;
    /**
     * r key.
     */
    int KEY_R = 0x13;
    /**
     * t key.
     */
    int KEY_T = 0x14;
    /**
     * y key.
     */
    int KEY_Y = 0x15;
    /**
     * u key.
     */
    int KEY_U = 0x16;
    /**
     * i key.
     */
    int KEY_I = 0x17;
    /**
     * o key.
     */
    int KEY_O = 0x18;
    /**
     * p key.
     */
    int KEY_P = 0x19;
    /**
     * [ key.
     */
    int KEY_LBRACKET = 0x1A;
    /**
     * ] key.
     */
    int KEY_RBRACKET = 0x1B;
    /**
     * enter (main keyboard) key.
     */
    int KEY_RETURN = 0x1C;
    /**
     * left control key.
     */
    int KEY_LCONTROL = 0x1D;
    /**
     * a key.
     */
    int KEY_A = 0x1E;
    /**
     * s key.
     */
    int KEY_S = 0x1F;
    /**
     * d key.
     */
    int KEY_D = 0x20;
    /**
     * f key.
     */
    int KEY_F = 0x21;
    /**
     * g key.
     */
    int KEY_G = 0x22;
    /**
     * h key.
     */
    int KEY_H = 0x23;
    /**
     * j key.
     */
    int KEY_J = 0x24;
    /**
     * k key.
     */
    int KEY_K = 0x25;
    /**
     * l key.
     */
    int KEY_L = 0x26;
    /**
     * ; key.
     */
    int KEY_SEMICOLON = 0x27;
    /**
     * ' key.
     */
    int KEY_APOSTROPHE = 0x28;
    /**
     * ` key.
     */
    int KEY_GRAVE = 0x29;
    /**
     * left shift key.
     */
    int KEY_LSHIFT = 0x2A;
    /**
     * \ key.
     */
    int KEY_BACKSLASH = 0x2B;
    /**
     * z key.
     */
    int KEY_Z = 0x2C;
    /**
     * x key.
     */
    int KEY_X = 0x2D;
    /**
     * c key.
     */
    int KEY_C = 0x2E;
    /**
     * v key.
     */
    int KEY_V = 0x2F;
    /**
     * b key.
     */
    int KEY_B = 0x30;
    /**
     * n key.
     */
    int KEY_N = 0x31;
    /**
     * m key.
     */
    int KEY_M = 0x32;
    /**
     * , key.
     */
    int KEY_COMMA = 0x33;
    /**
     * . key (main keyboard).
     */
    int KEY_PERIOD = 0x34;
    /**
     * / key (main keyboard).
     */
    int KEY_SLASH = 0x35;
    /**
     * right shift key.
     */
    int KEY_RSHIFT = 0x36;
    /**
     * * key (on keypad).
     */
    int KEY_MULTIPLY = 0x37;
    /**
     * left alt key.
     */
    int KEY_LMENU = 0x38;
    /**
     * space key.
     */
    int KEY_SPACE = 0x39;
    /**
     * caps lock key.
     */
    int KEY_CAPITAL = 0x3A;
    /**
     * F1 key.
     */
    int KEY_F1 = 0x3B;
    /**
     * F2 key.
     */
    int KEY_F2 = 0x3C;
    /**
     * F3 key.
     */
    int KEY_F3 = 0x3D;
    /**
     * F4 key.
     */
    int KEY_F4 = 0x3E;
    /**
     * F5 key.
     */
    int KEY_F5 = 0x3F;
    /**
     * F6 key.
     */
    int KEY_F6 = 0x40;
    /**
     * F7 key.
     */
    int KEY_F7 = 0x41;
    /**
     * F8 key.
     */
    int KEY_F8 = 0x42;
    /**
     * F9 key.
     */
    int KEY_F9 = 0x43;
    /**
     * F10 key.
     */
    int KEY_F10 = 0x44;
    /**
     * NumLK key.
     */
    int KEY_NUMLOCK = 0x45;
    /**
     * Scroll lock key.
     */
    int KEY_SCROLL = 0x46;
    /**
     * 7 key (num pad).
     */
    int KEY_NUMPAD7 = 0x47;
    /**
     * 8 key (num pad).
     */
    int KEY_NUMPAD8 = 0x48;
    /**
     * 9 key (num pad).
     */
    int KEY_NUMPAD9 = 0x49;
    /**
     * - key (num pad).
     */
    int KEY_SUBTRACT = 0x4A;
    /**
     * 4 key (num pad).
     */
    int KEY_NUMPAD4 = 0x4B;
    /**
     * 5 key (num pad).
     */
    int KEY_NUMPAD5 = 0x4C;
    /**
     * 6 key (num pad).
     */
    int KEY_NUMPAD6 = 0x4D;
    /**
     * + key (num pad).
     */
    int KEY_ADD = 0x4E;
    /**
     * 1 key (num pad).
     */
    int KEY_NUMPAD1 = 0x4F;
    /**
     * 2 key (num pad).
     */
    int KEY_NUMPAD2 = 0x50;
    /**
     * 3 key (num pad).
     */
    int KEY_NUMPAD3 = 0x51;
    /**
     * 0 key (num pad).
     */
    int KEY_NUMPAD0 = 0x52;
    /**
     * . key (num pad).
     */
    int KEY_DECIMAL = 0x53;
    /**
     * F11 key.
     */
    int KEY_F11 = 0x57;
    /**
     * F12 key.
     */
    int KEY_F12 = 0x58;
    /**
     * F13 key.
     */
    int KEY_F13 = 0x64;
    /**
     * F14 key.
     */
    int KEY_F14 = 0x65;
    /**
     * F15 key.
     */
    int KEY_F15 = 0x66;
    /**
     * kana key (Japanese).
     */
    int KEY_KANA = 0x70;
    /**
     * convert key (Japanese).
     */
    int KEY_CONVERT = 0x79;
    /**
     * noconvert key (Japanese).
     */
    int KEY_NOCONVERT = 0x7B;
    /**
     * yen key (Japanese).
     */
    int KEY_YEN = 0x7D;
    /**
     * = on num pad (NEC PC98).
     */
    int KEY_NUMPADEQUALS = 0x8D;
    /**
     * circum flex key (Japanese).
     */
    int KEY_CIRCUMFLEX = 0x90;
    /**
     * &#064; key (NEC PC98).
     */
    int KEY_AT = 0x91;
    /**
     * : key (NEC PC98)
     */
    int KEY_COLON = 0x92;
    /**
     * _ key (NEC PC98).
     */
    int KEY_UNDERLINE = 0x93;
    /**
     * kanji key (Japanese).
     */
    int KEY_KANJI = 0x94;
    /**
     * stop key (NEC PC98).
     */
    int KEY_STOP = 0x95;
    /**
     * ax key (Japanese).
     */
    int KEY_AX = 0x96;
    /**
     * (J3100).
     */
    int KEY_UNLABELED = 0x97;
    /**
     * Enter key (num pad).
     */
    int KEY_NUMPADENTER = 0x9C;
    /**
     * right control key.
     */
    int KEY_RCONTROL = 0x9D;
    /**
     * , key on num pad (NEC PC98).
     */
    int KEY_NUMPADCOMMA = 0xB3;
    /**
     * / key (num pad).
     */
    int KEY_DIVIDE = 0xB5;
    /**
     * SysRq key.
     */
    int KEY_SYSRQ = 0xB7;
    /**
     * right alt key.
     */
    int KEY_RMENU = 0xB8;
    /**
     * pause key.
     */
    int KEY_PAUSE = 0xC5;
    /**
     * home key.
     */
    int KEY_HOME = 0xC7;
    /**
     * up arrow key.
     */
    int KEY_UP = 0xC8;
    /**
     * PgUp key.
     */
    int KEY_PRIOR = 0xC9;
    /**
     * PgUp key.
     */
    int KEY_PGUP = KEY_PRIOR;

    /**
     * left arrow key.
     */
    int KEY_LEFT = 0xCB;
    /**
     * right arrow key.
     */
    int KEY_RIGHT = 0xCD;
    /**
     * end key.
     */
    int KEY_END = 0xCF;
    /**
     * down arrow key.
     */
    int KEY_DOWN = 0xD0;
    /**
     * PgDn key.
     */
    int KEY_NEXT = 0xD1;
    /**
     * PgDn key.
     */
    int KEY_PGDN = KEY_NEXT;

    /**
     * insert key.
     */
    int KEY_INSERT = 0xD2;
    /**
     * delete key.
     */
    int KEY_DELETE = 0xD3;

    /**
     * Left "Windows" key on PC keyboards, left "Option" key on Mac keyboards.
     */
    int KEY_LMETA  = 0xDB;

    /**
     * Right "Windows" key on PC keyboards, right "Option" key on Mac keyboards.
     */
    int KEY_RMETA = 0xDC;

    int KEY_APPS = 0xDD;
    /**
     * power key.
     */
    int KEY_POWER = 0xDE;
    /**
     * sleep key.
     */
    int KEY_SLEEP = 0xDF;

    /**
     * the last key.
     */
    int KEY_LAST = 0xE0;
}
