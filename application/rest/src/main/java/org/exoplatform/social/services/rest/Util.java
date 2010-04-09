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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
/**
 * Util.java: utility class for rest <br />
 * Created by The eXo Platform SEA
 * @author hoatle <hoatlevan at gmail dot com>
 * @since  Jan 5, 2009
 */
public class Util {
  
  /**
   * gets response constructed from provided params.
   * @param entity
   * @param uriInfo
   * @param mediaType
   * @param status
   * @return response
   */
  static public Response getResponse(Object entity, UriInfo uriInfo, MediaType mediaType, Response.Status status) {
    return Response.created(UriBuilder.fromUri(uriInfo.getAbsolutePath()).build())
                   .entity(entity)
                   .type(mediaType)
                   .status(status)
                   .build();
  }
  
  /**
   * gets mediaType from string format
   * Currently supports json and xml only
   * @param format
   * @return mediaType of matched or throw BAD_REQUEST exception
   * @throws Exception
   */
  static public MediaType getMediaType(String format) throws Exception {
    if (format.equals("json")) {
      return MediaType.APPLICATION_JSON_TYPE;
    } else if(format.equals("xml")) {
      return MediaType.APPLICATION_XML_TYPE;
    }
    throw new WebApplicationException(Response.Status.BAD_REQUEST);
  }
  
  static public String getPortalName(UriInfo uriInfo) {
    String path = uriInfo.getPath(false);
    return path.split("/")[1];
  }
}