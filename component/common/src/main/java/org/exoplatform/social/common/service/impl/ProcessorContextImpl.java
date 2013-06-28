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
package org.exoplatform.social.common.service.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.exoplatform.social.common.service.ProcessContext;
import org.exoplatform.social.common.service.SocialServiceContext;
import org.exoplatform.social.common.service.utils.ObjectHelper;
import org.exoplatform.social.common.service.utils.TraceElement;

public class ProcessorContextImpl implements ProcessContext {

  protected final SocialServiceContext context;
  private Map<String, Object> properties;
  private Exception exception;
  private boolean isDone;
  private StringBuffer tracer;
  private TraceElement traceElement;
  private String name;
  private Object lock = new Object();
  
  private AtomicInteger currentProcesses = new AtomicInteger(0);
  
  public ProcessorContextImpl(SocialServiceContext context) {
    this("DefaultProcessor", context);
  }
  
  public ProcessorContextImpl(String name, SocialServiceContext context) {
    this.context = context;
    this.tracer = new StringBuffer();
    this.name = name;
  }
  
  public ProcessorContextImpl(ProcessContext parent) {
    this(parent.getContext());
  }
  
  @Override
  public String toString() {
    return "ProcessorContext[Default]";
  }
  
  
  @Override
  public Object getProperty(String name) {
    if (properties != null) {
      return properties.get(name);
    }
    return null;
  }

  @Override
  public Object getProperty(String name, Object defaultValue) {
    Object got = getProperty(name);
    return got != null ? got : defaultValue;
  }

  @Override
  public <T> T getProperty(String name, Class<T> type) {
    Object value = getProperty(name);
    if (value == null) {
        // lets avoid NullPointerException when converting to boolean for null values
        if (boolean.class.isAssignableFrom(type)) {
            return (T) Boolean.FALSE;
        }
        if (int.class.isAssignableFrom(type)) {
          return (T) new Integer(-1);
        }
        return null;
    }

    // eager same instance type test to avoid the overhead of invoking the type converter
    // if already same type
    if (type.isInstance(value)) {
        return type.cast(value);
    }

    return (T) value;
  }

  @Override
  public <T> T getProperty(String name, Object defaultValue, Class<T> type) {
    Object value = getProperty(name, defaultValue);
    if (value == null) {
        // lets avoid NullPointerException when converting to boolean for null values
        if (boolean.class.isAssignableFrom(type)) {
            return (T) Boolean.FALSE;
        }
        return null;
    }

    // eager same instance type test to avoid the overhead of invoking the type converter
    // if already same type
    if (type.isInstance(value)) {
        return type.cast(value);
    }

    return (T) value;
  }

  @Override
  public void setProperty(String name, Object value) {
    if (value != null) {
      // avoid the NullPointException
      getProperties().put(name, value);
  } else {
      // if the value is null, we just remove the key from the map
      if (name != null) {
          getProperties().remove(name);
      }
  }
  }

  @Override
  public Object removeProperty(String name) {
    if (!hasProperties()) {
      return null;
    }
    return getProperties().remove(name);
  }

  @Override
  public Map<String, Object> getProperties() {
    if (properties == null) {
      properties = new ConcurrentHashMap<String, Object>();
    }
    return properties;
  }

  @Override
  public boolean hasProperties() {
    return properties != null && !properties.isEmpty();
  }

  public Exception getException() {
    return exception;
  }

  public <T> T getException(Class<T> type) {
    return ObjectHelper.getException(type, exception);
  }

  public void setException(Throwable t) {
    if (t == null) {
      this.exception = null;
    } else if (t instanceof Exception) {
      this.exception = (Exception) t;
    } else {
      this.exception = new Exception("Exception occurred during execution" ,t);
    }
  }

  @Override
  public boolean isFailed() {
    return getException() != null;
  }

  @Override
  public ProcessContext copy() {
    return null;
  }

  @Override
  public String getProcessorCompletion() {
    return null;
  }

  @Override
  public void setProcessorName(String name) {
    
  }

  @Override
  public SocialServiceContext getContext() {
    return context;
  }
  
  private static Map<String, Object> safeCopy(Map<String, Object> properties) {
    if (properties == null) {
        return null;
    }
    return new ConcurrentHashMap<String, Object>(properties);
  }

  @Override
  public boolean isInProgress() {
    return false;
  }

  @Override
  public boolean isDone() {
    return isDone;
  }
  
  @Override
  public void done(boolean isDone) {
    this.isDone = isDone;
  }

  @Override
  public void trace(String processorName, String trace) {
    synchronized (lock) {
      tracer.append(String.format("%s::%s-", processorName, trace));
    }
  }

  @Override
  public String getTraceLog() {
    return new StringBuffer().append(tracer.toString())
                             .append(getTraceElement().toString())
                             .toString();
  }
  
  @Override
  public StringBuffer getTracer() {
    return tracer;
  }
  
  @Override
  public TraceElement getTraceElement() {
    if (traceElement == null) {
      traceElement = TraceElement.getInstance(this.name);
    }
    return traceElement;
  }
  
  @Override
  public void setTraceElement(TraceElement traceElement) {
    this.traceElement = traceElement;
  }

  @Override
  public void totalProcesses(int total) {
    currentProcesses.set(total);
  }

  @Override
  public int getTotalProcesses() {
    return currentProcesses.get();
  }
  
}
