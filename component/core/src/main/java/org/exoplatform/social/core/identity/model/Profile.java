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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.social.core.chromattic.entity.ActivityProfileEntity;
import org.exoplatform.social.core.model.AvatarAttachment;

/**
 * The Class Profile.
 */
public class Profile {

  /** gender key. */
  public static final String        GENDER         = "gender";

  /** username key. */
  public static final String        USERNAME       = "username";

  /** firstname key. */
  public static final String        FIRST_NAME     = "firstName";

  /** lastname key. */
  public static final String        LAST_NAME      = "lastName";

  /** lastname key. */
  public static final String        FULL_NAME      = "fullName";
  
  /** email key. */
  public static final String        EMAIL          = "email";

  /** email key. */
  public static final String        ABOUT_ME       = "aboutMe";

  /** profile of a deleted user */
  public static final String        DELETED        = "deleted";

  /**
   * property of type {@link AvatarAttachment} that contains the avatar
   */
  public static final String        AVATAR         = "avatar";

  /**
   * url of the avatar (can be used instead of {@link #AVATAR})
   */
  @Deprecated
  public static final String        AVATAR_URL     = "avatarUrl";

  /** EXPERIENCE. */
  public static final String        EXPERIENCES    = "experiences";

  /** COMPANY. */
  public static final String        EXPERIENCES_COMPANY     = "company";

  /** POSITION. */
  public static final String        EXPERIENCES_POSITION    = "position";

  /** POSITION. */
  public static final String        EXPERIENCES_SKILLS      = "skills";

  /** START DATE OF EXPERIENCE. */
  public static final String        EXPERIENCES_START_DATE  = "startDate";

  /** END DATE OF EXPERIENCE. */
  public static final String        EXPERIENCES_END_DATE    = "endDate";

  /** CURRENT OR PAST EXPERIENCE. */
  public static final String        EXPERIENCES_IS_CURRENT  = "isCurrent";

  /** DESCRIPTION OF EXPERIENCE. */
  public static final String        EXPERIENCES_DESCRIPTION = "description";

  /** POSITION. */
  public static final String        POSITION       = "position";

  /**
   * An optional url for this profile
   */
  @Deprecated
  public static final String        URL            = "Url";

  /** PHONES key. */
  public static final String        CONTACT_PHONES = "phones";

  /** IMS key. */
  public static final String        CONTACT_IMS    = "ims";

  /** URLS key. */
  public static final String        CONTACT_URLS   = "urls";

  /** url postfix */
  public static final String        URL_POSTFIX    = "Url";

  /** Resized subfix */
  public static final String        RESIZED_SUBFIX = "RESIZED_";

  /** Space string */
  private static final String       SPACE_STR = " ";
  
  /** Types of updating of profile. */
  public static enum                UpdateType 
                                      {
                                        POSITION,
                                        BASIC_INFOR,
                                        CONTACT,
                                        EXPERIENCES,
                                        AVATAR
                                      };
                                      
  public static enum                AttachedActivityType
                                      {
                                        USER("userProfileActivityId"),
                                        SPACE("spaceProfileActivityId"),
                                        RELATION("relationActivityId"),
                                        RELATIONSHIP("relationShipActivityId");
                                        
                                        private String type;
                                        private AttachedActivityType(String type) {
                                          this.type = type;
                                        }
                                        public String value() {
                                          return this.type;
                                        }
                                        public void setActivityId(ActivityProfileEntity entity, String activityId) {
                                          switch (this) {
                                            case USER: {
                                              entity.setUserProfileActivityId(activityId);
                                              break;
                                            }
                                            case SPACE: {
                                              entity.setSpaceProfileActivityId(activityId);
                                              break;
                                            }
                                            case RELATION: {
                                              entity.setRelationActivityId(activityId);
                                              break;
                                            }
                                            case RELATIONSHIP: {
                                              entity.setRelationShipActivityId(activityId);
                                              break;
                                            }
                                            default :
                                              break;
                                          }
                                        }
                                        public String getActivityId(ActivityProfileEntity entity) {
                                          switch (this) {
                                          case USER: {
                                            return entity.getUserProfileActivityId();
                                          }
                                          case SPACE: {
                                            return entity.getSpaceProfileActivityId();
                                          }
                                          case RELATION: {
                                            return entity.getRelationActivityId();
                                          }
                                          case RELATIONSHIP: {
                                            return entity.getRelationShipActivityId();
                                          }
                                          default : {
                                            return null;
                                          }
                                        }
                                        }
                                      };
  
                                      
  /** The properties. */
  private final Map<String, Object> properties     = new HashMap<String, Object>();

