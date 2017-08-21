package mini.material.plugins;

import mini.asset.ShaderNodeDefinitionKey;
import mini.utils.blockparser.BlockLanguageParser;
import mini.utils.blockparser.Statement;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * ShaderNodeDefnition file loader (.j3sn)
 * <p>
 * a minisn file is a block style file like minid or mini. It must contain one
 * ShaderNodeDefinition{} block that contains several ShaderNodeDefinition{}
 * blocks
 */
public class ShaderNodeDefinitionLoader {

    public static Object load(ShaderNodeDefinitionKey key) throws IOException {
        ShaderNodeLoaderDelegate loaderDelegate = new ShaderNodeLoaderDelegate();

        InputStream in = key.getFile().getInputStream();
        List<Statement> roots = BlockLanguageParser.parse(in);

        if (roots.size() == 2) {
            Statement exception = roots.get(0);
            String line = exception.getLine();
            if (line.startsWith("Exception")) {
                throw new RuntimeException(line.substring("Exception ".length()));
            } else {
                throw new MatParseException(
                        "In multiroot shader node definition, expected first statement to be 'Exception'",
                        exception);
            }
        } else if (roots.size() != 1) {
            throw new MatParseException("Too many roots in J3SN file", roots.get(0));
        }

        return loaderDelegate.readNodesDefinitions(roots.get(0).getContents(), key);
    }
}
