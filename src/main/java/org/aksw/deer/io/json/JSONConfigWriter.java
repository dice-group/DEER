/*
 *
 */
package org.aksw.deer.io.json;

import java.util.ArrayList;
import java.util.List;
import org.aksw.deer.plugin.enrichment.IEnrichmentFunction;
import org.aksw.deer.plugin.enrichment.EnrichmentFunctionFactory;
import org.aksw.deer.plugin.operator.OperatorFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author eugen
 */
@Deprecated
public class JSONConfigWriter {

  private static final Logger moduleLogger = Logger.getLogger(IEnrichmentFunction.class.getName());

  private static String join(String[] values) {
    StringBuilder joined = new StringBuilder();
    for (int i = 0; i < values.length; i++) {
      joined.append("\"").append(values[i]).append("\"");
      if (i != values.length - 1) {
        joined.append(",");
      }
    }

    return joined.toString();
  }

  private static String buildModuleJSONString(String name, IEnrichmentFunction module) {
    StringBuilder moduleJSONConfig = new StringBuilder();
    StringBuilder moduleJSONConfigRequiredParams = new StringBuilder();
    moduleJSONConfig.append("{");
    moduleJSONConfig.append("\"$schema\":\"http://json-schema.org/draft-04/schema#\",");
    moduleJSONConfig.append("\"title\":\"").append(name).append("\",");
    moduleJSONConfig.append("\"description\":\"").append(EnrichmentFunctionFactory.getDescription(name))
      .append("\",");

    List<ParameterType> parameters = module.getParameterWithTypes();
    if (parameters.size() > 0) {
      moduleJSONConfig.append("\"type\":\"object\",");
      moduleJSONConfig.append("\"properties\":{");

      //create a list of required parameters
      List<ParameterType> requiredParameters = new ArrayList<ParameterType>();
      for (ParameterType parameter : parameters) {
        if (parameter.getRequired()) {
          requiredParameters.add(parameter);
        }
      }

      //create string of all parameters
      int parameterCounter = 0;
      int requiredParameterCounter = 0;
      for (ParameterType parameter : parameters) {
        String[] values = parameter.getValues();

        moduleJSONConfig.append("\"").append(parameter.getName()).append("\":{");
        moduleJSONConfig.append("\"description\":\"").append(parameter.getDescription())
          .append("\",");

        if (null == values) {
          moduleJSONConfig.append("\"type\":\"").append(parameter.getType()).append("\"");
        } else {
          moduleJSONConfig.append("\"enum\":[").append(join(values)).append("]");
        }

        moduleJSONConfig.append("}");

        if (requiredParameters.contains(parameter)) {
          moduleJSONConfigRequiredParams.append("\"").append(parameter.getName()).append("\"");
          if (requiredParameterCounter < requiredParameters.size() - 1) {
            moduleJSONConfigRequiredParams.append(",");
          }
          requiredParameterCounter++;
        }

        if (parameterCounter < parameters.size() - 1) {
          moduleJSONConfig.append(",");
        }
        parameterCounter++;
      }

      moduleJSONConfig.append("}");
    }

    if (moduleJSONConfigRequiredParams.length() > 0) {
      moduleJSONConfig.append(",\"required\":[").append(moduleJSONConfigRequiredParams).append("]");
    }

    moduleJSONConfig.append("}");

    return moduleJSONConfig.toString();
  }

  private static String buildOperatorJSONString(String name) {
    StringBuilder operatorJSONConfig = new StringBuilder();
    operatorJSONConfig.append("{");
    operatorJSONConfig.append("\"$schema\":\"http://json-schema.org/draft-04/schema#\",");
    operatorJSONConfig.append("\"title\":\"").append(name).append("\",");
    operatorJSONConfig.append("\"description\":\"").append(OperatorFactory.getDescription(name))
      .append("\"");

    //there are no parameters used in operator.
    //merge: model x model -> model
    //split: model -> model x model
    operatorJSONConfig.append("}");

    return operatorJSONConfig.toString();
  }

  private static String buildJSONString() {
    //get enrichment
    List<String> moduleNames = EnrichmentFunctionFactory.getNames();

    //get operator
    List<String> operatorNames = OperatorFactory.getNames();

    StringBuilder jsonConfig = new StringBuilder();
    jsonConfig.append("{");

    jsonConfig.append("\"enrichment\":[");

    if (moduleNames.size() > 0) {
      int i = 0;
      for (String moduleName : moduleNames) {
        IEnrichmentFunction module = EnrichmentFunctionFactory.createModule(moduleName);
        jsonConfig.append(buildModuleJSONString(moduleName, module));

        if (i < moduleNames.size() - 1) {
          jsonConfig.append(",");
        }
        i++;
      }
    }
    jsonConfig.append("],");

    jsonConfig.append("\"operator\":[");
    if (operatorNames.size() > 0) {

      int i = 0;
      for (String operatorName : operatorNames) {
//                DeerOperator operator = ModelOperatorFactory.createOperator(operatorName);
        jsonConfig.append(buildOperatorJSONString(operatorName));

        if (i < operatorNames.size() - 1) {
          jsonConfig.append(",");
        }
        i++;
      }
    }
    jsonConfig.append("]");

    jsonConfig.append("}");

    return jsonConfig.toString();
  }

  public static void write() {
    JSONConfigWriter.moduleLogger.setLevel(Level.OFF);
    String jsonConfig = JSONConfigWriter.buildJSONString();
    System.out.println(jsonConfig);
  }

  public static void main(String args[]) {
    JSONConfigWriter.write();
  }
}
