/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.portlet.PortletPreferences;
import javax.servlet.http.HttpServletRequest;

import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.pom.config.Utils;
import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.gatein.common.i18n.LocalizedString;
import org.gatein.common.util.Tools;
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletInvoker;
import org.gatein.pc.api.info.MetaInfo;
import org.gatein.pc.api.info.PortletInfo;

import com.ibm.icu.text.Transliterator;

/**
 * SpaceUtils Utility for working with space
 */
public class SpaceUtils {
  static private final Log                  LOG                                  = ExoLogger.getLogger(SpaceUtils.class);

  static public final String                SPACE_GROUP                          = "/spaces";

  static public final String                PLATFORM_USERS_GROUP                 = "/platform/users";

  static public final String                MANAGER                              = "manager";

  static public final String                MENU_CONTAINER                       = "Menu";

  static public final String                APPLICATION_CONTAINER                = "Application";

  static public final String                SPACE_URL                            = "SPACE_URL";

  static private final String               PORTLET_STR                          = "portlet";

  static private final String               GADGET_STR                           = "gadget";

  static private final ConcurrentHashMap<String, Application> appListCache       = new ConcurrentHashMap<String, Application>();

  private static final String               REMOTE_CATEGORY_NAME                 = "remote";
  
  // A {@link Transliterator} instance is stateless which has for consequences that it is Thread Safe and thus can be shared among several threads as mentioned in 
  // the javadoc
  private static final Transliterator       ACCENTS_CONVERTER                     = Transliterator.getInstance("Latin; NFD; [:Nonspacing Mark:] Remove; NFC;");
  
  /**
   * Creates a new group from an existing group. This new group will get all
   * data from existing group except for group name
   *
   * @param parentGroup
   * @param existingGroup
   * @param name
   * @return new Group
   * @throws Exception
   * @deprecated to be removed by 1.2.x
   */
  @SuppressWarnings("unchecked")
  static public Group createGroupFromExistingGroup(Group parentGroup,
                                                   Group existingGroup,
                                                   String name) throws Exception {
    OrganizationService orgSrc = getOrganizationService();
    GroupHandler groupHandler = orgSrc.getGroupHandler();
    MembershipHandler memberShipHandler = orgSrc.getMembershipHandler();
    Group newGroup = groupHandler.createGroupInstance();
    newGroup.setGroupName(name);
    newGroup.setLabel(name);
    newGroup.setDescription(existingGroup.getDescription());
    groupHandler.addChild(parentGroup, newGroup, true);
    Collection<Membership> memberShips = memberShipHandler.findMembershipsByGroup(existingGroup);
    Iterator<Membership> itr = memberShips.iterator();
    while (itr.hasNext()) {
      Membership membership = itr.next();
      User user = orgSrc.getUserHandler().findUserByName(membership.getUserName());
      MembershipType memberShipType = orgSrc.getMembershipTypeHandler()
                                            .findMembershipType(membership.getMembershipType());
      memberShipHandler.linkMembership(user, newGroup, memberShipType, true);
    }
    return newGroup;
  }

  /**
   * Gets applications that a group has right to access
   *
   * @param groupId
   * @return applications
   * @throws Exception
   * @deprecated
   */
  static public List<Application> getApplications(String groupId) throws Exception {

    List<Application> list = new CopyOnWriteArrayList<Application>();
    ApplicationRegistryService appRegistrySrc = getApplicationRegistryService();

    List<ApplicationCategory> listCategory = appRegistrySrc.getApplicationCategories();
    Iterator<ApplicationCategory> cateItr = listCategory.iterator();
    while (cateItr.hasNext()) {
      ApplicationCategory cate = cateItr.next();
      if (!hasAccessPermission(cate, groupId)) {
        cateItr.remove();
        continue;
      }
      ApplicationType<org.exoplatform.portal.pom.spi.portlet.Portlet> portletType = ApplicationType.PORTLET;
      List<Application> applications = appRegistrySrc.getApplications(cate, portletType);
      Iterator<Application> appIterator = applications.iterator();
      while (appIterator.hasNext()) {
        Application app = appIterator.next();
        if (!hasAccessPermission(app, groupId)) {
          appIterator.remove();
        } else {
          list.add(app);
        }
      }
    }
    return list;
  }

