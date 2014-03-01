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
package org.exoplatform.social.webui.space;

import javax.annotation.PreDestroy;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.SpaceAccessType;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.web.url.navigation.NavigationResource;
import org.exoplatform.web.url.navigation.NodeURL;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * @since 4.0  
 */

@ComponentConfig(
  template="war:/groovy/social/webui/space/UISpaceAccess.gtmpl",
  events = {
    @EventConfig(listeners = UISpaceAccess.AcceptActionListener.class),
    @EventConfig(listeners = UISpaceAccess.RequestToJoinActionListener.class),
    @EventConfig(listeners = UISpaceAccess.RefuseActionListener.class),
    @EventConfig(listeners = UISpaceAccess.JoinActionListener.class)
  }
)
public class UISpaceAccess extends UIContainer {
  private static final Log LOG = ExoLogger.getLogger(UISpaceAccess.class);
  static private final String ALL_SPACE_LINK = "all-spaces";
  
  private String status = null;
  private String spacePrettyName = null;
  private String spaceDisplayName = null;
  private String redirectURI = null;
  
  /**
   * Constructor for initialize UIPopupWindow for adding new space popup.
   *
   * @throws Exception
   */
  public UISpaceAccess() throws Exception {
    init();
  }

  /**
   * Inits at the first loading.
   * @since 4.0
   */
  public void init() {
    try {
      PortalRequestContext pcontext = Util.getPortalRequestContext();
      Object statusObject = pcontext.getRequest().getSession().getAttribute(SpaceAccessType.ACCESSED_TYPE_KEY);
      Object spacePrettyNameObj = pcontext.getRequest().getSession().getAttribute(SpaceAccessType.ACCESSED_SPACE_PRETTY_NAME_KEY);
      
      
      if (spacePrettyNameObj == null) {
        this.status = statusObject != null ? statusObject.toString() : "";
        this.spaceDisplayName = "";
        this.spacePrettyName = "";
        this.redirectURI = statusObject != null ? Utils.getURI(ALL_SPACE_LINK) : "";
        return;
        
      } 
      
      this.status = statusObject.toString();
      
      //
      this.spacePrettyName = spacePrettyNameObj.toString();
      Space space = Utils.getSpaceService().getSpaceByPrettyName(spacePrettyName);
      this.spaceDisplayName = space.getDisplayName();
      
      if ("social.space.access.not-access-wiki-space".equals(status)) {
        
        Object wikiPageObj = pcontext.getRequest().getSession().getAttribute(SpaceAccessType.ACCESSED_SPACE_WIKI_PAGE_KEY);

        pcontext.sendRedirect(getPermanWikiLink(spacePrettyName, wikiPageObj.toString()));
        return;
      } 
      
      this.redirectURI = "";
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
  }
  
  
  
  public String getStatus() {
    return status;
  }

  public String getSpacePrettyName() {
    return spacePrettyName;
  }

  public String getSpaceDisplayName() {
    return spaceDisplayName;
  }

  public String getRedirectURI() {
    return redirectURI;
  }

  /**
   * This method is fake to build permanent wiki link.
   * After Permanent Link feature will be finished by ECMS team, 
   * this method will be removed instead of Wiki API.
   * 
   * @param pcontext
   * @return
   */
  private String getPermanWikiLink(String spacePrettyName, String wikiPage) {
    StringBuilder sb = new StringBuilder("wiki/group/spaces/").append(spacePrettyName).append("/").append(wikiPage);
    
    RequestContext ctx = RequestContext.getCurrentInstance();
    NodeURL nodeURL =  ctx.createURL(NodeURL.TYPE);
    //nodeURL.setSchemeUse(true);
    NavigationResource resource = new NavigationResource(SiteType.PORTAL, Util.getPortalRequestContext().getPortalOwner(), sb.toString());
    return nodeURL.setResource(resource).toString(); 
  }
  
  @PreDestroy
  public void cleanSession() {
    PortalRequestContext pcontext = Util.getPortalRequestContext();
    pcontext.getRequest().getSession().removeAttribute(SpaceAccessType.ACCESSED_SPACE_PRETTY_NAME_KEY);
    pcontext.getRequest().getSession().removeAttribute(SpaceAccessType.ACCESSED_TYPE_KEY);
  }
  
 
  /**
   * Listens event when user accept an invited to join the space
   * @author thanhvc
   *
   */
  static public class AcceptActionListener extends EventListener<UISpaceAccess> {
    public void execute(Event<UISpaceAccess> event) throws Exception {
      UISpaceAccess uiSpaceAccess = event.getSource();
      String remoteId = Utils.getOwnerRemoteId();
      SpaceService s = Utils.getSpaceService();
      Space space = s.getSpaceByPrettyName(uiSpaceAccess.getSpacePrettyName());
      s.addMember(space, remoteId);
      event.getRequestContext().getJavascriptManager().getRequireJS().addScripts("(function(){ window.location.href = '" + Utils.getSpaceHomeURL(space) + "';})();");
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSpaceAccess.getParent());
    }
  }
  
