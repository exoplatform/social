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

package org.exoplatform.social.core.chromattic.entity;

import java.util.List;
import java.util.Map;

import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.FormattedBy;
import org.chromattic.api.annotations.Id;
import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.NamingPrefix;
import org.chromattic.api.annotations.OneToMany;
import org.chromattic.api.annotations.OneToOne;
import org.chromattic.api.annotations.Owner;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Properties;
import org.chromattic.api.annotations.Property;
import org.chromattic.ext.format.BaseEncodingObjectFormatter;
import org.chromattic.ext.ntdef.NTFile;
import org.exoplatform.social.core.storage.query.PropertyLiteralExpression;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
@PrimaryType(name = "soc:profiledefinition")
@FormattedBy(BaseEncodingObjectFormatter.class)
@NamingPrefix("soc")
public abstract class ProfileEntity {

  @Id
  public abstract String getId();

  /**
   * The users's avatar.
   */
  @MappedBy("soc:avatar")
  @OneToOne
  @Owner
  public abstract NTFile getAvatar();
  public abstract void setAvatar(NTFile avatar);

  @MappedBy("soc:profile")
  @OneToOne
  public abstract IdentityEntity getIdentity();
  public abstract void setIdentity(IdentityEntity identity);
  
  @MappedBy("soc:activityprofile")
  @OneToOne
  @Owner
  public abstract ActivityProfileEntity getActivityProfile();
  public abstract void setActivityProfile(ActivityProfileEntity entity);

  /**
   * The external URL of an identity who does not exist in the identities list of the Social providers,
   * (_OrganizationIdentityProvider_ and _SpaceIdentityProvider_).
   * @return
   */

  @Property(name = "soc:externalUrl")
  public abstract String getExternalUrl();
  public abstract void setExternalUrl(String profileUrl);

  /**
   * The external avatar URL of an identity who does not exist in the identities list of the Social providers,
   * (_OrganizationIdentityProvider_ and _SpaceIdentityProvider_).
   * @return
   */

  @Property(name = "soc:externalAvatarUrl")
  public abstract String getExternalAvatarUrl();
  public abstract void setExternalAvatarUrl(String avatarUrl);
  
  /**
   * The parent Id is the identity Id. It is used for queries.
   * @return
   */
  // TODO : find better
  @Property(name = "soc:parentId")
  public abstract String getParentId();
  public abstract void setParentId(String parentid);
  public static final PropertyLiteralExpression<String> parentId =
      new PropertyLiteralExpression<String>(String.class, "soc:parentId");

  /**
   * The created time
   */
  @Property(name = "soc:createdTime")
  public abstract Long getCreatedTime();
  public abstract void setCreatedTime(Long createdTime);
  public static final PropertyLiteralExpression<Long> createdTime =
      new PropertyLiteralExpression<Long>(Long.class, "soc:createdTime");

  /**
   * All the experiences stored in the profile.
   */
  @OneToMany
  @Owner
  public abstract Map<String, ProfileXpEntity> getXps();

  @Properties
  public abstract Map<String, List<String>> getProperties();
  public static final PropertyLiteralExpression<String> firstName =
      new PropertyLiteralExpression<String>(String.class, "void-firstName");
  
  public static final PropertyLiteralExpression<String> lastName =
      new PropertyLiteralExpression<String>(String.class, "void-lastName");

  public static final PropertyLiteralExpression<String> fullName =
      new PropertyLiteralExpression<String>(String.class, "void-fullName");

  public static final PropertyLiteralExpression<String> aboutMe =
      new PropertyLiteralExpression<String>(String.class, "void-aboutMe");

  public static final PropertyLiteralExpression<String> position =
      new PropertyLiteralExpression<String>(String.class, "void-position");
  
  public static final PropertyLiteralExpression<String> gender =
      new PropertyLiteralExpression<String>(String.class, "void-gender");

  public static final PropertyLiteralExpression<String> deleted =
      new PropertyLiteralExpression<String>(String.class, "void-deleted");

  public static final PropertyLiteralExpression<String> skills =
      new PropertyLiteralExpression<String>(String.class, "index-skills");

  public static final PropertyLiteralExpression<String> positions =
      new PropertyLiteralExpression<String>(String.class, "index-position");

  public static final PropertyLiteralExpression<String> organizations =
      new PropertyLiteralExpression<String>(String.class, "index-company");

  public static final PropertyLiteralExpression<String> jobsDescription =
      new PropertyLiteralExpression<String>(String.class, "index-description");

  @Create
  public abstract NTFile createAvatar();

  @Create
  public abstract ProfileXpEntity createXp();
  
  @Create
  public abstract ActivityProfileEntity createActivityProfile();

  public List<String> getProperty(String key) {
    return getProperties().get(key);
  }

  public void setProperty(String key, List<String> value) {
    getProperties().put(key, value);
  }
  
  public String getPropertyFirst(String key) {
    List<String> value = getProperties().get(key);
    return (value.size() > 0 ? value.get(0) : null);
  }
}