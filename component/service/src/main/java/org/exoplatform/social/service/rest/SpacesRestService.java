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

import static org.exoplatform.social.service.rest.RestChecker.checkAuthenticatedRequest;

import java.util.*;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.*;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.rest.impl.space.SpaceRestResourcesV1;
import org.exoplatform.social.rest.impl.user.UserRestResourcesV1;
import org.exoplatform.social.service.rest.api.models.IdentityNameList;
import org.exoplatform.social.service.rest.api.models.IdentityNameList.Option;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.controller.router.Router;
import org.exoplatform.web.controller.router.URIWriter;

/**
 *
 * Provides services for the space gadget to display a user's spaces and pending spaces.
 *
 * @anchor SpacesRestService
 *
 */
@Path("{portalName}/social/spaces")
public class SpacesRestService implements ResourceContainer {
  private static final Log           LOG               = ExoLogger.getLogger(SpacesRestService.class);

  /**
   * Confirmed Status information
   */
  private static final String CONFIRMED_STATUS = "confirmed";
  /**
   * Pending Status information
   */
  private static final String PENDING_STATUS = "pending";
  /**
   * Incoming Status information
   */
  private static final String INCOMING_STATUS = "incoming";
  /**
   * Public Status information
   */
  private static final String ALL_SPACES_STATUS = "all_spaces";

  private String portalContainerName;


  /**
   * Qualified name path for rendering url.
   * 
   * @since 1.2.2
   */
  private static final QualifiedName PATH = QualifiedName.create("gtn", "path");

  /**
   * Qualified name path for rendering url.
   * 
   * @since 1.2.9
   */
  private static final QualifiedName LANG = QualifiedName.create("gtn", "lang");
  
  /**
   * Qualified name site type for rendering url.
   * 
   * @since 1.2.2
   */
  private static final QualifiedName REQUEST_SITE_TYPE = QualifiedName.create("gtn", "sitetype");
  
  /**
   * Qualified name handler for rendering url.
   * 
   * @since 1.2.2
   */
  private static final QualifiedName REQUEST_HANDLER = QualifiedName.create("gtn", "handler");
  
  /**
   * Qualified name site name for rendering url.
   * 
   * @since 1.2.2
   */
  private static final QualifiedName REQUEST_SITE_NAME = QualifiedName.create("gtn", "sitename");
  
  private static final String ALL_SPACES = "all-spaces";
  
  private static final String JSON = "json";

  private Router                     router;

  private SpaceService               spaceService;

  public SpacesRestService(SpaceService spaceService, WebAppController webAppController) {
    this.spaceService = spaceService;
    this.router = webAppController.getRouter();
  }

