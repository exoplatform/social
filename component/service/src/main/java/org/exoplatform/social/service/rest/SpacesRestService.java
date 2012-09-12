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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

import org.apache.commons.collections.map.HashedMap;
import org.exoplatform.commons.utils.Safe;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.SpaceListAccess;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.controller.metadata.ControllerDescriptor;
import org.exoplatform.web.controller.metadata.DescriptorBuilder;
import org.exoplatform.web.controller.router.Router;
import org.exoplatform.web.controller.router.RouterConfigException;
import org.exoplatform.web.controller.router.URIWriter;

/**
 * SpacesRestService.java <br />
 * <p/>
 * Provides rest services for space gadget to display user's spaces and pending spaces. <br />
 * <p/>
 * GET: /restContextName/social/spaces/{userId}/mySpaces/show.{format} <br /> GET:
 * /restContextName/social/spaces/{userId}/pendingSpaces/show.{format} <br /> Example:<br /> GET:
 * http://localhost:8080/rest/social/spaces/root/mySpaces/show.json
 *
 * @author hoatle <hoatlevan at gmail dot com>
 * @since Jan 6, 2010
 */
@Path("{portalName}/social/spaces")
public class SpacesRestService implements ResourceContainer {
  private SpaceService _spaceService;
  private IdentityManager _identityManager;
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
  
  /**
   * constructor
   */
  public SpacesRestService() {
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
    _spaceService = getSpaceService();
    List<Space> mySpaces = null;
    List<SpaceRest> mySpacesRest = new ArrayList<SpaceRest>();
    try {
      mySpaces = _spaceService.getSpaces(userId);
      
      for (Space space : mySpaces) {
        SpaceRest spaceRest = new SpaceRest(space);
        mySpacesRest.add(spaceRest);
      }
      
      //fix for issue SOC-2039, sets the space url with new navigation controller
      Router router = this.getRouter(this.getConfigurationPath());
      
      this.fillSpacesURI(mySpacesRest, router);
    } catch (SpaceException e) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    } catch (Exception e) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
    
    spaceList.setSpaces(mySpacesRest);
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
    _spaceService = getSpaceService();
    List<Space> pendingSpaces;
    List<SpaceRest> pendingSpacesRest = new ArrayList<SpaceRest>();
    try {
      pendingSpaces = _spaceService.getPendingSpaces(userId);
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
   * shows mySpaceList by json/xml format
   *
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
    portalContainerName = portalName;
    
    String userId = null;
    if (state != null) {
      userId = state.getIdentity().getUserId();
    } 
    
    Identity identity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, false);
    if (identity == null) {
      userId = Util.getViewerId(uriInfo);
    }
    
    SpaceList mySpaceList = showMySpaceList(userId);
    
