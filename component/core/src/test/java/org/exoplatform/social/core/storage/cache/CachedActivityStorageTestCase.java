/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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

package org.exoplatform.social.core.storage.cache;

import org.apache.commons.lang.ArrayUtils;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.storage.impl.IdentityStorageImpl;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.social.core.test.MaxQueryNumber;
import org.exoplatform.social.core.test.QueryNumberTest;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
@QueryNumberTest
public class CachedActivityStorageTestCase extends AbstractCoreTest {

  private CachedActivityStorage activityStorage;
  private CachedRelationshipStorage relationshipStorage;
  private IdentityStorageImpl identityStorage;
  private SocialStorageCacheService cacheService;

  private Identity identity;
  private Identity identity2;

  private List<String> tearDownIdentityList;

  @Override
  protected void setUp() throws Exception {
    
    super.setUp();

    //
    activityStorage = (CachedActivityStorage) getContainer().getComponentInstanceOfType(CachedActivityStorage.class);
    relationshipStorage = (CachedRelationshipStorage) getContainer().getComponentInstanceOfType(CachedRelationshipStorage.class);
    identityStorage = (IdentityStorageImpl) getContainer().getComponentInstanceOfType(IdentityStorageImpl.class);
    cacheService = (SocialStorageCacheService) getContainer().getComponentInstanceOfType(SocialStorageCacheService.class);

    //
    cacheService.getActivitiesCache().clearCache();
    cacheService.getActivitiesCountCache().clearCache();
    cacheService.getActivityCache().clearCache();

    //
    identity = new Identity(OrganizationIdentityProvider.NAME, "mary");
    identityStorage.saveIdentity(identity);
    identity2 = new Identity(OrganizationIdentityProvider.NAME, "demo");
    identityStorage.saveIdentity(identity2);

    //
    tearDownIdentityList = new ArrayList<String>();
    tearDownIdentityList.add(identity.getId());
    tearDownIdentityList.add(identity2.getId());

  }

  @Override
  protected void tearDown() throws Exception {

    for (String id : tearDownIdentityList) {
      identityStorage.deleteIdentity(new Identity(id));
    }
    
    super.tearDown();

  }

  @MaxQueryNumber(300)
  public void testSaveActivity() throws Exception {

    //
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("hello");
    activity.setUserId(identity.getId());
    activityStorage.saveActivity(identity, activity);

    //
    assertEquals(1, cacheService.getActivityCache().getCacheSize());
    assertEquals(0, cacheService.getActivitiesCache().getCacheSize());

    //
    activityStorage.getActivityFeed(identity, 0, 20);

    //
    assertEquals(1, cacheService.getActivityCache().getCacheSize());
    assertEquals(1, cacheService.getActivitiesCache().getCacheSize());

    //
    ExoSocialActivity activity2 = new ExoSocialActivityImpl();
    activity2.setTitle("hello 2");
    activity2.setUserId(identity.getId());
    activityStorage.saveActivity(identity, activity2);

    //
    assertEquals(2, cacheService.getActivityCache().getCacheSize());
    assertEquals(0, cacheService.getActivitiesCache().getCacheSize());

  }

  @MaxQueryNumber(384)
  public void testRemoveActivity() throws Exception {

    //
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("hello");
    activity.setUserId(identity.getId());
    activityStorage.saveActivity(identity, activity);

    //
    assertEquals(1, cacheService.getActivityCache().getCacheSize());
    assertEquals(0, cacheService.getActivitiesCache().getCacheSize());

    //
    activityStorage.getActivityFeed(identity, 0, 20);

    //
    assertEquals(1, cacheService.getActivityCache().getCacheSize());
    assertEquals(1, cacheService.getActivitiesCache().getCacheSize());

    //
    activityStorage.deleteActivity(activity.getId());

    //
    assertEquals(0, cacheService.getActivityCache().getCacheSize());
    assertEquals(0, cacheService.getActivitiesCache().getCacheSize());


  }

  @MaxQueryNumber(495)
  public void testRelationshipActivity() throws Exception {

    //
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("hello on 1");
    activity.setUserId(identity.getId());
    activityStorage.saveActivity(identity, activity);

    ExoSocialActivity activity2 = new ExoSocialActivityImpl();
    activity2.setTitle("hello on 2");
    activity2.setUserId(identity2.getId());
    activityStorage.saveActivity(identity2, activity2);

    //
    activityStorage.getActivityFeed(identity, 0, 20);
    assertEquals(1, cacheService.getActivitiesCache().getCacheSize());

    Relationship relationship = new Relationship(identity, identity2, Relationship.Type.CONFIRMED);
    relationshipStorage.saveRelationship(relationship);
    assertEquals(0, cacheService.getActivitiesCache().getCacheSize());

    //
    activityStorage.getActivityFeed(identity, 0, 20);
    assertEquals(1, cacheService.getActivitiesCache().getCacheSize());

    //
    relationshipStorage.removeRelationship(relationship);
    assertEquals(0, cacheService.getActivitiesCache().getCacheSize());

  }