  /**
   * Gets the current user's spaces and pending spaces.
   *
   * @param uriInfo The requested URI information.
   * @param portalName The name of the current container.
   * @param format The format of the returned result.
   * 
   * @anchor SpacesRestService.showMySpaceList
   *
   * @return response
   * 
   * @throws Exception
   * 
   * @LevelAPI Platform
   * @deprecated Deprecated from 4.3.x. Replaced by a new API {@link UserRestResourcesV1#getSpacesOfUser(UriInfo, String, int, int, boolean, String)}
   */
  @GET
  @Path("mySpaces/show.{format}")
  public Response showMySpaceList(@Context UriInfo uriInfo,
                                  @PathParam("portalName") String portalName,
                                  @PathParam("format") String format) throws Exception {
    MediaType mediaType = Util.getMediaType(format);
    ConversationState state = ConversationState.getCurrent();
    portalContainerName = portalName;
    
    String userId = null;
    if (state != null) {
      userId = state.getIdentity().getUserId();
    }
    
    SpaceList mySpaceList = showMySpaceList(userId);
    
    this.fillUrlAllSpaces(mySpaceList, portalName);
    return Util.getResponse(mySpaceList, uriInfo, mediaType, Response.Status.OK);
  }
  
 
  /**
   * Provides a way to get the latest spaces ordered by last access and to be able to filter spaces, based on the application Id in the spaces.
   *
   *
   * @param uriInfo The requested URI information.
   * @param portalName The portal container name.
   * @param format The format of the returned result, for example, JSON, or XML.
   * @param offset Specifies the staring point of the returned results. It must be greater than or equal to 0.
   * @param limit Specifies the ending point of the returned results. It must be less than or equal to 10.
   * @param appId The application Id which is contained in spaces to filter, such as, Wiki, Discussion, Documents, Agenda and more.
   * @authentication
   * @request GET: {@code http://localhost:8080/rest/private/social/spaces/lastVisitedSpace/list.json?appId=Wiki&offset=0&limit=10}
   * @response
   * {
   * "spaces":[
   *        {"groupId":"/spaces/space_2","spaceUrl":null,"name":"space_2","displayName":"space 2","url":"space_2"},
   *        {"groupId":"/spaces/space_1","spaceUrl":null,"name":"space_1","displayName":"space 1","url":"space_1"}
   *       ],
   * "moreSpacesUrl":null
   * }
   * @return the response
   * @LevelAPI Platform
   * @anchor SpacesRestService.getLastVisitedSpace
   *
   */
  @GET
  @Path("lastVisitedSpace/list.{format}")
  public Response getLastVisitedSpace(@Context UriInfo uriInfo,
                                  @PathParam("portalName") String portalName,
                                  @PathParam("format") String format,
                                  @QueryParam("appId") String appId,
                                  @QueryParam("offset") int offset,
                                  @QueryParam("limit") int limit) throws Exception {
    checkAuthenticatedRequest();
    
    MediaType mediaType = Util.getMediaType(format, new String[]{format});
    ConversationState state = ConversationState.getCurrent();
    portalContainerName = portalName;
    
    String userId = null;
    if (state != null) {
      userId = state.getIdentity().getUserId();
    }
    
    //
    int newLimit = Math.min(limit, 100);
    int newOffset = 0;
    if (offset > 0) {
      newOffset = Math.min(offset, newLimit);
    } else {
      newOffset = 0;
    }
    
    //
    String newAppId = null;
    if (appId != null && appId.trim().length() > 0) {
      newAppId = appId;
    }
    
    SpaceList mySpaceList = getLastVisitedSpace(userId, newAppId, newOffset, newLimit);
    fillSpacesURI(mySpaceList.getSpaces());
    return Util.getResponse(mySpaceList, uriInfo, mediaType, Response.Status.OK);
  }
  
