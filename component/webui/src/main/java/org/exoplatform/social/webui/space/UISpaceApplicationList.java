/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.social.webui.space;

import java.util.*;
import java.util.Map.Entry;

import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;


/**
 * customized from UIApplicationList <br />
 * Created by The eXo Platform SAS
 * @author <a href="mailto:hoatlevan@gmail.com">hoatle</a>
 * @since Nov 09, 2009
 */
@ComponentConfig(
  template = "classpath:groovy/social/webui/space/UISpaceApplicationList.gtmpl",
  events = {
    @EventConfig(listeners = UISpaceApplicationList.SelectCategoryActionListener.class),
    @EventConfig(listeners = UISpaceApplicationList.InstallApplicationActionListener.class)
  }
)
public class UISpaceApplicationList extends UIContainer implements UIPopupComponent {
  private Map<ApplicationCategory, List<Application>> appStore;
  private ApplicationCategory selectedCategory;
  private Space space;

  /**
   * constructor
   */
  public UISpaceApplicationList() {

  }

  /**
   * Gets selected category
   * @return selected category
   */
  public ApplicationCategory getSelectedCategory() {
    return selectedCategory;
  }

  /**
   * Gets default selected category in case no category is selected for template.
   * The default selected category is the first key element in the appStore map.
   * @return default selected category
   */
  public ApplicationCategory getDefaultSelectedCategory() {
    if (appStore.keySet().size() > 0) {
      return appStore.keySet().iterator().next();
    }
    return null;
  }

  /**
   * Sets selected category
   * @param categoryName
   */
  public void setSelectedCategory(String categoryName) {
   Iterator<ApplicationCategory> categoryItr = appStore.keySet().iterator();
   while (categoryItr.hasNext()) {
     ApplicationCategory category = categoryItr.next();
     if (category.getName().equals(categoryName)) {
       selectedCategory = category;
     }
   }
  }

  /**
   * Sets space
   * @param space
   * @throws Exception
   */
  public void setSpace(Space space) throws Exception {
    this.space = space;
    appStore = new LinkedHashMap<ApplicationCategory, List<Application>>();
    Map<ApplicationCategory, List<Application>> gotAppStore = SpaceUtils.getAppStore(space);
    Iterator<Entry<ApplicationCategory, List<Application>>> entrySetItr = gotAppStore.entrySet().iterator();
    ApplicationCategory appCategory;
    List<Application> appList;
    while(entrySetItr.hasNext()) {
      Entry<ApplicationCategory, List<Application>> entrySet = entrySetItr.next();
      appCategory = entrySet.getKey();
      appList = entrySet.getValue();
      if (appList.size() > 0) {
        appStore.put(appCategory, appList);
      }
    }
  }
  /**
   * Gets space
   * @return space
   */
  public Space getSpace() {
    return space;
  }

  /**
   * Gets appStore
   * @return appStore
   */
  public Map<ApplicationCategory, List<Application>> getAppStore() {
    return appStore;
  }

  /**
   * Triggers this action when user selects on category
   * @author hoatle
   *
   */
  static public class SelectCategoryActionListener extends EventListener<UISpaceApplicationList> {

    @Override
    public void execute(Event<UISpaceApplicationList> event) throws Exception {
      String selectedCategory = event.getRequestContext().getRequestParameter(OBJECTID);
      UISpaceApplicationList uiSpaceAppList = event.getSource();
      uiSpaceAppList.setSelectedCategory(selectedCategory);
    }

  }

  /**
   * Triggers this action when user clicks on install button
   * @author hoatle
   */
  static public class InstallApplicationActionListener extends EventListener<UISpaceApplicationList> {
    @Override
    public void execute(Event<UISpaceApplicationList> event) throws Exception {
      String appId = event.getRequestContext().getRequestParameter(OBJECTID);
      UISpaceApplicationList uiSpaceAppList = event.getSource();
      SpaceService spaceService = uiSpaceAppList.getApplicationComponent(SpaceService.class);
      spaceService.installApplication(uiSpaceAppList.space, appId);
      spaceService.activateApplication(uiSpaceAppList.space, appId);
      UISpaceApplication uiSpaceApp = (UISpaceApplication) uiSpaceAppList.getAncestorOfType(UISpaceApplication.class);
      uiSpaceApp.setValue(uiSpaceAppList.space);
      SpaceUtils.updateUIWorkspace(Arrays.asList("UIMySpacePlatformToolBarPortlet", "SpacesToolbarPortlet", "SpaceMenuPortlet", "SpaceSettingPortlet"));
    }

  }
  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

}
