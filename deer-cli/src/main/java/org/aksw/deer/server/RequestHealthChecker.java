package org.aksw.deer.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

/**
 *
 */
public class RequestHealthChecker extends Thread {

  private static final Logger logger = LoggerFactory.getLogger(RequestHealthChecker.class);

  private ConcurrentMap<String, CompletableFuture<Void>> requestsMap;
  private Set<String> visited = new HashSet<>();

  RequestHealthChecker(ConcurrentMap<String, CompletableFuture<Void>> requestsMap) {
    setName("RequestHealthCheckerThread");
    this.requestsMap = requestsMap;
  }

  @Override
  public void run() {
    while(true) {
      requestsMap.forEach((requestId, request) -> {
        if (request.isDone() && !visited.contains(requestId)) {
          try {
            visited.add(requestId);
            request.get();
          } catch (InterruptedException | ExecutionException e) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(baos));
            try {
              MDC.put("requestId", requestId);
              logger.error("Request " + requestId + " completed with exception:" + e.getMessage());
              logger.error(baos.toString("UTF-8"));
              MDC.remove("requestId");
            } catch (UnsupportedEncodingException ee) {
              throw new RuntimeException(ee);
            }
          }
        }
      });
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        return;
      }
    }
  }
}
