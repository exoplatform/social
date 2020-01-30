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
package org.exoplatform.social.core.space.impl;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.space.SpaceApplication;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceTemplate;
import org.exoplatform.social.core.space.SpaceTemplateConfigPlugin;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceApplicationHandler;
import org.exoplatform.social.core.space.spi.SpaceTemplateService;
import org.picocontainer.Startable;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * {@link org.exoplatform.social.core.space.spi.SpaceTemplateService} implementation.
 */
public class SpaceTemplateServiceImpl implements SpaceTemplateService, Startable {

  private static final String DEFAULT_SPACE_TEMPLATE_PARAM= "defaultSpaceTemplate";

  private static final Log LOG = ExoLogger.getLogger(SpaceTemplateServiceImpl.class);

  private Map<String, SpaceApplicationHandler> spaceApplicationHandlers = new HashMap<String, SpaceApplicationHandler>();

  private Map<String, SpaceTemplate> spaceTemplates = new HashMap<>();

  private Map<String, SpaceTemplate> registeredSpaceTemplates = new HashMap<>();

  private Map<String, SpaceTemplate> extendedSpaceTemplates = new HashMap<>();

  private String defaultSpaceTemplate;

  public SpaceTemplateServiceImpl(InitParams params) {
    if (params != null) {
      defaultSpaceTemplate = params.getValueParam(DEFAULT_SPACE_TEMPLATE_PARAM).getValue();
    }
  }

  @Override
  public List<SpaceTemplate> getSpaceTemplates() {
    return Collections.unmodifiableList(new ArrayList<>(spaceTemplates.values().stream().map(SpaceTemplate::clone)
        .sorted(Comparator.comparing(SpaceTemplate::getName)).collect(Collectors.toList())));
  }

  @Override
  public List<SpaceTemplate> getSpaceTemplates(String userId) throws Exception {
    List<SpaceTemplate> list = new ArrayList<>();
    Identity identity = getIdentity(userId);
    for (SpaceTemplate spaceTemplate : getSpaceTemplates()) {
      String perms = spaceTemplate.getPermissions();
      if (perms != null) {
        Pattern pattern = Pattern.compile(";");
        List<String> permissions = pattern.splitAsStream(perms).collect(Collectors.toList());
        for (String perm : permissions) {
          UserACL.Permission permission = new UserACL.Permission();
          permission.setPermissionExpression(perm);
          String groupId = permission.getGroupId();
          String membership = permission.getMembership();
          if (identity.isMemberOf(groupId, membership)) {
            list.add(spaceTemplate);
            break;
          }
        }
      }
    }
    return list;
  }

  @Override
  public List<SpaceTemplate> getLabelledSpaceTemplates(String userId, String lang) throws Exception {
    List<SpaceTemplate> templatelist = new ArrayList<>();
    Identity identity = getIdentity(userId);
    ResourceBundleService resourceBundleService = CommonsUtils.getService(ResourceBundleService.class);
    OrganizationService organizationService = CommonsUtils.getService(OrganizationService.class);
    for (SpaceTemplate spaceTemplate : getSpaceTemplates(userId)) {
      String perms = spaceTemplate.getPermissions();
      StringBuilder templatePermission = new StringBuilder("");
      if (identity.isMemberOf("/platform/administrators")) {
        if (perms != null) {
          ResourceBundle resourceBundle = resourceBundleService.getResourceBundle("locale.portlet.social.SpaceTemplatesPortlet", new Locale(lang));
          Pattern pattern = Pattern.compile(";");
          List<String> permissions = pattern.splitAsStream(perms).collect(Collectors.toList());
          for (String perm : permissions) {
            UserACL.Permission permission = new UserACL.Permission();
            permission.setPermissionExpression(perm);
            String groupId = permission.getGroupId();
            String membership = permission.getMembership();
            if ("*".equals(membership)) {
              membership = resourceBundle.getString("social.spaces.templates.any");
            } else {
              membership = StringUtils.capitalize(membership);
            }
            GroupHandler groupHandler1 = organizationService.getGroupHandler();
            String groupName = groupId;
            Group group = groupHandler1.findGroupById(groupId);
            if (group != null) {
              groupName = group.getGroupName();
            }
            // Uppercase the first char
            groupName = StringUtils.capitalize(groupName);
            String key = resourceBundle.getString("social.spaces.templates.permissionEntry");
            templatePermission.append(key.replace("{0}", membership).replace("{1}", groupName));
            templatePermission.append(", ");
          }
          int index = templatePermission.lastIndexOf(", ");
          templatePermission.deleteCharAt(index);
        }
      }
      spaceTemplate.setPermissionsLabels(templatePermission.toString());
      templatelist.add(spaceTemplate);
    }
    return templatelist;
  }

  @Override
  public SpaceTemplate getSpaceTemplateByName(String name) {
    if (name == null) {
      LOG.warn("Attempt to get template with null name. Falling back to default template name");
      name = getDefaultSpaceTemplate();
    }
    SpaceTemplate template = spaceTemplates.get(name);
    if(template != null) {
      return template.clone();
    }
    SpaceTemplate defaultTemplate = spaceTemplates.get(getDefaultSpaceTemplate());
    return defaultTemplate.clone();
  }

