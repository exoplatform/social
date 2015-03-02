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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.test.AbstractCoreTest;

/**
 * Unit Tests for {@link ProfileUpdatesPublisher}
 *
 * @author hoat_le
 */
public class ProfileUpdatesPublisherTest extends AbstractCoreTest {
  private final Log LOG = ExoLogger.getLogger(ProfileUpdatesPublisher.class);
  private List<ExoSocialActivity> tearDownActivityList;
  private ActivityManager activityManager;
  private IdentityManager identityManager;
  private IdentityStorage identityStorage;
  private ProfileUpdatesPublisher publisher;
  private String userName = "root";
  private Identity rootIdentity;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    tearDownActivityList = new ArrayList<ExoSocialActivity>();
    activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
    assertNotNull("activityManager must not be null", activityManager);
    identityManager =  (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    assertNotNull("identityManager must not be null", identityManager);
    identityStorage =  (IdentityStorage) getContainer().getComponentInstanceOfType(IdentityStorage.class);
    assertNotNull("identityManager must not be null", identityStorage);
    publisher = (ProfileUpdatesPublisher) getContainer().getComponentInstanceOfType(ProfileUpdatesPublisher.class);
    assertNotNull("profileUpdatesPublisher must not be null", publisher);
    rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userName, true);
    assertNotNull("rootIdentity.getId() must not be null", rootIdentity.getId());
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
    identityManager.deleteIdentity(rootIdentity);
    super.tearDown();
  }

  public void testProfileUpdated() throws Exception {
    Profile profile = rootIdentity.getProfile();
    
    // update profile will be update on user profile properties also
    profile.setAttachedActivityType(Profile.AttachedActivityType.USER);
    
    //activityId must be null because it don't attach yet when we don't update profile
    assertNull(getActivityId(profile));
    
    //update the profile for the first time
    profile.setProperty(Profile.POSITION, "developer");
    profile.setListUpdateTypes(Arrays.asList(Profile.UpdateType.CONTACT));
    identityManager.updateProfile(profile);
    
    //from now, activity must not be null
    assertNotNull(getActivityId(profile));
    
    String activityId = getActivityId(profile);
    ExoSocialActivity activity = activityManager.getActivity(activityId);
    
    List<ExoSocialActivity> comments = activityManager.getCommentsWithListAccess(activity).loadAsList(0, 20);
    //Number of comments must be 1
    assertEquals(1, comments.size());
    assertEquals("Contact informations has been updated.", comments.get(0).getTitle());
    
    //update about me
    profile.setProperty(Profile.ABOUT_ME, "Nothing to say");
    profile.setListUpdateTypes(Arrays.asList(Profile.UpdateType.ABOUT_ME));
    activity = updateProfile(profile);
    comments = activityManager.getCommentsWithListAccess(activity).loadAsList(0, 20);
    assertNotNull(activity);
    assertEquals("About me has been updated.", comments.get(1).getTitle());
    
    //Number of comments must be 2
    assertEquals(2, comments.size());
    
    //update contact info and about me ==> 2 comments will be added
    profile.setProperty(Profile.ABOUT_ME, "Don't want to say");
    profile.setProperty(Profile.EMAIL, "abc@gmail.com");
    profile.setListUpdateTypes(Arrays.asList(Profile.UpdateType.ABOUT_ME, Profile.UpdateType.CONTACT));
    activity = updateProfile(profile);
    assertNotNull(activity);
    comments = activityManager.getCommentsWithListAccess(activity).loadAsList(0, 20);
    assertEquals("About me has been updated.", comments.get(2).getTitle());
    assertEquals("Contact informations has been updated.", comments.get(3).getTitle());
    
    //Number of comments must be 4
    assertEquals(4, comments.size());
    
    //update experience
    List<Map<String, String>> experiences = new ArrayList<Map<String, String>>();
    Map<String, String> exp1 = new HashMap<String, String>();
    exp1.put("company", "eXo");
    exp1.put("position", "developer");
    exp1.put("startDate", "1/1/2015");
    exp1.put("isCurrent", "true");
    experiences.add(exp1);
    profile.setProperty(Profile.EXPERIENCES, experiences);
    profile.setListUpdateTypes(Arrays.asList(Profile.UpdateType.EXPERIENCES));
    activity = updateProfile(profile);
    assertNotNull(activity);
    comments = activityManager.getCommentsWithListAccess(activity).loadAsList(0, 20);
    assertEquals("Experiences has been updated.", comments.get(4).getTitle());
    
    //Number of comments must be 5
    assertEquals(5, comments.size());
    
    //update avatar
    AvatarAttachment avatar = new AvatarAttachment();
    avatar.setMimeType("plain/text");
    avatar.setInputStream(new ByteArrayInputStream("Attachment content".getBytes()));
    profile.setProperty(Profile.AVATAR, avatar);
    profile.setListUpdateTypes(Arrays.asList(Profile.UpdateType.AVATAR));
    activity = updateProfile(profile);
    assertNotNull(activity);
    comments = activityManager.getCommentsWithListAccess(activity).loadAsList(0, 20);
    assertEquals("Avatar has been updated.", comments.get(5).getTitle());
    
    //Number of comments must be 6
    assertEquals(6, comments.size());
    
    // make sure just only one activity existing
    assertEquals(1, activityManager.getActivitiesWithListAccess(rootIdentity).getSize());
    
    // delete this activity
    activityManager.deleteActivity(activityId);
    assertEquals(0, activityManager.getActivitiesWithListAccess(rootIdentity).getSize());
    
    //re-updated profile will create new activity with a comment 
    profile.setProperty(Profile.POSITION, "worker");
    ExoSocialActivity newActivity = updateProfile(profile);
    //Number of comments must be 1
    assertEquals(1, activityManager.getCommentsWithListAccess(newActivity).getSize());
    
    activityId = getActivityId(profile);
    activityManager.deleteActivity(activityId);
  }
  
  private String getActivityId(Profile profile) {
    return identityStorage.getProfileActivityId(profile, Profile.AttachedActivityType.USER);
  }
  
  private ExoSocialActivity updateProfile(Profile profile) throws Exception {
    identityManager.updateProfile(profile);
    return activityManager.getActivity(getActivityId(profile));
  }
  
}