  /**
   * Gets appStore of HashMap type with key = ApplicationCategory and value =
   * list of applications. appStore is filter by access permission from that
   * group; filter by application category access permission and filtered by
   * application permission.
   *
   * @param space
   * @return appStore
   * @throws Exception
   */
  static public Map<ApplicationCategory, List<Application>> getAppStore(Space space) throws Exception {
    Map<ApplicationCategory, List<Application>> appStore = new LinkedHashMap<ApplicationCategory, List<Application>>();
    ApplicationRegistryService appRegistryService = getApplicationRegistryService();
    String groupId = space.getGroupId();
    List<ApplicationCategory> categoryList = appRegistryService.getApplicationCategories();
    Collections.sort(categoryList, new PortletCategoryComparator());
    Iterator<ApplicationCategory> cateItr = categoryList.iterator();
    while (cateItr.hasNext()) {
      ApplicationCategory appCategory = cateItr.next();
      if (!hasAccessPermission(appCategory, groupId)) {
        continue;
      }
      List<Application> tempAppList = new ArrayList<Application>();
      List<Application> appList = appCategory.getApplications();
      Collections.sort(appList, new PortletComparator());
      Iterator<Application> appItr = appList.iterator();
      while (appItr.hasNext()) {
        Application application = appItr.next();
        if (!hasAccessPermission(application, groupId)) {
          continue;
        }
        tempAppList.add(application);
      }
      if (tempAppList.size() > 0) {
        appStore.put(appCategory, tempAppList);
      }
    }
    return appStore;
  }

  /**
   * Gets application from portal container. This is used to get application
   * when get application by applicationRegistry return null.
   *
   * @param appId
   * @return An application has name match input appId.
   * @throws Exception
   */
  static public Application getAppFromPortalContainer(String appId) throws Exception {
    if (appListCache.containsKey(appId)) {
      return appListCache.get(appId);
    }

    ExoContainer container = ExoContainerContext.getCurrentContainer();

    PortletInvoker portletInvoker = (PortletInvoker) container.getComponentInstance(PortletInvoker.class);
    Set<Portlet> portlets = portletInvoker.getPortlets();
    ApplicationRegistryService appRegistryService = getApplicationRegistryService();
    for (Portlet portlet : portlets) {
      PortletInfo info = portlet.getInfo();
      String portletApplicationName = info.getApplicationName();
      String portletName = info.getName();

      portletApplicationName = portletApplicationName.replace('/', '_');
      portletName = portletName.replace('/', '_');

      if (portletName.equals(appId)) {
        LocalizedString keywordsLS = info.getMeta().getMetaValue(MetaInfo.KEYWORDS);

        String[] categoryNames = null;
        if (keywordsLS != null) {
          String keywords = keywordsLS.getDefaultString();
          if (keywords != null && keywords.length() != 0) {
            categoryNames = keywords.split(",");
          }
        }

        if (categoryNames == null || categoryNames.length == 0) {
          categoryNames = new String[] { portletApplicationName };
        }

        if (portlet.isRemote()) {
          categoryNames = Tools.appendTo(categoryNames, REMOTE_CATEGORY_NAME);
        }

        for (String categoryName : categoryNames) {
          ApplicationCategory category;

          categoryName = categoryName.trim();

          category = appRegistryService.getApplicationCategory(categoryName);
          if (category == null) {
            category = new ApplicationCategory();
            category.setName(categoryName);
            category.setDisplayName(categoryName);
          }

          Application app = appRegistryService.getApplication(categoryName + "/" + portletName);
          if (app != null)
            return app;

          // ContentType<?> contentType;
          String contentId;

          LocalizedString descriptionLS = portlet.getInfo()
                                                 .getMeta()
                                                 .getMetaValue(MetaInfo.DESCRIPTION);
          LocalizedString displayNameLS = portlet.getInfo()
                                                 .getMeta()
                                                 .getMetaValue(MetaInfo.DISPLAY_NAME);

          getLocalizedStringValue(descriptionLS, portletName);

          app = new Application();

          if (portlet.isRemote()) {
            // contentType = WSRP.CONTENT_TYPE;
            contentId = portlet.getContext().getId();
          } else {
            contentId = info.getApplicationName() + "/" + info.getName();
          }
          app.setType(ApplicationType.PORTLET);
          app.setContentId(contentId);
          app.setApplicationName(portletName);
          app.setCategoryName(categoryName);
          app.setDisplayName(getLocalizedStringValue(displayNameLS, portletName));
          app.setDescription(getLocalizedStringValue(descriptionLS, portletName));
          Application oldApp = appListCache.putIfAbsent(app.getApplicationName(), app);
          return oldApp == null ? app : oldApp;
        }
      }
    }

    return null;
  }

  /**
   * PortletCategoryComparator
   */
  static class PortletCategoryComparator implements Comparator<ApplicationCategory> {
    public int compare(ApplicationCategory cat1, ApplicationCategory cat2) {
      return cat1.getDisplayName().compareTo(cat2.getDisplayName());
    }
  }

  /**
   * PortletComparator
   */
  static class PortletComparator implements Comparator<Application> {
    public int compare(Application p1, Application p2) {
      return p1.getDisplayName().compareTo(p2.getDisplayName());
    }
  }

