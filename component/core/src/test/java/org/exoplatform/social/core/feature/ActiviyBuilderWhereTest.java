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
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.DEFAULT)

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
    ActivityBuilderWhere where = ActivityBuilderWhere.simple();
    List<Identity> identities = new ArrayList<Identity>(2);
    identities.add(demoIdentity);
    identities.add(rootIdentity);
    where.owners(identities);
    
    String expectedWhere = "(soc:identity = 'demo123456' OR soc:identity = 'root123456' ) AND soc:isComment = 'false' AND (soc:isHidden = 'false' OR soc:isHidden Is NULL ) ";
    String actualWhere =  where.build(filter);
    assertEquals(expectedWhere, actualWhere);
  }
  
  
  public void testFeedNewerOwners() throws Exception {
    ActivityFilter filter = ActivityFilter.newer();
    ActivityBuilderWhere where = ActivityBuilderWhere.simple();

    long accessPoint = Calendar.getInstance().getTime().getTime();
    
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.NEWER.from(accessPoint));
    
    //
    List<Identity> identities = new ArrayList<Identity>(2);
    identities.add(demoIdentity);
    identities.add(rootIdentity);
    where.owners(identities);
    
    String expectedWhere = "(soc:identity = 'demo123456' OR soc:identity = 'root123456' ) AND soc:isComment = 'false' AND (soc:isHidden = 'false' OR soc:isHidden Is NULL ) AND soc:lastUpdated > " + accessPoint + " ";
    String actualWhere =  where.build(filter);
    assertEquals(expectedWhere, actualWhere);
    
    //Order
    assertEquals(DIRECTION.ASC, filter.get(ActivityFilter.POSTED_TIME_ORDERBY).getDirection());
  }
  
  public void testFeedOlderOwners() throws Exception {
    ActivityFilter filter = ActivityFilter.older();
    ActivityBuilderWhere where = ActivityBuilderWhere.simple();

    long accessPoint = Calendar.getInstance().getTime().getTime();
    
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.OLDER.from(accessPoint));
    
    //
    List<Identity> identities = new ArrayList<Identity>(2);
    identities.add(demoIdentity);
    identities.add(rootIdentity);
    where.owners(identities);
    
    String expectedWhere = "(soc:identity = 'demo123456' OR soc:identity = 'root123456' ) AND soc:isComment = 'false' AND (soc:isHidden = 'false' OR soc:isHidden Is NULL ) AND soc:lastUpdated < " + accessPoint + " ";
    String actualWhere =  where.build(filter);
    assertEquals(expectedWhere, actualWhere);
    
    //Order
    assertEquals(DIRECTION.DESC, filter.get(ActivityFilter.POSTED_TIME_ORDERBY).getDirection());
  }
  
  public void testFeedWithMentions() throws Exception {
    ActivityFilter filter = ActivityFilter.older();
    ActivityBuilderWhere where = ActivityBuilderWhere.simple();

    //
    List<Identity> identities = new ArrayList<Identity>(2);
    identities.add(demoIdentity);
    identities.add(rootIdentity);
    where.owners(identities);
    where.mentioner(maryIdentity);
    
    String expectedWhere = "(soc:identity = 'demo123456' OR soc:identity = 'root123456' OR CONTAINS (soc:mentioners, 'mary123456') ) AND soc:isComment = 'false' AND (soc:isHidden = 'false' OR soc:isHidden Is NULL ) ";
    String actualWhere =  where.build(filter);
    assertEquals(expectedWhere, actualWhere);
    
  }
  
  private class InvokeBuildMethods implements Runnable {
    ActivityBuilderWhere where = null;
    ActivityFilter filter = null;
    
    public InvokeBuildMethods(ActivityBuilderWhere where, ActivityFilter filter) {
      this.where = where;
      this.filter = filter;
    }
    
    public void run() {
      where.build(filter);
    }
  }
  
  public void testFeedWithMultiThread() throws Exception {
    ActivityFilter filter = ActivityFilter.older();
    ActivityBuilderWhere where = ActivityBuilderWhere.viewedRange();
    long accessPoint = Calendar.getInstance().getTime().getTime();
    //
    List<Identity> identities = null;
    
    for( int i = 0; i < 10; i++ ) {

      filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.OLDER.from(accessPoint));
      identities = new ArrayList<Identity>();
      identities.add(demoIdentity);
      identities.add(rootIdentity);
      where.owners(identities);
      
      new Thread(new InvokeBuildMethods(where, filter)).start();
    }
    
    for ( int idx = 0; idx < 50; idx++ ) {
      identities = new ArrayList<Identity>();
      Identity identity = new Identity("id" + idx);
      identity.setRemoteId("" + idx);
      identities.add(identity);
      where.owners(identities);
    }
  }
  
  public void testUserNewerOwner() throws Exception {
    ActivityFilter filter = ActivityFilter.newer();
    ActivityBuilderWhere where = ActivityBuilderWhere.simple();

    long accessPoint = Calendar.getInstance().getTime().getTime();
    
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.NEWER.from(accessPoint));
  
    //
    List<Identity> identities = new ArrayList<Identity>(2);
    identities.add(demoIdentity);
    where.owners(identities);
    
    String expectedWhere = "(soc:identity = 'demo123456' ) AND soc:isComment = 'false' AND (soc:isHidden = 'false' OR soc:isHidden Is NULL ) AND soc:lastUpdated > " + accessPoint + " ";
    String actualWhere =  where.build(filter);
    assertEquals(expectedWhere, actualWhere);
    
    //Order
    assertEquals(DIRECTION.ASC, filter.get(ActivityFilter.POSTED_TIME_ORDERBY).getDirection());
  }
  
  public void testUserOlderOwner() throws Exception {
    ActivityFilter filter = ActivityFilter.older();
    ActivityBuilderWhere where = ActivityBuilderWhere.simple();

    long accessPoint = Calendar.getInstance().getTime().getTime();
    
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.OLDER.from(accessPoint));
    
    //
    List<Identity> identities = new ArrayList<Identity>(2);
    identities.add(demoIdentity);
    where.owners(identities);
    
    String expectedWhere = "(soc:identity = 'demo123456' ) AND soc:isComment = 'false' AND (soc:isHidden = 'false' OR soc:isHidden Is NULL ) AND soc:lastUpdated < " + accessPoint + " ";
    String actualWhere =  where.build(filter);
    assertEquals(expectedWhere, actualWhere);
    
    //Order
    assertEquals(DIRECTION.DESC, filter.get(ActivityFilter.POSTED_TIME_ORDERBY).getDirection());
  }
  
  public void testUserSpaceOwner() throws Exception {
    ActivityFilter filter = ActivityFilter.space();
    ActivityBuilderWhere where = ActivityBuilderWhere.userSpaces();

    Identity spaceIdentity = new Identity("space_new1");
    spaceIdentity.setRemoteId("space_new1");
    
    //
    List<Identity> identities = new ArrayList<Identity>(2);
    identities.add(spaceIdentity);
    where.owners(identities);
    
    String expectedWhere = "(soc:identity = 'space_new1' ) AND soc:isComment = 'false' AND (soc:isHidden = 'false' OR soc:isHidden Is NULL ) ";
    String actualWhere =  where.build(filter);
    assertEquals(expectedWhere, actualWhere);
    
    //Order
    assertEquals(DIRECTION.DESC, filter.get(ActivityFilter.LAST_UPDATED_ORDERBY).getDirection());
    assertEquals(DIRECTION.DESC, filter.get(ActivityFilter.POSTED_TIME_ORDERBY).getDirection());
  }
  
  public void testUserSpaceNewerOwner() throws Exception {
    ActivityFilter filter = ActivityFilter.spaceNewer();
    ActivityBuilderWhere where = ActivityBuilderWhere.userSpaces();

    long accessPoint = Calendar.getInstance().getTime().getTime();
    
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.NEWER.from(accessPoint));
  
    //
    List<Identity> identities = new ArrayList<Identity>(2);
    identities.add(demoIdentity);
    where.owners(identities);
    
    String expectedWhere = "(soc:identity = 'demo123456' ) AND soc:isComment = 'false' AND (soc:isHidden = 'false' OR soc:isHidden Is NULL ) AND soc:lastUpdated > " + accessPoint + " ";
    String actualWhere =  where.build(filter);
    assertEquals(expectedWhere, actualWhere);
    
    //Order
    assertEquals(DIRECTION.ASC, filter.get(ActivityFilter.LAST_UPDATED_ORDERBY).getDirection());
    assertEquals(DIRECTION.ASC, filter.get(ActivityFilter.POSTED_TIME_ORDERBY).getDirection());
  }
  
  public void testUserSpaceOlderOwner() throws Exception {
    ActivityFilter filter = ActivityFilter.spaceOlder();
    ActivityBuilderWhere where = ActivityBuilderWhere.userSpaces();

    long accessPoint = Calendar.getInstance().getTime().getTime();
    
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.OLDER.from(accessPoint));
    
    //
    List<Identity> identities = new ArrayList<Identity>(2);
    identities.add(demoIdentity);
    where.owners(identities);
    
    String expectedWhere = "(soc:identity = 'demo123456' ) AND soc:isComment = 'false' AND (soc:isHidden = 'false' OR soc:isHidden Is NULL ) AND soc:lastUpdated < " + accessPoint + " ";
    String actualWhere =  where.build(filter);
    assertEquals(expectedWhere, actualWhere);
    
    //Order
    assertEquals(DIRECTION.DESC, filter.get(ActivityFilter.LAST_UPDATED_ORDERBY).getDirection());
    assertEquals(DIRECTION.DESC, filter.get(ActivityFilter.POSTED_TIME_ORDERBY).getDirection());
  }
  
  public void testConnectionNewerOwners() throws Exception {
    ActivityFilter filter = ActivityFilter.newer();
    ActivityBuilderWhere where = ActivityBuilderWhere.simple();

    long accessPoint = Calendar.getInstance().getTime().getTime();
    
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.NEWER.from(accessPoint));
    
    //
    List<Identity> identities = new ArrayList<Identity>(2);
    identities.add(demoIdentity);
    identities.add(rootIdentity);
    where.owners(identities);
    
    String expectedWhere = "(soc:identity = 'demo123456' OR soc:identity = 'root123456' ) AND soc:isComment = 'false' AND (soc:isHidden = 'false' OR soc:isHidden Is NULL ) AND soc:lastUpdated > " + accessPoint + " ";
    String actualWhere =  where.build(filter);
    assertEquals(expectedWhere, actualWhere);
    
    //Order
    assertEquals(DIRECTION.ASC, filter.get(ActivityFilter.POSTED_TIME_ORDERBY).getDirection());
  }
  
  public void testConnectionOlderOwners() throws Exception {
    ActivityFilter filter = ActivityFilter.older();
    ActivityBuilderWhere where = ActivityBuilderWhere.simple();

    long accessPoint = Calendar.getInstance().getTime().getTime();
    
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.OLDER.from(accessPoint));
    
    //
    List<Identity> identities = new ArrayList<Identity>(2);
    identities.add(demoIdentity);
    identities.add(rootIdentity);
    where.owners(identities);
    
    String expectedWhere = "(soc:identity = 'demo123456' OR soc:identity = 'root123456' ) AND soc:isComment = 'false' AND (soc:isHidden = 'false' OR soc:isHidden Is NULL ) AND soc:lastUpdated < " + accessPoint + " ";
    String actualWhere =  where.build(filter);
    assertEquals(expectedWhere, actualWhere);
    
    //Order
    assertEquals(DIRECTION.DESC, filter.get(ActivityFilter.POSTED_TIME_ORDERBY).getDirection());
  }

}
