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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;

/**
 * The security manager helper class for Social Rest APIs.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since 1.2.0-GA
 * @since Jun 17, 2011
 */
public class SecurityManager {

  /**
   * <p>Checks if an authenticated identity can access an existing activity.</p>
   *
   * If the authenticated identity is the one who posted that existing activity, return true.<br />
   * If the existing activity belongs to that authenticated identity's activity stream, return true.<br />
   * If the existing activity belongs to that authenticated identity's connections' activity stream, return true.<br />
   * If the existing activity belongs to a space stream that the authenticated is a space member, return true.<br />
   * Otherwise, return false.
   *
   * @param portalContainer the specified portal container
   * @param authenticatedIdentity the authenticated identity to check
   * @param existingActivity the existing activity to check
   * @return true or false
   */
  public static boolean canAccessActivity(PortalContainer portalContainer, Identity authenticatedIdentity,
                                          ExoSocialActivity existingActivity) {
    //TODO implement this
    return false;
  }

  /**
   * <p>Checks if an poster identity has the permission to post activities on an owner identity stream.</p>
   *
   * If posterIdentity is the same as ownerIdentityStream, return true.<br />
   * If ownerIdentityStream is a user identity, and poster identity is connected to owner identity stream, return true.
   * <br />
   * If ownerIdentityStream is a space identity, and poster identity is a member of that space, return true.<br />
   * Otherwise, return false.
   *
   * @param portalContainer the specified portal container
   * @param authenticatedIdentity  the authenticated identity to check
   * @param ownerIdentityStream the identity of an existing activity stream.
   * @return true or false
   */
  public static boolean canPostActivity(PortalContainer portalContainer, Identity authenticatedIdentity,
                                        Identity ownerIdentityStream) {

    // if poserIdentity is the same as ownerIdentityStream, return true

    // Check if owner identity stream is a user identity or space identity

    // if user identity, check if connected

    //if space identity, check if is a member of
    //TODO implement this
    return false;
  }


  /**
   * <p>Checks if an authenticated identity has the permission to delete an existing activity.</p>
   *
   * If the authenticated identity is the identity who creates that existing activity, return true.<br />
   * If the authenticated identity is the stream owner of that existing activity, return true. <br />
   * Otherwise, return false.
   *
   * @param portalContainer the specified portal container
   * @param authenticatedIdentity the identity to check
   * @param existingActivity the existing activity
   * @return true or false
   */
  public static boolean canDeleteActivity(PortalContainer portalContainer, Identity authenticatedIdentity,
                                          ExoSocialActivity existingActivity) {
    //TODO implement this
    return false;
  }


  /**
   * <p>Checks if an authenticated identity has the permission to comment on an existing activity.</p>
   *
   * If commenterIdentity is the one who creates the existing activity, return true.<br />
   * If commenterIdentity is the one who is connected to existing activity's user identity, return true.<br />
   * If commenterIdentity is the one who is a member of the existing activity's space identity, return true.<br />
   * Otherwise, return false.
   *
   * @param portalContainer the specified portal container
   * @param authenticatedIdentity the authenticated identity to check
   * @param existingActivity the existing activity
   * @return true or false
   */
  public static boolean canCommentToActivity(PortalContainer portalContainer, Identity authenticatedIdentity,
                                       ExoSocialActivity existingActivity) {
    //TODO implement this
    return false;
  }

  /**
   * <p>Checks if a commenter identity has the permission to comment on an existing activity.</p>
   *
   * If commenterIdentity is the one who creates the existing activity, return true.<br />
   * If commenterIdentity is the one who is connected to existing activity's user identity, return true.<br />
   * If commenterIdentity is the one who is a member of the existing activity's space identity, return true.<br />
   * Otherwise, return false.
   *
   * @param portalContainer the specified portal container
   * @param authenticatedIdentity the authenticated identity to check
   * @param existingComment the existing comment
   * @return true or false
   */
  public static boolean canDeleteComment(PortalContainer portalContainer, Identity authenticatedIdentity,
                                         ExoSocialActivity existingComment) {
    //TODO implement this
    return false;
  }

}
