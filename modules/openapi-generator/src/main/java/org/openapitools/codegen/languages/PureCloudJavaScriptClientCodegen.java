package org.openapitools.codegen.languages;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.servers.Server;
import org.apache.commons.lang3.StringUtils;
import org.openapitools.codegen.*;
import org.openapitools.codegen.utils.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static org.openapitools.codegen.utils.StringUtils.*;

public class PureCloudJavaScriptClientCodegen extends DefaultCodegen implements CodegenConfig {
    private static String OPERATION_ID_PROPERTY_NAME = "x-purecloud-method-name";
    private static final Logger LOGGER = LoggerFactory.getLogger(PureCloudJavaScriptClientCodegen.class);
    public static final String PROJECT_NAME = "projectName";
    public static final String MODULE_NAME = "moduleName";
    public static final String PROJECT_DESCRIPTION = "projectDescription";
    public static final String PROJECT_VERSION = "projectVersion";
    public static final String PROJECT_LICENSE_NAME = "projectLicenseName";
    public static final String USE_PROMISES = "usePromises";
    public static final String USE_INHERITANCE = "useInheritance";
    public static final String EMIT_MODEL_METHODS = "emitModelMethods";
    public static final String EMIT_JS_DOC = "emitJSDoc";
    protected String projectName;
    protected String moduleName;
    protected String projectDescription;
    protected String projectVersion;
    protected String projectLicenseName;
    protected String invokerPackage;
    protected String sourceFolder = "src";
    protected String localVariablePrefix = "";
    protected boolean usePromises;
    protected boolean emitModelMethods;
    protected boolean emitJSDoc = true;
    protected String apiDocPath = "docs/";
    protected String modelDocPath = "docs/";

