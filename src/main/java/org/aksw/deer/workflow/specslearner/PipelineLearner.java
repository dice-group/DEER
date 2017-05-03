/**
 *
 */
package org.aksw.deer.workflow.specslearner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.aksw.deer.modules.DeerModule;
import org.aksw.deer.modules.dereferencing.DereferencingModule;
import org.aksw.deer.modules.authorityconformation.AuthorityConformationModule;
import org.aksw.deer.modules.filter.FilterModule;
import org.aksw.deer.modules.linking.LinkingModule;
import org.aksw.deer.modules.nlp.NLPModule;
import org.aksw.deer.modules.predicateconformation.PredicateConformationModule;

/**
 * @author sherif
 *
 */
public interface PipelineLearner {
    static final Set<DeerModule> MODULES =
            new HashSet<DeerModule>(Arrays.asList(
                    new LinkingModule(),
                    new NLPModule(),
                    new FilterModule(),
                    new AuthorityConformationModule(),
                    new PredicateConformationModule(),
                    new DereferencingModule()
            ));

//	public RefinementNodeOld run();
}
