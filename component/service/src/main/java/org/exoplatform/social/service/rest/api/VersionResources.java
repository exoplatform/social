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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.service.rest.Util;
import org.exoplatform.social.service.rest.api.models.Version;
import org.exoplatform.social.service.rest.api.models.Versions;

/**
 * <p>The version <tt>public</tt> rest service to gets the current latest rest service version and supported
 * versions.</p> <p> Url template: <tt>{rest_context_name}/api/social/version</tt> </p>
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Jun 9, 2011.
 */
@Path("api/social/version")
public class VersionResources implements ResourceContainer {
  /**
   * The latest social rest api version.
   */
  public static final String LATEST_VERSION = "v1-alpha1";

  /**
   * The supported versions
   */
  public static final List<String> SUPPORTED_VERSIONS = new ArrayList<String>();

  static {
    SUPPORTED_VERSIONS.add(LATEST_VERSION);
  }

  /**
   * Gets the latest social rest api version, this version number should be used as the latest and stable version. This
   * latest version is consider to include all new features and updates.
   *
   * @param uriInfo the uri info
   * @param format  the expected returned format
   * @return response of the request, the type bases on the format param
   */
  @GET
  @Path("latest.{format}")
  public Response getLatestVersion(@Context UriInfo uriInfo,
                                   @PathParam("format") String format) {
    final String[] supportedFormat = new String[]{"json"};
    MediaType mediaType = Util.getMediaType(format, supportedFormat);
    Version entity = new Version();
    entity.setVersion(LATEST_VERSION);
    return Util.getResponse(entity, uriInfo, mediaType, Status.OK);
  }


  /**
   * Gets the supported social rest api versions, this is for backward compatible. If a client application is using an
   * older social rest api version, it should just work. The list order must be from the latest to oldest versions.
   *
   * @param uriInfo the uri info
   * @param format  the expected returned format
   * @return response of the request, the type bases on the format param
   */
  @GET
  @Path("supported.{format}")
  public Response getSupportedVersions(@Context UriInfo uriInfo,
                                       @PathParam("format") String format) {
    final String[] supportedFormat = new String[]{"json", "xml"};
    Versions entity = new Versions();
    entity.getVersions().addAll(SUPPORTED_VERSIONS);
    return Util.getResponse(entity, uriInfo, Util.getMediaType(format, supportedFormat), Status.OK);
  }

  /**
   * The logger.
   */
  private final Log LOG = ExoLogger.getLogger(this.getClass());

}