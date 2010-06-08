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
package org.exoplatform.social.jcr;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;


/**
 * This class is meant to be the starting for any data storage access in Social.<br/>
 * Provides all JCR pathes usable in Social JCR data storage. <br/>
 * A {@link JCRSessionManager} accessible by {@link #getSessionManager()} is configured on the appropriate repository and workspace.<br/>
 * Relies on {@link NodeHierarchyCreator} to initialize the structure and provide pathes aliases.
 *
 * @author <a href="mailto:tungcnw@gmail.com">dang.tung</a>
 * @version $Revision$
 */
public class SocialDataLocation {

  private static final Log log = ExoLogger.getLogger(SocialDataLocation.class);

  /**
   * Parameter name use to designate the name of the workspace in the repository where the data is stored. Should be passed in constructor's {@link InitParams}
   */
  public static final String WORKSPACE_PARAM = "workspace";

  /**
   * Default repository name used if none is specified
   */
  public static final String DEFAULT_REPOSITORY_NAME = "repository";

  /**
   * Default workspace name used if none is specified
   */
  public static final String DEFAULT_WORKSPACE_NAME = "portal-system";

  private String socialSpaceRoot;
  private String socialSpaceHome;
  private String socialActivitiesHome;
  private String socialIdentityHome;
  private String socialProfileHome;
  private String socialRelationshipHome;

  private NodeHierarchyCreator creator;
  private String workspace;
  private JCRSessionManager sessionManager;
  private RepositoryService repositoryService;

  /**
   * Creates a new {@link SocialDataLocation} and initializes pathes.
   * @param params {@link #REPOSITORY_PARAM} and {@link #WORKSPACE_PARAM} are expected as value-param
   * @param creator used to resolve path names. It is also declared here to ensure that the data structure has been initalized before.
   */
  public SocialDataLocation(InitParams params, NodeHierarchyCreator creator, RepositoryService repositoryService) {
    this.creator = creator;
    this.workspace = getParam(params, WORKSPACE_PARAM, DEFAULT_WORKSPACE_NAME);
    this.repositoryService = repositoryService;
    this.sessionManager = new JCRSessionManager(workspace, repositoryService);
    initPathes();
  }

  /**
   * Initializes all pathes with {@link #getPath(String)}
   */
  private void initPathes() {
     socialSpaceRoot = getPath(Locations.SOCIAL_SPACE_ROOT);
     socialSpaceHome = getPath(Locations.SOCIAL_SPACE_HOME);
     socialActivitiesHome = getPath(Locations.SOCIAL_ACTIVITIES_HOME);
     socialIdentityHome = getPath(Locations.SOCIAL_IDENTITY_HOME);
     socialProfileHome = getPath(Locations.SOCIAL_PROFILE_HOME);
     socialRelationshipHome = getPath(Locations.SOCIAL_RELATIONSHIP_HOME);
  }

  /**
   * Change the storage location. Note that pathes are not reset
   * @param plugin plugin defining repository and workspace location for the data storage
   */
  public void setLocation(DataLocationPlugin plugin) {
    this.workspace = plugin.getWorkspace();
    this.sessionManager = new JCRSessionManager(workspace, repositoryService);
  }

  /**
   * Get a jcr path by location name.
   * @param locationName name of the location such a defined in {@link Locations}
   * @return jcr path corresponding the alias name in {@link NodeHierarchyCreator}.
   * If the creator was not set, returns the locationName.
   * The path returned is relative to root (no leading '/')
   */
  protected String getPath(String locationName) {
    if (creator == null) {
      return locationName;
    }

    String path = creator.getJcrPath(locationName);
    if (path != null) {
      path = path.substring(1);
    }
    return path;
  }

  private String getParam(InitParams params, String name, String defaultValue) {
    String result = null;
    try {
      result = params.getValueParam(name).getValue();
    } catch (Exception e) {
      log.warn("No '"+ name +"' value-param. Using default value: " + defaultValue);
    }

    if (result == null) {
      result = defaultValue;
    }
    return result;
  }

  public String getWorkspace() {
    return workspace;
  }

  public JCRSessionManager getSessionManager() {
    return sessionManager;
  }

  /**
   *
   * @return root path for Social Space data
   */
  public String getSocialSpaceRoot() {
    return socialSpaceRoot;
  }

  /**
   *
   * @return root path for Space data
   */
  public String getSocialSpaceHome() {
    return socialSpaceHome;
  }

  public String getSocialActivitiesHome() {
    return socialActivitiesHome;
  }

  public String getSocialIdentityHome() {
    return socialIdentityHome;
  }

  public String getSocialProfileHome() {
    return socialProfileHome;
  }

  public String getSocialRelationshipHome() {
    return socialRelationshipHome;
  }

  /**
   * Location names for Social data storage
   * @author <a href="mailto:tungcnw@gmail.com">dang.tung</a>
   * @version $Revision$
   */
  public interface Locations {

    public static final String SOCIAL_SPACE_ROOT = "socialSpaceRoot";
    public static final String SOCIAL_SPACE_HOME = "socialSpaceHome";
    public static final String SOCIAL_ACTIVITIES_HOME = "socialActivitiesHome";
    public static final String SOCIAL_IDENTITY_HOME = "socialIdentityHome";
    public static final String SOCIAL_PROFILE_HOME = "socialProfileHome";
    public static final String SOCIAL_RELATIONSHIP_HOME = "socialRelationshipHome";
    public static final String DEFAULT_APPS_LOCATION = "exo:applications";
  }

}
