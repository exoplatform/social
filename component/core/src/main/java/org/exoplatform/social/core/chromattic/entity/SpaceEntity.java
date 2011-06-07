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

  @Property(name = "soc:app")
  public abstract String getApp();
  public abstract void setApp(String app);

  @Property(name = "soc:name")
  public abstract String getPrettyName();
  public abstract void setPrettyName(String prettyName);
  public static final PropertyLiteralExpression<String> name =
      new PropertyLiteralExpression<String>(String.class, "soc:displayName");

  @Property(name = "soc:displayName")
  public abstract String getDisplayName();
  public abstract void setDisplayName(String displayName);
  public static final PropertyLiteralExpression<String> displayName =
      new PropertyLiteralExpression<String>(String.class, "soc:displayName");

  @Property(name = "soc:registration")
  public abstract String getRegistration();
  public abstract void setRegistration(String registration);

  @Property(name = "soc:description")
  public abstract String getDescription();
  public abstract void setDescription(String description);
  public static final PropertyLiteralExpression<String> description =
      new PropertyLiteralExpression<String>(String.class, "soc:description");

  @Property(name = "soc:type")
  public abstract String getType();
  public abstract void setType(String type);

  @Property(name = "soc:visibility")
  public abstract String getVisibility();
  public abstract void setVisibility(String visibility);

  @Property(name = "soc:priority")
  public abstract String getPriority();
  public abstract void setPriority(String priority);

  @Property(name = "soc:groupId")
  public abstract String getGroupId();
  public abstract void setGroupId(String groupId);
  public static final PropertyLiteralExpression<String> groupId =
      new PropertyLiteralExpression<String>(String.class, "soc:groupId");

  @Property(name = "soc:url")
  public abstract String getURL();
  public abstract void setURL(String url);
  public static final PropertyLiteralExpression<String> url =
      new PropertyLiteralExpression<String>(String.class, "soc:url");

  @Property(name = "soc:membersId")
  public abstract String[] getMembersId();
  public abstract void setMembersId(String[] membersId);
  public static final PropertyLiteralExpression<String> membersId =
      new PropertyLiteralExpression<String>(String.class, "soc:membersId");

  @Property(name = "soc:pendingMembersId")
  public abstract String[] getPendingMembersId();
  public abstract void setPendingMembersId(String[] pendingMembersId);
  public static final PropertyLiteralExpression<String> pendingMembersId =
      new PropertyLiteralExpression<String>(String.class, "soc:pendingMembersId");

  @Property(name = "soc:invitedMembersId")
  public abstract String[] getInvitedMembersId();
  public abstract void setInvitedMembersId(String[] invitedMembersId);
  public static final PropertyLiteralExpression<String> invitedMembersId =
      new PropertyLiteralExpression<String>(String.class, "soc:invitedMembersId");

  @Property(name = "soc:managerMembersId")
  public abstract String[] getManagerMembersId();
  public abstract void setManagerMembersId(String[] managerMembersId);
  public static final PropertyLiteralExpression<String> managerMembersId =
      new PropertyLiteralExpression<String>(String.class, "soc:managerMembersId");

}
