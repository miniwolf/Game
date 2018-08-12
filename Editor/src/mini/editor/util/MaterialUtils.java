package mini.editor.util;

import com.ss.rlib.common.util.FileUtils;
import com.ss.rlib.common.util.ObjectUtils;
import com.ss.rlib.common.util.StringUtils;
import mini.asset.AssetKey;
import mini.asset.MaterialKey;
import mini.asset.TextureKey;
import mini.editor.FileExtensions;
import mini.editor.annotation.FromAnyThread;
import mini.editor.annotation.EditorThread;
import mini.material.Material;
import mini.material.MaterialDef;
import mini.material.TechniqueDef;
import mini.shaders.VarType;
import mini.textures.Texture;

import java.nio.file.Path;

public class MaterialUtils {
    public static boolean isShaderFile(Path file) {
        var extension = FileUtils.getExtension(file);
        return FileExtensions.GLSL_FRAGMENT.equals(extension)
               || FileExtensions.GLSL_VERTEX.equals(extension);
    }

    public static boolean isTextureFile(Path file) {
        var extension = FileUtils.getExtension(file);
        return FileExtensions.IMAGE_DDS.equals(extension)
               || FileExtensions.IMAGE_HDR.equals(extension)
               || FileExtensions.IMAGE_JPEG.equals(extension)
               || FileExtensions.IMAGE_JPG.equals(extension)
               || FileExtensions.IMAGE_PNG.equals(extension)
               || FileExtensions.IMAGE_TGA.equals(extension);
    }

    @EditorThread
    public static Material updateMaterialIdNeed(
            Path file,
            Material material) {
        var assetManager = EditorUtil.getAssetManager();

        boolean needsReload = false;
        String textureKey = null;

        if (isShaderFile(file)) {
            if (!containsShader(material, file)) {
                return null;
            }

            needsReload = true;

            // If the shader was changed we need to reload the material definition
            var materialDef = material.getMaterialDef();
            var assetName = materialDef.getAssetName();
            assetManager.deleteFromCache(new AssetKey<>(assetName));
        } else if (isTextureFile(file)) {
            textureKey = containsTexture(material, file);
            if (textureKey == null) {
                return null;
            }
        }

        var assetName = material.getAssetName();

        // Attempt to refresh texture directly
        if (textureKey != null) {
            refreshTextures(material, textureKey);
            return null;
        } else if (!needsReload || assetName == null || assetName.isEmpty()) {
            return null;
        }

        var materialKey = new MaterialKey(assetName);
        assetManager.deleteFromCache(materialKey);

        var newMaterial = new Material(assetManager, material.getMaterialDef().getAssetName());
        migrateTo(newMaterial, material);

        return newMaterial;
    }

    @EditorThread
    private static void migrateTo(
            Material target,
            Material source) {
        var materialDef = target.getMaterialDef();
        var actualParams = source.getParams();
        actualParams.forEach(matParam -> {
            var param = materialDef.getMaterialParam(matParam.getName());
            if (param == null || param.getVarType() != matParam.getVarType()) {
                return;
            }

            target.setParam(matParam.getName(), matParam.getVarType(), matParam.getValue());
        });

        var additionRenderState = target.getAdditionalRenderState();
        additionRenderState.set(source.getAdditionalRenderState());

        target.setKey(source.getKey());
    }

    @EditorThread
    private static void refreshTextures(
            Material material,
            String textureKey) {
        var assetManager = EditorUtil.getAssetManager();
        material.getParams().forEach(matParam -> {
            var varType = matParam.getVarType();
            var value = matParam.getValue();

            if (varType != VarType.Texture2D || value == null) {
                return;
            }

            var texture = (Texture) value;
            var key = (TextureKey) texture.getKey();
            if (key != null && StringUtils.equals(key.getName(), textureKey)) {
                var newTexture = assetManager.loadAsset(key);
                matParam.setValue(newTexture);
            }
        });
    }

    /**
     * Checks the material if the texture represented in the file is already been added.
     *
     * @return The texture key or null.
     */
    private static String containsTexture(
            Material material,
            Path file) {
        var assetFile = ObjectUtils.notNull(
                EditorUtil.getAssetFile(file),
                "Cannot get the asset file");
        var assetPath = EditorUtil.toAssetPath(assetFile);

        return containsTexture(material, assetPath) ? assetPath : null;
    }

    private static boolean containsTexture(
            Material material,
            String assetPath) {
        var materialParams = material.getParams();
        for (var materialParam : materialParams) {
            if (materialParam.getVarType() != VarType.Texture2D) {
                continue;
            }

            var value = (Texture) materialParam.getValue();
            var textureKey = value == null ? null : (TextureKey) value.getKey();
            if (textureKey != null && StringUtils.equals(textureKey.getName(), assetPath)) {
                return true;
            }
        }
        return false;
    }

    @FromAnyThread
    private static boolean containsShader(
            Material material,
            Path file) {
        var materialDef = material.getMaterialDef();
        var assetFile = ObjectUtils.notNull(
                EditorUtil.getAssetFile(file),
                "Cannot get the asset file");
        var assetPath = EditorUtil.toAssetPath(assetFile);

        return containsShader(materialDef, assetPath);
    }

    @FromAnyThread
    private static boolean containsShader(
            MaterialDef materialDef,
            String assetPath) {
        var defaultTechniques = materialDef.getTechniqueDefs("Default");

        return defaultTechniques
                .stream()
                .map(TechniqueDef::getShaderProgramNames)
                .anyMatch(shaderProgramNames ->
                                  shaderProgramNames.containsValue(assetPath));
    }
}
