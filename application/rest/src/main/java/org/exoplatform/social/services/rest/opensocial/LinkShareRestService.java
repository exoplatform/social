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
package org.exoplatform.social.services.rest.opensocial;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.services.rest.Util;

/**
 * LinkShareRestService: gets information from a provided link.
 * POST: /rest/social/linkshare
 * Created by The eXo Platform SAS
 * @author hoatle <hoatlevan at gmail dot com>
 * @since  Dec 29, 2009
 */
@Path("social/linkshare")
public class LinkShareRestService implements ResourceContainer {
  /**
   * constructor
   */
  public LinkShareRestService() {
    
  }
  
  /**
   * gets linkShare
   * @param link
   * @param lang
   * @return
   */
  private LinkShare getLinkShare(String link, String lang) throws Exception {
    LinkShare ls;
    try {
      if (lang != null) {
        ls = LinkShare.getInstance(link, lang);
      } else {
        ls = LinkShare.getInstance(link);
      }
    } catch(Exception ex) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
    return ls;
  }
  
  @POST
  @Path("show.{format}")
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Response getLink(@Context UriInfo uriInfo,
                          @PathParam("format") String format,
                          LinkShareRequest linkShareRequest) throws Exception {
    MediaType mediaType = Util.getMediaType(format);
    if (!linkShareRequest.verify()) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    LinkShare linkShare = null;
    linkShare = getLinkShare(linkShareRequest.getLink(), linkShareRequest.getLang());
    return Util.getResponse(linkShare, uriInfo, mediaType, Response.Status.OK);
  }
}
