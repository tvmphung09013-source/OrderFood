package com.example.orderfood.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FunctionDeclaration {
    private String name;
    private String description;
    private List<Parameter> parameters;

    public FunctionDeclaration(String name, String description) {
        this.name = name;
        this.description = description;
        this.parameters = new ArrayList<>();
    }

    public void addParameter(Parameter parameter) {
        this.parameters.add(parameter);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject functionJson = new JSONObject();
        functionJson.put("name", name);
        functionJson.put("description", description);

        JSONObject parametersObj = new JSONObject();
        parametersObj.put("type", "object");
        
        JSONObject properties = new JSONObject();
        JSONArray required = new JSONArray();

        for (Parameter param : parameters) {
            properties.put(param.getName(), param.toJSON());
            if (param.isRequired()) {
                required.put(param.getName());
            }
        }

        parametersObj.put("properties", properties);
        parametersObj.put("required", required);

        functionJson.put("parameters", parametersObj);

        return functionJson;
    }

    public static class Parameter {
        private String name;
        private String type;
        private String description;
        private boolean required;

        public Parameter(String name, String type, String description, boolean required) {
            this.name = name;
            this.type = type;
            this.description = description;
            this.required = required;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }

        public boolean isRequired() {
            return required;
        }

        public JSONObject toJSON() throws JSONException {
            JSONObject paramJson = new JSONObject();
            paramJson.put("type", type);
            paramJson.put("description", description);
            return paramJson;
        }
    }
}
