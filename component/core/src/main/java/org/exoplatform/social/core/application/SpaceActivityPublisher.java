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

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;

/**
 * This listener is responsible for initializing and notifying activity stream
 * for the space. We create a special opensocial user (with a group provider)
 * ready to receive new activities.
 *
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public class SpaceActivityPublisher  extends SpaceListenerPlugin {

  /**
   * The exosocial:spaces activity type
   * @since 1.1.9
   */
  public static final String SPACES_ACTIVITY_TYPE = "exosocial:spaces";

  /**
   * The SPACE_DISPLAY_NAME_PARAM template param key
   * @since 1.1.9
   */
  public static final String SPACE_DISPLAY_NAME_PARAM = "SPACE_DISPLAY_NAME_PARAM";

  /**
   * The USER_NAME_PARAM template param key
   * @since 1.1.9
   */
  public static final String USER_NAME_PARAM = "USER_NAME_PARAM";
  
  private static Log      LOG = ExoLogger.getExoLogger(SpaceActivityPublisher.class);

  private ActivityManager activityManager;

  private IdentityManager identityManager;


  public SpaceActivityPublisher(InitParams params,
                                ActivityManager activityManager,
                                IdentityManager identityManager) throws Exception {
    this.activityManager = activityManager;
    this.identityManager = identityManager;

  }

  @Override
  public void spaceCreated(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    final String activityMessage = space.getName() + " was created by @" + event.getTarget() + " .";
    Map<String, String> templateParams = new HashMap<String, String>();
    templateParams.put(SPACE_DISPLAY_NAME_PARAM, space.getName());
    templateParams.put(USER_NAME_PARAM, "@" + event.getTarget());
    recordActivity(event, createActivity(event, activityMessage, "space_created", templateParams));
  }

  @Override
  public void spaceRemoved(SpaceLifeCycleEvent event) {
    LOG.info("space " + event.getSpace().getName() + " was removed!");
  }

  public void applicationActivated(SpaceLifeCycleEvent event) {

    LOG.info("application <b>" + event.getTarget() + "</b> was activated in space "
        + event.getSpace().getName());

  }

  public void applicationAdded(SpaceLifeCycleEvent event) {
    LOG.info("application <b>" + event.getTarget() + "</b> was added in space "
        + event.getSpace().getName());

  }

  public void applicationDeactivated(SpaceLifeCycleEvent event) {
    LOG.info("application " + event.getTarget() + " was deactivated in space "
        + event.getSpace().getName());

  }

  public void applicationRemoved(SpaceLifeCycleEvent event) {
    LOG.info("application " + event.getTarget() + " was removed in space "
        + event.getSpace().getName());
  }

  public void grantedLead(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    final String activityMessage = "@" + event.getTarget() + " was granted manager role.";
    Map<String, String> templateParams = new HashMap<String, String>();
    templateParams.put(USER_NAME_PARAM, "@" + event.getTarget());
    recordActivity(event, createActivity(event, activityMessage, "manager_role_granted", templateParams));
    LOG.debug("user " + event.getTarget() + " was granted manager role of space " + space.getName());
  }

  public void joined(SpaceLifeCycleEvent event) {
    final String activityMessage = "@" + event.getTarget() + " has joined the space.";
    Map<String, String> templateParams = new HashMap<String, String>();
    templateParams.put(USER_NAME_PARAM, "@" + event.getTarget());
    recordActivity(event, createActivity(event, activityMessage, "user_joined", templateParams));
    LOG.debug("user " + event.getTarget() + " joined space " + event.getSpace().getName());
  }

  public void left(SpaceLifeCycleEvent event) {
    final String activityMessage = "@" + event.getTarget() + " has left the space.";
    Map<String, String> templateParams = new HashMap<String, String>();
    templateParams.put(USER_NAME_PARAM, "@" + event.getTarget());
    recordActivity(event, createActivity(event, activityMessage, "member_left", templateParams));
    LOG.debug("user " + event.getTarget() + " has left of space " + event.getSpace().getName());
  }

  public void revokedLead(SpaceLifeCycleEvent event) {

    LOG.info("user " + event.getTarget() + " was revoked lead privileges of space "
        + event.getSpace().getName());
  }

  /**
   * Creates an activity.
   *
   * @param event the space lifeclycle event
   * @param activityMessage the activity message
   * @param titleId the titleId
   * @param templateParams the template params
   *
   * @return the created activity object
   */
  private Activity createActivity(SpaceLifeCycleEvent event, String activityMessage, String titleId,
                                           Map<String, String> templateParams) {
    Activity activity = new Activity();
    activity.setType(SPACES_ACTIVITY_TYPE);
    activity.setTitle(activityMessage);
    activity.setTitleId(titleId);
    activity.setTemplateParams(templateParams);
    return activity;
  }

  /**
   * Records an activity based on space lifecyle event and the activity object.
   *
   * @param event the space lifecyle event
   * @param activity the activity object
   */
  private void recordActivity(SpaceLifeCycleEvent event, Activity activity) {
    Space space = event.getSpace();
    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getName(), false);
    activityManager.saveActivity(spaceIdentity, activity);
  }
}
