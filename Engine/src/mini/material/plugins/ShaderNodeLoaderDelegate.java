package mini.material.plugins;

import mini.asset.ShaderNodeDefinitionKey;
import mini.material.MatParam;
import mini.material.MaterialDef;
import mini.material.ShaderGenerationInfo;
import mini.material.TechniqueDef;
import mini.shaders.ShaderNode;
import mini.shaders.ShaderNodeDefinition;
import mini.shaders.ShaderNodeVariable;
import mini.shaders.ShaderProgram;
import mini.shaders.ShaderUtils;
import mini.shaders.UniformBinding;
import mini.shaders.VarType;
import mini.shaders.VariableMapping;
import mini.utils.blockparser.Statement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is here to be able to load shaderNodeDefinition from both the
 * J3MLoader and ShaderNodeDefinitionLoader.
 * <p>
 * Also it allows to load the ShaderNodes from a minid file and build the
 * ShaderNodes list of each technique and the ShaderGenerationInfo needed to
 * generate shaders
 */
public class ShaderNodeLoaderDelegate {

    protected Map<String, ShaderNodeDefinition> nodeDefinitions;
    protected Map<String, ShaderNode> nodes;
    protected ShaderNodeDefinition shaderNodeDefinition;
    protected ShaderNode shaderNode;
    protected TechniqueDef techniqueDef;
    protected Map<String, DeclaredVariable> attributes = new HashMap<>();
    protected Map<String, DeclaredVariable> vertexDeclaredUniforms = new HashMap<>();
    protected Map<String, DeclaredVariable> fragmentDeclaredUniforms = new HashMap<>();
    protected Map<String, DeclaredVariable> varyings = new HashMap<>();
    protected MaterialDef materialDef;
    protected String shaderLanguage;
    protected String shaderName;
    protected String varNames = "";
    protected ConditionParser conditionParser = new ConditionParser();
    protected List<String> nulledConditions = new ArrayList<>();

    protected class DeclaredVariable {

        ShaderNodeVariable var;
        List<ShaderNode> nodes = new ArrayList<>();

        public DeclaredVariable(ShaderNodeVariable var) {
            this.var = var;
        }

        public final void addNode(ShaderNode c) {
            if (!nodes.contains(c)) {
                nodes.add(c);
            }
        }
    }

    /**
     * Read the ShaderNodesDefinitions block and returns a list of
     * ShaderNodesDefinition This method is used by the j3sn loader
     * <p>
     * note that the order of the definitions in the list is not guaranteed.
     *
     * @param statements the list statements to parse
     * @param key        the ShaderNodeDefinitionKey
     * @return a list of ShaderNodesDefinition
     * @throws IOException
     */
    public List<ShaderNodeDefinition> readNodesDefinitions(List<Statement> statements,
                                                           ShaderNodeDefinitionKey key)
            throws IOException {

        for (Statement statement : statements) {
            String[] split = statement.getLine().split("[ {]");
            if (statement.getLine().startsWith("ShaderNodeDefinition")) {
                String name = statement.getLine().substring("ShaderNodeDefinition".length()).trim();

                if (!getNodeDefinitions().containsKey(name)) {
                    shaderNodeDefinition = new ShaderNodeDefinition();
                    getNodeDefinitions().put(name, shaderNodeDefinition);
                    shaderNodeDefinition.setName(name);
                    shaderNodeDefinition.setPath(key.getFile().getName());
                    readShaderNodeDefinition(statement.getContents(), key);

                }
            } else {
                throw new MatParseException("ShaderNodeDefinition", split[0], statement);
            }
        }

        return new ArrayList<>(getNodeDefinitions().values());
    }

    /**
     * Read the ShaderNodesDefinitions block and internally stores a map of
     * ShaderNodesDefinition This method is used by the j3m loader.
     * <p>
     * When loaded in a material, the definitions are not stored as a list, but
     * they are stores in Shadernodes based on this definition.
     * <p>
     * The map is here to map the definition to the nodes, and ovoid reloading
     * already loaded definitions
     *
     * @param statements the list of statements to parse
     * @throws IOException
     */
    public void readNodesDefinitions(List<Statement> statements) throws IOException {
        readNodesDefinitions(statements, new ShaderNodeDefinitionKey());
    }

