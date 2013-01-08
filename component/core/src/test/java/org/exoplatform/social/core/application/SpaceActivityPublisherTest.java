/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.application;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.model.Space.UpdatedField;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.test.AbstractCoreTest;

/**
 * Unit Tests for {@link SpaceActivityPublisher}
 * @author hoat_le
 */
public class SpaceActivityPublisherTest extends  AbstractCoreTest {
  private final Log LOG = ExoLogger.getLogger(SpaceActivityPublisherTest.class);
  private ActivityManager activityManager;
  private IdentityManager identityManager;
  private IdentityStorage identityStorage;
  private SpaceService spaceService;
  private SpaceActivityPublisher spaceActivityPublisher;
  private List<ExoSocialActivity> tearDownActivityList;
  @Override
  public void setUp() throws Exception {
    super.setUp();
    tearDownActivityList = new ArrayList<ExoSocialActivity>();
    activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
    assertNotNull("activityManager must not be null", activityManager);
    identityManager =  (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    assertNotNull("identityManager must not be null", identityManager);
    spaceService = (SpaceService) getContainer().getComponentInstanceOfType(SpaceService.class);
    assertNotNull("spaceService must not be null", spaceService);
    spaceActivityPublisher = (SpaceActivityPublisher) getContainer().getComponentInstanceOfType(SpaceActivityPublisher.class);
    assertNotNull("spaceActivityPublisher must not be null", spaceActivityPublisher);
    identityStorage =  (IdentityStorage) getContainer().getComponentInstanceOfType(IdentityStorage.class);
    assertNotNull("identityStorage must not be null", identityStorage);
  }

  @Override
  public void tearDown() throws Exception {
    for (ExoSocialActivity activity : tearDownActivityList) {
      try {
        activityManager.deleteActivity(activity.getId());
      } catch (Exception e) {
        LOG.warn("can not delete activity with id: " + activity.getId());
      }
    }
    super.tearDown();
  }

  /**
   *
   * @throws Exception
   */
  public void testSpaceCreation() throws Exception {
    Identity rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");

    Space space = new Space();
    space.setDisplayName("Toto");
    space.setPrettyName(space.getDisplayName());
    space.setGroupId("/platform/users");
    spaceService.saveSpace(space, true);
    assertNotNull("space.getId() must not be null", space.getId());
    SpaceLifeCycleEvent event  = new SpaceLifeCycleEvent(space, rootIdentity.getRemoteId(), SpaceLifeCycleEvent.Type.SPACE_CREATED);
    spaceActivityPublisher.spaceCreated(event);

    Thread.sleep(3000);

    Identity identity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());
    List<ExoSocialActivity> activities = activityManager.getActivities(identity);
    assertEquals(1, activities.size());
    tearDownActivityList.add(activities.get(0));
    assertTrue(activities.get(0).getTitle().contains(space.getDisplayName()));
    assertTrue(activities.get(0).getTitle().contains("root"));

    ActivityStream activityStream = activities.get(0).getActivityStream();

    assertNotNull("activityStream.getId() must not be null", activityStream.getId());

    assertEquals("activityStream.getPrettyId() must return: " + space.getPrettyName(), space.getPrettyName(), activityStream.getPrettyId());
    assertEquals(ActivityStream.Type.SPACE, activityStream.getType());

    assertEquals(SpaceIdentityProvider.NAME, activityStream.getType().toString());

    //clean up
    spaceService.deleteSpace(space);
    identityManager.deleteIdentity(rootIdentity);
  }
  
  /**
  *
  * @throws Exception
  */
 public void testSpaceUpdated() throws Exception {
   Identity rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", false);
   Identity demoIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "demo", false);

