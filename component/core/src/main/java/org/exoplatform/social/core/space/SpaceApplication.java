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

/**
 * Definition of space application model.
 *
 */
import java.util.Map;

public class SpaceApplication implements Cloneable {
  private String portletApp;
  private String portletName;
  private String appTitle;
  private boolean removable;
  private int order;
  private String uri;
  private String icon;
  private Map<String, String> preferences;

  /**
   * Sets the portletApp - the war name file which has that portlet.
   *
   * @param portletApp
   */
  public void setPortletApp(String portletApp) {
    this.portletApp = portletApp;
  }

  /**
   * Gets the portletApp.
   *
   * @return
   */
  public String getPortletApp() {
    return portletApp;
  }

  /**
   * Sets portletName.
   *
   * @param portletName
   */
  public void setPortletName(String portletName) {
    this.portletName = portletName;
  }

  /**
   * Gets portletName.
   *
   * @return
   */
  public String getPortletName() {
    return portletName;
  }

  /**
   * Sets appTitle to be displayed on space's navigation.
   *
   * @param appTitle
   */
  public void setAppTitle(String appTitle) {
    this.appTitle = appTitle;
  }

  /**
   * Gets appTitle to be displayed on space's navigation.
   *
   * @return
   */
  public String getAppTitle() {
    return appTitle;
  }

  /**
   * Indicates if this application is removable or not.
   *
   * @param isRemovable
   */
  public void setRemovable(boolean isRemovable) {
    this.removable = isRemovable;
  }

  /**
   * Checks if this application is removable for not.
   *
   * @return
   */
  public boolean isRemovable() {
    return removable;
  }

  /**
   * Sets the order in the space's navigation.
   *
   * @param order
   */
  public void setOrder(int order) {
    this.order = order;
  }

  /**
   * Gets the order in the space's navigation.
   *
   * @return
   */
  public int getOrder() {
    return order;
  }

  /**
   * Sets the uri of this application page node.
   *
   * @param uri
   */
  public void setUri(String uri) {
    this.uri = uri;
  }

  /**
   * Gets the uri of the application page node.
   *
   * @return
   */
  public String getUri() {
    return uri;
  }

  /**
   * Sets the icon class for the application page node.
   *
   * @param icon
   */
  public void setIcon(String icon) {
    this.icon = icon;
  }

  /**
   * Gets the icon class for the application page node.
   * @return
   */
  public String getIcon() {
    return icon;
  }

  /**
   * Sets preferences for this application when installed.
   *
   * @param preferences
   */
  public void setPreferences(Map<String, String> preferences) {
    this.preferences = preferences;
  }

  /**
   * Gets preferences for this application when installed.
   *
   * @return
   */
  public Map<String, String> getPreferences() {
    return this.preferences;
  }

  @Override
  public SpaceApplication clone() {
    try {
      SpaceApplication spaceApplication = (SpaceApplication) super.clone();
      return spaceApplication;
    } catch (CloneNotSupportedException e) {
      return null;
    }
  }
}