    /**
     * effectively reads the ShaderNodesDefinitions block
     *
     * @param statements the list of statements to parse
     * @param key        the ShaderNodeDefinitionKey
     * @throws IOException
     */
    protected void readShaderNodeDefinition(List<Statement> statements, ShaderNodeDefinitionKey key)
            throws IOException {
        boolean isLoadDoc = key != null && key.isLoadDocumentation();
        for (Statement statement : statements) {
            try {
                String[] split = statement.getLine().split("[ {]");
                String line = statement.getLine();

                if (line.startsWith("Type")) {
                    String type = line.substring(line.lastIndexOf(':') + 1).trim();
                    shaderNodeDefinition.setType(ShaderProgram.ShaderType.valueOf(type));
                } else if (line.startsWith("Shader ")) {
                    readShaderStatement(statement);
                    shaderNodeDefinition.getShadersLanguage().add(shaderLanguage);
                    shaderNodeDefinition.getShadersPath().add(shaderName);
                } else if (line.startsWith("Documentation")) {
                    if (isLoadDoc) {
                        StringBuilder doc = new StringBuilder();
                        for (Statement statement1 : statement.getContents()) {
                            doc.append("\n").append(statement1.getLine());
                        }
                        shaderNodeDefinition.setDocumentation(doc.toString());
                    }
                } else if (line.startsWith("Input")) {
                    varNames = "";
                    for (Statement statement1 : statement.getContents()) {
                        try {
                            shaderNodeDefinition.getInputs().add(readVariable(statement1));
                        } catch (RuntimeException e) {
                            throw new MatParseException(e.getMessage(), statement1, e);
                        }
                    }
                } else if (line.startsWith("Output")) {
                    varNames = "";
                    for (Statement statement1 : statement.getContents()) {
                        try {
                            if (statement1.getLine().trim().equals("None")) {
                                shaderNodeDefinition.setNoOutput(true);
                            } else {
                                shaderNodeDefinition.getOutputs().add(readVariable(statement1));
                            }
                        } catch (RuntimeException e) {
                            throw new MatParseException(e.getMessage(), statement1, e);
                        }
                    }
                } else {
                    throw new MatParseException("one of Type, Shader, Documentation, Input, Output",
                                                split[0], statement);
                }
            } catch (RuntimeException e) {
                throw new MatParseException(e.getMessage(), statement, e);
            }
        }
    }

    /**
     * reads a variable declaration statement &lt;glslType&gt; &lt;varName&gt;
     *
     * @param statement the statement to parse
     * @return a ShaderNodeVariable extracted from the statement
     * @throws IOException
     */
    protected ShaderNodeVariable readVariable(Statement statement) throws IOException {
        String line = statement.getLine().trim().replaceAll("\\s*\\[", "[");
        String[] splitVar = line.split("\\s");
        if (splitVar.length != 2) {
            throw new MatParseException("2 arguments", splitVar.length + "", statement);
        }
        String varName = splitVar[1];
        String varType = splitVar[0];
        String multiplicity = null;

        if (varName.contains("[")) {
            //we have an array
            String[] arr = splitVar[1].split("\\[");
            varName = arr[0].trim();
            multiplicity = arr[1].replaceAll("]", "").trim();
        }
        if (varNames.contains(varName + ";")) {
            throw new MatParseException("Duplicate variable name " + varName, statement);
        }
        varNames += varName + ";";
        return new ShaderNodeVariable(varType, "", varName, multiplicity);
    }

    /**
     * reads the VertexShaderNodes{} block
     *
     * @param statements the list of statements to parse
     * @throws IOException
     */
    public void readVertexShaderNodes(List<Statement> statements) throws IOException {
        attributes.clear();
        readNodes(statements);
    }

