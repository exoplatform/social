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
package org.exoplatform.social.core.feature;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import junit.framework.TestCase;

import org.exoplatform.social.common.jcr.filter.FilterLiteral.DIRECTION;
import org.exoplatform.social.core.activity.filter.ActivityFilter;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.storage.api.ActivityStorage.TimestampType;
import org.exoplatform.social.core.storage.impl.ActivityBuilderWhere;

/**
 * Created by The eXo Platform SAS
 * Author : thanh_vucong
 *          thanh_vucong@exoplatform.com
 * Nov 29, 2012  
 */
public class ActiviyBuilderWhereTest extends TestCase {
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
  
  public void testWhereOwners() throws Exception {
    ActivityFilter filter = new ActivityFilter() {};
    ActivityBuilderWhere where = ActivityBuilderWhere.ACTIVITY_BUILDER;
    List<Identity> identities = new ArrayList<Identity>(2);
    identities.add(demoIdentity);
    identities.add(rootIdentity);
    where.owners(identities);
    
    String expectedWhere = "(soc:identity = 'demo123456' OR soc:identity = 'root123456' ) AND soc:isComment = 'false' ";
    String actualWhere =  where.build(filter);
    assertEquals(expectedWhere, actualWhere);
  }
  
  
  public void testFeedNewerOwners() throws Exception {
    ActivityFilter filter = ActivityFilter.ACTIVITY_FEED_NEWER_FILTER;
    ActivityBuilderWhere where = ActivityBuilderWhere.ACTIVITY_FEED_BUILDER;

    long accessPoint = Calendar.getInstance().getTime().getTime();
    
    filter.with(ActivityFilter.ACTIVITY_POINT_FIELD).value(TimestampType.NEWER.from(accessPoint));
    
    //
    List<Identity> identities = new ArrayList<Identity>(2);
    identities.add(demoIdentity);
    identities.add(rootIdentity);
    where.owners(identities);
    
    String expectedWhere = "(soc:identity = 'demo123456' OR soc:identity = 'root123456' ) AND soc:isComment = 'false' AND soc:lastUpdated > " + accessPoint + " ";
    String actualWhere =  where.build(filter);
    assertEquals(expectedWhere, actualWhere);
    
    //Order
    assertEquals(DIRECTION.ASC, filter.get(ActivityFilter.POSTED_TIME_ORDERBY).getDirection());
    assertEquals(DIRECTION.ASC, filter.get(ActivityFilter.LAST_UPDATED_ORDERBY).getDirection());
  }
  
  
  public void testFeedOlderOwners() throws Exception {
    ActivityFilter filter = ActivityFilter.ACTIVITY_FEED_OLDER_FILTER;
    ActivityBuilderWhere where = ActivityBuilderWhere.ACTIVITY_FEED_BUILDER;

    long accessPoint = Calendar.getInstance().getTime().getTime();
    
    filter.with(ActivityFilter.ACTIVITY_POINT_FIELD).value(TimestampType.OLDER.from(accessPoint));
    
    //
    List<Identity> identities = new ArrayList<Identity>(2);
    identities.add(demoIdentity);
    identities.add(rootIdentity);
    where.owners(identities);
    
    String expectedWhere = "(soc:identity = 'demo123456' OR soc:identity = 'root123456' ) AND soc:isComment = 'false' AND soc:lastUpdated < " + accessPoint + " ";
    String actualWhere =  where.build(filter);
    assertEquals(expectedWhere, actualWhere);
    
    //Order
    assertEquals(DIRECTION.DESC, filter.get(ActivityFilter.POSTED_TIME_ORDERBY).getDirection());
    assertEquals(DIRECTION.DESC, filter.get(ActivityFilter.LAST_UPDATED_ORDERBY).getDirection());
  }
  
