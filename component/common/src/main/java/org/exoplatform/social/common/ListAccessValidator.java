/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.social.common;

/**
 * Validates input information of list access.
 * 
 * @since 1.2.0-GA  
 */
public class ListAccessValidator {
  
  /**
   * Validates index of list access.
   * 
   * @param offset Start index.
   * @param limit End index.
   * @param size The size of list.
   * @throws IllegalArgumentException
   */
  public static void validateIndex(int offset, int limit, int size) throws IllegalArgumentException{
    if (offset < 0) {
      throw new IllegalArgumentException("Illegal index: offset must be a positive number.");
    }

    if (limit < 0) {
      throw new IllegalArgumentException("Illegal index: limit must be a positive number.");
    }
    
    if (offset > size) {
      throw new IllegalArgumentException("Illegal index: offset must be smaller than size of list.");
    }
    
  }
}