    public PureCloudJavaScriptClientCodegen() {
        super();
        outputFolder = "generated-code/js";
        apiTemplateFiles.put("api.mustache", ".js");
        embeddedTemplateDir = templateDir = "Javascript";
        apiPackage = "api";
        modelPackage = "model";
        apiDocTemplateFiles.put("api_doc.mustache", ".md");
        supportingFiles.add(new SupportingFile("rollup-cjs-for-node.config.mustache", "", "rollup-cjs-for-node.config.js"));
        supportingFiles.add(new SupportingFile("rollup-cjs-for-browserify.config.mustache", "", "rollup-cjs-for-browserify.config.js"));
        supportingFiles.add(new SupportingFile("rollup-amd.config.mustache", "", "rollup-amd.config.js"));
        // Typescript
        supportingFiles.add(new SupportingFile("index.d.ts.mustache", "", "index.d.ts"));
        // reference: http://www.w3schools.com/js/js_reserved.asp
        setReservedWordsLowerCase(
                Arrays.asList(
                        "abstract", "arguments", "boolean", "break", "byte",
                        "case", "catch", "char", "class", "const",
                        "continue", "debugger", "default", "delete", "do",
                        "double", "else", "enum", "eval", "export",
                        "extends", "false", "final", "finally", "float",
                        "for", "function", "goto", "if", "implements",
                        "import", "in", "instanceof", "int", "interface",
                        "let", "long", "native", "new", "null",
                        "package", "private", "protected", "public", "return",
                        "short", "static", "super", "switch", "synchronized",
                        "this", "throw", "throws", "transient", "true",
                        "try", "typeof", "var", "void", "volatile",
                        "while", "with", "yield",
                        "Array", "Date", "eval", "function", "hasOwnProperty",
                        "Infinity", "isFinite", "isNaN", "isPrototypeOf",
                        "Math", "NaN", "Number", "Object",
                        "prototype", "String", "toString", "undefined", "valueOf"));

        languageSpecificPrimitives = new HashSet<String>(
                Arrays.asList("String", "Boolean", "Integer", "Number", "Array", "Object", "Date", "File"));

        defaultIncludes = new HashSet<String>(languageSpecificPrimitives);
        instantiationTypes.put("array", "Array");
        instantiationTypes.put("list", "Array");
        instantiationTypes.put("map", "Object");
        typeMapping.clear();
        typeMapping.put("array", "Array");
        typeMapping.put("map", "Object");
        typeMapping.put("List", "Array");
        typeMapping.put("boolean", "Boolean");
        typeMapping.put("string", "String");
        typeMapping.put("int", "Number");
        typeMapping.put("float", "Number");
        typeMapping.put("number", "Number");
        typeMapping.put("DateTime", "Date");
        typeMapping.put("LocalDateTime", "Date");
        typeMapping.put("date", "String");
        typeMapping.put("long", "Number");
        typeMapping.put("short", "Number");
        typeMapping.put("char", "String");
        typeMapping.put("double", "Number");
        typeMapping.put("object", "Object");
        typeMapping.put("integer", "Number");
        typeMapping.put("URI", "String");
        // binary not supported in JavaScript client right now, using String as a
        // workaround
        typeMapping.put("ByteArray", "Blob"); // I don't see ByteArray defined in the Swagger docs.
        typeMapping.put("binary", "Blob");
        typeMapping.put("UUID", "String");

        importMapping.clear();

        cliOptions.add(
                new CliOption(CodegenConstants.SOURCE_FOLDER, CodegenConstants.SOURCE_FOLDER_DESC).defaultValue("src"));
        cliOptions.add(
                new CliOption(CodegenConstants.LOCAL_VARIABLE_PREFIX, CodegenConstants.LOCAL_VARIABLE_PREFIX_DESC));
        cliOptions.add(new CliOption(CodegenConstants.INVOKER_PACKAGE, CodegenConstants.INVOKER_PACKAGE_DESC));
        cliOptions.add(new CliOption(CodegenConstants.API_PACKAGE, CodegenConstants.API_PACKAGE_DESC));
        cliOptions.add(new CliOption(CodegenConstants.MODEL_PACKAGE, CodegenConstants.MODEL_PACKAGE_DESC));
        cliOptions.add(new CliOption(PROJECT_NAME,
                "name of the project (Default: generated from info.title or \"swagger-js-client\")"));
        cliOptions.add(new CliOption(MODULE_NAME,
                "module name for AMD, Node or globals (Default: generated from <projectName>)"));
        cliOptions.add(new CliOption(PROJECT_DESCRIPTION,
                "description of the project (Default: using info.description or \"Client library of <projectName>\")"));
        cliOptions.add(new CliOption(PROJECT_VERSION,
                "version of the project (Default: using info.version or \"1.0.0\")"));
        cliOptions.add(new CliOption(PROJECT_LICENSE_NAME,
                "name of the license the project uses (Default: using info.license.name)"));
        cliOptions.add(new CliOption(USE_PROMISES,
                "use Promises as return values from the client API, instead of superagent callbacks")
                .defaultValue(Boolean.FALSE.toString()));
        cliOptions.add(new CliOption(EMIT_MODEL_METHODS,
                "generate getters and setters for model properties")
                .defaultValue(Boolean.FALSE.toString()));
        cliOptions.add(new CliOption(EMIT_JS_DOC,
                "generate JSDoc comments")
                .defaultValue(Boolean.TRUE.toString()));
        cliOptions.add(new CliOption(USE_INHERITANCE,
                "use JavaScript prototype chains & delegation for inheritance")
                .defaultValue(Boolean.TRUE.toString()));

        apiDocTemplateFiles.put("api_json.mustache", ".json");
        operationTemplateFiles.put("operation_example.mustache", "-example.txt");
    }

    @Override
    /**
     * Get the operation ID or use default behavior if blank.
     *
     * @param operation  the operation object
     * @param path       the path of the operation
     * @param httpMethod the HTTP method of the operation
     * @return the (generated) operationId
     */
    protected String getOrGenerateOperationId(Operation operation, String path,
                                              String httpMethod) {
        if (operation.getExtensions().containsKey(OPERATION_ID_PROPERTY_NAME)) {
            String operationId = operation.getExtensions().get(OPERATION_ID_PROPERTY_NAME).toString();
            if (!StringUtils.isBlank(operationId)) {
                return operationId;
            }
        }
        return super.getOrGenerateOperationId(operation, path, httpMethod);
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.CLIENT;
    }

