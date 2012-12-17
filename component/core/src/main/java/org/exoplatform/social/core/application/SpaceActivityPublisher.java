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

import java.util.LinkedHashMap;
import java.util.Map;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;

/**
 * This listener is responsible for initializing and notifying activity stream for the space. We create a special
 * opensocial user (with a group provider) ready to receive new activities.
 *
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class SpaceActivityPublisher extends SpaceListenerPlugin {

  /**
   * The exosocial:spaces activity type
   * @since 1.2.8
   */
  public static final String SPACES_ACTIVITY_TYPE = "exosocial:spaces";

  /**
   * The SPACE_DISPLAY_NAME_PARAM template param key
   * @since 1.2.8
   */
  public static final String SPACE_DISPLAY_NAME_PARAM = "SPACE_DISPLAY_NAME_PARAM";

  /**
   * The USER_NAME_PARAM template param key
   * @since 1.2.8
   */
  public static final String USER_NAME_PARAM = "USER_NAME_PARAM";


  /**
   * The Logger.
   */
  private static final Log LOG = ExoLogger.getExoLogger(SpaceActivityPublisher.class);

  /**
   * activity manager for posting activities.
   */
  private ActivityManager activityManager;

  /**
   * identity manager for getting identities.
   */
  private IdentityManager identityManager;

  /**
   * Constructor.
   *
   * @param params the initial params
   * @param activityManager the activity manager
   * @param identityManager the identity manager
   */
  public SpaceActivityPublisher(final InitParams params,
                                final ActivityManager activityManager,
                                final IdentityManager identityManager) {
    this.activityManager = activityManager;
    this.identityManager = identityManager;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void spaceCreated(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    final String activityMessage = space.getDisplayName() + " was created by @" + event.getTarget() + " .";
    Map<String, String> templateParams = new LinkedHashMap<String, String>();
    templateParams.put(SPACE_DISPLAY_NAME_PARAM, space.getDisplayName());
    templateParams.put(USER_NAME_PARAM, "@" + event.getTarget());
    templateParams.put(BaseActivityProcessorPlugin.TEMPLATE_PARAM_TO_PROCESS, USER_NAME_PARAM);
    recordActivity(event, createActivity(event, activityMessage, "space_created", templateParams));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void spaceRemoved(SpaceLifeCycleEvent event) {
    LOG.debug("space " + event.getSpace().getDisplayName() + " was removed!");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void applicationActivated(SpaceLifeCycleEvent event) {
    LOG.debug("application <strong>" + event.getTarget() + "</strong> was activated in space "
            + event.getSpace().getDisplayName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void applicationAdded(SpaceLifeCycleEvent event) {
    LOG.debug("application <strong>" + event.getTarget() + "</strong> was added in space "
            + event.getSpace().getDisplayName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void applicationDeactivated(SpaceLifeCycleEvent event) {
    LOG.debug("application " + event.getTarget() + " was deactivated in space "
            + event.getSpace().getDisplayName());

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void applicationRemoved(SpaceLifeCycleEvent event) {
    LOG.debug("application " + event.getTarget() + " was removed in space "
            + event.getSpace().getDisplayName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void grantedLead(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    final String activityMessage = "@" + event.getTarget() + " was granted manager role.";
    Map<String, String> templateParams = new LinkedHashMap<String, String>();
    templateParams.put(USER_NAME_PARAM, "@" + event.getTarget());
    templateParams.put(BaseActivityProcessorPlugin.TEMPLATE_PARAM_TO_PROCESS, USER_NAME_PARAM);
    recordActivity(event, createActivity(event, activityMessage, "manager_role_granted", templateParams));
    LOG.debug("user " + event.getTarget() + " was granted manager role of space " + space.getDisplayName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void joined(SpaceLifeCycleEvent event) {
    final String activityMessage = "@" + event.getTarget() + " has joined the space.";
    Map<String, String> templateParams = new LinkedHashMap<String, String>();
    templateParams.put(USER_NAME_PARAM, "@" + event.getTarget());
    templateParams.put(BaseActivityProcessorPlugin.TEMPLATE_PARAM_TO_PROCESS, USER_NAME_PARAM);
    recordActivity(event, createActivity(event, activityMessage, "user_joined", templateParams));
    LOG.debug("user " + event.getTarget() + " joined space " + event.getSpace().getDisplayName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void left(SpaceLifeCycleEvent event) {
    final String activityMessage = "@" + event.getTarget() + " has left the space.";
    Map<String, String> templateParams = new LinkedHashMap<String, String>();
    templateParams.put(USER_NAME_PARAM, "@" + event.getTarget());
    templateParams.put(BaseActivityProcessorPlugin.TEMPLATE_PARAM_TO_PROCESS, USER_NAME_PARAM);
    recordActivity(event, createActivity(event, activityMessage, "member_left", templateParams));
    LOG.debug("user " + event.getTarget() + " has left of space " + event.getSpace().getDisplayName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void revokedLead(SpaceLifeCycleEvent event) {
    LOG.debug("user " + event.getTarget() + " was revoked lead privileges of space "
            + event.getSpace().getDisplayName());
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
  private ExoSocialActivity createActivity(SpaceLifeCycleEvent event, String activityMessage, String titleId,
                                           Map<String, String> templateParams) {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
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
  private void recordActivity(SpaceLifeCycleEvent event, ExoSocialActivity activity) {
    Space space = event.getSpace();
    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    activityManager.saveActivityNoReturn(spaceIdentity, activity);
  }

}
