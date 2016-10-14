/*
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.social.core.jpa.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.jpa.storage.dao.IdentityDAO;
import org.exoplatform.social.core.jpa.storage.entity.IdentityEntity;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
@Path(IdentityAvatarRestService.BASE_URL)
public class IdentityAvatarRestService implements ResourceContainer {
  private static final Log LOG = ExoLogger.getLogger(IdentityAvatarRestService.class);

  static final String BASE_URL = "/social/identity";

  private IdentityDAO identityDAO = null;
  private FileService fileService = null;

  private final CacheControl cc;

  public IdentityAvatarRestService() {
    cc = new CacheControl();
    cc.setMaxAge(86400);
  }

  @GET
  @Path("/{provider}/{username}/avatar")
  public Response avatar(@Context Request req, @PathParam("provider") String providerId, @PathParam("username") String username) {
    IdentityDAO identityDAO = getIdentityDAO();

    IdentityEntity entity = identityDAO.findByProviderAndRemoteId(providerId, username);
    if (entity == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    if (!entity.getRemoteId().equals(username)) {
      return Response.status(Response.Status.NOT_ACCEPTABLE).build();
    }

    Long fileId = entity.getAvatarFileId();
    if (fileId == null || fileId <= 0) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    try {
      FileItem file = getFileService().getFile(fileId);
      FileInfo info = file.getFileInfo();

      EntityTag eTag = new EntityTag(info.getChecksum());
      Response.ResponseBuilder rb = (eTag == null ? null : req.evaluatePreconditions(eTag));
      if (rb != null) {
        return rb.cacheControl(cc).tag(eTag).build();
      } else {
        MediaType type = MediaType.valueOf(info.getMimetype());
        return Response.ok(file.getAsStream(), type).tag(eTag).cacheControl(cc).build();
      }

    } catch (Exception ex) {
      LOG.warn("Can not load file ID: " + fileId);
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }

  private IdentityDAO getIdentityDAO() {
    if (identityDAO == null) {
      identityDAO = getService(IdentityDAO.class);
    }
    return identityDAO;
  }

  private FileService getFileService() {
    if (fileService == null) {
      fileService = getService(FileService.class);
    }
    return fileService;
  }

  private <T> T getService(Class<T> clazz) {
    PortalContainer portalContainer = PortalContainer.getInstance();
    return portalContainer.getComponentInstanceOfType(clazz);
  }

  public static String buildAvatarURL(String providerId, String remoteId) {
    if (providerId == null || remoteId == null) {
      return null;
    }

    String username = remoteId;
    String provider = providerId;
    try {
      username = URLEncoder.encode(username, "UTF-8");
      provider = URLEncoder.encode(provider, "UTF-8");
    } catch (UnsupportedEncodingException ex) {
      LOG.warn("Failure to encode username for build URL", ex);
    }

    return new StringBuilder("/rest").append(BASE_URL)
            .append("/").append(provider)
            .append("/").append(username)
            .append("/avatar")
            .toString();
  }
}
