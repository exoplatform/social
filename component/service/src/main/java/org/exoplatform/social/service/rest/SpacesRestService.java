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
package org.exoplatform.social.service.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.service.rest.PeopleRestService.UserNameList;

/**
 * SpacesRestService.java <br />
 *
 * Provides rest services for space gadget to display user's spaces and pending spaces. <br />
 *
 * GET: /restContextName/social/spaces/{userId}/mySpaces/show.{format} <br />
 * GET: /restContextName/social/spaces/{userId}/pendingSpaces/show.{format} <br />
 * Example:<br />
 * GET: http://localhost:8080/rest/social/spaces/root/mySpaces/show.json
 *
 * @author     hoatle <hoatlevan at gmail dot com>
 * @since      Jan 6, 2010
 * @copyright  eXo Platform SAS
 */
@Path("{portalName}/social/spaces")
public class SpacesRestService implements ResourceContainer {
  private SpaceService _spaceService;
  private IdentityManager _identityManager;
  /** Confirmed Status information */
  private static final String CONFIRMED_STATUS = "confirmed";
  /** Pending Status information */
  private static final String PENDING_STATUS = "pending";
  /** Incoming Status information */
  private static final String INCOMING_STATUS = "incoming";
  /** Public Status information */
  private static final String PUBLIC_STATUS = "public";
  
  /**
   * constructor
   */
  public SpacesRestService() {}

  /**
   * shows my spaceList by userId
   * @param userId
   * @return spaceList
   * @see SpaceList
   */
  private SpaceList showMySpaceList(String userId, String portalName) {
    SpaceList spaceList = new SpaceList();
    _spaceService = getSpaceService(portalName);
    List<Space> mySpaces;
    try {
      mySpaces = _spaceService.getSpaces(userId);
    } catch (SpaceException e) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
    spaceList.setSpaces(mySpaces);
    return spaceList;
  }

  /**
   * shows pending spaceList by userId
   * @param userId
   * @return spaceList
   * @see SpaceList
   */
  private SpaceList showPendingSpaceList(String userId, String portalName) {
    SpaceList spaceList = new SpaceList();
    _spaceService = getSpaceService(portalName);
    List<Space> pendingSpaces;
    try {
      pendingSpaces = _spaceService.getPendingSpaces(userId);
    } catch (SpaceException e) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
    spaceList.setSpaces(pendingSpaces);
    return spaceList;
  }
  
  /**
   * shows mySpaceList by json/xml format
   * @param uriInfo provided as {@link Context}
   * @param userId
   * @param format
   * @return response
   * @throws Exception
   */
  @GET
  @Path("mySpaces/show.{format}")
  public Response showMySpaceList(@Context UriInfo uriInfo,
		  						                @PathParam("portalName") String portalName,
                                  @PathParam("format") String format) throws Exception {
    MediaType mediaType = Util.getMediaType(format);
    ConversationState state = ConversationState.getCurrent();
    String userId = null;
    if (state != null) {
      userId = state.getIdentity().getUserId();
    } else {
      userId = getRemoteId(uriInfo, portalName);
    }

    SpaceList mySpaceList = showMySpaceList(userId, portalName);
    return Util.getResponse(mySpaceList, uriInfo, mediaType, Response.Status.OK);
  }

  /**
   * shows pendingSpaceList by json/xml format
   * @param uriInfo
   * @param userId
   * @param format
   * @return response
   * @throws Exception
   */
  @GET
  @Path("pendingSpaces/show.{format}")
  public Response showPendingSpaceList(@Context UriInfo uriInfo,
		  							                   @PathParam("portalName") String portalName,
                                       @PathParam("format") String format) throws Exception {
    MediaType mediaType = Util.getMediaType(format);
    String userId = ConversationState.getCurrent().getIdentity().getUserId();
    String remoteId = getRemoteId(uriInfo, portalName);
    if (!userId.equals(remoteId)) {
      return null;
    }
    SpaceList pendingSpaceList = showPendingSpaceList(userId, portalName);
    return Util.getResponse(pendingSpaceList, uriInfo, mediaType, Response.Status.OK);
  }