   Space space = new Space();
   space.setDisplayName("Toto");
   space.setPrettyName(space.getDisplayName());
   space.setGroupId("/platform/users");
   spaceService.saveSpace(space, true);
   assertNotNull("space.getId() must not be null", space.getId());
   SpaceLifeCycleEvent event  = new SpaceLifeCycleEvent(space, rootIdentity.getRemoteId(), SpaceLifeCycleEvent.Type.SPACE_CREATED);
   spaceActivityPublisher.spaceCreated(event);

   Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
   String activityId = identityStorage.getProfileActivityId(spaceIdentity.getProfile(), Profile.AttachedActivityType.SPACE);
   ExoSocialActivity activity = activityManager.getActivity(activityId);
   //Number of comments must be 0
   assertEquals(0, activityManager.getCommentsWithListAccess(activity).getSize());
   space.setEditor(rootIdentity.getRemoteId());
   spaceService.renameSpace(space, "Social");
   activity = activityManager.getActivity(activityId);
   assertEquals(1, activityManager.getCommentsWithListAccess(activity).getSize());
   List<ExoSocialActivity> comments = activityManager.getCommentsWithListAccess(activity).loadAsList(0, 20);
   assertEquals("Name has been updated to: "+space.getDisplayName(), comments.get(0).getTitle());
   
   space.setDescription("social's team");
   space.setField(UpdatedField.DESCRIPTION);
   spaceService.updateSpace(space);
   comments = activityManager.getCommentsWithListAccess(activity).loadAsList(0, 20);
   assertEquals(2, comments.size());
   assertEquals("Description has been updated to: "+space.getDescription(), comments.get(1).getTitle());
   
   //update avatar
   AvatarAttachment avatar = new AvatarAttachment();
   avatar.setMimeType("plain/text");
   avatar.setInputStream(new ByteArrayInputStream("Attachment content".getBytes()));
   space.setAvatarAttachment(avatar);
   spaceService.updateSpaceAvatar(space);
   comments = activityManager.getCommentsWithListAccess(activity).loadAsList(0, 20);
   assertEquals(3, comments.size());
   assertEquals("Space has a new avatar.", comments.get(2).getTitle());

   // delete this activity
   activityManager.deleteActivity(activityId);
   assertEquals(0, activityManager.getActivitiesWithListAccess(rootIdentity).getSize());
   
   spaceService.renameSpace(space, "SocialTeam");
   activityId = identityStorage.getProfileActivityId(spaceIdentity.getProfile(), Profile.AttachedActivityType.SPACE);
   ExoSocialActivity newActivity = activityManager.getActivity(activityId);
   //Number of comments must be 0
   assertEquals(0, activityManager.getCommentsWithListAccess(newActivity).getSize());
   
   { // test case for grant or revoke manage role of users
     String[] spaceManagers = new String[] {"root"};
     String[] spaceMembers = new String[] {"demo"};
     space.setField(null);
     space.setManagers(spaceManagers);
     space.setMembers(spaceMembers);
     space.setEditor("root");
     
     spaceService.setManager(space, "demo", true);
     
     comments = activityManager.getCommentsWithListAccess(newActivity).loadAsList(0, 20);
     
     assertEquals(1, activityManager.getCommentsWithListAccess(newActivity).getSize());
     assertEquals("<a href=\"/portal/classic/profile/demo\">Demo gtn</a> has been promoted as space's manager.", comments.get(0).getTitle());
     assertEquals(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", false).getId(), comments.get(0).getUserId());
     
     //
     spaceService.setManager(space, "demo", false);
     
     comments = activityManager.getCommentsWithListAccess(newActivity).loadAsList(0, 20);
     
     assertEquals(2, activityManager.getCommentsWithListAccess(newActivity).getSize());
     assertEquals("<a href=\"/portal/classic/profile/demo\">Demo gtn</a> has been revoked as space's manager.", comments.get(1).getTitle());
     assertEquals(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", false).getId(), comments.get(1).getUserId());
   }
   //clean up
   spaceService.deleteSpace(space);
   identityManager.deleteIdentity(rootIdentity);
   
 }
}
