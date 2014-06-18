/*
 * 
 */

package org.aksw.geolift.json;

import java.util.List;

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
    
    public ParameterType(String type, String name) {
        this.type = type;
        this.name = name;
    }
    
    public ParameterType(String type, String name, String values) {
        this.type = type;
        this.name = name;
        this.values = values.split(",");
    }

    public String getType() {
        return type;
    }
    
    public String getName() {
        return name;
    }
    
    public String[] getValues() {
        return values;
    }
}
