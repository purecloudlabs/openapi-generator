package org.openapitools.codegen.languages;

public class PureCloudJavaScriptGuestClientCodegen extends PureCloudJavaScriptClientCodegen {

    public PureCloudJavaScriptGuestClientCodegen() {
        super();
        apiDocTemplateFiles.put("api_json.mustache", ".json");
    }

    @Override
    public String getName() { return "purecloudjavascript-guest"; }

}
