/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.social.core.identity.provider;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.social.core.identity.IdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.webui.exception.MessageException;


/**
 * The Class OrganizationIdentityProvider.
 */
public class OrganizationIdentityProvider extends IdentityProvider<User> {

  /** Logger */
  private static final Log LOG = ExoLogger.getLogger(OrganizationIdentityProvider.NAME);

  /** The organization service. */
  private OrganizationService organizationService;

  /** The Constant NAME. */
  public final static String NAME = "organization";

  /**
   * Instantiates a new organization identity provider.
   *
   * @param organizationService the organization service
   */
  public OrganizationIdentityProvider(OrganizationService organizationService) {
    this.organizationService = organizationService;

  }

  @Override
  public String getName() {
    return NAME;
  }

  /**
   * {@inheritDoc}
   *
   * Return only 500 maximum users for this duplicated method.
   *
   * @return list of string containing user names.
   */
  public List<String> getAllUserId() {
    try {

      ListAccess<User> allUsers = organizationService.getUserHandler().findAllUsers();
      //Get 500 as maxium
      final int MAX_USERS = 500;
      User[] users = allUsers.load(0, allUsers.getSize() >= MAX_USERS ? MAX_USERS : allUsers.getSize());
      List<String> userIds = new ArrayList<String>();

      for (User user : users) {
        userIds.add(user.getUserName());
      }
      return userIds;
    } catch (Exception e) {
      throw new RuntimeException("Failed to load all users");
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public User findByRemoteId(String remoteId) {
    User user;
    try {
      RequestLifeCycle.begin((ComponentRequestLifecycle)organizationService);
      user = CommonsUtils.getUser(remoteId);
    } catch (Exception e) {
      LOG.warn("An error occured while getting identity from store", e);
      return null;
    } finally {
      RequestLifeCycle.end();
    }
    return user;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Identity createIdentity(User user) {
    Identity identity = new Identity(NAME, user.getUserName());
    identity.setEnable(user.isEnabled());
    return identity;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void populateProfile(Profile profile, User user) {
    profile.setProperty(Profile.FIRST_NAME, user.getFirstName());
    profile.setProperty(Profile.LAST_NAME, user.getLastName());
    profile.setProperty(Profile.FULL_NAME, user.getDisplayName());
    profile.setProperty(Profile.USERNAME, user.getUserName());
    profile.setProperty(Profile.EMAIL, user.getEmail());
  }

  /**
   * Synchronizes profile's changes to user's information.
   *
   * @param profile
   */
  @Override
  public void onUpdateProfile(Profile profile) throws MessageException {
    new UpdateProfileProcess(profile).doUpdate();
  }

  /**
   * synchronous changed profile what is happened in Social to Portal side.
   * @author thanh_vucong
   *
   */
  private class UpdateProfileProcess {
    
    private Profile updatedProfile = null;
    private String userName = null;
    
    public UpdateProfileProcess(Profile updatedProfile) {
      this.updatedProfile = updatedProfile;
      this.userName = (String) updatedProfile.getProperty(Profile.USERNAME);
    }
    
    /**
     * update profile information
     */
    public void doUpdate() throws MessageException {
      try {
        if (updatedProfile.getListUpdateTypes().contains(Profile.UpdateType.CONTACT)) {
          updateBasicInfo();
          updateContact();
        }
      } catch (Exception e) {
        if ( e instanceof MessageException) {
          throw (MessageException) e;
        } else {
          LOG.warn("Failed to update user by profile", e);
        }
        
      }
    }
    
    /**
     * Updates profile in Basic Information section
     * 
     * @throws Exception
     */
    private void updateBasicInfo() throws Exception {
      //
      String firstName = (String) updatedProfile.getProperty(Profile.FIRST_NAME);
      String lastName = (String) updatedProfile.getProperty(Profile.LAST_NAME);
      String email = (String) updatedProfile.getProperty(Profile.EMAIL);
      String fullName = firstName + " " + lastName;
      
      boolean hasUpdate = false;

      //
      User foundUser = organizationService.getUserHandler().findUserByName(this.userName);
      if(foundUser == null) {
        return;
      }
     
      //
      if (!StringUtils.equals(foundUser.getFirstName(), firstName)) {
        foundUser.setFirstName(firstName);
        hasUpdate = true;
      }
      if (!StringUtils.equals(foundUser.getLastName(), lastName)) {
        foundUser.setLastName(lastName);
        hasUpdate = true;
      }
      if (!StringUtils.equals(foundUser.getEmail(), email)) {
        foundUser.setEmail(email);
        hasUpdate = true;
      }
      
      if (!StringUtils.equals(foundUser.getDisplayName(), fullName)) {
        foundUser.setDisplayName(fullName);
        hasUpdate = true;
      }

      //
      if (hasUpdate) {
        organizationService.getUserHandler().saveUser(foundUser, true);        
      }
    }
    
    /**
     * Updates profile in Contact section
     * @throws Exception
     */
    private void updateContact() throws Exception {
      //
      String position = (String) updatedProfile.getProperty(Profile.POSITION);
      String gender = (String) updatedProfile.getProperty(Profile.GENDER);


      UserProfile foundUserProfile = organizationService.getUserProfileHandler()
                                                        .findUserProfileByName(userName);
      //
      if(foundUserProfile == null) {
        foundUserProfile = organizationService.getUserProfileHandler().createUserProfileInstance(userName);
      }

      boolean hasUpdated = false;
      String uPosition = foundUserProfile.getAttribute(UserProfile.PERSONAL_INFO_KEYS[7]);//user.jobtitle
      if (!StringUtils.equals(position, uPosition)) {
        foundUserProfile.setAttribute(UserProfile.PERSONAL_INFO_KEYS[7], position);//user.jobtitle
        hasUpdated = true;
      }
      String uGender = foundUserProfile.getAttribute(UserProfile.PERSONAL_INFO_KEYS[4]);// "user.gender"
      if (!StringUtils.equals(gender, uGender)) {
        foundUserProfile.setAttribute(UserProfile.PERSONAL_INFO_KEYS[4], gender);// "user.gender"
        hasUpdated = true;
      }
      if (hasUpdated) {
        organizationService.getUserProfileHandler().saveUserProfile(foundUserProfile, false);
      }
    }
  }
}
