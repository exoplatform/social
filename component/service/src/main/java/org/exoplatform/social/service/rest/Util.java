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

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.space.spi.SpaceService;

/**
 * Util.java: utility class for rest <br />.
 *
 * @author hoatle <hoatlevan at gmail dot com>
 * @since  Jan 5, 2009
 */
public final class Util {
  /**
   * Prevents constructing a new instance.
   */
  private Util() {
  }

  /**
   * Gets the response object constructed from the provided params.
   *
   * @param entity the identity
   * @param uriInfo the uri request info
   * @param mediaType the media type to be returned
   * @param status the status code
   * @return response the response object
   */
  static public Response getResponse(Object entity, UriInfo uriInfo, MediaType mediaType, Response.Status status) {
    return Response.created(UriBuilder.fromUri(uriInfo.getAbsolutePath()).build())
                   .entity(entity)
                   .type(mediaType)
                   .status(status)
                   .build();
  }
  
  /**
   * Gets mediaType from string format.
   * Currently supports json and xml only.
   *
   * @param format
   * @return mediaType of matched or throw BAD_REQUEST exception
   * @throws WebApplicationException
   * @deprecated User {@link #getMediaType(String, String[])} instead.
   *             Will be removed by 1.3.x
   */
  @Deprecated
  static public MediaType getMediaType(String format) throws WebApplicationException {
    if (format.equals("json")) {
      return MediaType.APPLICATION_JSON_TYPE;
    } else if(format.equals("xml")) {
      return MediaType.APPLICATION_XML_TYPE;
    }
    throw new WebApplicationException(Response.Status.BAD_REQUEST);
  }


  /**
   * Gets the media type from an expected format string (usually the input) and an array of supported format strings.
   * If epxectedFormat is not found in the supported format array, Status.UNSUPPORTED_MEDIA_TYPE is thrown.
   * The supported format must include one of those format: json, xml, atom or rss, otherwise Status.NOT_ACCEPTABLE
   * could be thrown.
   *
   * @param expectedFormat the expected input format
   * @param supportedFormats the supported format array
   * @return the associated media type
   */
  public static MediaType getMediaType(String expectedFormat, String[] supportedFormats) {

    if (!isSupportedFormat(expectedFormat, supportedFormats)) {
      throw new WebApplicationException(Status.UNSUPPORTED_MEDIA_TYPE);
    }

    if (expectedFormat.equals("json") && isSupportedFormat("json", supportedFormats)) {
      return MediaType.APPLICATION_JSON_TYPE;
    } else if (expectedFormat.equals("xml") && isSupportedFormat("xml", supportedFormats)) {
      return MediaType.APPLICATION_XML_TYPE;
    } else if (expectedFormat.equals("atom") && isSupportedFormat("atom", supportedFormats)) {
      return MediaType.APPLICATION_ATOM_XML_TYPE;
    }
    //TODO What's about RSS format?
    throw new WebApplicationException(Status.NOT_ACCEPTABLE);
  }

  
  /**
   * Get viewerId from servlet request data information.
   *  
   * @param uriInfo
   * @return
   */
  static public String getViewerId (UriInfo uriInfo) {
    URI uri = uriInfo.getRequestUri();
    String requestString = uri.getQuery();
    if (requestString == null) return null;
    String[] queryParts = requestString.split("&");
    String viewerId = null;
    for (String queryPart : queryParts) {
      if (queryPart.startsWith("opensocial_viewer_id")) {
        viewerId = queryPart.substring(queryPart.indexOf("=") + 1, queryPart.length());
        break;
      }
    }
    
    return viewerId;
  }

  /**
   * Gets identity of viewer user (logged-in user). Do not load profile.
   *
   * @return identity
   * @since 1.2.0 GA
   */
  public static Identity getViewerIdentity(String viewerId) {
    return getUserIdentity(viewerId, false);
  }

