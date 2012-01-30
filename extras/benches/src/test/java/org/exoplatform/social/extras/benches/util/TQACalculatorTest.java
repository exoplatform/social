/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.social.extras.benches.util;

import junit.framework.TestCase;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS
 * Author : thanh_vucong
 *          thanhvucong.78@gmail.com
 * Jan 18, 2012  
 */
public class TQACalculatorTest  extends TestCase {
  private static Log          LOG = ExoLogger.getLogger(TQACalculatorTest.class);
  private int countR = 0;
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    countR = 0;
  }
  
  @Override
  protected void tearDown() throws Exception {
    countR = 0;
    super.tearDown();
  }
  
  public void testCalculateU9_R2() throws Exception {
    int numberOfUser = 9;
    int numberOfRelationship = 2;
    
    inject(numberOfUser, numberOfRelationship);
    LOG.info("U9 x R2::Total of created Relationships:" + (countR *2));
    assertEquals(18, countR *2);
  }
  
  public void testCalculateU10_R4() throws Exception {
    int numberOfUser = 10;
    int numberOfRelationship = 4;
    
    inject(numberOfUser, numberOfRelationship);
    LOG.info("U10 x R4::Total of created Relationships:" + (countR *2));
    assertEquals(40, countR *2);
  }
  
  public void testCalculateU12_R3() throws Exception {
    int numberOfUser = 12;
    int numberOfRelationship = 3;
    
    inject(numberOfUser, numberOfRelationship);
    LOG.info("U10 x R4::Total of created Relationships:" + (countR *2));
    assertEquals(36, countR *2);
  }
  
  public void testCalculateU50_R4() throws Exception {
    int numberOfUser = 50;
    int numberOfRelationship = 4;
    
    inject(numberOfUser, numberOfRelationship);
    LOG.info("U10 x R4::Total of created Relationships:" + (countR *2));
    assertEquals(200, countR *2);
  }
  
  public void testCalculateU50_R1() throws Exception {
    int numberOfUser = 50;
    int numberOfRelationship = 1;
    
    inject(numberOfUser, numberOfRelationship);
    LOG.info("U10 x R4::Total of created Relationships:" + (countR *2));
    assertEquals(50, countR *2);
  }
  
  public void testXCalculateU9_R3() throws Exception {
    int numberOfUser = 9;
    int numberOfRelationship = 3;
    
    inject(numberOfUser, numberOfRelationship);
    LOG.info("U9 x R3::Total of created Relationships:" + (countR *2));
    assertEquals(24, countR *2);
  }
  
  public void testXCalculateU12_R4() throws Exception {
    int numberOfUser = 12;
    int numberOfRelationship = 4;
    
    inject(numberOfUser, numberOfRelationship);
    LOG.info("U12 x R4::Total of created Relationships:" + (countR *2));
    assertEquals(42, countR *2);
  }
  
  public void testXCalculateU50_R3() throws Exception {
    int numberOfUser = 50;
    int numberOfRelationship = 3;
    
    inject(numberOfUser, numberOfRelationship);
    LOG.info("U50 x R3::Total of created Relationships:" + (countR *2));
    assertEquals(146, countR *2);
  }
  
  private void inject(int numberOfUser, int numberOfRelationship) throws Exception {
    int groupNo = calculateGroups(numberOfUser, numberOfRelationship);
    int low = 0;
    int high = numberOfRelationship + 1;
    for (int i = 0; i< groupNo; i++) {
      
      generateRelationship(low, high);
      low = high;
      //next high = (i + 2) * (numberofRelationship +1)
      high = ((i+2) * (numberOfRelationship + 1));
      if (high > numberOfUser) {
        high = numberOfUser;
      }
    }
  }

  private int calculateGroups(int numberOfUser, int numberOfRelationship) {
    int groupNo = numberOfUser/(numberOfRelationship +1);
    if (numberOfUser%(numberOfRelationship +1) > 0) {
      groupNo++;
    }
    return groupNo;
  }
  
  
  private void generateRelationship(int low, long high) {
    int k = 0;
    for(int j=low; j< high; j++) {
      k = j + 1;
      
      if (k > high) {
        break;
      }
      
      for(;k < high; k++) {
        LOG.warn("Identity[" + (j) + "] connected with Identity[" + k + "]");
        countR++;
      }
    }
  }
  
}
