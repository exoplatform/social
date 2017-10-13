/*
 * Copyright (C) 2003-2016 eXo Platform SAS.
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

import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.storage.entity.*;
import org.exoplatform.social.core.jpa.test.BaseCoreTest;

import java.util.*;

public class SpaceDAOTest extends BaseCoreTest {
  private SpaceDAO spaceDAO;
  private ActivityDAO activityDao;
  private IdentityDAO identityDAO;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    spaceDAO = getService(SpaceDAO.class);
    activityDao = getService(ActivityDAO.class);
    identityDAO = getService(IdentityDAO.class);
  }

  @Override
  public void tearDown() throws Exception {
    spaceDAO.deleteAll();
    super.tearDown();
  }

  public void testSaveSpace() throws Exception {
    SpaceEntity spaceEntity = createSpace("testPrettyName");

    spaceDAO.create(spaceEntity);

    end();
    begin();

    SpaceEntity result = spaceDAO.find(spaceEntity.getId());
    assertSpace(spaceEntity, result);
  }

  public void testGetSpace() throws Exception {
    SpaceEntity spaceEntity = createSpace("testPrettyName");

    spaceDAO.create(spaceEntity);

    end();
    begin();

    SpaceEntity result = spaceDAO.getSpaceByDisplayName(spaceEntity.getDisplayName());
    assertSpace(spaceEntity, result);
    
    result = spaceDAO.getSpaceByGroupId(spaceEntity.getGroupId());
    assertSpace(spaceEntity, result);
    
    result = spaceDAO.getSpaceByPrettyName(spaceEntity.getPrettyName());
    assertSpace(spaceEntity, result);
    
    result = spaceDAO.getSpaceByURL(spaceEntity.getUrl());
    assertSpace(spaceEntity, result);
  }
  
  public void testGetLastSpace() throws Exception {
    SpaceEntity space2 = createSpace("testPrettyName2");
    spaceDAO.create(space2);

    end();
    begin();
    
    List<SpaceEntity> result = spaceDAO.getLastSpaces(1);
    assertEquals(1, result.size());
    assertSpace(space2, result.iterator().next());
  }

  private SpaceEntity createSpace(String spacePrettyName) {
    SpaceEntity spaceEntity = new SpaceEntity();    
    spaceEntity.setApp(createApp());
    spaceEntity.setAvatarLastUpdated(new Date());
    spaceEntity.setDescription("testDesc");
    spaceEntity.setDisplayName("testDisplayName");
    spaceEntity.setGroupId("testGroupId");
    spaceEntity.setPrettyName(spacePrettyName);
    spaceEntity.setPriority(SpaceEntity.PRIORITY.HIGH);
    spaceEntity.setRegistration(SpaceEntity.REGISTRATION.OPEN);
    spaceEntity.setUrl("testUrl");
    spaceEntity.setVisibility(SpaceEntity.VISIBILITY.PRIVATE);
    spaceEntity.setBannerLastUpdated(new Date());

    addMember(spaceEntity, "root", SpaceMemberEntity.Status.PENDING);
    return spaceEntity;
  }

  private void addMember(SpaceEntity spaceEntity, String username, SpaceMemberEntity.Status status) {
    SpaceMemberEntity mem = new SpaceMemberEntity();
    mem.setSpace(spaceEntity);
    mem.setStatus(status);
    mem.setUserId(username);
    spaceEntity.getMembers().add(mem);
  }

  private Set<AppEntity> createApp() {
    Set<AppEntity> apps = new HashSet<>();
    AppEntity app = new AppEntity();
    app.setAppId("appId");
    app.setAppName("appName");
    app.setRemovable(true);
    app.setStatus(AppEntity.Status.ACTIVE);
    apps.add(app);
    return apps;
  }

  private void assertSpace(SpaceEntity spaceEntity, SpaceEntity result) {
    assertNotNull(result);
    assertEquals(spaceEntity.getPrettyName(), result.getPrettyName());
    assertEquals(1, result.getApp().size());
    AppEntity appEx = spaceEntity.getApp().iterator().next();
    AppEntity app = result.getApp().iterator().next();
    assertEquals(appEx, app);
    assertEquals(appEx.isRemovable(), app.isRemovable());
    assertEquals(appEx.getStatus(), app.getStatus());
    assertEquals(spaceEntity.getDescription(), result.getDescription());
    assertEquals(spaceEntity.getDisplayName(), result.getDisplayName());
    assertEquals(spaceEntity.getGroupId(), result.getGroupId());
    assertEquals(spaceEntity.getPriority(), result.getPriority());
    assertEquals(spaceEntity.getRegistration(), result.getRegistration());
    assertEquals(spaceEntity.getUrl(), result.getUrl());
    assertEquals(spaceEntity.getUrl(), result.getUrl());
    assertEquals(spaceEntity.getVisibility(), result.getVisibility());
    assertEquals(spaceEntity.getAvatarLastUpdated(), result.getAvatarLastUpdated());
    assertEquals(spaceEntity.getBannerLastUpdated(), result.getBannerLastUpdated());
    assertEquals(spaceEntity.getCreatedDate(), result.getCreatedDate());
    assertEquals(1, result.getMembers().size());
  }

  public void testSaveActivity() throws Exception {
    String activityTitle = "activity title";
    String commentTitle = "Comment title";

    SpaceEntity spaceEntity = createSpace("testPrettyName3");
    spaceEntity = spaceDAO.create(spaceEntity);
    addMember(spaceEntity, maryIdentity.getRemoteId(), SpaceMemberEntity.Status.MEMBER);
    addMember(spaceEntity, johnIdentity.getRemoteId(), SpaceMemberEntity.Status.MEMBER);
    addMember(spaceEntity, demoIdentity.getRemoteId(), SpaceMemberEntity.Status.MEMBER);

    IdentityEntity spaceIdentity = identityDAO.create(createIdentity(SpaceIdentityProvider.NAME, spaceEntity.getPrettyName()));


    end();
    begin();

    Set<Long> activityIds = new HashSet<Long>();
    for (int i = 0; i < 15; i++) {
      ActivityEntity activity = createActivityInstance(activityTitle, maryIdentity.getId());
      activity = createActivity(spaceIdentity, activity);
      Long activityId = activity.getId();
      activityIds.add(activityId);
      ActivityEntity got = activityDao.find(activityId);
      assertNotNull(got);
      addCommentsLikersMentionners(activity, commentTitle);
      activity = activityDao.update(activity);
    }

    end();
    begin();

    spaceEntity = spaceDAO.find(spaceEntity.getId());
    spaceDAO.delete(spaceEntity);
    spaceIdentity = identityDAO.find(spaceIdentity.getId());
    identityDAO.delete(spaceIdentity);
    activityDao.deleteActivitiesByOwnerId(String.valueOf(spaceIdentity.getId()));
  }

  private void addCommentsLikersMentionners(ActivityEntity activity, String commentTitle) {
    commentOnActivity(activity, commentTitle, demoIdentity.getId());
    commentOnActivity(activity, commentTitle, johnIdentity.getId());

    activity.addLiker(demoIdentity.getId());
    activity.addLiker(johnIdentity.getId());
    activity.addLiker(maryIdentity.getId());

    Set<String> mentionners = new HashSet<String>();
    mentionners.add(demoIdentity.getId());
    mentionners.add(johnIdentity.getId());
    mentionners.add(maryIdentity.getId());
    activity.setMentionerIds(mentionners);
  }

  private void commentOnActivity(ActivityEntity activity, String commentTitle, String identityId) {
    ActivityEntity comment = new ActivityEntity();
    comment.setTitle(commentTitle);
    comment.setOwnerId(identityId);
    comment.setPosterId(identityId);
    activity.addComment(comment);
    activityDao.create(comment);
  }

  private ActivityEntity createActivityInstance(String activityTitle, String posterId) {
    ActivityEntity activity = new ActivityEntity();
    // test for reserving order of map values for i18n activity
    Map<String, String> templateParams = new LinkedHashMap<String, String>();
    templateParams.put("key1", "value 1");
    templateParams.put("key2", "value 2");
    templateParams.put("key3", "value 3");
    activity.setTemplateParams(templateParams);
    activity.setTitle(activityTitle);
    activity.setBody("The body of " + activityTitle);
    activity.setPosterId(posterId);
    activity.setType("DEFAULT_ACTIVITY");

    //
    activity.setHidden(false);
    activity.setLocked(false);
    //
    return activity;
  }

  private ActivityEntity createActivity(IdentityEntity ownerIdentity, ActivityEntity activity) {
    activity.setOwnerId(String.valueOf(ownerIdentity.getId()));
    activity.setPosterId(activity.getOwnerId());
    activity = activityDao.create(activity);
    return activity;
  }

  private IdentityEntity createIdentity(String providerId, String remoteId) {
    IdentityEntity entity = new IdentityEntity();
    entity.setProviderId(providerId);
    entity.setRemoteId(remoteId);
    entity.setEnabled(true);
    entity.setDeleted(false);
    return entity;
  }
}
