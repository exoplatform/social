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
package org.exoplatform.social.core.jpa.storage;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.application.SpaceActivityPublisher;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.test.AbstractCoreTest;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.model.Space.UpdatedField;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;

import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * Unit Tests for {@link SpaceActivityPublisher}
 * @author hoat_le
 */
public class SpaceActivityRDBMSPublisherTest extends AbstractCoreTest {
  private final Log LOG = ExoLogger.getLogger(SpaceActivityRDBMSPublisherTest.class);
  private IdentityStorage identityStorage;
  private SpaceStorage spaceStorage;
  private SpaceActivityPublisher spaceActivityPublisher;
  private RelationshipManager relationshipManager;

  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    spaceStorage = getService(SpaceStorage.class);
    spaceActivityPublisher = getService(SpaceActivityPublisher.class);
    identityStorage = getService(IdentityStorage.class);
    relationshipManager = getService(RelationshipManager.class);
    assertNotNull(spaceStorage);
    assertNotNull(spaceActivityPublisher);
    assertNotNull(identityStorage);

    rootIdentity = createIdentity("root");
    johnIdentity = createIdentity("john");
    maryIdentity = createIdentity("mary");
    demoIdentity = createIdentity("demo");
  }

  /**
   *
   * @throws Exception
   */
  public void testSpaceCreation() throws Exception {
    Space space = new Space();
    space.setDisplayName("Toto");
    space.setPrettyName(space.getDisplayName());
    space.setGroupId("/platform/users");
    space.setVisibility(Space.PRIVATE);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setRegistration(Space.OPEN);
    String[] managers = new String[] {"root"};
    String[] members = new String[] {"root"};
    space.setManagers(managers);
    space.setMembers(members);
    spaceService.saveSpace(space, true);
    assertNotNull("space.getId() must not be null", space.getId());
    SpaceLifeCycleEvent event  = new SpaceLifeCycleEvent(space, rootIdentity.getRemoteId(), SpaceLifeCycleEvent.Type.SPACE_CREATED);
    spaceActivityPublisher.spaceCreated(event);

    Identity identity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), true);
    RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getActivitiesOfSpaceWithListAccess(identity);
    assertEquals(1, listAccess.getSize());
    List<ExoSocialActivity> activities = listAccess.loadAsList(0, 10);
    
    listAccess = activityManager.getActivityFeedWithListAccess(rootIdentity);
    
    List<ExoSocialActivity> feed = listAccess.loadAsList(0, 10);
    assertEquals(1, feed.size());
    assertEquals(1, listAccess.getSize());
    
    Relationship relationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    relationshipManager.confirm(demoIdentity, rootIdentity);
    
    listAccess = activityManager.getActivityFeedWithListAccess(demoIdentity);
    assertEquals(0, listAccess.loadAsList(0, 10).size());
    assertEquals(0, listAccess.getSize());

    ActivityStream activityStream = activities.get(0).getActivityStream();

    assertNotNull("activityStream.getId() must not be null", activityStream.getId());

    assertEquals("activityStream.getPrettyId() must return: " + space.getPrettyName(), space.getPrettyName(), activityStream.getPrettyId());
    assertEquals(ActivityStream.Type.SPACE, activityStream.getType());

    assertEquals(SpaceIdentityProvider.NAME, activityStream.getType().toString());
  }
  
  /**
  *
  * @throws Exception
  */
 public void testSpaceUpdated() throws Exception {

   Space space = new Space();
   space.setDisplayName("Toto");
   space.setPrettyName(space.getDisplayName());
   space.setGroupId("/platform/users");
   space.setVisibility(Space.PRIVATE);
   space.setRegistration(Space.OPEN);
   space.setType(DefaultSpaceApplicationHandler.NAME);
   String[] managers = new String[] {"root"};
   String[] members = new String[] {"root"};
   space.setManagers(managers);
   space.setMembers(members);
   spaceService.saveSpace(space, true);
   assertNotNull("space.getId() must not be null", space.getId());
   SpaceLifeCycleEvent event  = new SpaceLifeCycleEvent(space, rootIdentity.getRemoteId(), SpaceLifeCycleEvent.Type.SPACE_CREATED);
   spaceActivityPublisher.spaceCreated(event);

   Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
   String activityId = identityStorage.getProfileActivityId(spaceIdentity.getProfile(), Profile.AttachedActivityType.SPACE);
   ExoSocialActivity activity = activityManager.getActivity(activityId);
   List<ExoSocialActivity> comments = activityManager.getCommentsWithListAccess(activity).loadAsList(0, 20);
   //Number of comments must be 1
   assertEquals(1, comments.size());
   assertEquals("Has joined the space.", comments.get(0).getTitle());
   
   //Rename space
   space.setEditor(rootIdentity.getRemoteId());
   spaceService.renameSpace(space, "Social");
   activity = activityManager.getActivity(activityId);
   assertEquals(2, activityManager.getCommentsWithListAccess(activity).getSize());
   comments = activityManager.getCommentsWithListAccess(activity).loadAsList(0, 20);
   assertEquals("Name has been updated to: "+space.getDisplayName(), comments.get(1).getTitle());
   
   //Update space's description
   space.setDescription("social's team");
   space.setField(UpdatedField.DESCRIPTION);
   spaceService.updateSpace(space);
   comments = activityManager.getCommentsWithListAccess(activity).loadAsList(0, 20);
   assertEquals(3, comments.size());
   assertEquals("Description has been updated to: social&#39;s team", comments.get(2).getTitle());
   
   //update avatar
   AvatarAttachment avatar = new AvatarAttachment();
   avatar.setMimeType("plain/text");
   avatar.setInputStream(new ByteArrayInputStream("Attachment content".getBytes()));
   space.setAvatarAttachment(avatar);
   spaceService.updateSpaceAvatar(space);
   comments = activityManager.getCommentsWithListAccess(activity).loadAsList(0, 20);
   assertEquals(4, comments.size());
   assertEquals("Space has a new avatar.", comments.get(3).getTitle());

   // delete this activity
   //Noted: Removed the activity here to make the exception in rdbms.
   //[main] ERROR org.exoplatform.commons.persistence.impl.ExoTransactionalAspect - Error while processing transactional method.
   //java.lang.NullPointerException
   activityManager.deleteActivity(activityId);
   assertEquals(0, activityManager.getActivitiesWithListAccess(spaceIdentity).getSize());
   
   space.setField(null);
   spaceService.renameSpace(space, "SocialTeam");
   activityId = identityStorage.getProfileActivityId(spaceIdentity.getProfile(), Profile.AttachedActivityType.SPACE);
   ExoSocialActivity newActivity = activityManager.getActivity(activityId);
   //Number of comments must be 1
   assertEquals(1, activityManager.getCommentsWithListAccess(newActivity).getSize());
   
   { // test case for grant or revoke manage role of users
     String[] spaceManagers = new String[] {"root"};
     String[] spaceMembers = new String[] {"demo"};
     space.setField(null);
     space.setManagers(spaceManagers);
     space.setMembers(spaceMembers);
     space.setEditor("root");
     
     spaceService.setManager(space, "demo", true);
     
     comments = activityManager.getCommentsWithListAccess(newActivity).loadAsList(0, 20);
     
     assertEquals(2, activityManager.getCommentsWithListAccess(newActivity).getSize());
     assertEquals(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", false).getId(), comments.get(1).getUserId());
     
     //
     spaceService.setManager(space, "demo", false);
     
     comments = activityManager.getCommentsWithListAccess(newActivity).loadAsList(0, 20);
     
     assertEquals(3, activityManager.getCommentsWithListAccess(newActivity).getSize());
     assertEquals(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", false).getId(), comments.get(2).getUserId());
   }
   
   {// update both name and description
     assertEquals("social's team", space.getDescription());
     
     String newDescription = "new description";
     space.setDescription(newDescription);
     space.setField(UpdatedField.DESCRIPTION);
     String newDisplayName = "newSpaceName";
     spaceService.renameSpace(space, newDisplayName);
     comments = activityManager.getCommentsWithListAccess(newActivity).loadAsList(0, 20);
     assertEquals(5, comments.size());
     assertEquals("Name has been updated to: " + space.getDisplayName(), comments.get(3).getTitle());
     assertEquals("Description has been updated to: " + space.getDescription(), comments.get(4).getTitle());
   }
   
   {
     assertEquals("new description", space.getDescription());
     
     space.setDescription("Cet espace est à chercher des bugs");
     space.setField(UpdatedField.DESCRIPTION);
     spaceService.updateSpace(space);
     comments = activityManager.getCommentsWithListAccess(newActivity).loadAsList(0, 20);
     assertEquals(6, comments.size());
   }
 }
 public void testSpaceHidden() throws Exception {
   //Create a hidden space
   Space space = new Space();
   space.setDisplayName("Toto");
   space.setPrettyName(space.getDisplayName());
   space.setGroupId("/platform/users");
   space.setVisibility(Space.HIDDEN);
   space.setRegistration(Space.OPEN);
   String[] managers = new String[] {"root"};
   String[] members = new String[] {"root"};
   space.setManagers(managers);
   space.setMembers(members);
   spaceService.saveSpace(space, true);
   //broadcast event
   SpaceLifeCycleEvent event  = new SpaceLifeCycleEvent(space, rootIdentity.getRemoteId(), SpaceLifeCycleEvent.Type.SPACE_CREATED);
   spaceActivityPublisher.spaceCreated(event);

   Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
   ListAccess<ExoSocialActivity> spaceActivities = activityManager.getActivitiesOfSpaceWithListAccess(spaceIdentity);
   ListAccess<ExoSocialActivity> userFeedActivities = activityManager.getActivityFeedWithListAccess(rootIdentity);
   
   assertEquals(0, userFeedActivities.getSize());
   assertEquals(0, spaceActivities.getSize());
   
   //Set space's visibility to PRIVATE
   space.setVisibility(Space.PRIVATE);
   spaceService.saveSpace(space, false);
   
   spaceActivities = activityManager.getActivitiesOfSpaceWithListAccess(spaceIdentity);
   userFeedActivities = activityManager.getActivityFeedWithListAccess(rootIdentity);
   
   //Check space activity stream
   assertEquals(1, spaceActivities.getSize());
   assertEquals(1, spaceActivities.load(0, 10).length);
   
   //Check user feed activity stream
   assertEquals(1, userFeedActivities.getSize());
   assertEquals(1, userFeedActivities.load(0, 10).length);
   
   //Set space's visibility to PRIVATE
   space.setVisibility(Space.HIDDEN);
   spaceService.saveSpace(space, false);
   
   spaceActivities = activityManager.getActivitiesOfSpaceWithListAccess(spaceIdentity);
   userFeedActivities = activityManager.getActivityFeedWithListAccess(rootIdentity);
   
   //Check space activity stream
   assertEquals(0, spaceActivities.getSize());
   assertEquals(0, spaceActivities.load(0, 10).length);

   //Check user feed activity stream
   assertEquals(0, userFeedActivities.getSize());
   assertEquals(0, userFeedActivities.load(0, 10).length);
 }

}
