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
package org.exoplatform.social.common.lifecycle;

import org.exoplatform.container.component.ComponentPlugin;

/**
 * A listener to follow the lifecycle.
 * 
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public interface LifeCycleListener<E extends LifeCycleEvent<?, ?>> extends ComponentPlugin {

  default String getName() {
    return null;
  }

  default void setName(String s) {
  }

  default String getDescription() {
    return null;
  }

  default void setDescription(String s) {
  }
}
