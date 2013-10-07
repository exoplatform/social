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

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class TraceList {

  private final Map<String, TraceElement> traceGroups;
  
  public TraceList() {
    traceGroups = new ConcurrentHashMap<String, TraceElement>();
  }
  
  public static TraceList getInstance() {
    return new TraceList();
  }
  
  public TraceElement addElement(String groupName, String processName) {
    TraceElement parent = null;
    if (traceGroups.containsKey(groupName)) {
      parent = traceGroups.get(groupName);
    }
    
    //
    if (parent == null) {
      parent = TraceElement.getInstance(groupName);
    }
    
    parent.addChild(processName);
    
    traceGroups.put(groupName, parent);
    //
    return parent;
  }
  
  public int size() {
    return traceGroups.size();
  }
  
  @Override
  public String toString() {
    StringBuffer out = new StringBuffer();
    for(Entry<String, TraceElement> e : traceGroups.entrySet()) {
      out.append(e.getValue().toString()).append("::");
    }
    return out.append("\n").toString();
  }
  
  public String toReport() {
    StringBuffer out = new StringBuffer();
    for(Entry<String, TraceElement> e : traceGroups.entrySet()) {
      out.append(e.getValue().toReport()).append("\n");
    }
    return out.toString();
  }
}