  /**
   * Utility for cleaning space name
   *
   * @param str
   * @return cleaned string
   */
  static public String cleanString(String str) {

    str = ACCENTS_CONVERTER.transliterate(str);

    // the character ? seems to not be changed to d by the transliterate
    // function

    StringBuilder cleanedStr = new StringBuilder(str.trim());
    // delete special character
    for (int i = 0; i < cleanedStr.length(); i++) {
      char c = cleanedStr.charAt(i);
      if (c == ' ') {
        if (i > 0 && cleanedStr.charAt(i - 1) == '_') {
          cleanedStr.deleteCharAt(i--);
        } else {
          c = '_';
          cleanedStr.setCharAt(i, c);
        }
        continue;
      }

      if (!(Character.isLetterOrDigit(c) || c == '_')) {
        cleanedStr.deleteCharAt(i--);
        continue;
      }

      if (i > 0 && c == '_' && cleanedStr.charAt(i - 1) == '_')
        cleanedStr.deleteCharAt(i--);
    }
    return cleanedStr.toString().toLowerCase();
  }

  /**
   * Utility for getting space url based on url address
   */
  /*
   * static public String getSpaceUrl() { PageNode selectedNode = null; try {
   * selectedNode = Util.getUIPortal().getSelectedNode(); } catch (Exception e)
   * { // TODO Auto-generated catch block e.printStackTrace(); } String spaceUrl
   * = selectedNode.getUri(); if (spaceUrl.contains("/")) { spaceUrl =
   * spaceUrl.split("/")[0]; } return spaceUrl; }
   */

  /**
   * Gets spaceName by portletPreference.
   *
   * @return
   */
  static public String getSpaceUrl() {
    PortletRequestContext pcontext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences pref = pcontext.getRequest().getPreferences();
    return pref.getValue(SPACE_URL, "");
  }

  /**
   * change spaceUrl preferences for all applications in a pageNode. This
   * pageNode is the clonedPage of spacetemplate.
   *
   * @param spacePageNode
   * @param space
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  static public void changeSpaceUrlPreference(PageNode spacePageNode,
                                              Space space,
                                              String newSpaceName) throws Exception {
    String pageId = spacePageNode.getPageReference();
    DataStorage dataStorage = getDataStorage();
    Page page = dataStorage.getPage(pageId);
    page.setTitle(page.getTitle().replace(space.getName(), newSpaceName));
    dataStorage.save(page);
    ArrayList<ModelObject> pageChildren = page.getChildren();
    Container menuContainer = findContainerById(pageChildren, MENU_CONTAINER);

    org.exoplatform.portal.config.model.Application<org.exoplatform.portal.pom.spi.portlet.Portlet> menuPortlet = (org.exoplatform.portal.config.model.Application<org.exoplatform.portal.pom.spi.portlet.Portlet>) menuContainer.getChildren()
                                                                                                                                                                                                                                 .get(0);

    ApplicationState<org.exoplatform.portal.pom.spi.portlet.Portlet> menuState = menuPortlet.getState();
    org.exoplatform.portal.pom.spi.portlet.Portlet menuPortletPreference;
    try {
      menuPortletPreference = dataStorage.load(menuState, ApplicationType.PORTLET);
      menuPortletPreference.setValue(SPACE_URL, space.getUrl());
      dataStorage.save(menuState, menuPortletPreference);
    } catch (Exception e) {
      LOG.warn("Can not save menu portlet preference!");
      e.printStackTrace();
    }

    Container applicationContainer = findContainerById(pageChildren, APPLICATION_CONTAINER);

    try {
      org.exoplatform.portal.config.model.Application<org.exoplatform.portal.pom.spi.portlet.Portlet> applicationPortlet = (org.exoplatform.portal.config.model.Application<org.exoplatform.portal.pom.spi.portlet.Portlet>) applicationContainer.getChildren()
                                                                                                                                                                                                                                                 .get(0);
      ApplicationState<org.exoplatform.portal.pom.spi.portlet.Portlet> appState = applicationPortlet.getState();
      org.exoplatform.portal.pom.spi.portlet.Portlet appPortletPreference;
      try {
        appPortletPreference = dataStorage.load(appState, ApplicationType.PORTLET);
        if (appPortletPreference == null || appPortletPreference.getPreference(SPACE_URL) == null) {
          return;
        } else {
          appPortletPreference.setValue(SPACE_URL, space.getUrl());
        }
        dataStorage.save(appState, appPortletPreference);
      } catch (Exception e) {
        LOG.warn("Can not save application portlet preference!");
        e.printStackTrace();
      }

    } catch (Exception e) {
      // ignore it? exception will happen when this is gadgetApplicationType
    }
  }

  /**
   * Finds container by id
   *
   * @param children
   * @param id
   * @return
   */
  public static Container findContainerById(ArrayList<ModelObject> children, String id) {
    Container found = null;
    for (Object obj : children) {
      if (org.exoplatform.portal.config.model.Application.class.isInstance(obj))
        continue;
      Container child = (Container) obj;
      if (child.getId() == null) {
        found = findContainerById(child.getChildren(), id);
        if (found != null)
          return found;
      } else {
        if (child.getId().equals(id))
          return child;
        else
          found = findContainerById(child.getChildren(), id);
        if (found != null)
          return found;
      }
    }
    return found;
  }