  public void testUserNewerOwner() throws Exception {
    ActivityFilter filter = ActivityFilter.ACTIVITY_NEWER_FILTER;
    ActivityBuilderWhere where = ActivityBuilderWhere.ACTIVITY_BUILDER;

    long accessPoint = Calendar.getInstance().getTime().getTime();
    
    filter.with(ActivityFilter.ACTIVITY_POINT_FIELD).value(TimestampType.NEWER.from(accessPoint));
  
    //
    List<Identity> identities = new ArrayList<Identity>(2);
    identities.add(demoIdentity);
    where.owners(identities);
    
    String expectedWhere = "(soc:identity = 'demo123456' ) AND soc:isComment = 'false' AND soc:postedTime > " + accessPoint + " ";
    String actualWhere =  where.build(filter);
    assertEquals(expectedWhere, actualWhere);
    
    //Order
    assertEquals(DIRECTION.ASC, filter.get(ActivityFilter.POSTED_TIME_ORDERBY).getDirection());
  }
  
  public void testUserOlderOwner() throws Exception {
    ActivityFilter filter = ActivityFilter.ACTIVITY_OLDER_FILTER;
    ActivityBuilderWhere where = ActivityBuilderWhere.ACTIVITY_BUILDER;

    long accessPoint = Calendar.getInstance().getTime().getTime();
    
    filter.with(ActivityFilter.ACTIVITY_POINT_FIELD).value(TimestampType.OLDER.from(accessPoint));
    
    //
    List<Identity> identities = new ArrayList<Identity>(2);
    identities.add(demoIdentity);
    where.owners(identities);
    
    String expectedWhere = "(soc:identity = 'demo123456' ) AND soc:isComment = 'false' AND soc:postedTime < " + accessPoint + " ";
    String actualWhere =  where.build(filter);
    assertEquals(expectedWhere, actualWhere);
    
    //Order
    assertEquals(DIRECTION.DESC, filter.get(ActivityFilter.POSTED_TIME_ORDERBY).getDirection());
  }
  
  public void testConnectionNewerOwners() throws Exception {
    ActivityFilter filter = ActivityFilter.ACTIVITY_NEWER_FILTER;
    ActivityBuilderWhere where = ActivityBuilderWhere.ACTIVITY_BUILDER;

    long accessPoint = Calendar.getInstance().getTime().getTime();
    
    filter.with(ActivityFilter.ACTIVITY_POINT_FIELD).value(TimestampType.NEWER.from(accessPoint));
    
    //
    List<Identity> identities = new ArrayList<Identity>(2);
    identities.add(demoIdentity);
    identities.add(rootIdentity);
    where.owners(identities);
    
    String expectedWhere = "(soc:identity = 'demo123456' OR soc:identity = 'root123456' ) AND soc:isComment = 'false' AND soc:postedTime > " + accessPoint + " ";
    String actualWhere =  where.build(filter);
    assertEquals(expectedWhere, actualWhere);
    
    //Order
    assertEquals(DIRECTION.ASC, filter.get(ActivityFilter.POSTED_TIME_ORDERBY).getDirection());
  }
  
  public void testConnectionOlderOwners() throws Exception {
    ActivityFilter filter = ActivityFilter.ACTIVITY_OLDER_FILTER;
    ActivityBuilderWhere where = ActivityBuilderWhere.ACTIVITY_BUILDER;

    long accessPoint = Calendar.getInstance().getTime().getTime();
    
    filter.with(ActivityFilter.ACTIVITY_POINT_FIELD).value(TimestampType.OLDER.from(accessPoint));
    
    //
    List<Identity> identities = new ArrayList<Identity>(2);
    identities.add(demoIdentity);
    identities.add(rootIdentity);
    where.owners(identities);
    
    String expectedWhere = "(soc:identity = 'demo123456' OR soc:identity = 'root123456' ) AND soc:isComment = 'false' AND soc:postedTime < " + accessPoint + " ";
    String actualWhere =  where.build(filter);
    assertEquals(expectedWhere, actualWhere);
    
    //Order
    assertEquals(DIRECTION.DESC, filter.get(ActivityFilter.POSTED_TIME_ORDERBY).getDirection());
  }

}
