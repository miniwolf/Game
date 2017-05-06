package mini.shaders.tools;

/**
 * Static tool box class for convenient methods to help debug shaders
 *
 * @author miniwolf
 */
public class ShaderDebug {

    /**
     * Append the line numbers to the source code of a shader to output it
     *
     * @param source the source
     * @return the formatted source code
     */
    public static String formatShaderSource(String source) {
        String[] sourceLines = source.split("\n");
        int nblines = 0;
        StringBuilder out = new StringBuilder();
        for (String string : sourceLines) {
            nblines++;
            out.append(nblines).append("\t").append(string).append("\n");
        }
        return out.toString();
    }
}