  @GET
  @Path("suggest.{format}")
  public Response suggestSpacenames(@Context UriInfo uriInfo,
                    @PathParam("portalName") String portalName,
                    @QueryParam("conditionToSearch") String conditionToSearch,
                    @QueryParam("typeOfRelation") String typeOfRelation,
                    @QueryParam("currentUser") String currentUser,
                    @PathParam("format") String format) throws Exception {
    
    MediaType mediaType = Util.getMediaType(format);
    SpaceNameList nameList = new SpaceNameList();
    List<Space> spaces = getSpaceService(portalName).getSpacesBySearchCondition(conditionToSearch);
    
    if (spaces.size() == 0) {
      return Util.getResponse(nameList, uriInfo, mediaType, Response.Status.OK);
    }
    
    String userId = currentUser;

    if (PENDING_STATUS.equals(typeOfRelation)) {
      addToNameList(spaces, userId, typeOfRelation, portalName, nameList);
    } else if (INCOMING_STATUS.equals(typeOfRelation)) {
      addToNameList(spaces, userId, typeOfRelation, portalName, nameList);
    } else if (CONFIRMED_STATUS.equals(typeOfRelation)){
      addToNameList(spaces, userId, typeOfRelation, portalName, nameList);
    } else { // publics.
      addToNameList(spaces, userId, PUBLIC_STATUS, portalName, nameList);
    }
    
    return Util.getResponse(nameList, uriInfo, mediaType, Response.Status.OK);
  }
  
  private void addToNameList(List<Space> spaces, String userId, String status, 
                                       String portalName, SpaceNameList nameList) throws SpaceException {
    SpaceService spaceSrv = getSpaceService(portalName);
    for (Space space : spaces) {
      if (PENDING_STATUS.equals(status) && (spaceSrv.isPending(space, userId))) {
        nameList.addName(space.getDisplayName());
        continue;
      } else if (INCOMING_STATUS.equals(status) && (spaceSrv.isInvited(space, userId))) {
        nameList.addName(space.getDisplayName());
        continue;
      } else if (CONFIRMED_STATUS.equals(status) && (spaceSrv.isMember(space, userId))) {
        nameList.addName(space.getDisplayName());
        continue;
      } else if (PUBLIC_STATUS.equals(status) && !space.getVisibility().equals(Space.HIDDEN)) {
        nameList.addName(space.getDisplayName());
      }
    }
  }
  
  /**
   * List that contains space from space service.<br>
   * Need this class for converter from rest service.
   */
  @XmlRootElement
  static public class SpaceList {
    private List<Space> _spaces;
    /**
     * sets space list
     * @param spaces space list
     */
    public void setSpaces(List<Space> spaces) { _spaces = spaces; }
    /**
     * gets space list
     * @return space list
     */
    public List<Space> getSpaces() { return _spaces; }
    /**
     * adds space to space list
     * @param space
     * @see Space
     */
    public void addSpace(Space space) {
      if (_spaces == null) {
        _spaces = new ArrayList<Space>();
      }
      _spaces.add(space);
    }
  }

  @XmlRootElement
  static public class SpaceNameList {
    private List<String> _names;
    /**
     * Sets space name list
     * @param space name list
     */
    public void setNames(List<String> names) {
      this._names = names; 
    }
    
    /**
     * Gets space name list
     * @return space name list
     */
    public List<String> getNames() { 
      return _names; 
    }
    
    /**
     * Add name to space name list
     * @param space name
     */
    public void addName(String name) {
      if (_names == null) {
        _names = new ArrayList<String>();
      }
      _names.add(name);
    }
  }
  
  /**
   * gets spaceService
   * @return spaceService
   * @see SpaceService
   */
  private SpaceService getSpaceService(String portalName) {
    PortalContainer portalContainer = (PortalContainer) ExoContainerContext.getContainerByName(portalName);
    return (SpaceService) portalContainer.getComponentInstanceOfType(SpaceService.class);
  }
  
  private String getRemoteId(UriInfo uriInfo, String portalName) throws Exception {
    String viewerId = Util.getViewerId(uriInfo);
    Identity identity = getIdentityManager(portalName).getIdentity(viewerId);
    return identity.getRemoteId();
  }

  /**
   * gets identityManager
   * @return
   */
  private IdentityManager getIdentityManager(String portalName) {
    if (_identityManager == null) {
      PortalContainer portalContainer = (PortalContainer) ExoContainerContext.getContainerByName(portalName);
      _identityManager = (IdentityManager) portalContainer.getComponentInstanceOfType(IdentityManager.class);
    }
    return _identityManager;
  }
}
