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
package org.exoplatform.social.webui.activity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.ArrayUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.social.core.activity.model.Util;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.profile.UIUserActivitiesDisplay;

/**
 * UserActivityListAccess
 * <p></p>
 *
 * @author Zuanoc
 * @copyright eXo SEA
 * @since Sep 7, 2010
 */
public class UserActivityListAccess implements ListAccess<Activity> {
  static private final Log LOG = ExoLogger.getLogger(SpaceActivityListAccess.class);
  
  private Identity ownerIdentity;
  private UIUserActivitiesDisplay.DisplayMode displayMode;
  private IdentityManager identityManager;
  private ActivityManager activityManager;
  private SpaceService spaceService;
  
  public UserActivityListAccess(Identity ownerIdentity, UIUserActivitiesDisplay.DisplayMode displayMode) {
    identityManager = (IdentityManager) PortalContainer.getComponent(IdentityManager.class);
    activityManager = (ActivityManager) PortalContainer.getComponent(ActivityManager.class);
    spaceService = (SpaceService) PortalContainer.getComponent(SpaceService.class);

    this.ownerIdentity = ownerIdentity;
    this.displayMode = displayMode;
  }

  public int getSize() throws Exception {
    if (displayMode == UIUserActivitiesDisplay.DisplayMode.MY_STATUS || displayMode == UIUserActivitiesDisplay.DisplayMode.OWNER_STATUS) {
      return activityManager.getActivitiesCount(ownerIdentity);
    } else if (displayMode == UIUserActivitiesDisplay.DisplayMode.SPACES) {
      return getActivitiesOfUserSpaces().length;
    } else {
      return getActivitiesOfConnections().length;
    }
  }
  
  public Activity[] load(int index, int length) throws Exception{
    List<Activity> activityList;
    if (displayMode == UIUserActivitiesDisplay.DisplayMode.MY_STATUS || displayMode == UIUserActivitiesDisplay.DisplayMode.OWNER_STATUS) {
      activityList = activityManager.getActivities(ownerIdentity, index, length);
    } else if (displayMode == UIUserActivitiesDisplay.DisplayMode.SPACES) {
      activityList = getActivitiesOfUserSpaces(index, length);
    } else {
      activityList = getActivitiesOfConnections(index, length);
    }

    return activityList.toArray(new Activity[activityList.size()]);
  }

  private Object[] getActivitiesOfConnections() throws Exception {
    List<Identity> connectionsList = getConnections();
    SortedSet<Activity> sortedActivityList = new TreeSet<Activity>(Util.activityComparator());

    String identityId;
    for (Identity identity : connectionsList) {
      List<Activity> tempActivityList = activityManager.getActivities(identity);
      identityId = identity.getId();
      for (Activity activity : tempActivityList) {
        if (activity.getUserId().equals(identityId)) {
          sortedActivityList.add(activity);
        }
      }
    }

    return sortedActivityList.toArray();
  }
  
  private List<Activity> getActivitiesOfConnections(int index, int length) throws Exception {
    Object[] activityArray = getActivitiesOfConnections();
    activityArray = ArrayUtils.subarray(activityArray, index, index+length);

    List<Activity> activityList = new ArrayList<Activity>();
    for (Object obj : activityArray) {
      activityList.add((Activity) obj);
    }

    return activityList;
  }

  private Object[] getActivitiesOfUserSpaces() {
    SortedSet<Activity> sortedActivityList = new TreeSet<Activity>(Util.activityComparator());

    try {
      List<Space> spaceList = spaceService.getAccessibleSpaces(ownerIdentity.getRemoteId());
      for (Space space : spaceList) {
        Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getId());
        List<Activity> spaceActivityList = activityManager.getActivities(spaceIdentity);
        sortedActivityList.addAll(spaceActivityList);
      }
    } catch (Exception e) {
      LOG.error(e);
    }

    return sortedActivityList.toArray();
  }

  private List<Activity> getActivitiesOfUserSpaces(int index, int length) {
    Object[] activityArray = getActivitiesOfUserSpaces();
    activityArray = ArrayUtils.subarray(activityArray, index, index+length);
    List<Activity> activityList = new ArrayList<Activity>();
    for (Object obj : activityArray) {
      activityList.add((Activity) obj);
    }
    return activityList;
  }

  private List<Identity> getConnections() throws Exception {
    List<Identity> connectionsList = identityManager.getIdentities(OrganizationIdentityProvider.NAME);
    Iterator<Identity> itr = connectionsList.iterator();
    while (itr.hasNext()) {
      Identity identity = itr.next();
      if (getConnectionStatus(identity) != Relationship.Type.CONFIRM) {
        itr.remove();
      }
    }
    return connectionsList;
  }

  private Relationship.Type getConnectionStatus(Identity identity) throws Exception {
    if (identity.getId().equals(ownerIdentity.getId())) {
      return Relationship.Type.SELF;
    }
    RelationshipManager relationshipManager = (RelationshipManager) PortalContainer.getComponent(RelationshipManager.class);
    Relationship relationship = relationshipManager.getRelationship(identity, ownerIdentity);
    return relationshipManager.getRelationshipStatus(relationship, ownerIdentity);
  }
}