  /**
   * Utility for setting navigation. Set pageNavigation, if existed in portal
   * navigations, reset; if not, added to portal navigations.
   *
   * @param nav
   */
  static public void setNavigation(PageNavigation nav) {
    if (nav == null)
      return;
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    if (context == null)
      return;
    UIPortalApplication uiPortalApplication = Util.getUIPortalApplication();
    try {
      UserPortalConfig userPortalConfig = uiPortalApplication.getUserPortalConfig();
      List<PageNavigation> navs = userPortalConfig.getNavigations();
      PageNavigation selectedNav = Util.getUIPortal().getSelectedNavigation();
      if (selectedNav.getId() == nav.getId()) {
        Util.getUIPortal().setSelectedNavigation(nav);
      }
      boolean alreadyExisted = false;
      for (int i = 0; i < navs.size(); i++) {
        if (navs.get(i).getId() == nav.getId()) {
          navs.set(i, nav);
          alreadyExisted = true;
          return;
        }
      }
      if (!alreadyExisted) {
        navs.add(nav);
      }
      userPortalConfig.setNavigations(navs);
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
    }
  }

  /**
   * Utility for removing portal navigation.
   *
   * @param nav
   * @throws Exception
   */
  static public void removeNavigation(PageNavigation nav) throws Exception {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    if (context == null) return;
    UserPortalConfig userPortalConfig = Util.getUIPortalApplication().getUserPortalConfig();
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    DataStorage dataStorage = (DataStorage) container.getComponentInstanceOfType(DataStorage.class);
    if (dataStorage == null) {
      LOG.warn("dataStorage is null!");
      return;
    }

    List<PageNavigation> navs = userPortalConfig.getNavigations();
    navs.remove(nav);
    try {
      userPortalConfig.setNavigations(navs);
    } catch (Exception e) {
      e.printStackTrace();
    }
    // TODO: should be removed and implement by each implementation.
    // current page not existed, broadcast to default home node
    // String portalOwner = Util.getPortalRequestContext().getPortalOwner();
    // PageNavigation portalNavigation = null;
    // for (PageNavigation pn: navs) {
    // if (pn.getOwnerId().equals(portalOwner)) {
    // portalNavigation = pn;
    // break;
    // }
    // }
    // List<PageNode> nodes = portalNavigation.getNodes();
    // PageNode selectedPageNode = nodes.get(0);
    // PortalRequestContext prContext = Util.getPortalRequestContext();
    // String redirect = prContext.getPortalURI() + selectedPageNode.getUri();
    // prContext.getResponse().sendRedirect(redirect);
    // prContext.setResponseComplete(true);
  }

  /**
   * Reload all portal navigations synchronizing navigations and stores in
   * currentAccessibleSpaces BUG: SOC-406, SOC-134
   *
   * @throws Exception
   */
  public static void reloadNavigation() throws Exception {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    SpaceService  spaceService = (SpaceService) container.getComponentInstanceOfType(SpaceService.class);
    String userId = Util.getPortalRequestContext().getRemoteUser();
    List<Space> spaces = spaceService.getAccessibleSpaces(userId);
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    if (context == null) return;
    UserPortalConfig userPortalConfig = Util.getUIPortalApplication().getUserPortalConfig();
    List<PageNavigation> navs = userPortalConfig.getNavigations();
    List<PageNavigation> spaceNavs = new ArrayList<PageNavigation>();
    String ownerId;
    for (PageNavigation nav : navs) {
      ownerId = nav.getOwnerId();
      try {
        Space space = getSpaceByGroupId(ownerId);
        if (space != null) {
          spaceNavs.add(nav);
        }
      } catch (Exception e) {
        // it does not exist, just ignore
      }
    }
    String groupId;
    // add new space navigation
    boolean spaceContained = false;
    for (Space space : spaces) {
      groupId = space.getGroupId();
      for (PageNavigation nav : spaceNavs) {
        if (groupId.equals(nav.getOwnerId())) {
          spaceContained = true;
          break;
        }
      }
      if (spaceContained == false) {
        setNavigation(getGroupNavigation(groupId));
      }
    }
    // remove deleted space navigation
    if (spaces.size() == 0) {
      // remove all navs
      for (PageNavigation nav : spaceNavs) {
        removeNavigation(nav);
      }
    } else {
      boolean navContained = false;
      for (PageNavigation nav : spaceNavs) {
        for (Space space : spaces) {
          groupId = space.getGroupId();
          if (groupId.equals(nav.getOwnerId())) {
            navContained = true;
            break;
          }
        }
        if (navContained == false) {
          removeNavigation(nav);
        }
      }
    }
  }

