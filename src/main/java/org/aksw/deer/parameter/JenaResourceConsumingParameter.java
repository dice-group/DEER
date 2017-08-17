package org.aksw.deer.parameter;

import java.util.function.Consumer;
import org.aksw.deer.parameter.Parameter;

/**
 * @author Kevin Dreßler
 */
public class JenaResourceConsumingParameter {

  private String name;
  private String description;
  private String defaultValue;
  private boolean required;
  private Consumer<String> assignmentConsumer;

  public JenaResourceConsumingParameter(String name, String description, String defaultValue, boolean required,
    Consumer<String> assignmentConsumer) {
    this.name = name;
    this.description = description;
    this.defaultValue = defaultValue;
    this.required = required;
    this.assignmentConsumer = assignmentConsumer;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public Consumer<String> getAssignmentConsumer() {
    return assignmentConsumer;
  }

  public boolean isRequired() {
    return required;
  }

}