  @MaxQueryNumber(300)
  public void testSaveComment() throws Exception {

    //
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("hello");
    activity.setUserId(identity.getId());
    activityStorage.saveActivity(identity, activity);

    //
    assertEquals(1, cacheService.getActivityCache().getCacheSize());
    assertEquals(0, cacheService.getActivitiesCache().getCacheSize());

    //
    activityStorage.getActivityFeed(identity, 0, 20);

    //
    assertEquals(1, cacheService.getActivityCache().getCacheSize());
    assertEquals(1, cacheService.getActivitiesCache().getCacheSize());

    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle("comment");
    comment.setUserId(identity.getId());
    activityStorage.saveComment(activity, comment);

    //
    assertEquals(2, cacheService.getActivityCache().getCacheSize());
    assertEquals(0, cacheService.getActivitiesCache().getCacheSize());
    assertEquals(activity.getId(), activityStorage.getActivityFeed(identity, 0, 20).get(0).getId());
    assertEquals(comment.getId(), activityStorage.getActivityFeed(identity, 0, 20).get(0).getReplyToId()[0]);

  }

  @MaxQueryNumber(360)
  public void testRemoveComment() throws Exception {
    //
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("hello");
    activity.setUserId(identity.getId());
    activityStorage.saveActivity(identity, activity);

    //
    assertEquals(1, cacheService.getActivityCache().getCacheSize());
    assertEquals(0, cacheService.getActivitiesCache().getCacheSize());

    //
    activityStorage.getActivityFeed(identity, 0, 20);

    //
    assertEquals(1, cacheService.getActivityCache().getCacheSize());
    assertEquals(1, cacheService.getActivitiesCache().getCacheSize());

    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle("comment");
    comment.setUserId(identity.getId());
    activityStorage.saveComment(activity, comment);

    //
    assertEquals(2, cacheService.getActivityCache().getCacheSize());
    assertEquals(0, cacheService.getActivitiesCache().getCacheSize());
    assertEquals(activity.getId(), activityStorage.getActivityFeed(identity, 0, 20).get(0).getId());
    assertEquals(comment.getId(), activityStorage.getActivityFeed(identity, 0, 20).get(0).getReplyToId()[0]);

    //
    activityStorage.deleteComment(activity.getId(), comment.getId());

    //
    assertEquals(0, cacheService.getActivityCache().getCacheSize());
    assertEquals(0, cacheService.getActivitiesCache().getCacheSize());
    assertEquals(activity.getId(), activityStorage.getActivityFeed(identity, 0, 20).get(0).getId());
    assertEquals(0, activityStorage.getActivityFeed(identity, 0, 20).get(0).getReplyToId().length);

  }
  
  @MaxQueryNumber(926)
  public void testUpdateActivity() throws Exception {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("hello");
    activity.setUserId(identity.getId());
    activityStorage.saveActivity(identity, activity);
    
    //
    assertEquals(1, cacheService.getActivityCache().getCacheSize());
    assertEquals(0, cacheService.getActivitiesCache().getCacheSize());

    //
    activityStorage.getActivityFeed(identity, 0, 20);

    //
    assertEquals(1, cacheService.getActivityCache().getCacheSize());
    assertEquals(1, cacheService.getActivitiesCache().getCacheSize());
    
    //
    List<ExoSocialActivity> idActivities = activityStorage.getUserActivities(identity, 0, 5);
    assertEquals(1, idActivities.size());
    
    List<ExoSocialActivity> id2Activities = activityStorage.getUserActivities(identity2, 0, 5);
    assertEquals(0, id2Activities.size());
    
    // identity2 like activity of identity1
    ExoSocialActivity gotActivity = activityStorage.getUserActivities(identity, 0, 5).get(0);
    String[] likeIdentityIds = gotActivity.getLikeIdentityIds();
    likeIdentityIds = (String[]) ArrayUtils.add(likeIdentityIds, identity2.getId());
    gotActivity.setLikeIdentityIds(likeIdentityIds);
    activityStorage.updateActivity(gotActivity);
    
    assertEquals(0, cacheService.getActivityCache().getCacheSize());
    assertEquals(0, cacheService.getActivitiesCache().getCacheSize());
    
    id2Activities = activityStorage.getUserActivities(identity2, 0, 5);
    assertEquals(1, id2Activities.size());
    
    assertEquals(1, cacheService.getActivityCache().getCacheSize());
    assertEquals(1, cacheService.getActivitiesCache().getCacheSize());
    
    //
    activityStorage.deleteActivity(activity.getId());
  }

}
