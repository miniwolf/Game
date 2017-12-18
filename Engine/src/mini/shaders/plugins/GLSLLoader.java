package mini.shaders.plugins;

import mini.asset.AssetInfo;
import mini.asset.AssetKey;
import mini.asset.AssetLoader;
import mini.utils.MyFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * GLSL File parser that supports #import pre-processor statement
 */
public class GLSLLoader implements AssetLoader {
    private Map<String, ShaderDependencyNode> dependCache = new HashMap<>();

    public Object load(AssetInfo info) {
        // The input stream provided is for the vertex shader,
        // to retrieve the fragment shader, use the content manager
        try (Reader reader = new InputStreamReader(info.openStream())) {
            if (info.getKey().getExtension().equals("glsllib")) {
                // NOTE: Loopback, GLSLLIB is loaded by this loader
                // and needs data as InputStream
                return reader;
            } else {
                ShaderDependencyNode rootNode = loadNode(reader, "[main]");
                StringBuilder extensions = new StringBuilder();
                String code = resolveDependencies(rootNode, new HashSet<>(), extensions);
                extensions.append(code);
                dependCache.clear();
                return extensions.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a {@link ShaderDependencyNode} from a stream representing shader code.
     *
     * @param in       The input stream containing shader code
     * @param nodeName
     * @return
     * @throws IOException
     */
    private ShaderDependencyNode loadNode(Reader reader, String nodeName) {
        ShaderDependencyNode node = new ShaderDependencyNode(nodeName);

        StringBuilder sb = new StringBuilder();
        StringBuilder sbExt = new StringBuilder();
        BufferedReader bufReader = null;
        try {
            bufReader = new BufferedReader(reader);
            String ln;
            if (!nodeName.equals("[main]")) {
                sb.append("// -- begin import ").append(nodeName).append(" --\n");
            }
            while ((ln = bufReader.readLine()) != null) {
                if (ln.trim().startsWith("#import ")) {
                    ln = ln.trim().substring(8).trim();
                    if (ln.startsWith("\"") && ln.endsWith("\"") && ln.length() > 3) {
                        // import user code
                        // remove quotes to get filename
                        ln = ln.substring(1, ln.length() - 1);
                        if (ln.equals(nodeName)) {
                            throw new IOException("Node depends on itself.");
                        }

                        // check cache first
                        ShaderDependencyNode dependNode = dependCache.get(ln);

                        if (dependNode == null) {
                            Reader dependNodeReader = new InputStreamReader(
                                    new MyFile(ln)
                                            .getInputStream()); // TODO: This seems fishy. Should load asset using assetManager
                            dependNode = loadNode(dependNodeReader, ln);
                        }

                        node.addDependency(sb.length(), dependNode);
                    }
                } else if (ln.trim().startsWith("#extension ")) {
                    sbExt.append(ln).append('\n');
                } else {
                    sb.append(ln).append('\n');
                }
            }
            if (!nodeName.equals("[main]")) {
                sb.append("// -- end import ").append(nodeName).append(" --\n");
            }
        } catch (IOException ex) {
            try {
                bufReader.close();
            } catch (IOException ignored) {
            }
            throw new RuntimeException("Failed to load shader node: " + nodeName, ex);
        }

        node.setSource(sb.toString());
        node.setExtensions(sbExt.toString());
        dependCache.put(nodeName, node);
        return node;
    }

    private String resolveDependencies(ShaderDependencyNode node,
                                       Set<ShaderDependencyNode> alreadyInjectedSet,
                                       StringBuilder extensions) {
        if (alreadyInjectedSet.contains(node)) {
            return "// " + node.getName() + " was already injected at the top.\n";
        } else {
            alreadyInjectedSet.add(node);
        }
        if (!node.getExtensions().isEmpty()) {
            extensions.append(node.getExtensions());
        }
        if (node.getDependencies().isEmpty()) {
            return node.getSource();
        } else {
            StringBuilder sb = new StringBuilder(node.getSource());
            List<String> resolvedShaderNodes = new ArrayList<>();

            for (ShaderDependencyNode dependencyNode : node.getDependencies()) {
                resolvedShaderNodes
                        .add(resolveDependencies(dependencyNode, alreadyInjectedSet, extensions));
            }
            List<Integer> injectIndices = node.getDependencyInjectIndices();
            for (int i = resolvedShaderNodes.size() - 1; i >= 0; i--) {
                // Must insert them backwards ..
                sb.insert(injectIndices.get(i), resolvedShaderNodes.get(i));
            }
            return sb.toString();
        }
    }

    /**
     * Used to load {@link ShaderDependencyNode}s.
     * Asset caching is disabled.
     */
    private class ShaderDependencyKey extends AssetKey {
        public ShaderDependencyKey(String name) {
            super(name);
        }
    }
}
