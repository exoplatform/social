/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.social.core.space.spi;

import org.exoplatform.social.common.lifecycle.LifeCycleEvent;
import org.exoplatform.social.core.space.model.Space;



/**
 * An event fired at different stages of the lifecycle of a space.
 *
 * @see {@link SpaceLifeCycleListener}
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public class SpaceLifeCycleEvent  extends LifeCycleEvent<String,Space>{

  public enum Type {
    SPACE_CREATED, SPACE_REMOVED, APP_ADDED, APP_REMOVED, APP_ACTIVATED, APP_DEACTIVATED, JOINED, LEFT, GRANTED_LEAD, REVOKED_LEAD
  };

  /**
   * Type of event
   */
  protected Type   type;


  public SpaceLifeCycleEvent(Space space, String target, Type eventType) {
    super(target, space);
    this.type = eventType;
  }

  public Type getType() {
    return type;
  }

  /**
   * space where the event occurs
   */
  public Space getSpace() {
    return payload;
  }

  /**
   * ID of the target of the event. May be an application or user ID
   */
  public String getTarget() {
    return source;
  }

  public String toString() {
    return source + ":" + type + "@" + payload.getName();
  }

}
