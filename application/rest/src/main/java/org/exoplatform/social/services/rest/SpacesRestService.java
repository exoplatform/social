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
package org.exoplatform.social.services.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceException;
import org.exoplatform.social.space.SpaceService;

/**
 * SpacesRestService.java
 *
 * @author     hoatle <hoatlevan at gmail dot com>
 * @since      Jan 6, 2010
 * @copyright  eXo Platform SAS 
 */
@Path("social/spaces")
public class SpacesRestService implements ResourceContainer {
  private SpaceService _spaceService;
  /**
   * constructor
   */
  public SpacesRestService() {}
  
  /**
   * shows my spaceList by userId
   * @param userId
   * @return
   */
  private SpaceList showMySpaceList(String userId) {
    SpaceList spaceList = new SpaceList();
    _spaceService = getSpaceService();
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
   * @return
   */
  private SpaceList showPendingSpaceList(String userId) {
    SpaceList spaceList = new SpaceList();
    _spaceService = getSpaceService();
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
   * @param uriInfo
   * @param userId
   * @param format
   * @return
   * @throws Exception
   */
  @GET
  @Path("{userId}/mySpaces/show.{format}")
  public Response showMySpaceList(@Context UriInfo uriInfo,
                                  @PathParam("userId") String userId,
                                  @PathParam("format") String format) throws Exception {
    MediaType mediaType = Util.getMediaType(format);
    SpaceList mySpaceList = showMySpaceList(userId);
    return Util.getResponse(mySpaceList, uriInfo, mediaType, Response.Status.OK);
  }
  
  @GET
  @Path("{userId}/pendingSpaces/show.{format}")
  public Response showPendingSpaceList(@Context UriInfo uriInfo,
                                           @PathParam("userId") String userId,
                                           @PathParam("format") String format) throws Exception {
    MediaType mediaType = Util.getMediaType(format);
    SpaceList pendingSpaceList = showPendingSpaceList(userId);
    return Util.getResponse(pendingSpaceList, uriInfo, mediaType, Response.Status.OK);
  }
  
  /**
   * List that contains space from space service.<br>
   * Need this class for converter from rest service.
   */
  @XmlRootElement
  public class SpaceList {
    private List<Space> _spaces;
    public void setSpaces(List<Space> spaces) { _spaces = spaces; }
    public List<Space> getSpaces() { return _spaces; }
    public void addSpace(Space space) {
      if (_spaces == null) {
        _spaces = new ArrayList<Space>();
      }
      _spaces.add(space);
    }
  }
  
  /**
   * gets spaceService
   * @return
   */
  private SpaceService getSpaceService() {
    if (_spaceService == null) {
      PortalContainer portalContainer = PortalContainer.getInstance();
      _spaceService = (SpaceService) portalContainer.getComponentInstanceOfType(SpaceService.class);
    }
    return _spaceService;
  }
}