  private static final Map<UpdateType, String[]> updateTypes = new HashMap<UpdateType, String[]>();
  static {
    updateTypes.put(UpdateType.POSITION, new String[] {POSITION});
    updateTypes.put(UpdateType.BASIC_INFOR, new String[] {FIRST_NAME, LAST_NAME, EMAIL});
    updateTypes.put(UpdateType.CONTACT, new String[] {GENDER, CONTACT_PHONES, CONTACT_IMS, CONTACT_URLS});
    updateTypes.put(UpdateType.EXPERIENCES, new String[] {EXPERIENCES});
    updateTypes.put(UpdateType.AVATAR, new String[] {AVATAR});
  }

  /** The identity. */
  private final Identity            identity;

  /** The id. */
  private String                    id;

  /** The last loaded time */
  private long                      lastLoaded;

  /** Indicates whether or not the profile has been modified locally */
  private boolean                   hasChanged;

  /** Indicates the type of profile are being modified locally */
  private UpdateType                updateType;

  /** Profile url, this will never be stored */
  private String                    url;

  /** Profile url, this will never be stored */
  private String                    avatarUrl;

  private AttachedActivityType      attachedActivityType;
  
  /** Profile created time **/
  private long                      createdTime;

  /**
   * Instantiates a new profile.
   *
   * @param identity the identity
   */
  public Profile(final Identity identity) {
    this.identity = identity;
  }

  /**
   * Gets the identity.
   *
   * @return the identity
   */
  public final Identity getIdentity() {
    return identity;
  }

  /**
   * Gets the id.
   *
   * @return the id
   */
  public final String getId() {
    return id;
  }

  /**
   * Sets the id.
   *
   * @param id the new id
   */
  public final void setId(final String id) {
    this.id = id;
  }

  /**
   * Gets the last loaded time.
   *
   * @return the last loaded time
   */
  public final long getLastLoaded() {
    return lastLoaded;
  }

  /**
   * Sets the last loaded time.
   *
   * @param lastLoaded the new last loaded time
   */
  public final void setLastLoaded(final long lastLoaded) {
    this.lastLoaded = lastLoaded;
  }

  /**
   * Indicates whether or not the profile has been modified locally.
   *
   * @return <code>true</code> if it has been modified locally, <code>false</code> otherwise.
   */
  public final boolean hasChanged() {
    return hasChanged;
  }

  /**
   * Clear the has changed flag.
   */
  public final void clearHasChanged() {
     setHasChanged(false);
  }

  /**
   * Gets type of update.
   * @return the updated type for a profile
   * @since 1.2.0-GA
   */
  public UpdateType getUpdateType() {
    return updateType;
  }

  public AttachedActivityType getAttachedActivityType() {
    return attachedActivityType;
  }

  public void setAttachedActivityType(AttachedActivityType attachedActivityType) {
    this.attachedActivityType = attachedActivityType;
  }

  /**
   * Sets type of update.
   * 
   * @param updateType
   * @since 1.2.0-GA
   */
  protected void setUpdateType(String updateType) {
    for (UpdateType key : updateTypes.keySet()) {
      String[] updateTypeValues = updateTypes.get(key);
      for (String value : updateTypeValues) {
        if(value.equals(updateType)) {
          this.updateType = key;
          break;
        }
      }
    }
  }

  /**
   * Sets the value of the property <code>hasChanged<code>.
   *
   * @param hasChanged the new hasChanged
   */
  private void setHasChanged(final boolean hasChanged) {
    this.hasChanged = hasChanged;
  }

  /**
   * Gets the property.
   *
   * @param name the name
   * @return the property
   */
  public final Object getProperty(final String name) {

    // TODO : remove with Profile.URL
    if (URL.equals(name)) {
      return this.url;
    }

    // TODO : remove with Profile.AVATAR_URL
    if (AVATAR_URL.equals(name)) {
      return this.avatarUrl;
    }

    return properties.get(name);
  }

