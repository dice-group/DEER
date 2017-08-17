package org.aksw.deer.parameter;

import java.util.function.Consumer;

/**
 * @author Kevin Dreßler
 */
public interface ParameterAssigner<T extends ParameterMap> extends Consumer<T> {

}