  /**
   * Gets space by groupId TODO: Move to SpaceService?
   *
   * @param groupId
   * @return
   * @throws SpaceException
   */
  private static Space getSpaceByGroupId(String groupId) throws SpaceException {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    SpaceService spaceService = (SpaceService) container.getComponentInstanceOfType(SpaceService.class);
    return spaceService.getSpaceByGroupId(groupId);
  }

  /**
   * Updates working work space
   */
  static public void updateWorkingWorkSpace() {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    if (context == null) return;
    UIPortalApplication uiPortalApplication = Util.getUIPortalApplication();
    UIWorkingWorkspace uiWorkingWS = uiPortalApplication.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
    PortalRequestContext pContext = Util.getPortalRequestContext();
    pContext.addUIComponentToUpdateByAjax(uiWorkingWS);
    pContext.setFullRender(true);
    // UIPortalApplication uiPortalApp = Util.getUIPortalApplication();
    // UIWorkingWorkspace uiWorkingWS =
    // uiPortalApp.getChild(UIWorkingWorkspace.class);
    // try {
    // uiWorkingWS.updatePortletsByName("UserToolbarGroupPortlet");
    // } catch (Exception e) {
    // // TODO: handle exception
    // }
  }

  /**
   * Update UIComponents by name in current WorkingWorkSpace
   * @param uiComponentsName List of UIComponent name which need to update
   * @throws Exception
   */
  static public void updateUIWorkspace(List<String> uiComponentsName) throws Exception {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    if (context == null) return;
    UIPortalApplication uiPortalApplication = Util.getUIPortalApplication();
    UIWorkingWorkspace uiWorkingWS = uiPortalApplication.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
    updatePortletsByNames(uiWorkingWS, uiComponentsName);
  }

  private static void updatePortletsByNames(UIWorkingWorkspace uiWorkingWS, List<String> componentNames){
     List<UIPortlet> portletInstancesInPage = new ArrayList<UIPortlet>();
    uiWorkingWS.findComponentOfType(portletInstancesInPage, UIPortlet.class);

     for (UIPortlet portlet : portletInstancesInPage)
     {
        String applicationId = portlet.getApplicationId();
        ApplicationType<?> type = portlet.getState().getApplicationType();
        if (type == ApplicationType.PORTLET)
        {
           String[] chunks = Utils.split("/", applicationId);
           if (componentNames.contains(chunks[1]))
           {
              Util.getPortalRequestContext().addUIComponentToUpdateByAjax(portlet);
           }
        }
        else if (type == ApplicationType.GADGET)
        {
           if (componentNames.contains(applicationId))
           {
              Util.getPortalRequestContext().addUIComponentToUpdateByAjax(portlet);
           }
        }
        else
        {
           throw new AssertionError("Need to handle wsrp case later");
        }
     }
  }
  /**
   * Creates new group in /Spaces node and return groupId
   *
   * @param spaceName String
   * @param creator String
   * @return groupId String
   * @throws SpaceException
   */
  static public String createGroup(String spaceName, String creator) throws SpaceException {

    OrganizationService organizationService = getOrganizationService();
    GroupHandler groupHandler = organizationService.getGroupHandler();
    Group parentGroup;
    Group newGroup;
    String groupId;
    String shortName;
    
    if (isSpaceNameExisted(spaceName)) {
      throw new SpaceException(SpaceException.Code.SPACE_ALREADY_EXIST);
    }
    
    try {
      parentGroup = groupHandler.findGroupById(SPACE_GROUP);
      // Creates new group
      newGroup = groupHandler.createGroupInstance();
      shortName = SpaceUtils.cleanString(spaceName);
      groupId = parentGroup.getId() + "/" + shortName;

      newGroup.setGroupName(shortName);
      newGroup.setLabel(spaceName);
      newGroup.setDescription("the " + parentGroup.getId() + "/" + shortName + " group");
      groupHandler.addChild(parentGroup, newGroup, true);
    } catch (Exception e) {
      if (e instanceof SpaceException) {
        throw (SpaceException) e;
      }
      throw new SpaceException(SpaceException.Code.UNABLE_TO_CREATE_GROUP, e);
    }

    try {
      // adds user as creator (manager)
      addCreatorToGroup(creator, groupId);
    } catch (Exception e) {
      // TODO:should rollback what has to be rollback here
      throw new SpaceException(SpaceException.Code.UNABLE_TO_ADD_CREATOR, e);
    }
    return groupId;
  }

