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
package org.exoplatform.social.core.space;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;

/**
 * Component plugin for configuring default applications to be installed when creating a new space.
 *
 * @author    <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since     Sep 1, 2010
 * @since     1.2.0-GA
 */
public class SpaceApplicationConfigPlugin extends BaseComponentPlugin {

  private static final String SPACE_HOME_PARAM_NAME = "spaceHomeApplication";

  private static final String SPACE_APPLICATION_LIST_PARAM_NAME = "spaceApplicationList";

  private SpaceApplication homeApplication;

  private List<SpaceApplication> spaceApplicationList;

  /**
   * Constructor
   */
  public SpaceApplicationConfigPlugin() {

  }

  /**
   * Constructor with init params
   *
   * @param initParams
   */
  public SpaceApplicationConfigPlugin(InitParams initParams) {
    homeApplication = (SpaceApplication) initParams.getObjectParam(SPACE_HOME_PARAM_NAME).getObject();
    spaceApplicationList = initParams.getObjectParamValues(SpaceApplicationConfigPlugin.class).
            get(0).getSpaceApplicationList();
  }

  /**
   * Sets home space application.
   *
   * @param application
   */
  public void setHomeApplication(SpaceApplication application) {
    homeApplication = application;
  }

  /**
   * Gets home space application.
   *
   * @return
   */
  public SpaceApplication getHomeApplication() {
    return homeApplication;
  }

  /**
   * Adds a space application to space application list.
   *
   * @param spaceApplication
   */
  public void addToSpaceApplicationList(SpaceApplication spaceApplication) {
    if (spaceApplicationList == null) {
      spaceApplicationList = new ArrayList<SpaceApplication>();
    }
    spaceApplicationList.add(spaceApplication);
  }

  /**
   * Sets space application list to be installed.
   *
   * @param applicationList
   */
  public void setSpaceApplicationList(List<SpaceApplication> applicationList) {
    spaceApplicationList = applicationList;
  }

  /**
   * Gets space application list to be installed.
   *
   * @return
   */
  public List<SpaceApplication> getSpaceApplicationList() {
    return spaceApplicationList;
  }

  /**
   * The space application model.
   */
  public static class SpaceApplication {
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
    public void isRemovable(boolean isRemovable) {
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

  }
}