    /**
     * reads a list of ShaderNode{} blocks
     *
     * @param statements the list of statements to parse
     * @throws IOException
     */
    protected void readShaderNode(List<Statement> statements) throws IOException {
        for (Statement statement : statements) {
            String line = statement.getLine();
            String[] split = statement.getLine().split("[ {]");
            if (line.startsWith("Definition")) {
                ShaderNodeDefinition def = findDefinition(statement);
                shaderNode.setDefinition(def);
                if (def.isNoOutput()) {
                    techniqueDef.getShaderGenerationInfo().getUnusedNodes()
                                .remove(shaderNode.getName());
                }
            } else if (line.startsWith("Condition")) {
                String condition = line.substring(line.lastIndexOf(":") + 1).trim();
                extractCondition(condition, statement);
                shaderNode.setCondition(conditionParser.getFormattedExpression());
            } else if (line.startsWith("InputMapping")) {
                for (Statement statement1 : statement.getContents()) {
                    VariableMapping mapping = readInputMapping(statement1);
                    techniqueDef.getShaderGenerationInfo().getUnusedNodes()
                                .remove(mapping.getRightVariable().getNameSpace());
                    shaderNode.getInputMapping().add(mapping);
                }
            } else if (line.startsWith("OutputMapping")) {
                for (Statement statement1 : statement.getContents()) {
                    VariableMapping mapping = readOutputMapping(statement1);
                    techniqueDef.getShaderGenerationInfo().getUnusedNodes()
                                .remove(shaderNode.getName());
                    shaderNode.getOutputMapping().add(mapping);
                }
            } else {
                throw new MatParseException("ShaderNodeDefinition", split[0], statement);
            }
        }

    }

    /**
     * reads a mapping statement. Sets the nameSpace, name and swizzling of the
     * left variable. Sets the name, nameSpace and swizzling of the right
     * variable types will be determined later.
     * <p>
     * <code>
     * Format : <nameSpace>.<varName>[.<swizzling>] =
     * <nameSpace>.<varName>[.<swizzling>][:Condition]
     * </code>
     *
     * @param statement the statement to read
     * @return the read mapping
     */
    protected VariableMapping parseMapping(Statement statement, boolean[] hasNameSpace)
            throws IOException {
        VariableMapping mapping = new VariableMapping();
        String[] cond = statement.getLine().split(":");

        String[] vars = cond[0].split("=");
        checkMappingFormat(vars, statement);
        ShaderNodeVariable[] variables = new ShaderNodeVariable[2];
        String[] swizzle = new String[2];
        for (int i = 0; i < vars.length; i++) {
            String[] expression = vars[i].trim().split("\\.");
            if (hasNameSpace[i]) {
                if (expression.length <= 3) {
                    variables[i] = new ShaderNodeVariable("", expression[0].trim(),
                                                          expression[1].trim());
                }
                if (expression.length == 3) {
                    swizzle[i] = expression[2].trim();
                }
            } else {
                if (expression.length <= 2) {
                    variables[i] = new ShaderNodeVariable("", expression[0].trim());
                }
                if (expression.length == 2) {
                    swizzle[i] = expression[1].trim();
                }
            }

        }

        mapping.setLeftVariable(variables[0]);
        mapping.setLeftSwizzling(swizzle[0] != null ? swizzle[0] : "");
        mapping.setRightVariable(variables[1]);
        mapping.setRightSwizzling(swizzle[1] != null ? swizzle[1] : "");

        if (cond.length > 1) {
            extractCondition(cond[1], statement);
            mapping.setCondition(conditionParser.getFormattedExpression());
        }

        return mapping;
    }

    /**
     * reads the FragmentShaderNodes{} block
     *
     * @param statements the list of statements to parse
     * @throws IOException
     */
    public void readFragmentShaderNodes(List<Statement> statements) throws IOException {
        readNodes(statements);
    }

    /**
     * Reads a Shader statement of this form <TYPE> <LANG> : <SOURCE>
     *
     * @param statement
     * @throws IOException
     */
    protected void readShaderStatement(Statement statement) throws IOException {
        String[] split = statement.getLine().split(":");
        if (split.length != 2) {
            throw new MatParseException("Shader statement syntax incorrect", statement);
        }
        String[] typeAndLang = split[0].split("\\p{javaWhitespace}+");
        if (typeAndLang.length != 2) {
            throw new MatParseException("Shader statement syntax incorrect", statement);
        }
        shaderName = split[1].trim();
        shaderLanguage = typeAndLang[1];
    }

    /**
     * Sets the technique definition currently being loaded
     *
     * @param techniqueDef the technique def
     */
    public void setTechniqueDef(TechniqueDef techniqueDef) {
        this.techniqueDef = techniqueDef;
    }

    /**
     * sets the material def currently being loaded
     *
     * @param materialDef
     */
    public void setMaterialDef(MaterialDef materialDef) {
        this.materialDef = materialDef;
    }

