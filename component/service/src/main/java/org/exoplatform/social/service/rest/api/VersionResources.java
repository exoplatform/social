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
import javax.ws.rs.Produces;
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
 * Provides the latest rest service version and provides the supported one.
 *
 * @anchor VersionResources
 * 
 */
@Path("api/social/version")
public class VersionResources implements ResourceContainer {
  /**
   * The latest social rest api version.
   */
  public static final String LATEST_VERSION = "v1-alpha3";


  /**
   * The supported versions
   */
  public static final List<String> SUPPORTED_VERSIONS = new ArrayList<String>();

  static {
    SUPPORTED_VERSIONS.add(LATEST_VERSION);
  }

  /**
   * Gets the latest eXo Social REST API version. This version number should be used as the latest and stable
   * version that is considered to include all new features and updates of eXo Social REST services.
   *
   * @param uriInfo The URI information.
   * @param format  The expected returned format.
   * 
   * @anchor VersionResources.getLatestVersion
   * 
   * @request
   *{code}
   * GET: http://localhost:8080/rest/api/social/version/latest.json
   * or
   * GET: http://localhost:8080/rest/api/social/version/latest.xml
   *{code}
   *
   * @response
   *{code:json}
   * {"version": "v1-alpha3"}
   *{code}
   * or
   *{code:xml}
   * <version>v1-alpha3</version>
   *{code}
   *
   * @return response of the request, the type bases on the format param
   * 
   * @LevelAPI Platform
   */
  @GET
  @Path("latest.{format}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Response getLatestVersion(@Context UriInfo uriInfo,
                                   @PathParam("format") String format) {
    final String[] supportedFormat = new String[]{"json"};
    MediaType mediaType = Util.getMediaType(format, supportedFormat);
    Version entity = new Version();
    entity.setVersion(LATEST_VERSION);
    return Util.getResponse(entity, uriInfo, mediaType, Status.OK);
  }


  /**
   * Gets eXo Social REST service versions that are supported. This is for backward compatibility. If a client
   * application is using an older eXo Social REST APIs version, all APIs of the version still can work. The array MUST
   * have the latest to oldest order. For example, {{{[v2, v1, v1-beta3]}}}, but not
   * {{{[v1, v2, v1-beta3]}}}.
   *
   * @param uriInfo The URI information.
   * @param format  The expected returned format.
   * 
   * @anchor VersionResources.getSupportedVersions
   * 
   * @request
   *{code}
   * GET: http://localhost:8080/rest/api/social/version/supported.json
   * or
   * GET: http://localhost:8080/rest/api/social/version/supported.xml
   *{code}
   *
   * @response
   *{code:json}
   * {"versions": ["v1-alpha3"]}
   *{code}
   * or
   *{code:xml}
   * <versions>
   *   <version>v1-alpha3</version>
   * </versions>
   *{code}
   *
   * @return response of the request, the type bases on the format param
   * 
   * @LevelAPI Platform
   */
  @GET
  @Path("supported.{format}")
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
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