    @Override
    public String getName() {
        return "purecloudjavascript";
    }

    @Override
    public String getHelp() {
        return "Generates a Javascript client library.";
    }

    @Override
    public void processOpts() {
        super.processOpts();
        if (additionalProperties.containsKey(PROJECT_NAME)) {
            setProjectName(((String) additionalProperties.get(PROJECT_NAME)));
        }
        if (additionalProperties.containsKey(MODULE_NAME)) {
            setModuleName(((String) additionalProperties.get(MODULE_NAME)));
        }
        if (additionalProperties.containsKey(PROJECT_DESCRIPTION)) {
            setProjectDescription(((String) additionalProperties.get(PROJECT_DESCRIPTION)));
        }
        if (additionalProperties.containsKey(PROJECT_VERSION)) {
            setProjectVersion(((String) additionalProperties.get(PROJECT_VERSION)));
        }
        if (additionalProperties.containsKey(PROJECT_LICENSE_NAME)) {
            setProjectLicenseName(((String) additionalProperties.get(PROJECT_LICENSE_NAME)));
        }
        if (additionalProperties.containsKey(CodegenConstants.LOCAL_VARIABLE_PREFIX)) {
            setLocalVariablePrefix((String) additionalProperties.get(CodegenConstants.LOCAL_VARIABLE_PREFIX));
        }
        if (additionalProperties.containsKey(CodegenConstants.SOURCE_FOLDER)) {
            setSourceFolder((String) additionalProperties.get(CodegenConstants.SOURCE_FOLDER));
        }
        if (additionalProperties.containsKey(CodegenConstants.INVOKER_PACKAGE)) {
            setInvokerPackage((String) additionalProperties.get(CodegenConstants.INVOKER_PACKAGE));
        }
        if (additionalProperties.containsKey(USE_PROMISES)) {
            setUsePromises(convertPropertyToBooleanAndWriteBack(USE_PROMISES));
        }
        if (additionalProperties.containsKey(USE_INHERITANCE)) {
            setUseInheritance(convertPropertyToBooleanAndWriteBack(USE_INHERITANCE));
        } else {
            supportsInheritance = true;
        }
        if (additionalProperties.containsKey(EMIT_MODEL_METHODS)) {
            setEmitModelMethods(convertPropertyToBooleanAndWriteBack(EMIT_MODEL_METHODS));
        }
        if (additionalProperties.containsKey(EMIT_JS_DOC)) {
            setEmitJSDoc(convertPropertyToBooleanAndWriteBack(EMIT_JS_DOC));
        }
    }