    /**
     * search a variable in the given list and updates its type and namespace
     *
     * @param var  the variable to update
     * @param list the variables list
     * @return true if the variable has been found and updated
     */
    protected boolean updateVariableFromList(ShaderNodeVariable var,
                                             List<ShaderNodeVariable> list) {
        for (ShaderNodeVariable shaderNodeVariable : list) {
            if (shaderNodeVariable.getName().equals(var.getName())) {
                var.setType(shaderNodeVariable.getType());
                var.setMultiplicity(shaderNodeVariable.getMultiplicity());
                var.setNameSpace(shaderNode.getName());
                return true;
            }
        }
        return false;
    }

    /**
     * updates the type of the right variable of a mapping from the type of the
     * left variable
     *
     * @param mapping the mapping to consider
     */
    protected void updateRightTypeFromLeftType(VariableMapping mapping) {
        String type = mapping.getLeftVariable().getType();
        int card = ShaderUtils.getCardinality(type, mapping.getRightSwizzling());
        if (card > 0) {
            if (card == 1) {
                type = "float";
            } else {
                type = "vec" + card;
            }
        }
        mapping.getRightVariable().setType(type);
    }

    /**
     * check if once a mapping expression is split by "=" the resulting array
     * have 2 elements
     *
     * @param vars      the array
     * @param statement the statement
     * @throws IOException
     */
    protected void checkMappingFormat(String[] vars, Statement statement) throws IOException {
        if (vars.length != 2) {
            throw new MatParseException(
                    "Not a valid expression should be '<varName>[.<swizzling>] = <nameSpace>.<varName>[.<swizzling>][:Condition]'",
                    statement);
        }
    }

    /**
     * finds a MatParam in the materialDef from the given name
     *
     * @param varName the matparam name
     * @return the MatParam
     */
    protected MatParam findMatParam(String varName) {
        for (MatParam matParam : materialDef.getMaterialParams()) {
            if (varName.equals(matParam.getName())) {
                return matParam;
            }
        }
        return null;
    }

    /**
     * finds an UniformBinding representing a WorldParam from the techniqueDef
     *
     * @param varName the name of the WorldParam
     * @return the corresponding UniformBinding to the WorldParam
     */
    protected UniformBinding findWorldParam(String varName) {
        for (UniformBinding worldParam : techniqueDef.getWorldBindings()) {
            if (varName.equals(worldParam.toString())) {
                return worldParam;
            }
        }
        return null;
    }

    /**
     * updates the right variable of the given mapping from a UniformBinding (a
     * WorldParam) it checks if the uniform hasn't already been loaded, add it
     * to the maps if not.
     *
     * @param param   the WorldParam UniformBinding
     * @param mapping the mapping
     * @param map     the map of uniforms to search into
     * @return true if the param was added to the map
     */
    protected boolean updateRightFromUniforms(UniformBinding param, VariableMapping mapping,
                                              Map<String, DeclaredVariable> map) {
        ShaderNodeVariable right = mapping.getRightVariable();
        String name = param.toString();

        DeclaredVariable dv = map.get(name);
        if (dv == null) {
            right.setType(param.getGlslType());
            right.setName(name);
            right.setPrefix("g_");
            dv = new DeclaredVariable(right);
            map.put(right.getName(), dv);
            dv.addNode(shaderNode);
            mapping.setRightVariable(right);
            return true;
        }
        dv.addNode(shaderNode);
        mapping.setRightVariable(dv.var);
        return false;
    }

