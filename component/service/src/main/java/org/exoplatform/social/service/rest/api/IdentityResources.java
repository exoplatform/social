/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.social.service.rest.api;

import java.lang.reflect.UndeclaredThrowableException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.storage.IdentityStorageException;
import org.exoplatform.social.service.rest.RestChecker;
import org.exoplatform.social.service.rest.Util;
import org.exoplatform.social.service.rest.api.models.IdentityRestOut;

/**
 * Identity Resources end point. 
 *
 * @author <a href="http://phuonglm.net">PhuongLM</a>
 * @since 1.2.2
 */
@Path("api/social/" + VersionResources.LATEST_VERSION + "/{portalContainerName}/identity/")
public class IdentityResources implements ResourceContainer {
  private static final String[] SUPPORTED_FORMAT = new String[]{"json"};
  
  /**
   * Get the identity and its associated profile by the activity ID.
   *
   * @param uriInfo The uri request uri.
   * @param portalContainerName The associated portal container name.
   * @param identityId The specified  ID of identity.
   * @param format The expected returned format.
   * @anchor SOCref.DevRef.RestService_APIs_v1alpha3.IdentityResources.Notes.Get
   * @authentication
   * @request
   *{code}
   * GET: http://localhost:8080/rest/private/api/social/v1-alpha3/portal/identity/123456789.json
   *{code}
   * @response
   *{code:json}
   * {
   *   "id" : "123456789",
   *   "providerId": "organization",
   *   "remoteId": "demo",
   *   "profile": {
   *     "fullName": "Demo Gtn",
   *     "avatarUrl": "http://localhost:8080/profile/avatar/demo.jpg"
   *   }
   * }
   *{code}
   * @return a response object
   * 
   */
  @GET
  @Path("{identityId}.{format}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getIdentityById( @Context UriInfo uriInfo,
                                   @PathParam("portalContainerName") String portalContainerName,
                                   @PathParam("identityId") String identityId,
                                   @PathParam("format") String format) {
    RestChecker.checkAuthenticatedRequest();

    RestChecker.checkValidPortalContainerName(portalContainerName);
    
    if(identityId == null || identityId.equals("")){
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    
    MediaType mediaType = RestChecker.checkSupportedFormat(format, SUPPORTED_FORMAT);
    
    IdentityManager identityManager = Util.getIdentityManager(portalContainerName);
    try{
      Identity identity = identityManager.getIdentity(identityId, true);
      IdentityRestOut resultIdentity = new IdentityRestOut(identity);      
      Util.buildAbsoluteAvatarURL(resultIdentity);
      return Util.getResponse(resultIdentity, uriInfo, mediaType, Response.Status.OK);
    } catch (UndeclaredThrowableException undeclaredThrowableException) {
      if(undeclaredThrowableException.getCause() instanceof IdentityStorageException){
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      } else {
        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
      }
    } catch (Exception e) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  /**
   * Get the identity and its associated profile by specifying its _providerId_ and _remoteId_. Every identity has
   * its providerId and remoteId. There could be as many identities as possible. Currently, there are 2 built-in types
   * of identities (user identities and space identities) in Social.
   *
   * @param uriInfo The uri request uri.
   * @param portalContainerName The associated portal container name.
   * @param providerId The providerId of Identity.
   * @param remoteId The remoteId of Identity.
   * @param format The expected returned format.
   * @anchor SOCref.DevRef.RestService_APIs_v1alpha3.IdentityResources.identity.Get
   * @authentication
   * @request
   *{code}
   * GET: http://localhost:8080/rest/private/api/social/v1-alpha3/portal/identity/organization/demo.json
   *{code}
   * *user identities*: _providerId_ = organization; _remoteId_ = portal user name.
   *
   * *space identities:* _providerId_ = space; _remoteId_ = space's pretty name.
   *
   * @response
   *{code:json}
   * {
   *   "id" : "123456789",
   *   "providerId": "organization",
   *   "remoteId": "demo",
   *   "profile": {
   *     "fullName": "Demo Gtn",
   *     "avatarUrl": "http://localhost:8080/portal/demo/profile/avatar/demo.jpg"
   *   }
   * }
   *{code}
   * @return a response object
   * @since 1.2.2
   */
  @GET
  @Path("{providerId}/{remoteId}.{format}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getIdentityProviderIdAndRemoteId(@Context UriInfo uriInfo,
                                                   @PathParam("portalContainerName") String portalContainerName,
                                                   @PathParam("providerId") String providerId,
                                                   @PathParam("remoteId") String remoteId,
                                                   @PathParam("format") String format){
    RestChecker.checkAuthenticatedRequest();

    RestChecker.checkValidPortalContainerName(portalContainerName);
    
    if(providerId == null || providerId.equals("") || remoteId == null || remoteId.equals("")){
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    
    MediaType mediaType = RestChecker.checkSupportedFormat(format, SUPPORTED_FORMAT);

    IdentityManager identityManager = Util.getIdentityManager(portalContainerName);
    
    try{
      Identity identity = identityManager.getOrCreateIdentity(providerId, remoteId, true);
      IdentityRestOut resultIdentity = new IdentityRestOut(identity);
      Util.buildAbsoluteAvatarURL(resultIdentity);
      
      return Util.getResponse(resultIdentity, uriInfo, mediaType, Response.Status.OK);
    } catch (UndeclaredThrowableException undeclaredThrowableException) {
      if(undeclaredThrowableException.getCause() instanceof IdentityStorageException){
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      } else {
        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
      }
    } catch (Exception e) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }
}
