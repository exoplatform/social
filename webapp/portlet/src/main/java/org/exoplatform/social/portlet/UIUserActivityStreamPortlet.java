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
package org.exoplatform.social.portlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.social.webui.composer.PopupContainer;
import org.exoplatform.social.webui.composer.UIComposer;
import org.exoplatform.social.webui.composer.UIComposer.PostContext;
import org.exoplatform.social.webui.profile.UIUserActivitiesDisplay;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * UIUserActivityStreamPortlet.java
 * </p>
 * <p>
 * Display activity composer, and user's activities.
 * </p>
 * @author    <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since     Jul 25, 2010
 * @copyright eXo SAS
 */
@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/groovy/social/portlet/UIUserActivityStreamPortlet.gtmpl"
)
public class UIUserActivityStreamPortlet extends UIPortletApplication {
  private String ownerName;
  private String viewerName;
  private UIComposer uiComposer;
  private boolean composerDisplayed = false;
  UIUserActivitiesDisplay uiUserActivitiesDisplay;
  private String activityId;
  private static final String SINGLE_ACTIVITY_NODE = "activity";
  private static final String SINGLE_ACTIVITY_REDIRECT_LINK_PREFIX = "activity/redirect";
  private static final String NOTIFICATION_REST_PREFIX = "/social/notifications/redirectUrl";
  private static final Log LOG = ExoLogger.getLogger(UIUserActivityStreamPortlet.class.getName());
  /**
   * constructor
   *
   * @throws Exception
   */
  public UIUserActivityStreamPortlet() throws Exception {
    viewerName = Utils.getViewerRemoteId();
    ownerName = Utils.getOwnerRemoteId();
    uiComposer = addChild(UIComposer.class, null, null);
    activityId = Utils.getActivityID();    
    
    if (activityId != null) {
      uiComposer.setPostContext(PostContext.SINGLE);
      uiComposer.setRendered(false);
      composerDisplayed = false;
    } else {      
      uiComposer.setPostContext(PostContext.USER);
      composerDisplayed = true;             
    }  
	  
    uiUserActivitiesDisplay = addChild(UIUserActivitiesDisplay.class, null, "UIUserActivitiesDisplay");
    uiComposer.setActivityDisplay(uiUserActivitiesDisplay);
    addChild(PopupContainer.class, null, "HiddenContainer");
  }

  public boolean isComposerDisplayed() {
    // Case of activity viewer
    if (Utils.getActivityID() != null) {
      return false;
    }
    //
    return composerDisplayed && Utils.getOwnerIdentity().isEnable();
  }
  
  public String getActivityId() {
    activityId = Utils.getActivityID();
    return activityId;
  }
  
  /**
   * If activity is from a space and the current user is member of this space
   * the he has permission to view it
   * 
   * @param activity
   * @return
   */
  private boolean hasPermissionToViewActivity(ExoSocialActivity activity) {
    Space space = Utils.getSpaceService().getSpaceByPrettyName(activity.getStreamOwner());
    if (space == null)
      return true;
    return space != null && Utils.getSpaceService().isMember(space, Utils.getViewerRemoteId());
  }
  
  /**
   * resets to reload all activities
   *
   * @throws Exception
   */
  public void refresh() throws Exception {
    redirectActivity();
    viewerName = Utils.getViewerRemoteId();
    ownerName = Utils.getOwnerRemoteId();
    if (viewerName.equals(ownerName)) {
      uiComposer.isActivityStreamOwner(true);
      uiComposer.setRendered(true);
    } else {
      uiComposer.isActivityStreamOwner(false);

      Relationship relationship = Utils.getRelationshipManager().get(Utils.getViewerIdentity(), Utils.getOwnerIdentity());
      if (relationship != null && (relationship.getStatus() == Relationship.Type.CONFIRMED)) {
        uiComposer.setRendered(true);
      } else {
        uiComposer.setRendered(false);
      }
    }
    uiUserActivitiesDisplay.setOwnerName(ownerName);
  }

  /**
   * Checks if title is displayed or not. This title is only displayed when
   * matching url: /activities
   *
   * @return
   */
  public final boolean isTitleDisplayed() {
    final String activities = "/activities";
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    HttpServletRequest request = portalRequestContext.getRequest();
    String str = request.getRequestURL().toString();
    return str.contains(activities);
  }
  
  /**
   * Check if the page display single activity or not
   * @return boolean 
   */
  public boolean isSingleActivity() {
    return SINGLE_ACTIVITY_NODE.equals(Utils.getSelectedNode());
  }
  
  /**
   * Redirects to activity page viewer (single page) if user has been logged in successfully.
   * Resolves problem query params are removing by portal in case of logging in is required. (SOC-3832).
   * 
   * @throws Exception 
   */
  public void redirectActivity() throws Exception {
    String path = Utils.getSelectedNode();
    if (path.contains(SINGLE_ACTIVITY_REDIRECT_LINK_PREFIX)) {
      PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
      HttpServletResponse response = portalRequestContext.getResponse();
      ExoContainerContext context = CommonsUtils.getService(ExoContainerContext.class);
      String link = new StringBuffer(CommonsUtils.getCurrentDomain()).append("/").append(context.getRestContextName())
                                            .append(NOTIFICATION_REST_PREFIX)
                                            .append(path.replace(SINGLE_ACTIVITY_REDIRECT_LINK_PREFIX, ""))
                                            .toString();
      response.sendRedirect(link);
    }
  }
  
  /**
   * Get activity title when display single activity
   * @return activityTitle
   */
  public String getSingleActivityTitle() {
    String gotId = Utils.getValueFromRequestParam("id");
    if (gotId == null) {
      gotId = Utils.getValueFromRefererURI("id");
      LOG.debug("got id from referer uri::activityId = " + gotId);
    }
    LOG.debug("got id from parameter::activityId = " + gotId);
    if (gotId != null) {
      ExoSocialActivity activity = Utils.getActivityManager().getActivity(gotId);
      if (activity != null && hasPermissionToViewActivity(activity)) {
        LOG.debug("got the activity = " + activity.toString());
        return activity.getTitle();
      }
    }
    
    LOG.debug("Activity title is NULL");
    return null;
  }

  /**
   * Renders popup message in case this child has not rendered in template.
   * 
   * @throws Exception
   * @since 1.2.0-GA
   */
}