  /**
   * Gets identity from the remote id (user name)
   * 
   * @param userName
   * @param loadProfile
   * @return identity
   * @since 1.2.0 GA
   */
  public static Identity getUserIdentity(String userName, boolean loadProfile) {
    return getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, userName, loadProfile);
  }
  
  /**
   * Gets identityManager with default portal container.
   *
   * @return identityManager
   * @since 1.2.0 GA
   */
  public static final IdentityManager getIdentityManager() {
    return (IdentityManager) getDefaultPortalContainer().getComponentInstanceOfType(IdentityManager.class);
  }

  /**
   * Gets {@link IdentityManager} with specified portal container name.
   *
   * @param portalContainerName the specified portal container name
   * @return the identity manager
   * @since  1.2.0-GA
   */
  public static final IdentityManager getIdentityManager(String portalContainerName) {
    return (IdentityManager) getPortalContainerByName(portalContainerName).
                             getComponentInstanceOfType(IdentityManager.class);
  }


  /**
   * Gets {@link SpaceService} with default portal container.
   *
   * @return the space service
   * @since  1.2.0-GA
   */
  public static final SpaceService getSpaceService() {
    return (SpaceService) getDefaultPortalContainer().getComponentInstanceOfType(SpaceService.class);
  }

  /**
   * Gets {@link SpaceService} with specified portal container name.
   *
   * @param portalContainerName the specified portal container name
   * @return the space service
   * @since  1.2.0-GA
   */
  public static final SpaceService getSpaceService(String portalContainerName) {
    return (SpaceService) getPortalContainerByName(portalContainerName).getComponentInstanceOfType(SpaceService.class);
  }


  /**
   * Gets {@link ActivityManager} with default portal container.
   *
   * @return the activity manager
   * @since  1.2.0-GA
   */
  public static final ActivityManager getActivityManager() {
    return (ActivityManager) getDefaultPortalContainer().getComponentInstanceOfType(ActivityManager.class);
  }

  /**
   * Gets {@link ActivityManager} with specified portal container name.
   *
   * @param portalContainerName the specified portal container
   * @return the activity manager
   * @since  1.2.0-GA
   */
  public static final ActivityManager getActivityManager(String portalContainerName) {
    return (ActivityManager) getPortalContainerByName(portalContainerName).
                             getComponentInstanceOfType(ActivityManager.class);
  }

  /**
   * Gets {@link RelationshipManager} with default portal container.
   *
   * @return the relationship manager
   * @since  1.2.0-GA
   */
  public static final RelationshipManager getRelationshipManager() {
    return (RelationshipManager) getDefaultPortalContainer().getComponentInstanceOfType(RelationshipManager.class);
  }


  /**
   * Gets {@link RelationshipManager} with specified portal container name.
   *
   * @param portalContainerName the specified portal container name
   * @return the relationship manager
   * @since  1.2.0-GA
   */
  public static final RelationshipManager getRelationshipManager(String portalContainerName) {
    return (RelationshipManager) getPortalContainerByName(portalContainerName).
                                 getComponentInstanceOfType(RelationshipManager.class);
  }


  /**
   * Converts a timestamp string to time string by the pattern: EEE MMM d HH:mm:ss Z yyyy.
   *
   * @param timestamp the timstamp to convert
   * @return the time string
   */
  public static final String convertTimestampToTimeString(long timestamp) {
   SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy");
   dateFormat.setTimeZone(TimeZone.getDefault());
   return dateFormat.format(new Date(timestamp));
  }



  /**
   * Checks if an expected format is supported not not.
   *
   * @param expectedFormat  the expected format
   * @param supportedFormats the array of supported format
   * @return true or false
   */
  private static boolean isSupportedFormat(String expectedFormat, String[] supportedFormats) {
    for (String supportedFormat : supportedFormats) {
      if (supportedFormat.equals(expectedFormat)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Gets default portal container name.
   *
   * @return the portal container
   */
  private static PortalContainer getDefaultPortalContainer() {
    return PortalContainer.getInstance();
  }

  /**
   * Gets a portal container by its name.
   *
   * @param portalContainerName the specified portal container name
   * @return the portal container name
   */
  private static PortalContainer getPortalContainerByName(String portalContainerName) {
    return (PortalContainer) ExoContainerContext.getContainerByName(portalContainerName);
  }

}