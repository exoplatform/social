/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.social.common.jcr.filter;

import org.exoplatform.social.common.jcr.filter.FilterLiteral.FilterOption;

public class FieldLiteral<T>  {

  private final Class<T> type;
  private final String name;

  public FieldLiteral(final Class<T> type, final String name) {

    if (type == null) {
      throw new NullPointerException();
    }

    if (name == null) {
      throw new NullPointerException();
    }

    this.type = type;
    this.name = name;
  }

  public Class<T> getType() {
    return type;
  }

  public String getName() {
    return name;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    
    FieldLiteral<?> other = null;
    if (obj instanceof FieldLiteral) {
      other = (FieldLiteral<?>) obj;
    } else {
      return false;
    }
    
    return this.name.equals(other.name) && this.type.equals(other.type);
  }

}