    /**
     * updates the right variable of the given mapping from a MatParam (a
     * WorldParam) it checks if the uniform hasn't already been loaded, add it
     * to the maps if not.
     *
     * @param param   the MatParam
     * @param mapping the mapping
     * @param map     the map of uniforms to search into
     * @return true if the param was added to the map
     */
    public boolean updateRightFromUniforms(MatParam param, VariableMapping mapping,
                                           Map<String, DeclaredVariable> map, Statement statement)
            throws MatParseException {
        ShaderNodeVariable right = mapping.getRightVariable();
        DeclaredVariable dv = map.get(param.getName());
        if (dv == null) {
            right.setType(param.getVarType().getGlslType());
            right.setName(param.getName());
            right.setPrefix("m_");
            if (mapping.getLeftVariable().getMultiplicity() != null) {
                if (!param.getVarType().name().endsWith("Array")) {
                    throw new MatParseException(param.getName() + " is not of Array type",
                                                statement);
                }
                String multiplicity = mapping.getLeftVariable().getMultiplicity();
                try { // TODO: Stop hacking this and use a regex...
                    Integer.parseInt(multiplicity);
                } catch (NumberFormatException nfe) {
                    //multiplicity is not an int attempting to find for a material parameter.
                    MatParam mp = findMatParam(multiplicity);
                    if (mp != null) {
                        //It's tied to a material param, let's create a define and use this as the multiplicity
                        addDefine(multiplicity, VarType.Int);
                        multiplicity = multiplicity.toUpperCase();
                        mapping.getLeftVariable().setMultiplicity(multiplicity);
                        //only declare the variable if the define is defined.
                        mapping.getLeftVariable().setCondition(
                                mergeConditions(mapping.getLeftVariable().getCondition(),
                                                "defined(" + multiplicity + ")", "||"));
                    } else {
                        throw new MatParseException(
                                "Wrong multiplicity for variable"
                                + mapping.getLeftVariable().getName() + ". " + multiplicity
                                + " should be an int or a declared material parameter.", statement);
                    }
                }
                //the right variable must have the same multiplicity and the same condition.
                right.setMultiplicity(multiplicity);
                right.setCondition(mapping.getLeftVariable().getCondition());
            }
            dv = new DeclaredVariable(right);
            map.put(right.getName(), dv);
            dv.addNode(shaderNode);
            mapping.setRightVariable(right);
            return true;
        }
        dv.addNode(shaderNode);
        mapping.setRightVariable(dv.var);
        return false;
    }

    /**
     * updates a variable from the Attribute list
     *
     * @param right   the variable
     * @param mapping the mapping
     */
    public void updateVarFromAttributes(ShaderNodeVariable right, VariableMapping mapping) {
        DeclaredVariable dv = attributes.get(right.getName());
        if (dv == null) {
            dv = new DeclaredVariable(right);
            attributes.put(right.getName(), dv);
            updateRightTypeFromLeftType(mapping);
        } else {
            mapping.setRightVariable(dv.var);
        }
        dv.addNode(shaderNode);
    }

    /**
     * Adds a define to the technique def
     *
     * @param paramName
     */
    public void addDefine(String paramName, VarType paramType) {
        if (techniqueDef.getShaderParamDefine(paramName) == null) {
            techniqueDef.addShaderParamDefine(paramName, paramType, paramName.toUpperCase());
        }
    }

    /**
     * find a variable with the given name from the list of variable
     *
     * @param vars         a list of shaderNodeVariables
     * @param rightVarName the variable name to search for
     * @return the found variable or null is not found
     */
    public ShaderNodeVariable findNodeOutput(List<ShaderNodeVariable> vars, String rightVarName) {
        ShaderNodeVariable var = null;
        for (ShaderNodeVariable variable : vars) {
            if (variable.getName().equals(rightVarName)) {
                var = variable;
            }
        }
        return var;
    }

    /**
     * extract and check a condition expression
     *
     * @param cond      the condition expression
     * @param statement the statement being read
     * @throws IOException
     */
    public void extractCondition(String cond, Statement statement) throws IOException {
        List<String> defines = conditionParser.extractDefines(cond);
        for (String string : defines) {
            MatParam param = findMatParam(string);
            if (param != null) {
                addDefine(param.getName(), param.getVarType());
            } else {
                throw new MatParseException(
                        "Invalid condition, condition must match a Material Parameter named "
                        + cond, statement);
            }
        }
    }

