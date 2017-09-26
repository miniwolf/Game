package mini.font.plugins;

import mini.asset.FontKey;
import mini.asset.MaterialKey;
import mini.asset.TextureKey;
import mini.font.BitmapCharacter;
import mini.font.BitmapCharacterSet;
import mini.font.BitmapFont;
import mini.material.Material;
import mini.material.MaterialDef;
import mini.material.RenderState;
import mini.material.plugins.MiniLoader;
import mini.textures.Texture;
import mini.textures.plugins.AWTLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BitmapFontLoader {
    public static BitmapFont loadFont(FontKey fontKey) {
        InputStream in = fontKey.getFile().getInputStream();
        BitmapFontLoader loader = new BitmapFontLoader();
        BitmapFont load = null;
        try {
            load = loader.load(fontKey.getFile().getDirectory(), in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return load;
    }

    public BitmapFont load(String folder, InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        BitmapCharacterSet charSet = new BitmapCharacterSet();
        Material[] matPages = null;
        MaterialDef spriteMat = (MaterialDef) MiniLoader
                .load(new MaterialKey("MatDefs/Misc/Unshaded.minid"));
        BitmapFont font = new BitmapFont();

        String regex = "[\\s=]+";
        font.setCharSet(charSet);
        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split(regex);
            if ("char".equals(tokens[0])) {
                BitmapCharacter ch = null;
                for (int i = 1; i < tokens.length; i++) {
                    String token = tokens[i];
                    if ("id".equals(token)) {
                        int idx = Integer.parseInt(tokens[i + 1]);
                        ch = new BitmapCharacter();
                        charSet.addCharacter(idx, ch);
                    } else if ("x".equals(token)) {
                        ch.setX(Integer.parseInt(tokens[i + 1]));
                    } else if ("y".equals(token)) {
                        ch.setY(Integer.parseInt(tokens[i + 1]));
                    } else if ("width".equals(token)) {
                        ch.setWidth(Integer.parseInt(tokens[i + 1]));
                    } else if ("height".equals(token)) {
                        ch.setHeight(Integer.parseInt(tokens[i + 1]));
                    } else if ("xoffset".equals(token)) {
                        ch.setXOffset(Integer.parseInt(tokens[i + 1]));
                    } else if ("yoffset".equals(token)) {
                        ch.setYOffset(Integer.parseInt(tokens[i + 1]));
                    } else if ("xadvance".equals(token)) {
                        ch.setXAdvance(Integer.parseInt(tokens[i + 1]));
                    } else if ("page".equals(token)) {
                        ch.setPage(Integer.parseInt(tokens[i + 1]));
                    }
                }
            } else if ("info".equals(tokens[0])) {
                for (int i = 1; i < tokens.length; i++) {
                    if ("size".equals(tokens[i])) {
                        charSet.setRenderedSize(Integer.parseInt(tokens[i + 1]));
                    }
                }
            } else if ("common".equals(tokens[0])) {
                for (int i = 1; i < tokens.length; i++) {
                    String token = tokens[i];
                    if ("lineHeight".equals(token)) {
                        charSet.setLineHeight(Integer.parseInt(tokens[i + 1]));
                    } else if ("base".equals(token)) {
                        charSet.setBase(Integer.parseInt(tokens[i + 1]));
                    } else if ("scaleW".equals(token)) {
                        charSet.setWidth(Integer.parseInt(tokens[i + 1]));
                    } else if ("scaleH".equals(token)) {
                        charSet.setHeight(Integer.parseInt(tokens[i + 1]));
                    } else if ("pages".equals(token)) { // number of texture pages
                        matPages = new Material[Integer.parseInt(tokens[i + 1])];
                        font.setPages(matPages);
                    }
                }
            } else if ("page".equals(tokens[0])) {
                Texture tex = null;
                int index = 0;
                for (int i = 1; i < tokens.length; i++) {
                    String token = tokens[i];
                    if ("id".equals(token)) {
                        index = Integer.parseInt(tokens[i + 1]);
                    } else if ("file".equals(token)) {
                        String file = tokens[i + 1];
                        if (file.startsWith("\"")) {
                            file = file.substring(1, file.length() - 1);
                        }
                        TextureKey key = new TextureKey(folder + file, true);
                        key.setGenerateMips(false);
                        tex = (Texture) AWTLoader.load(key);
                        tex.setMagFilter(Texture.MagFilter.Bilinear);
                        tex.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);
                    }
                }

                // setup page
                Material mat = new Material(spriteMat);
                mat.setTexture("ColorMap", tex);
                mat.setBoolean("VertexColor", true);
                mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
                matPages[index] = mat;
            } else if ("kerning".equals(tokens[0])) {
                // TODO: Build kerning lists
            }
        }
        return font;
    }
}