    return Util.getResponse(mySpaceList, uriInfo, mediaType, Response.Status.OK);
  }

  /**
   * shows pendingSpaceList by json/xml format
   *
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
    portalContainerName = portalName;
    if (!userId.equals(Util.getViewerId(uriInfo))) {
      return null;
    }
    SpaceList pendingSpaceList = showPendingSpaceList(userId);
    return Util.getResponse(pendingSpaceList, uriInfo, mediaType, Response.Status.OK);
  }

  /**
   * Suggests space's name for searching.
   *
   * @param uriInfo
   * @param portalName
   * @param conditionToSearch
   * @param typeOfRelation
   * @param userId
   * @param format
   * @return
   * @throws Exception
   */
  @GET
  @Path("suggest.{format}")
  public Response suggestSpacenames(@Context UriInfo uriInfo,
                                    @PathParam("portalName") String portalName,
                                    @QueryParam("conditionToSearch") String conditionToSearch,
                                    @QueryParam("typeOfRelation") String typeOfRelation,
                                    @QueryParam("currentUser") String userId,
                                    @PathParam("format") String format) throws Exception {

    MediaType mediaType = Util.getMediaType(format);
    SpaceNameList nameList = new SpaceNameList();
    portalContainerName = portalName;
    SpaceService spaceSrv = getSpaceService();

    SpaceListAccess listAccess = spaceSrv.getVisibleSpacesWithListAccess(userId, new SpaceFilter(conditionToSearch));
    List<Space> spaces = Arrays.asList(listAccess.load(0, 10));
    
    for (Space space : spaces) {
      if (ALL_SPACES_STATUS.equals(typeOfRelation)) {
        nameList.addName(space.getDisplayName());
      } else {
        if (PENDING_STATUS.equals(typeOfRelation) && (spaceSrv.isPending(space, userId))) {
          nameList.addName(space.getDisplayName());
          continue;
        } else if (INCOMING_STATUS.equals(typeOfRelation) && (spaceSrv.isInvited(space, userId))) {
          nameList.addName(space.getDisplayName());
          continue;
        } else if (CONFIRMED_STATUS.equals(typeOfRelation) && (spaceSrv.isMember(space, userId))) {
          nameList.addName(space.getDisplayName());
          continue;
        }
      }
    }

    return Util.getResponse(nameList, uriInfo, mediaType, Response.Status.OK);
  }

  /**
   * List that contains space from space service.<br> Need this class for converter from rest
   * service.
   */
  @XmlRootElement
  static public class SpaceList {
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
        _spaces = new ArrayList<SpaceRest>();
      }
      _spaces.add(space);
    }
  }

  @XmlRootElement
  static public class SpaceNameList {
    private List<String> _names;

    /**
     * Sets space name list
     *
     * @param space name list
     */
    public void setNames(List<String> names) {
      this._names = names;
    }

    /**
     * Gets space name list
     *
     * @return space name list
     */
    public List<String> getNames() {
      return _names;
    }

    /**
     * Add name to space name list
     *
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
   *
   * @return spaceService
   * @see SpaceService
   */
  private SpaceService getSpaceService() {
    return (SpaceService) getPortalContainer().getComponentInstanceOfType(SpaceService.class);
  }

  /**
   * gets identityManager
   *
   * @return
   */
  private IdentityManager getIdentityManager() {
    if (_identityManager == null) {
      _identityManager = (IdentityManager) getPortalContainer().getComponentInstanceOfType(IdentityManager.class);
    }
    return _identityManager;
  }

  private PortalContainer getPortalContainer() {
    return (PortalContainer) ExoContainerContext.getContainerByName(portalContainerName);
  }

  /**
   * Fills the spaces uri.
   * 
   * @param mySpaces
   * @param router
   * @since 1.2.2
   */
  @SuppressWarnings("unchecked")
  private void fillSpacesURI(List<SpaceRest> mySpaces, Router router) {
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
  
  /**
   * Gets the configuration path of file controller.xml
   * 
   * @return
   * @since 1.2.2
   */
  private String getConfigurationPath() {
    PortalContainer portalContainer= this.getPortalContainer();
    WebAppController webAppController = (WebAppController) portalContainer.getComponentInstanceOfType(WebAppController.class);
    return webAppController.getConfigurationPath();
  }
  
  /**
   * Gets the router from path of file controller.xml
   * 
   * @param path
   * @return
   * @throws IOException
   * @throws RouterConfigException
   * @since 1.2.2
   */
  private Router getRouter(String path) throws IOException, RouterConfigException {
     File f = new File(path);
     if (!f.exists()) {
        throw new MalformedURLException("Could not resolve path " + path);
     }
     if (!f.isFile()) {
        throw new MalformedURLException("Could not resolve path " + path + " to a valid file");
     }
     return this.getRouter(f.toURI().toURL());
  }
  
  /**
   * Gets the router from url.
   * 
   * @param url
   * @return
   * @throws RouterConfigException
   * @throws IOException
   * @since 1.2.2
   */
  private Router getRouter(URL url) throws RouterConfigException, IOException {
     InputStream in = url.openStream();
     try {
        ControllerDescriptor routerDesc = new DescriptorBuilder().build(in);
        return new Router(routerDesc);
     } finally {
        Safe.close(in);
     }
  }
}
