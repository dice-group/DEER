/**
 *
 */
package org.aksw.deer.workflow.specslearner;

import org.aksw.deer.modules.DeerModule;
import org.aksw.deer.modules.ModuleFactory;

import java.util.List;

/**
 * @author sherif
 *
 */
public interface PipelineLearner {
    List<DeerModule> MODULES = ModuleFactory.getImplementations();
}