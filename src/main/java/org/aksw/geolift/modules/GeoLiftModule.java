/**
 * 
 */
package org.aksw.geolift.modules;

import com.hp.hpl.jena.rdf.model.Model;
import java.util.List;
import java.util.Map;
import org.aksw.geolift.json.ParameterType;

/**
 * @author sherif
 *
 */
public interface GeoLiftModule {
	public Model process(Model model, Map<String, String> parameters);
	public List<String> getParameters();
        public List<ParameterType> getParameterWithTypes();
}
