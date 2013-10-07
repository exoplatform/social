/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.common.service;

import java.util.Map;

import org.exoplatform.social.common.service.utils.TraceElement;

public interface ProcessContext {

  Object getProperty(String name);
  
  Object getProperty(String name, Object defaultValue);
  
  <T> T getProperty(String name, Class<T> type);
  
  <T> T getProperty(String name, Object defaultValue, Class<T> type);
  
  void setProperty(String name, Object value);
  
  Object removeProperty(String name);
  
  Map<String, Object> getProperties();
  
  boolean hasProperties();
  
  Exception getException();
  
  <T> T getException(Class<T> type);
  
  void setException(Throwable t);
  
  boolean isFailed();
  
  boolean isInProgress();
  
  boolean isDone();
  
  void done(boolean isDone);
  
  ProcessContext copy();

  String getProcessorCompletion();
  
  void setProcessorName(String name);
  
  SocialServiceContext getContext();
  
  void trace(String processorName, String trace);
  
  StringBuffer getTracer();
  
  String getTraceLog();
  
  void setTraceElement(TraceElement traceElement);
  
  TraceElement getTraceElement();
  
  void totalProcesses(int total);
  
  int getTotalProcesses();
}
