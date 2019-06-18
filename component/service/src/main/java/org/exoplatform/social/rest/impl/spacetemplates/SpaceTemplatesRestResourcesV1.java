/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
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
package org.exoplatform.social.rest.impl.spacetemplates;

import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.space.SpaceTemplate;
import org.exoplatform.social.core.space.spi.SpaceTemplateService;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.api.ErrorResource;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.rest.api.SocialRest;
import org.exoplatform.social.service.rest.api.VersionResources;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.InputStream;
import java.util.List;

/**
 *
 * Provides REST Services for manipulating space templates.
 *
 */
@Path(VersionResources.VERSION_ONE + "/social/spaceTemplates")
@Api(tags = VersionResources.VERSION_ONE + "/social/spaceTemplates", value = VersionResources.VERSION_ONE + "/social/spaceTemplates", description = "Managing Spaces Templates")
public class SpaceTemplatesRestResourcesV1 implements SocialRest {

  private SpaceTemplateService spaceTemplateService;
  private ConfigurationManager configurationManager;
  private static final Log LOG = ExoLogger.getLogger(SpaceTemplatesRestResourcesV1.class);

  public SpaceTemplatesRestResourcesV1(SpaceTemplateService spaceTemplateService, ConfigurationManager configurationManager) {
    this.spaceTemplateService = spaceTemplateService;
    this.configurationManager = configurationManager;
  }

  @GET
  @Path("templates")
  @RolesAllowed("users")
  @ApiOperation(value = "Gets all spaces templates",
          httpMethod = "GET",
          response = Response.class,
          notes = "This returns space templates details")
  @ApiResponses(value = {
          @ApiResponse (code = 200, message = "Request fulfilled"),
          @ApiResponse (code = 500, message = "Internal server error")})
  public Response getAllTemplates(@Context UriInfo uriInfo,
                                  @ApiParam(value = "User language", required = true) @QueryParam("lang") String lang) {
    Identity identity = ConversationState.getCurrent().getIdentity();
    String userId = identity.getUserId();
    try {
      List<SpaceTemplate> list = spaceTemplateService.getLabelledSpaceTemplates(userId, lang);
      return EntityBuilder.getResponse(list, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
    } catch (Exception e) {
      LOG.error("Cannot get list of templates for user {}, with lang {}", userId, lang, e);
      return EntityBuilder.getResponse(new ErrorResource("Error occurred while getting list of space templates", "space templates permissions not extracted"),
          uriInfo, RestUtils.getJsonMediaType(), Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  @GET
  @Path("bannerStream")
  @RolesAllowed("users")
  @ApiOperation(value = "Gets space template banner",
          httpMethod = "GET",
          response = Response.class,
          notes = "This returns space template banner input stream")
  @ApiResponses(value = {
          @ApiResponse (code = 200, message = "Request fulfilled"),
          @ApiResponse (code = 404, message = "Resource not found"),
          @ApiResponse (code = 500, message = "Internal server error")})
  public Response getBannerStream(@Context UriInfo uriInfo,
                                  @Context Request request,
                                  @ApiParam(value = "Space template name", required = true) @QueryParam("templateName") String templateName) {
    SpaceTemplate spaceTemplate = spaceTemplateService.getSpaceTemplateByName(templateName);
    if (spaceTemplate == null) {
      LOG.debug("Cannot find space template: {}", templateName);
      return EntityBuilder.getResponse(new ErrorResource("space template does not exist: " + templateName, "space template not found"),
          uriInfo, RestUtils.getJsonMediaType(), Response.Status.NOT_FOUND);
    }
    String bannerPath = spaceTemplate.getBannerPath();
    if (StringUtils.isNotBlank(bannerPath)) {
      // change once the image will be dynamically loaded from DB,
      // currently, a constant is used instead of last modified date because the banner doesn't change in sources.
      EntityTag eTag = new EntityTag(Integer.toString(templateName.hashCode()));
      Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);
      if (builder == null) {
        InputStream bannerStream = null;
        try {
           bannerStream = configurationManager.getInputStream(bannerPath);
        } catch (Exception e) {
          LOG.warn("Error retrieving banner image of template {}", templateName, e);
          return EntityBuilder.getResponse(new ErrorResource("inputStream could not be extracted from path: " + bannerPath, "inputStream not extracted"),
              uriInfo, RestUtils.getJsonMediaType(), Response.Status.INTERNAL_SERVER_ERROR);
        }
        if (bannerStream == null) {
          throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        builder = Response.ok(bannerStream, "image/png");
        builder.tag(eTag);
      }
      CacheControl cc = new CacheControl();
      cc.setMaxAge(86400);
      builder.cacheControl(cc);
      return builder.cacheControl(cc).build();
    }
    return EntityBuilder.getResponse(new ErrorResource("image does not exist in path: " + bannerPath, "banner not found"),
        uriInfo, RestUtils.getJsonMediaType(), Response.Status.NOT_FOUND);
  }
}
