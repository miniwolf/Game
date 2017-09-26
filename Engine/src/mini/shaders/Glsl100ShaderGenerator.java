package mini.shaders;

import mini.asset.ShaderNodeDefinitionKey;
import mini.material.ShaderGenerationInfo;
import mini.material.TechniqueDef;
import mini.material.plugins.ConditionParser;
import mini.shaders.plugins.GLSLLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This shader Generator can generate Vertex and Fragment shaders from
 * shadernodes for GLSL 1.0
 */
public class Glsl100ShaderGenerator {
    /**
     * indentation value for generation
     */
    private int indent;
    /**
     * the technique def to use for the shader generation
     */
    private TechniqueDef techniqueDef = null;
    /**
     * Extension pattern
     */
    private Pattern extensions = Pattern.compile("(#extension.*\\s+)");

    /**
     * the indentation characters 1à tabulation characters
     */
    private final static String INDENTCHAR = "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t";
    private ShaderNodeVariable inPosTmp;

    public void initialize(TechniqueDef techniqueDef) {
        this.techniqueDef = techniqueDef;
    }

    /**
     * parses the source and moves all the extensions at the top of the shader source as having extension declarations
     * in the middle of a shader is against the specs and not supported by all drivers.
     *
     * @param sourceDeclaration
     * @return String containing shader source code
     */
    private String moveExtensionsUp(StringBuilder sourceDeclaration) {
        Matcher m = extensions.matcher(sourceDeclaration.toString());
        StringBuilder finalSource = new StringBuilder();
        while (m.find()) {
            finalSource.append(m.group());
        }
        finalSource.append(m.replaceAll(""));
        return finalSource.toString();
    }

    /**
     * Appends declaration and main part of a node to the shader declaration and
     * main part. the loadedSource is split by "void main(){" to split
     * declaration from main part of the node source code.The trailing "}" is
     * removed from the main part. Each part is then respectively passed to
     * generateDeclarativeSection and generateNodeMainSection.
     *
     * @param loadedSource      the actual source code loaded for this node.
     * @param shaderPath        path the the shader file
     * @param sourceDeclaration the Shader declaration part string builder.
     * @param source            the Shader main part StringBuilder.
     * @param shaderNode        the shader node.
     * @param info              the ShaderGenerationInfo.
     * @see ShaderGenerator#generateDeclarativeSection
     * @see ShaderGenerator#generateNodeMainSection
     */
    private void appendNodeDeclarationAndMain(String loadedSource, StringBuilder sourceDeclaration,
                                              StringBuilder source, ShaderNode shaderNode,
                                              ShaderGenerationInfo info, String shaderPath) {
        if (loadedSource.length() > 1) {
            loadedSource = loadedSource.substring(0, loadedSource.lastIndexOf("}"));
            String[] sourceParts = loadedSource.split("\\s*void\\s*main\\s*\\(\\s*\\)\\s*\\{");
            if (sourceParts.length < 2) {
                throw new IllegalArgumentException(
                        "Syntax error in " + shaderPath + ". Cannot find 'void main(){' in \n"
                        + loadedSource);
            }
            generateDeclarativeSection(sourceDeclaration, shaderNode, sourceParts[0], info);
            generateNodeMainSection(source, shaderNode, sourceParts[1], info);
        } else {
            //if source is empty, we still call generateNodeMainSection so that mappings can be done.
            generateNodeMainSection(source, shaderNode, loadedSource, info);
        }
    }

    /**
     * returns the shaderpath index according to the version of the generator.
     * This allow to select the higher version of the shader that the generator
     * can handle
     *
     * @param shaderNode the shaderNode being processed
     * @param type       the shaderType
     * @return the index of the shader path in ShaderNodeDefinition shadersPath list
     * @throws NumberFormatException
     */
    protected int findShaderIndexFromVersion(ShaderNode shaderNode, Shader.ShaderType type)
            throws NumberFormatException {
        int index = 0;
        List<String> lang = shaderNode.getDefinition().getShadersLanguage();
        int genVersion = Integer.parseInt(getLanguageAndVersion(type).substring(4));
        int curVersion = 0;
        for (int i = 0; i < lang.size(); i++) {
            int version = Integer.parseInt(lang.get(i).substring(4));
            if (version > curVersion && version <= genVersion) {
                curVersion = version;
                index = i;
            }
        }
        return index;
    }

