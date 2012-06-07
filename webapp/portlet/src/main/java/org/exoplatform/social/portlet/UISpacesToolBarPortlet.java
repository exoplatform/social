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
package org.exoplatform.social.portlet;


import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.portlet.MimeResponse;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceURL;

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.navigation.GenericScope;
import org.exoplatform.portal.mop.navigation.NodeChange;
import org.exoplatform.portal.mop.navigation.NodeChangeQueue;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserNodeFilterConfig;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * {@link UISpaceToolBarPortlet} used as a portlet displaying spaces.<br />
 * @author <a href="mailto:hanhvq@gmail.com">hanhvq</a>
 * @since Oct 7, 2009
 */
@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/groovy/social/portlet/UISpacesToolBarPortlet.gtmpl"
)
public class UISpacesToolBarPortlet extends UIPortletApplication {

  private static final String SPACE_SETTINGS = "settings";
  protected static final int DEFAULT_LEVEL = 2;
  private Scope toolbarScope;
  private UserNodeFilterConfig toolbarFilterConfig;

  /**
   * constructor
   *
   * @throws Exception
   */
  public UISpacesToolBarPortlet() throws Exception {
    int level = DEFAULT_LEVEL;
    try {
      PortletRequestContext context = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
      PortletRequest prequest = context.getRequest();
      PortletPreferences prefers = prequest.getPreferences();

      level = Integer.valueOf(prefers.getValue("level", String.valueOf(DEFAULT_LEVEL)));
    } catch (Exception ex) {
      log.warn("Preference for navigation level can only be integer");
    }
    
    if (level <= 0) {
      toolbarScope = Scope.ALL;
    } else {
      toolbarScope =  GenericScope.treeShape(level);
    }
    
    
    UserNodeFilterConfig.Builder builder = UserNodeFilterConfig.builder();
    builder.withReadWriteCheck().withVisibility(Visibility.DISPLAYED, Visibility.TEMPORAL);
    builder.withTemporalCheck();
    toolbarFilterConfig = builder.build();     
  }

  private SpaceService spaceService = null;
  private String userId = null;

  public List<UserNavigation> getSpaceNavigations() throws Exception {
    String remoteUser = getUserId();
    List<Space> spaces = getSpaceService().getAccessibleSpaces(remoteUser);

    UserPortal userPortal = SpaceUtils.getUserPortal();
    List<UserNavigation> allNavigations = userPortal.getNavigations();
    List<UserNavigation> navigations = new LinkedList<UserNavigation>();
    
    
    // Copy to another list to fix Concurency error
    for (UserNavigation navi : allNavigations) {
      navigations.add(navi);
    }
    Iterator<UserNavigation> navigationItr = navigations.iterator();
    String ownerId;
    String[] navigationParts;
    Space space;
    while (navigationItr.hasNext()) {
      ownerId = navigationItr.next().getKey().getName();
      if (ownerId.startsWith("/spaces")) {
        navigationParts = ownerId.split("/");
        space = spaceService.getSpaceByUrl(navigationParts[2]);
        if (space == null) {
          space = spaceService.getSpaceByGroupId("/spaces/" + navigationParts[2]);
        }
        if (space == null) {
          navigationItr.remove();
        }
        if (!navigationParts[1].equals("spaces") && !spaces.contains(space)) {
          navigationItr.remove();
        }
      } else { // not spaces navigation
        navigationItr.remove();
      }
    }

    Collections.sort(navigations, new SpaceNameComparator());
    return navigations;
  }

  /**
   * Verifying the UserNode which need to render in the Groovy template.
   * @param spaceNode SpaceNode
   * @param applicationNode ApplicationNode
   * @return TRUE/FALSE to render.
   * @throws SpaceException
   */
  public boolean isRender(UserNode spaceNode, UserNode applicationNode) throws SpaceException {
    SpaceService spaceSrv = getSpaceService();
    String remoteUser = getUserId();
    String spaceUrl = spaceNode.getURI();
    if (spaceUrl.contains("/")) {
      spaceUrl = spaceUrl.split("/")[0];
    }

    Space space = spaceSrv.getSpaceByUrl(spaceUrl);

    // Have no page.
    if (applicationNode.getPageRef() == null) {
      return false;
    }
    
    // space is deleted
    if (space == null) {
      return false;
    }

    if (spaceSrv.hasSettingPermission(space, remoteUser)) {
      return true;
    }

    if (SPACE_SETTINGS.equals(applicationNode.getName())) {
      return false;
    }
    
    return true;
  }

  /**
   * Retrieving the selected node.
   * @return
   * @throws Exception
   */
  protected UserNode getSelectedNode() throws Exception {
    return Util.getUIPortal().getSelectedUserNode();
  }


