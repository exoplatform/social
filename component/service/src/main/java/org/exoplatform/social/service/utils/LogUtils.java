package org.exoplatform.social.service.utils;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class LogUtils {

  public static void logInfo(String serviceName,
                             String operationName,
                             String parameters,
                             Class name) {
    Log log = ExoLogger.getLogger(name);
    log.info("service={} operation={} parameters=\"{}\"",
             serviceName,
             operationName,
             parameters);
  }

  public static void logError(String serviceName,
                              String operationName,
                              String parameters,
                              Class name) {
    Log log = ExoLogger.getLogger(name);
    log.error("service={} operation={} parameters=\"{}\"",
             serviceName,
             operationName,
             parameters);
  }
}
