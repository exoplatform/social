/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.social.core.activity.model;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;

/**
 * ActivityStream implementation.
 */
public class ActivityStreamImpl implements ActivityStream {

  /**
   * Logger.
   */
  private static final Log LOG = ExoLogger.getLogger(ActivityStreamImpl.class);

  /**
   * Internal storage uuid, from "published" node.
   */
  private String id;
  /**
   * With context of user, prettyId is remoteUser (root, john...). With context
   * of space, prettyId is spaceName (space.getName()). By using this prettyId,
   * we can construct its url to portal ui.
   */
  private String prettyId;

  /**
   * Context Type.
   */
  private Type type;

  /**
   * Stream tittle.
   */
  private String title;

  /**
   * Favicon URL for this stream.
   */
  private String faviconUrl;

  /**
   * Permalink link to this stream (url on Social).
   */
  private String permaLink;

  /**
   * {@inheritDoc}
   */
  public final void setType(final String name) {
    //TODO this is not loosely coupled
    if (name.equals(OrganizationIdentityProvider.NAME)) {
      setType(Type.USER);
    } else if (name.equals(SpaceIdentityProvider.NAME)) {
      setType(Type.SPACE);
    } else {
      LOG.warn("Failed to set activity stream type with type:" + name);
    }
  }

  /**
   * @{inheritDoc}
   */
  public final String getId() {
    return id;
  }

  /**
   * {@inheritDoc}
   */
  public final void setId(final String uuid) {
    this.id = uuid;
  }

  /**
   * {@inheritDoc}
   */
  public final String getPrettyId() {
    return prettyId;
  }

  /**
   * {@inheritDoc}
   */
  public final void setPrettyId(final String sPrettyId) {
    prettyId = sPrettyId;
  }

  /**
   * {@inheritDoc}
   */
  public final Type getType() {
    return type;
  }

  /**
   * {@inheritDoc}
   */
  public final void setType(final Type sType) {
    type = sType;
  }

  /**
   * {@inheritDoc}
   */
  public final String getFaviconUrl() {
    return faviconUrl;
  }

  /**
   * {@inheritDoc}
   */
  public final void setFaviconUrl(final String sFaviconUrl) {
    faviconUrl = sFaviconUrl;
  }

  /**
   * {@inheritDoc}
   */
  public final String getTitle() {
    return title;
  }

  /**
   * {@inheritDoc}
   */
  public final void setTitle(final String sTitle) {
    title = sTitle;
  }

  /**
   * {@inheritDoc}
   */
  public final String getPermaLink() {
    return permaLink;
  }

  /**
   * {@inheritDoc}
   */
  public final void setPermaLink(final String sPermaLink) {
    permaLink = sPermaLink;
  }
}
