/**
 *
 */
package org.aksw.deer.workflow.specslearner;

import java.util.List;
import org.aksw.deer.modules.DeerModule;
import org.aksw.deer.modules.ModuleFactory;

/**
 * @author sherif
 */
public interface PipelineLearner {

  List<DeerModule> MODULES = ModuleFactory.getImplementations();
}