  /**
   * Gets space display info
   *
   * @param uriInfo The requested URI information.
   * @authentication
   * @request GET: {@code http://localhost:8080/rest/private/social/spaces/spaceInfo/?spaceName=space1}
   * @response
   * {
   * {"displayName":"space 2","url":"","imageSource":""},
   * }
   * @return the response
   * @LevelAPI Platform
   * @anchor SpacesRestService.getSpaceInfo
   *
   */
  @GET
  @Path("spaceInfo")
  public Response getSpaceInfo(@Context UriInfo uriInfo,
          @PathParam("portalName") String portalName,
          @QueryParam("spaceName") String spaceName) throws Exception {
    checkAuthenticatedRequest();
    
    MediaType mediaType = Util.getMediaType(JSON, new String[]{JSON});
    portalContainerName = portalName;
    
    Space space = spaceService.getSpaceByPrettyName(spaceName);

    if (space == null && StringUtils.isNotBlank(spaceName)) {
      space = spaceService.getSpaceByGroupId(SpaceUtils.SPACE_GROUP + "/" + spaceName);
    }

    if (space == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    Map<String, String> spaceInfo = new HashMap<>();
    spaceInfo.put("displayName", space.getDisplayName());
    //spaceInfo.put("url", LinkProvider.getSpaceUri(space.getUrl()));
    spaceInfo.put("url", buildSpaceUrl(space.getUrl()));
    
    spaceInfo.put("imageSource", space.getAvatarUrl() != null ? space.getAvatarUrl() :
                                 LinkProvider.SPACE_DEFAULT_AVATAR_URL);
    
    return Util.getResponse(spaceInfo, uriInfo, mediaType, Response.Status.OK);
  }
  
  private String buildSpaceUrl(final String spaceUrl) {
      //http://localhost:8080/portal/g/:spaces:space1/space1
      return "/" + portalContainerName + "/g/:spaces:" +
                   spaceUrl + "/" + spaceUrl;
  }
  
  /**
   * Gets a user's pending spaces.
   * 
   * @param uriInfo The requested URI information.
   * @param portalName The portal container name.
   * @param format The format of the returned result, for example, JSON, or XML.
   * 
   * @anchor SpacesRestService.showPendingSpaceList
   *
   * @return response
   * 
   * @throws Exception
   * 
   * @LevelAPI Platform
   */
  @GET
  @Path("pendingSpaces/show.{format}")
  public Response showPendingSpaceList(@Context UriInfo uriInfo,
                                       @PathParam("portalName") String portalName,
                                       @PathParam("format") String format) throws Exception {
    MediaType mediaType = Util.getMediaType(format);
    String userId = ConversationState.getCurrent().getIdentity().getUserId();
    portalContainerName = portalName;

    SpaceList pendingSpaceList = showPendingSpaceList(userId);
    return Util.getResponse(pendingSpaceList, uriInfo, mediaType, Response.Status.OK);
  }

  /**
   * Suggests the space's name for searching.
   *
   * @param uriInfo The requested URI information.
   * @param portalName The name of portal.
   * @param conditionToSearch The input information to search.
   * @param typeOfRelation The type of relationship of the user and the space.
   * @param userId The Id of current user.
   * @param format The format of the returned result, for example, JSON, or XML.
   * @return
   * @throws Exception
   * @LevelAPI Platform
   * @anchor SpacesRestService.suggestSpacenames
   * @deprecated Deprecated from 4.3.x. Replaced by a new API {@link SpaceRestResourcesV1#getSpaces(UriInfo, Request, String, int, int, String, String, boolean, String)}
   */
  @GET
  @RolesAllowed("users")
  @Path("suggest.{format}")
  public Response suggestSpacenames(@Context UriInfo uriInfo,
                                    @Context HttpServletRequest request,
                                    @PathParam("portalName") String portalName,
                                    @QueryParam("conditionToSearch") String conditionToSearch,
                                    @QueryParam("typeOfRelation") String typeOfRelation,
                                    @QueryParam("currentUser") String userId,
                                    @PathParam("format") String format) throws Exception {

    if(StringUtils.isBlank(userId)) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    if(!userId.equals(request.getRemoteUser())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    MediaType mediaType = Util.getMediaType(format);
    portalContainerName = portalName;
    IdentityNameList nameList = new IdentityNameList();

    
      if (ALL_SPACES_STATUS.equals(typeOfRelation)) {
        ListAccess<Space> listAccess = spaceService.getAccessibleSpacesByFilter(userId, new SpaceFilter(conditionToSearch));
        List<Space> spaces = Arrays.asList(listAccess.load(0, 10));
        addSpaceNames(nameList, spaces);
      } else {
        if (PENDING_STATUS.equals(typeOfRelation)) {
          ListAccess<Space> listAccess = spaceService.getPendingSpacesByFilter(userId, new SpaceFilter(conditionToSearch));
          List<Space> spaces = Arrays.asList(listAccess.load(0, 10));
          addSpaceNames(nameList, spaces);
        } else if (INCOMING_STATUS.equals(typeOfRelation)) {
          ListAccess<Space> listAccess = spaceService.getInvitedSpacesByFilter(userId, new SpaceFilter(conditionToSearch));
          List<Space> spaces = Arrays.asList(listAccess.load(0, 10));
          addSpaceNames(nameList, spaces);
        } else if (CONFIRMED_STATUS.equals(typeOfRelation)) {
          ListAccess<Space> listAccess = spaceService.getMemberSpacesByFilter(userId, new SpaceFilter(conditionToSearch));
          List<Space> spaces = Arrays.asList(listAccess.load(0, 10));
          addSpaceNames(nameList, spaces);
        }
    }

    return Util.getResponse(nameList, uriInfo, mediaType, Response.Status.OK);
  }

  private void addSpaceNames(IdentityNameList nameList, List<Space> spaces) {
    int i = 1;
    for (Space space : spaces) {
      Option opt = new Option();
      opt.setType("space");
      opt.setInvalid(false);
      opt.setOrder(i++);
      opt.setText(space.getDisplayName());
      opt.setValue(space.getPrettyName());
      opt.setAvatarUrl(space.getAvatarUrl());
      nameList.addOption(opt);
    }
  }

  /**
   * List that contains space from space service.<br> Need this class for converter from rest
   * service.
   */
  @XmlRootElement
  static public class SpaceList {
    private String moreSpacesUrl;
    
    private List<SpaceRest> _spaces;

    /**
     * sets space list
     *
     * @param spaces space list
     */
    public void setSpaces(List<SpaceRest> spaces) {
      _spaces = spaces;
    }

    /**
     * gets space list
     *
     * @return space list
     */
    public List<SpaceRest> getSpaces() {
      return _spaces;
    }

    /**
     * adds space to space list
     *
     * @param space
     * @see Space
     */
    public void addSpace(SpaceRest space) {
      if (_spaces == null) {
        _spaces = new LinkedList<SpaceRest>();
      }
      _spaces.add(space);
    }
    
    /**
     * Get the url of all spaces.
     * 
     * @return
     * @since 1.2.9
     */
    public String getMoreSpacesUrl() {
      return moreSpacesUrl;
    }
    
    /**
     * Set the url of all spaces.
     * 
     * @param allSpacesUrl
     * @since 1.2.9
     */
    public void setMoreSpacesUrl(String allSpacesUrl) {
      moreSpacesUrl = allSpacesUrl;
    }
  }

  /**
   * shows my spaceList by userId
   *
   * @param userId
   * @return spaceList
   * @see SpaceList
   */
  private SpaceList showMySpaceList(String userId) {
    SpaceList spaceList = new SpaceList();
    List<Space> mySpaces = null;
    List<SpaceRest> mySpacesRest = new ArrayList<SpaceRest>();
    try {
      mySpaces = spaceService.getSpaces(userId);
      
      for (Space space : mySpaces) {
        SpaceRest spaceRest = new SpaceRest(space);
        mySpacesRest.add(spaceRest);
      }
      
      this.fillSpacesURI(mySpacesRest);
    } catch (SpaceException e) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    } catch (Exception e) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
    
    spaceList.setSpaces(mySpacesRest);
    return spaceList;
  }
  
  /**
   * get my spaceList by userId which user is last visited
   * 
   * @param userId
   * @param appId
   * @param limit
   * @return
   */
  private SpaceList getLastVisitedSpace(String userId, String appId, int offset, int limit) {
    SpaceList spaceList = new SpaceList();
    List<Space> mySpaces = null;
    try {
      mySpaces = spaceService.getLastAccessedSpace(userId, appId, offset, limit);
      SpaceRest spaceRest;
      for (Space space : mySpaces) {
        spaceRest = new SpaceRest(space);
        spaceList.addSpace(spaceRest);
      }
    } catch (Exception e) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
    return spaceList;
  }

  /**
   * shows pending spaceList by userId
   *
   * @param userId
   * @return spaceList
   * @see SpaceList
   */
  private SpaceList showPendingSpaceList(String userId) {
    SpaceList spaceList = new SpaceList();
    List<Space> pendingSpaces;
    List<SpaceRest> pendingSpacesRest = new ArrayList<SpaceRest>();
    try {
      pendingSpaces = spaceService.getPendingSpaces(userId);
      for (Space space : pendingSpaces) {
        SpaceRest spaceRest = new SpaceRest(space);
        pendingSpacesRest.add(spaceRest);
      }
    } catch (SpaceException e) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
    spaceList.setSpaces(pendingSpacesRest);
    return spaceList;
  }

  /**
   * Fill url for more spaces.
   * 
   * @param spaceList
   * @param portalOwner
   * @since 1.2.9
   */
  private void fillUrlAllSpaces(SpaceList spaceList, String portalOwner) {
    try {
      Map<QualifiedName, String> qualifiedName = new HashedMap();
      qualifiedName.put(REQUEST_HANDLER, "portal");
      qualifiedName.put(REQUEST_SITE_TYPE, "portal");
      qualifiedName.put(LANG, "");
      
      StringBuilder urlBuilder = new StringBuilder();
      qualifiedName.put(REQUEST_SITE_NAME, portalOwner);
      qualifiedName.put(PATH, ALL_SPACES);
      router.render(qualifiedName, new URIWriter(urlBuilder));
      spaceList.setMoreSpacesUrl(urlBuilder.toString());
    } catch (Exception e) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Fills the spaces uri.
   * 
   * @param mySpaces
   * @since 1.2.2
   */
  @SuppressWarnings("unchecked")
  private void fillSpacesURI(List<SpaceRest> mySpaces) {
    if (mySpaces == null || mySpaces.isEmpty()) {
      return;
    }
    try {
      Map<QualifiedName, String> qualifiedName = new HashedMap ();
      qualifiedName.put(REQUEST_HANDLER, "portal");
      qualifiedName.put(REQUEST_SITE_TYPE, "group");
      
      for (SpaceRest space : mySpaces) {
        StringBuilder urlBuilder = new StringBuilder();
        qualifiedName.put(REQUEST_SITE_NAME, space.getGroupId());
        qualifiedName.put(PATH, space.getUrl());
        router.render(qualifiedName, new URIWriter(urlBuilder));
        space.setSpaceUrl(urlBuilder.toString());
      }
    } catch (Exception e) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

}
