package com.example.orderfood.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ConversationHistory {
    private List<Content> contents;

    public ConversationHistory() {
        this.contents = new ArrayList<>();
    }

    public void addUserMessage(String text) {
        Content content = new Content("user");
        content.addTextPart(text);
        contents.add(content);
    }

    public void addModelMessage(String text) {
        Content content = new Content("model");
        content.addTextPart(text);
        contents.add(content);
    }

    public void addFunctionCall(String functionName, JSONObject args) {
        Content content = new Content("model");
        content.addFunctionCallPart(functionName, args);
        contents.add(content);
    }

    public void addFunctionResponse(String functionName, JSONObject response) {
        Content content = new Content("function");
        content.addFunctionResponsePart(functionName, response);
        contents.add(content);
    }

    public List<Content> getContents() {
        return contents;
    }

    public JSONArray toJSONArray() throws JSONException {
        JSONArray contentsArray = new JSONArray();
        for (Content content : contents) {
            contentsArray.put(content.toJSON());
        }
        return contentsArray;
    }

    public static class Content {
        private String role;
        private List<Part> parts;

        public Content(String role) {
            this.role = role;
            this.parts = new ArrayList<>();
        }

        public void addTextPart(String text) {
            parts.add(new TextPart(text));
        }

        public void addFunctionCallPart(String functionName, JSONObject args) {
            parts.add(new FunctionCallPart(functionName, args));
        }

        public void addFunctionResponsePart(String functionName, JSONObject response) {
            parts.add(new FunctionResponsePart(functionName, response));
        }

        public String getRole() {
            return role;
        }

        public List<Part> getParts() {
            return parts;
        }

        public JSONObject toJSON() throws JSONException {
            JSONObject contentJson = new JSONObject();
            contentJson.put("role", role);

            JSONArray partsArray = new JSONArray();
            for (Part part : parts) {
                partsArray.put(part.toJSON());
            }
            contentJson.put("parts", partsArray);

            return contentJson;
        }
    }

    public interface Part {
        JSONObject toJSON() throws JSONException;
    }

    public static class TextPart implements Part {
        private String text;

        public TextPart(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        @Override
        public JSONObject toJSON() throws JSONException {
            JSONObject partJson = new JSONObject();
            partJson.put("text", text);
            return partJson;
        }
    }

    public static class FunctionCallPart implements Part {
        private String functionName;
        private JSONObject args;

        public FunctionCallPart(String functionName, JSONObject args) {
            this.functionName = functionName;
            this.args = args;
        }

        public String getFunctionName() {
            return functionName;
        }

        public JSONObject getArgs() {
            return args;
        }

        @Override
        public JSONObject toJSON() throws JSONException {
            JSONObject functionCall = new JSONObject();
            functionCall.put("name", functionName);
            functionCall.put("args", args);

            JSONObject partJson = new JSONObject();
            partJson.put("functionCall", functionCall);
            return partJson;
        }
    }

    public static class FunctionResponsePart implements Part {
        private String functionName;
        private JSONObject response;

        public FunctionResponsePart(String functionName, JSONObject response) {
            this.functionName = functionName;
            this.response = response;
        }

        public String getFunctionName() {
            return functionName;
        }

        public JSONObject getResponse() {
            return response;
        }

        @Override
        public JSONObject toJSON() throws JSONException {
            JSONObject functionResponse = new JSONObject();
            functionResponse.put("name", functionName);
            functionResponse.put("response", response);

            JSONObject partJson = new JSONObject();
            partJson.put("functionResponse", functionResponse);
            return partJson;
        }
    }
}
