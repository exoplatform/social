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

package org.exoplatform.social.rest.api;

import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * Provides REST/JSON API  to manage the binding between a space and an organization group.
 */

public interface GroupSpaceBindingRestResources extends SocialRest{

    /**
     * Return a list of binding in json format
     *
     * @param uriInfo
     * @param spaceId Id of the space
     * @param spaceRole Role in the space (member/manager)
     * @param offset Bindings list offset
     * @param limit Bindings list limit
     * @param returnSize Return Size of the list?
     * @return List of binding object for this context (space + role)
     * @throws Exception
     */
    @GET
    public abstract Response getBindingsBySpaceContext(@Context UriInfo uriInfo,
                                                       @QueryParam("spaceId") String spaceId,
                                                       @QueryParam("spaceRole") String spaceRole,
                                                       @QueryParam("offset") int offset,
                                                       @QueryParam("limit") int limit,
                                                       @QueryParam("returnSize") boolean returnSize) throws Exception;
}