  /**
   * gets spaceService
   *
   * @return spaceService
   * @see SpaceService
   */
  private SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = getApplicationComponent(SpaceService.class);
    }
    return spaceService;
  }

  /**
   * gets remote user Id
   *
   * @return userId
   */
  private String getUserId() {
    if (userId == null) {
      userId = Util.getPortalRequestContext().getRemoteUser();
    }
    return userId;
  }
  
  /**
   * Getting the Node children base on the UserNavigation which provides for Groovy template.
   * @param nav UserNavigation.
   * @return
   * @throws Exception
   */
  public Collection<UserNode> getNavigationNodes(UserNavigation nav) throws Exception {
    if (nav != null) {
      try {
        //toolbarScope
        UserNode rootNodes = SpaceUtils.getUserPortal().getNode(nav, toolbarScope, toolbarFilterConfig, null);
        return rootNodes.getChildren();
      } catch (Exception ex) {
        log.warn(nav.getKey().getName() + " has been deleted");
      }
    }
    return Collections.emptyList();
  }
  
  @Override
  public void serveResource(WebuiRequestContext context) throws Exception
  {      
     super.serveResource(context);
     
     ResourceRequest req = context.getRequest();
     String id = req.getResourceID();
     
     JSONArray jsChilds = getChildrenAsJSON(getNodeFromResourceID(id));
     if (jsChilds == null)
     {
        return;
     }
     
     MimeResponse res = context.getResponse(); 
     res.setContentType("text/json"); 
     res.getWriter().write(jsChilds.toString());
  }
  
  private UserNode getNodeFromResourceID(String resourceId) throws Exception {
    UserNavigation currNav = getCurrentUserNavigation();
    if (currNav == null)
      return null;

    UserPortal userPortal = SpaceUtils.getUserPortal();
    //UserNodeFilterConfig = null ????
    UserNode node = userPortal.resolvePath(currNav, null, resourceId);
    if (node != null && node.getURI().equals(resourceId)) {
      return node;
    }
    return null;
  }
  
  /**
   * Retrieving the Current Navigation.
   * @return
   * @throws Exception
   */
  public UserNavigation getCurrentUserNavigation() throws Exception {
    WebuiRequestContext rcontext = WebuiRequestContext.getCurrentInstance();
    return SpaceUtils.getUserPortal().getNavigation(SiteKey.user(rcontext.getRemoteUser()));
  }
  
  private JSONArray getChildrenAsJSON(UserNode userNode) throws Exception {
    if (userNode == null) {
      return null;
    }

    NodeChangeQueue<UserNode> queue = new NodeChangeQueue<UserNode>();
    //Scope.CHILDREN ???
    SpaceUtils.getUserPortal().updateNode(userNode, toolbarScope, queue);
    for (NodeChange<UserNode> change : queue) {
      if (change instanceof NodeChange.Removed) {
        UserNode deletedNode = ((NodeChange.Removed<UserNode>) change).getTarget();
        if (hasRelationship(deletedNode, userNode)) {
          return null;
        }
      }
    }
    Collection<UserNode> childs = userNode.getChildren();

    JSONArray jsChilds = new JSONArray();
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    MimeResponse res = context.getResponse();
    for (UserNode child : childs) {
      jsChilds.put(toJSON(child, userNode.getNavigation().getKey().getName(), res));
    }
    return jsChilds;
  }
  
  private boolean hasRelationship(UserNode parent, UserNode userNode) {
    if (parent.getId().equals(userNode.getId())) {
      return true;
    }
    for (UserNode child : parent.getChildren()) {
      if (hasRelationship(child, userNode)) {
        return true;
      }
    }
    return false;
  }

  protected JSONObject toJSON(UserNode node, String navId, MimeResponse res) throws Exception {
    JSONObject json = new JSONObject();
    String nodeId = node.getId();

    json.put("label", node.getEncodedResolvedLabel());
    json.put("hasChild", node.getChildrenCount() > 0);
    json.put("isSelected", nodeId.equals(getSelectedNode().getId()));
    json.put("icon", node.getIcon());

    ResourceURL rsURL = res.createResourceURL();
    rsURL.setResourceID(res.encodeURL(getResourceIdFromNode(node, navId)));
    json.put("getNodeURL", rsURL.toString());
    json.put("actionLink", Utils.getSpaceURL(node));

    JSONArray childs = new JSONArray();
    for (UserNode child : node.getChildren()) {
      childs.put(toJSON(child, navId, res));
    }
    json.put("childs", childs);
    return json;
  }
  
  private String getResourceIdFromNode(UserNode node, String navId) throws Exception {
    if (node == null) {
      throw new IllegalArgumentException("node can't be null");
    }
    return node.getURI();
  }
  
  /**
   * Sorts space name in ascending order.
   * 
   * @author quangpld
   */
  private class SpaceNameComparator implements Comparator<UserNavigation> {

    @Override
    public int compare(UserNavigation u1, UserNavigation u2) {
      return u1.getKey().getName().compareToIgnoreCase(u2.getKey().getName());
    }
  }
}
