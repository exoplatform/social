/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.exoplatform.social.extras.migration;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeModel;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.space.SpaceUtils;

import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class TemplateTool {

  private final PortalContainer container;
  private final OrganizationService service;
  private final NavigationService navigationService;

  private final String UI_SPACE_NEW_TEMPLATE = "system:/groovy/portal/webui/container/UIContainer.gtmpl";

  private static final Log LOG = ExoLogger.getLogger(TemplateTool.class);

  public TemplateTool() {

    this.container = PortalContainer.getInstance();
    this.service = (OrganizationService) container.getComponentInstanceOfType(OrganizationService.class);
    this.navigationService = (NavigationService) container.getComponentInstanceOfType(NavigationService.class);

  }

  public void run() {

    try {

      //
      RequestLifeCycle.begin(PortalContainer.getInstance());

      //
      GroupHandler groupHandler = service.getGroupHandler();
      Group spaces = groupHandler.findGroupById("/spaces");
      Collection<Group> groups = groupHandler.findGroups(spaces);

      for (Group group : groups) {

        // update template
        Query query = new Query<Page>("group", group.getId(), null, null, Page.class);
        DataStorage dataStorage = SpaceUtils.getDataStorage();
        List<Page> pages = dataStorage.find(query).getAll();
        for (Page page : pages) {
          if (page.getChildren().size() > 0) {
            ModelObject modelObject = page.getChildren().get(0);
            if (modelObject instanceof Container) {
              Container container = (Container) modelObject;
              if (!UI_SPACE_NEW_TEMPLATE.equals(container.getTemplate())) {
  
                container.setTemplate(UI_SPACE_NEW_TEMPLATE);
                dataStorage.save(page);
                LOG.info("Update template for " + group.getId() + " : " + page.getTitle());
  
              }
              else {
                LOG.info("Skip template for " + group.getId() + " : " + page.getTitle() + " (already done)");
              }
            }
          }
        }

        // update settings name
        NavigationContext pageNavCtx = navigationService.loadNavigation(SiteKey.group(group.getId()));
        NodeContext nodeCtx = navigationService.loadNode(NodeModel.SELF_MODEL, pageNavCtx, Scope.GRANDCHILDREN, null).getNode();
        LOG.info("Check navigations for : " + group.getId());
        for (NodeContext ctx : (Collection<NodeContext>) nodeCtx.getNodes()) {
          for (NodeContext ctxPage : (Collection<NodeContext>) ctx.getNodes()) {
            if (
                "SpaceSettingPortlet".equals(ctxPage.getName()) ||
                "Space_Settings".equals(ctxPage.getName()) ||
                "Space_settings".equals(ctxPage.getName())) {
              
              ctxPage.setName("settings");
              navigationService.saveNode(ctxPage, null);
              LOG.info("Update setting page name : " + ctxPage.getName());

            }
          }
        }

      }

    }
    catch (Exception e) {
      LOG.info("Error during template migration : " + e.getMessage(), e);
    }
    finally {
      RequestLifeCycle.end();
    }

  }

}