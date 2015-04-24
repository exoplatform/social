/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
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
package org.exoplatform.social.service.rest;

import java.io.InputStream;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.common.lifecycle.SocialChromatticLifeCycle;
import org.exoplatform.social.common.service.utils.TraceElement;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Oct
 * 8, 2014
 */
@Path("{portalName}/social/updater")
public class SocialUpdaterRest implements ResourceContainer {

  /**
   * The logger.
   */
  private static final Log LOG = ExoLogger.getLogger(SocialUpdaterRest.class);

  /**
   * constructor
   */
  public SocialUpdaterRest() {
  }

  /**
   * Node type updater by rest service.
   * URI: http://localhost:8080/rest/private/portal/social/updater/model
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @GET
  @Path("model")
  public Response updaterModelOverrided(@Context UriInfo uriInfo) throws Exception {
    
    RestChecker.checkAuthenticatedRequest();
    
    UserACL acl = CommonsUtils.getService(UserACL.class);
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    
    //Check if the current user is a super user
    if (! acl.getSuperUser().equals(currentUser)) {
      LOG.warn("You don't have permission to update the model.");
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    
    TraceElement trace = TraceElement.getInstance("updater social model");
    trace.start();
    
    ChromatticManager manager = CommonsUtils.getService(ChromatticManager.class);
    SocialChromatticLifeCycle lifeCycle = (SocialChromatticLifeCycle) manager.getLifeCycle(SocialChromatticLifeCycle.SOCIAL_LIFECYCLE_NAME);
    
    RepositoryService repositoryService = CommonsUtils.getService(RepositoryService.class);
    Session session = null;
    try {
      ManageableRepository repository = repositoryService.getCurrentRepository();
      SessionProviderService sessionProviderService = CommonsUtils.getService(SessionProviderService.class);

      SessionProvider sProvider = sessionProviderService.getSystemSessionProvider(null);
      session = sProvider.getSession(lifeCycle.getWorkspaceName(), repository);

      ExtendedNodeTypeManager nodeTypeManager = (ExtendedNodeTypeManager) session.getWorkspace().getNodeTypeManager();
      nodeTypeManager.registerNodeTypes(getModelIS(), ExtendedNodeTypeManager.REPLACE_IF_EXISTS, "text/xml");
      session.save();

    } catch (RepositoryException e) {
      LOG.error(e);
    } finally {
      if (session != null && session.isLive()) {
        session.logout();
      }
      //
      trace.end();
      LOG.info(trace.toString());
    }
    return Util.getResponse("social model updater sucessfully", uriInfo, MediaType.TEXT_PLAIN_TYPE, Status.OK);
  }

  public InputStream getModelIS() {
    return Thread.currentThread().getContextClassLoader().getResourceAsStream("conf/portal/chromattic-nodetypes.xml");
  }
}