    @Override
    public void preprocessOpenAPI(OpenAPI openAPI) {
        super.preprocessOpenAPI(openAPI);
        if (openAPI.getInfo() != null) {
            Info info = openAPI.getInfo();
            if (StringUtils.isBlank(projectName) && info.getTitle() != null) {
                // when projectName is not specified, generate it from info.title
                projectName = dashize(info.getTitle());
            }
            if (StringUtils.isBlank(projectVersion)) {
                // when projectVersion is not specified, use info.version
                projectVersion = info.getVersion();
            }
            if (projectDescription == null) {
                // when projectDescription is not specified, use info.description
                projectDescription = info.getDescription();
            }
            if (additionalProperties.get(PROJECT_LICENSE_NAME) == null) {
                // when projectLicense is not specified, use info.license
                if (info.getLicense() != null) {
                    License license = info.getLicense();
                    additionalProperties.put(PROJECT_LICENSE_NAME, license.getName());
                }
            }
        }
        // default values
        if (StringUtils.isBlank(projectName)) {
            projectName = "openapi-js-client";
        }
        if (StringUtils.isBlank(moduleName)) {
            moduleName = camelize(underscore(projectName));
        }
        if (StringUtils.isBlank(projectVersion)) {
            projectVersion = "1.0.0";
        }
        if (projectDescription == null) {
            projectDescription = "Client library of " + projectName;
        }
        additionalProperties.put(PROJECT_NAME, projectName);
        additionalProperties.put(MODULE_NAME, moduleName);
        additionalProperties.put(PROJECT_DESCRIPTION, escapeText(projectDescription));
        additionalProperties.put(PROJECT_VERSION, projectVersion);
        additionalProperties.put(CodegenConstants.API_PACKAGE, apiPackage);
        additionalProperties.put(CodegenConstants.INVOKER_PACKAGE, invokerPackage);
        additionalProperties.put(CodegenConstants.LOCAL_VARIABLE_PREFIX, localVariablePrefix);
        additionalProperties.put(CodegenConstants.MODEL_PACKAGE, modelPackage);
        additionalProperties.put(CodegenConstants.SOURCE_FOLDER, sourceFolder);
        additionalProperties.put(USE_PROMISES, usePromises);
        additionalProperties.put(USE_INHERITANCE, supportsInheritance);
        additionalProperties.put(EMIT_MODEL_METHODS, emitModelMethods);
        additionalProperties.put(EMIT_JS_DOC, emitJSDoc);
        // make api and model doc path available in mustache template
        additionalProperties.put("apiDocPath", apiDocPath);
        supportingFiles.add(new SupportingFile("package.mustache", "", "package.json"));
        supportingFiles.add(new SupportingFile("index.mustache", createPath(sourceFolder, invokerPackage), "index.js"));
        supportingFiles.add(new SupportingFile("ApiClient.mustache", createPath(sourceFolder, invokerPackage), "ApiClient.js"));
        supportingFiles.add(new SupportingFile("configuration.mustache", createPath(sourceFolder, invokerPackage), "configuration.js"));
        supportingFiles.add(new SupportingFile("logger.mustache", createPath(sourceFolder, invokerPackage), "logger.js"));
        supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));
    }

    @Override
    public String escapeReservedWord(String name) {
        return "_" + name;
    }

    /**
     * Concatenates an array of path segments into a path string.
     *
     * @param segments The path segments to concatenate. A segment may contain
     *                 either of the file separator characters '\' or '/'.
     *                 A segment is ignored if it is <code>null</code>, empty or
     *                 &quot;.&quot;.
     * @return A path string using the correct platform-specific file separator
     *         character.
     */
    private String createPath(String... segments) {
        StringBuilder buf = new StringBuilder();
        for (String segment : segments) {
            if (!StringUtils.isEmpty(segment) && !segment.equals(".")) {
                if (buf.length() != 0)
                    buf.append(File.separatorChar);
                buf.append(segment);
            }
        }
        for (int i = 0; i < buf.length(); i++) {
            char c = buf.charAt(i);
            if ((c == '/' || c == '\\') && c != File.separatorChar)
                buf.setCharAt(i, File.separatorChar);
        }
        return buf.toString();
    }

    @Override
    public String apiFileFolder() {
        return createPath(outputFolder, sourceFolder, invokerPackage, apiPackage());
    }

    @Override
    public String modelFileFolder() {
        return createPath(outputFolder, sourceFolder, invokerPackage, modelPackage());
    }

    public void setInvokerPackage(String invokerPackage) {
        this.invokerPackage = invokerPackage;
    }

    public void setSourceFolder(String sourceFolder) {
        this.sourceFolder = sourceFolder;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setLocalVariablePrefix(String localVariablePrefix) {
        this.localVariablePrefix = localVariablePrefix;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public void setProjectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
    }

    public void setProjectLicenseName(String projectLicenseName) {
        this.projectLicenseName = projectLicenseName;
    }

    public void setUsePromises(boolean usePromises) {
        this.usePromises = usePromises;
    }

    public void setUseInheritance(boolean useInheritance) {
        this.supportsInheritance = useInheritance;
    }

    public void setEmitModelMethods(boolean emitModelMethods) {
        this.emitModelMethods = emitModelMethods;
    }

    public void setEmitJSDoc(boolean emitJSDoc) {
        this.emitJSDoc = emitJSDoc;
    }

    @Override
    public String apiDocFileFolder() {
        return createPath(outputFolder, apiDocPath);
    }

    @Override
    public String modelDocFileFolder() {
        return createPath(outputFolder, modelDocPath);
    }

    @Override
    public String toApiDocFilename(String name) {
        return toApiName(name);
    }

    @Override
    public String toModelDocFilename(String name) {
        return toModelName(name);
    }

    @Override
    public String toVarName(String name) {
        // sanitize name
        name = sanitizeName(name); // FIXME parameter should not be assigned. Also declare it as "final"
        if ("_".equals(name)) {
            name = "_u";
        }
        // if it's all uppper case, do nothing
        if (name.matches("^[A-Z_]*$")) {
            return name;
        }
        // camelize (lower first character) the variable name
        // pet_id => petId
        name = camelize(name, true);
        // for reserved word or word starting with number, append _
        if (isReservedWord(name) || name.matches("^\\d.*")) {
            name = escapeReservedWord(name);
        }
        return name;
    }

    @Override
    public String toParamName(String name) {
        // should be the same as variable name
        return toVarName(name);
    }

    @Override
    public String toModelFilename(String name) {
        // should be the same as the model name
        return toModelName(name);
    }

    @Override
    public String toModelImport(String name) {
        return name;
    }

    @Override
    public String toApiImport(String name) {
        return toApiName(name);
    }

    @Override
    public String getTypeDeclaration(Schema p) {
        if (ModelUtils.isArraySchema(p)) {
            ArraySchema ap = (ArraySchema) p;
            Schema inner = ap.getItems();
            return "[" + getTypeDeclaration(inner) + "]";
        } else if (ModelUtils.isMapSchema(p)) {
            MapSchema mp = (MapSchema) p;
            Schema inner = (Schema) mp.getAdditionalProperties();
            return "{String: " + getTypeDeclaration(inner) + "}";
        }
        return super.getTypeDeclaration(p);
    }

    @Override
    public String toDefaultValue(Schema p) {
        if (ModelUtils.isBooleanSchema(p)) {
            if (p.getDefault() != null) {
                return p.getDefault().toString();
            }
        } else if (ModelUtils.isDateSchema(p)) {
            // TODO
        } else if (ModelUtils.isDateTimeSchema(p)) {
            // TODO
        } else if (ModelUtils.isNumberSchema(p)) {
            if (p.getDefault() != null) {
                return p.getDefault().toString();
            }
        } else if (ModelUtils.isIntegerSchema(p)) {
            if (p.getDefault() != null) {
                return p.getDefault().toString();
            }
        } else if (ModelUtils.isStringSchema(p)) {
            if (p.getDefault() != null) {
                return "'" + p.getDefault() + "'";
            }
        }

        return null;
    }

    @Override
    public String toDefaultValueWithParam(String name, Schema schema) {
        String type = normalizeType(getTypeDeclaration(schema));
        if (!StringUtils.isEmpty(schema.get$ref())) {
            return " = " + type + ".constructFromObject(data['" + name + "']);";
        } else {
            return " = ApiClient.convertToType(data['" + name + "'], " + type + ");";
        }
    }

    @Override
    public void setParameterExampleValue(CodegenParameter p) {
        String example;
        if (p.defaultValue == null) {
            example = p.example;
        } else {
            example = p.defaultValue;
        }
        String type = p.baseType;
        if (type == null) {
            type = p.dataType;
        }
        if ("String".equals(type)) {
            if (example == null) {
                example = p.paramName + "_example";
            }
            example = "\"" + escapeText(example) + "\"";
        } else if ("Integer".equals(type)) {
            if (example == null) {
                example = "56";
            }
        } else if ("Number".equals(type)) {
            if (example == null) {
                example = "3.4";
            }
        } else if ("Boolean".equals(type)) {
            if (example == null) {
                example = "true";
            }
        } else if ("File".equals(type)) {
            if (example == null) {
                example = "/path/to/file";
            }
            example = "\"" + escapeText(example) + "\"";
        } else if ("Date".equals(type)) {
            if (example == null) {
                example = "2013-10-20T19:20:30+01:00";
            }
            example = "new Date(\"" + escapeText(example) + "\")";
        } else if (!languageSpecificPrimitives.contains(type)) {
            // type is a model class, e.g. User
            // example = "new " + moduleName + "." + type + "()";

            // Ensure a base type is set
            if (p.baseType == null || p.baseType.equals("")) {
                p.baseType = p.dataType;
            }
            /*
             * API-2772
             * Since models have been removed from the SDK, the examples shouldn't reference
             * them. This will
             * display something like
             * var body = {}; // Object | User
             * instead of
             * var body = new platformClient.UpdateUser(); // UpdateUser | User
             */
            example = "{}";
            p.dataType = "Object";
        }
        if (example == null) {
            example = "null";
        } else if (Boolean.TRUE.equals(p.isArray)) {
            example = "[" + example + "]";
        } else if (Boolean.TRUE.equals(p.isMap)) {
            example = "{key: " + example + "}";
        }
        p.example = example;
    }

    /**
     * Normalize type by wrapping primitive types with single quotes.
     *
     * @param type Primitive type
     * @return Normalized type
     */
    public String normalizeType(String type) {
        return type.replaceAll("\\b(Boolean|Integer|Number|String|Date)\\b", "'$1'");
    }

    @Override
    public String getSchemaType(Schema p) {
        String schemaType = super.getSchemaType(p);
        String type;
        if (typeMapping.containsKey(schemaType)) {
            type = typeMapping.get(schemaType);
            if (!needToImport(type)) {
                return type;
            }
        } else {
            type = schemaType;
        }
        if (null == type) {
            LOGGER.error("No Type defined for Property " + p);
        }
        return toModelName(type);
    }

    @Override
    public String toOperationId(String operationId) {
        // throw exception if method name is empty
        if (StringUtils.isEmpty(operationId)) {
            throw new RuntimeException("Empty method/operation name (operationId) not allowed");
        }
        operationId = camelize(sanitizeName(operationId), true);
        // method name cannot use reserved keyword, e.g. return
        if (isReservedWord(operationId)) {
            String newOperationId = camelize("call_" + operationId, true);
            LOGGER.warn(operationId + " (reserved word) cannot be used as method name. Renamed to " + newOperationId);
            return newOperationId;
        }
        return operationId;
    }

    @Override
    public CodegenOperation fromOperation(String path, String httpMethod, Operation operation, List<Server> servers) {
        CodegenOperation op = super.fromOperation(path, httpMethod, operation, servers);
        if (op.returnType != null) {
            op.returnType = normalizeType(op.returnType);
        }
        // Set vendor-extension to be used in template:
        // x-codegen-hasMoreRequired
        // x-codegen-hasMoreOptional
        // x-codegen-hasRequiredParams
        CodegenParameter lastRequired = null;
        CodegenParameter lastOptional = null;
        for (CodegenParameter p : op.allParams) {
            if (Boolean.TRUE.equals(p.required) && p.required) {
                lastRequired = p;
            } else {
                lastOptional = p;
            }
        }
        for (CodegenParameter p : op.allParams) {
            if (p == lastRequired) {
                p.vendorExtensions.put("x-codegen-hasMoreRequired", false);
            } else if (p == lastOptional) {
                p.vendorExtensions.put("x-codegen-hasMoreOptional", false);
            } else {
                p.vendorExtensions.put("x-codegen-hasMoreRequired", true);
                p.vendorExtensions.put("x-codegen-hasMoreOptional", true);
            }
        }
        op.vendorExtensions.put("x-codegen-hasRequiredParams", lastRequired != null);
        return op;
    }

    private String trimBrackets(String s) {
        if (s != null) {
            int beginIdx = s.charAt(0) == '[' ? 1 : 0;
            int endIdx = s.length();
            if (s.charAt(endIdx - 1) == ']')
                endIdx--;
            return s.substring(beginIdx, endIdx);
        }
        return null;
    }

    private String getModelledType(String dataType) {
        // return "module:" + (StringUtils.isEmpty(invokerPackage) ? "" :
        // (invokerPackage + "/"))
        // + (StringUtils.isEmpty(modelPackage) ? "" : (modelPackage + "/")) + dataType;

        /*
         * API-2772
         * Since models have been removed from the SDK, the jsdocs shouldn't reference
         * them. This will
         * display something like
         *
         * @param {Object} body User
         * instead of
         *
         * @param {module:purecloud-platform-client-v2/model/Object} body User
         */
        return "Object";
    }

    private String getJSDocTypeWithBraces(CodegenModel cm, CodegenProperty cp) {
        return "{" + getJSDocType(cm, cp) + "}";
    }

    private String getJSDocTypeWithBraces(CodegenParameter cp) {
        return "{" + getJSDocType(cp) + "}";
    }

    private String getJSDocType(CodegenModel cm, CodegenProperty cp) {
        if (Boolean.TRUE.equals(cp.isContainer)) {
            if (cp.containerType.equals("array")) {
                return "Array.<" + getJSDocType(cm, cp.items) + ">";
            }
            else if (cp.containerType.equals("map")) {
                return "Object.<String, " + getJSDocType(cm, cp.items) + ">";
            }
        }
        String dataType = trimBrackets(cp.datatypeWithEnum);
        if (cp.isEnum) {
            dataType = cm.classname + '.' + dataType;
        }
        if (isModelledType(cp)) {
            dataType = getModelledType(dataType);
        }
        return dataType;
    }

    private String getJSDocType(CodegenOperation co) {
        String returnType = trimBrackets(co.returnType);
        if (returnType != null) {
            if (isModelledType(co))
                returnType = getModelledType(returnType);
            if (Boolean.TRUE.equals(co.isArray)) {
                return "Array.<" + returnType + ">";
            } else if (Boolean.TRUE.equals(co.isMap)) {
                return "Object.<String, " + returnType + ">";
            }
        }
        return returnType;
    }

    private String getJSDocType(CodegenParameter cp) {
        String dataType = trimBrackets(cp.dataType);
        if (isModelledType(cp))
            dataType = getModelledType(dataType);
        if (Boolean.TRUE.equals(cp.isArray)) {
            return "Array.<" + dataType + ">";
        } else if (Boolean.TRUE.equals(cp.isMap)) {
            return "Object.<String, " + dataType + ">";
        }
        return dataType;
    }

    private String getJSDocTypeWithBraces(CodegenOperation co) {
        String jsDocType = getJSDocType(co);
        return jsDocType == null ? null : "{" + jsDocType + "}";
    }

    private boolean isModelledType(CodegenProperty cp) {
        // N.B. enums count as modelled types, file is not modelled (SuperAgent uses
        // some 3rd party library).
        return cp.isEnum || !languageSpecificPrimitives.contains(cp.baseType == null ? cp.dataType : cp.baseType);
    }

    private boolean isModelledType(CodegenParameter cp) {
        // N.B. enums count as modelled types, file is not modelled (SuperAgent uses
        // some 3rd party library).
        return cp.isEnum || !languageSpecificPrimitives.contains(cp.baseType == null ? cp.dataType : cp.baseType);
    }

    private boolean isModelledType(CodegenOperation co) {
        // This seems to be the only way to tell whether an operation return type is
        // modelled.
        return !Boolean.TRUE.equals(co.returnTypeIsPrimitive);
    }

    @Override
    public Map<String, Object> postProcessSupportingFileData(Map<String, Object> objs) {
        super.postProcessSupportingFileData(objs);
        Map<String, Object> apiInfo = (Map<String, Object>) objs.get("apiInfo");
        List<HashMap<String, Object>> apiList = (List<HashMap<String, Object>>) apiInfo.get("apis");
        // removing duplicate apis
        apiList.subList(apiList.size() / 2, apiList.size()).clear();

        return objs;
    }

    @Override
    public Map<String, Object> postProcessOperationsWithModels(Map<String, Object> objs, List<Object> allModels) {
        // Generate and store argument list string of each operation into
        // vendor-extension: x-codegen-argList.
        Map<String, Object> operations = (Map<String, Object>) objs.get("operations");
        if (operations != null) {
            List<CodegenOperation> ops = (List<CodegenOperation>) operations.get("operation");
            for (CodegenOperation operation : ops) {
                if (operation.summary != null) {
                    operation.summary = processDescription(operation.summary);
                }
                if (operation.notes != null) {
                    operation.notes = processNotes(operation.notes);
                }
                List<String> argList = new ArrayList<>();
                boolean hasOptionalParams = false;
                for (CodegenParameter p : operation.allParams) {
                    if (p.example != null) {
                        p.example = processNotes(p.example);
                    }
                    if (p.description != null) {
                        p.description = processDescription(p.description);
                    }
                    if (p.defaultValue != null) {
                        p.defaultValue = processNotes(p.defaultValue);
                    }
                    if (Boolean.TRUE.equals(p.required) && p.required) {
                        argList.add(p.paramName);
                    } else {
                        hasOptionalParams = true;
                    }
                }
                if (hasOptionalParams) {
                    argList.add("opts");
                }
                if (!usePromises) {
                    argList.add("callback");
                }
                String joinedArgList = StringUtils.join(argList, ", ");
                operation.vendorExtensions.put("x-codegen-arg-list", joinedArgList);
                // Store JSDoc type specification into vendor-extension: x-jsdoc-type.
                for (CodegenParameter cp : operation.allParams) {
                    String jsdocType = getJSDocTypeWithBraces(cp);
                    cp.vendorExtensions.put("x-jsdoc-type", jsdocType);
                }
                String jsdocType = getJSDocTypeWithBraces(operation);
                operation.vendorExtensions.put("x-jsdoc-type", jsdocType);
            }
        }
        return objs;
    }

    @Override
    protected boolean needToImport(String type) {
        return !defaultIncludes.contains(type)
                && !languageSpecificPrimitives.contains(type);
    }

    @Override
    public String toEnumName(CodegenProperty property) {
        return sanitizeName(camelize(property.name)) + "Enum";
    }

    @Override
    public String toEnumVarName(String value, String datatype) {
        if (value.length() == 0) {
            return "empty";
        }
        // for symbol, e.g. $, #
        if (getSymbolName(value) != null) {
            return (getSymbolName(value)).toUpperCase(Locale.getDefault());
        }
        return value;
    }

    @Override
    public String toEnumValue(String value, String datatype) {
        if ("Integer".equals(datatype) || "Number".equals(datatype)) {
            return value;
        } else {
            return "\"" + escapeText(value) + "\"";
        }
    }

    private String processDescription(String description) {
        return description
                .replace("\\\"", "")
                .replace("&", "and");

    }

    private String processNotes(String note) {
        return note
                .replace("\\", "")
                .replace("'", "");
    }

    public static String getTypeScriptResponseType(String dataType) {
        String typeScriptType = dataType;
        if (typeScriptType.startsWith("[")) {
            // Recurse and wrap with array syntax
            typeScriptType = "Array<"
                    + getTypeScriptResponseType(typeScriptType.substring(1, typeScriptType.length() - 1)) + ">";
        } else if (typeScriptType.startsWith("{String: ")) {
            // Recurse and wrap with dictionary (key/value map) syntax
            typeScriptType = "{ [key: string]: "
                    + getTypeScriptResponseType(typeScriptType.substring(9, typeScriptType.length() - 1)) + "; }";
        } else {
            // Map JavaScript types to TypeScript types
            switch (typeScriptType) {
                case "Number":
                    typeScriptType = "number";
                    break;
                case "Boolean":
                    typeScriptType = "boolean";
                    break;
                case "String":
                case "Date":
                    typeScriptType = "string";
                    break;
                case "Object":
                    typeScriptType = "object";
                    break;
                case "Void":
                    typeScriptType = "void";
                    break;
                case "Null":
                    typeScriptType = "null";
                    break;
                case "Undefined":
                    typeScriptType = "undefined";
                    break;
                case "File":
                    typeScriptType = "any";
                    break;
                default:
                    // This is expected to be the name of a model from swagger
                    typeScriptType = "Models." + typeScriptType;
            }
        }
        return typeScriptType;
    }

}