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
package org.exoplatform.social.core.identity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.exoplatform.social.core.identity.model.Identity;

public class IdentityResultTest extends TestCase {
  
  Identity demoIdentity = null;
  
  Identity rootIdentity = null;
  
  Identity maryIdentity = null;
  
  Identity johnIdentity = null;
  
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    init();
  }
  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    demoIdentity = null;
    rootIdentity = null;
    maryIdentity = null;
    johnIdentity = null;
        
  }
  
  private void init() {
    demoIdentity = new Identity("demo123456");
    demoIdentity.setRemoteId("demo");
    
    rootIdentity = new Identity("root123456");
    rootIdentity.setRemoteId("root");
    
    maryIdentity = new Identity("mary123456");
    maryIdentity.setRemoteId("mary");
    
    johnIdentity = new Identity("john123456");
    johnIdentity.setRemoteId("john");
  }
  
  public void testResult() throws Exception {
    IdentityResult result = new IdentityResult(4);
    
    result.add(demoIdentity);
    result.add(rootIdentity);
    result.add(maryIdentity);
    result.add(johnIdentity);
    //
    result.add(demoIdentity);
    
    assertEquals(4, result.size());
  }
  
  public void testResultOffsetLimit() throws Exception {
    //offset and limit result = 2
    IdentityResult result = new IdentityResult(0, 2, 4);
    
    List<Identity> identities = new ArrayList<Identity>(4);
    identities.add(demoIdentity);
    identities.add(rootIdentity);
    identities.add(maryIdentity);
    identities.add(johnIdentity);
    
    Iterator<Identity> it = identities.iterator();
    
    while(it.hasNext()) {
      result.add(it.next());
      
      if (result.addMore() == false) {
        break;
      }
    }
    
    assertEquals(2, result.size());
    
    it = identities.iterator();
    
    //offset and limit result = 2
    result = new IdentityResult(2, 1, 4);
    
    while(it.hasNext()) {
      result.add(it.next());
      
      if (result.addMore() == false) {
        break;
      }
    }
    
    assertEquals(1, result.size());
  }


}
