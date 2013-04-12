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
package org.exoplatform.social.core.activity.filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;

public class ActivityIteratorTest extends TestCase {
  private ActivityIterator ait = null;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }
  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    
    ait = null;
  }

  public void testAddMore() throws Exception {
    ait = new ActivityIterator(0, 0, 0);
    assertFalse(ait.addMore());
  }
  
  public void testAddOneElement() throws Exception {
    ait = new ActivityIterator(0, 1, 1);
    ExoSocialActivity activity1 = new ExoSocialActivityImpl();
    activity1.setTitle("title @demo hi");
    ait.add(activity1);
    
    assertFalse(ait.addMore());
  }
  
  public void testAddMoreElements() throws Exception {
    ait = new ActivityIterator(0, 2, 2);
    ExoSocialActivity activity1 = new ExoSocialActivityImpl();
    activity1.setId("id1");
    activity1.setTitle("title @demo hi");
    ait.add(activity1);
    
    assertTrue(ait.addMore());
    
    // the same activityId
    activity1 = new ExoSocialActivityImpl();
    activity1.setId("id1");
    activity1.setTitle("title 2 hi");
    ait.add(activity1);
    
    assertTrue(ait.addMore());
    
    // the same activityId
    activity1 = new ExoSocialActivityImpl();
    activity1.setId("id3");
    activity1.setTitle("title 3 hi");
    ait.add(activity1);
    
    assertFalse(ait.addMore());
  }
  
  public void testAddMoreElementsOffset1() throws Exception {
    ait = new ActivityIterator(1, 2, 3);
    ExoSocialActivity activity1 = new ExoSocialActivityImpl();
    activity1.setId("id1");
    activity1.setTitle("title @demo hi");
    ait.add(activity1);
    
    assertTrue(ait.addMore());
    
    // the same activityId
    activity1 = new ExoSocialActivityImpl();
    activity1.setId("id1");
    activity1.setTitle("title 2 hi");
    ait.add(activity1);
    
    assertTrue(ait.addMore());
    
    // the same activityId
    activity1 = new ExoSocialActivityImpl();
    activity1.setId("id3");
    activity1.setTitle("title 3 hi");
    ait.add(activity1);
    
    assertFalse(ait.addMore());
    assertEquals(2, ait.result().size());
  }
  
  public void testAddMoreElementsOffset2() throws Exception {
    ait = new ActivityIterator(2, 2, 3);
    ExoSocialActivity activity1 = new ExoSocialActivityImpl();
    activity1.setId("id1");
    activity1.setTitle("title @demo hi");
    ait.add(activity1);
    
    assertTrue(ait.addMore());
    
    // the same activityId
    activity1 = new ExoSocialActivityImpl();
    activity1.setId("id2");
    activity1.setTitle("title 2 hi");
    ait.add(activity1);
    
    assertTrue(ait.addMore());
    
    // the same activityId
    activity1 = new ExoSocialActivityImpl();
    activity1.setId("id3");
    activity1.setTitle("title 3 hi");
    ait.add(activity1);
    
    assertFalse(ait.addMore());
    assertEquals(2, ait.result().size());
  }
  
  public void testAddMoreElementsOffset3() throws Exception {
    ait = new ActivityIterator(3, 2, 3);
    ExoSocialActivity activity1 = new ExoSocialActivityImpl();
    activity1.setId("id1");
    activity1.setTitle("title @demo hi");
    ait.add(activity1);
    
    assertTrue(ait.addMore());
    
    // the same activityId
    activity1 = new ExoSocialActivityImpl();
    activity1.setId("id2");
    activity1.setTitle("title 2 hi");
    ait.add(activity1);
    
    assertTrue(ait.addMore());
    
    // the same activityId
    activity1 = new ExoSocialActivityImpl();
    activity1.setId("id3");
    activity1.setTitle("title 3 hi");
    ait.add(activity1);
    
    assertTrue(ait.addMore());
    assertEquals(1, ait.result().size());
  }
  
  public void testCalculatorOffset0() throws Exception {
    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();
    
    //fill 5 others
    for (int i = 0; i < 5; ++i) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setId("id " + i);
      activity.setTitle("title " + i);
      activity.isComment(false);
      activities.add(activity);
    }
    
    long offset  = 0;
    long limit  = 20;
    ExoSocialActivity entity = null;
    
    ActivityIterator ait = new ActivityIterator(offset, limit, 5);
    
    //
    Iterator<ExoSocialActivity> it = activities.iterator();

    //
    while (it.hasNext()) {
      entity = it.next();

      ait.add(entity);

      //
      if (ait.addMore() == false) {
        break;
      }
    }

    //
    assertEquals(5, ait.result().size());
  }
  
  public void testCalculatorOffsetGreater0() throws Exception {
    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();
    
    //fill 5 others
    for (int i = 0; i < 5; ++i) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setId("id " + i);
      activity.setTitle("title " + i);
      activity.isComment(false);
      activities.add(activity);
    }
    
    long offset  = 2;
    long limit  = 20;
    ExoSocialActivity entity = null;
    
    ActivityIterator ait = new ActivityIterator(offset, limit, 5);
    
    //
    Iterator<ExoSocialActivity> it = activities.iterator();

    //
    while (it.hasNext()) {
      entity = it.next();

      ait.add(entity);

      //
      if (ait.addMore() == false) {
        break;
      }
    }
    
    //
    assertEquals(4, ait.result().size());
  }
  

}
