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

import org.exoplatform.social.common.service.utils.TraceList;

import junit.framework.TestCase;

public class TraceListTest extends TestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }
  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }
  
  public void testAddChild() throws Exception {
    TraceList list = TraceList.getInstance();
    list.addElement("Feed", "Feed-Thread-1");
    list.addElement("Connections", "Feed-Thread-1");
    list.addElement("MySpaces", "Feed-Thread-1");
    list.addElement("MyActivities", "Feed-Thread-1");
    
    list.addElement("Feed", "Feed-Thread-2");
    
    //
    String got = list.toString();
    
    //
    System.out.print(got);
    assertTrue(got.indexOf("Feed") > 0);
  }
  
  public void testReport() throws Exception {
    TraceList list = TraceList.getInstance();
    list.addElement("Feed", "Feed-Thread-1");
    list.addElement("Connections", "Connections-Thread-1");
    list.addElement("MySpaces", "MySpaces-Thread-1");
    list.addElement("MyActivities", "MyActivities-Thread-1");
    
    list.addElement("Feed", "Feed-Thread-2");
    
    //
    String got = list.toReport();
    
    //
    System.out.print(got);
    assertTrue(got.indexOf("Feed") > 0);
  }
}