  /**
   * Sets the property.
   *
   * @param name the name
   * @param value the value
   */
  public final void setProperty(final String name, final Object value) {

    // TODO : remove with Profile.URL
    if (URL.equals(name)) {
      this.url = value.toString();
      return;
    }

    // TODO : remove with Profile.AVATAR_URL
    if (AVATAR_URL.equals(name)) {
      this.avatarUrl = value.toString();
      return;
    }

    properties.put(name, value);
    setHasChanged(true);
    setUpdateType(name);

  }

  /**
   * Contains.
   *
   * @param name the name
   * @return true, if successful
   */
  public final boolean contains(final String name) {
    return properties.containsKey(name);
  }

  /**
   * Gets the properties.
   *
   * @return the properties
   */
  public final Map<String, Object> getProperties() {
    return properties;
  }

  /**
   * Removes the property.
   *
   * @param name the name
   */
  public final void removeProperty(final String name) {
    properties.remove(name);
    setHasChanged(true);
  }

  /**
   * Gets the property value.
   *
   * @param name the name
   * @return the property value
   * @deprecated use {@link #getProperty(String)}. Will be removed at 1.3.x
   * @return
   */
  public final Object getPropertyValue(final String name) {
    return getProperty(name);
  }

  /**
   * Gets the full name.
   *
   * @return the full name
   */
  public final String getFullName() {
    String fullName = (String) getProperty(FULL_NAME);
    if (fullName != null && fullName.length() > 0) {
      return fullName;
    }
    
    String firstName = (String) getProperty(FIRST_NAME);
    String lastName = (String) getProperty(LAST_NAME);
    fullName = (firstName != null) ? firstName : StringUtils.EMPTY;
    fullName += (lastName != null) ? SPACE_STR + lastName : StringUtils.EMPTY;
    return fullName;
  }
  
  /**
   * Get this profile URL
   * 
   * @return this profile URL
   */
  public final String getUrl() {
    return url;
  }

  /**
   * Set this profile URL
   */
  public void setUrl(final String url) {
    this.url = url;
  }


  /**
   * Gets email address of this profile.
   * 
   * @return email in String format
   */
  public final String getEmail() {
    return (String) getProperty(EMAIL);
  }
  
  /**
   * Add or modify properties of the profile
   * 
   * @param props
   */
  public final void addOrModifyProperties(final Map<String, Object> props) {
    Iterator<Map.Entry<String, Object>> it = props.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, Object> entry = it.next();
      String key = entry.getKey();
      // we skip all the property that are jcr related
      if (key.contains(":")) {
        continue;
      }
      setProperty(key, entry.getValue());
    }
    setHasChanged(true);
  }

  /**
   * Gets avatar url
   * 
   * @return avatar image source
   * @deprecated use {@link #getAvatarUrl()}. Will be removed at 1.3.x
   */
  @Deprecated
  public final String getAvatarImageSource() {
    return getAvatarUrl();
  }

  /**
   * Gets avatar url
   * 
   * @return avatar image source
   * @since 1.2.0-GA
   */
  public final String getAvatarUrl() {
    return avatarUrl;
  }

  /**
   * Sets avatar url
   *
   * @since 1.2.0-GA
   */
  public void setAvatarUrl(final String avatarUrl) {
    this.avatarUrl = avatarUrl;
  }

  /**
   * Gets position
   * 
   * @return position
   * @since 1.2.0-GA
   */
  public final String getPosition() {
    return (String) getProperty(Profile.POSITION);
  }
  
  /**
   * Gets gender
   * 
   * @return gender of user
   * @since 4.0.0.Alpha1
   */
  public final String getGender() {
    return (String) getProperty(Profile.GENDER);
  }
  
  /**
   * Gets Phones
   * 
   * @return list of user's number phone
   * @since 4.0.0.Alpha1
   */
  public final List<Map<String, String>> getPhones() {
    return (List<Map<String, String>>) getProperty(Profile.CONTACT_PHONES);
  }

  public long getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(Long createdTime) {
    if (createdTime != null) {
      this.createdTime = createdTime;
    } else {
      this.createdTime = System.currentTimeMillis();
    }
  }

  /*
     * Get uuid, identity, properties of profile
     * @see java.lang.Object#toString()
     */
  @Override
  public final String toString() {
    return "[uuid : " + id + " identity : " + identity.getId() + " properties: " + properties;
  }
}
