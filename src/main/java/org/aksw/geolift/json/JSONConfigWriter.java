/*
 *
 */
package org.aksw.geolift.json;

import java.util.List;
import org.aksw.geolift.modules.*;
import org.aksw.geolift.operators.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
/**
 *
 * @author eugen
 */
public class JSONConfigWriter {
    private static final Logger logger = Logger.getLogger(GeoLiftModule.class.getName());
    
    private static String join(String[] values) {
        StringBuilder joined = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            joined.append("\"").append(values[i]).append("\"");
            if(i != values.length - 1) {
                joined.append(",");
            }
        }
        
        return joined.toString();
    }
    
    private static String buildModuleJSONString(String name, GeoLiftModule module) {
        StringBuilder moduleJSONConfig = new StringBuilder();
        moduleJSONConfig.append("{");
        moduleJSONConfig.append("\"$schema\":\"http://json-schema.org/draft-04/schema#\",");
        moduleJSONConfig.append("\"title\":\"").append(name).append("\",");
        moduleJSONConfig.append("\"description\":\"\",");
        
        List<ParameterType> parameters = module.getParameterWithTypes();
        if(parameters.size() > 0) {
            moduleJSONConfig.append("\"type\":\"object\",");
            moduleJSONConfig.append("\"properties\":{");
            
            int i = 0;
            for(ParameterType parameter:parameters) {
                String[] values = parameter.getValues();
                
                moduleJSONConfig.append("\"").append(parameter.getName()).append("\":{");
                moduleJSONConfig.append("\"description\":\"\",");
                
                if(null == values) {
                    moduleJSONConfig.append("\"type\":\"").append(parameter.getType()).append("\"");
                } else {
                    moduleJSONConfig.append("\"enum\":[").append(join(values)).append("]");
                }
                
                moduleJSONConfig.append("}");
                
                if(i < parameters.size() - 1) {
                    moduleJSONConfig.append(",");
                }
                i++;
            }
            
            moduleJSONConfig.append("}");
        }
        
        moduleJSONConfig.append("}");
        
        return moduleJSONConfig.toString();
    }
    
    private static String buildJSONString() {
        //get modules
        ModuleFactory moduleFactory = new ModuleFactory();        
        List<String> moduleNames = moduleFactory.getNames();
        
        //get operators
        ModelOperatorFactory modelOperatorFactory = new ModelOperatorFactory();
        List<String> operatorNames = modelOperatorFactory.getNames();
        
        StringBuilder jsonConfig = new StringBuilder();
        jsonConfig.append("[");
        
        int i = 0;
        for (String moduleName: moduleNames) {
            GeoLiftModule module = ModuleFactory.getModule(moduleName);
            jsonConfig.append(buildModuleJSONString(moduleName, module));
            
            if (i < moduleNames.size() - 1) {
                jsonConfig.append(",");
            }
            i++;
        }
        
        jsonConfig.append("]");
        
        return jsonConfig.toString();
    }
    
    public static void write() {
        JSONConfigWriter.logger.setLevel(Level.OFF);
        String jsonConfig = JSONConfigWriter.buildJSONString();
        System.out.println(jsonConfig);
    }
    
    public static void main(String args[]) {
        JSONConfigWriter.write();
    }
}
