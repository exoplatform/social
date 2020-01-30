/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
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
package org.exoplatform.social.core.jpa.storage.dao;

import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.storage.entity.StreamItemEntity;
import org.exoplatform.social.core.jpa.test.BaseCoreTest;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.IdentityStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Sep 17, 2015  
 */
public class StreamItemDAOTest extends BaseCoreTest {
  private List<ExoSocialActivity> tearDownActivityList;
  private Identity ghostIdentity;
  private Identity raulIdentity;
  private Identity jameIdentity;
  private Identity paulIdentity;
  
  private IdentityStorage identityStorage;
  private StreamItemDAO streamItemDAO;
  private SpaceService spaceService;

  @Override
  public void setUp() throws Exception {
    super.setUp();    
    
    identityStorage = getService(IdentityStorage.class);
    streamItemDAO = getService(StreamItemDAO.class);
    spaceService = getService(SpaceService.class);
    //
    tearDownActivityList = new ArrayList<ExoSocialActivity>();
    //
    ghostIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "ghost", true);
    raulIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "raul", true);
    jameIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "jame", true);
    paulIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "paul", true);

    getService(ActivityDAO.class).deleteAll();
  }

  @Override
  public void tearDown() throws Exception {
    for (ExoSocialActivity activity : tearDownActivityList) {
      activityStorage.deleteActivity(activity.getId());
    }
    
    //
    for (Space space : spaceService.getAllSpaces()) {
      Identity spaceIdentity = identityStorage.findIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());
      if (spaceIdentity != null) {
        identityStorage.deleteIdentity(spaceIdentity);
      }
      spaceService.deleteSpace(space);
    }

    identityManager.deleteIdentity(ghostIdentity);
    identityManager.deleteIdentity(jameIdentity);
    identityManager.deleteIdentity(raulIdentity);
    identityManager.deleteIdentity(paulIdentity);
    //
    // logout
    ConversationState.setCurrent(null);
    super.tearDown();
  }

  public void testPostActivity() {
    ExoSocialActivity activity = createActivity("post on my stream", demoIdentity.getId());
    activityStorage.saveActivity(demoIdentity, activity);
    tearDownActivityList.add(activity);
    List<StreamItemEntity> items = streamItemDAO.findStreamItemByActivityId(Long.valueOf(activity.getId()));
    assertEquals(1, items.size());
  }
  
  public void testGetFeedWithPostActivity() {
    ExoSocialActivity activity = createActivity("post on my stream", demoIdentity.getId());
    activityStorage.saveActivity(demoIdentity, activity);
    tearDownActivityList.add(activity);
    List<String> ids = activityStorage.getActivityIdsFeed(demoIdentity, 0, 10);
    assertEquals(1, ids.size());
  }
  
  public void testCommentOnHisActivity() {
    ExoSocialActivity activity = createActivity("post on my stream", demoIdentity.getId());
    activityStorage.saveActivity(demoIdentity, activity);
    tearDownActivityList.add(activity);
    
    ExoSocialActivity comment = createActivity("comment on his activity", demoIdentity.getId());
    activityStorage.saveComment(activity, comment);
    
    List<StreamItemEntity> items = streamItemDAO.findStreamItemByActivityId(Long.valueOf(activity.getId()));
    assertEquals(2, items.size());
  }
  
  public void testCommentOnOtherActivity() {
    ExoSocialActivity activity = createActivity("post on my stream", demoIdentity.getId());
    activityStorage.saveActivity(demoIdentity, activity);
    tearDownActivityList.add(activity);

    Relationship demoMaryConnection = relationshipManager.inviteToConnect(demoIdentity, maryIdentity);
    relationshipManager.confirm(demoIdentity, maryIdentity);
    
    ExoSocialActivity comment = createActivity("comment on demo's activity", maryIdentity.getId());
    activityStorage.saveComment(activity, comment);
    
    List<String> ids = activityStorage.getActivityIdsFeed(maryIdentity, 0, 10);
    assertEquals(1, ids.size());

    relationshipManager.delete(demoMaryConnection);
  }
  
  public void testGetFeedWithCommentOnOtherActivity() {
    ExoSocialActivity activity = createActivity("post on my stream", demoIdentity.getId());
    activityStorage.saveActivity(demoIdentity, activity);
    tearDownActivityList.add(activity);
    
    ExoSocialActivity comment = createActivity("comment on demo's activity", maryIdentity.getId());
    activityStorage.saveComment(activity, comment);
    
    List<StreamItemEntity> items = streamItemDAO.findStreamItemByActivityId(Long.valueOf(activity.getId()));
    assertEquals(2, items.size());
  }
  
  public void testDoubleCommentOnOtherActivity() {
    ExoSocialActivity activity = createActivity("post on my stream", demoIdentity.getId());
    activityStorage.saveActivity(demoIdentity, activity);
    tearDownActivityList.add(activity);
    
    ExoSocialActivity comment = createActivity("comment on demo's activity 1", maryIdentity.getId());
    activityStorage.saveComment(activity, comment);
    
    comment = createActivity("comment on demo's activity 2", maryIdentity.getId());
    activityStorage.saveComment(activity, comment);
    
    List<StreamItemEntity> items = streamItemDAO.findStreamItemByActivityId(Long.valueOf(activity.getId()));
    assertEquals(2, items.size());
  }
  
  public void testGetFeedWithDoubleCommentOnOtherActivity() {
    ExoSocialActivity activity = createActivity("post on my stream", demoIdentity.getId());
    activityStorage.saveActivity(demoIdentity, activity);
    tearDownActivityList.add(activity);

    Relationship demoMaryConnection = relationshipManager.inviteToConnect(demoIdentity, maryIdentity);
    relationshipManager.confirm(demoIdentity, maryIdentity);
    
    ExoSocialActivity comment = createActivity("comment on demo's activity 1", maryIdentity.getId());
    activityStorage.saveComment(activity, comment);
    
    comment = createActivity("comment on demo's activity 2", maryIdentity.getId());
    activityStorage.saveComment(activity, comment);
    
    List<String> ids = activityStorage.getActivityIdsFeed(maryIdentity, 0, 10);
    assertEquals(1, ids.size());

    relationshipManager.delete(demoMaryConnection);
  }
  
  public void testGetFeedWithConnections() throws Exception {
    ExoSocialActivity activity = createActivity("post on my stream", demoIdentity.getId());
    activityStorage.saveActivity(demoIdentity, activity);
    tearDownActivityList.add(activity);
    
    Relationship demoMaryConnection = relationshipManager.inviteToConnect(demoIdentity, maryIdentity);
    relationshipManager.confirm(demoIdentity, maryIdentity);
    
    List<String> ids = activityStorage.getActivityIdsFeed(maryIdentity, 0, 10);
    assertEquals(1, ids.size());
    relationshipManager.delete(demoMaryConnection);
  }
  
  public void testStreamsWithConnectionsAndMention() throws Exception {
    ExoSocialActivity activity = createActivity("post on my stream @mary", demoIdentity.getId());
    activityStorage.saveActivity(demoIdentity, activity);
    tearDownActivityList.add(activity);
    
    Relationship demoMaryConnection = relationshipManager.inviteToConnect(demoIdentity, maryIdentity);
    relationshipManager.confirm(demoIdentity, maryIdentity);
    
    List<String> ids = activityStorage.getActivityIdsFeed(maryIdentity, 0, 10);
    assertEquals(1, ids.size());
    
    ids = activityStorage.getActivityIdsOfConnections(maryIdentity, 0, 10);
    assertEquals(1, ids.size());
    
    ids = activityStorage.getUserIdsActivities(demoIdentity, 0, 10);
    assertEquals(1, ids.size());
    
    relationshipManager.delete(demoMaryConnection);
  }
  
  public void testCommentOnOtherActivityAndMention() {
    ExoSocialActivity activity = createActivity("post on my stream", demoIdentity.getId());
    activityStorage.saveActivity(demoIdentity, activity);
    tearDownActivityList.add(activity);
    
    ExoSocialActivity comment = createActivity("comment on demo's activity @demo", maryIdentity.getId());
    activityStorage.saveComment(activity, comment);
    
    List<StreamItemEntity> items = streamItemDAO.findStreamItemByActivityId(Long.valueOf(activity.getId()));
    assertEquals(3, items.size());
  }
  
  public void testCommentOnOtherActivityAndOtherMention() {
    ExoSocialActivity activity = createActivity("post on my stream", demoIdentity.getId());
    activityStorage.saveActivity(demoIdentity, activity);
    tearDownActivityList.add(activity);
    
    ExoSocialActivity comment = createActivity("comment on demo's activity @root", maryIdentity.getId());
    activityStorage.saveComment(activity, comment);
    
    List<StreamItemEntity> items = streamItemDAO.findStreamItemByActivityId(Long.valueOf(activity.getId()));
    assertEquals(3, items.size());
  }
  
  public void testStreamsWithCommentOnOtherActivityAndOtherMention() {
    ExoSocialActivity activity = createActivity("post on my stream @mary", demoIdentity.getId());
    activityStorage.saveActivity(demoIdentity, activity);
    tearDownActivityList.add(activity);

    Relationship demoMaryConnection = relationshipManager.inviteToConnect(demoIdentity, maryIdentity);
    relationshipManager.confirm(demoIdentity, maryIdentity);
    
    ExoSocialActivity comment = createActivity("comment on demo's activity", maryIdentity.getId());
    activityStorage.saveComment(activity, comment);
    
    List<String> ids = activityStorage.getActivityIdsFeed(maryIdentity, 0, 10);
    assertEquals(1, ids.size());

    relationshipManager.delete(demoMaryConnection);
  }
  
  public void testPostActivityAndMention() {
    ExoSocialActivity activity = createActivity("post activity and mention @mary", demoIdentity.getId());
    activityStorage.saveActivity(demoIdentity, activity);
    tearDownActivityList.add(activity);
    List<StreamItemEntity> items = streamItemDAO.findStreamItemByActivityId(Long.valueOf(activity.getId()));
    assertEquals(2, items.size());
  }
  
  public void testLikerActivity() {
    ExoSocialActivity activity = createActivity("post on stream and mary liker", demoIdentity.getId());
    activityStorage.saveActivity(demoIdentity, activity);
    tearDownActivityList.add(activity);
    activityManager.saveLike(activity, maryIdentity);
    List<StreamItemEntity> items = streamItemDAO.findStreamItemByActivityId(Long.valueOf(activity.getId()));
    assertEquals(2, items.size());
  }
  
  public void testDislikeActivity() {
    ExoSocialActivity activity = createActivity("post on stream and mary liker", demoIdentity.getId());
    activityStorage.saveActivity(demoIdentity, activity);
    tearDownActivityList.add(activity);
    activityManager.saveLike(activity, maryIdentity);
    activityManager.deleteLike(activity, maryIdentity);;
    List<StreamItemEntity> items = streamItemDAO.findStreamItemByActivityId(Long.valueOf(activity.getId()));
    assertEquals(1, items.size());
  }
  
  public void testPostOnViewerStream() {
    ExoSocialActivity activity = createActivity("demo post on mary's stream", demoIdentity.getId());
    activityStorage.saveActivity(maryIdentity, activity);
    tearDownActivityList.add(activity);
    List<StreamItemEntity> items = streamItemDAO.findStreamItemByActivityId(Long.valueOf(activity.getId()));
    assertEquals(2, items.size());
  }
  
  public void testPostOnSpace() throws Exception {
    Space space = this.getSpaceInstance(spaceService, 0);
    Identity spaceIdentity = this.identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    //
    ExoSocialActivity activity = createActivity("demo post on the space 1", demoIdentity.getId());
    activityStorage.saveActivity(spaceIdentity, activity);
    
    List<StreamItemEntity> items = streamItemDAO.findStreamItemByActivityId(Long.valueOf(activity.getId()));
    assertEquals(2, items.size());
    
    tearDownActivityList.add(activity);
  }

  public void testPostOnSpaceAndMention() throws Exception {
    Space space = this.getSpaceInstance(spaceService, 0);
    Identity spaceIdentity = this.identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    //
    ExoSocialActivity activity = createActivity("demo post on the space 1 and mention @mary", demoIdentity.getId());
    activityStorage.saveActivity(spaceIdentity, activity);
    
    List<StreamItemEntity> items = streamItemDAO.findStreamItemByActivityId(Long.valueOf(activity.getId()));
    assertEquals(3, items.size());
    
    tearDownActivityList.add(activity);
  }

  public void testDeleteActivity() {
    ExoSocialActivity activity = createActivity("post on stream and mary liker", demoIdentity.getId());
    activityStorage.saveActivity(demoIdentity, activity);
    activityManager.saveLike(activity, maryIdentity);
    activityManager.deleteActivity(activity);
    List<StreamItemEntity> items = streamItemDAO.findStreamItemByActivityId(Long.valueOf(activity.getId()));
    assertEquals(0, items.size());
  }

  public void testAddPlusTenActivitiesWithComment() {
    ExoSocialActivity activity = createActivity("Post on my stream", maryIdentity.getId());
    activityStorage.saveActivity(maryIdentity, activity);
    tearDownActivityList.add(activity);

    ExoSocialActivity socialActivity;
    for (int i=1; i<=11; i++) {
      socialActivity = createActivity("Post on my stream " + i, maryIdentity.getId());
      activityStorage.saveActivity(maryIdentity, socialActivity);
      tearDownActivityList.add(socialActivity);
    }

    // comment on first activity to make it appear at first position in activity stream
    ExoSocialActivity comment = createActivity("Comment on first activity", maryIdentity.getId());
    activityStorage.saveComment(activity, comment);

    List<String> ids;
    ids = activityStorage.getActivityIdsFeed(maryIdentity, 0, 10);
    assertEquals(10, ids.size());
    assertEquals("First activity should be at first position", activity.getId(), ids.get(0));

    ids = activityStorage.getActivityIdsFeed(maryIdentity, 10, 10);
    assertEquals(2, ids.size());

    //Test returned ids for My activities filter
    ids = activityStorage.getUserIdsActivities(maryIdentity,0,10);
    assertEquals(10, ids.size());
    ids = activityStorage.getUserIdsActivities(maryIdentity,10,10);
    assertEquals(2, ids.size());
  }

  public void testWithRelationAddPlusTenActivitiesWithComment() {
    ExoSocialActivity activity = createActivity("Post on my stream", maryIdentity.getId());
    activityStorage.saveActivity(maryIdentity, activity);
    tearDownActivityList.add(activity);

    Relationship demoMaryConnection = relationshipManager.inviteToConnect(demoIdentity, maryIdentity);
    relationshipManager.confirm(demoIdentity, maryIdentity);

    ExoSocialActivity socialActivity;
    for (int i=1; i<=11; i++) {
      socialActivity = createActivity("Post on my stream " + i, demoIdentity.getId());
      activityStorage.saveActivity(demoIdentity, socialActivity);
      tearDownActivityList.add(socialActivity);
    }

    // comment on first activity to make it appear at first position in activity stream
    ExoSocialActivity comment = createActivity("Comment on first activity", demoIdentity.getId());
    activityStorage.saveComment(activity, comment);

    List<String> ids;
    ids = activityStorage.getActivityIdsFeed(demoIdentity, 0, 10);
    assertEquals(10, ids.size());
    assertEquals("First activity should be at first position", activity.getId(), ids.get(0));

    ids = activityStorage.getActivityIdsFeed(demoIdentity, 10, 10);
    assertEquals(2, ids.size());
    
    //Make sure the count of all activities is right
    ids = activityStorage.getActivityIdsFeed(demoIdentity, 0, 20);
    assertEquals(12, ids.size());

    //Test returned ids for My activities filter
    ids = activityStorage.getUserIdsActivities(demoIdentity,0,10);
    assertEquals(10, ids.size());
    ids = activityStorage.getUserIdsActivities(demoIdentity,10,10);
    assertEquals(2, ids.size());

    relationshipManager.delete(demoMaryConnection);
  }

  public void testPostPlusTenActivitiesOnSpaceWithComment() throws Exception {
    Space space = this.getSpaceInstance(spaceService, 0);
    Identity spaceIdentity = this.identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    space.setMembers(new String[]{maryIdentity.getRemoteId(),demoIdentity.getRemoteId()});
    //
    ExoSocialActivity activity = createActivity("Post on the space", maryIdentity.getId());
    activityStorage.saveActivity(spaceIdentity, activity);
    tearDownActivityList.add(activity);

    ExoSocialActivity socialActivity;
    for (int i=1; i<=11; i++) {
      socialActivity = createActivity("Post on the space " + i, demoIdentity.getId());
      activityStorage.saveActivity(spaceIdentity, socialActivity);
      tearDownActivityList.add(socialActivity);
    }

    // comment on first activity to make it appear at first position in activity stream
    ExoSocialActivity comment = createActivity("Comment on first activity", demoIdentity.getId());
    activityStorage.saveComment(activity, comment);

    List<String> ids;
    ids = activityStorage.getActivityIdsFeed(maryIdentity, 0, 10);
    assertEquals(1, ids.size());
    assertEquals("First activity should be at first position since it is commented", activity.getId(), ids.get(0));

    ids = activityStorage.getActivityIdsFeed(demoIdentity, 0, 10);
    assertEquals(10, ids.size());
    ids = activityStorage.getActivityIdsFeed(demoIdentity, 10, 10);
    assertEquals(2, ids.size());

    //Test returned ids for My activities filter
    ids = activityStorage.getUserIdsActivities(maryIdentity,0,10);
    assertEquals(1, ids.size());

    //Test returned ids for a Space
    ids = activityStorage.getSpaceActivityIds(spaceIdentity,0,10);
    assertEquals(10, ids.size());
    ids = activityStorage.getSpaceActivityIds(spaceIdentity,10,10);
    assertEquals(2, ids.size());

    //Test returned ids for My Spaces filter with no relation
    ids = activityStorage.getUserSpacesActivityIds(demoIdentity,0,10);
    assertEquals(10, ids.size());
    ids = activityStorage.getUserSpacesActivityIds(demoIdentity,10,10);
    assertEquals(2, ids.size());

    space.setMembers(new String[]{demoIdentity.getRemoteId()});
  }

  public void testWithRelationPostPlusTenActivitiesOnSpaceWithComment() throws Exception {
    Space space = this.getSpaceInstance(spaceService, 0);
    Identity spaceIdentity = this.identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    space.setMembers(new String[]{maryIdentity.getRemoteId(),demoIdentity.getRemoteId()});
    //
    
    ExoSocialActivity activity = createActivity("Post on the space", maryIdentity.getId());
    activityStorage.saveActivity(spaceIdentity, activity);
    tearDownActivityList.add(activity);

    Relationship demoMaryConnection = relationshipManager.inviteToConnect(demoIdentity, maryIdentity);
    relationshipManager.confirm(demoIdentity, maryIdentity);

    ExoSocialActivity socialActivity;
    for (int i=1; i<=11; i++) {
      socialActivity = createActivity("Post on the space " + i, demoIdentity.getId());
      activityStorage.saveActivity(spaceIdentity, socialActivity);
      tearDownActivityList.add(socialActivity);
    }

    // comment on first activity to make it appear at first position in activity stream
    ExoSocialActivity comment = createActivity("Comment on first activity", demoIdentity.getId());
    activityStorage.saveComment(activity, comment);

    List<String> ids;
    ids = activityStorage.getActivityIdsFeed(demoIdentity, 0, 10);
    assertEquals(10, ids.size());
    assertEquals("First activity should be at first position since it is commented", activity.getId(), ids.get(0));

    ids = activityStorage.getActivityIdsFeed(demoIdentity, 10, 10);
    assertEquals(2, ids.size());

    //Make sure the count of all activities is right
    ids = activityStorage.getActivityIdsFeed(demoIdentity, 0, 20);
    assertEquals(12, ids.size());
    
    //Test returned ids for My activities filter
    ids = activityStorage.getUserIdsActivities(demoIdentity,0,10);
    assertEquals(10, ids.size());
    ids = activityStorage.getUserIdsActivities(demoIdentity,10,10);
    assertEquals(1, ids.size());

    //Test returned ids for a Space
    ids = activityStorage.getSpaceActivityIds(spaceIdentity,0,10);
    assertEquals(10, ids.size());
    ids = activityStorage.getSpaceActivityIds(spaceIdentity,10,10);
    assertEquals(2, ids.size());

    //Test returned ids for My Spaces filter
    ids = activityStorage.getUserSpacesActivityIds(demoIdentity,0,10);
    assertEquals(10, ids.size());
    ids = activityStorage.getUserSpacesActivityIds(demoIdentity,10,10);
    assertEquals(2, ids.size());

    relationshipManager.delete(demoMaryConnection);
    space.setMembers(new String[]{demoIdentity.getRemoteId()});
  }

  public void testWithRelationInteractOnActivities() {
    // Mary post activities to interact with by Demo
    ExoSocialActivity activityToComment = createActivity("Post on my stream", maryIdentity.getId());
    activityStorage.saveActivity(maryIdentity, activityToComment);
    tearDownActivityList.add(activityToComment);

    ExoSocialActivity activityToLike = createActivity("Post on my stream", maryIdentity.getId());
    activityStorage.saveActivity(maryIdentity, activityToLike);
    tearDownActivityList.add(activityToLike);

    ExoSocialActivity activityToMentionIn = createActivity("Post on my stream", maryIdentity.getId());
    activityStorage.saveActivity(maryIdentity, activityToMentionIn);
    tearDownActivityList.add(activityToMentionIn);

    Relationship demoMaryConnection = relationshipManager.inviteToConnect(demoIdentity, maryIdentity);
    relationshipManager.confirm(demoIdentity, maryIdentity);

    // comment on activity
    ExoSocialActivity comment = createActivity("Comment on first activity", demoIdentity.getId());
    activityStorage.saveComment(activityToComment, comment);

    // Like activity
    activityManager.saveLike(activityToLike, demoIdentity);
    
    // Mention in activity
    ExoSocialActivity MentionComment = createActivity("comment on demo's activity @mary", demoIdentity.getId());
    activityStorage.saveComment(activityToMentionIn, MentionComment);

    // Get Activities on Activity Stream for user demo
    List<String> ids;
    ids = activityStorage.getActivityIdsFeed(demoIdentity, 0, 10);
    assertEquals(3, ids.size());
    assertEquals("Last activity commented should be at first position", activityToMentionIn.getId(), ids.get(0));

    // Get Activities on Activity Stream for user mary
    ids = activityStorage.getActivityIdsFeed(maryIdentity, 0, 10);
    assertEquals(3, ids.size());
    assertEquals("Last activity commented should be at first position", activityToMentionIn.getId(), ids.get(0));

    //Make sure the count of all activities is right
    ids = activityStorage.getActivityIdsFeed(demoIdentity, 0, 10);
    assertEquals(3, ids.size());

    //Test returned ids for My activities filter of user demo
    ids = activityStorage.getUserIdsActivities(demoIdentity,0,10);
    assertEquals(3, ids.size());

    //Test returned ids for My activities filter of user mary
    ids = activityStorage.getUserIdsActivities(maryIdentity,0,10);
    assertEquals(3, ids.size());

    relationshipManager.delete(demoMaryConnection);
  }

  private Space getSpaceInstance(SpaceService spaceService, int number) throws Exception {
    Space space = new Space();
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/space" + number);
    space.setUrl(space.getPrettyName());
    String[] managers = new String[] {"demo"};
    String[] members = new String[] {"demo"};
    space.setManagers(managers);
    space.setMembers(members);
    spaceService.saveSpace(space, true);
    return space;
  }

  private ExoSocialActivity createActivity(String title, String posterId) {
    //
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(title);
    activity.setTitleId("TitleID");
    activity.setType("UserActivity");
    activity.setBody("Body of " + activity.getTitle());
    activity.setBodyId("BodyId of " + activity.getTitle());
    activity.setLikeIdentityIds(new String[] { "demo", "mary" });
    activity.setMentionedIds(new String[] { "demo", "john" });
    activity.setCommentedIds(new String[] {});
    activity.setReplyToId(new String[] {});
    activity.setAppId("AppID");
    activity.setExternalId("External ID");
    activity.setUserId(posterId);

    return activity;
  }
  
  

}
