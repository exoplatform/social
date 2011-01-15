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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.social.core.identity.IdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.service.LinkProvider;


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

  //TODO: dang.tung: maybe we don't need it but it will fix the problem from portal - get user
  /** The user cache. */
  private Map<String, User> userCache = new HashMap<String, User>();

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

  /* (non-Javadoc)
   * @see org.exoplatform.social.core.identity.IdentityProvider#getAllUserId()
   */
  public List<String> getAllUserId() {
    try {
    PageList pl;

      pl = organizationService.getUserHandler().findUsers(new Query());

    List<User> userList = pl.getAll();
    List<String> userIds = new ArrayList<String>();

    for (User user : userList) {
      userIds.add(user.getUserName());
    }
    return userIds;
    } catch (Exception e) {
      throw new RuntimeException("Failed to load all users");
    }
  }

  /**
   * Gets the user from cache.
   *
   * @param userName the user name
   * @return the user from cache
   */
  private User getUserFromCache(String userName) {
    return userCache.get(userName);
  }

  /**
   * Adds the user to cache.
   *
   * @param user the user
   */
  private void addUserToCache(User user) {
    if(getUserFromCache(user.getUserName()) == null)
      userCache.put(user.getUserName(), user);
  }

  /**
   * Refresh cache.
   */
  private void refreshCache() {
    userCache.clear();
  }

  @Override
  public User findByRemoteId(String remoteId) {
    User user;
    try {
      RequestLifeCycle.begin((ComponentRequestLifecycle)organizationService);
      UserHandler userHandler = organizationService.getUserHandler();
      user = userHandler.findUserByName(remoteId);
    } catch (Exception e) {
      return null;
    } finally {
      RequestLifeCycle.end();
    }
    return user;
  }

  @Override
  public Identity createIdentity(User user) {
    Identity identity = new Identity(NAME, user.getUserName());
    //GlobalId globalId = new GlobalId(OrganizationIdentityProvider.NAME + GlobalId.SEPARATOR + user.getUserName());
    //identity.setId(globalId.toString());
    //identityManager.saveIdentity(identity);
    return identity;
  }

  @Override
  public void populateProfile(Profile profile, User user) {
    profile.setProperty(Profile.FIRST_NAME, user.getFirstName());
    profile.setProperty(Profile.LAST_NAME, user.getLastName());
    profile.setProperty(Profile.USERNAME, user.getUserName());
    profile.setProperty(Profile.URL, LinkProvider.getProfileUri(user.getUserName()));

    if (user.getEmail() != null && !profile.contains("emails")) {
      List<Map<String,String>> emails = new ArrayList<Map<String,String>>();
      Map<String,String> email = new HashMap<String,String>();
      email.put("key", "work");
      email.put("value", user.getEmail());
      emails.add(email);
      profile.setProperty("emails", emails);
    }
    //identityManager.saveProfile(profile);
  }
}