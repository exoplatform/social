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

import org.chromattic.api.annotations.Id;
import org.chromattic.api.annotations.Name;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;
import org.exoplatform.social.core.storage.query.PropertyLiteralExpression;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
@PrimaryType(name = "soc:spacedefinition")
public abstract class SpaceEntity {

  @Id
  public abstract String getId();
  
  @Name
  public abstract String getName();
  public abstract void setName(String name);

  /**
   * The list of applications with portlet Id, application name, and its state (installed, activated, deactivated).
   */
  @Property(name = "soc:app")
  public abstract String getApp();
  public abstract void setApp(String app);
  public static final PropertyLiteralExpression<String> app =
      new PropertyLiteralExpression<String>(String.class, "soc:app");

  /**
   * The space name.
   */
  @Property(name = "soc:name")
  public abstract String getPrettyName();
  public abstract void setPrettyName(String prettyName);
  public static final PropertyLiteralExpression<String> name =
      new PropertyLiteralExpression<String>(String.class, "soc:name");

  /**
   * The display name of a space.
   */
  @Property(name = "soc:displayName")
  public abstract String getDisplayName();
  public abstract void setDisplayName(String displayName);
  public static final PropertyLiteralExpression<String> displayName =
      new PropertyLiteralExpression<String>(String.class, "soc:displayName");

  /**
   * The space registration status: open, validation, and close.
   */
  @Property(name = "soc:registration")
  public abstract String getRegistration();
  public abstract void setRegistration(String registration);
  public static final PropertyLiteralExpression<String> registration =
    new PropertyLiteralExpression<String>(String.class, "soc:registration");

  /**
   * The description of a space.
   */
  @Property(name = "soc:description")
  public abstract String getDescription();
  public abstract void setDescription(String description);
  public static final PropertyLiteralExpression<String> description =
      new PropertyLiteralExpression<String>(String.class, "soc:description");

  /**
   * The last time when the avatar is updated.
   */
  @Property(name = "soc:avatarLastUpdated")
  public abstract Long getAvatarLastUpdated();
  public abstract void setAvatarLastUpdated(Long avatarLastUpdated);

  /**
   * The type of space which is used to run in the Classic or WebOS mode.
   */
  @Property(name = "soc:type")
  public abstract String getType();
  public abstract void setType(String type);

  /**
   * The space visibility: public, private, and hidden.
   */
  @Property(name = "soc:visibility")
  public abstract String getVisibility();
  public abstract void setVisibility(String visibility);
  public static final PropertyLiteralExpression<String> visibility =
    new PropertyLiteralExpression<String>(String.class, "soc:visibility");

  /**
   * The space priority level that is used to sort spaces in the spaces list. It contains three values: 1, 2 and 3. The smaller value has the higher priority level.
   */
  @Property(name = "soc:priority")
  public abstract String getPriority();
  public abstract void setPriority(String priority);

  /**
   * The group associated with the corresponding space.
   */
  @Property(name = "soc:groupId")
  public abstract String getGroupId();
  public abstract void setGroupId(String groupId);
  public static final PropertyLiteralExpression<String> groupId =
      new PropertyLiteralExpression<String>(String.class, "soc:groupId");

  /**
   * The link to access a space.
   */
  @Property(name = "soc:url")
  public abstract String getURL();
  public abstract void setURL(String url);
  public static final PropertyLiteralExpression<String> url =
      new PropertyLiteralExpression<String>(String.class, "soc:url");

  /**
   * The list of users which are members of a space.
   */
  @Property(name = "soc:membersId")
  public abstract String[] getMembersId();
  public abstract void setMembersId(String[] membersId);
  public static final PropertyLiteralExpression<String> membersId =
      new PropertyLiteralExpression<String>(String.class, "soc:membersId");

  /**
   * The list of users who are pending for validation to join a space.
   */
  @Property(name = "soc:pendingMembersId")
  public abstract String[] getPendingMembersId();
  public abstract void setPendingMembersId(String[] pendingMembersId);
  public static final PropertyLiteralExpression<String> pendingMembersId =
      new PropertyLiteralExpression<String>(String.class, "soc:pendingMembersId");

  /**
   * The list of users who are invited to join a space.
   */
  @Property(name = "soc:invitedMembersId")
  public abstract String[] getInvitedMembersId();
  public abstract void setInvitedMembersId(String[] invitedMembersId);
  public static final PropertyLiteralExpression<String> invitedMembersId =
      new PropertyLiteralExpression<String>(String.class, "soc:invitedMembersId");

  /**
   * The list of users who are managers of a space.
   */
  @Property(name = "soc:managerMembersId")
  public abstract String[] getManagerMembersId();
  public abstract void setManagerMembersId(String[] managerMembersId);
  public static final PropertyLiteralExpression<String> managerMembersId =
      new PropertyLiteralExpression<String>(String.class, "soc:managerMembersId");

  /**
   * The created time
   */
  @Property(name = "soc:createdTime")
  public abstract Long getCreatedTime();
  public abstract void setCreatedTime(Long createdTime);
  public static final PropertyLiteralExpression<Long> createdTime =
      new PropertyLiteralExpression<Long>(Long.class, "soc:createdTime");

}
