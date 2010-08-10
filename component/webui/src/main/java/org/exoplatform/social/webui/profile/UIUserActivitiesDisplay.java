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
package org.exoplatform.social.webui.profile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
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
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.activity.UIActivitiesContainer;
import org.exoplatform.social.webui.composer.UIComposer.PostContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Displays user's activities
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Jul 30, 2010
 * @copyright eXo SAS
 */
@ComponentConfig(
  template = "classpath:groovy/social/webui/profile/UIUserActivitiesDisplay.gtmpl",
  events = {
    @EventConfig(listeners = UIUserActivitiesDisplay.ChangeDisplayModeActionListener.class)
  }
)
public class UIUserActivitiesDisplay extends UIContainer {

  static private final Log      LOG = ExoLogger.getLogger(UIUserActivitiesDisplay.class);

  public enum DisplayMode {
    CONNECTIONS,
    SPACES,
    MY_STATUS
  }
  private DisplayMode selectedDisplayMode = DisplayMode.CONNECTIONS;

  private String                ownerName;

  private SortedSet<Activity> sortedActivityList;

  private int displayedActivityItems = 20;

  private List<Activity>        activityList;

  private UIActivitiesContainer uiActivitiesContainer;

  /**
   * constructor
   */
  public UIUserActivitiesDisplay() {

  }

  public int getDisplayedActivityItems() {
    return displayedActivityItems;
  }

  public void setDisplayedActivityItems(int itemsNumber) {
    this.displayedActivityItems = itemsNumber;
  }

  public void setSelectedDisplayMode(DisplayMode displayMode) {
    selectedDisplayMode = displayMode;
    try {
      init();
    } catch (Exception e) {
      LOG.error("Failed to init()");
    }
  }

  public DisplayMode getSelectedDisplayMode() {
    return selectedDisplayMode;
  }
  /**
   * sets activity stream owner (user remote Id)
   *
   * @param ownerName
   * @throws Exception
   */
  public void setOwnerName(String ownerName) throws Exception {
    this.ownerName = ownerName;
    init();
  }

  public String getOwnerName() {
    return ownerName;
  }

  public static class ChangeDisplayModeActionListener extends EventListener<UIUserActivitiesDisplay> {

    @Override
    public void execute(Event<UIUserActivitiesDisplay> event) throws Exception {
      UIUserActivitiesDisplay uiUserActivitiesDisplay = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      String selectedDisplayMode = requestContext.getRequestParameter(OBJECTID);
      if (selectedDisplayMode.equals(DisplayMode.MY_STATUS.toString())) {
        uiUserActivitiesDisplay.setSelectedDisplayMode(DisplayMode.MY_STATUS);
      } else if (selectedDisplayMode.equals(DisplayMode.SPACES.toString())) {
        uiUserActivitiesDisplay.setSelectedDisplayMode(DisplayMode.SPACES);
      } else {
        uiUserActivitiesDisplay.setSelectedDisplayMode(DisplayMode.CONNECTIONS);
      }
      requestContext.addUIComponentToUpdateByAjax(uiUserActivitiesDisplay);
    }

  }
  /**
   * initialize
   *
   * @throws Exception
   */
  public void init() throws Exception {
    Validate.notNull(ownerName, "ownerName must not be null.");
    Identity ownerIdentity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                                      ownerName);
    activityList = new ArrayList<Activity>();
    sortedActivityList = new TreeSet<Activity>(Util.activityComparator());

    if (getSelectedDisplayMode() == DisplayMode.MY_STATUS) {
      activityList = getActivityManager().getActivities(ownerIdentity);
    } else if (getSelectedDisplayMode() == DisplayMode.SPACES) {
      SpaceService spaceService = getApplicationComponent(SpaceService.class);
      ActivityManager activityManager = getActivityManager();
      IdentityManager identityManager = getIdentityManager();
      try {
        List<Space> spaceList = spaceService.getAccessibleSpaces(ownerName);
        for (Space space : spaceList) {
          Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getId());
          List<Activity> spaceActivityList =  activityManager.getActivities(spaceIdentity);
          sortedActivityList.addAll(spaceActivityList);
        }
        Object[] activityArray = sortedActivityList.toArray();
        if (sortedActivityList.size() > displayedActivityItems) {
          activityArray = ArrayUtils.subarray(activityArray, 0, displayedActivityItems);
        }
        for (Object obj : activityArray) {
          activityList.add((Activity) obj);
        }
      } catch (SpaceException e) {
        LOG.error("failed to get spaceList by userId: " + ownerName);
      } catch (Exception e) {
        LOG.error("failed to init() in UIMySpacesActivitiesDisplay");
      }
    } else {
      List<Identity> connectionsList = getConnections();
      ActivityManager activityManager = getActivityManager();
      for (Identity identity : connectionsList) {
        sortedActivityList.addAll(activityManager.getActivities(identity));
      }
      Object[] activityArray = sortedActivityList.toArray();
      if (sortedActivityList.size() > displayedActivityItems) {
        activityArray =  ArrayUtils.subarray(activityArray, 0, displayedActivityItems);
      }
      for (Object obj : activityArray) {
        activityList.add((Activity) obj);
      }
    }

    removeChild(UIActivitiesContainer.class);

    uiActivitiesContainer = addChild(UIActivitiesContainer.class, null, null);
    uiActivitiesContainer.setPostContext(PostContext.USER);
    uiActivitiesContainer.setOwnerName(ownerName);
    uiActivitiesContainer.setActivityList(activityList);

  }

  /**
   * Loads all existing identity connected to ownerName
   *
   * @return all existing identity
   * @throws Exception
   */
  private List<Identity> getConnections() throws Exception {
    List<Identity> connectionsList = getIdentityManager().getIdentities(OrganizationIdentityProvider.NAME);
    Iterator<Identity> itr = connectionsList.iterator();
    while (itr.hasNext()) {
      Identity identity = itr.next();
      if (getConnectionStatus(identity) != Relationship.Type.CONFIRM) {
        itr.remove();
      }
    }
    return connectionsList;
  }

  /**
   * Gets contact status between current user and identity that is checked.<br>
   *
   * @param identity Object is checked status with current user.
   * @return type of relationship status that equivalent the relationship.
   * @throws Exception
   */
  private Relationship.Type getConnectionStatus(Identity identity) throws Exception {
    if (identity.getId().equals(getOwnerIdentity().getId())) {
      return Relationship.Type.SELF;
    }
    RelationshipManager relationshipManager = getRelationshipManger();
    Relationship relationship = relationshipManager.getRelationship(identity, getOwnerIdentity());
    return relationshipManager.getRelationshipStatus(relationship, getOwnerIdentity());
  }

  /**
   * Gets owner's identity.<br>
   *
   * @return identity of ownerName.
   * @throws Exception
   */
  private Identity getOwnerIdentity() throws Exception {
    IdentityManager identityManger = getIdentityManager();
    return identityManger.getOrCreateIdentity(OrganizationIdentityProvider.NAME, ownerName);
  }

  /**
   * Gets identityManager
   *
   * @return
   */
  private IdentityManager getIdentityManager() {
    return getApplicationComponent(IdentityManager.class);
  }

  /**
   * Gets activityManager
   *
   * @return
   */
  private ActivityManager getActivityManager() {
    return getApplicationComponent(ActivityManager.class);
  }

  /**
   * Gets relationshipManager
   *
   * @return
   */
  private RelationshipManager getRelationshipManger() {
    return getApplicationComponent(RelationshipManager.class);
  }
}
