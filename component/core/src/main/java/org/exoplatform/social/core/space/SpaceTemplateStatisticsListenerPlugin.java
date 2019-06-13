/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
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
package org.exoplatform.social.core.space;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Provides Log messages to trace space creation or modification (applications, registration, visibility and banner).
 * These log messages could be processed to get statistics.
 *
 */
public class SpaceTemplateStatisticsListenerPlugin extends SpaceListenerPlugin {

  private static final String FORMAT_CREATION = "local_service={} operation={} " +
      "parameters=\"date:{},space_name:{},space_id:{},user_social_id:{},template_name:{}\"";

  private static final String FORMAT_MODIFICATION = "local_service={} operation={} " +
      "parameters=\"date:{},space_name:{},space_id:{},user_social_id:{},template_name:{},modification_type:{}\"";

  private static final String SPACE_TEMPLATES_SERVICE = "space_templates";

  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  private static final String CREATE_SPACE_OPERATION = "create_space";

  private static final String MODIFY_SPACE_OPERATION = "modify_space";

  /**
   * The Logger.
   */
  private static final Log LOG = ExoLogger.getExoLogger(SpaceTemplateStatisticsListenerPlugin.class);

  private IdentityManager identityManager;

  /**
   * Constructor.
   *
   * @param identityManager the identity manager
   */
  public SpaceTemplateStatisticsListenerPlugin(IdentityManager identityManager) {
    this.identityManager = identityManager;
  }


  @Override
  public void spaceCreated(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    String spaceName = space.getPrettyName();
    String spaceId = space.getId();
    String creator = space.getManagers()[0];
    Identity creatorIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, creator, true);
    String creatorId = creatorIdentity.getId();
    String templateName = space.getTemplate();
    String dateString = DATE_FORMAT.format(new Date());
    LOG.info(FORMAT_CREATION, SPACE_TEMPLATES_SERVICE, CREATE_SPACE_OPERATION,
        dateString, spaceName, spaceId, creatorId, templateName);
  }

  @Override
  public void spaceRemoved(SpaceLifeCycleEvent event) {

  }

  @Override
  public void applicationAdded(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    String spaceId = space.getId();
    if (spaceId != null) {
      String spaceName = space.getPrettyName();
      String creator = space.getManagers()[0];
      Identity creatorIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, creator, true);
      String creatorId = creatorIdentity.getId();
      String templateName = space.getTemplate();
      String dateString = DATE_FORMAT.format(new Date());
      LOG.info(FORMAT_MODIFICATION, SPACE_TEMPLATES_SERVICE, MODIFY_SPACE_OPERATION,
          dateString, spaceName, spaceId, creatorId, templateName, "application");
    }
  }

  @Override
  public void applicationRemoved(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    String spaceId = space.getId();
    if (spaceId != null) {
      String spaceName = space.getPrettyName();
      String creator = space.getManagers()[0];
      Identity creatorIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, creator, true);
      String creatorId = creatorIdentity.getId();
      String templateName = space.getTemplate();
      String dateString = DATE_FORMAT.format(new Date());
      LOG.info(FORMAT_MODIFICATION, SPACE_TEMPLATES_SERVICE, MODIFY_SPACE_OPERATION,
          dateString, spaceName, spaceId, creatorId, templateName, "application");
    }
  }

  @Override
  public void applicationActivated(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    String spaceId = space.getId();
    if (spaceId != null) {
      String spaceName = space.getPrettyName();
      String creator = space.getManagers()[0];
      Identity creatorIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, creator, true);
      String creatorId = creatorIdentity.getId();
      String templateName = space.getTemplate();
      String dateString = DATE_FORMAT.format(new Date());
      LOG.info(FORMAT_MODIFICATION, SPACE_TEMPLATES_SERVICE, MODIFY_SPACE_OPERATION,
          dateString, spaceName, spaceId, creatorId, templateName, "application");
    }
  }

  @Override
  public void applicationDeactivated(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    String spaceId = space.getId();
    if (spaceId != null) {
      String spaceName = space.getPrettyName();
      String creator = space.getManagers()[0];
      Identity creatorIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, creator, true);
      String creatorId = creatorIdentity.getId();
      String templateName = space.getTemplate();
      String dateString = DATE_FORMAT.format(new Date());
      LOG.info(FORMAT_MODIFICATION, SPACE_TEMPLATES_SERVICE, MODIFY_SPACE_OPERATION,
          dateString, spaceName, spaceId, creatorId, templateName, "application");
    }
  }

  @Override
  public void joined(SpaceLifeCycleEvent event) {

  }

  @Override
  public void left(SpaceLifeCycleEvent event) {

  }

  @Override
  public void grantedLead(SpaceLifeCycleEvent event) {

  }

  @Override
  public void revokedLead(SpaceLifeCycleEvent event) {

  }

  @Override
  public void spaceRenamed(SpaceLifeCycleEvent event) {

  }

  @Override
  public void spaceDescriptionEdited(SpaceLifeCycleEvent event) {

  }

  @Override
  public void spaceAvatarEdited(SpaceLifeCycleEvent event) {

  }

  @Override
  public void spaceAccessEdited(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    String spaceId = space.getId();
    if (spaceId != null) {
      String spaceName = space.getPrettyName();
      String creator = space.getManagers()[0];
      Identity creatorIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, creator, true);
      String creatorId = creatorIdentity.getId();
      String templateName = space.getTemplate();
      String dateString = DATE_FORMAT.format(new Date());
      LOG.info(FORMAT_MODIFICATION, SPACE_TEMPLATES_SERVICE, MODIFY_SPACE_OPERATION,
          dateString, spaceName, spaceId, creatorId, templateName, "visibility");
    }
  }

  @Override
  public void addInvitedUser(SpaceLifeCycleEvent event) {

  }

  @Override
  public void addPendingUser(SpaceLifeCycleEvent event) {

  }

  @Override
  public void spaceRegistrationEdited(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    String spaceId = space.getId();
    if (spaceId != null) {
      String spaceName = space.getPrettyName();
      String creator = space.getManagers()[0];
      Identity creatorIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, creator, true);
      String creatorId = creatorIdentity.getId();
      String templateName = space.getTemplate();
      String dateString = DATE_FORMAT.format(new Date());
      LOG.info(FORMAT_MODIFICATION, SPACE_TEMPLATES_SERVICE, MODIFY_SPACE_OPERATION,
          dateString, spaceName, spaceId, creatorId, templateName, "registration");
    }
  }

  @Override
  public void spaceBannerEdited(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    String spaceId = space.getId();
    if (spaceId != null && event.getSource() != null) {
      String spaceName = space.getPrettyName();
      String creator = space.getManagers()[0];
      Identity creatorIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, creator, true);
      String creatorId = creatorIdentity.getId();
      String templateName = space.getTemplate();
      String dateString = DATE_FORMAT.format(new Date());
      LOG.info(FORMAT_MODIFICATION, SPACE_TEMPLATES_SERVICE, MODIFY_SPACE_OPERATION,
          dateString, spaceName, spaceId, creatorId, templateName, "banner");
    }
  }
}
