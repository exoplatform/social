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
package org.exoplatform.social.webui.space.access;

import java.io.IOException;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.application.RequestNavigationData;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.router.ExoRouter;
import org.exoplatform.social.common.router.ExoRouter.Route;
import org.exoplatform.social.core.space.SpaceAccessType;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.web.application.Application;
import org.exoplatform.web.application.ApplicationLifecycle;
import org.exoplatform.web.application.RequestFailure;
import org.exoplatform.webui.application.WebuiRequestContext;

/**
 * Created by The eXo Platform SAS
 * Author : thanh_vucong
 *          thanh_vucong@exoplatform.com
 * Oct 17, 2012  
 */
public class SpaceAccessApplicationLifecycle implements ApplicationLifecycle<WebuiRequestContext> {

  private static final Log LOG = ExoLogger.getLogger(SpaceAccessApplicationLifecycle.class);
  
  
  @Override
  public void onInit(Application app) throws Exception {
    
  }

  @Override
  public void onStartRequest(Application app, WebuiRequestContext context) throws Exception {
    PortalRequestContext pcontext = (PortalRequestContext)context;
    

    //SiteKey siteKey = new SiteKey(pcontext.getSiteType(), pcontext.getSiteName());
    
    String requestPath = pcontext.getControllerContext().getParameter(RequestNavigationData.REQUEST_PATH);
    
    Route route = ExoRouter.route(requestPath);
    if (route == null) { 
      return;
    }
    
    String spacePrettyName = route.localArgs.get("spacePrettyName");
    String appName = route.localArgs.get("appName");
    String page = route.localArgs.get("page");
    
    
    if (pcontext.getSiteType().equals(SiteType.GROUP)
        && pcontext.getSiteName().startsWith("/spaces") && spacePrettyName != null
        && spacePrettyName.length() > 0) {
      
      Space space = Utils.getSpaceService().getSpaceByPrettyName(spacePrettyName);
      String remoteId = Utils.getViewerRemoteId();
      
    
      if (inSuperAdminGroup(remoteId, space)) {
        return;
      }
      
      if ("wiki".equals(appName) && (page != null && page.length() > 0)) {
        processWikiSpaceAccess(pcontext, remoteId, space, page);
      } else {
        processSpaceAccess(pcontext, remoteId, space);
      }
    }
  }
  
  
  private boolean inSuperAdminGroup(String remoteId, Space space) {
   //special case when remoteId is super administrator and allow to access
    return SpaceAccessType.SUPER_ADMINISTRATOR.doCheck(remoteId, space);
  }

  
  private void processWikiSpaceAccess(PortalRequestContext pcontext, String remoteId, Space space, String wikiPage) throws IOException {
    
    boolean gotStatus = SpaceAccessType.SPACE_NOT_FOUND.doCheck(remoteId, space);
    if (gotStatus) {
      sendRedirect(pcontext, SpaceAccessType.SPACE_NOT_FOUND, null);
      return;
    }
    
    //Gets Wiki Page Perma link
    //http://int.exoplatform.org/portal/intranet/wiki/group/spaces/engineering/Spec_Func_-_Wiki_Page_Permalink
    //sendRedirect to Pemanent URI
    
    gotStatus = SpaceAccessType.NOT_ACCESS_WIKI_SPACE.doCheck(remoteId, space);
    if (gotStatus) {
      pcontext.getRequest().getSession().setAttribute(SpaceAccessType.ACCESSED_SPACE_WIKI_PAGE_KEY, wikiPage);
      
      
      sendRedirect(pcontext, SpaceAccessType.NOT_ACCESS_WIKI_SPACE, space.getPrettyName());
      return;
    }
  }
  
  private void processSpaceAccess(PortalRequestContext pcontext, String remoteId, Space space) throws IOException {
    
    
    //
    boolean gotStatus = SpaceAccessType.SPACE_NOT_FOUND.doCheck(remoteId, space);
    if (gotStatus) {
      sendRedirect(pcontext, SpaceAccessType.SPACE_NOT_FOUND, null);
      return;
    }
    
    //
    gotStatus = SpaceAccessType.INVITED_SPACE.doCheck(remoteId, space);
    if (gotStatus) {
      sendRedirect(pcontext, SpaceAccessType.INVITED_SPACE, space.getPrettyName());
      return;
    }
    
    //
    gotStatus = SpaceAccessType.REQUESTED_JOIN_SPACE.doCheck(remoteId, space);
    if (gotStatus) {
      sendRedirect(pcontext, SpaceAccessType.REQUESTED_JOIN_SPACE, space.getPrettyName());
      return;
    }
    
    //
    gotStatus = SpaceAccessType.JOIN_SPACE.doCheck(remoteId, space);
    if (gotStatus) {
      sendRedirect(pcontext, SpaceAccessType.JOIN_SPACE, space.getPrettyName());
      return;
    }

    //
    gotStatus = SpaceAccessType.REQUEST_JOIN_SPACE.doCheck(remoteId, space);
    if (gotStatus) {
      sendRedirect(pcontext, SpaceAccessType.REQUEST_JOIN_SPACE, space.getPrettyName());
      return;
    }
    //
    gotStatus = SpaceAccessType.CLOSED_SPACE.doCheck(remoteId, space);
    if (gotStatus) {
      sendRedirect(pcontext, SpaceAccessType.CLOSED_SPACE, space.getPrettyName());
      return;
    }

    
  }
  
  private void sendRedirect(PortalRequestContext pcontext, SpaceAccessType type, String spacePrettyName) throws IOException {
    //build url for redirect here.
    String url = Utils.getURI(SpaceAccessType.NODE_REDIRECT);
    LOG.info(type.toString());
    
    String requestPath = pcontext.getRequestURI();
    
    pcontext.getRequest().getSession().setAttribute(SpaceAccessType.ACCESSED_TYPE_KEY, type);
    pcontext.getRequest().getSession().setAttribute(SpaceAccessType.ACCESSED_SPACE_PRETTY_NAME_KEY, spacePrettyName);
    pcontext.getRequest().getSession().setAttribute(SpaceAccessType.ACCESSED_SPACE_REQUEST_PATH_KEY, requestPath);
    
    pcontext.setResponseComplete(true);
    
    pcontext.sendRedirect(url);
  }

  @Override
  public void onFailRequest(Application app, WebuiRequestContext context, RequestFailure failureType) {
    
  }

  @Override
  public void onEndRequest(Application app, WebuiRequestContext context) throws Exception {
    PortalRequestContext pcontext = (PortalRequestContext)context;
    Utils.setCurrentNavigationData(pcontext.getNavigationData());
  }

  @Override
  public void onDestroy(Application app) throws Exception {
    
  }

}