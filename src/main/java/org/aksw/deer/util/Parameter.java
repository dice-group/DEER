package org.aksw.deer.util;

import java.util.function.Consumer;

/**
 * @author Kevin Dreßler
 */
public class Parameter {

  private String name;
  private String description;
  private String defaultValue;
  private boolean required;
  private boolean enumerable;
  private Consumer<String> assignmentConsumer;

  public Parameter(String name, String description, String defaultValue, boolean required,
    boolean enumerable, Consumer<String> assignmentConsumer) {
    this.name = name;
    this.description = description;
    this.defaultValue = defaultValue;
    this.required = required;
    this.enumerable = enumerable;
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

  public boolean isEnumerable() {
    return enumerable;
  }
}
