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
import org.exoplatform.social.core.space.spi.SpaceTemplateService;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.rest.api.SocialRest;
import org.exoplatform.social.service.rest.api.VersionResources;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 *
 * Provides REST Services for manipulating space templates.
 *
 */
@Path(VersionResources.VERSION_ONE + "/social/spaceTemplates")
@Api(tags = VersionResources.VERSION_ONE + "/social/spaceTemplates", value = VersionResources.VERSION_ONE + "/social/spaceTemplates", description = "Managing Spaces Templates")
public class SpaceTemplatesRestResourcesV1 implements SocialRest {

  private SpaceTemplateService spaceTemplateService;

  public SpaceTemplatesRestResourcesV1(SpaceTemplateService spaceTemplateService) {
    this.spaceTemplateService = spaceTemplateService;
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
  public Response getAllTemplates(@Context UriInfo uriInfo)  {
    return EntityBuilder.getResponse(spaceTemplateService.getSpaceTemplates(), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
}