  @Override
  public void registerSpaceTemplatePlugin(SpaceTemplateConfigPlugin spaceTemplateConfigPlugin) {
    SpaceTemplate spaceTemplate = spaceTemplateConfigPlugin.getSpaceTemplate();
    if (spaceTemplate == null) {
      LOG.warn("No space template found !");
      return;
    }
    registeredSpaceTemplates.put(spaceTemplate.getName(), spaceTemplate);
  }

  @Override
  public void extendSpaceTemplatePlugin(SpaceTemplateConfigPlugin spaceTemplateConfigPlugin) {
    SpaceTemplate spaceTemplateExtension = spaceTemplateConfigPlugin.getSpaceTemplate();
    if(spaceTemplateExtension == null || StringUtils.isBlank(spaceTemplateExtension.getName())) {
      LOG.warn("Space template plugin doesn't have mandatory object: {}. The plugin will be ignored.", spaceTemplateConfigPlugin);
      return;
    }
    extendedSpaceTemplates.put(spaceTemplateExtension.getName(), spaceTemplateExtension);
  }

  /**
   * Add space application handler
   *
   */
  @Override
  public void registerSpaceApplicationHandler(SpaceApplicationHandler spaceApplicationHandler) {
    this.spaceApplicationHandlers.put(spaceApplicationHandler.getName(), spaceApplicationHandler);
  }

  /**
   * Gets space application handlers
   *
   * @return
   */
  @Override
  public Map<String, SpaceApplicationHandler> getSpaceApplicationHandlers() {
    return Collections.unmodifiableMap(this.spaceApplicationHandlers);
  }

  @Override
  public String getDefaultSpaceTemplate() {
    return defaultSpaceTemplate;
  }

  @Override
  public void initSpaceApplications(Space space, SpaceApplicationHandler spaceApplicationHandler) throws SpaceException {
    String type = space.getTemplate();
    SpaceTemplate spaceTemplate = getSpaceTemplateByName(type);
    spaceApplicationHandler.initApps(space, spaceTemplate);
    List<SpaceApplication> apps = spaceTemplate.getSpaceApplicationList();
    if (apps != null) {
      for (SpaceApplication spaceApplication : apps) {
        setApp(space, spaceApplication.getPortletName(), spaceApplication.getAppTitle(), spaceApplication.isRemovable(),
            Space.ACTIVE_STATUS);
      }
    }
  }

  @Override
  public void setApp(Space space, String appId, String appName, boolean isRemovable, String status) {
    String apps = space.getApp();
    // an application status is composed with the form of
    // [appId:appName:isRemovableString:status]
    String applicationStatus = appId + ":" + appName;
    if (isRemovable) {
      applicationStatus += ":true";
    } else {
      applicationStatus += ":false";
    }
    applicationStatus += ":" + status;
    if (apps == null) {
      apps = applicationStatus;
    } else {
      apps += "," + applicationStatus;
    }
    space.setApp(apps);
  }

  @Override
  public void start() {
    spaceTemplates = registeredSpaceTemplates;
    for (String spaceTemplateExtensionName : extendedSpaceTemplates.keySet()) {
      SpaceTemplate spaceTemplateExtension = extendedSpaceTemplates.get(spaceTemplateExtensionName);
      List<SpaceApplication> apps = spaceTemplateExtension.getSpaceApplicationList();
      SpaceTemplate toExtendSpaceTemplate = this.spaceTemplates.get(spaceTemplateExtensionName);
      if (toExtendSpaceTemplate == null) {
        LOG.warn("Can't extend Space template '{}' with applications {} because the space template can't be found.",
                 spaceTemplateExtensionName,
                 apps == null ? "" : apps.stream().map(SpaceApplication::getPortletName).collect(Collectors.toList()));
      }
      if (spaceTemplateExtension.getBannerPath() != null) {
        LOG.warn("Banner path defined in extension of space template {} isn't extensible", spaceTemplateExtensionName);
      }
      if (spaceTemplateExtension.getSpaceHomeApplication() != null) {
        LOG.warn("Space home defined in extension of space template {} isn't extensible", spaceTemplateExtensionName);
      }
      if (apps != null && toExtendSpaceTemplate != null) {
        for (SpaceApplication application : apps) {
          toExtendSpaceTemplate.addToSpaceApplicationList(application);
        }
      }
    }
  }

  @Override
  public void stop() {

  }

  private Identity getIdentity(String userId) throws Exception {
    IdentityRegistry identityRegistry = CommonsUtils.getService(IdentityRegistry.class);
    OrganizationService organizationService = CommonsUtils.getService(OrganizationService.class);
    Identity identity = identityRegistry.getIdentity(userId);
    if (identity == null) {
      Collection<Membership> memberships = new ArrayList<>();
      memberships = organizationService.getMembershipHandler().findMembershipsByUser(userId);
      List<MembershipEntry> entries = new ArrayList<>();
      for (Membership membership : memberships) {
        entries.add(new MembershipEntry(membership.getGroupId(), membership.getMembershipType()));
      }
      identity = new Identity(userId, entries);
    }
    return identity;
  }
}