    /**
     * reads an input mapping
     *
     * @param statement1 the statement being read
     * @return the mapping
     * @throws IOException
     */
    public VariableMapping readInputMapping(Statement statement1) throws IOException {
        VariableMapping mapping;
        try {
            mapping = parseMapping(statement1, new boolean[]{false, true});
        } catch (Exception e) {
            throw new MatParseException("Unexpected mapping format", statement1, e);
        }
        ShaderNodeVariable left = mapping.getLeftVariable();
        ShaderNodeVariable right = mapping.getRightVariable();
        if (!updateVariableFromList(left, shaderNode.getDefinition().getInputs())) {
            throw new MatParseException(
                    left.getName() + " is not an input variable of " + shaderNode.getDefinition()
                                                                                 .getName(),
                    statement1);
        }

        if (left.getType().startsWith("sampler") && !right.getNameSpace().equals("MatParam")) {
            throw new MatParseException("Samplers can only be assigned to MatParams", statement1);
        }

        switch (right.getNameSpace()) {
            case "Global":
                right.setType("vec4");//Globals are all vec4 for now (maybe forever...)
                storeGlobal(right, statement1);
                break;
            case "Attr":
                if (shaderNode.getDefinition().getType() == ShaderProgram.ShaderType.Fragment) {
                    throw new MatParseException(
                            "Cannot have an attribute as input in a fragment shader" + right
                                    .getName(), statement1);
                }
                updateVarFromAttributes(mapping.getRightVariable(), mapping);
                storeAttribute(mapping.getRightVariable());
                break;
            case "MatParam":
                MatParam param = findMatParam(right.getName());
                if (param == null) {
                    throw new MatParseException(
                            "Could not find a Material Parameter named " + right.getName(),
                            statement1);
                }
                if (shaderNode.getDefinition().getType() == ShaderProgram.ShaderType.Vertex) {
                    if (updateRightFromUniforms(param, mapping, vertexDeclaredUniforms,
                                                statement1)) {
                        storeVertexUniform(mapping.getRightVariable());
                    }
                } else {
                    if (updateRightFromUniforms(param, mapping, fragmentDeclaredUniforms,
                                                statement1)) {
                        if (mapping.getRightVariable().getType().contains("|")) {
                            String type = fixSamplerType(left.getType(),
                                                         mapping.getRightVariable().getType());
                            if (type != null) {
                                mapping.getRightVariable().setType(type);
                            } else {
                                throw new MatParseException(param.getVarType().toString()
                                                            + " can only be matched to one of "
                                                            + param.getVarType().getGlslType()
                                                                   .replaceAll("\\|", ",")
                                                            + " found " + left.getType(),
                                                            statement1);
                            }
                        }
                        storeFragmentUniform(mapping.getRightVariable());
                    }
                }

                break;
            case "WorldParam":
                UniformBinding worldParam = findWorldParam(right.getName());
                if (worldParam == null) {
                    throw new MatParseException(
                            "Could not find a World Parameter named " + right.getName(),
                            statement1);
                }
                if (shaderNode.getDefinition().getType() == ShaderProgram.ShaderType.Vertex) {
                    if (updateRightFromUniforms(worldParam, mapping, vertexDeclaredUniforms)) {
                        storeVertexUniform(mapping.getRightVariable());
                    }
                } else {
                    if (updateRightFromUniforms(worldParam, mapping, fragmentDeclaredUniforms)) {
                        storeFragmentUniform(mapping.getRightVariable());
                    }
                }

                break;
            default:
                ShaderNode node = nodes.get(right.getNameSpace());
                if (node == null) {
                    throw new MatParseException("Undeclared node" + right.getNameSpace()
                                                + ". Make sure this node is declared before the current node",
                                                statement1);
                }
                ShaderNodeVariable var = findNodeOutput(node.getDefinition().getOutputs(),
                                                        right.getName());
                if (var == null) {
                    throw new MatParseException(
                            "Cannot find output variable" + right.getName() + " form ShaderNode "
                            + node.getName(), statement1);
                }
                right.setNameSpace(node.getName());
                right.setType(var.getType());
                right.setMultiplicity(var.getMultiplicity());
                mapping.setRightVariable(right);
                storeVaryings(node, mapping.getRightVariable());

                break;
        }

        checkTypes(mapping, statement1);

        return mapping;
    }

    /**
     * reads an output mapping
     *
     * @param statement1 the statement being read
     * @return the mapping
     * @throws IOException
     */
    public VariableMapping readOutputMapping(Statement statement1) throws IOException {
        VariableMapping mapping;
        try {
            mapping = parseMapping(statement1, new boolean[]{true, false});
        } catch (Exception e) {
            throw new MatParseException("Unexpected mapping format", statement1, e);
        }
        ShaderNodeVariable left = mapping.getLeftVariable();
        ShaderNodeVariable right = mapping.getRightVariable();

        if (left.getType().startsWith("sampler") || right.getType().startsWith("sampler")) {
            throw new MatParseException("Samplers can only be inputs", statement1);
        }

        if (left.getNameSpace().equals("Global")) {
            left.setType("vec4");//Globals are all vec4 for now (maybe forever...)
            storeGlobal(left, statement1);
        } else {
            throw new MatParseException(
                    "Only Global nameSpace is allowed for outputMapping, got" + left.getNameSpace(),
                    statement1);
        }

        if (!updateVariableFromList(right, shaderNode.getDefinition().getOutputs())) {
            throw new MatParseException(
                    right.getName() + " is not an output variable of " + shaderNode.getDefinition()
                                                                                   .getName(),
                    statement1);
        }

        checkTypes(mapping, statement1);

        return mapping;
    }

