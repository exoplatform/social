/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.social.webui;

import java.util.List;

import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.webui.UIApplicationCategorySelector.SelectActionListener;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;

/**
 * The ui component is used to display a list of application category and
 * broadcasts the select event listener to parent ui.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since  1.2.8
 * @since  Jan 17, 2012
 */
@ComponentConfig(
 template = "war:/groovy/social/webui/UIApplicationCategorySelector.gtmpl",
 events = {
   @EventConfig(listeners = SelectActionListener.class)
 }
)
public class UIApplicationCategorySelector extends UIContainer {

  /**
   * The logger.
   */
  private static final Log LOG = ExoLogger.getLogger(UIApplicationCategorySelector.class);

  /**
   * The application category list.
   */
  private List<ApplicationCategory> applicationCategoryList;

  /**
   * The selected application category.
   */
  private ApplicationCategory selectedApplicationCategory;


  /**
   * Sets the application category list to be displayed.
   *
   * @param newApplicationCategoryList the application category list.
   */
  public final void setApplicationCategoryList(List<ApplicationCategory> newApplicationCategoryList) {
    this.applicationCategoryList = newApplicationCategoryList;
  }

  /**
   * Gets the application category list.
   *
   * @return the application category list.
   */
  public final List<ApplicationCategory> getApplicationCategoryList() {
    return this.applicationCategoryList;
  }

  /**
   * Sets the selected application category.
   *
   * @param newApplicationCategory the selected application category.
   */
  public final void setSelectedApplicationCategory(ApplicationCategory newApplicationCategory) {
    this.selectedApplicationCategory = newApplicationCategory;
  }

  /**
   * Gets the selected application category.
   *
   * @return the selected application category.
   */
  public final ApplicationCategory getSelectedApplicationCategory() {
    if (this.selectedApplicationCategory == null) {
      this.selectedApplicationCategory = applicationCategoryList.get(0);
    }
    return this.selectedApplicationCategory;
  }

  /**
   * The event listener for "Select" action.
   * Sets the selected application category and try to broadcast to parent ui component.
   */
  public static class SelectActionListener extends EventListener<UIApplicationCategorySelector> {

    @Override
    public void execute(Event<UIApplicationCategorySelector> event) throws Exception {
      UIApplicationCategorySelector uiApplicationCategorySelector = event.getSource();
      String categoryName = event.getRequestContext().getRequestParameter(OBJECTID);
      uiApplicationCategorySelector.setSelectedApplicationCategory(categoryName);
      if (uiApplicationCategorySelector.getParent() != null) {
        Event<UIComponent> selectEvent = uiApplicationCategorySelector.
                                             getParent().createEvent("Select", Phase.DECODE, event.getRequestContext());
        if (selectEvent == null) {
          LOG.warn("Failed to broadcast SelectItemActionListener.");
          return;
        }
        selectEvent.broadcast();
      }
    }
  }

  /**
   * Sets the selected application category by category name.
   *
   * @param categoryName the application category name.
   */
  private void setSelectedApplicationCategory(String categoryName) {
    if (getSelectedApplicationCategory().getName().equals(categoryName)) {
      return;
    }
    for (ApplicationCategory applicationCategory : applicationCategoryList) {
      if (categoryName.equals(applicationCategory.getName())) {
        setSelectedApplicationCategory(applicationCategory);
        break;
      }
    }
  }

}
