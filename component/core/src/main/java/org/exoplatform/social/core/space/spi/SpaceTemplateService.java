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
package org.exoplatform.social.core.space.spi;

import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceTemplate;
import org.exoplatform.social.core.space.SpaceTemplateConfigPlugin;
import org.exoplatform.social.core.space.model.Space;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Provides methods to define and word with space template.
 *
 */
public interface SpaceTemplateService {

  /**
   * Gets a list of space templates
   *
   * @return The space templates list.
   *
   */
  List<SpaceTemplate> getSpaceTemplates();

  /**
   * Gets all space templates on which the user has permissions
   *
   * @param userId
   */
  List<SpaceTemplate> getSpaceTemplates(String userId) throws Exception;

  /**
   * Gets all space templates on which the user has permissions with permission labels according to lang
   * If userId is not an administrators, all permissions are empty
   *  @param userId
   * @param lang
   */
  List<SpaceTemplate> getLabelledSpaceTemplates(String userId, String lang) throws Exception;

  /**
   * Gets a space template by name
   *
   * @param name
   * @return The space template.
   *
   */
  SpaceTemplate getSpaceTemplateByName(String name);

  /**
   * Adds space template config plugin for configuring the space applications, visibility, registration and banner.
   * <br>
   *
   * @param spaceTemplateConfigPlugin The space template config plugin to be added.
   */
  void registerSpaceTemplatePlugin(SpaceTemplateConfigPlugin spaceTemplateConfigPlugin);

  /**
   * Extends space template config plugin for extending the space applications.
   * <br>
   *
   * @param spaceTemplateConfigPlugin The space template config plugin to be added.
   */
  void extendSpaceTemplatePlugin(SpaceTemplateConfigPlugin spaceTemplateConfigPlugin);

  /**
   * Adds space application handler for configuring the space applications.
   * <br>
   *
   * @param spaceApplicationHandler The space application handler to be added.
   */
  void registerSpaceApplicationHandler(SpaceApplicationHandler spaceApplicationHandler);

  /**
   * Gets registered space application handlers.
   * <br>
   *
   */
  Map<String, SpaceApplicationHandler> getSpaceApplicationHandlers();

  /**
   * Gets the default space template name.
   * <br>
   *
   */
  String getDefaultSpaceTemplate();

  /**
   * Init space applications.
   *
   * @param space The space..
   */
  void initSpaceApplications(Space space, SpaceApplicationHandler applicationHandler) throws SpaceException;

  /**
   * an application status is composed with the form of:
   * [appId:appDisplayName:isRemovableString:status]. And space app properties is the combined
   * of application statuses separated by a comma (,). For example:
   * space.getApp() ="SpaceSettingPortlet:SpaceSettingPortletName:false:active,MembersPortlet:MembersPortlet:true:active"
   * ;
   *
   * @param space
   * @param appId
   * @param appName
   * @param isRemovable
   * @param status
   */
  void setApp(Space space, String appId, String appName, boolean isRemovable, String status);
}