    /**
     * Reads a list of ShaderNodes
     *
     * @param statements the list of statements to read
     * @throws IOException
     */
    public void readNodes(List<Statement> statements) throws IOException {
        if (techniqueDef.getShaderNodes() == null) {
            techniqueDef.setShaderNodes(new ArrayList<>());
            techniqueDef.setShaderGenerationInfo(new ShaderGenerationInfo());
        }

        for (Statement statement : statements) {
            String[] split = statement.getLine().split("[ {]");
            if (statement.getLine().startsWith("ShaderNode ")) {
                String name = statement.getLine().substring("ShaderNode".length()).trim();
                if (nodes == null) {
                    nodes = new HashMap<>();
                }
                if (!nodes.containsKey(name)) {
                    shaderNode = new ShaderNode();
                    shaderNode.setName(name);
                    techniqueDef.getShaderGenerationInfo().getUnusedNodes().add(name);

                    readShaderNode(statement.getContents());
                    nodes.put(name, shaderNode);
                    techniqueDef.getShaderNodes().add(shaderNode);
                } else {
                    throw new MatParseException("ShaderNode " + name + " is already defined",
                                                statement);
                }

            } else {
                throw new MatParseException("ShaderNode", split[0], statement);
            }
        }
    }

    /**
     * retrieve the leftType corresponding sampler type from the rightType
     *
     * @param leftType  the left samplerType
     * @param rightType the right sampler type (can be multiple types separated
     *                  by "|"
     * @return the type or null if not found
     */
    public String fixSamplerType(String leftType, String rightType) {
        String[] types = rightType.split("\\|");
        for (String string : types) {
            if (leftType.equals(string)) {
                return string;
            }
        }
        return null;
    }

    /**
     * stores a global output
     *
     * @param var        the variable to store
     * @param statement1 the statement being read
     * @throws IOException
     */
    public void storeGlobal(ShaderNodeVariable var, Statement statement1) throws IOException {
        var.setShaderOutput(true);
        if (shaderNode.getDefinition().getType() == ShaderProgram.ShaderType.Vertex) {
            ShaderNodeVariable global = techniqueDef.getShaderGenerationInfo().getVertexGlobal();
            if (global != null) {
                if (!global.getName().equals(var.getName())) {
                    throw new MatParseException(
                            "A global output is already defined for the vertex shader: " + global
                                    .getName() + ". vertex shader can only have one global output",
                            statement1);
                }
            } else {
                techniqueDef.getShaderGenerationInfo().setVertexGlobal(var);
            }
        } else if (shaderNode.getDefinition().getType() == ShaderProgram.ShaderType.Fragment) {
            storeVariable(var, techniqueDef.getShaderGenerationInfo().getFragmentGlobals());
        }
    }

    /**
     * store an attribute
     *
     * @param var the variable to store
     */
    public void storeAttribute(ShaderNodeVariable var) {
        storeVariable(var, techniqueDef.getShaderGenerationInfo().getAttributes());
    }

    /**
     * store a vertex uniform
     *
     * @param var the variable to store
     */
    public void storeVertexUniform(ShaderNodeVariable var) {
        storeVariable(var, techniqueDef.getShaderGenerationInfo().getVertexUniforms());

    }

    /**
     * store a fragment uniform
     *
     * @param var the variable to store
     */
    public void storeFragmentUniform(ShaderNodeVariable var) {
        storeVariable(var, techniqueDef.getShaderGenerationInfo().getFragmentUniforms());

    }

    /**
     * find the definition from this statement (loads it if necessary)
     *
     * @param statement the statement being read
     * @return the definition
     * @throws IOException
     */
    public ShaderNodeDefinition findDefinition(Statement statement) throws IOException {
        String defLine[] = statement.getLine().split(":");
        String defName = defLine[1].trim();

        ShaderNodeDefinition def = getNodeDefinitions().get(defName);
        if (def == null) {
            if (defLine.length == 3) {
                List<ShaderNodeDefinition> defs;
                try {
                    defs = (List<ShaderNodeDefinition>) ShaderNodeDefinitionLoader
                            .load(new ShaderNodeDefinitionKey(defLine[2].trim()));
                } catch (RuntimeException e) {
                    throw new MatParseException("Couldn't find " + defLine[2].trim(), statement, e);
                }

                for (ShaderNodeDefinition definition : defs) {
                    if (defName.equals(definition.getName())) {
                        def = definition;
                    }
                    if (!(getNodeDefinitions().containsKey(definition.getName()))) {
                        getNodeDefinitions().put(definition.getName(), definition);
                    }
                }
            }
            if (def == null) {
                throw new MatParseException(
                        defName + " is not a declared as Shader Node Definition", statement);
            }
        }
        return def;
    }

