/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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

import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.FormattedBy;
import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.Name;
import org.chromattic.api.annotations.NamingPrefix;
import org.chromattic.api.annotations.OneToOne;
import org.chromattic.api.annotations.Owner;
import org.chromattic.api.annotations.Path;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.ext.format.BaseEncodingObjectFormatter;

@PrimaryType(name="soc:streamsdefinition")
@FormattedBy(BaseEncodingObjectFormatter.class)
@NamingPrefix("soc")
public abstract class StreamsEntity {
  
  @Name
  public abstract String getName();

  @Path
  public abstract String getPath();

  /**
   * Store all connection activities in the all stream of an identity.
   */
  @MappedBy("soc:connections")
  @OneToOne
  @Owner
  public abstract ActivityRefListEntity getConnections();
  public abstract void setConnections(ActivityRefListEntity activityRefListEntity);
  
  /**
   * Store all my spaces activities in the all stream of an identity.
   */
  @MappedBy("soc:myspaces")
  @OneToOne
  @Owner
  public abstract ActivityRefListEntity getMySpaces();
  public abstract void setMySpaces(ActivityRefListEntity activityRefListEntity);
  
  /**
   * Store space activities stream on space stream.
   */
  @MappedBy("soc:space")
  @OneToOne
  @Owner
  public abstract ActivityRefListEntity getSpace();
  public abstract void setSpace(ActivityRefListEntity activityRefListEntity);
  
  /**
   * Store all the activities in the my stream of an identity.
   */
  @MappedBy("soc:owner")
  @OneToOne
  @Owner
  public abstract ActivityRefListEntity getOwner();
  public abstract void setOwner(ActivityRefListEntity activityRefListEntity);
  

  /**
   * Store all activities in the all stream of an identity.
   */
  @MappedBy("soc:all")
  @OneToOne
  @Owner
  public abstract ActivityRefListEntity getAll();
  public abstract void setAll(ActivityRefListEntity activityRefListEntity);
  
  @Create
  public abstract ActivityRefListEntity createStream(String name);
  
  /**
   * Creates All Stream node with node's name = "soc:all"
   * @return
   */
  public ActivityRefListEntity createAllStream() {
    return createStream("soc:all");
  }
  
  /**
   * Creates Owner Stream node with node's name = "soc:owner"
   * @return
   */
  public ActivityRefListEntity createOwnerStream() {
    return createStream("soc:owner");
  }
  
  /**
   * Creates Space Stream node with node's name = "soc:space"
   * @return
   */
  public ActivityRefListEntity createSpaceStream() {
    return createStream("soc:space");
  }
  
  /**
   * Creates My Spaces Stream node with node's name = "soc:myspaces"
   * @return
   */
  public ActivityRefListEntity createMySpacesStream() {
    return createStream("soc:myspaces");
  }
  
  /**
   * Creates Connections Stream node with node's name = "soc:connections"
   * @return
   */
  public ActivityRefListEntity createConnectionsStream() {
    return createStream("soc:connections");
  }
}