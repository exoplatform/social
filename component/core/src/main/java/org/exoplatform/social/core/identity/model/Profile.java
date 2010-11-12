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
package org.exoplatform.social.core.identity.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.image.ImageUtils;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.service.LinkProvider;

/**
 * The Class Profile.
 */
public class Profile {

  private static final Log LOG = ExoLogger.getLogger(Profile.class);

  public static final String USERNAME = "username";

  public static final String FIRST_NAME = "firstName";

  public static final String LAST_NAME = "lastName";

  /**
   * property of type {@link AvatarAttachment} that contains the avatar
   */
  public static String AVATAR = "avatar";

  /**
   * url of the avatar (can be used instead of {@link #AVATAR})
   */
  public static final String        AVATAR_URL     = "avatarUrl";

  public static final String        URL_POSTFIX    = "Url";

  public static final String        RESIZED_SUBFIX = "RESIZED_";

  /**
   * An optional url for this profile
   */
  public static final String URL = "Url";

  /** The properties. */
  private final Map<String, Object> properties = new HashMap<String, Object>();

  /** The identity. */
  private final Identity identity;

  /** The id. */
  private String id;

  /** The last loaded time */
  private long lastLoaded;
  
  /** Indicates whether or not the profile has been modified locally */
  private boolean hasChanged;
  
  /**
   * Instantiates a new profile.
   *
   * @param id the id
   */
  public Profile(Identity id) {
    this.identity = id;
  }

  /**
   * Gets the identity.
   *
   * @return the identity
   */
  public Identity getIdentity() {
    return identity;
  }

  /**
   * Gets the id.
   *
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the id.
   *
   * @param id the new id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Gets the last loaded time.
   *
   * @return the last loaded time
   */
  public long getLastLoaded() {
    return lastLoaded;
  }

  /**
   * Sets the last loaded time.
   *
   * @param lastLoaded the new last loaded time
   */
  public void setLastLoaded(long lastLoaded) {
    this.lastLoaded = lastLoaded;
  }

  /**
   * Indicates whether or not the profile has been modified locally.
   *
   * @return <code>true</code> if it has been modified locally, <code>false</code> otherwise.
   */
  public boolean hasChanged() {
    return hasChanged;
  }

  /**
   * Clear the has changed flag.
   */
  public void clearHasChanged() {
     setHasChanged(false);
  }

  /**
   * Sets the value of the property <code>hasChanged<code>.
   *
   * @param hasChanged the new hasChanged
   */
  private void setHasChanged(boolean hasChanged) {
    this.hasChanged = hasChanged;
  }
  
  /**
   * Gets the property.
   *
   * @param name the name
   * @return the property
   */
  public Object getProperty(String name) {
    return properties.get(name);
  }

  /**
   * Sets the property.
   *
   * @param name the name
   * @param value the value
   */
  public void setProperty(String name, Object value) {
    properties.put(name, value);
    setHasChanged(true);
  }

  /**
   * Contains.
   *
   * @param name the name
   * @return true, if successful
   */
  public boolean contains(String name) {
    return properties.containsKey(name);
  }

  /**
   * Gets the properties.
   *
   * @return the properties
   */
  public Map<String, Object> getProperties() {
    return properties;
  }

  /**
   * Removes the property.
   *
   * @param name the name
   */
  public void removeProperty(String name) {
    properties.remove(name);
    setHasChanged(true);
  }

  /**
   * Gets the property value.
   *
   * @param name the name
   * @return the property value
   * @deprecated
   * @return
   */
  @Deprecated
  public Object getPropertyValue(String name) {
    return getProperty(name);
  }

  /**
   * Gets the full name.
   *
   * @return the full name
   */
  public String getFullName() {
    String first = (String) getProperty(FIRST_NAME);
    String last = (String) getProperty(LAST_NAME);
    String all = (first != null) ? first : "";
    all += (last != null) ? " " + last : "";
    return all;
  }

  /**
   * @return null or an url if available
   */
  public String getAvatarImageSource() {
    String avatarUrl = (String) getProperty(AVATAR_URL);
    if (avatarUrl != null) {
      return avatarUrl;
    }
    AvatarAttachment avatarAttachment = (AvatarAttachment) getProperty(AVATAR);
    if (avatarAttachment != null) {
      return LinkProvider.buildAvatarUrl(avatarAttachment);
    }
    return null;
  }

  /**
   * @param containerByName
   * @return
   */
  public String getAvatarImageSource(PortalContainer containerByName) {
    return getAvatarImageSource();
  }

  /**
   * Gets user's avatar image source by specifying width and height property The
   * method will create a new scaled image file then return you the url to view
   * that file
   * 
   * @author tuan_nguyenxuan Oct 26, 2010
   * @return null or an url if available
   * @throws Exception
   */
  public String getAvatarImageSource(int width, int height) {
    // Determine the key of avatar file and avatar url like avatar_30x30 and
    // avatar_30x30Url
    String postfix = ImageUtils.buildImagePostfix(width, height);
    String keyFile = AVATAR + postfix;
    String keyURL = AVATAR + postfix + URL_POSTFIX;
    // When the resized avatar with params size is exist, we return immediately
    String avatarUrl = (String) getProperty(keyURL);
    if (avatarUrl != null) {
      return avatarUrl;
    }
    IdentityManager identityManager = (IdentityManager) PortalContainer.getInstance()
                                                                       .getComponentInstanceOfType(IdentityManager.class);
    // When had resized avatar but hadn't avatar url we build the avatar url
    // then return
    AvatarAttachment avatarAttachment = (AvatarAttachment) getProperty(keyURL);
    if (avatarAttachment != null) {
      avatarUrl = LinkProvider.buildAvatarUrl(avatarAttachment);
      setProperty(keyURL, avatarUrl);
      identityManager.saveProfile(this);
      return avatarUrl;
    }
    // When hadn't avatar yet we return null
    avatarAttachment = (AvatarAttachment) getProperty(AVATAR);
    if (avatarAttachment == null)
      return null;
    // Otherwise we create the resize avatar then return the avatar url
    InputStream inputStream = new ByteArrayInputStream(avatarAttachment.getImageBytes());
    String mimeType = avatarAttachment.getMimeType();
    AvatarAttachment newAvatarAttachment = ImageUtils.createResizedAvatarAttachment(inputStream,
                                                                                   width,
                                                                                   height,
                                                                                   avatarAttachment.getId()
                                                                                       + postfix,
                                                                                   ImageUtils.buildFileName(avatarAttachment.getFileName(),
                                                                                                           RESIZED_SUBFIX,
                                                                                                           postfix),
                                                                                   mimeType,
                                                                                   avatarAttachment.getWorkspace());

    if (newAvatarAttachment == null)
      return getAvatarImageSource();
    // Set property that contain resized avatar file to profile
    setProperty(keyFile, newAvatarAttachment);
    identityManager.saveProfile(this);
    // Build the url to that resized avatar file then save and return
    avatarUrl = LinkProvider.buildAvatarUrl(newAvatarAttachment);
    setProperty(keyURL, avatarUrl);
    identityManager.saveProfile(this);
    return avatarUrl;
  }

  /**
   * Get this profile URL
   * 
   * @return
   */
  public String getUrl() {
    return (String) getProperty(URL);
  }
}