  /**
   * Removes a group owning a space
   *
   * @param space
   * @throws SpaceException
   */
  static public void removeGroup(Space space) throws SpaceException {
    try {
      OrganizationService organizationService = getOrganizationService();
      GroupHandler groupHandler = organizationService.getGroupHandler();
      Group group = groupHandler.findGroupById(space.getGroupId());
      groupHandler.removeGroup(group, true);
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_REMOVE_GROUP, e);
    }
  }

  /**
   * Checks if a space or a group already exists
   *
   * @param spaceName
   * @return boolean if existed return true, else return false
   * @throws SpaceException with code INTERNAL_SERVER_ERROR
   */
  static public boolean isSpaceNameExisted(String spaceName) throws SpaceException {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    OrganizationService organizationService = getOrganizationService();
    GroupHandler groupHandler = organizationService.getGroupHandler();
      
    if (context == null) return false;
    String spacePrettyName = cleanString(spaceName); // Compares with Existing DashBoard Tabs's & Spaces's Names
    String groupId = null;
    try {
      groupId = groupHandler.findGroupById(SPACE_GROUP).getId() + "/" + spacePrettyName;
    } catch (Exception e) {
      throw (SpaceException) e;
    }
    
    if (getSpaceByGroupId(groupId) != null) return true;
  
    List<PageNavigation> allNavs = Util.getUIPortalApplication().getUserPortalConfig().getNavigations();
    
    for (PageNavigation pn : allNavs) {
      if (groupId.equals(pn.getOwnerId())) return false;
      ArrayList<PageNode> nodes = pn.getNodes();
      for (PageNode node : nodes) {
        if (node.getName().equals(spacePrettyName)) return true;
      }
    }
    
    return false;
  }
  
  /**
   * When user chooses an existing group, that user will be added to that group
   * as a manager
   *
   * @param creator String
   * @param groupId String
   * @throws SpaceException with code UNABLE_TO_ADD_CREATOR
   */
  static public void addCreatorToGroup(String creator, String groupId) throws SpaceException {
    PortalContainer portalContainer = PortalContainer.getInstance();
    OrganizationService organizationService = (OrganizationService) portalContainer.getComponentInstanceOfType(OrganizationService.class);
    try {
      // TODO: checks whether user is already manager?
      GroupHandler groupHandler = organizationService.getGroupHandler();
      Group existingGroup = groupHandler.findGroupById(groupId);
      User user = organizationService.getUserHandler().findUserByName(creator);
      MembershipType membershipType = organizationService.getMembershipTypeHandler()
                                                         .findMembershipType(MANAGER);
      organizationService.getMembershipHandler().linkMembership(user,
                                                                existingGroup,
                                                                membershipType,
                                                                true);

    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_ADD_CREATOR, e);
    }
  }

  /**
   * Creates group navigation if not existed or return existing group navigation
   * based on groupId
   *
   * @param groupId String
   * @return spaceNav PageNavigation
   * @throws SpaceException
   */
  static public PageNavigation createGroupNavigation(String groupId) throws SpaceException {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    DataStorage dataStorage = (DataStorage) container.getComponentInstance(DataStorage.class);
    PageNavigation spaceNav;
    try {
      spaceNav = dataStorage.getPageNavigation(PortalConfig.GROUP_TYPE, groupId);
      if (spaceNav == null) {
        // creates new space navigation
        spaceNav = new PageNavigation();
        spaceNav.setOwnerType(PortalConfig.GROUP_TYPE);
        spaceNav.setOwnerId(groupId);
        spaceNav.setModifiable(true);
        dataStorage.create(spaceNav);
      }
      return spaceNav;
    } catch (Exception e) {
      // TODO:should rollback what has to be rollback here
      throw new SpaceException(SpaceException.Code.UNABLE_TO_CREAT_NAV, e);
    }
  }

  /**
   * Removes group navigations.
   *
   * @param groupId
   * @throws SpaceException
   */
  static public void removeGroupNavigation(String groupId) throws SpaceException {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    DataStorage dataStorage = (DataStorage) container.getComponentInstanceOfType(DataStorage.class);
    PageNavigation spaceNav;
    try {
      spaceNav = dataStorage.getPageNavigation(PortalConfig.GROUP_TYPE, groupId);
      if (spaceNav != null) {
        UIPortal uiPortal = Util.getUIPortal();
        List<PageNavigation> pnavigations = uiPortal.getNavigations();
        pnavigations.remove(spaceNav);
        dataStorage.remove(spaceNav);
      } else {
        throw new Exception("spaceNav is null");
      }
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_REMOVE_NAV, e);
    }

  }

  /**
   * Gets pageNavigation by a space's groupId
   *
   * @param groupId
   * @return pageNavigation
   * @throws Exception
   * @throws Exception
   */
  static public PageNavigation getGroupNavigation(String groupId) throws Exception {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    DataStorage dataStorage = (DataStorage) container.getComponentInstanceOfType(DataStorage.class);
    return (PageNavigation) dataStorage.getPageNavigation(PortalConfig.GROUP_TYPE, groupId);
  }

  /**
   * This related to a bug from portal. When this bug is resolved, use
   * pageNavigation.getNode(space.getUrl());
   *
   * @param pageNavigation
   * @param spaceUrl
   * @return
   */
  static public PageNode getHomeNode(PageNavigation pageNavigation, String spaceUrl) {
    PageNode homeNode = pageNavigation.getNode(spaceUrl);
    // works around
    if (homeNode == null) {
      List<PageNode> pageNodes = pageNavigation.getNodes();
      for (PageNode pageNode : pageNodes) {
        if (pageNode.getUri().equals(spaceUrl)) {
          homeNode = pageNode;
          break;
        }
      }
    }
    return homeNode;
  }

  /**
   * Sorts spaces list by priority and alphabet order
   *
   * @param spaces
   * @return ordered spaces list
   */
  static public List<Space> getOrderedSpaces(List<Space> spaces) {
    Iterator<Space> itr = spaces.iterator();
    List<Space> orderedSpaces = new ArrayList<Space>();
    List<Space> middleSpaces = new ArrayList<Space>();
    List<Space> lowSpaces = new ArrayList<Space>();
    Space space = null;
    while (itr.hasNext()) {
      space = itr.next();
      String priority = space.getPriority();
      if (priority.equals(Space.HIGH_PRIORITY)) {
        orderedSpaces.add(space);
      } else if (priority.equals(Space.INTERMEDIATE_PRIORITY)) {
        middleSpaces.add(space);
      } else if (priority.equals(Space.LOW_PRIORITY)) {
        lowSpaces.add(space);
      }
    }
    for (Space sp : middleSpaces) {
      orderedSpaces.add(sp);
    }
    for (Space sp : lowSpaces) {
      orderedSpaces.add(sp);
    }
    return orderedSpaces;
  }

  /**
   * Utility for counting the number of members in a space
   *
   * @param space
   * @return the number of members
   * @throws SpaceException
   */
  static public int countMembers(Space space) throws SpaceException {
    try {
      PortalContainer portalContainer = PortalContainer.getInstance();
      SpaceService spaceService = (SpaceService) portalContainer.getComponentInstanceOfType(SpaceService.class);
      return spaceService.getMembers(space).size();
    } catch (Exception e) {
      LOG.error("Failed to count space members for " + space, e);
      return 0;
    }
  }

  /**
   * Gets app status in a space
   *
   * @param space
   * @param appId
   * @return appStatus
   */
  static public String getAppStatus(Space space, String appId) {
    String installedApps = space.getApp();
    if (installedApps == null)
      return null;
    if (installedApps.contains(appId)) {
      String appStatusPattern = getAppStatusPattern(installedApps, appId);
      return appStatusPattern.split(":")[3];
    }
    return null;
  }

  /**
   * Gets appNodeName in a space by its appId
   *
   * @param space
   * @param appId
   * @return
   */
  static public String getAppNodeName(Space space, String appId) {
    String installedApps = space.getApp();
    if (installedApps == null)
      return null;
    if (installedApps.contains(appId)) {
      String appStatusPatern = getAppStatusPattern(installedApps, appId);
      /* return appStatusPatern.split(":")[1]; */
      return appStatusPatern.split(":")[0];
    }
    return null;
  }

  /**
   * Checks if an application is removable or not. Default will return true.
   *
   * @param space
   * @param appId
   * @return-
   */
  static public boolean isRemovableApp(Space space, String appId) {
    String installedApps = space.getApp();
    if (installedApps.contains(appId)) {
      String appStatus = getAppStatusPattern(installedApps, appId);
      String[] spliter = appStatus.split(":");
      if (spliter[2].equals("false"))
        return false;
    }
    return true;
  }

  /**
   * Gets all application id from a space
   *
   * @param space
   * @return
   */
  static public List<String> getAppIdList(Space space) {
    List<String> appIdList = new ArrayList<String>();
    String installedApps = space.getApp();
    if (installedApps != null) {
      if (installedApps.contains(",")) {
        String[] appStatuses = installedApps.split(",");
        for (String appStatus : appStatuses) {
          appIdList.add((appStatus.split(":"))[0]);
        }
      } else {
        appIdList.add((installedApps.split(":"))[0]);
      }
    }
    return appIdList;
  }

  /**
   * Utility for getting absolute url
   *
   * @return absolute url
   * @throws SpaceException
   */
  static public String getAbsoluteUrl() throws SpaceException {
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    HttpServletRequest request = portalRequestContext.getRequest();
    String str = request.getRequestURL().toString();
    return str.substring(0, str.indexOf(portalRequestContext.getRequestContextPath()));
  }

  /**
   * Checks if a user is removed or not.<br>
   *
   * @param userName User name for checking.
   * @throws SpaceException if user is removed.
   */
  static public void checkUserExisting(String userName) throws SpaceException {
    OrganizationService orgService = getOrganizationService();
    User user = null;
    try {
      user = orgService.getUserHandler().findUserByName(userName);
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.ERROR_RETRIEVING_USER, e);
    }

    if (user == null) {
      throw new SpaceException(SpaceException.Code.ERROR_RETRIEVING_USER);
    }
  }

  /**
   * Check an application is installed or not yet.
   *
   * @param space
   * @param appId
   * @return
   */
  static public boolean isInstalledApp(Space space, String appId) {
    String installedApps = space.getApp();
    if (installedApps == null) {
      return false;
    }
    String[] apps = installedApps.split(",");
    String[] appPart;
    for (int idx = 0; idx < apps.length; idx++) {
      appPart = apps[idx].split(":");
      if (appPart[0].equals(appId))
        return true;
    }
    return false;
  }

  /**
   * Get display application name in formal.
   * 
   * @param appDisplayName
   * @return
   */
  static public String getDisplayAppName(String appDisplayName) {
    return getDisplayAppName(getDisplayAppName(appDisplayName, PORTLET_STR), GADGET_STR);
  }
  
  static private String getDisplayAppName(String appDisplayName, String key) {
    if (appDisplayName.toLowerCase().endsWith(key)) {
      return appDisplayName.substring(0, appDisplayName.length() - key.length()).trim();
    }
    return appDisplayName;
  }
  /**
   * Gets appStatusPattern: [appId:appNodeName:isRemovableString:status]
   *
   * @param installedApps
   * @param appId
   * @return
   */
  static private String getAppStatusPattern(String installedApps, String appId) {
    if (installedApps == null)
      return null;
    if (installedApps.contains(appId)) {
      String appStatus = installedApps.substring(installedApps.indexOf(appId));
      if (appStatus.contains(",")) {
        appStatus = appStatus.substring(0, appStatus.indexOf(","));
      }
      String[] splited = appStatus.split(":");
      if (splited.length != 4) {
        LOG.warn("appStatus is not in correct form of [appId:appNodeName:isRemovableString:status] : "
            + appStatus);
        return null;
      }
      return appStatus;
    }
    return null;
  }

  /**
   * Checks if a group can have access to an application
   *
   * @param app
   * @param groupId
   * @return
   * @throws Exception
   */
  static private boolean hasAccessPermission(Application app, String groupId) throws Exception {
    List<String> permissions = app.getAccessPermissions();
    if (permissions == null)
      return false;
    for (String ele : permissions) {
      if (hasViewPermission(ele, groupId))
        return true;
    }
    return false;
  }

  /**
   * Gets Organization Service
   *
   * @return
   */
  static public OrganizationService getOrganizationService() {
    PortalContainer portalContainer = PortalContainer.getInstance();
    return (OrganizationService) portalContainer.getComponentInstanceOfType(OrganizationService.class);
  }

  /**
   * Gets dataStorage
   *
   * @return
   */
  static public DataStorage getDataStorage() {
    PortalContainer portalContainer = PortalContainer.getInstance();
    return (DataStorage) portalContainer.getComponentInstanceOfType(DataStorage.class);
  }

  /**
   * Checks if a group have access permission to an application category
   *
   * @param app
   * @param groupId
   * @return true if that group has access permission; otherwise, false
   * @throws Exception
   */
  static private boolean hasAccessPermission(ApplicationCategory app, String groupId) throws Exception {
    List<String> permissions = app.getAccessPermissions();
    if (permissions == null)
      return false;
    for (String ele : permissions) {
      if (hasViewPermission(ele, groupId)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks view permission
   *
   * @param expPerm
   * @param groupId
   * @return
   * @throws Exception
   */
  static private boolean hasViewPermission(String expPerm, String groupId) throws Exception {
    if (UserACL.EVERYONE.equals(expPerm))
      return true;
    String[] temp = expPerm.split(":");
    if (temp.length < 2)
      return false;
    String tempExp = temp[1].trim();
    if (tempExp.equals(groupId) || tempExp.equals(PLATFORM_USERS_GROUP)) {
      return true;
    }
    return false;
  }

  /**
   * Gets localized string value
   *
   * @param localizedString
   * @param portletName
   * @return
   */
  private static String getLocalizedStringValue(LocalizedString localizedString, String portletName) {
    if (localizedString == null || localizedString.getDefaultString() == null) {
      return portletName;
    } else {
      return localizedString.getDefaultString();
    }
  }

  /**
   * Gets application registry service
   *
   * @return
   */
  private static ApplicationRegistryService getApplicationRegistryService() {
    PortalContainer portalContainer = PortalContainer.getInstance();
    return (ApplicationRegistryService) portalContainer.getComponentInstanceOfType(ApplicationRegistryService.class);
  }
}
