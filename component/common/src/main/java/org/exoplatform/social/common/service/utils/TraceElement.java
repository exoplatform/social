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
package org.exoplatform.social.common.service.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;

public class TraceElement {
  
  private AtomicLong elapsedTime = new AtomicLong(0);
  private final String traceName;
  private final LogWatch logWatch;
  private TraceElement parent;
  
  private List<TraceElement> children;

  private TraceElement(String traceName) {
    logWatch = new LogWatch(traceName);
    this.traceName = traceName;
  }
  
  public List<TraceElement> getChildren() {
    if (children == null) {
      children = new ArrayList<TraceElement>(); 
    }
    return children;
  }
  
  public static TraceElement getInstance(String traceName) {
    return new TraceElement(traceName);
  }
  
  public TraceElement addChild(String traceName) {
    TraceElement child = getInstance(traceName);
    child.setParrent(this);
    
    //
    getChildren().add(child);
    
    //
    return child;
  }
  
  public void start() {
    logWatch.start();
  }
  
  public void end() {
    this.elapsedTime.set(logWatch.elapsedTime());
  }
  
  public long getElapsedTime() {
    return this.elapsedTime.get();
  }
  
  public TraceElement getParent() {
    return parent;
  }
  
  public boolean hasParent() {
    return this.parent != null;
  }
  
  public void setParrent(TraceElement parent) {
    this.parent = parent;
  }
  
  public String getName() {
    return this.traceName;
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    
    if (!(o instanceof TraceElement)) {
      return false;
    }

    TraceElement element = (TraceElement) o;

    if (traceName != element.traceName) {
      return false;
    }

    return true;
  }
  
  @Override
  public String toString() {
    return String.format("%s - %s ", this.traceName, logWatch.toString(getElapsedTime(), TimeUnit.MILLISECONDS));
  }
  
  public String toReport() {
    StringBuffer out = new StringBuffer();
    if (getChildren().size() > 0) {
      out.append("{");
      
      int len = getChildren().size();
      int index = 0;
      
      for(TraceElement e : getChildren()) {
        if (++index > 1) out.append("{space}");
          
        out.append(e.toString());
        
        if (index < len) out.append("\n");
        
        elapsedTime.addAndGet(e.getElapsedTime());
        
      }
      out.append("}");
    }
    
    String head = String.format("%s - %s ", this.traceName, logWatch.toString(getElapsedTime(), TimeUnit.MILLISECONDS));
    String tail = StringUtils.replace(out.toString(), "{space}", StringUtils.repeat(" ", head.length() + 1));
    
    return head + tail;
  }
}