  /**
   * Listens event when user request to join space
   * 
   */
  static public class RequestToJoinActionListener extends EventListener<UISpaceAccess> {
    @Override
    public void execute(Event<UISpaceAccess> event) throws Exception {
      UISpaceAccess uiSpaceAccess = event.getSource();
      String remoteId = Utils.getOwnerRemoteId();
      SpaceService s = Utils.getSpaceService();
      Space space = s.getSpaceByPrettyName(uiSpaceAccess.getSpacePrettyName());
      s.addPendingUser(space, remoteId);
      event.getRequestContext().getJavascriptManager().getRequireJS().addScripts("(function(){ window.location.href = '" + Utils.getSpaceHomeURL(space) + "';})();");
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSpaceAccess.getParent());
 
    }
  }
  
  /**
   * This action trigger when user click on refuse button.
   *
   * @author thanhvc
   */
  static public class RefuseActionListener extends EventListener<UISpaceAccess> {

    @Override
    public void execute(Event<UISpaceAccess> event) throws Exception {
      UISpaceAccess uiSpaceAccess = event.getSource();
      String remoteId = Utils.getOwnerRemoteId();
      SpaceService s = Utils.getSpaceService();
      Space space = s.getSpaceByPrettyName(uiSpaceAccess.getSpacePrettyName());
      s.removeInvitedUser(space, remoteId);
      event.getRequestContext().getJavascriptManager().getRequireJS().addScripts("(function(){ window.location.href = '" + Utils.getURI(ALL_SPACE_LINK) + "';})();");
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSpaceAccess.getParent());

    }

  }

  /**
   * This action is triggered when user click on join button
   */
  static public class JoinActionListener extends EventListener<UISpaceAccess> {

    @Override
    public void execute(Event<UISpaceAccess> event) throws Exception {
      UISpaceAccess uiSpaceAccess = event.getSource();
      String remoteId = Utils.getOwnerRemoteId();
      SpaceService s = Utils.getSpaceService();
      Space space = s.getSpaceByPrettyName(uiSpaceAccess.getSpacePrettyName());
      s.addMember(space, remoteId);
      //
      PortalRequestContext pcontext = Util.getPortalRequestContext();
      String originalRequest = pcontext.getRequest().getSession().getAttribute(SpaceAccessType.ACCESSED_SPACE_REQUEST_PATH_KEY).toString();
      pcontext.getRequest().getSession().removeAttribute(SpaceAccessType.ACCESSED_SPACE_REQUEST_PATH_KEY);

      event.getRequestContext().getJavascriptManager().getRequireJS().addScripts("(function(){ window.location.href = '" + originalRequest + "';})();");
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSpaceAccess.getParent());
    }

  }

  
}

