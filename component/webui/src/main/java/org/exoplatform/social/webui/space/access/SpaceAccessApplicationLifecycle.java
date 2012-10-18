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
    
    LOG.info("RequestNavigationData::siteType =" + pcontext.getSiteType());
    LOG.info("RequestNavigationData::siteName =" + pcontext.getSiteName());
    LOG.info("RequestNavigationData::requestPath =" + requestPath);
    
    if (pcontext.getSiteType().equals(SiteType.GROUP)
        && pcontext.getSiteName().startsWith("/spaces") && requestPath != null
        && requestPath.length() > 0) {
      
      Space space = Utils.getSpaceService().getSpaceByPrettyName(requestPath);
      String remoteId = Utils.getOwnerRemoteId();
      
      //
      boolean gotStatus = SpaceAccessType.SPACE_NOT_FOUND.doCheck(remoteId, space);
      if (gotStatus) {
        sendRedirect(pcontext, SpaceAccessType.SPACE_NOT_FOUND, gotStatus);
        return;
      }
      
      //
      gotStatus = SpaceAccessType.INVITED_SPACE.doCheck(remoteId, space);
      if (gotStatus) {
        sendRedirect(pcontext, SpaceAccessType.INVITED_SPACE, gotStatus);
        return;
      }
      
      //
      gotStatus = SpaceAccessType.REQUESTED_JOIN_SPACE.doCheck(remoteId, space);
      if (gotStatus) {
        sendRedirect(pcontext, SpaceAccessType.REQUESTED_JOIN_SPACE, gotStatus);
        return;
      }
      
      //
      gotStatus = SpaceAccessType.NOT_MEMBER_SPACE.doCheck(remoteId, space);
      if (gotStatus) {
        sendRedirect(pcontext, SpaceAccessType.NOT_MEMBER_SPACE, gotStatus);
        return;
      }
      
      //
      gotStatus = SpaceAccessType.PRIVATE_SPACE.doCheck(remoteId, space);
      if (gotStatus) {
        sendRedirect(pcontext, SpaceAccessType.PRIVATE_SPACE, gotStatus);
        return;
      }

      //
      gotStatus = SpaceAccessType.NOT_ADMINISTRATOR.doCheck(remoteId, space);
      if (gotStatus) {
        sendRedirect(pcontext, SpaceAccessType.NOT_ADMINISTRATOR, gotStatus);
        return;
      }
      
    }
  }
  
  private void sendRedirect(PortalRequestContext pcontext, SpaceAccessType type, boolean status) throws IOException {
    //build url for redirect here.
    String url = Utils.getURI(SpaceAccessType.NODE_REDIRECT);
    LOG.info(type.toString());
    pcontext.setAttribute(SpaceAccessType.ACCESS_TYPE_KEY, type);
    pcontext.sendRedirect(url);
  }

  @Override
  public void onFailRequest(Application app, WebuiRequestContext context, RequestFailure failureType) throws Exception {
    
  }

  @Override
  public void onEndRequest(Application app, WebuiRequestContext context) throws Exception {
    LOG.info("SpaceAccessApplicationLifecycle::onEndRequest ================================|");
    
  }

  @Override
  public void onDestroy(Application app) throws Exception {
    
  }

}
