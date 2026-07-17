package com.example.metadataui.card.model;

public final class ParameterBinding {
    private final String source;
    private final String path;
    private final Object value;
    private final boolean required;

    public ParameterBinding(String source, String path, Object value, boolean required) {
        this.source = source;
        this.path = path;
        this.value = value;
        this.required = required;
    }

    public static ParameterBinding pageContext(String path) {
        return new ParameterBinding("page-context", path, null, true);
    }

    public static ParameterBinding cardData(String path) {
        return new ParameterBinding("card-data", path, null, true);
    }

    public static ParameterBinding literal(Object value) {
        return new ParameterBinding("literal", null, value, false);
    }

    public String getSource() {
        return source;
    }

    public String getPath() {
        return path;
    }

    public Object getValue() {
        return value;
    }

    public boolean isRequired() {
        return required;
    }
}
