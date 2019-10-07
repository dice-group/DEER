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
    if (nodeId == null) {
      writeGlobal(jobId, nodeJson);
      return;
    }
    String nodeName = nodeId.toString();
    JSONObject jobJson = backend.get(jobId);
    if (jobJson == null) {
      jobJson = new JSONObject();
      backend.put(jobId, jobJson);
    }
    JSONObject insertJson = jobJson.optJSONObject("operatorStats");
    if (insertJson == null) {
      jobJson.put("operatorStats", new JSONObject());
      insertJson = jobJson.getJSONObject("operatorStats");
    }
    JSONObject oldNodeJson = insertJson.optJSONObject(nodeName);
    if (oldNodeJson == null) {
      insertJson.put(nodeName, nodeJson);
    } else {
      insertJson.put(nodeName, mergeJSONObjects(oldNodeJson, nodeJson));
    }
  }

  private static void writeGlobal(String jobId, JSONObject json) {
    JSONObject jobJson = backend.get(jobId);
    if (jobJson == null) {
      jobJson = new JSONObject();
      backend.put(jobId, jobJson);
    }
    JSONObject oldJson = jobJson.optJSONObject("globalStats");
    if (oldJson == null) {
      jobJson.put("globalStats", json);
    } else {
      jobJson.put("globalStats", mergeJSONObjects(oldJson, json));
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
