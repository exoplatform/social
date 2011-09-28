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
package org.exoplatform.social.service.rest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.security.ConversationState;

/**
 * The utility class for flow checking on rest methods.
 *
 * 1. is authenticated?
 * 2. is valid portal container name?
 * 4. is supported media type?
 * 4. is id found?
 * 5. is allowed to access?
 * 6. is any invalid params?
 * 7. returns results.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Sep 29, 2011
 * @since 1.2.3
 */
public final class RestChecker {

  /**
   * Checks if the request is authenticated or not.
   * If not, throws WebApplicationException with 401 status code.
   */
  public static void checkAuthenticatedRequest() {
    if (!isAuthenticatedRequest()) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
  }


  /**
   * Checks if the provided portal container name exists or not.
   * If not, throws WebApplicationException with 400 status code.
   *
   * @param portalContainerName the provided portal container name
   * @return the associated portal container instance if valid portal container name
   */
  public static PortalContainer checkValidPortalContainerName(String portalContainerName) {
    PortalContainer portalContainer = Util.getPortalContainerByName(portalContainerName);
    if (portalContainer == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    return portalContainer;
  }

  /**
   * Checks if the expected format is supported or not.
   * If not, throws WebApplicationException with 415 status code.
   *
   * @param expectedFormat the expected format
   * @param supportedFormats the supported formats
   * @return the associated media type instance if the expected format is supported
   */
  public static MediaType checkSupportedFormat(String expectedFormat, String[] supportedFormats) {
    return Util.getMediaType(expectedFormat, supportedFormats);
  }

  /**
   * Private constructor to avoid instantiate.
   */
  private RestChecker() {

  }

  /**
   * Checks if a request is authenticated or not.
   *
   * @return a boolean value
   */
  private static boolean isAuthenticatedRequest() {
    return (ConversationState.getCurrent()!= null && ConversationState.getCurrent().getIdentity() != null &&
            ConversationState.getCurrent().getIdentity().getUserId() != null);
  }

}