    /**
     * iterates through shader nodes to load them and generate the shader
     * declaration part and main body extracted from the shader nodes, for the
     * given shader type
     *
     * @param shaderNodes       the list of shader nodes
     * @param sourceDeclaration the declaration part StringBuilder of the shader to generate
     * @param source            the main part StringBuilder of the shader to generate
     * @param info              the ShaderGenerationInfo
     * @param type              the Shader type
     */
    private void generateDeclarationAndMainBody(List<ShaderNode> shaderNodes,
                                                StringBuilder sourceDeclaration,
                                                StringBuilder source, ShaderGenerationInfo info,
                                                Shader.ShaderType type) {
        for (ShaderNode shaderNode : shaderNodes) {
            if (info.getUnusedNodes().contains(shaderNode.getName())) {
                continue;
            }
            if (shaderNode.getDefinition().getType() == type) {
                int index = findShaderIndexFromVersion(shaderNode, type);
                String shaderPath = shaderNode.getDefinition().getShadersPath().get(index);
                String loadedSource = null;
                try {
                    loadedSource = (String) GLSLLoader.load(new ShaderNodeDefinitionKey(shaderPath));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                appendNodeDeclarationAndMain(loadedSource, sourceDeclaration, source, shaderNode,
                                             info, shaderPath);
            }
        }
    }

    /**
     * This method is responsible for the shader generation.
     *
     * @param shaderNodes the list of shader nodes
     * @param info        the ShaderGenerationInfo filled during the Technique loading
     * @param type        the type of shader to generate
     * @return the code of the generated vertex shader
     */
    protected String buildShader(List<ShaderNode> shaderNodes, ShaderGenerationInfo info,
                                 Shader.ShaderType type) {
        if (type == Shader.ShaderType.TessellationControl ||
            type == Shader.ShaderType.TessellationEvaluation ||
            type == Shader.ShaderType.Geometry) {
            // TODO: Those are not supported.
            // Too much code assumes that type is either Vertex or Fragment
            return null;
        }

        indent = 0;
        StringBuilder sourceDeclaration = new StringBuilder();
        StringBuilder source = new StringBuilder();

        generateUniforms(sourceDeclaration, info, type);
        if (type == Shader.ShaderType.Vertex) {
            generateAttributes(sourceDeclaration, info);
        }

        generateVaryings(sourceDeclaration, info, type);
        generateStartOfMainSection(source, info, type);
        generateDeclarationAndMainBody(shaderNodes, sourceDeclaration, source, info, type);
        generateEndOfMainSection(source, info, type);

        sourceDeclaration.append(source);
        return moveExtensionsUp(sourceDeclaration);
    }

    /**
     * Generate vertex and fragment shaders for the given technique
     *
     * @return a Shader program
     */
    public Shader generateShader(String definesSourceCode) {
        if (techniqueDef == null) {
            throw new UnsupportedOperationException("The shaderGenerator was not "
                                                    + "properly initialized, call "
                                                    + "initialize(TechniqueDef) before any generation");
        }

        String techniqueName = techniqueDef.getName();
        ShaderGenerationInfo info = techniqueDef.getShaderGenerationInfo();

        Shader shader = new Shader();
        for (Shader.ShaderType type : Shader.ShaderType.values()) {
            String extension = type.getExtension();
            String language = getLanguageAndVersion(type);
            String shaderSourceCode = buildShader(techniqueDef.getShaderNodes(), info, type);

            if (shaderSourceCode != null) {
                String shaderSourceAssetName = techniqueName + "." + extension;
                shader.addSource(type, shaderSourceAssetName, shaderSourceCode, definesSourceCode,
                                 language);
            }
        }

        techniqueDef = null;
        return shader;
    }

    private void generateUniforms(StringBuilder source, ShaderGenerationInfo info,
                                  Shader.ShaderType type) {
        generateUniforms(source,
                         type == Shader.ShaderType.Vertex ? info.getVertexUniforms() :
                         info.getFragmentUniforms());
    }

    /**
     * declare a list of uniforms
     *
     * @param source   the source to append to
     * @param uniforms the list of uniforms
     */
    private void generateUniforms(StringBuilder source, List<ShaderNodeVariable> uniforms) {
        source.append("\n");
        for (ShaderNodeVariable var : uniforms) {
            declareVariable(source, var, false, "uniform");
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * attributes are all declared, inPositon is declared even if it's not in
     * the list and it's condition is nulled.
     */
    private void generateAttributes(StringBuilder source, ShaderGenerationInfo info) {
        source.append("\n");
        boolean inPosition = false;
        for (ShaderNodeVariable var : info.getAttributes()) {
            if (var.getName().equals("inPosition")) {
                inPosition = true;
                var.setCondition(null);
                fixInPositionType(var);
                //keep track on the InPosition variable to avoid iterating through attributes again
                inPosTmp = var;
            }
            declareAttribute(source, var);

        }
        if (!inPosition) {
            inPosTmp = new ShaderNodeVariable("vec3", "inPosition");
            declareAttribute(source, inPosTmp);
        }

    }

    private void generateVaryings(StringBuilder source, ShaderGenerationInfo info,
                                  Shader.ShaderType type) {
        source.append("\n");
        info.getVaryings()
            .forEach(var -> declareVarying(source, var, type != Shader.ShaderType.Vertex));
    }

    /**
     * {@inheritDoc}
     * <p>
     * if the declaration contains no code nothing is done, else it's appended
     */
    private void generateDeclarativeSection(StringBuilder source, ShaderNode shaderNode,
                                            String nodeSource, ShaderGenerationInfo info) {
        if (nodeSource.replaceAll("\\n", "").trim().length() > 0) {
            nodeSource = updateDefinesName(nodeSource, shaderNode);
            source.append("\n");
            unIndent();
            startCondition(shaderNode.getCondition(), source);
            source.append(nodeSource);
            source.append("\n");
            endCondition(shaderNode.getCondition(), source);
            indent();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Shader outputs are declared and initialized inside the main section
     */
    private void generateStartOfMainSection(StringBuilder source, ShaderGenerationInfo info,
                                            Shader.ShaderType type) {
        source.append("\n");
        source.append("void main(){\n");
        indent();
        appendIndent(source);
        if (type == Shader.ShaderType.Vertex) {
            declareGlobalPosition(info, source);
        } else if (type == Shader.ShaderType.Fragment) {
            for (ShaderNodeVariable global : info.getFragmentGlobals()) {
                declareVariable(source, global, "vec4(1.0)");
            }
        }
        source.append("\n");
    }

    /**
     * outputs are assigned to built in glsl output. then the main section is
     * closed
     * <p>
     * This code accounts for multi render target and correctly output to
     * gl_FragData if several output are declared for the fragment shader
     */
    private void generateEndOfMainSection(StringBuilder source, ShaderGenerationInfo info,
                                          Shader.ShaderType type) {
        source.append("\n");
        if (type == Shader.ShaderType.Vertex) {
            appendOutput(source, "gl_Position", info.getVertexGlobal());
        } else if (type == Shader.ShaderType.Fragment) {
            List<ShaderNodeVariable> globals = info.getFragmentGlobals();
            if (globals.size() == 1) {
                appendOutput(source, "gl_FragColor", globals.get(0));
            } else {
                int i = 0;
                //Multi Render Target
                for (ShaderNodeVariable global : globals) {
                    appendOutput(source, "gl_FragData[" + i + "]", global);
                    i++;
                }
            }
        }
        unIndent();
        appendIndent(source);
        source.append("}\n");
    }

    /**
     * Appends an output assignment to a shader globalOutputName = nameSpace_varName;
     *
     * @param source           the source StringBuilter to append the code.
     * @param globalOutputName the name of the global output (can be gl_Position or gl_FragColor
     *                         etc...).
     * @param var              the variable to assign to the output.
     */
    protected void appendOutput(StringBuilder source, String globalOutputName,
                                ShaderNodeVariable var) {
        appendIndent(source);
        source.append(globalOutputName);
        source.append(" = ");
        source.append(var.getNameSpace());
        source.append("_");
        source.append(var.getName());
        source.append(";\n");
    }

    /**
     * this methods does things in this order :
     * <p>
     * 1. declaring and mapping input<br>
     * variables : variable replaced with MatParams or WorldParams that are Samplers are not
     * declared and are replaced by the param actual name in the code. For others
     * variables, the name space is appended with a "_" before the variable name
     * in the code to avoid names collision between shaderNodes. <br>
     * <p>
     * 2. declaring output variables : <br>
     * variables are declared if they were not already
     * declared as input (inputs can also be outputs) or if they are not
     * declared as varyings. The variable name is also prefixed with the s=name
     * space and "_" in the shaderNode code <br>
     * <p>
     * 3. append of the actual ShaderNode code <br>
     * <p>
     * 4. mapping outputs to global output if needed<br>
     * <p>
     * <br>
     * All of this is embed in a #if conditional statement if needed
     */
    private void generateNodeMainSection(StringBuilder source, ShaderNode shaderNode,
                                         String nodeSource, ShaderGenerationInfo info) {

        nodeSource = updateDefinesName(nodeSource, shaderNode);
        source.append("\n");
        comment(source, shaderNode, "Begin");
        startCondition(shaderNode.getCondition(), source);

        List<String> declaredInputs = new ArrayList<String>();
        for (VariableMapping mapping : shaderNode.getInputMapping()) {

            //Variables fed with a sampler matparam or world param are replaced by the matparam itself
            //It avoids issue with samplers that have to be uniforms.
            if (isWorldOrMaterialParam(mapping.getRightVariable()) && mapping.getRightVariable()
                                                                             .getType().startsWith(
                            "sampler")) {
                nodeSource = replace(nodeSource, mapping.getLeftVariable(),
                                     mapping.getRightVariable().getPrefix() + mapping
                                             .getRightVariable().getName());
            } else {
                if (mapping.getLeftVariable().getType().startsWith("sampler")) {
                    throw new IllegalArgumentException("a Sampler must be a uniform");
                }
                map(mapping, source);
                String newName = shaderNode.getName() + "_" + mapping.getLeftVariable().getName();
                if (!declaredInputs.contains(newName)) {
                    nodeSource = replace(nodeSource, mapping.getLeftVariable(), newName);
                    declaredInputs.add(newName);
                }
            }
        }

        for (ShaderNodeVariable var : shaderNode.getDefinition().getOutputs()) {
            ShaderNodeVariable v = new ShaderNodeVariable(var.getType(), shaderNode.getName(),
                                                          var.getName(), var.getMultiplicity());
            if (!declaredInputs.contains(shaderNode.getName() + "_" + var.getName())) {
                if (!isVarying(info, v)) {
                    declareVariable(source, v);
                }
                nodeSource = replaceVariableName(nodeSource, v);
            }
        }

        source.append(nodeSource);

        for (VariableMapping mapping : shaderNode.getOutputMapping()) {
            map(mapping, source);
        }
        endCondition(shaderNode.getCondition(), source);
        comment(source, shaderNode, "End");
    }

    /**
     * declares a variable, embed in a conditional block if needed
     *
     * @param source          the StringBuilder to use
     * @param var             the variable to declare
     * @param appendNameSpace true to append the nameSpace + "_"
     */
    protected void declareVariable(StringBuilder source, ShaderNodeVariable var,
                                   boolean appendNameSpace) {
        declareVariable(source, var, appendNameSpace, null);
    }

    /**
     * declares a variable, embed in a conditional block if needed. the namespace is appended with "_"
     *
     * @param source the StringBuilder to use
     * @param var    the variable to declare
     */
    private void declareVariable(StringBuilder source, ShaderNodeVariable var) {
        declareVariable(source, var, true, null);
    }

    /**
     * declares a variable, embed in a conditional block if needed. the namespace is appended with "_"
     *
     * @param source the StringBuilder to use
     * @param var    the variable to declare
     * @param value  the initialization value to assign the the variable
     */
    private void declareVariable(StringBuilder source, ShaderNodeVariable var, String value) {
        declareVariable(source, var, value, true, null);
    }

    /**
     * declares a variable, embed in a conditional block if needed.
     *
     * @param source          the StringBuilder to use
     * @param var             the variable to declare
     * @param appendNameSpace true to append the nameSpace + "_"
     * @param modifier        the modifier of the variable (attribute, varying, in , out,...)
     */
    private void declareVariable(StringBuilder source, ShaderNodeVariable var,
                                 boolean appendNameSpace, String modifier) {
        declareVariable(source, var, null, appendNameSpace, modifier);
    }

    /**
     * declares a variable, embed in a conditional block if needed.
     *
     * @param source          the StringBuilder to use
     * @param var             the variable to declare
     * @param value           the initialization value to assign the the variable
     * @param appendNameSpace true to append the nameSpace + "_"
     * @param modifier        the modifier of the variable (attribute, varying, in , out,...)
     */
    private void declareVariable(StringBuilder source, ShaderNodeVariable var, String value,
                                 boolean appendNameSpace, String modifier) {
        startCondition(var.getCondition(), source);
        appendIndent(source);
        if (modifier != null) {
            source.append(modifier);
            source.append(" ");
        }

        source.append(var.getType());
        source.append(" ");
        if (appendNameSpace) {
            source.append(var.getNameSpace());
            source.append("_");
        }
        source.append(var.getPrefix());
        source.append(var.getName());
        if (var.getMultiplicity() != null) {
            source.append("[");
            source.append(var.getMultiplicity().toUpperCase());
            source.append("]");
        }
        if (value != null) {
            source.append(" = ");
            source.append(value);
        }
        source.append(";\n");
        endCondition(var.getCondition(), source);
    }

    /**
     * Starts a conditional block
     *
     * @param condition the block condition
     * @param source    the StringBuilder to use
     */
    protected void startCondition(String condition, StringBuilder source) {
        if (condition != null) {
            appendIndent(source);
            source.append("#if ");
            source.append(condition);
            source.append("\n");
            indent();
        }
    }

    /**
     * Ends a conditional block
     *
     * @param condition the block condition
     * @param source    the StringBuilder to use
     */
    protected void endCondition(String condition, StringBuilder source) {
        if (condition != null) {
            unIndent();
            appendIndent(source);
            source.append("#endif\n");

        }
    }

    /**
     * Appends a mapping to the source, embed in a conditional block if needed,
     * with variables nameSpaces and swizzle.
     *
     * @param mapping the VariableMapping to append
     * @param source  the StringBuilder to use
     */
    protected void map(VariableMapping mapping, StringBuilder source) {
        startCondition(mapping.getCondition(), source);
        appendIndent(source);
        if (!mapping.getLeftVariable().isShaderOutput()) {
            source.append(mapping.getLeftVariable().getType());
            source.append(" ");
        }
        source.append(mapping.getLeftVariable().getNameSpace());
        source.append("_");
        source.append(mapping.getLeftVariable().getName());
        if (mapping.getLeftVariable().getMultiplicity() != null) {
            source.append("[");
            source.append(mapping.getLeftVariable().getMultiplicity());
            source.append("]");
        }

        //left swizzle, the variable can't be declared and assigned on the same line.
        if (mapping.getLeftSwizzling().length() > 0) {
            //initialize the declared variable to 0.0
            source.append(" = ");
            source.append(mapping.getLeftVariable().getType());
            source.append("(0.0);\n");
            appendIndent(source);
            //assign the value on a new line
            source.append(mapping.getLeftVariable().getNameSpace());
            source.append("_");
            source.append(mapping.getLeftVariable().getName());
            source.append(".");
            source.append(mapping.getLeftSwizzling());
        }
        source.append(" = ");
        String namePrefix = getAppendableNameSpace(mapping.getRightVariable());
        source.append(namePrefix);
        source.append(mapping.getRightVariable().getPrefix());
        source.append(mapping.getRightVariable().getName());
        if (mapping.getRightSwizzling().length() > 0) {
            source.append(".");
            source.append(mapping.getRightSwizzling());
        }
        source.append(";\n");
        endCondition(mapping.getCondition(), source);
    }

    /**
     * replaces a variable name in a shaderNode source code by prefixing it
     * with its nameSpace and "_" if needed.
     *
     * @param nodeSource the source to modify
     * @param var        the variable to replace
     * @return the modified source
     */
    protected String replaceVariableName(String nodeSource, ShaderNodeVariable var) {
        String namePrefix = getAppendableNameSpace(var);
        String newName = namePrefix + var.getName();
        nodeSource = replace(nodeSource, var, newName);
        return nodeSource;
    }

    /**
     * Finds if a variable is a varying
     *
     * @param info the ShaderGenerationInfo
     * @param v    the variable
     * @return true is the given variable is a varying
     */
    protected boolean isVarying(ShaderGenerationInfo info, ShaderNodeVariable v) {
        boolean isVarying = false;
        for (ShaderNodeVariable shaderNodeVariable : info.getVaryings()) {
            if (shaderNodeVariable.equals(v)) {
                isVarying = true;
            }
        }
        return isVarying;
    }

    /**
     * Appends a comment to the generated code
     *
     * @param source     the StringBuilder to use
     * @param shaderNode the shader node being processed (to append its name)
     * @param comment    the comment to append
     */
    protected void comment(StringBuilder source, ShaderNode shaderNode, String comment) {
        appendIndent(source);
        source.append("//");
        source.append(shaderNode.getName());
        source.append(" : ");
        source.append(comment);
        source.append("\n");
    }

    /**
     * returns the name space to append for a variable.
     * Attributes, WorldParam and MatParam names space must not be appended
     *
     * @param var the variable
     * @return the namespace to append for this variable
     */
    protected String getAppendableNameSpace(ShaderNodeVariable var) {
        String namePrefix = var.getNameSpace() + "_";
        if (namePrefix.equals("Attr_") || namePrefix.equals("WorldParam_") || namePrefix
                .equals("MatParam_")) {
            namePrefix = "";
        }
        return namePrefix;
    }

    /**
     * transforms defines name is the shader node code.
     * One can use a #if defined(inputVariableName) in a shaderNode code.
     * This method is responsible for changing the variable name with the
     * appropriate defined based on the mapping condition of this variable.
     * Complex condition syntax are handled.
     *
     * @param nodeSource the sahderNode source code
     * @param shaderNode the ShaderNode being processed
     * @return the modified shaderNode source.
     */
    protected String updateDefinesName(String nodeSource, ShaderNode shaderNode) {
        String[] lines = nodeSource.split("\\n");
        ConditionParser parser = new ConditionParser();
        for (String line : lines) {

            if (line.trim().startsWith("#if")) {
                List<String> params = parser.extractDefines(line.trim());
                String l = line.trim().replaceAll("defined", "").replaceAll("#if ", "")
                               .replaceAll("#ifdef", "");
                boolean match = false;
                for (String param : params) {
                    for (VariableMapping map : shaderNode.getInputMapping()) {
                        if ((map.getLeftVariable().getName()).equals(param)) {
                            if (map.getCondition() != null) {
                                l = l.replaceAll(param, map.getCondition());
                                match = true;
                            }
                        }
                    }
                }
                if (match) {
                    nodeSource = nodeSource.replace(line.trim(), "#if " + l);
                }
            }
        }
        return nodeSource;
    }

    /**
     * replaced a variable name in a source code with the given name
     *
     * @param nodeSource the source to use
     * @param var        the variable
     * @param newName    the new name of the variable
     * @return the modified source code
     */
    protected String replace(String nodeSource, ShaderNodeVariable var, String newName) {
        nodeSource = nodeSource.replaceAll("(?<=\\W)" + var.getName() + "(?=\\W)", newName);
        return nodeSource;
    }

    /**
     * Finds if a variable is a world or a material parameter
     *
     * @param var the variable
     * @return true if the variable is a Word or material parameter
     */
    protected boolean isWorldOrMaterialParam(ShaderNodeVariable var) {
        return var.getNameSpace().equals("MatParam") || var.getNameSpace().equals("WorldParam");
    }

    protected String getLanguageAndVersion(Shader.ShaderType type) {
        return "GLSL100";
    }

    /**
     * appends indentation.
     *
     * @param source
     */
    protected void appendIndent(StringBuilder source) {
        source.append(INDENTCHAR.substring(0, indent));
    }

    /**
     * Declares an attribute
     *
     * @param source the StringBuilder to use
     * @param var    the variable to declare as an attribute
     */
    protected void declareAttribute(StringBuilder source, ShaderNodeVariable var) {
        declareVariable(source, var, false, "attribute");
    }

    /**
     * Declares a varying
     *
     * @param source the StringBuilder to use
     * @param var    the variable to declare as an varying
     * @param input  a boolean set to true if the this varying is an input.
     *               this in not used in this implementation but can be used in overridings
     *               implementation
     */
    protected void declareVarying(StringBuilder source, ShaderNodeVariable var, boolean input) {
        declareVariable(source, var, true, "varying");
    }

    /**
     * Decrease indentation with a check so the indent is never negative.
     */
    protected void unIndent() {
        indent--;
        indent = Math.max(0, indent);
    }

    /**
     * increase indentation with a check so that indentation is never over 10
     */
    protected void indent() {
        indent++;
        indent = Math.min(10, indent);
    }

    /**
     * makes sure inPosition attribute is of type vec3 or vec4
     *
     * @param var the inPosition attribute
     */
    private void fixInPositionType(ShaderNodeVariable var) {
        if (!var.getType().equals("vec3") || !var.getType().equals("vec4")) {
            var.setType("vec3");
        }
    }

    /**
     * declare and assign the global position in the vertex shader.
     *
     * @param info   the shader generation info
     * @param source the shader source being generated
     */
    protected void declareGlobalPosition(ShaderGenerationInfo info, StringBuilder source) {
        declareVariable(source, info.getVertexGlobal(),
                        inPosTmp.getType().equals(info.getVertexGlobal().getType())
                        ? "inPosition"
                        : "vec4(inPosition,1.0)");
    }
}