    /**
     * store a varying
     *
     * @param node     the shaderNode
     * @param variable the variable to store
     */
    public void storeVaryings(ShaderNode node, ShaderNodeVariable variable) {
        variable.setShaderOutput(true);
        if (node.getDefinition().getType() == ShaderProgram.ShaderType.Vertex
            && shaderNode.getDefinition().getType() == ShaderProgram.ShaderType.Fragment) {
            DeclaredVariable dv = varyings.get(variable.getName());
            if (dv == null) {
                techniqueDef.getShaderGenerationInfo().getVaryings().add(variable);
                dv = new DeclaredVariable(variable);

                varyings.put(variable.getName(), dv);
            }
            dv.addNode(shaderNode);
            //if a variable is declared with the same name as an input and an output and is a varying, set it as a shader output so it's declared as a varying only once.
            for (VariableMapping variableMapping : node.getInputMapping()) {
                if (variableMapping.getLeftVariable().getName().equals(variable.getName())) {
                    variableMapping.getLeftVariable().setShaderOutput(true);
                }
            }
        }

    }

    /**
     * merges 2 condition with the given operator
     *
     * @param condition1 the first condition
     * @param condition2 the second condition
     * @param operator   the operator ("&&" or "||&)
     * @return the merged condition
     */
    public String mergeConditions(String condition1, String condition2, String operator) {
        if (condition1 != null) {
            if (condition2 == null) {
                return condition1;
            } else {
                return "(" + condition1 + ") " + operator + " (" + condition2 + ")";
            }
        } else {
            return condition2;
        }
    }

    /**
     * search a variable in a list from its name and merge the conditions of the
     * variables
     *
     * @param variable the variable
     * @param varList  the variable list
     */
    public void storeVariable(ShaderNodeVariable variable, List<ShaderNodeVariable> varList) {
        for (ShaderNodeVariable var : varList) {
            if (var.getName().equals(variable.getName())) {
                return;
            }
        }
        varList.add(variable);
    }

    /**
     * check the types of a mapping, left type must match right type take the
     * swizzle into account
     *
     * @param mapping    the mapping
     * @param statement1 the statement being read
     * @throws MatParseException
     */
    protected void checkTypes(VariableMapping mapping, Statement statement1)
            throws MatParseException {
        if (!ShaderUtils.typesMatch(mapping)) {
            String ls = mapping.getLeftSwizzling().length() == 0 ? "" :
                        "." + mapping.getLeftSwizzling();
            String rs = mapping.getRightSwizzling().length() == 0 ? "" :
                        "." + mapping.getRightSwizzling();
            throw new MatParseException(
                    "Type mismatch, cannot convert " + mapping.getRightVariable().getType() + rs
                    + " to " + mapping.getLeftVariable().getType() + ls, statement1);
        }
        if (!ShaderUtils.multiplicityMatch(mapping)) {
            String type1 = mapping.getLeftVariable().getType() + "[" + mapping.getLeftVariable()
                                                                              .getMultiplicity()
                           + "]";
            String type2 = mapping.getRightVariable().getType() + "[" + mapping.getRightVariable()
                                                                               .getMultiplicity()
                           + "]";
            throw new MatParseException("Type mismatch, cannot convert " + type1 + " to " + type2,
                                        statement1);
        }
    }

    private Map<String, ShaderNodeDefinition> getNodeDefinitions() {
        if (nodeDefinitions == null) {
            nodeDefinitions = new HashMap<>();
        }
        return nodeDefinitions;
    }

    public void clear() {
        nodeDefinitions.clear();
        nodes.clear();
        shaderNodeDefinition = null;
        shaderNode = null;
        techniqueDef = null;
        attributes.clear();
        vertexDeclaredUniforms.clear();
        fragmentDeclaredUniforms.clear();
        varyings.clear();
        materialDef = null;
        shaderLanguage = "";
        shaderName = "";
        varNames = "";
        nulledConditions.clear();
    }
}
