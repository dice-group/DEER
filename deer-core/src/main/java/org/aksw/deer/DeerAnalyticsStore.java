package org.aksw.deer;


import org.apache.jena.rdf.model.Resource;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 *
 */
public class DeerAnalyticsStore {

  private static Map<String, JSONObject> backend = new HashMap<>();

  public static synchronized void write(String jobId, Resource nodeId, JSONObject nodeJson) {
    String nodeName = nodeId.toString();
    JSONObject jobJson = backend.get(jobId);
    if (jobJson == null) {
      jobJson = new JSONObject();
      backend.put(jobId, jobJson);
    }
    JSONObject oldNodeJson = jobJson.optJSONObject(nodeName);
    if (oldNodeJson == null) {
      jobJson.put(nodeName, nodeJson);
    } else {
      jobJson.put(nodeName, mergeJSONObjects(oldNodeJson, nodeJson));
    }
  }

  public static synchronized JSONObject getAnalyticsForJob(String jobId) {
    return backend.get(jobId);
  }

  private static JSONObject mergeJSONObjects(JSONObject json1, JSONObject json2) {
    JSONObject mergedJSON;
    try {
      mergedJSON = new JSONObject(json1, JSONObject.getNames(json1));
      for (String crunchifyKey : JSONObject.getNames(json2)) {
        mergedJSON.put(crunchifyKey, json2.get(crunchifyKey));
      }

    } catch (JSONException e) {
      throw new RuntimeException("JSON Exception" + e);
    }
    return mergedJSON;
  }

}
