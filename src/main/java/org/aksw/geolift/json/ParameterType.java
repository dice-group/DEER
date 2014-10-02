/*
 * 
 */

package org.aksw.geolift.json;

/**
 *
 * @author eugen
 */
public class ParameterType {
    public static final String STRING = "string";
    public static final String BOOLEAN = "boolean";

    private String type = "";
    private String name = null;
    private String[] values = null;
    private String description = "";
    private boolean required = false;
    
    public ParameterType(String type, String name, String description) {
        this.type = type;
        this.name = name;
        this.description = description;
    }
    
    public ParameterType(String type, String name, String description, boolean required) {
        this.type = type;
        this.name = name;
        this.required = required;
        this.description = description;
    }
    
    public ParameterType(String type, String name, String values, String description) {
        this.type = type;
        this.name = name;
        this.values = values.split(",");
        this.description = description;
    }
    
     public ParameterType(String type, String name, String values, String description, boolean required) {
        this.type = type;
        this.name = name;
        this.values = values.split(",");
        this.description = description;
        this.required = required;
    }

    public String getType() {
        return this.type;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String[] getValues() {
        return this.values;
    }

    public String getDescription() {
        return this.description;
    }
    
    public boolean getRequired() {
        return this.required;
    }
}
